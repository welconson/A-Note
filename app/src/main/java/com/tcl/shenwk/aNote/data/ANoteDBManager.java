package com.tcl.shenwk.aNote.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.tcl.shenwk.aNote.entity.NoteEntity;
import com.tcl.shenwk.aNote.entity.NoteTagEntity;
import com.tcl.shenwk.aNote.entity.ResourceDataEntity;
import com.tcl.shenwk.aNote.entity.TagRecordEntity;
import com.tcl.shenwk.aNote.util.Constants;

import java.util.ArrayList;
import java.util.List;

import static com.tcl.shenwk.aNote.data.DBFieldsName.*;

/**
 * Local database accessing interfaces.
 * Created by shenwk on 2018/2/2.
 */

public class ANoteDBManager {
    private static String TAG = "ANoteDBManager";
    private static ANoteDBManager mInstance = null;
    public interface UpdateFlagTable{
        int UPDATE_NOTE_TITLE = 1;
        int UPDATE_UPDATE_TIMESTAMP = 2;
        int UPDATE_LOCATION_INFO = (1 << 2);
        int UPDATE_HAS_ARCHIVED = (1 << 3);
        int UPDATE_IS_LABELED_DISCARDED = (1 << 4);
        int UPDATE_ALL = UPDATE_NOTE_TITLE | UPDATE_UPDATE_TIMESTAMP | UPDATE_LOCATION_INFO
                | UPDATE_HAS_ARCHIVED | UPDATE_IS_LABELED_DISCARDED;
    }
    private ContentResolver contentResolver;

    private ANoteDBManager(Context context) {
        this.contentResolver = context.getContentResolver();
        ANoteContentObserver aNoteContentObserver = new ANoteContentObserver(new Handler(Looper.getMainLooper()), TAG, context);
        this.contentResolver.registerContentObserver(Uri.parse(ContentProviderConstants.BASE_URI), true, aNoteContentObserver);
    }

    public static ANoteDBManager getInstance(Context context){
        synchronized(ANoteDBManager.class){
            if(mInstance == null)
                mInstance = new ANoteDBManager(context.getApplicationContext());
        }
        return mInstance;
    }

    public long insertNoteRecord(NoteEntity noteEntity){
        long ret = -1;
        if(noteEntity == null)
            return ret;
        ContentValues contentValues = new ContentValues();
        contentValues.put(NOTE_TITLE, noteEntity.getNoteTitle());
        contentValues.put(NOTE_DIR_NAME, noteEntity.getNoteDirName());
        contentValues.put(CREATE_TIMESTAMP, noteEntity.getCreateTimestamp());
        contentValues.put(UPDATE_TIMESTAMP, noteEntity.getUpdateTimestamp());
        contentValues.put(LOCATION_INFO, noteEntity.getLocationInfo());
        contentValues.put(HAS_ARCHIVED, noteEntity.hasArchived());
        contentValues.put(IS_LABELED_DISCARDED, noteEntity.isLabeledDiscarded());
        Uri uri = contentResolver.insert(ContentProviderConstants.NOTE_TABLE_URI, contentValues);
        if (uri == null || (ret = ContentUris.parseId(uri)) == -1) {
            Log.i(TAG, "insertNoteRecord: insert into table " + NOTE_TABLE_NAME + " error");
        }
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
        if (contentValues.size() > 1) {
            ret = contentResolver.update(ContentProviderConstants.NOTE_TABLE_URI, contentValues
                    , DBFieldsName.NOTE_ID + " = " + noteEntity.getNoteId(), null);
        }
        if(ret == -1) {
            Log.i(TAG, "updateNoteRecord: update execute failed");
        }
        else {
            Log.i(TAG, "updateNoteRecord: update affected " + ret + " rows");
        }
    }

