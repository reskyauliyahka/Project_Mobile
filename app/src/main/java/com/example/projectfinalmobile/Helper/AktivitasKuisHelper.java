package com.example.projectfinalmobile.Helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.projectfinalmobile.Database.DatabaseContract;
import com.example.projectfinalmobile.Database.DatabaseHelper;

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

    public long insertAktivitasKuis(int userId, int kuisId, int skor, String tanggal, SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.AktivitasKuis.USER_ID, userId);
        values.put(DatabaseContract.AktivitasKuis.KUIS_ID, kuisId);
        values.put(DatabaseContract.AktivitasKuis.SKOR, skor);
        values.put(DatabaseContract.AktivitasKuis.TANGGAL, tanggal);

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



}

