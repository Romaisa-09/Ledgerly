package com.example.ledgerly;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

import kotlin.Triple;

public class DatabaseHelperVendor {

    private static final String DATABASE_NAME    = "LedgerlyVendorsDB";
    private static final int    DATABASE_VERSION = 1;

    private static final String TABLE_NAME    = "Vendor_Table";
    private static final String KEY_ID        = "_id";
    private static final String KEY_EMAIL     = "_email";
    private static final String KEY_PASSWORD  = "_password";
    private static final String KEY_USERNAME  = "_username";

    private final Context context;
    private CreateDataBase helper;
    private SQLiteDatabase database;

    public DatabaseHelperVendor(Context context) {
        this.context = context;
    }

    public void open() {
        helper   = new CreateDataBase(context, DATABASE_NAME, null, DATABASE_VERSION);
        database = helper.getWritableDatabase();
    }

    public void close() {
        database.close();
        helper.close();
    }

    public void insert(String username, String email, String password) {
        ContentValues cv = new ContentValues();
        cv.put(KEY_USERNAME, username);
        cv.put(KEY_EMAIL,    email);
        cv.put(KEY_PASSWORD, password);

        long rec = database.insert(TABLE_NAME, null, cv);
        Toast.makeText(context,
                        rec == -1
                                ? "Data not inserted"
                                : "Vendor added with ID " + rec,
                        Toast.LENGTH_SHORT)
                .show();
    }

    public Triple<Boolean, Integer, String> isValidUser(String email, String password) {
        // Ensure database is open
        if (database == null || !database.isOpen()) {
            open();
        }

        String[] columns   = { KEY_ID, KEY_USERNAME };
        String selection   = KEY_EMAIL + "=? AND " + KEY_PASSWORD + "=?";
        String[] selectionArgs = { email, password };

        Cursor cursor = database.query(
                TABLE_NAME,
                columns,
                selection,
                selectionArgs,
                null, null, null
        );

        boolean valid = false;
        int userId    = -1;
        String username = null;

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                // Use getColumnIndexOrThrow to catch typos at development time
                int idxId   = cursor.getColumnIndexOrThrow(KEY_ID);
                int idxUser = cursor.getColumnIndexOrThrow(KEY_USERNAME);
                userId   = cursor.getInt(idxId);
                username = cursor.getString(idxUser);
                valid    = true;
            }
            cursor.close();
        }

        return new Triple<>(valid, userId, username);
    }

    private static class CreateDataBase extends SQLiteOpenHelper {
        CreateDataBase(@Nullable Context ctx,
                       @Nullable String name,
                       @Nullable SQLiteDatabase.CursorFactory fac,
                       int ver) {
            super(ctx, name, fac, ver);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(
                    "CREATE TABLE " + TABLE_NAME + " (" +
                            KEY_ID       + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            KEY_EMAIL    + " TEXT NOT NULL, " +
                            KEY_PASSWORD + " TEXT NOT NULL, " +
                            KEY_USERNAME + " TEXT NOT NULL" +
                            ");"
            );
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }
}
