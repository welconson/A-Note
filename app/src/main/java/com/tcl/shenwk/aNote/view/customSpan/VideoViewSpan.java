package com.tcl.shenwk.aNote.view.customSpan;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.view.View;

import com.tcl.shenwk.aNote.entry.ResourceDataEntry;
import com.tcl.shenwk.aNote.util.Constants;
import com.tcl.shenwk.aNote.util.FileUtil;

/**
 * Created by shenwk on 2018/3/19.
 */

public class VideoViewSpan extends ViewSpan {
    private final int mDuration;

    public VideoViewSpan(View view, ResourceDataEntry resourceDataEntry) {
        super(view, resourceDataEntry);
        mDuration = extractDurationByFilePath(resourceDataEntry.getPath());
    }

    public VideoViewSpan(View view, Uri uri, ResourceDataEntry resourceDataEntry) {
        super(view, uri, resourceDataEntry);
        resourceDataEntry.setFileName(FileUtil.getFileNameFromURI(view.getContext(), uri, resourceDataEntry.getDataType()));
        mDuration = extractDurationByUri(view.getContext(), uri);
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
        return Constants.RESOURCE_TYPE_VIDEO;
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

    public int getDuration() {
        return mDuration;
    }
}
