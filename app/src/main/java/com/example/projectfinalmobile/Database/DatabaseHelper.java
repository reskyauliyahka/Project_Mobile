package com.example.projectfinalmobile.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static String DATABASE_NAME = "Kuis.db";
    public static int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_USERS = "CREATE TABLE " + DatabaseContract.Users.TABLE_NAME + " (" +
                DatabaseContract.Users._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DatabaseContract.Users.USERNAME + " TEXT, " +
                DatabaseContract.Users.EMAIL + " TEXT, " +
                DatabaseContract.Users.PASSWORD + " TEXT, " +
                DatabaseContract.Users.PROFILE_PICTURE + " TEXT, " +
                DatabaseContract.Users.CREATED_AT + " TEXT, " +
                DatabaseContract.Users.UPDATED_AT + " TEXT)";

        String SQL_CREATE_KUIS = "CREATE TABLE " + DatabaseContract.Kuis.TABLE_NAME + " (" +
                DatabaseContract.Kuis._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DatabaseContract.Kuis.USER_ID + " INTEGER, " +
                DatabaseContract.Kuis.JUDUL + " TEXT, " +
                DatabaseContract.Kuis.KATEGORI + " TEXT, " +
                DatabaseContract.Kuis.TIPE + " TEXT, " +
                DatabaseContract.Kuis.TINGKAT_KESULITAN + " TEXT, " +
                DatabaseContract.Kuis.IMG_URL + " TEXT, " +
                DatabaseContract.Kuis.CREATED_AT + " TEXT, " +
                DatabaseContract.Kuis.UPDATED_AT + " TEXT)";

        String SQL_CREATE_PERTANYAAN = "CREATE TABLE " + DatabaseContract.Pertanyaan.TABLE_NAME + " (" +
                DatabaseContract.Pertanyaan._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DatabaseContract.Pertanyaan.KUIS_ID + " INTEGER, " +
                DatabaseContract.Pertanyaan.PERTANYAAN + " TEXT, " +
                DatabaseContract.Pertanyaan.JAWABAN + " TEXT)";

        String SQL_CREATE_OPSI = "CREATE TABLE " + DatabaseContract.OpsiJawaban.TABLE_NAME + " (" +
                DatabaseContract.OpsiJawaban._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DatabaseContract.OpsiJawaban.PERTANYAAN_ID + " INTEGER, " +
                DatabaseContract.OpsiJawaban.TEKS + " TEXT, " +
                "FOREIGN KEY (" + DatabaseContract.OpsiJawaban.PERTANYAAN_ID + ") REFERENCES " +
                DatabaseContract.Pertanyaan.TABLE_NAME + "(" + DatabaseContract.Pertanyaan._ID + ") ON DELETE CASCADE)";


        String SQL_CREATE_FAVORIT = "CREATE TABLE " + DatabaseContract.Favorit.TABLE_NAME + " (" +
                DatabaseContract.Favorit._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DatabaseContract.Favorit.USER_ID + " INTEGER, " +
                DatabaseContract.Favorit.KUIS_ID + " INTEGER)";

        String SQL_CREATE_AKTIVITAS = "CREATE TABLE " + DatabaseContract.AktivitasKuis.TABLE_NAME + " (" +
                DatabaseContract.AktivitasKuis._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DatabaseContract.AktivitasKuis.USER_ID + " INTEGER, " +
                DatabaseContract.AktivitasKuis.KUIS_ID + " INTEGER, " +
                DatabaseContract.AktivitasKuis.SKOR + " INTEGER, " +
                DatabaseContract.AktivitasKuis.TANGGAL + " TEXT," +
                DatabaseContract.AktivitasKuis.LIST_JAWABAN_USER + " TEXT)";

        db.execSQL(SQL_CREATE_USERS);
        db.execSQL(SQL_CREATE_KUIS);
        db.execSQL(SQL_CREATE_PERTANYAAN);
        db.execSQL(SQL_CREATE_OPSI);
        db.execSQL(SQL_CREATE_FAVORIT);
        db.execSQL(SQL_CREATE_AKTIVITAS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.Users.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.Kuis.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.Pertanyaan.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.OpsiJawaban.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.Favorit.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.AktivitasKuis.TABLE_NAME);
        onCreate(db);
    }

}
