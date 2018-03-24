package com.tcl.shenwk.aNote.entry;

import com.tcl.shenwk.aNote.util.DateUtil;

import java.util.Date;

/**
 * TagNoteRecord entry class.
 * Created by shenwk on 2018/3/19.
 */

public class TagRecordEntry {
    // Labelled the tag record which is a normal record,
    // there is no need of database operation.
    public final static int NORMAL = 0;
    // Labelled the tag record which need to be delete in database operation.
    public final static int TO_DELETE = 1;
    // Labelled the tag record which is a new created record to store in database.
    public final static int NEW_CREATE = 2;

    private long tagRecordId;
    private long noteId;
    private long tagId;
    private long createTimestamp;

    public int status = NORMAL;

    public TagRecordEntry(long tagId) {
        this.tagId = tagId;
        createTimestamp = DateUtil.getInstance().getTime();
    }

    public TagRecordEntry() {

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
