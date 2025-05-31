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

    public long insert(ContentValues values) {
        return database.insert(DatabaseContract.AktivitasKuis.TABLE_NAME, null, values);
    }

    public Cursor getAktivitasByUserId(int userId) {
        return database.query(DatabaseContract.AktivitasKuis.TABLE_NAME,
                null,
                DatabaseContract.AktivitasKuis.USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null, null);
    }
}

