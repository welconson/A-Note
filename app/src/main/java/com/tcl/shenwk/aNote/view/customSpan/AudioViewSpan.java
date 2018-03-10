package com.tcl.shenwk.aNote.view.customSpan;

import android.content.res.Resources;
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
 * Created by shenwk on 2018/3/8.
 */

public class AudioViewSpan extends ViewSpan {
    private static final String TAG = "AudioViewSpan";

    public AudioViewSpan(View view, int x, int y) {
        super(view, x, y);
        View play = view.findViewById(R.id.play_audio);
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
                        action = "ACTION_DOWN";break;
                    case MotionEvent.ACTION_UP:
                        action = "ACTION_UP";v.performClick();break;
                    case MotionEvent.ACTION_CANCEL:
                        action = "ACTION_CANCEL";break;
                        default:action = "other action";
                }
                Log.i(TAG, "onTouch: AudioViewSpan " + event.getAction() + "  " + action);
                return true;
            }
        });
    }

    @Override
    public void measure() {
        View view = getView();
        float density = view.getResources().getDisplayMetrics().density;
        final int widthSpec = View.MeasureSpec.makeMeasureSpec((int)(Constants.VIEW_SPAN_WIDTH * density), View.MeasureSpec.EXACTLY);
        final int heightSpec = View.MeasureSpec.makeMeasureSpec((int)(Constants.VIEW_SPAN_HEIGHT * density), View.MeasureSpec.EXACTLY);
        view.measure(widthSpec, heightSpec);
    }
}
