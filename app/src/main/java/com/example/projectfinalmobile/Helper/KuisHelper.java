package com.example.projectfinalmobile.Helper;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.example.projectfinalmobile.Database.DatabaseContract;
import com.example.projectfinalmobile.Database.DatabaseHelper;
import com.example.projectfinalmobile.Model.KuisModel;
import com.example.projectfinalmobile.Model.PertanyaanModel;

import java.util.ArrayList;
import java.util.List;

public class KuisHelper {
    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;

    public KuisHelper(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public boolean isKuisExist(String judul) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        boolean exists = false;

        try {
            String query = "SELECT 1 FROM " + DatabaseContract.Kuis.TABLE_NAME + " WHERE "
                    + DatabaseContract.Kuis.JUDUL + " = ?";
            cursor = db.rawQuery(query, new String[]{judul});
            exists = cursor != null && cursor.moveToFirst();
        } finally {
            if (cursor != null) cursor.close();

        }

        return exists;
    }

    public int getIdByTitle(String judul) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        int id = -1;

        try {
            String query = "SELECT " + DatabaseContract.Kuis._ID +
                    " FROM " + DatabaseContract.Kuis.TABLE_NAME +
                    " WHERE " + DatabaseContract.Kuis.JUDUL + " = ?";
            cursor = db.rawQuery(query, new String[]{judul});

            if (cursor != null && cursor.moveToFirst()) {
                id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.Kuis._ID));
            }
        } finally {
            if (cursor != null) cursor.close();

        }

        return id;
    }

    public long insertKuisLengkap(KuisModel kuis, SQLiteDatabase db) {

        ContentValues values = new ContentValues();
        if (kuis.getId() != 0) {
            values.put(DatabaseContract.Kuis._ID, kuis.getId());
        }
        values.put(DatabaseContract.Kuis.JUDUL, kuis.getTitle());
        values.put(DatabaseContract.Kuis.TIPE, kuis.getType());
        values.put(DatabaseContract.Kuis.KATEGORI, kuis.getCategory());
        values.put(DatabaseContract.Kuis.TINGKAT_KESULITAN, kuis.getDifficulty());
        values.put(DatabaseContract.Kuis.IMG_URL, kuis.getId_Image());

        values.put(DatabaseContract.Kuis.USER_ID, kuis.getUserId());


        long kuisId = db.insert(DatabaseContract.Kuis.TABLE_NAME, null, values);

        if (kuisId != -1 && kuis.getQuestions() != null) {
            for (PertanyaanModel pertanyaan : kuis.getQuestions()) {
                ContentValues pertanyaanValues = new ContentValues();
                pertanyaanValues.put(DatabaseContract.Pertanyaan.KUIS_ID, kuisId);
                pertanyaanValues.put(DatabaseContract.Pertanyaan.PERTANYAAN, pertanyaan.getQuestion());
                pertanyaanValues.put(DatabaseContract.Pertanyaan.JAWABAN, pertanyaan.getAnswer());

                long pertanyaanId = db.insert(DatabaseContract.Pertanyaan.TABLE_NAME, null, pertanyaanValues);

                if (pertanyaanId != -1 && pertanyaan.getOptions() != null) {
                    for (String opsi : pertanyaan.getOptions()) {
                        ContentValues opsiValues = new ContentValues();
                        opsiValues.put(DatabaseContract.OpsiJawaban.PERTANYAAN_ID, pertanyaanId);
                        opsiValues.put(DatabaseContract.OpsiJawaban.TEKS, opsi);
                        db.insert(DatabaseContract.OpsiJawaban.TABLE_NAME, null, opsiValues);
                    }
                }
            }
        }

        return kuisId;
    }

    public KuisModel getFullKuisByIdWithFilter(int id, String kategori, String tipe, String tingkat, String keyword) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = DatabaseContract.Kuis._ID + " = ?";
        List<String> args = new ArrayList<>();
        args.add(String.valueOf(id));

        if (kategori != null && !kategori.isEmpty()) {
            selection += " AND " + DatabaseContract.Kuis.KATEGORI + " = ?";
            args.add(kategori);
        }

        if (tipe != null && !tipe.isEmpty()) {
            selection += " AND " + DatabaseContract.Kuis.TIPE + " = ?";
            args.add(tipe);
        }

        if (tingkat != null && !tingkat.isEmpty()) {
            selection += " AND " + DatabaseContract.Kuis.TINGKAT_KESULITAN + " = ?";
            args.add(tingkat);
        }

        if (keyword != null && !keyword.isEmpty()) {
            selection += " AND " + DatabaseContract.Kuis.JUDUL + " LIKE ?";
            args.add("%" + keyword + "%");
        }

        Cursor cursor = db.query(
                DatabaseContract.Kuis.TABLE_NAME,
                null,
                selection,
                args.toArray(new String[0]),
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            KuisModel kuis = new KuisModel();
            kuis.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.Kuis._ID)));
            kuis.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Kuis.JUDUL)));
            kuis.setType(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Kuis.TIPE)));
            kuis.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Kuis.KATEGORI)));
            kuis.setDifficulty(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Kuis.TINGKAT_KESULITAN)));
            kuis.setId_image(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Kuis.IMG_URL)));

            List<PertanyaanModel> pertanyaanList = new ArrayList<>();
            Cursor cursorPertanyaan = db.query(
                    DatabaseContract.Pertanyaan.TABLE_NAME,
                    null,
                    DatabaseContract.Pertanyaan.KUIS_ID + " = ?",
                    new String[]{String.valueOf(kuis.getId())},
                    null, null, null
            );

            if (cursorPertanyaan != null) {
                while (cursorPertanyaan.moveToNext()) {
                    PertanyaanModel pertanyaan = new PertanyaanModel();
                    int pertanyaanId = cursorPertanyaan.getInt(cursorPertanyaan.getColumnIndexOrThrow(DatabaseContract.Pertanyaan._ID));
                    pertanyaan.setKuis_id(pertanyaanId);
                    pertanyaan.setQuestion(cursorPertanyaan.getString(cursorPertanyaan.getColumnIndexOrThrow(DatabaseContract.Pertanyaan.PERTANYAAN)));
                    pertanyaan.setAnswer(cursorPertanyaan.getString(cursorPertanyaan.getColumnIndexOrThrow(DatabaseContract.Pertanyaan.JAWABAN)));

                    List<String> opsiList = new ArrayList<>();
                    Cursor cursorOpsi = db.query(
                            DatabaseContract.OpsiJawaban.TABLE_NAME,
                            null,
                            DatabaseContract.OpsiJawaban.PERTANYAAN_ID + " = ?",
                            new String[]{String.valueOf(pertanyaanId)},
                            null, null, null
                    );

                    if (cursorOpsi != null) {
                        while (cursorOpsi.moveToNext()) {
                            opsiList.add(cursorOpsi.getString(cursorOpsi.getColumnIndexOrThrow(DatabaseContract.OpsiJawaban.TEKS)));
                        }
                        cursorOpsi.close();
                    }

                    pertanyaan.setOptions(opsiList);
                    pertanyaanList.add(pertanyaan);
                }
                cursorPertanyaan.close();
            }

            kuis.setQuestions(pertanyaanList);

            cursor.close();
            return kuis;
        }

        if (cursor != null) cursor.close();
        return null;
    }

    public List<KuisModel> getKuisByUserId(String userId) {
        List<KuisModel> daftarKuis = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                DatabaseContract.Kuis.TABLE_NAME,
                null,
                DatabaseContract.Kuis.USER_ID + " = ?",
                new String[]{userId},
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                KuisModel kuis = new KuisModel();
                int kuisId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.Kuis._ID));
                kuis.setId(kuisId);
                kuis.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Kuis.JUDUL)));
                kuis.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Kuis.KATEGORI)));
                kuis.setType(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Kuis.TIPE)));
                kuis.setDifficulty(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Kuis.TINGKAT_KESULITAN)));
                kuis.setId_image(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Kuis.IMG_URL)));
                kuis.setUserId(userId);

                List<PertanyaanModel> pertanyaanList = new ArrayList<>();
                Cursor cursorPertanyaan = db.query(
                        DatabaseContract.Pertanyaan.TABLE_NAME,
                        null,
                        DatabaseContract.Pertanyaan.KUIS_ID + " = ?",
                        new String[]{String.valueOf(kuisId)},
                        null, null, null
                );

                if (cursorPertanyaan != null && cursorPertanyaan.moveToFirst()) {
                    do {
                        PertanyaanModel pertanyaan = new PertanyaanModel();
                        int pertanyaanId = cursorPertanyaan.getInt(cursorPertanyaan.getColumnIndexOrThrow(DatabaseContract.Pertanyaan._ID));
                        pertanyaan.setKuis_id(pertanyaanId);
                        pertanyaan.setQuestion(cursorPertanyaan.getString(cursorPertanyaan.getColumnIndexOrThrow(DatabaseContract.Pertanyaan.PERTANYAAN)));
                        pertanyaan.setAnswer(cursorPertanyaan.getString(cursorPertanyaan.getColumnIndexOrThrow(DatabaseContract.Pertanyaan.JAWABAN)));

                        List<String> opsiList = new ArrayList<>();
                        Cursor cursorOpsi = db.query(
                                DatabaseContract.OpsiJawaban.TABLE_NAME,
                                null,
                                DatabaseContract.OpsiJawaban.PERTANYAAN_ID + " = ?",
                                new String[]{String.valueOf(pertanyaanId)},
                                null, null, null
                        );

                        if (cursorOpsi != null && cursorOpsi.moveToFirst()) {
                            do {
                                opsiList.add(cursorOpsi.getString(cursorOpsi.getColumnIndexOrThrow(DatabaseContract.OpsiJawaban.TEKS)));
                            } while (cursorOpsi.moveToNext());
                            cursorOpsi.close();
                        }

                        pertanyaan.setOptions(opsiList);
                        pertanyaanList.add(pertanyaan);
                    } while (cursorPertanyaan.moveToNext());
                    cursorPertanyaan.close();
                }

                kuis.setQuestions(pertanyaanList);
                daftarKuis.add(kuis);
            } while (cursor.moveToNext());
            cursor.close();
        }

        return daftarKuis;
    }

    public List<KuisModel> getAllKuis2() {
        List<KuisModel> daftarKuis = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                DatabaseContract.Kuis.TABLE_NAME,
                null,
                null,
                null,
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                KuisModel kuis = new KuisModel();
                int kuisId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.Kuis._ID));
                kuis.setId(kuisId);
                kuis.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Kuis.JUDUL)));
                kuis.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Kuis.KATEGORI)));
                kuis.setType(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Kuis.TIPE)));
                kuis.setDifficulty(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Kuis.TINGKAT_KESULITAN)));
                kuis.setId_image(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Kuis.IMG_URL)));
                kuis.setUserId(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Kuis.USER_ID))); // ambil dari database

                // Ambil pertanyaan untuk kuis ini
                List<PertanyaanModel> pertanyaanList = new ArrayList<>();
                Cursor cursorPertanyaan = db.query(
                        DatabaseContract.Pertanyaan.TABLE_NAME,
                        null,
                        DatabaseContract.Pertanyaan.KUIS_ID + " = ?",
                        new String[]{String.valueOf(kuisId)},
                        null, null, null
                );

                if (cursorPertanyaan != null && cursorPertanyaan.moveToFirst()) {
                    do {
                        PertanyaanModel pertanyaan = new PertanyaanModel();
                        int pertanyaanId = cursorPertanyaan.getInt(cursorPertanyaan.getColumnIndexOrThrow(DatabaseContract.Pertanyaan._ID));
                        pertanyaan.setKuis_id(pertanyaanId);
                        pertanyaan.setQuestion(cursorPertanyaan.getString(cursorPertanyaan.getColumnIndexOrThrow(DatabaseContract.Pertanyaan.PERTANYAAN)));
                        pertanyaan.setAnswer(cursorPertanyaan.getString(cursorPertanyaan.getColumnIndexOrThrow(DatabaseContract.Pertanyaan.JAWABAN)));

                        // Ambil opsi jawaban untuk pertanyaan ini
                        List<String> opsiList = new ArrayList<>();
                        Cursor cursorOpsi = db.query(
                                DatabaseContract.OpsiJawaban.TABLE_NAME,
                                null,
                                DatabaseContract.OpsiJawaban.PERTANYAAN_ID + " = ?",
                                new String[]{String.valueOf(pertanyaanId)},
                                null, null, null
                        );

                        if (cursorOpsi != null && cursorOpsi.moveToFirst()) {
                            do {
                                opsiList.add(cursorOpsi.getString(cursorOpsi.getColumnIndexOrThrow(DatabaseContract.OpsiJawaban.TEKS)));
                            } while (cursorOpsi.moveToNext());
                            cursorOpsi.close();
                        }

                        pertanyaan.setOptions(opsiList);
                        pertanyaanList.add(pertanyaan);
                    } while (cursorPertanyaan.moveToNext());
                    cursorPertanyaan.close();
                }

                kuis.setQuestions(pertanyaanList);
                daftarKuis.add(kuis);
            } while (cursor.moveToNext());
            cursor.close();
        }

        return daftarKuis;
    }

    public boolean deleteFullKuisById(int kuisId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        boolean success = false;

        try {
            // Step 1: Ambil semua pertanyaan dari kuis ini
            Cursor cursorPertanyaan = db.query(
                    DatabaseContract.Pertanyaan.TABLE_NAME,
                    new String[]{DatabaseContract.Pertanyaan._ID},
                    DatabaseContract.Pertanyaan.KUIS_ID + " = ?",
                    new String[]{String.valueOf(kuisId)},
                    null, null, null
            );

            if (cursorPertanyaan != null) {
                while (cursorPertanyaan.moveToNext()) {
                    int pertanyaanId = cursorPertanyaan.getInt(cursorPertanyaan.getColumnIndexOrThrow(DatabaseContract.Pertanyaan._ID));

                    // Step 2: Hapus semua opsi jawaban terkait pertanyaan ini
                    db.delete(DatabaseContract.OpsiJawaban.TABLE_NAME,
                            DatabaseContract.OpsiJawaban.PERTANYAAN_ID + " = ?",
                            new String[]{String.valueOf(pertanyaanId)});
                }
                cursorPertanyaan.close();
            }

            // Step 3: Hapus semua pertanyaan dari kuis ini
            db.delete(DatabaseContract.Pertanyaan.TABLE_NAME,
                    DatabaseContract.Pertanyaan.KUIS_ID + " = ?",
                    new String[]{String.valueOf(kuisId)});

            // Step 4: Hapus kuis dari tabel Kuis
            db.delete(DatabaseContract.Kuis.TABLE_NAME,
                    DatabaseContract.Kuis._ID + " = ?",
                    new String[]{String.valueOf(kuisId)});

            db.setTransactionSuccessful();
            success = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }

        return success;
    }

    public boolean updateKuisLengkap(KuisModel kuis, SQLiteDatabase db) {
        db.beginTransaction();
        boolean success = false;

        try {
            // 1. Update data kuis
            ContentValues values = new ContentValues();
            values.put(DatabaseContract.Kuis.JUDUL, kuis.getTitle());
            values.put(DatabaseContract.Kuis.TIPE, kuis.getType());
            values.put(DatabaseContract.Kuis.KATEGORI, kuis.getCategory());
            values.put(DatabaseContract.Kuis.TINGKAT_KESULITAN, kuis.getDifficulty());
            values.put(DatabaseContract.Kuis.IMG_URL, kuis.getId_Image());
            values.put(DatabaseContract.Kuis.USER_ID, kuis.getUserId());

            int rowsUpdated = db.update(DatabaseContract.Kuis.TABLE_NAME,
                    values,
                    DatabaseContract.Kuis._ID + " = ?",
                    new String[]{String.valueOf(kuis.getId())});

            if (rowsUpdated <= 0) return false;

            // 2. Hapus semua pertanyaan lama dan opsi jawaban terkait
            Cursor cursorPertanyaan = db.query(
                    DatabaseContract.Pertanyaan.TABLE_NAME,
                    new String[]{DatabaseContract.Pertanyaan._ID},
                    DatabaseContract.Pertanyaan.KUIS_ID + " = ?",
                    new String[]{String.valueOf(kuis.getId())},
                    null, null, null
            );

            if (cursorPertanyaan != null) {
                while (cursorPertanyaan.moveToNext()) {
                    int pertanyaanId = cursorPertanyaan.getInt(cursorPertanyaan.getColumnIndexOrThrow(DatabaseContract.Pertanyaan._ID));

                    db.delete(DatabaseContract.OpsiJawaban.TABLE_NAME,
                            DatabaseContract.OpsiJawaban.PERTANYAAN_ID + " = ?",
                            new String[]{String.valueOf(pertanyaanId)});
                }
                cursorPertanyaan.close();
            }

            db.delete(DatabaseContract.Pertanyaan.TABLE_NAME,
                    DatabaseContract.Pertanyaan.KUIS_ID + " = ?",
                    new String[]{String.valueOf(kuis.getId())});

            // 3. Tambahkan pertanyaan dan opsi baru
            if (kuis.getQuestions() != null) {
                for (PertanyaanModel pertanyaan : kuis.getQuestions()) {
                    ContentValues pertanyaanValues = new ContentValues();
                    pertanyaanValues.put(DatabaseContract.Pertanyaan.KUIS_ID, kuis.getId());
                    pertanyaanValues.put(DatabaseContract.Pertanyaan.PERTANYAAN, pertanyaan.getQuestion());
                    pertanyaanValues.put(DatabaseContract.Pertanyaan.JAWABAN, pertanyaan.getAnswer());

                    long pertanyaanId = db.insert(DatabaseContract.Pertanyaan.TABLE_NAME, null, pertanyaanValues);

                    if (pertanyaanId != -1 && pertanyaan.getOptions() != null) {
                        for (String opsi : pertanyaan.getOptions()) {
                            ContentValues opsiValues = new ContentValues();
                            opsiValues.put(DatabaseContract.OpsiJawaban.PERTANYAAN_ID, pertanyaanId);
                            opsiValues.put(DatabaseContract.OpsiJawaban.TEKS, opsi);
                            db.insert(DatabaseContract.OpsiJawaban.TABLE_NAME, null, opsiValues);
                        }
                    }
                }
            }

            db.setTransactionSuccessful();
            success = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }

        return success;
    }
    public SQLiteDatabase getWritableDatabase() {
        return dbHelper.getWritableDatabase();
    }


}
