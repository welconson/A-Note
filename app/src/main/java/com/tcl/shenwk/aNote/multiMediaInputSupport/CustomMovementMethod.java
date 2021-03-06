package com.tcl.shenwk.aNote.multiMediaInputSupport;

import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.ArrowKeyMovementMethod;
import android.text.method.MovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.text.method.Touch;
import android.view.MotionEvent;
import android.widget.Scroller;
import android.widget.TextView;

import com.tcl.shenwk.aNote.CustomSpanSharedUtility;
import com.tcl.shenwk.aNote.view.customSpan.ViewSpan;

/**
 * Created by shenwk on 2018/1/29.
 */

public class CustomMovementMethod extends ArrowKeyMovementMethod {
    private static CustomMovementMethod mInstance = null;
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
        super.onTouchEvent(widget, buffer, event);
        return true;
    }

    public static CustomMovementMethod getInstance(){
        if(mInstance == null)
            mInstance = new CustomMovementMethod();
        return mInstance;
    }
}
