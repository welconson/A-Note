package com.tcl.shenwk.aNote.view.customSpan;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.tcl.shenwk.aNote.R;
import com.tcl.shenwk.aNote.entity.ResourceDataEntity;
import com.tcl.shenwk.aNote.util.Constants;
import com.tcl.shenwk.aNote.util.FileUtil;

import java.io.FileNotFoundException;

/**
 * Created by shenwk on 2018/3/15.
 */

public class ImageViewSpan extends ViewSpan{
    private static final String TAG = "ImageViewSpan";
    private static float TEXT_VIEW_HEIGHT = -1;
    private static float TEXT_VIEW_WIDTH = -1;
    private static int MAX_IMAGE_LENGTH = 8192;
    private Bitmap bitmap;

    public ImageViewSpan(View view, ResourceDataEntity resourceDataEntity) {
        super(view, resourceDataEntity);
        setViewReaction();
    }

    public ImageViewSpan(View view, Uri uri, ResourceDataEntity resourceDataEntity) {
        super(view, uri, resourceDataEntity);
        resourceDataEntity.setFileName(FileUtil.getFileNameFromURI(view.getContext(),
                uri, resourceDataEntity.getDataType()));
        setViewReaction();
    }

    @Override
    public void measure() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        //options.inJustDecodeBounds = true;
        if(getResourceDataUri() == null) {
            bitmap = BitmapFactory.decodeFile(getFilePath(), options);
        }
        else bitmap = getOptionsByUri(getView().getContext(), getResourceDataUri(), options);
        if(TEXT_VIEW_HEIGHT == -1){
            View view = ((Activity) getView().getContext()).getWindow().getDecorView().findViewById(R.id.linearLayout);
            TEXT_VIEW_HEIGHT = view.getHeight();
            TEXT_VIEW_WIDTH = view.getWidth();
            Log.i(TAG, "measure: text view height: " + TEXT_VIEW_HEIGHT + " , " + TEXT_VIEW_WIDTH);
        }
        bitmap = scaleToProperImage(options);
        final int widthSpec = View.MeasureSpec.makeMeasureSpec(options.outWidth, View.MeasureSpec.EXACTLY);
        final int heightSpec = View.MeasureSpec.makeMeasureSpec(options.outHeight, View.MeasureSpec.EXACTLY);
        ImageView imageView = getView().findViewById(R.id.image_span_image);
        imageView.setImageBitmap(bitmap);
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

    private Bitmap scaleToProperImage(BitmapFactory.Options options){
        float h = options.outHeight;
        float w = options.outWidth;
        float scale = 1;
        if(((int) w) > TEXT_VIEW_WIDTH){
            scale = TEXT_VIEW_WIDTH / w;
            options.outWidth = (int) (w * scale);
            options.outHeight = (int) (h * scale);
        } else if(((int) h) > MAX_IMAGE_LENGTH){
            scale = MAX_IMAGE_LENGTH / h;
            options.outWidth = (int) (w * scale);
            options.outHeight = (int) (h * scale);
        }
        if(scale == 1){
            return bitmap;
        }
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap,0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
        bitmap.recycle();
        return resizedBitmap;
    }
}
