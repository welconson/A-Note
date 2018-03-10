package com.tcl.shenwk.aNote.util;

/**
 * Set of all global constants used in module
 * Created by shenwk on 2018/1/26.
 */

public interface Constants {
    //span tag start
    String IMAGE_SPAN_TAG = "[image]";
    String AUDIO_SPAN_TAG = "[audio]";
    //span tag end

    //
    int ARCHIVED = 1;
    int NOT_ARCHIVED = 0;
    int LABELED_DISCARD = 1;
    int NOTE_LABELED_DISCARD = 0;
    int NO_NOTE_ID = -1;
    //

    //Extra name start
    String ACTION_EDIT_NOTE = "com.tcl.shenwk.aNote.edit_type";
    String EDIT_NOTE_ID = "note_id";
    String ITEM_POSITION = "item_position";
    String ITEM_ENTRY = "item_entry";
    int DEFAULT_ITEM_POSITION = -1;
    int ITEM_BEGIN_POSITION = 0;
    String ACTION_TO_HOME_PAGE = "action_to_home_page";
    int HOME_PAGE_NORMAL_RESUME = 0;
    int HOME_PAGE_UPDATE_RESUME = 1;
    //Extra name end

    //db constants start
    String A_NOTE_DATABASE_NAME = "a_note_local.db";
    int DB_VERSION = 1;
    /*
    multimedia resource type
     */
    int MULTIMEDIA_TYPE_IMAGE = 0;
    //db constants end

    //file constants start
    String CONTENT_FILE_NAME = "content";
    int CONTENT_FILE_NAME_LENGTH = 12;
    //file constants end

    //Intent request code
    int SELECT_IMAGE = 0;
    int SELECT_AUDIO = 1;
    //Intent request code

    //Span size constant
    int VIEW_SPAN_HEIGHT = 72;
    int VIEW_SPAN_WIDTH = 216;
}
