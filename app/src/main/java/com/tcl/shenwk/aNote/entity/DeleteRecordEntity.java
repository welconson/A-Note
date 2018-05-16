package com.tcl.shenwk.aNote.entity;

public class DeleteRecordEntity {
    private long deleteRecordId;
    private int deleteItemType;
    private long syncRowId;

    public long getDeleteRecordId() {
        return deleteRecordId;
    }

    public void setDeleteRecordId(long deleteRecordId) {
        this.deleteRecordId = deleteRecordId;
    }

    public int getDeleteItemType() {
        return deleteItemType;
    }

    public void setDeleteItemType(int deleteItemType) {
        this.deleteItemType = deleteItemType;
    }

    public long getSyncRowId() {
        return syncRowId;
    }

    public void setSyncRowId(long syncRowId) {
        this.syncRowId = syncRowId;
    }
}
