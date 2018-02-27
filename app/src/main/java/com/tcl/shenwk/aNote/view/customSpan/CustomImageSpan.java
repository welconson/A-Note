package com.tcl.shenwk.aNote.view.customSpan;

import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.style.DynamicDrawableSpan;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import com.tcl.shenwk.aNote.CustomSpanSharedUtility;

/**
 * Span class for show image in edittext, and custom some events for it.
 * Created by shenwk on 2018/1/26.
 */

public class CustomImageSpan extends DynamicDrawableSpan implements OnClickListener, View.OnLongClickListener, CustomSpanSharedUtility {
    private static String TAG = "CustomImageSpan";
    private Drawable mDrawable = null;
    private boolean isShow = false;
    private View mView = null;
    public CustomImageSpan(Drawable drawable) {
        mDrawable = drawable;
    }

    @Override
    @Deprecated
    public Drawable getDrawable() {
        return mDrawable;
    }

    @Override
    public void onClick(View v) {
        Log.i(TAG, "onClick: ");
        // TODO: 2018/2/1 span 点击事件后期完善
//        Context context = v.getContext();
//        if(mView == null) {
//            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//            if (layoutInflater != null) {
//                mView = layoutInflater.inflate(R.layout.show_image_layout, (ViewGroup) v.getParent());
//                Log.i(TAG, "onClick: ");
//                mView.setBackground(mDrawable);
//            }
//        }
//        if(isShow){
//            mView.setVisibility(View.INVISIBLE);
//        }
//        else mView.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }

    @Override
    public void actionOnclick(View v) {
        onClick(v);
    }

    @Override
    public void actionOnLongClick(View v) {
        onLongClick(v);
    }
}
