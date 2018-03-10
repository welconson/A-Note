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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tcl.shenwk.aNote.R;

/**
 * Replace default implementation Drawable with View, so we can custom
 * all interactions we want of any subclass span of this.
 * Created by shenwk on 2018/2/11.
 */

public abstract class ViewSpan extends DynamicDrawableSpan implements View.OnTouchListener{
    private static final String TAG = "ViewSpan";
    private View mView;
    private float mCurrentX;
    private float mCurrentY;
    private int xOffset;
    private int yOffset;

    public ViewSpan(View view, int x, int y) {
        super(DynamicDrawableSpan.ALIGN_BOTTOM);
        this.mView = view;
        xOffset = x;
        yOffset = y;
        mView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.i(TAG, "ViewSpan onTouch: x = " + event.getX() + " , y = " + event.getY());
                return false;
            }
        });
//        mView.findViewById(R.id.setting).setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                Log.i(TAG, "onTouch: setting");
//                return true;
//            }
//        });
//        mView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.i(TAG, "onClick: ");
//            }
//        });
        measure();
//        final int widthSpec = View.MeasureSpec.makeMeasureSpec(500, View.MeasureSpec.EXACTLY);
//        final int heightSpec = View.MeasureSpec.makeMeasureSpec(500, View.MeasureSpec.EXACTLY);
//        mView.measure(widthSpec, heightSpec);
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
        Log.i(TAG, "draw: x = " + (int)x + " , y = " + transY);
        mCurrentX = x;
        mCurrentY = transY;
        canvas.translate(x, transY);
        mView.layout(0,0, mView.getMeasuredWidth(), mView.getMeasuredHeight());
        mView.draw(canvas);
        canvas.restore();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int []location = new int[2];
        mView.getLocationOnScreen(location);
        event.offsetLocation(- v.getPaddingLeft() - mCurrentX, - v.getPaddingTop() - mCurrentY);
        Log.i(TAG, "onTouch: x = " + event.getX() + " , y = " + event.getY());
        mView.dispatchTouchEvent(event);
        return true;
    }

    public abstract void measure();

    public View getView(){
        return mView;
    }
}
