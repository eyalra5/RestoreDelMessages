package com.whatsapp.restoredelmsg;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MessageDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "messages.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_MESSAGES = "messages";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_SENDER = "sender";
    public static final String COLUMN_MESSAGE = "message";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_MESSAGES + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_SENDER + " TEXT, " +
                    COLUMN_MESSAGE + " TEXT, " +
                    COLUMN_TIMESTAMP + " INTEGER" +
                    ");";

    public MessageDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        onCreate(db);
    }

    public void insertMessage(String sender, String message) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SENDER, sender);
        values.put(COLUMN_MESSAGE, message);
        values.put(COLUMN_TIMESTAMP, System.currentTimeMillis());
        db.insert(TABLE_MESSAGES, null, values);
        db.close();
    }

    public Cursor getAllMessages() {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(TABLE_MESSAGES, null, null, null, null, null, COLUMN_TIMESTAMP + " DESC");
    }
}
