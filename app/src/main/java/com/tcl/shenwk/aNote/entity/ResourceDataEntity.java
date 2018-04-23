package com.tcl.shenwk.aNote.entity;

import com.tcl.shenwk.aNote.util.Constants;

import java.io.Serializable;

/**
 * Multi media data
 * Created by shenwk on 2018/3/10.
 */

public class ResourceDataEntity implements Serializable{
    private long resourceId = Constants.NO_RESOURCE_ID;
    private long noteId = Constants.NO_NOTE_ID;
    private String fileName;
    private String resourceRelativePath = null;
    private int dataType;
    private int spanStart;
    private int duration = 0;
    private String size;

    public long getResourceId() {
        return resourceId;
    }

    public void setResourceId(long resourceId) {
        this.resourceId = resourceId;
    }

    public long getNoteId() {
        return noteId;
    }

    public void setNoteId(long noteId) {
        this.noteId = noteId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getDataType() {
        return dataType;
    }

    public void setDataType(int dataType) {
        this.dataType = dataType;
    }

    public int getSpanStart() {
        return spanStart;
    }

    public void setSpanStart(int spanStart) {
        this.spanStart = spanStart;
    }

    public String getResourceRelativePath() {
        return resourceRelativePath;
    }

    public void setResourceRelativePath(String resourceRelativePath) {
        this.resourceRelativePath = resourceRelativePath;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }
}
