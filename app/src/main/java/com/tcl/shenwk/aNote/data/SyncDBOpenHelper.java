package com.tcl.shenwk.aNote.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static com.tcl.shenwk.aNote.data.DBFieldsName.*;

public class SyncDBOpenHelper extends SQLiteOpenHelper {
    private static final String TAG = "SyncDBOpenHelper";

    public SyncDBOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + SYNC_OPERATION_RECORD_TABLE_NAME + " ("
                + SYNC_OPERATION_RECORD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + SYNC_OPERATION_RECORD_EXIST_INITIAL_STATUS + " INTEGER, "
                + SYNC_OPERATION_RECORD_LAST_OPERATION_TYPE + " INTEGER, "
                + SYNC_OPERATION_RECORD_LOCAL_ROW_ID + " INTEGER, "
                + SYNC_OPERATION_RECORD_TYPE + " INTEGER "
                + ")";
        Log.i(TAG, "onCreate: " + sql);
        db.execSQL(sql);

        sql = "CREATE TABLE " + DELETE_RECORD_TABLE_NAME + "(" + DELETE_RECORD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DELETE_RECORD_TYPE + " INTEGER, " + SYNC_ROW_ID + " INTEGER" + ")";
        Log.i(TAG, "onCreate: " + sql);
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
