package com.tcl.shenwk.aNote.model;

/**
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
    String NOTE_CONTENT_PATH = "note_content_path";
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
    String DATA_TYPE = "data_type";
    String SPAN_START = "span_start";
}
