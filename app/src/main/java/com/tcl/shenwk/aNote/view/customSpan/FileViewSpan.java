package com.tcl.shenwk.aNote.view.customSpan;

import android.net.Uri;
import android.view.View;

import com.tcl.shenwk.aNote.entity.ResourceDataEntity;
import com.tcl.shenwk.aNote.util.Constants;
import com.tcl.shenwk.aNote.util.FileUtil;

/**
 * Created by shenwk on 2018/3/19.
 */

public class FileViewSpan extends ViewSpan {
    public FileViewSpan(View view, ResourceDataEntity resourceDataEntity) {
        super(view, resourceDataEntity);
    }

    public FileViewSpan(View view, Uri uri, ResourceDataEntity resourceDataEntity) {
        super(view, uri, resourceDataEntity);
        resourceDataEntity.setFileName(FileUtil.getFileNameFromURI(view.getContext(), uri, resourceDataEntity.getDataType()));
    }

    @Override
    public void measure() {
        View view = getView();
        float density = view.getResources().getDisplayMetrics().density;
        final int widthSpec = View.MeasureSpec.makeMeasureSpec((int)(Constants.VIEW_SPAN_WIDTH * density), View.MeasureSpec.EXACTLY);
        final int heightSpec = View.MeasureSpec.makeMeasureSpec((int)(Constants.VIEW_SPAN_HEIGHT * density), View.MeasureSpec.EXACTLY);
        view.measure(widthSpec, heightSpec);
    }

    @Override
    public int getResourceDataType() {
        return Constants.RESOURCE_TYPE_FILE;
    }
}