    public List<NoteEntity> queryAllNoteRecord(){
        List<NoteEntity> noteEntries = new ArrayList<>();
        String orderBy = NOTE_ID + " DESC";
        Cursor cursor = contentResolver.query(ContentProviderConstants.NOTE_TABLE_URI, null, null, null, orderBy);
        if(cursor == null)
            return noteEntries;
        while(cursor.moveToNext()){
            NoteEntity noteEntity = new NoteEntity();
            noteEntity.setNoteId(cursor.getLong(cursor.getColumnIndex(NOTE_ID)));
            noteEntity.setNoteTitle(cursor.getString(cursor.getColumnIndex(NOTE_TITLE)));
            noteEntity.setNoteDirName(cursor.getString(cursor.getColumnIndex(NOTE_DIR_NAME)));
            noteEntity.setCreateTimestamp(cursor.getLong(cursor.getColumnIndex(CREATE_TIMESTAMP)));
            noteEntity.setUpdateTimestamp(cursor.getLong(cursor.getColumnIndex(UPDATE_TIMESTAMP)));
            noteEntity.setLocationInfo(cursor.getString(cursor.getColumnIndex(LOCATION_INFO)));
            noteEntity.setHasArchived(cursor.getInt(cursor.getColumnIndex(HAS_ARCHIVED)) == Constants.ARCHIVED);
            noteEntity.setIsLabeledDiscarded(cursor.getInt(cursor.getColumnIndex(IS_LABELED_DISCARDED)) == Constants.LABELED_DISCARD);
            noteEntries.add(noteEntity);
        }
        cursor.close();
        return noteEntries;
    }

    public NoteEntity querySingleNoteRecordById(long noteId){
        NoteEntity noteEntity = new NoteEntity();
        String whereClause = NOTE_ID + " = " + noteId;
        Cursor cursor = contentResolver.query(ContentProviderConstants.NOTE_TABLE_URI,
                null, whereClause, null, null);
        if(cursor == null)
            return null;
        if(cursor.moveToNext()){
            noteEntity.setNoteId(cursor.getLong(cursor.getColumnIndex(NOTE_ID)));
            noteEntity.setNoteTitle(cursor.getString(cursor.getColumnIndex(NOTE_TITLE)));
            noteEntity.setNoteDirName(cursor.getString(cursor.getColumnIndex(NOTE_DIR_NAME)));
            noteEntity.setCreateTimestamp(cursor.getLong(cursor.getColumnIndex(CREATE_TIMESTAMP)));
            noteEntity.setUpdateTimestamp(cursor.getLong(cursor.getColumnIndex(UPDATE_TIMESTAMP)));
            noteEntity.setLocationInfo(cursor.getString(cursor.getColumnIndex(LOCATION_INFO)));
            noteEntity.setHasArchived(cursor.getInt(cursor.getColumnIndex(HAS_ARCHIVED)) == Constants.ARCHIVED);
            noteEntity.setIsLabeledDiscarded(cursor.getInt(cursor.getColumnIndex(IS_LABELED_DISCARDED)) == Constants.LABELED_DISCARD);
        }
        cursor.close();
        return noteEntity;
    }

    public void deleteNoteRecord(long noteId){
        if(noteId < 0){
            return;
        }
        String whereClause = NOTE_ID + " = " + noteId;
        int ret = contentResolver.delete(ContentProviderConstants.NOTE_TABLE_URI,
                whereClause, null);
        if(ret == -1){
            Log.d(TAG, "deleteNoteRecord: delete error on noteId " + noteId);
        }
    }

    /**
     * Resource data record database operation
     */
    public long insertResourceData(ResourceDataEntity resourceDataEntity){
        long ret = -1;
        if(resourceDataEntity == null)
            return ret;
        ContentValues contentValues = new ContentValues();
        contentValues.put(NOTE_ID, resourceDataEntity.getNoteId());
        contentValues.put(RESOURCE_FILE_NAME, resourceDataEntity.getFileName());
        contentValues.put(RESOURCE_PATH, resourceDataEntity.getResourceRelativePath());
        contentValues.put(DATA_TYPE, resourceDataEntity.getDataType());
        contentValues.put(SPAN_START, resourceDataEntity.getSpanStart());
        Uri uri = contentResolver.insert(ContentProviderConstants.RESOURCE_TABLE_URI, contentValues);
        ret = ContentUris.parseId(uri);
        if(ret == -1){
            Log.i(TAG, "insertResourceData: insert into table " + RESOURCE_TABLE_NAME + " error");
        }
        return ret;
    }

