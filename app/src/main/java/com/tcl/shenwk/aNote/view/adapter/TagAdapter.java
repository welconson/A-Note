package com.tcl.shenwk.aNote.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.tcl.shenwk.aNote.R;
import com.tcl.shenwk.aNote.entry.NoteTagEntry;
import com.tcl.shenwk.aNote.entry.TagRecordEntry;
import com.tcl.shenwk.aNote.util.StringUtil;
import com.tcl.shenwk.aNote.view.viewholder.SelectTagViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapt to show tag Inside note edit activity.
 * Created by shenwk on 2018/3/20.
 */

public class TagAdapter extends RecyclerView.Adapter {
    private LayoutInflater layoutInflater;
    private List<TagItem> tagItems;
    private RecyclerView recyclerView;

    public TagAdapter(LayoutInflater layoutInflater, List<TagItem> tagItems, RecyclerView recyclerView) {
        this.layoutInflater = layoutInflater;
        this.tagItems = tagItems;
        this.recyclerView = recyclerView;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final SelectTagViewHolder selectTagViewHolder = new SelectTagViewHolder(
                layoutInflater.inflate(R.layout.tag_edit_item, parent, false));
        selectTagViewHolder.checkBox.setOnCheckedChangeListener(checkBoxOnCheckListener);
        return selectTagViewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        SelectTagViewHolder selectTagViewHolder = ((SelectTagViewHolder) holder);
        selectTagViewHolder.textView.setText(tagItems.get(position).noteTagEntry.getTagName());
        selectTagViewHolder.checkBox.setChecked(tagItems.get(position).isChecked);
    }

    @Override
    public int getItemCount() {
        return tagItems.size();
    }

    public void addNewTag(NoteTagEntry noteTagEntry) {
        if (noteTagEntry == null)
            return;
        TagItem tagItem = new TagItem();
        tagItem.noteTagEntry = noteTagEntry;
        tagItem.isChecked = true;
        tagItem.tagRecordEntry = new TagRecordEntry(noteTagEntry.getTagId());
        tagItem.tagRecordEntry.status = TagRecordEntry.NEW_CREATE;
        tagItems.add(tagItem);
        notifyItemInserted(tagItems.size() - 1);
    }

    static class TagItem {
        NoteTagEntry noteTagEntry;
        TagRecordEntry tagRecordEntry = null;
        boolean isChecked = false;
    }

    public static List<TagItem> setItemList(List<NoteTagEntry> noteTagEntries,
                                            List<TagRecordEntry> tagRecordEntries) {
        List<TagItem> tagItems = new ArrayList<>();
        for (NoteTagEntry noteTagEntry : noteTagEntries) {
            TagItem tagItem = new TagItem();
            tagItem.noteTagEntry = noteTagEntry;
            if (tagRecordEntries != null) {
                for (TagRecordEntry tagRecordEntry : tagRecordEntries) {
                    if (tagRecordEntry.getTagId() == noteTagEntry.getTagId()) {
                        if(tagRecordEntry.status == TagRecordEntry.NEW_CREATE ||
                                tagRecordEntry.status == TagRecordEntry.NORMAL)
                            tagItem.isChecked = true;
                        tagItem.tagRecordEntry = tagRecordEntry;
                        break;
                    }
                }
            }
            tagItems.add(tagItem);
        }
        return tagItems;
    }

    public void checkOnPosition(int position) {
        tagItems.get(position).isChecked = true;
        notifyItemChanged(position);
    }

    public int isTagInList(String tagName) {
        int position = 0;
        for (TagItem tagItem : tagItems) {
            if (StringUtil.equal(tagName, tagItem.noteTagEntry.getTagName()))
                return position;
            position++;
        }
        return -1;
    }

    public List<TagRecordEntry> getCheckedList() {
        List<TagRecordEntry> tagRecordEntries = new ArrayList<>();
        for (TagItem tagItem : tagItems) {
            TagRecordEntry tagRecordEntry = tagItem.tagRecordEntry;
            // If tagRecordEntry is not null, maybe it need to be stored, deleted or display.
            if(tagItem.tagRecordEntry != null){
                // Need to be stored
                if(tagRecordEntry.status == TagRecordEntry.NEW_CREATE && tagItem.isChecked){
                    tagRecordEntries.add(tagRecordEntry);
                }
                // Need to be deleted or nothing to to, depending on status.
                else if(tagRecordEntry.status == TagRecordEntry.NORMAL ||
                        tagRecordEntry.status == TagRecordEntry.TO_DELETE){
                    tagRecordEntry.status = tagItem.isChecked ? TagRecordEntry.NORMAL : TagRecordEntry.TO_DELETE;
                    tagRecordEntries.add(tagRecordEntry);
                }
            }
            // If tagRecordEntry is null but it is checked, it will be a new created record.
            else if (tagItem.isChecked) {
                tagRecordEntry = new TagRecordEntry(tagItem.noteTagEntry.getTagId());
                tagRecordEntry.status = TagRecordEntry.NEW_CREATE;
                tagRecordEntries.add(tagRecordEntry);
            }
        }
        return tagRecordEntries;
    }

    private CompoundButton.OnCheckedChangeListener checkBoxOnCheckListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            RecyclerView.ViewHolder viewHolder =recyclerView.getChildViewHolder(((View) buttonView.getParent()));
            if(viewHolder != null) {
                int position = viewHolder.getAdapterPosition();
                if (position != RecyclerView.NO_ID)
                    tagItems.get(position).isChecked = isChecked;
            }
        }
    };
}
