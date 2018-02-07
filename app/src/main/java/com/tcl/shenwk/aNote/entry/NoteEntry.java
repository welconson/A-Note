package com.tcl.shenwk.aNote.entry;

import com.tcl.shenwk.aNote.util.Constants;

import java.util.Date;

/**
 * Note entry class.
 * Created by shenwk on 2018/2/5.
 */

public class NoteEntry {
    private long noteId;
    private String noteTitle;
    private String noteContentPath;
    private String createTimestamp;
    private String updateTimestamp;
    private String locationInfo;
    private int hasArchived;
    private int isLabeledDiscarded;
    private String noteContent;

    public NoteEntry(String noteTitle, String noteContentPath, String createTimestamp, String updateTimestamp,
                     String locationInfo, int hasArchived, int isLabeledDiscarded) {
        this.noteTitle = noteTitle;
        this.noteContentPath = noteContentPath;
        this.createTimestamp = createTimestamp;
        this.updateTimestamp = updateTimestamp;
        this.locationInfo = locationInfo;
        this.hasArchived = hasArchived;
        this.isLabeledDiscarded = isLabeledDiscarded;
    }

    public NoteEntry() {
        noteId = Constants.NO_NOTE_ID;
        Date date = new Date();
        createTimestamp = date.toString();
        updateTimestamp = date.toString();
        noteContent = null;
        noteTitle = null;
        hasArchived = Constants.NOT_ARCHIVED;
        isLabeledDiscarded = Constants.NOTE_LABELED_DISCARD;
        noteContent = null;
    }

    public long getNoteId() {
        return noteId;
    }

    public void setNoteId(long noteId) {
        this.noteId = noteId;
    }

    public String getNoteTitle() {
        return noteTitle;
    }

    public void setNoteTitle(String noteTitle) {
        this.noteTitle = noteTitle;
    }

    public String getNoteContentPath() {
        return noteContentPath;
    }

    public void setNoteContentPath(String noteContentPath) {
        this.noteContentPath = noteContentPath;
    }

    public String getCreateTimestamp() {
        return createTimestamp;
    }

    public void setCreateTimestamp(String createTimestamp) {
        this.createTimestamp = createTimestamp;
    }

    public String getUpdateTimestamp() {
        return updateTimestamp;
    }

    public void setUpdateTimestamp(String updateTimestamp) {
        this.updateTimestamp = updateTimestamp;
    }

    public String getLocationInfo() {
        return locationInfo;
    }

    public void setLocationInfo(String locationInfo) {
        this.locationInfo = locationInfo;
    }

    public int getHasArchived() {
        return hasArchived;
    }

    public void setHasArchived(int hasArchived) {
        this.hasArchived = hasArchived;
    }

    public int getIsLabeledDiscarded() {
        return isLabeledDiscarded;
    }

    public void setIsLabeledDiscarded(int isLabeledDiscarded) {
        this.isLabeledDiscarded = isLabeledDiscarded;
    }

    public String getNoteContent() {
        return noteContent;
    }

    public void setNoteContent(String noteContent) {
        this.noteContent = noteContent;
    }
}
