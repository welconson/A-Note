package com.tcl.shenwk.aNote.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.tcl.shenwk.aNote.entity.NoteEntity;
import com.tcl.shenwk.aNote.entity.SyncItemEntity;
import com.tcl.shenwk.aNote.util.Constants;

import java.util.ArrayList;
import java.util.List;

public class CompareDBManager {
    private static final String TAG = "CompareDBManager";
    public static final int TABLE_CODE_NOTE = 1;
    public static final int TABLE_CODE_RESOURCE = 2;
    public static final int TABLE_CODE_TAG = 3;
    public static final int TABLE_CODE_TAG_RECORD = 4;

    private DBOpenHelper dbOpenHelper;

    public CompareDBManager(String dbPath) {
        dbOpenHelper = new DBOpenHelper(dbPath);
    }
    public List<SyncItemEntity> queryAllNoteSyncItem(){
        List<SyncItemEntity> syncItemEntities = new ArrayList<>();
        if(dbOpenHelper.getDatabase() != null){
            String []columns = {DBFieldsName.NOTE_ID, DBFieldsName.SYNC_MODIFY_TIME, DBFieldsName.SYNC_LAST_UPDATE_TIME, DBFieldsName.SYNC_ROW_ID};
            String orderBy = DBFieldsName.SYNC_ROW_ID + " DESC ," + DBFieldsName.NOTE_ID + " ASC";
            Cursor cursor = dbOpenHelper.getDatabase().query(
                    DBFieldsName.NOTE_TABLE_NAME,
                    columns,
                    null,
                    null,
                    null,
                    null,
                    orderBy);
            while(cursor.moveToNext()) {
                SyncItemEntity syncItemEntity = new SyncItemEntity();
                syncItemEntity.setItemId(cursor.getLong(cursor.getColumnIndex(DBFieldsName.NOTE_ID)));
                syncItemEntity.setModifyTime(cursor.getLong(cursor.getColumnIndex(DBFieldsName.SYNC_MODIFY_TIME)));
                syncItemEntity.setLastUpdateTime(cursor.getLong(cursor.getColumnIndex(DBFieldsName.SYNC_LAST_UPDATE_TIME)));
                syncItemEntity.setSyncRowId(cursor.getLong(cursor.getColumnIndex(DBFieldsName.SYNC_ROW_ID)));
                syncItemEntities.add(syncItemEntity);
            }
            cursor.close();
            Log.i(TAG, "queryAllNoteSyncItem: " + syncItemEntities.size() + " sync items");
        }else {
            Log.i(TAG, "queryAllNoteSyncItem: database open error");
        }
        return syncItemEntities;
    }

    public long queryMaxSyncRowId(){
        long max = Constants.SYNC_ROW_ID_NO_ID;
        if(dbOpenHelper.getDatabase() != null){
            String sql = "select Max(" + DBFieldsName.SYNC_ROW_ID + ") from " + DBFieldsName.NOTE_TABLE_NAME;
            Log.i(TAG, "queryMaxSyncRowId: " + sql);
            Cursor cursor = dbOpenHelper.getDatabase().rawQuery(sql, null);
            if(cursor.moveToNext()){
                max = cursor.getLong(0);
            }
            cursor.close();
        }
        Log.i(TAG, "queryMaxSyncRowId: maxRowId = " + max);
        return max;
    }

    public void setSyncItem(long noteId, SyncItemEntity syncItemEntity){
        if(dbOpenHelper.getDatabase() != null){
            String whereClause = DBFieldsName.NOTE_ID + "=" + noteId;
            ContentValues contentValues = new ContentValues();
            contentValues.put(DBFieldsName.SYNC_ROW_ID, syncItemEntity.getSyncRowId());
            contentValues.put(DBFieldsName.SYNC_LAST_UPDATE_TIME, syncItemEntity.getLastUpdateTime());
            int ret = dbOpenHelper.getDatabase().update(DBFieldsName.NOTE_TABLE_NAME, contentValues, whereClause, null);
            Log.i(TAG, "setSyncItem: affected row num " + ret);
        }else {
            Log.i(TAG, "setSyncItem: database open error");
        }
    }

    public void close(){
        dbOpenHelper.close();
    }

    public NoteEntity querySingleNoteContentPathById(long noteId) {
        NoteEntity noteEntity = null;
        SQLiteDatabase database = dbOpenHelper.getDatabase();
        if(database != null){
            noteEntity = new NoteEntity();
            String []columns = {DBFieldsName.NOTE_DIR_NAME};
            String selection = DBFieldsName.NOTE_ID + "=" + noteId;
            Cursor cursor = database.query(DBFieldsName.NOTE_TABLE_NAME, columns, selection, null, null, null, null);
            if(cursor.moveToNext())
                noteEntity.setNoteDirName(cursor.getString(cursor.getColumnIndex(DBFieldsName.NOTE_DIR_NAME)));
            cursor.close();
        }
        return noteEntity;
    }

