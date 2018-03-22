package com.tcl.shenwk.aNote.view.customSpan;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.tcl.shenwk.aNote.R;
import com.tcl.shenwk.aNote.entry.ResourceDataEntry;
import com.tcl.shenwk.aNote.util.Constants;
import com.tcl.shenwk.aNote.util.FileUtil;

import java.io.FileNotFoundException;

/**
 * Created by shenwk on 2018/3/15.
 */

public class ImageViewSpan extends ViewSpan{
    private static final String TAG = "ImageViewSpan";
    private Bitmap bitmap;

    public ImageViewSpan(View view, ResourceDataEntry resourceDataEntry) {
        super(view, resourceDataEntry);
        setViewReaction();
    }

    public ImageViewSpan(View view, Uri uri, ResourceDataEntry resourceDataEntry) {
        super(view, uri, resourceDataEntry);
        resourceDataEntry.setFileName(FileUtil.getFileNameFromURI(view.getContext(),
                uri, resourceDataEntry.getDataType()));
        setViewReaction();
    }

    @Override
    public void measure() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        //options.inJustDecodeBounds = true;
        if(getResourceDataUri() == null) {
            bitmap = BitmapFactory.decodeFile(getFilePath(), options);
        }
        else bitmap = getOptionsByUri(getView().getContext(), getResourceDataUri(), options);
        final int widthSpec = View.MeasureSpec.makeMeasureSpec(options.outWidth, View.MeasureSpec.EXACTLY);
        final int heightSpec = View.MeasureSpec.makeMeasureSpec(options.outHeight, View.MeasureSpec.EXACTLY);
        BitmapDrawable bitmapDrawable = new BitmapDrawable(getView().getContext().getResources(), bitmap);
        bitmapDrawable.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
        ImageView imageView = getView().findViewById(R.id.image_span_image);
        imageView.setImageBitmap(bitmap);
        imageView.setBackground(bitmapDrawable);
        getView().measure(widthSpec, heightSpec);
    }

    @Override
    public int getResourceDataType() {
        return Constants.RESOURCE_TYPE_IMAGE;
    }

    private void setViewReaction(){
        View view = getView();
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_UP:
                        v.performClick();
                        Log.i(TAG, "onTouch: UP");
                        break;
                    case MotionEvent.ACTION_DOWN:
                        Log.i(TAG, "onTouch: DOWN");
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        Log.i(TAG, "onTouch: CANCEL");
                        break;
                        default:
                            Log.i(TAG, "onTouch: unknowns action + " + event.getAction());
                }
                return true;
            }
        });
    }

    private Bitmap getOptionsByUri(Context context, Uri uri, BitmapFactory.Options options) {
        Bitmap bitmap = null;
        ContentResolver contentResolver = context.getContentResolver();
        try {
            AssetFileDescriptor assetFileDescriptor = contentResolver.openAssetFileDescriptor(uri, "r");
            if (assetFileDescriptor != null)
                bitmap = BitmapFactory.decodeFileDescriptor(assetFileDescriptor.getFileDescriptor(), null, options);
            else Log.i(TAG, "getOptionsByUri: decode bitmap from uri error");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bitmap;
    }
}
