package com.tcl.shenwk.aNote.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static com.tcl.shenwk.aNote.model.DBFieldsName.*;

/**
 * Create, open, update database entity.
 * Created by shenwk on 2018/2/2.
 */

public class ANoteDBOpenHelper extends SQLiteOpenHelper {
    private static String TAG = "ANoteDBOpenHelper";
    public ANoteDBOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + NOTE_TABLE_NAME + " ( " + NOTE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + NOTE_TITLE + " TEXT NOT NULL, " + NOTE_CONTENT_PATH + " TEXT NOT NULL, " + CREATE_TIMESTAMP + " INTEGER, "
                + UPDATE_TIMESTAMP + " INTEGER, " + LOCATION_INFO + " TEXT, " + HAS_ARCHIVED + " INTEGER, "
                + IS_LABELED_DISCARDED + " INTEGER " + " )";
        db.execSQL(sql);
        Log.i(TAG, "onCreate: " + sql);

        sql = "CREATE TABLE " + RESOURCE_TABLE_NAME + " ( " + RESOURCE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + NOTE_ID + " INTEGER, " + RESOURCE_FILE_NAME + " TEXT NOT NULL, " + RESOURCE_PATH + " TEXT NOT NULL, "
                + DATA_TYPE + " INTEGER, " + SPAN_START + " INTEGER " +  " )";
        db.execSQL(sql);
        Log.i(TAG, "onCreate: " + sql);

        sql = "CREATE TABLE " + TAG_TABLE_NAME + " (" + TAG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + NOTE_ID + " INTEGER, " + TAG_NAME + " TEXT NOT NULL, " + TAG_ROOT_ID + " INTEGER, "
                + TAG_CREATE_TIMESTAMP + " INTEGER "+ ")";
        db.execSQL(sql);
        Log.d(TAG, "onCreate: " + sql);

        sql = "CREATE TABLE " + TAG_RECORD_TABLE_NAME + " (" + TAG_RECORD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + TAG_ID + " INTEGER, " + NOTE_ID + " INTEGER, " + TAG_RECORD_CREATE_TIMESTAMP + " INTEGER " + ")";
        db.execSQL(sql);
        Log.d(TAG, "onCreate: " + sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(TAG, "onUpgrade: oldVersion = " + oldVersion + " , newVersion = " + newVersion);
    }
}
