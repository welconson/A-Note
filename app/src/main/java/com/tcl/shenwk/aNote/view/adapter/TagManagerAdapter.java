package com.tcl.shenwk.aNote.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.tcl.shenwk.aNote.R;
import com.tcl.shenwk.aNote.entry.NoteEntry;
import com.tcl.shenwk.aNote.entry.NoteTagEntry;
import com.tcl.shenwk.aNote.model.ANoteDBManager;
import com.tcl.shenwk.aNote.view.viewholder.TagManagerViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shenwk on 2018/3/23.
 */

public class TagManagerAdapter extends RecyclerView.Adapter {
    private static final int NO_NUMBER = -1;
    private List<TagItem> primaryTagItems;
    private List<NoteEntry> noteEntries;
    private LayoutInflater inflater = null;

    public TagManagerAdapter(List<NoteTagEntry> primaryTagEntries, LayoutInflater inflater) {
        primaryTagItems = new ArrayList<>();
        for(NoteTagEntry noteTagEntry : primaryTagEntries){
            TagItem tagItem = new TagItem();
            tagItem.noteTagEntry = noteTagEntry;
            primaryTagItems.add(tagItem);
        }
        this.inflater = inflater;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TagManagerViewHolder(
                inflater.inflate(R.layout.tag_manager_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        TagItem tagItem = primaryTagItems.get(position);
        TagManagerViewHolder tagManagerViewHolder = ((TagManagerViewHolder) holder);
        tagManagerViewHolder.tagName.setText(tagItem.noteTagEntry.getTagName());
        if(tagItem.subTagEntries == null){
            tagItem.subTagEntries = ANoteDBManager.getInstance(((TagManagerViewHolder) holder).tagName.getContext())
                    .queryAllSubTagByTagId(tagItem.noteTagEntry.getTagId());
        }
//        if(tagItem.directNoteEntries == null){
//            tagItem.directNoteEntries = ANoteDBManager.getInstance(((TagManagerViewHolder) holder).tagName.getContext())
//                    .queryTagRecordByTagId(tagItem.noteTagEntry.getTagId());
//        }
        tagManagerViewHolder.tagNumber.setText("subtags: " + tagItem.subTagEntries.size());// + " , notes: " + tagItem.directNoteEntries.size());
    }

    @Override
    public int getItemCount() {
        return primaryTagItems.size();
    }

    private class TagItem{
        private NoteTagEntry noteTagEntry;
        private List<NoteTagEntry> subTagEntries = null;
        private List<NoteEntry> directNoteEntries = null;
    }
}
