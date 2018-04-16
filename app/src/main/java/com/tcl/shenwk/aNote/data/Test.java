package com.tcl.shenwk.aNote.data;

import android.content.ContentResolver;
import android.database.Cursor;
import android.util.Log;

public class Test {
    String TAG = "ANoteProviderTest";
    // A "projection" defines the columns that will be returned for each row
    String[] mProjection =
            {
                    DBFieldsName.NOTE_DIR_NAME,
                    DBFieldsName.NOTE_TITLE
            };

    // Defines a string to contain the selection clause
    String mSelectionClause = null;

    // Initializes an array to contain selection arguments
    String[] mSelectionArgs = {""};



    public void query(ContentResolver contentResolver){
        Cursor cursor = contentResolver.query(ContentProviderConstants.NOTE_TABLE_URI, mProjection, mSelectionClause, null, null);
        if(cursor == null)
            return;
        while(cursor.moveToNext()){
            Log.i(TAG, "query: " + cursor.getString(cursor.getColumnIndex(DBFieldsName.NOTE_DIR_NAME))
                + ", " + cursor.getString(cursor.getColumnIndex(DBFieldsName.NOTE_TITLE)));
        }
        cursor.close();
    }
}
