package com.tcl.shenwk.aNote.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.tcl.shenwk.aNote.model.ANoteDBOpenHelper;
import com.tcl.shenwk.aNote.model.DBFieldsName;
import com.tcl.shenwk.aNote.util.Constants;
import com.tcl.shenwk.aNote.util.StringUtil;

import static com.tcl.shenwk.aNote.data.ContentProviderConstants.*;

public class ANoteContentProvider extends ContentProvider {
    private static final String TAG = "ANoteContentProvider";
    private static final UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);


    private ANoteDBOpenHelper mDBHelper;

    static{
        mUriMatcher.addURI(AUTHORITY, DBFieldsName.NOTE_TABLE_NAME, CODE_NOTE_TABLE);
        mUriMatcher.addURI(AUTHORITY, DBFieldsName.RESOURCE_TABLE_NAME, CODE_RESOURCE_TABLE);
        mUriMatcher.addURI(AUTHORITY, DBFieldsName.TAG_TABLE_NAME, CODE_TAG_TABLE);
        mUriMatcher.addURI(AUTHORITY, DBFieldsName.TAG_RECORD_TABLE_NAME, CODE_TAG_RECORD_TABLE);
    }

    @Override
    public boolean onCreate() {
        mDBHelper = new ANoteDBOpenHelper(getContext(), Constants.A_NOTE_DATABASE_NAME, null, Constants.DB_VERSION);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        String table = parseTable(uri);
        if(TextUtils.isEmpty(table))
            return null;
        SQLiteDatabase database = mDBHelper.getReadableDatabase();
        return database.query(table, projection, selection, selectionArgs, null, null, sortOrder);
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        String table = parseTable(uri);
        if(TextUtils.isEmpty(table) || values == null || values.size() == 0)
            return null;
        SQLiteDatabase database = mDBHelper.getWritableDatabase();
        long rowId = database.insert(table, null, values);
        return Uri.withAppendedPath(uri, String.valueOf(rowId));
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        String table = parseTable(uri);
        if(TextUtils.isEmpty(table))
            return 0;
        SQLiteDatabase database = mDBHelper.getWritableDatabase();
        return database.delete(table, selection, selectionArgs);
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        String table = parseTable(uri);
        if(TextUtils.isEmpty(table) || values == null || values.size() == 0)
            return 0;
        SQLiteDatabase database = mDBHelper.getWritableDatabase();
        return database.update(table, values, selection, selectionArgs);
    }

    public String parseTable(Uri uri){
        String table;
        switch (mUriMatcher.match(uri)){
            case CODE_NOTE_TABLE:{
                table = DBFieldsName.NOTE_TABLE_NAME;
                break;
            }
            case CODE_RESOURCE_TABLE:{
                table = DBFieldsName.RESOURCE_TABLE_NAME;
                break;
            }
            case CODE_TAG_TABLE:{
                table = DBFieldsName.TAG_TABLE_NAME;
                break;
            }
            case CODE_TAG_RECORD_TABLE:{
                table = DBFieldsName.TAG_RECORD_TABLE_NAME;
                break;
            }
            default:{
                Log.d(TAG, "query: uri matched no database table");
                return null;
            }
        }
        return table;
    }
}
