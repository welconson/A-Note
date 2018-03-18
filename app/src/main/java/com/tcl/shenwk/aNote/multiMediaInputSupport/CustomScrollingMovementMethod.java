package com.tcl.shenwk.aNote.multiMediaInputSupport;

import android.text.Layout;
import android.text.Spannable;
import android.text.method.MovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.text.method.Touch;
import android.view.MotionEvent;
import android.widget.TextView;

import com.tcl.shenwk.aNote.view.customSpan.ViewSpan;

/**
 * Inside TextView, there is a MovementMethod, provided cursor positioning,
 * scrolling and text selection functionality.Here we add event reaction to
 * make our custom spans to receive the motion event.
 * Created by shenwk on 2018/3/16.
 */

public class CustomScrollingMovementMethod extends ScrollingMovementMethod {
    private static MovementMethod sInstance;
    @Override
    public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
        int action = event.getAction();
        int initialScrollX = Touch.getInitialScrollX(widget, buffer);
        int initialScrollY = Touch.getInitialScrollY(widget, buffer);
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
            int x = (int) event.getX();
            int y = (int) event.getY();

            x -= widget.getTotalPaddingLeft();
            y -= widget.getTotalPaddingTop();

            x += widget.getScrollX();
            y += widget.getScrollY();

            Layout layout = widget.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            ViewSpan[] links = buffer.getSpans(off, off, ViewSpan.class);
            if(links.length != 0){
                links[0].onTouch(widget, event);
//                Selection.setSelection(buffer, off);
            }
        }
        Touch.onTouchEvent(widget, buffer, event);
        return false;
    }

    public static MovementMethod getInstance() {
        if (sInstance == null)
            sInstance = new CustomScrollingMovementMethod();

        return sInstance;
    }
}
