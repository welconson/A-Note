package com.tcl.shenwk.aNote.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.tcl.shenwk.aNote.R;
import com.tcl.shenwk.aNote.entity.NoteTagEntity;
import com.tcl.shenwk.aNote.entity.TagRecordEntity;
import com.tcl.shenwk.aNote.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapt to show tag Inside note edit activity.
 * Created by shenwk on 2018/3/20.
 */

public class TagRecordEditAdapter extends RecyclerView.Adapter {
    private LayoutInflater layoutInflater;
    private List<TagItem> tagItems;
    private OnItemClickListener onItemClickListener;
    private boolean hasRootTag = false;

    public TagRecordEditAdapter(LayoutInflater layoutInflater, List<TagItem> tagItems) {
        this.layoutInflater = layoutInflater;
        this.tagItems = tagItems;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        SelectTagViewHolder selectTagViewHolder = new SelectTagViewHolder(
                layoutInflater.inflate(R.layout.tag_edit_item, parent, false));
        return selectTagViewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        SelectTagViewHolder selectTagViewHolder = ((SelectTagViewHolder) holder);
        selectTagViewHolder.textView.setText(tagItems.get(position).noteTagEntity.getTagName());
        selectTagViewHolder.checkBox.setChecked(tagItems.get(position).isChecked);
    }

    @Override
    public int getItemCount() {
        return tagItems.size();
    }

