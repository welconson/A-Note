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

    //MULTIMEDIA table fields
    String MULTIMEDIA_TABLE_NAME = "multimedia";
    String MULTIMEDIA_ID = "multimedia_id";
    String FILE_NAME = "file_name";
    String FILE_PATH = "file_path";
    String DATA_TYPE = "data_type";
}
