package com.tcl.shenwk.aNote.entity;

import com.tcl.shenwk.aNote.util.Constants;
import com.tcl.shenwk.aNote.util.DateUtil;

import java.io.Serializable;

/**
 * Note entity class.
 * Created by shenwk on 2018/2/5.
 */

public class NoteEntity implements Serializable{
    private long noteId;
    private String noteTitle;
    private String noteDirName;
    private long createTimestamp;
    private long updateTimestamp;
    private String locationInfo;
    private boolean hasArchived;
    private boolean isLabeledDiscarded;

    public NoteEntity(String noteTitle, String noteDirName, long createTimestamp, long updateTimestamp,
                      String locationInfo, boolean hasArchived, boolean isLabeledDiscarded) {
        this.noteTitle = noteTitle;
        this.noteDirName = noteDirName;
        this.createTimestamp = createTimestamp;
        this.updateTimestamp = updateTimestamp;
        this.locationInfo = locationInfo;
        this.hasArchived = hasArchived;
        this.isLabeledDiscarded = isLabeledDiscarded;
    }

    public NoteEntity() {
        noteId = Constants.NO_NOTE_ID;
        createTimestamp = DateUtil.getInstance().getTime();
        updateTimestamp = DateUtil.getInstance().getTime();
        noteTitle = null;
        hasArchived = false;
        isLabeledDiscarded = false;
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

    public String getNoteDirName() {
        return noteDirName;
    }

    public void setNoteDirName(String noteDirName) {
        this.noteDirName = noteDirName;
    }

    public long getCreateTimestamp() {
        return createTimestamp;
    }

    public void setCreateTimestamp(long createTimestamp) {
        this.createTimestamp = createTimestamp;
    }

    public long getUpdateTimestamp() {
        return updateTimestamp;
    }

    public void setUpdateTimestamp(long updateTimestamp) {
        this.updateTimestamp = updateTimestamp;
    }

    public String getLocationInfo() {
        return locationInfo;
    }

    public void setLocationInfo(String locationInfo) {
        this.locationInfo = locationInfo;
    }

    public boolean hasArchived() {
        return hasArchived;
    }

    public void setHasArchived(boolean hasArchived) {
        this.hasArchived = hasArchived;
    }

    public boolean isLabeledDiscarded() {
        return isLabeledDiscarded;
    }

    public void setIsLabeledDiscarded(boolean isLabeledDiscarded) {
        this.isLabeledDiscarded = isLabeledDiscarded;
    }
}
