package com.example.projectfinalmobile.Helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.projectfinalmobile.Database.DatabaseContract;
import com.example.projectfinalmobile.Database.DatabaseHelper;

public class FavoritHelper {
    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;

    public FavoritHelper(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public long insert(ContentValues values) {
        return database.insert(DatabaseContract.Favorit.TABLE_NAME, null, values);
    }

    public int deleteFavorit(int userId, int kuisId) {
        return database.delete(
                DatabaseContract.Favorit.TABLE_NAME,
                DatabaseContract.Favorit.USER_ID + "=? AND " + DatabaseContract.Favorit.KUIS_ID + "=?",
                new String[]{String.valueOf(userId), String.valueOf(kuisId)}
        );
    }


    public Cursor getFavoritByUserId(int userId) {
        return database.query(DatabaseContract.Favorit.TABLE_NAME,
                null,
                DatabaseContract.Favorit.USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null, null);
    }

    public long insertFavorit(int userId, int kuisId) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.Favorit.USER_ID, userId);
        values.put(DatabaseContract.Favorit.KUIS_ID, kuisId);
        return insert(values);
    }

    public boolean isFavorit(int userId, int kuisId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        boolean exists = false;

        try {
            String query = "SELECT 1 FROM " + DatabaseContract.Favorit.TABLE_NAME +
                    " WHERE " + DatabaseContract.Favorit.USER_ID + " = ? AND " +
                    DatabaseContract.Favorit.KUIS_ID + " = ?";
            cursor = db.rawQuery(query, new String[]{String.valueOf(userId), String.valueOf(kuisId)});
            exists = cursor != null && cursor.moveToFirst();
        } finally {
            if (cursor != null) cursor.close();
        }

        return exists;
    }

}

