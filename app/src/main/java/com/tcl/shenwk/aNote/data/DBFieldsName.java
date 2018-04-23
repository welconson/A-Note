package com.tcl.shenwk.aNote.data;

/**
 * Database fields definition
 * Created by shenwk on 2018/2/5.
 */

public interface DBFieldsName {
    //USER table fields
    String USER_TABLE_NAME = "user";
    String USER_ID = "user_id";

    // NOTE table fields
    String NOTE_TABLE_NAME = "note";
    String NOTE_ID = "note_id";
    String NOTE_TITLE = "note_title";
    String NOTE_DIR_NAME = "note_content_path";
    String CREATE_TIMESTAMP = "create_timestamp";
    String UPDATE_TIMESTAMP = "update_timestamp";
    String LOCATION_INFO = "location_info";
    String HAS_ARCHIVED = "has_archived";
    String IS_LABELED_DISCARDED = "is_labeled_discarded";

    //RESOURCE_DATA table fields
    String RESOURCE_TABLE_NAME = "resource";
    String RESOURCE_ID = "resource_id";
    String RESOURCE_FILE_NAME = "resource_file_name";
    String RESOURCE_PATH = "resource_path";
    String RESOURCE_DURATION = "resource_duration";
    String RESOURCE_SIZE = "resource_size";
    String DATA_TYPE = "data_type";
    String SPAN_START = "span_start";

    //TAG table fields
    String TAG_TABLE_NAME = "tag";
    String TAG_ID = "tag_id";
    String TAG_NAME = "tag_name";
    String TAG_CREATE_TIMESTAMP = "tag_create_timestamp";
    String TAG_ROOT_ID = "tag_root_id";

    //TAG_NOTE_RECORD table fields
    String TAG_RECORD_TABLE_NAME = "tag_note_record_table_name";
    String TAG_RECORD_ID = "tag_note_record_id";
    String TAG_RECORD_CREATE_TIMESTAMP = "tag_note_record_create_timestamp";

    //SYNC_RECORD table fields
    String MODIFY_RECORD_ID = "sync_record_id";
    String MODIFY_RECORD_INITIAL_STATUS = "status";
    String MODIFY_RECORD_LAST_OPERATION_TYPE = "operation_type";
    String MODIFY_RECORD_TYPE = "type";
    String MODIFY_RECORD_ROW_ID = "rowID";
    String MODIFY_RECORD_PATH = "";
    String MODIFY_RECORD_TIMESTAMP = "timestamp";
}
