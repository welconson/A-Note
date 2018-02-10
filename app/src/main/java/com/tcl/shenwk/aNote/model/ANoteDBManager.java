package com.tcl.shenwk.aNote.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.tcl.shenwk.aNote.entry.NoteEntry;
import com.tcl.shenwk.aNote.util.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Local database accessing interfaces.
 * Created by shenwk on 2018/2/2.
 */

public class ANoteDBManager {
    private static String TAG = "ANoteDBManager";
    private static ANoteDBManager mInstance = null;
    private ANoteDBOpenHelper mDBHelper;
    interface UpdateFlagTable{
        int UPDATE_NOTE_TITLE = 1;
        int UPDATE_UPDATE_TIMESTAMP = 2;
        int UPDATE_LOCATION_INFO = (1 << 2);
        int UPDATE_HAS_ARCHIVED = (1 << 3);
        int UPDATE_IS_LABELED_DISCARDED = (1 << 4);
        int UPDATE_ALL = UPDATE_NOTE_TITLE | UPDATE_UPDATE_TIMESTAMP | UPDATE_LOCATION_INFO
                | UPDATE_HAS_ARCHIVED | UPDATE_IS_LABELED_DISCARDED;
    }

    private ANoteDBManager(Context context) {
        this.mDBHelper = new ANoteDBOpenHelper(context, Constants.A_NOTE_DATABASE_NAME, null, Constants.DB_VERSION);
    }

    public static ANoteDBManager getInstance(Context context){
        synchronized(ANoteDBManager.class){
            if(mInstance == null)
                mInstance = new ANoteDBManager(context);
        }
        return mInstance;
    }

    public long insertNoteRecord(NoteEntry noteEntry){
        long ret = -1;
        if(noteEntry == null)
            return ret;
        String sql = "insert into " + DBFieldsName.NOTE_TABLE_NAME + " (" + DBFieldsName.NOTE_TITLE + ", "
                + DBFieldsName.NOTE_CONTENT_PATH + ", " + DBFieldsName.CREATE_TIMESTAMP + ", "
                + DBFieldsName.UPDATE_TIMESTAMP + ", " + DBFieldsName.LOCATION_INFO + ", "
                + DBFieldsName.HAS_ARCHIVED + ", " + DBFieldsName.IS_LABELED_DISCARDED + ") "
                + " values(?,?,?,?,?,?,?);";
        SQLiteDatabase database = mDBHelper.getWritableDatabase();
        database.beginTransaction();
        SQLiteStatement statement = database.compileStatement(sql);
        if(noteEntry.getNoteTitle() == null)
            statement.bindNull(1);
        else statement.bindString(1, noteEntry.getNoteTitle());
        if(noteEntry.getNoteContentPath() == null)
            statement.bindNull(2);
        else statement.bindString(2, noteEntry.getNoteContentPath());
        if(noteEntry.getCreateTimestamp() == null)
            statement.bindNull(3);
        else statement.bindString(3, noteEntry.getCreateTimestamp());
        if(noteEntry.getUpdateTimestamp() == null)
            statement.bindNull(4);
        else statement.bindString(4, noteEntry.getUpdateTimestamp());
        if(noteEntry.getLocationInfo() == null)
            statement.bindNull(5);
        else statement.bindString(5, noteEntry.getLocationInfo());
        statement.bindLong(6, noteEntry.getHasArchived());
        statement.bindLong(7, noteEntry.getIsLabeledDiscarded());

        // return value is row id, used to search corresponding note record
        ret = statement.executeInsert();
        database.setTransactionSuccessful();
        database.endTransaction();
        mDBHelper.close();
        return ret;
    }
    /**
     *
     * @param noteEntry     NoteEntry which contained contents need to be updated.
     * @param updateFlags   Tell which field should be updated.
     */
    public void updateNoteRecord(NoteEntry noteEntry, int updateFlags){
        if(noteEntry == null || updateFlags == 0)
            return;
        int ret = -1;
        SQLiteDatabase database = mDBHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        if((updateFlags & UpdateFlagTable.UPDATE_NOTE_TITLE) != 0 && noteEntry.getNoteTitle() != null)
            contentValues.put(DBFieldsName.NOTE_TITLE, noteEntry.getNoteTitle());
        contentValues.put(DBFieldsName.UPDATE_TIMESTAMP, noteEntry.getUpdateTimestamp());
        if((updateFlags & UpdateFlagTable.UPDATE_LOCATION_INFO) != 0 && noteEntry.getLocationInfo() != null)
            contentValues.put(DBFieldsName.LOCATION_INFO, noteEntry.getLocationInfo());
        if((updateFlags & UpdateFlagTable.UPDATE_HAS_ARCHIVED) != 0)
            contentValues.put(DBFieldsName.HAS_ARCHIVED, noteEntry.getHasArchived());
        if((updateFlags & UpdateFlagTable.UPDATE_IS_LABELED_DISCARDED) != 0)
            contentValues.put(DBFieldsName.IS_LABELED_DISCARDED, noteEntry.getIsLabeledDiscarded());
        if (contentValues.size() != 0) {
            ret = database.update(DBFieldsName.NOTE_TABLE_NAME, contentValues,
                    DBFieldsName.NOTE_ID + " = " + noteEntry.getNoteId(), null);
        }
        database.close();
        if(ret == -1) {
            Log.i(TAG, "updateNoteRecord: update execute failed");
        }
        else {
            Log.i(TAG, "updateNoteRecord: update execute successfully");
        }
    }

