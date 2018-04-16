package com.tcl.shenwk.aNote.data;

import android.content.ContentResolver;
import android.net.Uri;

public interface ContentProviderConstants {
    String AUTHORITY = "com.tcl.shenwk.aNote.data.aNoteContentProvider";
    Uri NOTE_TABLE_URI = Uri.parse(ContentResolver.SCHEME_CONTENT + "://" + AUTHORITY + "/" + DBFieldsName.NOTE_TABLE_NAME);
    Uri RESOURCE_TABLE_URI = Uri.parse(ContentResolver.SCHEME_CONTENT + "://" + AUTHORITY + "/" + DBFieldsName.RESOURCE_TABLE_NAME);
    Uri TAG_TABLE_URI = Uri.parse(ContentResolver.SCHEME_CONTENT + "://" + AUTHORITY + "/" + DBFieldsName.TAG_TABLE_NAME);
    Uri TAG_RECORD_TABLE_URI = Uri.parse(ContentResolver.SCHEME_CONTENT + "://" + AUTHORITY + "/" + DBFieldsName.TAG_RECORD_TABLE_NAME);

    // matcher code
    int CODE_NOTE_TABLE = 1;
    int CODE_RESOURCE_TABLE = 2;
    int CODE_TAG_TABLE = 3;
    int CODE_TAG_RECORD_TABLE = 4;
}
