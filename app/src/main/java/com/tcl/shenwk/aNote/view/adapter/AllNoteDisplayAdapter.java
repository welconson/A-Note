package com.tcl.shenwk.aNote.view.adapter;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tcl.shenwk.aNote.R;
import com.tcl.shenwk.aNote.data.DataProvider;
import com.tcl.shenwk.aNote.entity.NoteEntity;
import com.tcl.shenwk.aNote.entity.ResourceDataEntity;
import com.tcl.shenwk.aNote.manager.LoginManager;
import com.tcl.shenwk.aNote.manager.SyncManager;
import com.tcl.shenwk.aNote.task.DownloadTask;
import com.tcl.shenwk.aNote.util.Constants;
import com.tcl.shenwk.aNote.util.FileUtil;
import com.tcl.shenwk.aNote.util.UrlSource;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Adapter used for holding note preview items.
 * Created by shenwk on 2018/2/6.
 */

public class AllNoteDisplayAdapter extends RecyclerView.Adapter {
    private static String TAG = "AllNoteDisplayAdapter";

    private static final int MSG_DOWNLOAD_FINISHED = 1;
    private static final int MSG_DOWNLOAD_ERROR = 2;

    private LayoutInflater mInflater = null;
    private List<PreviewNoteItem> mPreviewNoteItemList;
    private OnItemClickListener onItemClickListener;
    private Handler handler;

    public AllNoteDisplayAdapter(LayoutInflater inflater, List<PreviewNoteItem> previewNoteEntries) {
        super();
        mInflater = inflater;
        mPreviewNoteItemList = previewNoteEntries;
        handler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case MSG_DOWNLOAD_FINISHED:{
                        notifyItemChanged(msg.arg1);
                        break;
                    }
                    case MSG_DOWNLOAD_ERROR:{
                        break;
                    }
                }
            }
        };
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
        setViewItemDisplay(previewNoteItem, (NoteDisplayViewHolder) holder, position);
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

    private void setViewItemDisplay(PreviewNoteItem previewNoteItem, NoteDisplayViewHolder noteDisplayViewHolder, final int position){
        NoteEntity noteEntity = previewNoteItem.noteEntity;
        TextView textView = noteDisplayViewHolder.itemTitle;
        if(noteEntity.getNoteTitle() == null || noteEntity.getNoteTitle().equals("")){
            textView.setText(R.string.item_no_title);
        }
        else textView.setText(noteEntity.getNoteTitle());
        textView = noteDisplayViewHolder.itemText;
        Context context = noteDisplayViewHolder.itemImage.getContext();
        // check if there is local file, if not turn to server get content file
        if(!FileUtil.isFileOrDirectoryExist(FileUtil.getNoteContentPath(context, noteEntity.getNoteDirName()))){
            if(!FileUtil.isFileOrDirectoryExist(FileUtil.getNoteDirPath(context, noteEntity.getNoteDirName()))){
                FileUtil.createDir(FileUtil.getNoteDirPath(context, noteEntity.getNoteDirName()));
                FileUtil.createDir(FileUtil.getNoteDirPath(context, noteEntity.getNoteDirName()) + File.separator + Constants.RESOURCE_DIR);
            }
            try {
                SyncManager.getInstance(context).realTimeDownload(
                        new URL(UrlSource.URL_SYNC_DOWNLOAD),
                        noteEntity.getNoteDirName() + File.separator + Constants.CONTENT_FILE_NAME,
                        FileUtil.getNoteContentPath(context, noteEntity.getNoteDirName()),
                        new DownloadTask.OnFinishListener() {
                            @Override
                            public void onSuccess() {
                                Log.i(TAG, "onSuccess: download successfully");
                                Message message = handler.obtainMessage(MSG_DOWNLOAD_FINISHED);
                                message.arg1 = position;
                                handler.sendMessage(message);
                            }

                            @Override
                            public void onError(String err) {
                                Log.i(TAG, "onError: download error");
                                Message message = handler.obtainMessage(MSG_DOWNLOAD_ERROR);
                                message.arg1 = position;
                                handler.sendMessage(message);
                            }
                        }
                );
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }else {
            textView.setText(generatePreviewText(FileUtil.readFile(
                    FileUtil.getNoteContentPath(context, noteEntity.getNoteDirName())),
                    previewNoteItem.preResourceDataEntries));

            if (previewNoteItem.preResourceDataEntries != null && previewNoteItem.preResourceDataEntries.size() != 0) {
                ImageView imageView = noteDisplayViewHolder.itemImage;
                setItemInfoLayoutParameterWithResource(noteDisplayViewHolder.itemInfo);
                switch (previewNoteItem.preResourceDataEntries.get(0).getDataType()) {
                    case Constants.RESOURCE_TYPE_IMAGE:
                        imageView.setImageBitmap(BitmapFactory.decodeFile(
                                FileUtil.getResourcePath(context, previewNoteItem.preResourceDataEntries.get(0).getResourceRelativePath())));
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
            } else {
                ImageView imageView = noteDisplayViewHolder.itemImage;
                imageView.setBackground(null);
                imageView.setImageDrawable(null);
                setItemInfoLayoutParameterToNoResource(noteDisplayViewHolder.itemInfo);
            }
        }
    }

    /**
     * Adjust the ItemInfo without resource data's thumbnail displaying, expanding the itemInfo view.
     * ItemInfo contains the title and preview content and its displaying will corresponding to
     * resource data's thumbnail Once there is a resource data inside the note,
     * we will display the first resource data's thumbnail, and adjust the ItemInfo's position.
     * @param itemInfoView  viewItem to be adjusted.
     */
    private void setItemInfoLayoutParameterToNoResource(View itemInfoView){
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)itemInfoView.getLayoutParams();
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE);
        itemInfoView.setLayoutParams(layoutParams);
    }

    /**
     * Adjust the ItemInfo with Resource data's thumbnail displaying,cutting down the itemInfo view.
     * See comment above.
     * @param itemInfoView  viewItem to be adjusted.
     */
    private void setItemInfoLayoutParameterWithResource(View itemInfoView){
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)itemInfoView.getLayoutParams();
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END, 0);
        itemInfoView.setLayoutParams(layoutParams);
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
        TextView itemTitle;
        TextView itemText;
        ImageView itemImage;
        View itemInfo;
        public NoteDisplayViewHolder(View itemView) {
            super(itemView);
            itemTitle = itemView.findViewById(R.id.item_title);
            itemText = itemView.findViewById(R.id.item_text);
            itemImage = itemView.findViewById(R.id.item_image);
            itemInfo = itemView.findViewById(R.id.item_info);
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

    public void reload(){
        mPreviewNoteItemList.clear();
        mPreviewNoteItemList = DataProvider.getInstance(this.mInflater.getContext()).transformNoteEntityToPreviewList(
                DataProvider.getInstance(mInflater.getContext()).getAllNoteEntities()
        );
        notifyDataSetChanged();
    }
}
