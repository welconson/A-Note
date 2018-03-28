package com.tcl.shenwk.aNote.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tcl.shenwk.aNote.R;
import com.tcl.shenwk.aNote.entity.NoteEntity;
import com.tcl.shenwk.aNote.entity.NoteTagEntity;
import com.tcl.shenwk.aNote.entity.TagRecordEntity;
import com.tcl.shenwk.aNote.model.ANoteDBManager;
import com.tcl.shenwk.aNote.util.DateUtil;
import com.tcl.shenwk.aNote.util.StringUtil;

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
    private NoteTagEntity hierarchyTagentity = null;
    private int tagNum = 0;
    private LayoutInflater inflater = null;
    private OnItemClickListener onItemClickListener = null;

    public TagManagerAdapter(List<NoteTagEntity> primaryTagEntries, List<NoteEntity> noteEntries, LayoutInflater inflater) {
        displayItems = new ArrayList<>();
        for(NoteTagEntity noteTagEntity : primaryTagEntries){
            displayItems.add(wrapItem(noteTagEntity));
        }
        tagNum = displayItems.size();
        for(NoteEntity noteEntity : noteEntries){
            displayItems.add(wrapItem(noteEntity));
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
            tagViewHolder.setTagName(tagItem.noteTagEntity.getTagName());
            if(tagItem.subTagEntries == null){
                tagItem.subTagEntries = ANoteDBManager.getInstance(context).queryAllSubTagByRootTagId(tagItem.noteTagEntity.getTagId());
            }
            if(tagItem.tagRecordEntries == null){
                tagItem.tagRecordEntries = ANoteDBManager.getInstance(context).queryTagRecordByTagId(tagItem.noteTagEntity.getTagId());
            }
            tagViewHolder.setInsideTagInfo("subtags: " + tagItem.subTagEntries.size() + " , notes: " + tagItem.tagRecordEntries.size());
            tagViewHolder.setTagCreatedTime(DateUtil.getInstance().getHumanReadableTimeString(context, tagItem.noteTagEntity.getCreateTime()));
        } else {
            NoteViewHolder noteViewHolder = (NoteViewHolder) baseTagManagerViewHolder;
            NoteItem noteItem = (NoteItem) displayItems.get(position);
            noteViewHolder.setNoteName(noteItem.noteEntity.getNoteTitle());
            noteViewHolder.setNoteCreatedTime(DateUtil.getInstance().getHumanReadableTimeString(context, noteItem.noteEntity.getCreateTimestamp()));
        }
    }

    @Override
    public int getItemCount() {
        return displayItems.size();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public NoteTagEntity getHierarchyTagentity() {
        return hierarchyTagentity;
    }

    public void setHierarchyTagentity(NoteTagEntity hierarchyTagentity) {
        this.hierarchyTagentity = hierarchyTagentity;
    }

    private abstract class DisplayItem {
        protected int type;

        public abstract void setNeedRebind();
    }

    public class NoteItem extends DisplayItem{
        public NoteEntity noteEntity;
        NoteItem() {
            type = TYPE_NOTE;
        }

        @Override
        public void setNeedRebind() {

        }
    }

    public class TagItem extends DisplayItem{
        private NoteTagEntity noteTagEntity;
        private List<NoteTagEntity> subTagEntries = null;
        private List<TagRecordEntity> tagRecordEntries = null;

        TagItem() {
            type = TYPE_TAG;
        }

        public NoteTagEntity getNoteTagEntity() {
            return noteTagEntity;
        }

        public List<NoteTagEntity> getSubTagEntries() {
            return subTagEntries;
        }

        public List<TagRecordEntity> getTagRecordEntries() {
            return tagRecordEntries;
        }

        @Override
        public void setNeedRebind(){
            subTagEntries = null;
            tagRecordEntries = null;
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

    public class TagViewHolder extends BaseTagManagerViewHolder implements View.OnClickListener, View.OnLongClickListener{
        TextView tagName;
        TextView insideTagInfo;
        TextView tagCreatedTime;

        TagViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
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

        @Override
        public boolean onLongClick(View v) {
            Log.i(TAG, "onLongClick: tag");
            if(onItemClickListener != null){
                onItemClickListener.onTagLongClick(v, getAdapterPosition(), ((TagItem) displayItems.get(getAdapterPosition())));
            }
            return true;
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
        void onTagLongClick(View v, int position, TagItem tagItem);
        void onNoteClick(int position, NoteItem noteItem);
    }

    public void refreshNoteItemByPosition(int position, NoteEntity noteEntity){
        if(noteEntity != null && position >= 0) {
            ((NoteItem) displayItems.get(position)).noteEntity = noteEntity;
            notifyItemChanged(position);
        }
    }

    public int isTagInList(String tagName) {
        int position = 0;
        for (DisplayItem tagItem : displayItems) {
            if (StringUtil.equal(tagName, ((TagItem) tagItem).noteTagEntity.getTagName()))
                return position;
            if(tagNum < ++position)
                break;
        }
        return -1;
    }

    private DisplayItem wrapItem(NoteTagEntity noteTagEntity){
        TagItem tagItem = new TagItem();
        tagItem.noteTagEntity = noteTagEntity;
        return tagItem;
    }

    private DisplayItem wrapItem(NoteEntity noteEntity){
        NoteItem noteItem = new NoteItem();
        noteItem.noteEntity = noteEntity;
        return noteItem;
    }

    public void insertItem(NoteTagEntity noteTagEntity) {
        if(noteTagEntity == null)
            return;
        displayItems.add(tagNum, wrapItem(noteTagEntity));
        notifyItemInserted(tagNum++);
    }

    public void insertItem(NoteEntity noteEntity){
        if(noteEntity == null)
            return;
        displayItems.add(tagNum, wrapItem(noteEntity));
        notifyItemInserted(tagNum);
    }

    public void removeItem(int position){
        if(displayItems.get(position).type == TYPE_TAG)
            tagNum--;
        displayItems.remove(position);
        notifyItemRemoved(position);
    }

    public void setNeedUpdateData(){
        for(DisplayItem tagItem : displayItems){
            tagItem.setNeedRebind();
        }
    }
}
