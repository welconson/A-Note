package com.tcl.shenwk.aNote.manager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.tcl.shenwk.aNote.task.DownloadTask;

import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.concurrent.ThreadPoolExecutor;

public class RealTimeDownloadManager {
    private static final String TAG = "RealTimeDownloadManager";

    private String cookie;
    private Context context;
    private final Object lock = new Object();
    private boolean isDownloading;
    private LooperThread looperThread;

    public RealTimeDownloadManager(Context context) {
        this.context = context;
        this.isDownloading = false;
        this.looperThread = new LooperThread();
    }

    // add download task to worker thread message queue
    public void addTask(DownloadTask downloadTask){
        Log.i(TAG, "addTask: " + Thread.currentThread());
        looperThread.handler.post(new DownloadRunnable(downloadTask));
    }

    private class DownloadRunnable implements Runnable{
        private DownloadTask downloadTask;

        DownloadRunnable(DownloadTask downloadTask) {
            this.downloadTask = downloadTask;
        }

        @Override
        public void run() {
            downloadTask.start();
        }
    }

    private class LooperThread extends Thread{
        private Handler handler;
        @SuppressLint("HandlerLeak")
        @Override
        public void run() {
            Looper.prepare();
            handler = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    Log.i(TAG, "handleMessage: " + Thread.currentThread());
                }
            };
            Log.i(TAG, "run: create worker thread looper, " + Thread.currentThread());
            Looper.loop();
        }
    }
}
