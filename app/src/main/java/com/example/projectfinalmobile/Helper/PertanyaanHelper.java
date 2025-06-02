package com.example.projectfinalmobile.Helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.projectfinalmobile.Database.DatabaseContract;
import com.example.projectfinalmobile.Database.DatabaseHelper;

public class PertanyaanHelper {
    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;

    public PertanyaanHelper(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public long insert(ContentValues values) {
        return database.insert(DatabaseContract.Pertanyaan.TABLE_NAME, null, values);
    }

}

