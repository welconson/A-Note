package com.tcl.shenwk.aNote.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tcl.shenwk.aNote.R;
import com.tcl.shenwk.aNote.entry.NoteEntry;
import com.tcl.shenwk.aNote.entry.NoteTagEntry;
import com.tcl.shenwk.aNote.entry.TagRecordEntry;
import com.tcl.shenwk.aNote.model.ANoteDBManager;
import com.tcl.shenwk.aNote.util.DateUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter of tag manager's RecyclerView.
 * Created by shenwk on 2018/3/23.
 */

public class TagManagerAdapter extends RecyclerView.Adapter {
    private static final String TAG = "TagManagerAdapter";
    private static final int NO_NUMBER = -1;
    private static final int TYPE_TAG = 1;
    private static final int TYPE_NOTE = 2;
    private List<DisplayItem> displayItems;
    private List<NoteEntry> noteEntries;
    private LayoutInflater inflater = null;
    private OnItemClickListener onItemClickListener = null;

    public TagManagerAdapter(List<NoteTagEntry> primaryTagEntries, List<NoteEntry> noteEntries, LayoutInflater inflater) {
        displayItems = new ArrayList<>();
        for(NoteTagEntry noteTagEntry : primaryTagEntries){
            TagItem displayItem = new TagItem();
            displayItem.noteTagEntry = noteTagEntry;
            displayItems.add(displayItem);
        }
        for(NoteEntry noteEntry : noteEntries){
            NoteItem noteItem = new NoteItem();
            noteItem.noteEntry = noteEntry;
            displayItems.add(noteItem);
        }
        this.inflater = inflater;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == TYPE_TAG)
            return new TagViewHolder(
                    inflater.inflate(R.layout.tag_display_tag_item, parent, false));
        else return new NoteViewHolder(
                inflater.inflate(R.layout.tag_display_note_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        BaseTagManagerViewHolder baseTagManagerViewHolder = ((BaseTagManagerViewHolder) holder);
        Context context = holder.itemView.getContext();
        if(baseTagManagerViewHolder.type == TYPE_TAG){
            TagViewHolder tagViewHolder = (TagViewHolder) baseTagManagerViewHolder;
            TagItem tagItem = (TagItem) displayItems.get(position);
            tagViewHolder.setTagName(tagItem.noteTagEntry.getTagName());
            if(tagItem.subTagEntries == null){
                tagItem.subTagEntries = ANoteDBManager.getInstance(context).queryAllSubTagByTagId(tagItem.noteTagEntry.getTagId());
            }
            if(tagItem.tagRecordEntries == null){
                tagItem.tagRecordEntries = ANoteDBManager.getInstance(context).queryTagRecordByTagId(tagItem.noteTagEntry.getTagId());
            }
            tagViewHolder.setInsideTagInfo("subtags: " + tagItem.subTagEntries.size() + " , notes: " + tagItem.tagRecordEntries.size());
            tagViewHolder.setTagCreatedTime(DateUtil.getInstance().getHumanReadableTimeString(context, tagItem.noteTagEntry.getCreateTime()));
        } else {
            NoteViewHolder noteViewHolder = (NoteViewHolder) baseTagManagerViewHolder;
            NoteItem noteItem = (NoteItem) displayItems.get(position);
            noteViewHolder.setNoteName(noteItem.noteEntry.getNoteTitle());
            noteViewHolder.setNoteCreatedTime(DateUtil.getInstance().getHumanReadableTimeString(context, noteItem.noteEntry.getCreateTimestamp()));
        }
    }

    @Override
    public int getItemCount() {
        return displayItems.size();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    private class DisplayItem {
        protected int type;
    }

    public class NoteItem extends DisplayItem{
        public NoteEntry noteEntry;
        NoteItem() {
            type = TYPE_NOTE;
        }
    }

    public class TagItem extends DisplayItem{
        private NoteTagEntry noteTagEntry;
        private List<NoteTagEntry> subTagEntries = null;
        private List<TagRecordEntry> tagRecordEntries = null;

        TagItem() {
            type = TYPE_TAG;
        }

        public NoteTagEntry getNoteTagEntry() {
            return noteTagEntry;
        }

        public List<NoteTagEntry> getSubTagEntries() {
            return subTagEntries;
        }

        public List<TagRecordEntry> getTagRecordEntries() {
            return tagRecordEntries;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return displayItems.get(position).type;
    }

    public class BaseTagManagerViewHolder extends RecyclerView.ViewHolder{
        public int type;

        BaseTagManagerViewHolder(View itemView) {
            super(itemView);
        }
    }

    public class TagViewHolder extends BaseTagManagerViewHolder implements View.OnClickListener{
        TextView tagName;
        TextView insideTagInfo;
        TextView tagCreatedTime;

        TagViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            tagName = itemView.findViewById(R.id.tag_name);
            insideTagInfo = itemView.findViewById(R.id.inside_item_info);
            tagCreatedTime = itemView.findViewById(R.id.tag_created_time);
            type = TYPE_TAG;
        }

        public void setTagName(String tagName) {
            this.tagName.setText(tagName);
        }

        public void setInsideTagInfo(String insideTagInfo) {
            this.insideTagInfo.setText(insideTagInfo);
        }

        public void setTagCreatedTime(String tagCreatedTime) {
            this.tagCreatedTime.setText(tagCreatedTime);
        }

        @Override
        public void onClick(View v) {
            Log.i(TAG, "onClick: tag");
            if(onItemClickListener != null){
                onItemClickListener.onTagClick((TagItem) displayItems.get(getAdapterPosition()));
            }
        }
    }

    public class NoteViewHolder extends BaseTagManagerViewHolder implements View.OnClickListener{
        TextView noteName;
        TextView noteCreatedTime;

        NoteViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            type = TYPE_NOTE;
            noteName = itemView.findViewById(R.id.note_name);
            noteCreatedTime = itemView.findViewById(R.id.note_created_time);
        }
        public void setNoteName(String noteName) {
            this.noteName.setText(noteName);
        }

        public void setNoteCreatedTime(String noteCreatedTime) {
            this.noteCreatedTime.setText(noteCreatedTime);
        }

        @Override
        public void onClick(View v) {
            Log.i(TAG, "onClick: note");
            if(onItemClickListener != null){
                onItemClickListener.onNoteClick(getAdapterPosition(), (NoteItem) displayItems.get(getAdapterPosition()));
            }
        }
    }

    public interface OnItemClickListener {
        void onTagClick(TagItem tagItem);
        void onNoteClick(int position, NoteItem noteItem);
    }

    public void refreshSingleItemByPosition(int position, NoteEntry noteEntry){
        if(noteEntry != null && position >= 0) {
            ((NoteItem) displayItems.get(position)).noteEntry = noteEntry;
            notifyItemChanged(position);
        }
    }
}
