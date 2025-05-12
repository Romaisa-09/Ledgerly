package com.example.ledgerly;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class DatabaseHelperTransaction {

    private static final String DATABASE_NAME    = "LedgerlyTransactionDB";
    private static final int    DATABASE_VERSION = 1;

    private static final String TABLE_NAME       = "Transaction_Table";
    private static final String KEY_VENDOR_ID    = "_vendorid";
    private static final String KEY_CUSTOMER_ID  = "_customerid";
    private static final String KEY_ID           = "_id";
    private static final String KEY_NAME         = "_name";
    private static final String KEY_DATE         = "_date";
    private static final String KEY_TIME         = "_time";
    private static final String KEY_SEND         = "_send";
    private static final String KEY_RECEIVE      = "_receive";
    private static final String KEY_AMOUNT       = "_amount";

    private final Context context;
    private CreateDataBase helper;
    private SQLiteDatabase database;

    public DatabaseHelperTransaction(Context context) {
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

    public void insertTransaction(int vendorId, int customerId,
                                  String name, String date, String time,
                                  int send, int receive, int amount) {
        ContentValues cv = new ContentValues();
        cv.put(KEY_VENDOR_ID,   vendorId);
        cv.put(KEY_CUSTOMER_ID, customerId);
        cv.put(KEY_NAME,        name);
        cv.put(KEY_DATE,        date);
        cv.put(KEY_TIME,        time);
        cv.put(KEY_SEND,        send);
        cv.put(KEY_RECEIVE,     receive);
        cv.put(KEY_AMOUNT,      amount);

        long row = database.insert(TABLE_NAME, null, cv);
        Toast.makeText(context,
                row == -1 ? "Transaction not inserted" : "Transaction added (ID=" + row + ")",
                Toast.LENGTH_SHORT).show();
    }

    public void updateTransaction(int id, String name, int amount) {
        ContentValues cv = new ContentValues();
        cv.put(KEY_NAME,   name);
        cv.put(KEY_AMOUNT, amount);

        int count = database.update(
                TABLE_NAME, cv, KEY_ID + "=?", new String[]{ String.valueOf(id) }
        );
        Toast.makeText(context,
                count > 0 ? "Transaction updated" : "Transaction update failed",
                Toast.LENGTH_SHORT).show();
    }

    public void deleteTransaction(int id) {
        int count = database.delete(
                TABLE_NAME, KEY_ID + "=?", new String[]{ String.valueOf(id) }
        );
        Toast.makeText(context,
                count > 0 ? "Transaction deleted" : "Transaction delete failed",
                Toast.LENGTH_SHORT).show();
    }

    public ArrayList<Transaction> readAllTransactions(int customerId) {
        ArrayList<Transaction> list = new ArrayList<>();
        Cursor cursor = database.rawQuery(
                "SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_CUSTOMER_ID + "=?",
                new String[]{ String.valueOf(customerId) }
        );
        if (cursor.moveToFirst()) {
            do {
                Transaction t = new Transaction(
                        cursor.getInt(cursor.getColumnIndex(KEY_VENDOR_ID)),
                        cursor.getInt(cursor.getColumnIndex(KEY_CUSTOMER_ID)),
                        cursor.getInt(cursor.getColumnIndex(KEY_ID)),
                        cursor.getString(cursor.getColumnIndex(KEY_NAME)),
                        cursor.getString(cursor.getColumnIndex(KEY_DATE)),
                        cursor.getString(cursor.getColumnIndex(KEY_TIME)),
                        cursor.getInt(cursor.getColumnIndex(KEY_SEND)),
                        cursor.getInt(cursor.getColumnIndex(KEY_RECEIVE)),
                        cursor.getInt(cursor.getColumnIndex(KEY_AMOUNT))
                );
                list.add(t);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public int getSendValue(int transactionId) {
        return querySingleInt(KEY_SEND, transactionId);
    }

    public int getReceiveValue(int transactionId) {
        return querySingleInt(KEY_RECEIVE, transactionId);
    }

    public int getAmount(int transactionId) {
        return querySingleInt(KEY_AMOUNT, transactionId);
    }

    public String getName(int transactionId) {
        String name = "";
        Cursor c = database.rawQuery(
                "SELECT " + KEY_NAME + " FROM " + TABLE_NAME + " WHERE " + KEY_ID + "=?",
                new String[]{ String.valueOf(transactionId) }
        );
        if (c.moveToFirst()) {
            name = c.getString(c.getColumnIndex(KEY_NAME));
        }
        c.close();
        return name;
    }

    private int querySingleInt(String column, int id) {
        int value = 0;
        Cursor c = database.rawQuery(
                "SELECT " + column + " FROM " + TABLE_NAME + " WHERE " + KEY_ID + "=?",
                new String[]{ String.valueOf(id) }
        );
        if (c.moveToFirst()) {
            value = c.getInt(c.getColumnIndex(column));
        }
        c.close();
        return value;
    }

    public ArrayList<Transaction> readTransactionsWithinDateRange(
            int customerId, String startDate, String endDate) {
        ArrayList<Transaction> list = new ArrayList<>();
        Cursor c = database.rawQuery(
                "SELECT * FROM " + TABLE_NAME +
                        " WHERE " + KEY_CUSTOMER_ID + "=? AND " +
                        KEY_DATE + " BETWEEN ? AND ?",
                new String[]{ String.valueOf(customerId), startDate, endDate }
        );
        if (c.moveToFirst()) {
            do {
                Transaction t = new Transaction(
                        c.getInt(c.getColumnIndex(KEY_VENDOR_ID)),
                        c.getInt(c.getColumnIndex(KEY_CUSTOMER_ID)),
                        c.getInt(c.getColumnIndex(KEY_ID)),
                        c.getString(c.getColumnIndex(KEY_NAME)),
                        c.getString(c.getColumnIndex(KEY_DATE)),
                        c.getString(c.getColumnIndex(KEY_TIME)),
                        c.getInt(c.getColumnIndex(KEY_SEND)),
                        c.getInt(c.getColumnIndex(KEY_RECEIVE)),
                        c.getInt(c.getColumnIndex(KEY_AMOUNT))
                );
                list.add(t);
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }

    private static class CreateDataBase extends SQLiteOpenHelper {
        CreateDataBase(@Nullable Context ctx, @Nullable String name,
                       @Nullable SQLiteDatabase.CursorFactory fac, int ver) {
            super(ctx, name, fac, ver);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(
                    "CREATE TABLE " + TABLE_NAME + " (" +
                            KEY_VENDOR_ID   + " INTEGER NOT NULL, " +
                            KEY_CUSTOMER_ID + " INTEGER NOT NULL, " +
                            KEY_ID          + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            KEY_NAME        + " TEXT NOT NULL, " +
                            KEY_DATE        + " TEXT NOT NULL, " +
                            KEY_TIME        + " TEXT NOT NULL, " +
                            KEY_SEND        + " INTEGER NOT NULL, " +
                            KEY_RECEIVE     + " INTEGER NOT NULL, " +
                            KEY_AMOUNT      + " INTEGER NOT NULL" +
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
