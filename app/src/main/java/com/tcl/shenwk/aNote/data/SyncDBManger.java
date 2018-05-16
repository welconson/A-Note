package com.tcl.shenwk.aNote.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import com.tcl.shenwk.aNote.entity.DeleteRecordEntity;
import com.tcl.shenwk.aNote.util.Constants;
import com.tcl.shenwk.aNote.util.UrlSource;

public class SyncDBManger {
    private static final String TAG = "SyncDBManger";
    private static SyncDBManger mInstance = null;
    private SQLiteOpenHelper dbHelper;

    private SyncDBManger(Context context) {
        dbHelper = new SyncDBOpenHelper(context, Constants.SYNC_DATABASE_NAME, null, Constants.SYNC_DATABASE_VERSION);
    }

    public static SyncDBManger getInstance(Context context){
        if(mInstance == null){
            mInstance = new SyncDBManger(context);
        }
        return mInstance;
    }

    public long insertDeleteRecord(DeleteRecordEntity deleteRecordEntity){
        long ret;
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBFieldsName.DELETE_RECORD_TYPE, deleteRecordEntity.getDeleteItemType());
        contentValues.put(DBFieldsName.SYNC_ROW_ID, deleteRecordEntity.getSyncRowId());
        ret = database.insert(DBFieldsName.DELETE_RECORD_TABLE_NAME, null, contentValues);
        return ret;
    }

    /**
     * Query a delete record via itemType and syncRowId,
     * if record exist, return the record id, or Constants.SYNC_DELETE_ID_NO_ID.
     * @param itemType      delete record type.
     * @param syncRowId     record syncRowId.
     * @return  if record exist, return the record id, or Constants.SYNC_DELETE_ID_NO_ID.
     */
    public long queryDeleteRecordId(int itemType, long syncRowId){
        long ret = Constants.SYNC_DELETE_ID_NO_ID;
        String selection = DBFieldsName.DELETE_RECORD_TYPE + "=" + itemType + " and " + DBFieldsName.SYNC_ROW_ID + "=" + syncRowId;
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Cursor cursor = database.query(
                DBFieldsName.DELETE_RECORD_TABLE_NAME,
                new String[]{DBFieldsName.DELETE_RECORD_ID},
                selection,
                null,
                null,
                null,
                null
        );
        if(cursor.moveToNext()){
            ret = cursor.getLong(cursor.getColumnIndex(DBFieldsName.DELETE_RECORD_ID));
        }
        cursor.close();
        database.close();
        return ret;
    }

    public void cleanDeleteRecordTable(){
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.execSQL("delete from " + DBFieldsName.DELETE_RECORD_TABLE_NAME);
        database.close();
    }

    private long getDeleteSyncRowId(Context context, Uri uri, String[] projection, String selection){
        long syncRowId = -1;
        Cursor cursor = context.getContentResolver().query(
                uri,
                projection,
                selection,
                null,
                null,
                null);
        if(cursor != null && cursor.moveToNext()){
            syncRowId = cursor.getLong(0);
            cursor.close();
        }
        return syncRowId;
    }

    public void checkDeleteRecord(Context context, long deleteItemId, int deleteItemType){
        Uri uri;
        String selection;
        String[] projection = new String[]{DBFieldsName.SYNC_ROW_ID};
        switch (deleteItemType){
            case Constants.DELETE_ITEM_TYPE_NOTE:
                uri = ContentProviderConstants.NOTE_TABLE_URI;
                selection = DBFieldsName.NOTE_ID + "=" + deleteItemId;
                break;
            case Constants.DELETE_ITEM_TYPE_RESOURCE:
                uri = ContentProviderConstants.RESOURCE_TABLE_URI;
                selection = DBFieldsName.RESOURCE_ID + "=" + deleteItemId;
                break;
            case Constants.DELETE_ITEM_TYPE_TAG:
                uri = ContentProviderConstants.TAG_TABLE_URI;
                selection = DBFieldsName.TAG_ID + "=" + deleteItemId;
                break;
            case Constants.DELETE_ITEM_TYPE_TAG_RECORD:
                uri = ContentProviderConstants.TAG_RECORD_TABLE_URI;
                selection = DBFieldsName.TAG_RECORD_ID + "=" + deleteItemId;
                break;
            default:return;
        }
        long syncRowId = getDeleteSyncRowId(context, uri, projection, selection);
        if(syncRowId != -1){
            DeleteRecordEntity deleteRecordEntity = new DeleteRecordEntity();
            deleteRecordEntity.setDeleteItemType(deleteItemType);
            deleteRecordEntity.setSyncRowId(syncRowId);
            SyncDBManger.getInstance(context).insertDeleteRecord(deleteRecordEntity);
        }
    }
}
