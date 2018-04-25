package com.tcl.shenwk.aNote.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.tcl.shenwk.aNote.entity.SyncItemEntity;
import com.tcl.shenwk.aNote.util.Constants;

import java.util.ArrayList;
import java.util.List;

public class DBOpenHelper {
    private static final String TAG = "DBOpenHelper";
    private SQLiteDatabase database;

    public DBOpenHelper(String dbPath) {
        this.database = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
    }

    public SQLiteDatabase getDatabase(){
        return database;
    }

    public void close(){
        database.close();
    }
}
