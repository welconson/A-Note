package com.tcl.shenwk.aNote.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.tcl.shenwk.aNote.entity.NoteEntity;
import com.tcl.shenwk.aNote.entity.NoteTagEntity;
import com.tcl.shenwk.aNote.entity.ResourceDataEntity;
import com.tcl.shenwk.aNote.entity.TagRecordEntity;
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

    public long insertNoteRecord(NoteEntity noteEntity){
        long ret = -1;
        if(noteEntity == null)
            return ret;
        String sql = "insert into " + NOTE_TABLE_NAME + " (" + NOTE_TITLE + ", "
                + NOTE_CONTENT_PATH + ", " + CREATE_TIMESTAMP + ", "
                + UPDATE_TIMESTAMP + ", " + LOCATION_INFO + ", "
                + HAS_ARCHIVED + ", " + IS_LABELED_DISCARDED + ") "
                + " values(?,?,?,?,?,?,?);";
        SQLiteDatabase database = mDBHelper.getWritableDatabase();
        database.beginTransaction();
        SQLiteStatement statement = database.compileStatement(sql);
        if(noteEntity.getNoteTitle() == null)
            statement.bindNull(1);
        else statement.bindString(1, noteEntity.getNoteTitle());
        if(noteEntity.getNotePath() == null)
            statement.bindNull(2);
        else statement.bindString(2, noteEntity.getNotePath());
        statement.bindLong(3, noteEntity.getCreateTimestamp());
        statement.bindLong(4, noteEntity.getUpdateTimestamp());
        if(noteEntity.getLocationInfo() == null)
            statement.bindNull(5);
        else statement.bindString(5, noteEntity.getLocationInfo());
        statement.bindLong(6, noteEntity.hasArchived() ? Constants.ARCHIVED : Constants.NOT_ARCHIVED);
        statement.bindLong(7, noteEntity.isLabeledDiscarded() ? Constants.LABELED_DISCARD : Constants.NOTE_LABELED_DISCARD);

        // return value is row id, used to search corresponding note record
        ret = statement.executeInsert();
        database.setTransactionSuccessful();
        database.endTransaction();
        mDBHelper.close();
        return ret;
    }
    /**
     *
     * @param noteEntity     NoteEntity which contained contents need to be updated.
     * @param updateFlags   Tell which field should be updated.
     */
    public void updateNoteRecord(NoteEntity noteEntity, int updateFlags){
        if(noteEntity == null || updateFlags == 0)
            return;
        int ret = -1;
        SQLiteDatabase database = mDBHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        if((updateFlags & UpdateFlagTable.UPDATE_NOTE_TITLE) != 0 && noteEntity.getNoteTitle() != null)
            contentValues.put(NOTE_TITLE, noteEntity.getNoteTitle());
        contentValues.put(UPDATE_TIMESTAMP, noteEntity.getUpdateTimestamp());
        if((updateFlags & UpdateFlagTable.UPDATE_LOCATION_INFO) != 0 && noteEntity.getLocationInfo() != null)
            contentValues.put(LOCATION_INFO, noteEntity.getLocationInfo());
        if((updateFlags & UpdateFlagTable.UPDATE_HAS_ARCHIVED) != 0)
            contentValues.put(HAS_ARCHIVED, noteEntity.hasArchived() ? Constants.ARCHIVED : Constants.NOT_ARCHIVED);
        if((updateFlags & UpdateFlagTable.UPDATE_IS_LABELED_DISCARDED) != 0)
            contentValues.put(IS_LABELED_DISCARDED, noteEntity.isLabeledDiscarded() ? Constants.LABELED_DISCARD : Constants.NOTE_LABELED_DISCARD);
        if (contentValues.size() != 0) {
            ret = database.update(NOTE_TABLE_NAME, contentValues,
                    NOTE_ID + " = " + noteEntity.getNoteId(), null);
        }
        database.close();
        if(ret == -1) {
            Log.i(TAG, "updateNoteRecord: update execute failed");
        }
        else {
            Log.i(TAG, "updateNoteRecord: update execute successfully");
        }
    }

    public List<NoteEntity> queryAllNoteRecord(){
        List<NoteEntity> noteEntries = new ArrayList<>();
        SQLiteDatabase database = mDBHelper.getReadableDatabase();
        String orderBy = NOTE_ID + " DESC";
        Cursor cursor = database.query(NOTE_TABLE_NAME, null, null,
                null, null, null, orderBy);
        while(cursor.moveToNext()){
            NoteEntity noteEntity = new NoteEntity();
            noteEntity.setNoteId(cursor.getLong(cursor.getColumnIndex(NOTE_ID)));
            noteEntity.setNoteTitle(cursor.getString(cursor.getColumnIndex(NOTE_TITLE)));
            noteEntity.setNotePath(cursor.getString(cursor.getColumnIndex(NOTE_CONTENT_PATH)));
            noteEntity.setCreateTimestamp(cursor.getLong(cursor.getColumnIndex(CREATE_TIMESTAMP)));
            noteEntity.setUpdateTimestamp(cursor.getLong(cursor.getColumnIndex(UPDATE_TIMESTAMP)));
            noteEntity.setLocationInfo(cursor.getString(cursor.getColumnIndex(LOCATION_INFO)));
            noteEntity.setHasArchived(cursor.getInt(cursor.getColumnIndex(HAS_ARCHIVED)) == Constants.ARCHIVED);
            noteEntity.setIsLabeledDiscarded(cursor.getInt(cursor.getColumnIndex(IS_LABELED_DISCARDED)) == Constants.LABELED_DISCARD);
            noteEntries.add(noteEntity);
        }
        cursor.close();
        database.close();
        return noteEntries;
    }

    public NoteEntity querySingleNoteRecordById(long noteId){
        SQLiteDatabase database = mDBHelper.getReadableDatabase();
        NoteEntity noteEntity = new NoteEntity();
        String whereClause = NOTE_ID + " = " + noteId;
        Cursor cursor = database.query(NOTE_TABLE_NAME, null, whereClause,
                null, null, null, null);
        if(cursor.moveToNext()){
            noteEntity.setNoteId(cursor.getLong(cursor.getColumnIndex(NOTE_ID)));
            noteEntity.setNoteTitle(cursor.getString(cursor.getColumnIndex(NOTE_TITLE)));
            noteEntity.setNotePath(cursor.getString(cursor.getColumnIndex(NOTE_CONTENT_PATH)));
            noteEntity.setCreateTimestamp(cursor.getLong(cursor.getColumnIndex(CREATE_TIMESTAMP)));
            noteEntity.setUpdateTimestamp(cursor.getLong(cursor.getColumnIndex(UPDATE_TIMESTAMP)));
            noteEntity.setLocationInfo(cursor.getString(cursor.getColumnIndex(LOCATION_INFO)));
            noteEntity.setHasArchived(cursor.getInt(cursor.getColumnIndex(HAS_ARCHIVED)) == Constants.ARCHIVED);
            noteEntity.setIsLabeledDiscarded(cursor.getInt(cursor.getColumnIndex(IS_LABELED_DISCARDED)) == Constants.LABELED_DISCARD);
        }
        cursor.close();
        database.close();
        return noteEntity;
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
    public long insertResourceData(ResourceDataEntity resourceDataEntity){
        long ret = -1;
        if(resourceDataEntity == null)
            return ret;
        String sql = "insert into " + RESOURCE_TABLE_NAME + " ( " + NOTE_ID + ", "
                + RESOURCE_FILE_NAME + ", " + RESOURCE_PATH  + ", " + DATA_TYPE + ", "
                + SPAN_START  +  " ) " + "values(?, ?, ?, ?, ?)";
        Log.i(TAG, "insertResourceData: " + sql);
        SQLiteDatabase database = mDBHelper.getWritableDatabase();
        database.beginTransaction();
        SQLiteStatement statement = database.compileStatement(sql);
        statement.bindLong(1, resourceDataEntity.getNoteId());
        if(resourceDataEntity.getFileName() != null)
            statement.bindString(2, resourceDataEntity.getFileName());
        else statement.bindNull(2);
        if(resourceDataEntity.getPath() != null)
            statement.bindString(3, resourceDataEntity.getPath());
        else statement.bindNull(3);
        statement.bindLong(4, resourceDataEntity.getDataType());
        statement.bindLong(5, resourceDataEntity.getSpanStart());

        ret = statement.executeInsert();
        if(ret == -1){
            Log.i(TAG, "insertResourceData: insert resourceDataEntity error of noteId " + resourceDataEntity.getNoteId());
        }
        else Log.i(TAG, "insertResourceData: insert resourceDataEntity successfully");
        database.setTransactionSuccessful();
        database.endTransaction();
        mDBHelper.close();
        return ret;
    }

    public List<ResourceDataEntity> queryAllResourceDataByNoteId(long noteId){
        List<ResourceDataEntity> resourceDataEntries = new ArrayList<>();
        SQLiteDatabase database = mDBHelper.getReadableDatabase();
        String orderBy = SPAN_START + " ASC";
        String sql = NOTE_ID  + " = " + noteId;
        Cursor cursor = database.query(RESOURCE_TABLE_NAME, null, sql,
                null, null, null, orderBy);
        while(cursor.moveToNext()){
            ResourceDataEntity resourceDataEntity = new ResourceDataEntity();
            resourceDataEntity.setResourceId(cursor.getLong(cursor.getColumnIndex(RESOURCE_ID)));
            resourceDataEntity.setNoteId(cursor.getLong(cursor.getColumnIndex(NOTE_ID)));
            resourceDataEntity.setFileName(cursor.getString(cursor.getColumnIndex(RESOURCE_FILE_NAME)));
            resourceDataEntity.setPath(cursor.getString(cursor.getColumnIndex(RESOURCE_PATH)));
            resourceDataEntity.setDataType(cursor.getInt(cursor.getColumnIndex(DATA_TYPE)));
            resourceDataEntity.setSpanStart(cursor.getInt(cursor.getColumnIndex(SPAN_START)));
            resourceDataEntries.add(resourceDataEntity);
        }
        cursor.close();
        database.close();
        return resourceDataEntries;
    }

    public ResourceDataEntity queryFirstResourceDataByNoteId(long noteId){
        ResourceDataEntity resourceDataEntity = null;
        SQLiteDatabase database = mDBHelper.getReadableDatabase();
        String orderBy = SPAN_START + " ASC";
        String sql = NOTE_ID  + " = " + noteId;
        Cursor cursor = database.query(RESOURCE_TABLE_NAME, null, sql,
                null, null, null, orderBy);
        if(cursor.moveToNext()){
            resourceDataEntity = new ResourceDataEntity();
            resourceDataEntity.setResourceId(cursor.getLong(cursor.getColumnIndex(RESOURCE_ID)));
            resourceDataEntity.setNoteId(cursor.getLong(cursor.getColumnIndex(NOTE_ID)));
            resourceDataEntity.setFileName(cursor.getString(cursor.getColumnIndex(RESOURCE_FILE_NAME)));
            resourceDataEntity.setPath(cursor.getString(cursor.getColumnIndex(RESOURCE_PATH)));
            resourceDataEntity.setDataType(cursor.getInt(cursor.getColumnIndex(DATA_TYPE)));
            resourceDataEntity.setSpanStart(cursor.getInt(cursor.getColumnIndex(SPAN_START)));
        }
        cursor.close();
        database.close();
        return resourceDataEntity;
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

    public void updateResourceData(ResourceDataEntity resourceDataEntity){
        if(resourceDataEntity == null){
            return;
        }
        // There is just a kind of possibility that we need to update the span_start filed.
        SQLiteDatabase database = mDBHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(SPAN_START, resourceDataEntity.getSpanStart());
        if (contentValues.size() != 0) {
            if (database.update(RESOURCE_TABLE_NAME, contentValues,
                    RESOURCE_ID + " = " + resourceDataEntity.getResourceId(), null) == -1) {
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
    public long insertTag(NoteTagEntity noteTagEntity){
        long ret = -1;
        String sql = "insert into " + TAG_TABLE_NAME + " ( " + TAG_NAME + ", "
                + TAG_CREATE_TIMESTAMP + ", " + TAG_ROOT_ID + " ) " + " values(?,?,?)";
        Log.i(TAG, "insertItem: " + sql);
        SQLiteDatabase database = mDBHelper.getWritableDatabase();
        SQLiteStatement statement = database.compileStatement(sql);
        database.beginTransaction();
        if(noteTagEntity.getTagName() != null)
            statement.bindString(1, noteTagEntity.getTagName());
        else statement.bindNull(1);
        statement.bindLong(2, noteTagEntity.getCreateTime());
        statement.bindLong(3, noteTagEntity.getRootTagId());
        ret = statement.executeInsert();
        if(ret == -1)
            Log.i(TAG, "insertItem: insert tag entity error");
        else Log.i(TAG, "insertItem: insert tag successfully");
        database.setTransactionSuccessful();
        database.endTransaction();
        mDBHelper.close();
        return ret;
    }

    public List<NoteTagEntity> queryAllTopTag(){
        List<NoteTagEntity> noteTagEntries = new ArrayList<>();
        SQLiteDatabase database = mDBHelper.getReadableDatabase();
        String selection = TAG_ROOT_ID + " = " + Constants.NO_TAG_ID;
        Cursor cursor = database.query(TAG_TABLE_NAME, null, selection ,
                null, null, null, null);
        while(cursor.moveToNext()){
            NoteTagEntity noteTagEntity = new NoteTagEntity();
            noteTagEntity.setTagId(cursor.getLong(cursor.getColumnIndex(TAG_ID)));
            noteTagEntity.setTagName(cursor.getString(cursor.getColumnIndex(TAG_NAME)));
            noteTagEntity.setCreateTime(cursor.getLong(cursor.getColumnIndex(TAG_CREATE_TIMESTAMP)));
            noteTagEntity.setRootTagId(cursor.getLong(cursor.getColumnIndex(TAG_ROOT_ID)));
            noteTagEntries.add(noteTagEntity);
        }
        cursor.close();
        database.close();
        return noteTagEntries;
    }

    public List<NoteTagEntity> queryAllSubTagByRootTagId(long tagId){
        List<NoteTagEntity> noteTagEntries = new ArrayList<>();
        SQLiteDatabase database = mDBHelper.getReadableDatabase();
        String selection = TAG_ROOT_ID + " = " + tagId;
        Cursor cursor = database.query(TAG_TABLE_NAME, null, selection,
                null, null, null, null);
        while(cursor.moveToNext()){
            NoteTagEntity noteTagEntity = new NoteTagEntity();
            noteTagEntity.setTagId(cursor.getLong(cursor.getColumnIndex(TAG_ID)));
            noteTagEntity.setTagName(cursor.getString(cursor.getColumnIndex(TAG_NAME)));
            noteTagEntity.setCreateTime(cursor.getLong(cursor.getColumnIndex(TAG_CREATE_TIMESTAMP)));
            noteTagEntity.setRootTagId(cursor.getLong(cursor.getColumnIndex(TAG_ROOT_ID)));
            noteTagEntries.add(noteTagEntity);
        }
        cursor.close();
        database.close();
        return noteTagEntries;
    }

    public void updateTag(NoteTagEntity noteTagEntity){
        if(noteTagEntity.getTagId() < 0)
            return;
        SQLiteDatabase database = mDBHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(TAG_NAME, noteTagEntity.getTagName());
        contentValues.put(TAG_ROOT_ID, noteTagEntity.getRootTagId());
        String whereClause = TAG_ID + " = " + noteTagEntity.getTagId();
        if(contentValues.size() != 0){
            if(database.update(TAG_TABLE_NAME, contentValues, whereClause, null) == -1)
                Log.i(TAG, "updateTag: update tag " + noteTagEntity.getTagId() + " failed");
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
    public long insertTagRecord(TagRecordEntity tagRecordEntity){
        long ret = 1;
        SQLiteDatabase database = mDBHelper.getWritableDatabase();
        String sql = "INSERT INTO " + TAG_RECORD_TABLE_NAME + " ( " + TAG_ID + ", "
                + NOTE_ID + ", " + TAG_RECORD_CREATE_TIMESTAMP + " ) " + "VALUES(?,?,?)";
        Log.i(TAG, "insertTagRecord: " + sql);
        SQLiteStatement statement = database.compileStatement(sql);
        database.beginTransaction();
        statement.bindLong(1, tagRecordEntity.getTagId());
        statement.bindLong(2, tagRecordEntity.getNoteId());
        statement.bindLong(3, tagRecordEntity.getCreateTimestamp());
        ret = statement.executeInsert();
        if(ret == -1)
            Log.i(TAG, "insertTagRecord: insert tag record failed");
        else Log.i(TAG, "insertTagRecord: insert tag record successfully");
        database.setTransactionSuccessful();
        database.endTransaction();
        database.close();
        return ret;
    }

    public List<TagRecordEntity> queryAllTagRecordByNoteId(long noteId){
        List<TagRecordEntity> tagRecordEntries = new ArrayList<>();
        SQLiteDatabase database = mDBHelper.getReadableDatabase();
        String selection = NOTE_ID + " = " + noteId;
        Cursor cursor = database.query(TAG_RECORD_TABLE_NAME, null, selection,
                null, null, null, null);
        while(cursor.moveToNext()){
            TagRecordEntity tagRecordEntity = new TagRecordEntity();
            tagRecordEntity.setTagRecordId(cursor.getLong(cursor.getColumnIndex(TAG_RECORD_ID)));
            tagRecordEntity.setTagId(cursor.getLong(cursor.getColumnIndex(TAG_ID)));
            tagRecordEntity.setNoteId(cursor.getLong(cursor.getColumnIndex(NOTE_ID)));
            tagRecordEntity.setCreateTimestamp(cursor.getLong(cursor.getColumnIndex(TAG_RECORD_CREATE_TIMESTAMP)));
            tagRecordEntries.add(tagRecordEntity);
        }
        cursor.close();
        database.close();
        return tagRecordEntries;
    }

    public List<TagRecordEntity> queryTagRecordByTagId(long tagId){
        List<TagRecordEntity> tagRecordEntries = new ArrayList<>();
        SQLiteDatabase database = mDBHelper.getReadableDatabase();
        String selection = TAG_ID + " = " + tagId;
        String orderBy = NOTE_ID + " DESC";
        Cursor cursor = database.query(TAG_RECORD_TABLE_NAME, null, selection,
                null, null, null, orderBy);
        while(cursor.moveToNext()){
            TagRecordEntity tagRecordEntity = new TagRecordEntity();
            tagRecordEntity.setTagRecordId(cursor.getLong(cursor.getColumnIndex(TAG_RECORD_ID)));
            tagRecordEntity.setTagId(cursor.getLong(cursor.getColumnIndex(TAG_ID)));
            tagRecordEntity.setNoteId(cursor.getLong(cursor.getColumnIndex(NOTE_ID)));
            tagRecordEntity.setCreateTimestamp(cursor.getLong(cursor.getColumnIndex(TAG_RECORD_CREATE_TIMESTAMP)));
            tagRecordEntries.add(tagRecordEntity);
        }
        cursor.close();
        database.close();
        return tagRecordEntries;
    }

    public TagRecordEntity queryTagRecordByDoubleId(long tagId, long noteId){
        TagRecordEntity tagRecordEntity = null;
        SQLiteDatabase database = mDBHelper.getReadableDatabase();
        String selection = TAG_ID + " = " + tagId + " and " + NOTE_ID + " = " + noteId;
        String orderBy = NOTE_ID + " DESC";
        Cursor cursor = database.query(TAG_RECORD_TABLE_NAME, null, selection,
                null, null, null, orderBy);
        if(cursor.moveToNext()){
            tagRecordEntity = new TagRecordEntity();
            tagRecordEntity.setTagRecordId(cursor.getLong(cursor.getColumnIndex(TAG_RECORD_ID)));
            tagRecordEntity.setTagId(cursor.getLong(cursor.getColumnIndex(TAG_ID)));
            tagRecordEntity.setNoteId(cursor.getLong(cursor.getColumnIndex(NOTE_ID)));
            tagRecordEntity.setCreateTimestamp(cursor.getLong(cursor.getColumnIndex(TAG_RECORD_CREATE_TIMESTAMP)));
        }
        cursor.close();
        database.close();
        return tagRecordEntity;
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
