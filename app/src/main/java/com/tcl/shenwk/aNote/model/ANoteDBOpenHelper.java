package com.tcl.shenwk.aNote.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static com.tcl.shenwk.aNote.model.DBFieldsName.*;

/**
 * Create, open, update database entry.
 * Created by shenwk on 2018/2/2.
 */

public class ANoteDBOpenHelper extends SQLiteOpenHelper {
    public ANoteDBOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + NOTE_TABLE_NAME + " ( " + NOTE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + NOTE_TITLE + " TEXT NOT NULL, " + NOTE_CONTENT_PATH + " TEXT NOT NULL, " + CREATE_TIMESTAMP + " TEXT, "
                + UPDATE_TIMESTAMP + " TEXT, " + LOCATION_INFO + " TEXT, " + HAS_ARCHIVED + " INTEGER, "
                + IS_LABELED_DISCARDED + " INTEGER " + " )";
        db.execSQL(sql);
        Log.i("ANoteDBOpenHelper", "onCreate: " + sql);
        sql = "CREATE TABLE " + MULTIMEDIA_TABLE_NAME + " ( " + MULTIMEDIA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + FILE_NAME + " TEXT NOT NULL, " + FILE_PATH + " TEXT NOT NULL, " + DATA_TYPE + " INTEGER " + " )";
        db.execSQL(sql);
        Log.i("ANoteDBOpenHelper", "onCreate: " + sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
