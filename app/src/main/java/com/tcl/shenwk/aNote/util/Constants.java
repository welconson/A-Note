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
    String EDIT_NOTE_ID = "note_id";
    String ITEM_POSITION = "position";
    String ITEM_NOTE_ENTRY = "note";
    String ITEM_RESOURCE_ENTRY = "res";
    int DEFAULT_ITEM_POSITION = -1;
    int ITEM_BEGIN_POSITION = 0;


    String EDIT_TO_HOME_PAGE_STATUS = "action_to_home_page";
    int HOME_PAGE_NORMAL_RESUME = 0;
    int HOME_PAGE_UPDATE_RESUME = 1;

    String RESULT_SOURCE_TO_HOME_PAGE = "result_to_home_page";
    int FROM_NO_WHERE = -1;
    int FROM_EDIT_ACTIVITY = 0;
    //Extra name and values end

    //db constants start
    String A_NOTE_DATABASE_NAME = "a_note_local.db";
    int DB_VERSION = 1;
    //db constants end

    //file constants start
    String CONTENT_FILE_NAME = "content";
    int NOTE_DIRECTORY_LENGTH = 12;
    int RESOURCE_FILE_NAME_LENGTH = 12;
    String RESOURCE_DIR = "resourceData";
    int PREVIEW_CONTENT_TEXT_LENGTH = 220;
    //file constants end

    //Intent request code
    int SELECT_IMAGE = 0;
    int SELECT_AUDIO = 1;
    int SELECT_VIDEO = 2;
    int SELECT_FILE = 3;
    //Intent request code

    //message what start
    int MESSAGE_ON_ACTIVITY_RESULT = 0;
    //message what end


    //View span size constant(dp)
    int VIEW_SPAN_HEIGHT = 72;
    int VIEW_SPAN_WIDTH = 288;
    //View span size constant(dp)

    //Resource data type
    int RESOURCE_TYPE_IMAGE = 0;
    int RESOURCE_TYPE_AUDIO = 1;
    int RESOURCE_TYPE_VIDEO = 2;
    int RESOURCE_TYPE_FILE = 3;
    //Resource data type

    //Toast text
    String TOAST_TEXT_WITHOUT_PERMISSION = "Permission Denial";
    String TOAST_NEW_NOTE_WITH_NOTHING = "do not save a empty note";
    String TOAST_TAG_NOT_EMPTY = "tag name can not be a empty value";
}
