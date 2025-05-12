package com.example.ledgerly;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class DatabaseHelperCustomer {

    private final String DATABASE_NAME = "LedgerlyDB";
    private final int DATABASE_VERSION = 1;

    private final String TABLE_NAME = "Ledgerly_Table";
    private final String KEY_VENDOR_ID = "_vendorid";
    private final String KEY_ID = "_id";
    private final String KEY_NAME = "_name";
    private final String KEY_DATE = "_date";
    private final String KEY_TIME = "_time";
    private final String KEY_REMAINING_AMOUNT = "_remaining_amount";
    private final String KEY_PHONE_NUMBER = "_phone_number";

    private CreateDataBase helper;
    private SQLiteDatabase database;
    private Context context;

    public DatabaseHelperCustomer(Context context) {
        this.context = context;
    }

    public void open() {
        helper = new CreateDataBase(context, DATABASE_NAME, null, DATABASE_VERSION);
        database = helper.getWritableDatabase();
    }

    public void close() {
        database.close();
        helper.close();
    }

    public void insertCustomer(int vendorId, String name, String date, String time, String phoneNumber) {
        ContentValues cv = new ContentValues();
        cv.put(KEY_VENDOR_ID, vendorId);
        cv.put(KEY_NAME, name);
        cv.put(KEY_DATE, date);
        cv.put(KEY_TIME, time);
        cv.put(KEY_PHONE_NUMBER, phoneNumber);

        long rec = database.insert(TABLE_NAME, null, cv);
        Toast.makeText(context,
                rec == -1 ? "Data not inserted" : "Added record ID " + rec,
                Toast.LENGTH_SHORT).show();
    }

    public void updateCustomer(int id, String name, String phone) {
        ContentValues cv = new ContentValues();
        cv.put(KEY_ID, id);
        cv.put(KEY_NAME, name);
        cv.put(KEY_PHONE_NUMBER, phone);

        int rows = database.update(TABLE_NAME, cv, KEY_ID + "=?", new String[]{String.valueOf(id)});
        Toast.makeText(context,
                rows > 0 ? "Customer updated" : "Customer not updated",
                Toast.LENGTH_SHORT).show();
    }

    public void updateCustomerRemainingAmount(int id, int amount) {
        ContentValues cv = new ContentValues();
        cv.put(KEY_ID, id);
        cv.put(KEY_REMAINING_AMOUNT, amount);

        int rows = database.update(TABLE_NAME, cv, KEY_ID + "=?", new String[]{String.valueOf(id)});
        Toast.makeText(context,
                rows > 0 ? "Remaining amount updated" : "Update failed",
                Toast.LENGTH_SHORT).show();
    }

    public void deleteCustomer(int id) {
        int rows = database.delete(TABLE_NAME, KEY_ID + "=?", new String[]{String.valueOf(id)});
        Toast.makeText(context,
                rows > 0 ? "Customer deleted" : "Delete failed",
                Toast.LENGTH_SHORT).show();
    }

    public ArrayList<Customer> readAllCustomers(int vendorId) {
        ArrayList<Customer> list = new ArrayList<>();
        Cursor cursor = database.rawQuery(
                "SELECT * FROM " + TABLE_NAME + " WHERE " + KEY_VENDOR_ID + "=?",
                new String[]{String.valueOf(vendorId)}
        );

        if (cursor.moveToFirst()) {
            do {
                // inside readAllCustomers loop:
                Customer c = new Customer();
                c.setVid(cursor.getInt(cursor.getColumnIndex(KEY_VENDOR_ID)));
                c.setCid(cursor.getInt(cursor.getColumnIndex(KEY_ID)));
                c.setName(cursor.getString(cursor.getColumnIndex(KEY_NAME)));
                c.setDate(cursor.getString(cursor.getColumnIndex(KEY_DATE)));
                c.setTime(cursor.getString(cursor.getColumnIndex(KEY_TIME)));
// fix these two:
                c.setRemainingAmount(cursor.getInt(cursor.getColumnIndex(KEY_REMAINING_AMOUNT)));
                c.setPhoneNumber(cursor.getString(cursor.getColumnIndex(KEY_PHONE_NUMBER)));
                list.add(c);

            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public int getRemainingAmountForCustomer(int customerId) {
        int amt = 0;
        Cursor cursor = database.rawQuery(
                "SELECT " + KEY_REMAINING_AMOUNT + " FROM " + TABLE_NAME + " WHERE " + KEY_ID + "=?",
                new String[]{String.valueOf(customerId)}
        );
        if (cursor.moveToFirst())
            amt = cursor.getInt(cursor.getColumnIndex(KEY_REMAINING_AMOUNT));
        cursor.close();
        return amt;
    }

    public String getPhoneNumber(int customerId) {
        String phone = null;
        Cursor cursor = database.rawQuery(
                "SELECT " + KEY_PHONE_NUMBER + " FROM " + TABLE_NAME + " WHERE " + KEY_ID + "=?",
                new String[]{String.valueOf(customerId)}
        );
        if (cursor.moveToFirst())
            phone = cursor.getString(cursor.getColumnIndex(KEY_PHONE_NUMBER));
        cursor.close();
        return phone;
    }

    private static class CreateDataBase extends SQLiteOpenHelper {
        CreateDataBase(@Nullable Context ctx, @Nullable String name,
                       @Nullable SQLiteDatabase.CursorFactory fac, int ver) {
            super(ctx, name, fac, ver);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String sql = "CREATE TABLE Ledgerly_Table (" +
                    "_vendorid INTEGER NOT NULL," +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "_name TEXT NOT NULL," +
                    "_date TEXT NOT NULL," +
                    "_time TEXT NOT NULL," +
                    "_remaining_amount INTEGER DEFAULT 0," +
                    "_phone_number TEXT NOT NULL" +
                    ");";
            db.execSQL(sql);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
            db.execSQL("DROP TABLE IF EXISTS Ledgerly_Table");
            onCreate(db);
        }
    }
}
