package com.tcl.shenwk.aNote.entry;

import com.tcl.shenwk.aNote.util.Constants;
import com.tcl.shenwk.aNote.util.DateUtil;

import java.util.Date;

/**
 * Note tag entry class.
 * Created by shenwk on 2018/3/19.
 */

public class NoteTagEntry {
    private long tagId = Constants.NO_TAG_ID;
    private String tagName;
    private long createTime;
    private long rootTagId = Constants.NO_TAG_ID;

    public NoteTagEntry() {
    }

    /**
     * Constructor used to create a new tag for adding.
     * @param tagName tag name.
     */
    public NoteTagEntry(String tagName) {
        this.tagName = tagName;
        this.createTime = DateUtil.getInstance().getTime();
    }

    public long getTagId() {
        return tagId;
    }

    public void setTagId(long tagId) {
        this.tagId = tagId;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getRootTagId() {
        return rootTagId;
    }

    public void setRootTagId(long rootTagId) {
        this.rootTagId = rootTagId;
    }
}
