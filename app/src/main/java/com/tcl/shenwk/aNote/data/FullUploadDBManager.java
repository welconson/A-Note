package com.tcl.shenwk.aNote.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.tcl.shenwk.aNote.util.Constants;

public class FullUploadDBManager {
    private static final String TAG = "FullUploadDBManager";
    private DBOpenHelper dbOpenHelper;

    public FullUploadDBManager(Context context) {
        this.dbOpenHelper = new DBOpenHelper(context.getDatabasePath(Constants.A_NOTE_DATA_DATABASE_NAME).getAbsolutePath());
    }

    public void syncItemSetting(long updateTime){
        SQLiteDatabase database = dbOpenHelper.getDatabase();
        if(database != null){
            String sql = "update " + DBFieldsName.NOTE_TABLE_NAME + " set " + DBFieldsName.SYNC_ROW_ID + "=" + DBFieldsName.NOTE_ID
                    + "," + DBFieldsName.SYNC_LAST_UPDATE_TIME + "=" + updateTime;
            database.execSQL(sql);
        }
    }
}
