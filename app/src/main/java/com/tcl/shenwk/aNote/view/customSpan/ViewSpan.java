package com.tcl.shenwk.aNote.view.customSpan;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ReplacementSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

/**
 * Replace default implementation Drawable with View, so we can custom
 * all interactions we want of any subclass span of this.
 * Created by shenwk on 2018/2/11.
 */

public class ViewSpan extends DynamicDrawableSpan {
    private static final String TAG = "ViewSpan";
    private View mView;

    public ViewSpan(View mView) {
        super(DynamicDrawableSpan.ALIGN_BASELINE);
        this.mView = mView;
        final int widthSpec = View.MeasureSpec.makeMeasureSpec(500, View.MeasureSpec.EXACTLY);
        final int heightSpec = View.MeasureSpec.makeMeasureSpec(500, View.MeasureSpec.EXACTLY);
        mView.measure(widthSpec, heightSpec);
//        Log.i(TAG, "getSize: after height = " + mView.getMeasuredHeight() +
//                " , width = " + mView.getMeasuredWidth());
    }

    @Override
    public Drawable getDrawable() {
        return null;
    }

    @Override
    public int getSize(@NonNull Paint paint, CharSequence text, int start, int end, @Nullable Paint.FontMetricsInt fm) {
        if (fm != null) {
            fm.ascent = -mView.getMeasuredHeight();
            fm.descent = 0;

            fm.top = fm.ascent;
            fm.bottom = 0;
        }
        return mView.getMeasuredWidth();
    }

    @Override
    public void draw(Canvas canvas, CharSequence text,
                     int start, int end, float x,
                     int top, int y, int bottom, Paint paint) {
        canvas.save();
        int transY = bottom - mView.getMeasuredHeight();
        if (mVerticalAlignment == ALIGN_BASELINE) {
            transY -= paint.getFontMetricsInt().descent;
        }

        canvas.translate(x, transY);
        mView.layout(0,0, mView.getMeasuredWidth(), mView.getMeasuredHeight());
        mView.draw(canvas);
        canvas.restore();
    }
}
