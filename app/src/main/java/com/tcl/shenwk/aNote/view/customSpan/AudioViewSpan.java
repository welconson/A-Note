package com.tcl.shenwk.aNote.view.customSpan;

import android.content.res.Resources;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.tcl.shenwk.aNote.R;
import com.tcl.shenwk.aNote.util.Constants;
import com.tcl.shenwk.aNote.view.activity.EditNoteActivity;

/**
 * Used for Audio as a span to replace the [audio] tag.
 * Implement onTouch and onClick events, to response user's operation.
 * Created by shenwk on 2018/3/8.
 */

public class AudioViewSpan extends ViewSpan {
    private static final String TAG = "AudioViewSpan";
    private final int mDuration;

    public AudioViewSpan(View view, Uri uri) {
        super(view, uri);
        // Invoke MediaMetadataRetriever to get concrete information about the media resource
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(view.getContext(), uri);
        String durationString = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        mDuration = durationString == null ? 0 : Integer.valueOf(durationString);
        Log.i(TAG, "AudioViewSpan: mDuration = " + mDuration);

        init();
    }

    public AudioViewSpan(View view, String fileName, String filePath){
        super(view, fileName, filePath);
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(filePath);
        String durationString = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        mDuration = durationString == null ? 0 : Integer.valueOf(durationString);
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
        View play = getView().findViewById(R.id.play_audio);
        if(play != null) {
            play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "onClick: AudioViewSpan");
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
}
