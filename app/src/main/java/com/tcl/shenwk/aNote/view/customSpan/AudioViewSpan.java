package com.tcl.shenwk.aNote.view.customSpan;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.tcl.shenwk.aNote.R;
import com.tcl.shenwk.aNote.entity.ResourceDataEntity;
import com.tcl.shenwk.aNote.util.Constants;
import com.tcl.shenwk.aNote.util.FileUtil;

/**
 * Used for Audio as a span to replace the [audio] tag.
 * Implement onTouch and onClick events, to response user's operation.
 * Created by shenwk on 2018/3/8.
 */

public class AudioViewSpan extends ViewSpan {
    private static final String TAG = "AudioViewSpan";
    private final int mDuration;
    private OnClickListener onClickListener = null;

    public AudioViewSpan(View view, Uri uri, ResourceDataEntity resourceDataEntity) {
        super(view, uri, resourceDataEntity);
        mDuration = extractDurationByUri(view.getContext(), uri);
        resourceDataEntity.setDuration(mDuration);
        resourceDataEntity.setFileName(FileUtil.getFileNameFromURI(view.getContext(),
                uri, resourceDataEntity.getDataType()));
        Log.i(TAG, "AudioViewSpan: mDuration = " + mDuration);

        init();
    }

    public AudioViewSpan(View view, ResourceDataEntity resourceDataEntity){
        super(view, resourceDataEntity);
        mDuration = resourceDataEntity.getDuration();
        Log.i(TAG, "AudioViewSpan: mDuration = " + mDuration);

        init();
    }

    @Override
    public void measure() {
        View view = getView();
        float density = view.getResources().getDisplayMetrics().density;
        final int widthSpec = View.MeasureSpec.makeMeasureSpec((int)(Constants.VIEW_SPAN_WIDTH * density), View.MeasureSpec.EXACTLY);
        final int heightSpec = View.MeasureSpec.makeMeasureSpec((int)(Constants.VIEW_SPAN_HEIGHT * density), View.MeasureSpec.EXACTLY);
        view.measure(widthSpec, heightSpec);
    }

    @Override
    public int getResourceDataType() {
        return Constants.RESOURCE_TYPE_AUDIO;
    }

    public int getDuration() {
        return mDuration;
    }

    private void init(){
        View play = getView().findViewById(R.id.open_button);
        if(play != null) {
            play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(onClickListener != null){
                        Uri uri = getResourceDataUri();
                        if(uri == null)
                            uri = FileUtil.generateUriFromFilePath(getFilePath());
                        onClickListener.onPlayClick(v, uri, getFilePath());
                    }
                }
            });
            play.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    String action;
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            action = "ACTION_DOWN";
                            break;
                        case MotionEvent.ACTION_UP:
                            action = "ACTION_UP";
                            v.performClick();
                            break;
                        case MotionEvent.ACTION_CANCEL:
                            action = "ACTION_CANCEL";
                            break;
                        default:
                            action = "other action";
                    }
                    Log.i(TAG, "onTouch: AudioViewSpan " + event.getAction() + "  " + action);
                    return true;
                }
            });
        }
        else{
            Log.i(TAG, "AudioViewSpan: error layout xml file for AudioViewSpan");
        }
    }

    private int extractDurationByUri(Context context, Uri uri){
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(context, uri);
        String durationString = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        return durationString == null ? 0 : Integer.valueOf(durationString);
    }

    private int extractDurationByFilePath(String filePath){
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(filePath);
        String durationString = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        return durationString == null ? 0 : Integer.valueOf(durationString);
    }

    public interface OnClickListener{
        void onPlayClick(View v, Uri uri, String filePath);
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }
}
