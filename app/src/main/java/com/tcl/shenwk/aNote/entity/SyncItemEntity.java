package com.tcl.shenwk.aNote.entity;

public class SyncItemEntity {
    private long itemId;
    private long modifyTime;
    private long lastUpdateTime;
    private long syncRowId;

    public long getItemId() {
        return itemId;
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }

    public long getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(long modifyTime) {
        this.modifyTime = modifyTime;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public long getSyncRowId() {
        return syncRowId;
    }

    public void setSyncRowId(long syncRowId) {
        this.syncRowId = syncRowId;
    }

    @Override
    public String toString() {
        return "itemId = " + itemId + ", modifyTime = " + modifyTime + ", lastUpdateTime = " + lastUpdateTime + ", syncRowId = " + syncRowId;
    }
}
