package com.tcl.shenwk.aNote.multiMediaInputSupport;

import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.ArrowKeyMovementMethod;
import android.text.method.MetaKeyKeyListener;
import android.text.method.Touch;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.view.MotionEvent;
import android.widget.TextView;

import com.tcl.shenwk.aNote.SpanListener;
import com.tcl.shenwk.aNote.view.customSpan.CustomImageSpan;

/**
 * Created by shenwk on 2018/1/29.
 */

public class CustomMovementMethod extends ArrowKeyMovementMethod {
    private static CustomMovementMethod mInstance = null;
    @Override
    public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
        int action = event.getAction();
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

            SpanListener[] links = buffer.getSpans(off, off, SpanListener.class);

            if (links.length != 0) {
                if (action == MotionEvent.ACTION_UP) {
                    links[0].actionOnclick(widget);
                }
//                else if (action == MotionEvent.ACTION_DOWN) {
//                    Selection.setSelection(buffer,
//                            buffer.getSpanStart(links[0]),
//                            buffer.getSpanEnd(links[0]));
//                }
//                return true;
//            } else {
//                Selection.removeSelection(buffer);
            }
        }
        return super.onTouchEvent(widget, buffer, event);
    }

    public static CustomMovementMethod getInstance(){
        if(mInstance == null)
            mInstance = new CustomMovementMethod();
        return mInstance;
    }
}
