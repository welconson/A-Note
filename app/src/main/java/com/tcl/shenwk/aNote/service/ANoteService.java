package com.tcl.shenwk.aNote.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.tcl.shenwk.aNote.manager.SyncManager;

/**
 * Created by shenwk on 2018/3/30.
 */

public class ANoteService extends Service {
    private static final String TAG = "ANoteService";
    private SyncManager syncManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind: ");
        return new ANoteBinder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: ");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate: ");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand: ");
        if(syncManager == null)
            syncManager = SyncManager.getInstance(getApplicationContext());
        return super.onStartCommand(intent, flags, startId);
    }

    public class ANoteBinder extends Binder{
        public void sync(){
            syncManager.startManualSync();
        }
    }
}