    public List<NoteEntry> queryAllNotesRecord(){
        List<NoteEntry> noteEntries = new ArrayList<>();
        SQLiteDatabase database = mDBHelper.getReadableDatabase();
        String orderBy = DBFieldsName.NOTE_ID + " DESC";
        Cursor cursor = database.query(DBFieldsName.NOTE_TABLE_NAME, null, null,
                null, null, null, orderBy);
        while(cursor.moveToNext()){
            NoteEntry noteEntry = new NoteEntry();
            noteEntry.setNoteId(cursor.getLong(cursor.getColumnIndex(DBFieldsName.NOTE_ID)));
            noteEntry.setNoteTitle(cursor.getString(cursor.getColumnIndex(DBFieldsName.NOTE_TITLE)));
            noteEntry.setNoteContentPath(cursor.getString(cursor.getColumnIndex(DBFieldsName.NOTE_CONTENT_PATH)));
            noteEntry.setCreateTimestamp(cursor.getString(cursor.getColumnIndex(DBFieldsName.CREATE_TIMESTAMP)));
            noteEntry.setUpdateTimestamp(cursor.getString(cursor.getColumnIndex(DBFieldsName.UPDATE_TIMESTAMP)));
            noteEntry.setLocationInfo(cursor.getString(cursor.getColumnIndex(DBFieldsName.LOCATION_INFO)));
            noteEntry.setHasArchived(cursor.getInt(cursor.getColumnIndex(DBFieldsName.HAS_ARCHIVED)));
            noteEntry.setIsLabeledDiscarded(cursor.getInt(cursor.getColumnIndex(DBFieldsName.IS_LABELED_DISCARDED)));
            noteEntries.add(noteEntry);
        }
        cursor.close();
        database.close();
        return noteEntries;
    }

    public NoteEntry querySingleNoteRecordById(long noteId){
        SQLiteDatabase database = mDBHelper.getReadableDatabase();
        NoteEntry noteEntry = new NoteEntry();
        String whereClause = DBFieldsName.NOTE_ID + " = " + noteId;
        Cursor cursor = database.query(DBFieldsName.NOTE_TABLE_NAME, null, whereClause,
                null, null, null, null);
        if(cursor.moveToNext()){
            noteEntry.setNoteId(cursor.getLong(cursor.getColumnIndex(DBFieldsName.NOTE_ID)));
            noteEntry.setNoteTitle(cursor.getString(cursor.getColumnIndex(DBFieldsName.NOTE_TITLE)));
            noteEntry.setNoteContentPath(cursor.getString(cursor.getColumnIndex(DBFieldsName.NOTE_CONTENT_PATH)));
            noteEntry.setCreateTimestamp(cursor.getString(cursor.getColumnIndex(DBFieldsName.CREATE_TIMESTAMP)));
            noteEntry.setUpdateTimestamp(cursor.getString(cursor.getColumnIndex(DBFieldsName.UPDATE_TIMESTAMP)));
            noteEntry.setLocationInfo(cursor.getString(cursor.getColumnIndex(DBFieldsName.LOCATION_INFO)));
            noteEntry.setHasArchived(cursor.getInt(cursor.getColumnIndex(DBFieldsName.HAS_ARCHIVED)));
            noteEntry.setIsLabeledDiscarded(cursor.getInt(cursor.getColumnIndex(DBFieldsName.IS_LABELED_DISCARDED)));
        }
        cursor.close();
        database.close();
        return noteEntry;
    }

    public void deleteNoteRecord(long noteId){
        if(noteId < 0){
            return;
        }
        SQLiteDatabase database = mDBHelper.getWritableDatabase();
        String whereClause = DBFieldsName.NOTE_ID + " = " + noteId;
        database.delete(DBFieldsName.NOTE_TABLE_NAME, whereClause, null);
        database.close();
    }
}
