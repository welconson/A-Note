package com.tcl.shenwk.aNote.entity;

import com.tcl.shenwk.aNote.util.Constants;
import com.tcl.shenwk.aNote.util.DateUtil;

/**
 * TagNoteRecord entity class.
 * Created by shenwk on 2018/3/19.
 */

public class TagRecordEntity {
    // Labelled the tag record which is a normal record,
    // there is no need of database operation.
    public final static int NORMAL = 0;
    // Labelled the tag record which need to be delete in database operation.
    public final static int TO_DELETE = 1;
    // Labelled the tag record which is a new created record to store in database.
    public final static int NEW_CREATE = 2;

    private long tagRecordId = Constants.NO_TAG_RECORD_ID;
    private long noteId = Constants.NO_NOTE_ID;
    private long tagId = Constants.NO_TAG_ID;
    private long createTimestamp;

    public int status = NORMAL;

    public TagRecordEntity(long tagId) {
        this.tagId = tagId;
        createTimestamp = DateUtil.getInstance().getTime();
    }

    public TagRecordEntity() {

    }

    public long getTagRecordId() {
        return tagRecordId;
    }

    public void setTagRecordId(long tagRecordId) {
        this.tagRecordId = tagRecordId;
    }

    public long getNoteId() {
        return noteId;
    }

    public void setNoteId(long noteId) {
        this.noteId = noteId;
    }

    public long getTagId() {
        return tagId;
    }

    public void setTagId(long tagId) {
        this.tagId = tagId;
    }

    public long getCreateTimestamp() {
        return createTimestamp;
    }

    public void setCreateTimestamp(long createTimestamp) {
        this.createTimestamp = createTimestamp;
    }
}
