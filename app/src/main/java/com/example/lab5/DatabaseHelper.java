
package com.example.lab5;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "TodoDB";
    public static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "todos";
    public static final String COL_ID = "_id";
    public static final String COL_MESSAGE = "message";
    public static final String COL_URGENT = "urgent";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_MESSAGE + " TEXT NOT NULL, "
                + COL_URGENT + " INTEGER NOT NULL DEFAULT 0)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db,
                          int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
