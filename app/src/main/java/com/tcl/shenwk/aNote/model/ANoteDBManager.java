package com.tcl.shenwk.aNote.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.tcl.shenwk.aNote.entry.NoteTagEntry;
import com.tcl.shenwk.aNote.entry.ResourceDataEntry;
import com.tcl.shenwk.aNote.entry.NoteEntry;
import com.tcl.shenwk.aNote.entry.TagRecordEntry;
import com.tcl.shenwk.aNote.util.Constants;

import java.util.ArrayList;
import java.util.List;

import static com.tcl.shenwk.aNote.model.DBFieldsName.*;

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
        String sql = "insert into " + NOTE_TABLE_NAME + " (" + NOTE_TITLE + ", "
                + NOTE_CONTENT_PATH + ", " + CREATE_TIMESTAMP + ", "
                + UPDATE_TIMESTAMP + ", " + LOCATION_INFO + ", "
                + HAS_ARCHIVED + ", " + IS_LABELED_DISCARDED + ") "
                + " values(?,?,?,?,?,?,?);";
        SQLiteDatabase database = mDBHelper.getWritableDatabase();
        database.beginTransaction();
        SQLiteStatement statement = database.compileStatement(sql);
        if(noteEntry.getNoteTitle() == null)
            statement.bindNull(1);
        else statement.bindString(1, noteEntry.getNoteTitle());
        if(noteEntry.getNotePath() == null)
            statement.bindNull(2);
        else statement.bindString(2, noteEntry.getNotePath());
        statement.bindLong(3, noteEntry.getCreateTimestamp());
        statement.bindLong(4, noteEntry.getUpdateTimestamp());
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
            contentValues.put(NOTE_TITLE, noteEntry.getNoteTitle());
        contentValues.put(UPDATE_TIMESTAMP, noteEntry.getUpdateTimestamp());
        if((updateFlags & UpdateFlagTable.UPDATE_LOCATION_INFO) != 0 && noteEntry.getLocationInfo() != null)
            contentValues.put(LOCATION_INFO, noteEntry.getLocationInfo());
        if((updateFlags & UpdateFlagTable.UPDATE_HAS_ARCHIVED) != 0)
            contentValues.put(HAS_ARCHIVED, noteEntry.getHasArchived());
        if((updateFlags & UpdateFlagTable.UPDATE_IS_LABELED_DISCARDED) != 0)
            contentValues.put(IS_LABELED_DISCARDED, noteEntry.getIsLabeledDiscarded());
        if (contentValues.size() != 0) {
            ret = database.update(NOTE_TABLE_NAME, contentValues,
                    NOTE_ID + " = " + noteEntry.getNoteId(), null);
        }
        database.close();
        if(ret == -1) {
            Log.i(TAG, "updateNoteRecord: update execute failed");
        }
        else {
            Log.i(TAG, "updateNoteRecord: update execute successfully");
        }
    }

    public List<NoteEntry> queryAllNoteRecord(){
        List<NoteEntry> noteEntries = new ArrayList<>();
        SQLiteDatabase database = mDBHelper.getReadableDatabase();
        String orderBy = NOTE_ID + " DESC";
        Cursor cursor = database.query(NOTE_TABLE_NAME, null, null,
                null, null, null, orderBy);
        while(cursor.moveToNext()){
            NoteEntry noteEntry = new NoteEntry();
            noteEntry.setNoteId(cursor.getLong(cursor.getColumnIndex(NOTE_ID)));
            noteEntry.setNoteTitle(cursor.getString(cursor.getColumnIndex(NOTE_TITLE)));
            noteEntry.setNotePath(cursor.getString(cursor.getColumnIndex(NOTE_CONTENT_PATH)));
            noteEntry.setCreateTimestamp(cursor.getLong(cursor.getColumnIndex(CREATE_TIMESTAMP)));
            noteEntry.setUpdateTimestamp(cursor.getLong(cursor.getColumnIndex(UPDATE_TIMESTAMP)));
            noteEntry.setLocationInfo(cursor.getString(cursor.getColumnIndex(LOCATION_INFO)));
            noteEntry.setHasArchived(cursor.getInt(cursor.getColumnIndex(HAS_ARCHIVED)));
            noteEntry.setIsLabeledDiscarded(cursor.getInt(cursor.getColumnIndex(IS_LABELED_DISCARDED)));
            noteEntries.add(noteEntry);
        }
        cursor.close();
        database.close();
        return noteEntries;
    }

    public NoteEntry querySingleNoteRecordById(long noteId){
        SQLiteDatabase database = mDBHelper.getReadableDatabase();
        NoteEntry noteEntry = new NoteEntry();
        String whereClause = NOTE_ID + " = " + noteId;
        Cursor cursor = database.query(NOTE_TABLE_NAME, null, whereClause,
                null, null, null, null);
        if(cursor.moveToNext()){
            noteEntry.setNoteId(cursor.getLong(cursor.getColumnIndex(NOTE_ID)));
            noteEntry.setNoteTitle(cursor.getString(cursor.getColumnIndex(NOTE_TITLE)));
            noteEntry.setNotePath(cursor.getString(cursor.getColumnIndex(NOTE_CONTENT_PATH)));
            noteEntry.setCreateTimestamp(cursor.getLong(cursor.getColumnIndex(CREATE_TIMESTAMP)));
            noteEntry.setUpdateTimestamp(cursor.getLong(cursor.getColumnIndex(UPDATE_TIMESTAMP)));
            noteEntry.setLocationInfo(cursor.getString(cursor.getColumnIndex(LOCATION_INFO)));
            noteEntry.setHasArchived(cursor.getInt(cursor.getColumnIndex(HAS_ARCHIVED)));
            noteEntry.setIsLabeledDiscarded(cursor.getInt(cursor.getColumnIndex(IS_LABELED_DISCARDED)));
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
        String whereClause = NOTE_ID + " = " + noteId;
        database.delete(NOTE_TABLE_NAME, whereClause, null);
        database.close();
    }

    /**
     * Resource data record database operation
     */
    public long insertResourceData(ResourceDataEntry resourceDataEntry){
        long ret = -1;
        if(resourceDataEntry == null)
            return ret;
        String sql = "insert into " + RESOURCE_TABLE_NAME + " ( " + NOTE_ID + ", "
                + RESOURCE_FILE_NAME + ", " + RESOURCE_PATH  + ", " + DATA_TYPE + ", "
                + SPAN_START  +  " ) " + "values(?, ?, ?, ?, ?)";
        Log.i(TAG, "insertResourceData: " + sql);
        SQLiteDatabase database = mDBHelper.getWritableDatabase();
        database.beginTransaction();
        SQLiteStatement statement = database.compileStatement(sql);
        statement.bindLong(1, resourceDataEntry.getNoteId());
        if(resourceDataEntry.getFileName() != null)
            statement.bindString(2, resourceDataEntry.getFileName());
        else statement.bindNull(2);
        if(resourceDataEntry.getPath() != null)
            statement.bindString(3, resourceDataEntry.getPath());
        else statement.bindNull(3);
        statement.bindLong(4, resourceDataEntry.getDataType());
        statement.bindLong(5, resourceDataEntry.getSpanStart());

        ret = statement.executeInsert();
        if(ret == -1){
            Log.i(TAG, "insertResourceData: insert resourceDataEntry error of noteId " + resourceDataEntry.getNoteId());
        }
        else Log.i(TAG, "insertResourceData: insert resourceDataEntry successfully");
        database.setTransactionSuccessful();
        database.endTransaction();
        mDBHelper.close();
        return ret;
    }

    public List<ResourceDataEntry> queryAllResourceDataByNoteId(long noteId){
        List<ResourceDataEntry> resourceDataEntries = new ArrayList<>();
        SQLiteDatabase database = mDBHelper.getReadableDatabase();
        String orderBy = SPAN_START + " ASC";
        String sql = NOTE_ID  + " = " + noteId;
        Cursor cursor = database.query(RESOURCE_TABLE_NAME, null, sql,
                null, null, null, orderBy);
        while(cursor.moveToNext()){
            ResourceDataEntry resourceDataEntry = new ResourceDataEntry();
            resourceDataEntry.setResourceId(cursor.getLong(cursor.getColumnIndex(RESOURCE_ID)));
            resourceDataEntry.setNoteId(cursor.getLong(cursor.getColumnIndex(NOTE_ID)));
            resourceDataEntry.setFileName(cursor.getString(cursor.getColumnIndex(RESOURCE_FILE_NAME)));
            resourceDataEntry.setPath(cursor.getString(cursor.getColumnIndex(RESOURCE_PATH)));
            resourceDataEntry.setDataType(cursor.getInt(cursor.getColumnIndex(DATA_TYPE)));
            resourceDataEntry.setSpanStart(cursor.getInt(cursor.getColumnIndex(SPAN_START)));
            resourceDataEntries.add(resourceDataEntry);
        }
        cursor.close();
        database.close();
        return resourceDataEntries;
    }

    public ResourceDataEntry queryFirstResourceDataByNoteId(long noteId){
        ResourceDataEntry resourceDataEntry = null;
        SQLiteDatabase database = mDBHelper.getReadableDatabase();
        String orderBy = SPAN_START + " ASC";
        String sql = NOTE_ID  + " = " + noteId;
        Cursor cursor = database.query(RESOURCE_TABLE_NAME, null, sql,
                null, null, null, orderBy);
        if(cursor.moveToNext()){
            resourceDataEntry = new ResourceDataEntry();
            resourceDataEntry.setResourceId(cursor.getLong(cursor.getColumnIndex(RESOURCE_ID)));
            resourceDataEntry.setNoteId(cursor.getLong(cursor.getColumnIndex(NOTE_ID)));
            resourceDataEntry.setFileName(cursor.getString(cursor.getColumnIndex(RESOURCE_FILE_NAME)));
            resourceDataEntry.setPath(cursor.getString(cursor.getColumnIndex(RESOURCE_PATH)));
            resourceDataEntry.setDataType(cursor.getInt(cursor.getColumnIndex(DATA_TYPE)));
            resourceDataEntry.setSpanStart(cursor.getInt(cursor.getColumnIndex(SPAN_START)));
        }
        cursor.close();
        database.close();
        return resourceDataEntry;
    }

    public void deleteResourceData(long resourceId){
        if(resourceId < 0){
            return;
        }
        SQLiteDatabase database = mDBHelper.getWritableDatabase();
        String whereClause = RESOURCE_ID + " = " + resourceId;
        database.delete(RESOURCE_TABLE_NAME, whereClause, null);
        database.close();
    }

    public void deleteResourceDataByNoteId(long noteId){
        if(noteId < 0){
            return;
        }
        SQLiteDatabase database = mDBHelper.getWritableDatabase();
        String whereClause = NOTE_ID + " = " + noteId;
        database.delete(RESOURCE_TABLE_NAME, whereClause, null);
        database.close();
    }

    public void updateResourceData(ResourceDataEntry resourceDataEntry){
        if(resourceDataEntry == null){
            return;
        }
        // There is just a kind of possibility that we need to update the span_start filed.
        SQLiteDatabase database = mDBHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(SPAN_START, resourceDataEntry.getSpanStart());
        if (contentValues.size() != 0) {
            if (database.update(RESOURCE_TABLE_NAME, contentValues,
                    RESOURCE_ID + " = " + resourceDataEntry.getResourceId(), null) == -1) {
                Log.i(TAG, "updateResourceData: resource data record update error");
            } else {
                Log.i(TAG, "updateResourceData: resource data record update successfully");
            }
        }
        database.close();
    }

    /**
     * TAG table database operations.
     */
    public long insertTag(NoteTagEntry noteTagEntry){
        long ret = -1;
        String sql = "insert into " + TAG_TABLE_NAME + " ( " + TAG_NAME + ", "
                + TAG_CREATE_TIMESTAMP + ", " + TAG_ROOT_ID + " ) " + " values(?,?,?)";
        Log.i(TAG, "insertTag: " + sql);
        SQLiteDatabase database = mDBHelper.getWritableDatabase();
        SQLiteStatement statement = database.compileStatement(sql);
        database.beginTransaction();
        if(noteTagEntry.getTagName() != null)
            statement.bindString(1, noteTagEntry.getTagName());
        else statement.bindNull(1);
        statement.bindLong(2, noteTagEntry.getCreateTime());
        statement.bindLong(3, noteTagEntry.getRootTagId());
        ret = statement.executeInsert();
        if(ret == -1)
            Log.i(TAG, "insertTag: insert tag entry error");
        else Log.i(TAG, "insertTag: insert tag successfully");
        database.setTransactionSuccessful();
        database.endTransaction();
        mDBHelper.close();
        return ret;
    }

    public List<NoteTagEntry> queryAllTag(){
        List<NoteTagEntry> noteTagEntries = new ArrayList<>();
        SQLiteDatabase database = mDBHelper.getReadableDatabase();
        Cursor cursor = database.query(TAG_TABLE_NAME, null, null,
                null, null, null, null);
        while(cursor.moveToNext()){
            NoteTagEntry noteTagEntry = new NoteTagEntry();
            noteTagEntry.setTagId(cursor.getLong(cursor.getColumnIndex(TAG_ID)));
            noteTagEntry.setTagName(cursor.getString(cursor.getColumnIndex(TAG_NAME)));
            noteTagEntry.setCreateTime(cursor.getLong(cursor.getColumnIndex(TAG_CREATE_TIMESTAMP)));
            noteTagEntry.setRootTagId(cursor.getLong(cursor.getColumnIndex(TAG_ROOT_ID)));
            noteTagEntries.add(noteTagEntry);
        }
        cursor.close();
        database.close();
        return noteTagEntries;
    }

    public List<NoteTagEntry> queryAllSubTagByTagId(long tagId){
        List<NoteTagEntry> noteTagEntries = new ArrayList<>();
        SQLiteDatabase database = mDBHelper.getReadableDatabase();
        String selection = TAG_ROOT_ID + " = " + tagId;
        Cursor cursor = database.query(TAG_TABLE_NAME, null, selection,
                null, null, null, null);
        while(cursor.moveToNext()){
            NoteTagEntry noteTagEntry = new NoteTagEntry();
            noteTagEntry.setTagId(cursor.getLong(cursor.getColumnIndex(TAG_ID)));
            noteTagEntry.setTagName(cursor.getString(cursor.getColumnIndex(TAG_NAME)));
            noteTagEntry.setCreateTime(cursor.getLong(cursor.getColumnIndex(TAG_CREATE_TIMESTAMP)));
            noteTagEntry.setRootTagId(cursor.getLong(cursor.getColumnIndex(TAG_ROOT_ID)));
            noteTagEntries.add(noteTagEntry);
        }
        cursor.close();
        database.close();
        return noteTagEntries;
    }

    public void updateTag(NoteTagEntry noteTagEntry){
        if(noteTagEntry.getTagId() < 0)
            return;
        SQLiteDatabase database = mDBHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(TAG_NAME, noteTagEntry.getTagName());
        contentValues.put(TAG_ROOT_ID, noteTagEntry.getRootTagId());
        String whereClause = TAG_ID + " = " + noteTagEntry.getTagId();
        if(contentValues.size() != 0){
            if(database.update(TAG_TABLE_NAME, contentValues, whereClause, null) == -1)
                Log.i(TAG, "updateTag: update tag " + noteTagEntry.getTagId() + " failed");
            else
                Log.i(TAG, "updateTag: update tag successfully");
        }
        database.close();
    }

    public void deleteTag(long tagId){
        if(tagId < 0)
            return;
        SQLiteDatabase database = mDBHelper.getWritableDatabase();
        String whereClause = TAG_ID + " = " + tagId;
        database.delete(TAG_TABLE_NAME, whereClause, null);
        database.close();
    }

    /**
     * TAG_NOTE_RECORD database operations.
     */
    public long insertTagRecord(TagRecordEntry tagRecordEntry){
        long ret = 1;
        SQLiteDatabase database = mDBHelper.getWritableDatabase();
        String sql = "INSERT INTO " + TAG_RECORD_TABLE_NAME + " ( " + TAG_ID + ", "
                + NOTE_ID + ", " + TAG_RECORD_CREATE_TIMESTAMP + " ) " + "VALUES(?,?,?)";
        Log.i(TAG, "insertTagRecord: " + sql);
        SQLiteStatement statement = database.compileStatement(sql);
        database.beginTransaction();
        statement.bindLong(1, tagRecordEntry.getTagId());
        statement.bindLong(2, tagRecordEntry.getNoteId());
        statement.bindLong(3, tagRecordEntry.getCreateTimestamp());
        ret = statement.executeInsert();
        if(ret == -1)
            Log.i(TAG, "insertTagRecord: insert tag record failed");
        else Log.i(TAG, "insertTagRecord: insert tag record successfully");
        database.setTransactionSuccessful();
        database.endTransaction();
        database.close();
        return ret;
    }

    public List<TagRecordEntry> queryAllTagRecordByNoteId(long noteId){
        List<TagRecordEntry> tagRecordEntries = new ArrayList<>();
        SQLiteDatabase database = mDBHelper.getReadableDatabase();
        String selection = NOTE_ID + " = " + noteId;
        Cursor cursor = database.query(TAG_RECORD_TABLE_NAME, null, selection,
                null, null, null, null);
        while(cursor.moveToNext()){
            TagRecordEntry tagRecordEntry = new TagRecordEntry();
            tagRecordEntry.setTagRecordId(cursor.getLong(cursor.getColumnIndex(TAG_RECORD_ID)));
            tagRecordEntry.setTagId(cursor.getLong(cursor.getColumnIndex(TAG_ID)));
            tagRecordEntry.setNoteId(cursor.getLong(cursor.getColumnIndex(NOTE_ID)));
            tagRecordEntry.setCreateTimestamp(cursor.getLong(cursor.getColumnIndex(TAG_RECORD_CREATE_TIMESTAMP)));
            tagRecordEntries.add(tagRecordEntry);
        }
        cursor.close();
        database.close();
        return tagRecordEntries;
    }

    public List<TagRecordEntry> queryTagRecordByTagId(long tagId){
        List<TagRecordEntry> tagRecordEntries = new ArrayList<>();
        SQLiteDatabase database = mDBHelper.getReadableDatabase();
        String selection = TAG_ID + " = " + tagId;
        String orderBy = NOTE_ID + " DESC";
        Cursor cursor = database.query(TAG_RECORD_TABLE_NAME, null, selection,
                null, null, null, orderBy);
        while(cursor.moveToNext()){
            TagRecordEntry tagRecordEntry = new TagRecordEntry();
            tagRecordEntry.setTagRecordId(cursor.getLong(cursor.getColumnIndex(TAG_RECORD_ID)));
            tagRecordEntry.setTagId(cursor.getLong(cursor.getColumnIndex(TAG_ID)));
            tagRecordEntry.setNoteId(cursor.getLong(cursor.getColumnIndex(NOTE_ID)));
            tagRecordEntry.setCreateTimestamp(cursor.getLong(cursor.getColumnIndex(TAG_RECORD_CREATE_TIMESTAMP)));
            tagRecordEntries.add(tagRecordEntry);
        }
        cursor.close();
        database.close();
        return tagRecordEntries;
    }

    public void deleteTagRecord(long tagRecordId){
        if(tagRecordId < 0)
            return;
        SQLiteDatabase database = mDBHelper.getWritableDatabase();
        String whereClause = TAG_RECORD_ID + " = " + tagRecordId;
        Log.i(TAG, "deleteTagRecord: " + whereClause);
        database.delete(TAG_RECORD_TABLE_NAME, whereClause, null);
        database.close();
    }

    public void deleteTagRecordByNoteId(long noteId){
        if(noteId < 0)
            return;
        SQLiteDatabase database = mDBHelper.getWritableDatabase();
        String whereClause = NOTE_ID + " = " + noteId;
        Log.i(TAG, "deleteTagRecord: " + whereClause);
        database.delete(TAG_RECORD_TABLE_NAME, whereClause, null);
        database.close();
    }
}