    public List<ResourceDataEntity> queryAllResourceDataByNoteId(long noteId){
        List<ResourceDataEntity> resourceDataEntries = new ArrayList<>();
        String orderBy = SPAN_START + " ASC";
        String whereClause = NOTE_ID  + " = " + noteId;
        Cursor cursor = contentResolver.query(ContentProviderConstants.RESOURCE_TABLE_URI, null, whereClause,
                null, orderBy);
        if(cursor == null) {
            Log.i(TAG, "queryAllResourceDataByNoteId: content provider return error");
            return resourceDataEntries;
        }
        while(cursor.moveToNext()){
            ResourceDataEntity resourceDataEntity = new ResourceDataEntity();
            resourceDataEntity.setResourceId(cursor.getLong(cursor.getColumnIndex(RESOURCE_ID)));
            resourceDataEntity.setNoteId(cursor.getLong(cursor.getColumnIndex(NOTE_ID)));
            resourceDataEntity.setFileName(cursor.getString(cursor.getColumnIndex(RESOURCE_FILE_NAME)));
            resourceDataEntity.setResourceRelativePath(cursor.getString(cursor.getColumnIndex(RESOURCE_PATH)));
            resourceDataEntity.setDataType(cursor.getInt(cursor.getColumnIndex(DATA_TYPE)));
            resourceDataEntity.setSpanStart(cursor.getInt(cursor.getColumnIndex(SPAN_START)));
            resourceDataEntries.add(resourceDataEntity);
        }
        cursor.close();
        return resourceDataEntries;
    }

    public ResourceDataEntity queryFirstResourceDataByNoteId(long noteId){
        ResourceDataEntity resourceDataEntity = null;
        String orderBy = SPAN_START + " ASC";
        String whereClause = NOTE_ID  + " = " + noteId;
        Cursor cursor = contentResolver.query(ContentProviderConstants.RESOURCE_TABLE_URI, null, whereClause,
                null, orderBy);
        if(cursor == null){
            Log.i(TAG, "queryAllResourceDataByNoteId: content provider return error");
            return null;
        }
        if(cursor.moveToNext()){
            resourceDataEntity = new ResourceDataEntity();
            resourceDataEntity.setResourceId(cursor.getLong(cursor.getColumnIndex(RESOURCE_ID)));
            resourceDataEntity.setNoteId(cursor.getLong(cursor.getColumnIndex(NOTE_ID)));
            resourceDataEntity.setFileName(cursor.getString(cursor.getColumnIndex(RESOURCE_FILE_NAME)));
            resourceDataEntity.setResourceRelativePath(cursor.getString(cursor.getColumnIndex(RESOURCE_PATH)));
            resourceDataEntity.setDataType(cursor.getInt(cursor.getColumnIndex(DATA_TYPE)));
            resourceDataEntity.setSpanStart(cursor.getInt(cursor.getColumnIndex(SPAN_START)));
        }
        cursor.close();
        return resourceDataEntity;
    }

    public void deleteResourceData(long resourceId){
        if(resourceId < 0){
            return;
        }
        String whereClause = RESOURCE_ID + " = " + resourceId;
        int ret = contentResolver.delete(ContentProviderConstants.RESOURCE_TABLE_URI, whereClause, null);
        if(ret == -1){
            Log.i(TAG, "deleteResourceData: delete resource file error on resourceId " + resourceId);
        }
    }

    public void deleteResourceDataByNoteId(long noteId){
        if(noteId < 0) {
            return;
        }
        String whereClause = NOTE_ID + " = " + noteId;
        int ret = contentResolver.delete(ContentProviderConstants.RESOURCE_TABLE_URI, whereClause, null);
        if(ret == -1){
            Log.i(TAG, "deleteResourceData: delete resource file error on noteId " + noteId);
        }
    }

    public void updateResourceData(ResourceDataEntity resourceDataEntity){
        if(resourceDataEntity == null){
            return;
        }
        // There is just a kind of possibility that we need to update the span_start field.
        String whereClause = RESOURCE_ID + " = " + resourceDataEntity.getResourceId();
        ContentValues contentValues = new ContentValues();
        contentValues.put(SPAN_START, resourceDataEntity.getSpanStart());
        int ret = contentResolver.update(ContentProviderConstants.RESOURCE_TABLE_URI, contentValues, whereClause, null);
        Log.i(TAG, "updateResourceData: update rows = " + ret);
    }

    /**
     * TAG table database operations.
     */
    public long insertTag(NoteTagEntity noteTagEntity){
        long ret = -1;
        ContentValues contentValues = new ContentValues();
        contentValues.put(TAG_NAME, noteTagEntity.getTagName());
        contentValues.put(TAG_CREATE_TIMESTAMP, noteTagEntity.getCreateTime());
        contentValues.put(TAG_ROOT_ID, noteTagEntity.getRootTagId());
        Uri uri = contentResolver.insert(ContentProviderConstants.TAG_TABLE_URI, contentValues);
        if(uri == null || (ret = ContentUris.parseId(uri)) == -1){
            Log.i(TAG, "insertTag: insert into table " + TAG_TABLE_NAME + " error");
        }
        return ret;
    }

