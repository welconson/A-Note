package com.tcl.shenwk.aNote.entry;

/**
 * Multi media data
 * Created by shenwk on 2018/3/10.
 */

public class ResourceDataEntry {
    private long resourceId;
    private long noteId;
    private String fileName;
    private String path;
    private int dataType;
    private int spanStart;

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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
