package com.tcl.shenwk.aNote.view.adapter;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tcl.shenwk.aNote.R;
import com.tcl.shenwk.aNote.entity.NoteEntity;
import com.tcl.shenwk.aNote.entity.ResourceDataEntity;
import com.tcl.shenwk.aNote.model.NoteHandler;
import com.tcl.shenwk.aNote.util.Constants;
import com.tcl.shenwk.aNote.util.FileUtil;

import java.util.List;

/**
 * Adapter used for holding note preview items.
 * Created by shenwk on 2018/2/6.
 */

public class AllNoteDisplayAdapter extends RecyclerView.Adapter {
    private static String TAG = "AllNoteDisplayAdapter";
    private LayoutInflater mInflater = null;
    private List<PreviewNoteItem> mPreviewNoteItemList;
    private OnItemClickListener onItemClickListener;

    public AllNoteDisplayAdapter(LayoutInflater inflater, List<PreviewNoteItem> previewNoteEntries) {
        super();
        mInflater = inflater;
        mPreviewNoteItemList = previewNoteEntries;
    }

    @Override
    public NoteDisplayViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View item = mInflater.inflate(R.layout.note_preview_item, parent, false);
        return new NoteDisplayViewHolder(item);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        View viewItem = holder.itemView;
        PreviewNoteItem previewNoteItem = mPreviewNoteItemList.get(position);
        setViewItemDisplay(previewNoteItem, viewItem);
    }

    @Override
    public int getItemCount() {
        return mPreviewNoteItemList.size();
    }

    public void refreshSingleItemByPosition(int position, PreviewNoteItem previewNoteItem){
        if(position < 0 || previewNoteItem == null)
            return;
        mPreviewNoteItemList.set(position, previewNoteItem);
        notifyItemChanged(position);
    }

    public void addItem(PreviewNoteItem previewNoteItem){
        if(previewNoteItem == null)
            return;
        mPreviewNoteItemList.add(Constants.ITEM_BEGIN_POSITION, previewNoteItem);
        notifyItemInserted(Constants.ITEM_BEGIN_POSITION);
    }

    private void setViewItemDisplay(PreviewNoteItem previewNoteItem, View viewItem){
        NoteEntity noteEntity = previewNoteItem.noteEntity;
        TextView textView = viewItem.findViewById(R.id.item_title);
        if(noteEntity.getNoteTitle() == null || noteEntity.getNoteTitle().equals("")){
            textView.setText(R.string.item_no_title);
        }
        else textView.setText(noteEntity.getNoteTitle());
        textView = viewItem.findViewById(R.id.item_text);
        Context context = viewItem.getContext();
        textView.setText(generatePreviewText(FileUtil.readFile(
                FileUtil.getContentFileName(noteEntity.getNotePath())),
                previewNoteItem.preResourceDataEntries));

        if(previewNoteItem.preResourceDataEntries != null && previewNoteItem.preResourceDataEntries.size() != 0) {
            ImageView imageView = viewItem.findViewById(R.id.item_image);
            setItemInfoLayoutParameterWithResource(viewItem);
            switch (previewNoteItem.preResourceDataEntries.get(0).getDataType()){
                case Constants.RESOURCE_TYPE_IMAGE:
                    imageView.setImageBitmap(BitmapFactory.decodeFile(
                            previewNoteItem.preResourceDataEntries.get(0).getPath()));
                    break;
                case Constants.RESOURCE_TYPE_AUDIO:
                    imageView.setBackground(context.getDrawable(R.color.primaryGrey));
                    imageView.setImageDrawable(context.getDrawable(R.drawable.ic_audiotrack));
                    break;
                case Constants.RESOURCE_TYPE_VIDEO:
                    imageView.setBackground(context.getDrawable(R.color.primaryGrey));
                    imageView.setImageDrawable(context.getDrawable(R.drawable.ic_videocam));
                    break;
                case Constants.RESOURCE_TYPE_FILE:
                    imageView.setBackground(context.getDrawable(R.color.primaryGrey));
                    imageView.setImageDrawable(context.getDrawable(R.drawable.ic_insert_drive_file));
                    break;
                    default:
                        imageView.setBackground(null);
                        imageView.setImageDrawable(null);
            }
        }
        else{
            ImageView imageView = viewItem.findViewById(R.id.item_image);
            imageView.setBackground(null);
            imageView.setImageDrawable(null);
            setItemInfoLayoutParameterToNoResource(viewItem);
        }
    }

    /**
     * Adjust the ItemInfo without resource data's thumbnail displaying, expanding the itemInfo view.
     * ItemInfo contains the title and preview content and its displaying will corresponding to
     * resource data's thumbnail Once there is a resource data inside the note,
     * we will display the first resource data's thumbnail, and adjust the ItemInfo's position.
     * @param viewItem  viewItem to be adjusted.
     */
    private void setItemInfoLayoutParameterToNoResource(View viewItem){
        View view = viewItem.findViewById(R.id.item_info);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE);
        view.setLayoutParams(layoutParams);
    }

    /**
     * Adjust the ItemInfo with Resource data's thumbnail displaying,cutting down the itemInfo view.
     * See comment above.
     * @param viewItem  viewItem to be adjusted.
     */
    private void setItemInfoLayoutParameterWithResource(View viewItem){
        View view = viewItem.findViewById(R.id.item_info);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)view.getLayoutParams();
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END, 0);
    }

    /**
     * Extract preview content string from the original content, replace span tag with backspace.
     * Since there is span tag information inside content files, and we don't want to should these
     * information. So before display we will do some operation to simply replace it with a backspace.
     * @param oriContent    original content string.
     * @param resourceDataEntries   resource list of the itemView.
     * @return  preview content string.
     */
    private String generatePreviewText(String oriContent, List<ResourceDataEntity> resourceDataEntries){
        String previewText = "";
        if(oriContent != null) {
            int spanCount = 0;
            int end = 0;
            int spanLength = 0;
            int resourceSize = resourceDataEntries.size();
            while (end < Constants.PREVIEW_CONTENT_TEXT_LENGTH) {
                if(resourceSize > spanCount) {
                    ResourceDataEntity resourceDataEntity = resourceDataEntries.get(spanCount);
                    int spanStart = resourceDataEntity.getSpanStart();
                    if (spanStart < Constants.PREVIEW_CONTENT_TEXT_LENGTH - spanLength) {
                        int spanEnd;
                        String tag = Constants.RESOURCE_TAG;
                        spanLength += tag.length();
                        previewText += oriContent.substring(end, spanStart) + " ";
                        spanEnd = spanStart + tag.length();
                        end = spanEnd;
                        spanCount++;
                    } else {
                        previewText += oriContent.substring(end, spanStart);
                        break;
                    }
                }
                else{
                    int preLength;
                    if(Constants.PREVIEW_CONTENT_TEXT_LENGTH + spanLength + spanCount> oriContent.length())
                        preLength = oriContent.length();
                    else
                        preLength = Constants.PREVIEW_CONTENT_TEXT_LENGTH + spanLength + spanCount;
                    previewText += oriContent.substring(end, preLength);
                    break;
                }
            }
        }
        Log.i(TAG, "generatePreviewText: preview text length " + previewText.length());
        return previewText;
    }

    public class NoteDisplayViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{
        public NoteDisplayViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(onItemClickListener != null){
                onItemClickListener.onItemClick(getAdapterPosition(), v);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if(onItemClickListener != null){
                onItemClickListener.onItemLongClick(getAdapterPosition(), v);
            }
            return true;
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position, View v);
        void onItemLongClick(int position, View v);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public static class PreviewNoteItem {
        public NoteEntity noteEntity;
        public List<ResourceDataEntity> preResourceDataEntries;
    }
    
    public PreviewNoteItem getItemByPosition(int position){
        if(position < 0 || position >= mPreviewNoteItemList.size())
            return  null;
        else return mPreviewNoteItemList.get(position);
    }

    public void removeItemByPosition(int position){
        if(position < 0 || position >= mPreviewNoteItemList.size()){
            return;
        }
        else mPreviewNoteItemList.remove(position);
    }
}
