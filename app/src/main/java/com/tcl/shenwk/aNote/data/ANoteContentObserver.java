package com.tcl.shenwk.aNote.data;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.tcl.shenwk.aNote.manager.SyncManager;

public class ANoteContentObserver extends ContentObserver {
    private final String TAG;
    private Context context;

    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     */
    public ANoteContentObserver(Handler handler, String TAG, Context context) {
        super(handler);
        this.TAG = TAG;
        this.context = context.getApplicationContext();
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        Log.i(TAG, "onChange with uri:" + uri);
        SyncManager.getInstance(context).increaseUpdateCode();
    }

    @Override
    public void onChange(boolean selfChange) {
        this.onChange(selfChange, null);
        Log.i(TAG, "onChange: ");
    }

    @Override
    public boolean deliverSelfNotifications() {
        return true;
    }
}
