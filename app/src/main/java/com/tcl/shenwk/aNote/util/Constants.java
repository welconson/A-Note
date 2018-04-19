package com.tcl.shenwk.aNote.util;

/**
 * Set of all global constants used in module
 * Created by shenwk on 2018/1/26.
 */

public interface Constants {
    char TAG = 65532;
    //span tag start
    String IMAGE_SPAN_TAG = "" + TAG;
    String AUDIO_SPAN_TAG = "" + TAG;
    String VIDEO_SPAN_TAG = "" + TAG;
    String FILE_SPAN_TAG = "" + TAG;
    String RESOURCE_TAG = new String(new char[]{TAG});
    //span tag end

    //
    int ARCHIVED = 1;
    int NOT_ARCHIVED = 0;
    int LABELED_DISCARD = 1;
    int NOTE_LABELED_DISCARD = 0;
    int NO_NOTE_ID = -1;
    int NO_RESOURCE_ID = -1;
    int NO_TAG_ID = -1;
    int NO_TAG_RECORD_ID = -1;
    //

    //Extra name and value start
    String ACTION_TYPE_OF_EDIT_NOTE = "com.tcl.shenwk.aNote.edit_type";
    String ITEM_POSITION = "position";
    String ITEM_NOTE_ENTITY = "note";
    String WITH_TAG_ID = "with__tag_record";
    String ITEM_RESOURCE_entity = "res";
    int DEFAULT_ITEM_POSITION = -1;
    int ITEM_BEGIN_POSITION = 0;

    //Extra name and values end

    //db constants start
    String A_NOTE_DATA_DATABASE_NAME = "a_note_local.db";
    int A_NOTE_DATA_DB_VERSION = 1;

    String SYNC_DATABASE_NAME = "sync.db";
    int SYNC_DATABASE_VERSION = 1;
    //db constants end

    //file constants start
    String CONTENT_FILE_NAME = "content";
    int NOTE_DIRECTORY_LENGTH = 12;
    int RESOURCE_FILE_NAME_LENGTH = 12;
    String RESOURCE_DIR = "resourceData";
    int PREVIEW_CONTENT_TEXT_LENGTH = 220;
    String TEMP_FILE_DIR = "temp";  //which is used to store temp file user created and will be move to corresponding directory when finishing editing note.
    //file constants end

    //View span size constant(dp)
    int VIEW_SPAN_HEIGHT = 72;
    int VIEW_SPAN_WIDTH = 324;
    //View span size constant(dp)

    //Resource data type
    int RESOURCE_TYPE_IMAGE = 0;
    int RESOURCE_TYPE_AUDIO = 1;
    int RESOURCE_TYPE_VIDEO = 2;
    int RESOURCE_TYPE_FILE = 3;
    //Resource data type

    // SharePreference
    String PREFERENCE_USER_INFO = "user_info";

    String PREFERENCE_FIELD_USER_ID = "user_id";
    String PREFERENCE_FIELD_USER_EMAIL = "email";
    String PREFERENCE_FIELD_USER_NAME = "name";
    String PREFERENCE_FIELD_USER_PASSWORD = "password";
    String PREFERENCE_FIELD_ACCOUNT_CREATE_TIME = "create_time";

    String PREFERENCE_FIELD_LOGIN_STATUS = "login_status";
    String PREFERENCE_FIELD_NEED_FULL_DOWNLOAD = "need_full_download";
    String PREFERENCE_FIELD_UPDATE_CODE = "update_code";

    // JSON constants
    String JSON_USER_ID = "userId";
    String JSON_USER_EMAIL = "email";
    String JSON_USER_NAME = "name";
    String JSON_USER_PASSWORD = "password";
    String JSON_ACCOUNT_CREATE_TIME = "createTime";
    String JSON_UPDATE_CODE = "updateCode";
    String JSON_SERVER_DATABASE_PATH = "serverDatabasePath";
    String JSON_DOWNLOAD_PATH = "downloadPath";

    String JSON_REQUEST_RESULT = "result";

    String SYNC_FILE_TYPE_DATABASE = "database";
    String SYNC_FILE_TYPE_RESOURCE = "resource";
    String SYNC_FILE_TYPE_CONTENT = "content";

    //broadcast action
    String BROADCAST_ACTION_SYNC_MODIFIED = "com.tcl.shenwk.aNote.SYNC_FINISHED";
}