    public NoteEntity querySingleWholeNoteById(long noteId){
        NoteEntity noteEntity = null;
        SQLiteDatabase database = dbOpenHelper.getDatabase();
        if(database != null){
            noteEntity = new NoteEntity();
            String selection = DBFieldsName.NOTE_ID + "=" + noteId;
            Cursor cursor = database.query(DBFieldsName.NOTE_TABLE_NAME, null, selection, null, null, null, null);
            if(cursor.moveToNext()){
                noteEntity.setNoteId(cursor.getLong(cursor.getColumnIndex(DBFieldsName.NOTE_ID)));
                noteEntity.setNoteTitle(cursor.getString(cursor.getColumnIndex(DBFieldsName.NOTE_TITLE)));
                noteEntity.setNoteDirName(cursor.getString(cursor.getColumnIndex(DBFieldsName.NOTE_DIR_NAME)));
                noteEntity.setCreateTimestamp(cursor.getLong(cursor.getColumnIndex(DBFieldsName.CREATE_TIMESTAMP)));
                noteEntity.setSyncModifyTime(cursor.getLong(cursor.getColumnIndex(DBFieldsName.SYNC_MODIFY_TIME)));
                noteEntity.setLocationInfo(cursor.getString(cursor.getColumnIndex(DBFieldsName.LOCATION_INFO)));
                noteEntity.setHasArchived(cursor.getInt(cursor.getColumnIndex(DBFieldsName.HAS_ARCHIVED)) == Constants.ARCHIVED);
                noteEntity.setIsLabeledDiscarded(cursor.getInt(cursor.getColumnIndex(DBFieldsName.IS_LABELED_DISCARDED)) == Constants.LABELED_DISCARD);
            }
            cursor.close();
        }
        return noteEntity;
    }

    public void deleteNote(long noteId){
        SQLiteDatabase database = dbOpenHelper.getDatabase();
        if(database != null){
            String whereClause = DBFieldsName.NOTE_ID + "=" + noteId;
            database.delete(DBFieldsName.NOTE_TABLE_NAME, whereClause, null);
        }
    }

    public void insertSingleWholeNoteById(NoteEntity noteEntity, SyncItemEntity syncItemEntity) {
        SQLiteDatabase database = dbOpenHelper.getDatabase();
        if(database != null){
            ContentValues contentValues = new ContentValues();
            contentValues.put(DBFieldsName.NOTE_TITLE, noteEntity.getNoteTitle());
            contentValues.put(DBFieldsName.NOTE_DIR_NAME, noteEntity.getNoteDirName());
            contentValues.put(DBFieldsName.CREATE_TIMESTAMP, noteEntity.getCreateTimestamp());
            contentValues.put(DBFieldsName.LOCATION_INFO, noteEntity.getLocationInfo());
            contentValues.put(DBFieldsName.HAS_ARCHIVED, noteEntity.hasArchived());
            contentValues.put(DBFieldsName.IS_LABELED_DISCARDED, noteEntity.isLabeledDiscarded());
            contentValues.put(DBFieldsName.SYNC_ROW_ID, syncItemEntity.getSyncRowId());
            contentValues.put(DBFieldsName.SYNC_MODIFY_TIME, syncItemEntity.getModifyTime());
            contentValues.put(DBFieldsName.SYNC_LAST_UPDATE_TIME, syncItemEntity.getLastUpdateTime());
            database.insert(DBFieldsName.NOTE_TABLE_NAME, null, contentValues);
        }
    }

    public void updateWholeNoteById(NoteEntity noteEntity, SyncItemEntity syncItemEntity){
        SQLiteDatabase database = dbOpenHelper.getDatabase();
        if(database != null){
            ContentValues contentValues = new ContentValues();
            contentValues.put(DBFieldsName.NOTE_TITLE, noteEntity.getNoteTitle());
            contentValues.put(DBFieldsName.NOTE_DIR_NAME, noteEntity.getNoteDirName());
            contentValues.put(DBFieldsName.CREATE_TIMESTAMP, noteEntity.getCreateTimestamp());
            contentValues.put(DBFieldsName.LOCATION_INFO, noteEntity.getLocationInfo());
            contentValues.put(DBFieldsName.HAS_ARCHIVED, noteEntity.hasArchived());
            contentValues.put(DBFieldsName.IS_LABELED_DISCARDED, noteEntity.isLabeledDiscarded());
            contentValues.put(DBFieldsName.SYNC_ROW_ID, syncItemEntity.getSyncRowId());
            contentValues.put(DBFieldsName.SYNC_MODIFY_TIME, syncItemEntity.getModifyTime());
            contentValues.put(DBFieldsName.SYNC_LAST_UPDATE_TIME, syncItemEntity.getLastUpdateTime());
            String whereClause = DBFieldsName.SYNC_ROW_ID + "=" + syncItemEntity.getSyncRowId();
            database.update(DBFieldsName.NOTE_TABLE_NAME, contentValues, whereClause, null);
        }
    }

    // when local syncItem is newer than server, lastUpdateTime should be update.
    public void updateSyncItemLastUpdateTime(int tableCode, long syncRowId, long lastUpdateTime){
        SQLiteDatabase database = dbOpenHelper.getDatabase();
        String table;
        switch (tableCode){
            case TABLE_CODE_NOTE:
                table = DBFieldsName.NOTE_TABLE_NAME;
                break;
            case TABLE_CODE_RESOURCE:
                table = DBFieldsName.RESOURCE_TABLE_NAME;
                break;
            case TABLE_CODE_TAG:
                table = DBFieldsName.TAG_TABLE_NAME;
                break;
            case TABLE_CODE_TAG_RECORD:
                table = DBFieldsName.TAG_RECORD_TABLE_NAME;
                break;
            default: table = null;
        }
        if(database != null){
            ContentValues contentValues = new ContentValues();
            contentValues.put(DBFieldsName.SYNC_LAST_UPDATE_TIME, lastUpdateTime);
            String whereClause = DBFieldsName.SYNC_ROW_ID + "=" + syncRowId;
            database.update(table, contentValues, whereClause, null);
        }
    }
}
