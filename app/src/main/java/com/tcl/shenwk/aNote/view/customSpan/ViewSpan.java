package com.tcl.shenwk.aNote.view.customSpan;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.style.DynamicDrawableSpan;
import android.view.MotionEvent;
import android.view.View;

import com.tcl.shenwk.aNote.util.FileUtil;

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
    private Uri mResourceDataUri;
    private String mFileName;
    private String mFilePath;
    private int mResourceDataType;

     private ViewSpan(View view){
        super(DynamicDrawableSpan.ALIGN_BOTTOM);
        this.mView = view;
        measure();
         //        mView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                Log.i(TAG, "ViewSpan onTouch: x = " + event.getX() + " , y = " + event.getY());
//                return false;
//            }
//        });
    }

    ViewSpan(View view, Uri uri) {
        this(view);
        this.mResourceDataUri = uri;
        this.mFileName = FileUtil.getFileNameFromURI(view.getContext(), uri, getResourceDataType());
    }

    ViewSpan(View view, String fileName, String filePath){
        this(view);
        this.mFileName = fileName;
        this.mFilePath = filePath;
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
//        Log.i(TAG, "draw: x = " + (int)x + " , y = " + transY);
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
        // We need to adjust the event relative location of the View.
        // Calculate offset of the View inside the TextView.
        event.offsetLocation(- v.getPaddingLeft() - mCurrentX, - v.getPaddingTop() - mCurrentY);
        mView.dispatchTouchEvent(event);
        return true;
    }

    /**
     * Implement the method in subclass to set right size of view measuring,
     * cause the view will never be added to View Tree.
     */
    public abstract void measure();

    public abstract int getResourceDataType();

    public View getView(){
        return mView;
    }

    public Uri getResourceDataUri() {
        return mResourceDataUri;
    }

    public String getFileName() {
        return mFileName;
    }

    public String getFilePath() {
        return mFilePath;
    }
}
