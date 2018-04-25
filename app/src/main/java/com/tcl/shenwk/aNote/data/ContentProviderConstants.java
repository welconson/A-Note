package com.tcl.shenwk.aNote.data;

import android.content.ContentResolver;
import android.net.Uri;

public interface ContentProviderConstants {
    String AUTHORITY = "com.tcl.shenwk.aNote.data.aNoteContentProvider";
    String BASE_URI = ContentResolver.SCHEME_CONTENT + "://" + AUTHORITY;

    Uri NOTE_TABLE_URI = Uri.parse(BASE_URI + "/" + DBFieldsName.NOTE_TABLE_NAME);
    Uri RESOURCE_TABLE_URI = Uri.parse(BASE_URI + "/" + DBFieldsName.RESOURCE_TABLE_NAME);
    Uri TAG_TABLE_URI = Uri.parse(BASE_URI + "/" + DBFieldsName.TAG_TABLE_NAME);
    Uri TAG_RECORD_TABLE_URI = Uri.parse(BASE_URI + "/" + DBFieldsName.TAG_RECORD_TABLE_NAME);

    // matcher code
    int CODE_NOTE_TABLE = 1;
    int CODE_RESOURCE_TABLE = 2;
    int CODE_TAG_TABLE = 3;
    int CODE_TAG_RECORD_TABLE = 4;

    int URI_TYPE_THRESHOLD = 100;

    int SYNC_CODE_NOTE_TABLE = 101;
    int SYNC_CODE_RESOURCE_TABLE = 102;
    int SYNC_CODE_TAG_TABLE = 103;
    int SYNC_CODE_TAG_RECORD_TABLE = 104;
}