    public List<NoteTagEntity> queryAllTopTag(){
        List<NoteTagEntity> noteTagEntries = new ArrayList<>();
        String selection = TAG_ROOT_ID + " = " + Constants.NO_TAG_ID;
        Cursor cursor = contentResolver.query(ContentProviderConstants.TAG_TABLE_URI,
                null, selection, null, null);
        if(cursor == null){
            Log.i(TAG, "queryAllTopTag: content provider return error");
            return noteTagEntries;
        }
        while(cursor.moveToNext()){
            NoteTagEntity noteTagEntity = new NoteTagEntity();
            noteTagEntity.setTagId(cursor.getLong(cursor.getColumnIndex(TAG_ID)));
            noteTagEntity.setTagName(cursor.getString(cursor.getColumnIndex(TAG_NAME)));
            noteTagEntity.setCreateTime(cursor.getLong(cursor.getColumnIndex(TAG_CREATE_TIMESTAMP)));
            noteTagEntity.setRootTagId(cursor.getLong(cursor.getColumnIndex(TAG_ROOT_ID)));
            noteTagEntries.add(noteTagEntity);
        }
        cursor.close();
        return noteTagEntries;
    }

    public List<NoteTagEntity> queryAllSubTagByRootTagId(long tagId){
        List<NoteTagEntity> noteTagEntries = new ArrayList<>();
        String selection = TAG_ROOT_ID + " = " + tagId;
        Cursor cursor = contentResolver.query(ContentProviderConstants.TAG_TABLE_URI,
                null, selection, null, null);
        if(cursor == null){
            Log.i(TAG, "queryAllSubTagByRootTagId: content provider return error");
            return noteTagEntries;
        }
        while(cursor.moveToNext()){
            NoteTagEntity noteTagEntity = new NoteTagEntity();
            noteTagEntity.setTagId(cursor.getLong(cursor.getColumnIndex(TAG_ID)));
            noteTagEntity.setTagName(cursor.getString(cursor.getColumnIndex(TAG_NAME)));
            noteTagEntity.setCreateTime(cursor.getLong(cursor.getColumnIndex(TAG_CREATE_TIMESTAMP)));
            noteTagEntity.setRootTagId(cursor.getLong(cursor.getColumnIndex(TAG_ROOT_ID)));
            noteTagEntries.add(noteTagEntity);
        }
        cursor.close();
        return noteTagEntries;
    }

    public void updateTag(NoteTagEntity noteTagEntity){
        if(noteTagEntity.getTagId() < 0)
            return;
        ContentValues contentValues = new ContentValues();
        contentValues.put(TAG_NAME, noteTagEntity.getTagName());
        contentValues.put(TAG_ROOT_ID, noteTagEntity.getRootTagId());
        String whereClause = TAG_ID + " = " + noteTagEntity.getTagId();
        int ret = contentResolver.update(ContentProviderConstants.TAG_TABLE_URI, contentValues, whereClause, null);
        Log.i(TAG, "updateTag: update affected rows = " + ret);
    }

    public void deleteTag(long tagId){
        if(tagId < 0)
            return;
        String whereClause = TAG_ID + " = " + tagId;
        int ret = contentResolver.delete(ContentProviderConstants.TAG_TABLE_URI, whereClause, null);
        Log.i(TAG, "deleteTag: delete rows = " + ret);
    }

    /**
     * TAG_NOTE_RECORD database operations.
     */
    public long insertTagRecord(TagRecordEntity tagRecordEntity){
        long ret = 1;
        ContentValues contentValues = new ContentValues();
        contentValues.put(TAG_ID, tagRecordEntity.getTagId());
        contentValues.put(NOTE_ID, tagRecordEntity.getNoteId());
        contentValues.put(TAG_RECORD_CREATE_TIMESTAMP, tagRecordEntity.getCreateTimestamp());
        Uri uri = contentResolver.insert(ContentProviderConstants.TAG_RECORD_TABLE_URI, contentValues);
        if(uri == null || (ret = ContentUris.parseId(uri)) == -1){
            Log.d(TAG, "insertTagRecord: insert into tag record table error");
        }
        return ret;
    }

