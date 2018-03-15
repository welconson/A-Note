package com.tcl.shenwk.aNote.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
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
import com.tcl.shenwk.aNote.entry.NoteEntry;
import com.tcl.shenwk.aNote.entry.ResourceDataEntry;
import com.tcl.shenwk.aNote.model.NoteHandler;
import com.tcl.shenwk.aNote.util.Constants;
import com.tcl.shenwk.aNote.util.FileUtil;
import com.tcl.shenwk.aNote.view.activity.EditNoteActivity;
import com.tcl.shenwk.aNote.view.activity.HomePageActivity;

import java.util.List;

/**
 * Adapter used for holding note preview items.
 * Created by shenwk on 2018/2/6.
 */

public class CustomAdapter extends RecyclerView.Adapter<CustomViewHolder> {
    private static String TAG = "CustomAdapter";
    private LayoutInflater mInflater = null;
    private List<HomePageActivity.PreviewNoteEntry> mPreviewNoteEntryList;

    public CustomAdapter(LayoutInflater inflater, List<HomePageActivity.PreviewNoteEntry> previewNoteEntries) {
        super();
        mInflater = inflater;
        mPreviewNoteEntryList = previewNoteEntries;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View item = mInflater.inflate(R.layout.note_preview_item, parent, false);
        return new CustomViewHolder(item);
    }

    @Override
    public void onBindViewHolder(final CustomViewHolder holder, int position) {
        View viewItem = holder.itemView;
        HomePageActivity.PreviewNoteEntry previewNoteEntry = mPreviewNoteEntryList.get(position);
        setViewItemDisplay(previewNoteEntry, viewItem);
        setViewItemInteraction(viewItem, holder);
    }

    @Override
    public int getItemCount() {
        return mPreviewNoteEntryList.size();
    }

    public void refreshSingleItemByPosition(int position, HomePageActivity.PreviewNoteEntry previewNoteEntry){
        if(position < 0 || previewNoteEntry == null)
            return;
        mPreviewNoteEntryList.set(position, previewNoteEntry);
        notifyItemChanged(position);
    }

    public void addItem(HomePageActivity.PreviewNoteEntry previewNoteEntry){
        if(previewNoteEntry == null)
            return;
        mPreviewNoteEntryList.add(Constants.ITEM_BEGIN_POSITION, previewNoteEntry);
        notifyItemInserted(Constants.ITEM_BEGIN_POSITION);
    }

    private void setViewItemDisplay(HomePageActivity.PreviewNoteEntry previewNoteEntry, View viewItem){
        NoteEntry noteEntry = previewNoteEntry.noteEntry;
        TextView textView = viewItem.findViewById(R.id.item_title);
        if(noteEntry.getNoteTitle() == null || noteEntry.getNoteTitle().equals("")){
            textView.setText(R.string.item_no_title);
        }
        else textView.setText(noteEntry.getNoteTitle());
        textView = viewItem.findViewById(R.id.item_text);
        Context context = viewItem.getContext();
        textView.setText(generatePreviewText(FileUtil.readFile(
                FileUtil.getContentFileName(noteEntry.getNotePath())),
                previewNoteEntry.preResourceDataEntries));

        if(previewNoteEntry.preResourceDataEntries != null && previewNoteEntry.preResourceDataEntries.size() != 0) {
            ImageView imageView = viewItem.findViewById(R.id.item_image);
            setItemInfoLayoutParameterWithResource(viewItem);
            switch (previewNoteEntry.preResourceDataEntries.get(0).getDataType()){
                case Constants.RESOURCE_TYPE_IMAGE:
                    imageView.setBackground(context.getDrawable(R.color.colorPrimary));
                    imageView.setBackground(Drawable.createFromPath(
                            previewNoteEntry.preResourceDataEntries.get(0).getFileName()));
                    break;
                case Constants.RESOURCE_TYPE_AUDIO:
                    imageView.setBackground(context.getDrawable(R.color.colorPrimary));
                    imageView.setImageDrawable(context.getDrawable(R.drawable.ic_audiotrack));
                    break;
                case Constants.RESOURCE_TYPE_VIDEO:
                    imageView.setBackground(context.getDrawable(R.color.colorPrimary));
                    imageView.setImageDrawable(context.getDrawable(R.drawable.ic_videocam));
                    break;
                case Constants.RESOURCE_TYPE_FILE:
                    imageView.setBackground(context.getDrawable(R.color.colorPrimary));
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

    private void setViewItemInteraction(View viewItem, final RecyclerView.ViewHolder holder){
        viewItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                NoteEntry noteEntry = mPreviewNoteEntryList.get(position).noteEntry;
                Intent intent = new Intent(v.getContext(), EditNoteActivity.class);
                intent.putExtra(Constants.ACTION_EDIT_NOTE, EditNoteActivity.EDIT_TYPE_MODIFY);
                intent.putExtra(Constants.EDIT_NOTE_ID, noteEntry.getNoteId());
                intent.putExtra(Constants.ITEM_NOTE_ENTRY, noteEntry);
                intent.putExtra(Constants.ITEM_POSITION, position);
                v.getContext().startActivity(intent);
            }
        });
        viewItem.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {
                PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
                Menu menu = popupMenu.getMenu();
                menu.add("delete");
                menu.getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int position = holder.getAdapterPosition();
                        NoteEntry noteEntry = mPreviewNoteEntryList.get(position).noteEntry;
                        Log.i(TAG, "onMenuItemClick: delete onClick");
                        NoteHandler.removeNote(v.getContext(), noteEntry);
                        mPreviewNoteEntryList.remove(position);
                        notifyItemRemoved(position);
                        return true;
                    }
                });
                menu.add("detail");
                popupMenu.setGravity(Gravity.END);
                popupMenu.show();
                return true;
            }
        });
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
    private String generatePreviewText(String oriContent, List<ResourceDataEntry> resourceDataEntries){
        String previewText = "";
        if(oriContent != null) {
            int spanCount = 0;
            int end = 0;
            int spanLength = 0;
            int resourceSize = resourceDataEntries.size();
            while (end < Constants.PREVIEW_CONTENT_TEXT_LENGTH) {
                if(resourceSize > spanCount) {
                    ResourceDataEntry resourceDataEntry = resourceDataEntries.get(spanCount);
                    int spanStart = resourceDataEntry.getSpanStart();
                    if (spanStart < Constants.PREVIEW_CONTENT_TEXT_LENGTH - spanLength) {
                        int spanEnd = spanStart;
                        String tag = getResourceTag(resourceDataEntry.getDataType());
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

    /**
     * Get the span tag string corresponding to the span type.
     * @param type  span type.
     * @return  span tag.
     */
    private String getResourceTag(int type) {
        String tag = "";
        switch (type) {
            case Constants.RESOURCE_TYPE_IMAGE:
                tag = Constants.IMAGE_SPAN_TAG;
                break;
            case Constants.RESOURCE_TYPE_AUDIO:
                tag = Constants.AUDIO_SPAN_TAG;
                break;
            case Constants.RESOURCE_TYPE_VIDEO:
                tag = Constants.VIDEO_SPAN_TAG;
                break;
            case Constants.RESOURCE_TYPE_FILE:
                tag = Constants.FILE_SPAN_TAG;
                break;
        }
        return tag;
    }
}
