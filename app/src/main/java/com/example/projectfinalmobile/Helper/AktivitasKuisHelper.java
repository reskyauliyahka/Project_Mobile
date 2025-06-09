package com.example.projectfinalmobile.Helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.projectfinalmobile.Database.DatabaseContract;
import com.example.projectfinalmobile.Database.DatabaseHelper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AktivitasKuisHelper {
    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;


    public AktivitasKuisHelper(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public Cursor getAktivitasByUserId(int userId) {
        return database.query(DatabaseContract.AktivitasKuis.TABLE_NAME,
                null,
                DatabaseContract.AktivitasKuis.USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null, null);
    }

    public long insertAktivitasKuis(int userId, int kuisId, int skor, String tanggal, List<String> listJawabanUser, SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.AktivitasKuis.USER_ID, userId);
        values.put(DatabaseContract.AktivitasKuis.KUIS_ID, kuisId);
        values.put(DatabaseContract.AktivitasKuis.SKOR, skor);
        values.put(DatabaseContract.AktivitasKuis.TANGGAL, tanggal);

        // Convert List<String> to JSON string
        Gson gson = new Gson();
        String jsonJawabanUser = gson.toJson(listJawabanUser);
        values.put(DatabaseContract.AktivitasKuis.LIST_JAWABAN_USER, jsonJawabanUser);

        return db.insert(DatabaseContract.AktivitasKuis.TABLE_NAME, null, values);
    }

    public boolean isKuisSudahDikerjakan(int userId, long kuisId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseContract.AktivitasKuis.TABLE_NAME,
                null,
                DatabaseContract.AktivitasKuis.USER_ID + "=? AND " + DatabaseContract.AktivitasKuis.KUIS_ID + "=?",
                new String[]{String.valueOf(userId), String.valueOf(kuisId)},
                null, null, null
        );
        boolean sudahDikerjakan = cursor != null && cursor.getCount() > 0;
        if (cursor != null) cursor.close();
        return sudahDikerjakan;
    }

    public int getSkorByUserIdAndKuisId(int userId, int kuisId) {
        int skor = -1; // default jika tidak ditemukan

        Cursor cursor = database.query(
                DatabaseContract.AktivitasKuis.TABLE_NAME,
                new String[]{DatabaseContract.AktivitasKuis.SKOR},
                DatabaseContract.AktivitasKuis.USER_ID + "=? AND " + DatabaseContract.AktivitasKuis.KUIS_ID + "=?",
                new String[]{String.valueOf(userId), String.valueOf(kuisId)},
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            skor = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.AktivitasKuis.SKOR));
            cursor.close();
        }

        return skor;
    }

    public List<String> getJawabanUser(int userId, int kuisId) {
        List<String> jawabanUser = new ArrayList<>();
        Cursor cursor = database.query(
                DatabaseContract.AktivitasKuis.TABLE_NAME,
                new String[]{DatabaseContract.AktivitasKuis.LIST_JAWABAN_USER},
                DatabaseContract.AktivitasKuis.USER_ID + "=? AND " + DatabaseContract.AktivitasKuis.KUIS_ID + "=?",
                new String[]{String.valueOf(userId), String.valueOf(kuisId)},
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            String jsonJawabanUser = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.AktivitasKuis.LIST_JAWABAN_USER));
            cursor.close();

            // Convert JSON string back to List<String>
            Gson gson = new Gson();
            Type type = new TypeToken<List<String>>(){}.getType();
            jawabanUser = gson.fromJson(jsonJawabanUser, type);
        }

        return jawabanUser;
    }


    public double getRataRataSkorByUserId(int userId) {
        double rataRata = 0;
        int totalSkor = 0;
        int jumlah = 0;

        Cursor cursor = database.query(
                DatabaseContract.AktivitasKuis.TABLE_NAME,
                new String[]{DatabaseContract.AktivitasKuis.SKOR},
                DatabaseContract.AktivitasKuis.USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int skor = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.AktivitasKuis.SKOR));
                totalSkor += skor;
                jumlah++;
            } while (cursor.moveToNext());

            cursor.close();
        }

        if (jumlah > 0) {
            rataRata = (double) totalSkor / jumlah;
        }

        return rataRata;
    }

    public Map<Integer, Map<String, Integer>> getStatistikJawabanByKuisId(int kuisId) {
        Map<Integer, Map<String, Integer>> statistik = new HashMap<>();

        Cursor cursor = database.query(
                DatabaseContract.AktivitasKuis.TABLE_NAME,
                new String[]{DatabaseContract.AktivitasKuis.LIST_JAWABAN_USER},
                DatabaseContract.AktivitasKuis.KUIS_ID + "=?",
                new String[]{String.valueOf(kuisId)},
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<String>>(){}.getType();

            do {
                String jsonJawaban = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.AktivitasKuis.LIST_JAWABAN_USER));
                List<String> jawabanUser = gson.fromJson(jsonJawaban, type);

                for (int i = 0; i < jawabanUser.size(); i++) {
                    String jawaban = jawabanUser.get(i);

                    Map<String, Integer> jawabanMap = statistik.getOrDefault(i, new HashMap<>());
                    int count = jawabanMap.getOrDefault(jawaban, 0);
                    jawabanMap.put(jawaban, count + 1);
                    statistik.put(i, jawabanMap);
                }
            } while (cursor.moveToNext());

            cursor.close();
        }

        return statistik; // key = indeks soal, value = map jawaban A/B/C/D -> jumlah user
    }

    public List<Map<String, Object>> getUserYangMengerjakanKuis(int kuisId) {
        List<Map<String, Object>> result = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT u.username, u.profile_picture, a.skor FROM " +
                DatabaseContract.AktivitasKuis.TABLE_NAME + " a " +
                "JOIN " + DatabaseContract.Users.TABLE_NAME + " u " +
                "ON a." + DatabaseContract.AktivitasKuis.USER_ID + " = u." + DatabaseContract.Users._ID +
                " WHERE a." + DatabaseContract.AktivitasKuis.KUIS_ID + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(kuisId)});

        if (cursor.moveToFirst()) {
            do {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("username", cursor.getString(cursor.getColumnIndexOrThrow("username")));
                userMap.put("skor", cursor.getInt(cursor.getColumnIndexOrThrow("skor")));
                userMap.put("foto_profil", cursor.getString(cursor.getColumnIndexOrThrow("profile_picture")));

                // kamu bisa menambahkan `foto_profil` jika sudah tersedia
                result.add(userMap);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return result;
    }

    public List<Integer> getHistoriSkorByUserId(int userId) {
        List<Integer> skorList = new ArrayList<>();

        Cursor cursor = database.rawQuery(
                "SELECT " + DatabaseContract.AktivitasKuis.SKOR +
                        " FROM " + DatabaseContract.AktivitasKuis.TABLE_NAME +
                        " WHERE " + DatabaseContract.AktivitasKuis.USER_ID + " = ?" +
                        " ORDER BY " + DatabaseContract.AktivitasKuis.TANGGAL + " DESC LIMIT 10",
                new String[]{String.valueOf(userId)}
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int skor = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.AktivitasKuis.SKOR));
                skorList.add(skor);
            } while (cursor.moveToNext());
            cursor.close();
        }

        // Balikkan urutan agar grafik tampil dari yang lama ke yang terbaru
        java.util.Collections.reverse(skorList);

        return skorList;
    }

}