    public List<TagRecordEntity> queryAllTagRecordByNoteId(long noteId){
        List<TagRecordEntity> tagRecordEntities = new ArrayList<>();

        String selection = NOTE_ID + " = " + noteId;
        Cursor cursor = contentResolver.query(ContentProviderConstants.TAG_RECORD_TABLE_URI,
                null, selection, null, null);
        if(cursor == null){
            Log.i(TAG, "queryAllTagRecordByNoteId: content provider error");
            return tagRecordEntities;
        }
        while(cursor.moveToNext()){
            TagRecordEntity tagRecordEntity = new TagRecordEntity();
            tagRecordEntity.setTagRecordId(cursor.getLong(cursor.getColumnIndex(TAG_RECORD_ID)));
            tagRecordEntity.setTagId(cursor.getLong(cursor.getColumnIndex(TAG_ID)));
            tagRecordEntity.setNoteId(cursor.getLong(cursor.getColumnIndex(NOTE_ID)));
            tagRecordEntity.setCreateTimestamp(cursor.getLong(cursor.getColumnIndex(TAG_RECORD_CREATE_TIMESTAMP)));
            tagRecordEntities.add(tagRecordEntity);
        }
        cursor.close();
        return tagRecordEntities;
    }

    public List<TagRecordEntity> queryTagRecordByTagId(long tagId){
        List<TagRecordEntity> tagRecordEntities = new ArrayList<>();
        String selection = TAG_ID + " = " + tagId;
        String orderBy = NOTE_ID + " DESC";
        Cursor cursor = contentResolver.query(ContentProviderConstants.TAG_RECORD_TABLE_URI,
                null, selection, null, orderBy);
        if(cursor == null){
            Log.i(TAG, "queryTagRecordByTagId: content provider error");
            return tagRecordEntities;
        }
        while(cursor.moveToNext()){
            TagRecordEntity tagRecordEntity = new TagRecordEntity();
            tagRecordEntity.setTagRecordId(cursor.getLong(cursor.getColumnIndex(TAG_RECORD_ID)));
            tagRecordEntity.setTagId(cursor.getLong(cursor.getColumnIndex(TAG_ID)));
            tagRecordEntity.setNoteId(cursor.getLong(cursor.getColumnIndex(NOTE_ID)));
            tagRecordEntity.setCreateTimestamp(cursor.getLong(cursor.getColumnIndex(TAG_RECORD_CREATE_TIMESTAMP)));
            tagRecordEntities.add(tagRecordEntity);
        }
        cursor.close();
        return tagRecordEntities;
    }

    public TagRecordEntity queryTagRecordByDoubleId(long tagId, long noteId){
        TagRecordEntity tagRecordEntity = null;
        String selection = TAG_ID + " = " + tagId + " and " + NOTE_ID + " = " + noteId;
        String orderBy = NOTE_ID + " DESC";
        Cursor cursor = contentResolver.query(ContentProviderConstants.TAG_RECORD_TABLE_URI,
                null, selection, null, orderBy);
        if(cursor == null){
            Log.i(TAG, "queryTagRecordByDoubleId: content provider error");
            return null;
        }
        if(cursor.moveToNext()){
            tagRecordEntity = new TagRecordEntity();
            tagRecordEntity.setTagRecordId(cursor.getLong(cursor.getColumnIndex(TAG_RECORD_ID)));
            tagRecordEntity.setTagId(cursor.getLong(cursor.getColumnIndex(TAG_ID)));
            tagRecordEntity.setNoteId(cursor.getLong(cursor.getColumnIndex(NOTE_ID)));
            tagRecordEntity.setCreateTimestamp(cursor.getLong(cursor.getColumnIndex(TAG_RECORD_CREATE_TIMESTAMP)));
        }
        cursor.close();
        return tagRecordEntity;
    }

    public void deleteTagRecord(long tagRecordId){
        if(tagRecordId < 0)
            return;
        String whereClause = TAG_RECORD_ID + " = " + tagRecordId;
        int ret = contentResolver.delete(ContentProviderConstants.TAG_RECORD_TABLE_URI, whereClause, null);
        Log.i(TAG, "deleteTagRecord: delete affected rows = " + ret);
    }

    public void deleteTagRecordByNoteId(long noteId){
        if(noteId < 0)
            return;
        String whereClause = NOTE_ID + " = " + noteId;
        int ret = contentResolver.delete(ContentProviderConstants.TAG_RECORD_TABLE_URI, whereClause, null);
        Log.i(TAG, "deleteTagRecordByNoteId: delete affected rows = " + ret);
    }
}
