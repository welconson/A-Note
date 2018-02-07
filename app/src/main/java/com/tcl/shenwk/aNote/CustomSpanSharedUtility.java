package com.tcl.shenwk.aNote;

import android.view.View;

/**
 * Interface to implement multiple media span share the same click events calling.
 * Created by shenwk on 2018/2/1.
 */

public interface CustomSpanSharedUtility {
    public void actionOnclick(View v);
    public void actionOnLongClick(View v);
}