    public void setHasRootTag(boolean hasRootTag) {
        this.hasRootTag = hasRootTag;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void addNewTag(NoteTagEntity noteTagEntity) {
        if (noteTagEntity == null)
            return;
        TagItem tagItem = new TagItem();
        tagItem.noteTagEntity = noteTagEntity;
        tagItem.isChecked = true;
        tagItem.tagRecordEntity = new TagRecordEntity(noteTagEntity.getTagId());
        tagItem.tagRecordEntity.status = TagRecordEntity.NEW_CREATE;
        tagItems.add(tagItem);
        notifyItemInserted(tagItems.size() - 1);
    }

    static class TagItem {
        NoteTagEntity noteTagEntity;
        TagRecordEntity tagRecordEntity = null;
        boolean isChecked = false;
    }

    public static List<TagItem> setItemList(List<NoteTagEntity> noteTagEntries,
                                            List<TagRecordEntity> tagRecordEntries) {
        List<TagItem> tagItems = new ArrayList<>();
        for (NoteTagEntity noteTagEntity : noteTagEntries) {
            TagItem tagItem = new TagItem();
            tagItem.noteTagEntity = noteTagEntity;
            if (tagRecordEntries != null) {
                for (TagRecordEntity tagRecordEntity : tagRecordEntries) {
                    if (tagRecordEntity.getTagId() == noteTagEntity.getTagId()) {
                        if(tagRecordEntity.status == TagRecordEntity.NEW_CREATE ||
                                tagRecordEntity.status == TagRecordEntity.NORMAL)
                            tagItem.isChecked = true;
                        tagItem.tagRecordEntity = tagRecordEntity;
                        break;
                    }
                }
            }
            tagItems.add(tagItem);
        }
        return tagItems;
    }

    /**
     * Set item list for sub tag adapter
     * @param noteTagEntries    sub tags under the primary tag.
     * @param tagRecordEntity    checked tag record entity through the tag hierarchy tree,
     *                          if no tag was checked, it should be null.
     * @return  Adapter data set list.
     */
    public static List<TagItem> setItemList(List<NoteTagEntity> noteTagEntries, TagRecordEntity tagRecordEntity){
        List<TagItem> tagItems = new ArrayList<>();
        for(NoteTagEntity noteTagEntity : noteTagEntries) {
            TagItem tagItem = new TagItem();
            if(tagRecordEntity != null && noteTagEntity.getTagId() == tagRecordEntity.getTagId()) {
                tagItem.isChecked = true;
            }
            tagItem.noteTagEntity = noteTagEntity;
            tagItems.add(tagItem);
        }

        return tagItems;
    }

    public void checkOnPosition(int position) {
        if(!hasRootTag) {
            tagItems.get(position).isChecked = true;
        } else {
            checkOnPositionUnderRootTag(position);
        }
        notifyItemChanged(position);
    }

    // Check under root tag, if there is other tag has been checked before, set isChecked to false.
    private void checkOnPositionUnderRootTag(int position){
        int oldPosition = unCheckTag();
        if(oldPosition != -1)
            notifyItemChanged(oldPosition);
        tagItems.get(position).isChecked = true;
        notifyItemChanged(position);
    }

    // un check all tags in tagItems.
    private int unCheckTag(){
        int oldPosition = -1;
        for(TagItem tagItem : tagItems){
            oldPosition++;
            if(tagItem.isChecked){
                tagItem.isChecked = false;
                break;
            }
        }
        return oldPosition;
    }

    public int getPositionByTagId(long tagId){
        int position = -1;
        for(TagItem tagItem : tagItems) {
            position++;
            if (tagId == tagItem.noteTagEntity.getTagId())
                break;
        }
        return position;
    }

    public void unCheckOnPosition(int position){
        if(position < 0)
            return;
        tagItems.get(position).isChecked = false;
        notifyItemChanged(position);
    }

    public int isTagInList(String tagName) {
        int position = 0;
        for (TagItem tagItem : tagItems) {
            if (StringUtil.equal(tagName, tagItem.noteTagEntity.getTagName()))
                return position;
            position++;
        }
        return -1;
    }

    public List<TagRecordEntity> getCheckedList() {
        List<TagRecordEntity> tagRecordEntries = new ArrayList<>();
        for (TagItem tagItem : tagItems) {
            TagRecordEntity tagRecordEntity = tagItem.tagRecordEntity;
            // If tagRecordEntity is not null, maybe it need to be stored, deleted or display.
            if(tagItem.tagRecordEntity != null){
                // Need to be stored
                if(tagRecordEntity.status == TagRecordEntity.NEW_CREATE && tagItem.isChecked){
                    tagRecordEntries.add(tagRecordEntity);
                }
                // Need to be deleted or nothing to to, depending on status.
                else if(tagRecordEntity.status == TagRecordEntity.NORMAL ||
                        tagRecordEntity.status == TagRecordEntity.TO_DELETE){
                    tagRecordEntity.status = tagItem.isChecked ? TagRecordEntity.NORMAL : TagRecordEntity.TO_DELETE;
                    tagRecordEntries.add(tagRecordEntity);
                }
            }
            // If tagRecordEntity is null but it is checked, it will be a new created record.
            else if (tagItem.isChecked) {
                tagRecordEntity = new TagRecordEntity(tagItem.noteTagEntity.getTagId());
                tagRecordEntity.status = TagRecordEntity.NEW_CREATE;
                tagRecordEntries.add(tagRecordEntity);
            }
        }
        return tagRecordEntries;
    }

    public TagRecordEntity getCheckedTagRecordUnderRootTag(){
        TagRecordEntity tagRecordEntity = null;
        for(TagItem item : tagItems){
            if(item.isChecked) {
                if(item.tagRecordEntity == null) {
                    tagRecordEntity = new TagRecordEntity();
                    tagRecordEntity.setTagId(item.noteTagEntity.getTagId());
                    tagRecordEntity.status = TagRecordEntity.NEW_CREATE;
                }
                else tagRecordEntity = item.tagRecordEntity;
                break;
            }
        }
        return tagRecordEntity;
    }

    public class SelectTagViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, CompoundButton.OnCheckedChangeListener{
        TextView textView;
        CheckBox checkBox;
        SelectTagViewHolder(View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkBox);
            textView = itemView.findViewById(R.id.tag_item_name);
            checkBox.setOnCheckedChangeListener(this);
            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View v) {
            TagRecordEntity tagRecordEntity = null;
            if(onItemClickListener != null){
                TagItem tagItem = tagItems.get(getAdapterPosition());
                if(checkBox.isChecked()){
                    if(tagItem.tagRecordEntity == null) {
                        tagItem.tagRecordEntity = new TagRecordEntity();
                        tagItem.tagRecordEntity.setTagId(tagItem.noteTagEntity.getTagId());
                        tagItem.tagRecordEntity.status = TagRecordEntity.NEW_CREATE;
                        tagRecordEntity = tagItem.tagRecordEntity;
                    }
                    else tagRecordEntity = tagItem.tagRecordEntity;
                }
                onItemClickListener.onItemClick(tagItems.get(getAdapterPosition()).noteTagEntity, tagRecordEntity);
            }
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            int position = getAdapterPosition();
            if(hasRootTag && isChecked) {
                // if it is not a root tags adapter, undo check of other tags.
                int oldPosition = unCheckTag();
                if(oldPosition != -1 && position != oldPosition)
                    notifyItemChanged(oldPosition);
            }
            tagItems.get(position).isChecked = isChecked;
        }
    }

    public interface OnItemClickListener{
        void onItemClick(NoteTagEntity noteTagEntity, TagRecordEntity tagRecordEntity);
    }
}
