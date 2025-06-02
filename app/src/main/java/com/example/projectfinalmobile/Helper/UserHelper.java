package com.example.projectfinalmobile.Helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.projectfinalmobile.Database.DatabaseContract;
import com.example.projectfinalmobile.Database.DatabaseHelper;

public class UserHelper {
    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;

    public UserHelper(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        if (database != null && database.isOpen()) {
            database.close();
        }
        dbHelper.close();
    }

    public long insert(ContentValues values) {
        return database.insert(DatabaseContract.Users.TABLE_NAME, null, values);
    }

    public int update(int id, ContentValues values) {
        return database.update(DatabaseContract.Users.TABLE_NAME, values,
                DatabaseContract.Users._ID + "=?", new String[]{String.valueOf(id)});
    }

    public int delete(int id) {
        return database.delete(DatabaseContract.Users.TABLE_NAME,
                DatabaseContract.Users._ID + "=?", new String[]{String.valueOf(id)});
    }



    public Cursor checkUserLogin(String usernameOrEmail, String password) {
        if (database == null || !database.isOpen()) {
            open();
        }

        String query = "SELECT * FROM " + DatabaseContract.Users.TABLE_NAME +
                " WHERE (" + DatabaseContract.Users.USERNAME + "=? OR " +
                DatabaseContract.Users.EMAIL + "=?) AND " +
                DatabaseContract.Users.PASSWORD + "=?";
        return database.rawQuery(query, new String[]{usernameOrEmail, usernameOrEmail, password});
    }

    public boolean isUsernameExists(String username) {
        Cursor cursor = database.query(
                DatabaseContract.Users.TABLE_NAME,
                null,
                DatabaseContract.Users.USERNAME + "=?",
                new String[]{username},
                null, null, null
        );
        boolean exists = (cursor != null && cursor.moveToFirst());
        if (cursor != null) cursor.close();
        return exists;
    }

    public boolean isEmailExists(String email) {
        Cursor cursor = database.query(
                DatabaseContract.Users.TABLE_NAME,
                null,
                DatabaseContract.Users.EMAIL + "=?",
                new String[]{email},
                null, null, null
        );
        boolean exists = (cursor != null && cursor.moveToFirst());
        if (cursor != null) cursor.close();
        return exists;
    }

    public Cursor getUserById(int userId) {
        if (database == null || !database.isOpen()) {
            open();
        }

        return database.query(
                DatabaseContract.Users.TABLE_NAME,
                new String[]{DatabaseContract.Users.USERNAME, DatabaseContract.Users.EMAIL},
                DatabaseContract.Users._ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null, null
        );
    }

}
