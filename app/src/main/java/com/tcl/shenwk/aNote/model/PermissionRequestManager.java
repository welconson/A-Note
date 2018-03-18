package com.tcl.shenwk.aNote.model;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.tcl.shenwk.aNote.util.Constants;
import com.tcl.shenwk.aNote.view.activity.EditNoteActivity;

import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by shenwk on 2018/3/14.
 */

public class PermissionRequestManager {
    private static final String TAG = "PermissionReqManager";
    private static PermissionRequestManager mInstance;
    private static int NUM_OF_CORES = Runtime.getRuntime().availableProcessors();
    private static final int KEEP_ALIVE_TIME = 1;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;

    private final BlockingDeque<Runnable> mWorkQueue = new LinkedBlockingDeque<>();

    private final ThreadPoolExecutor mThreadPoolExecutor = new ThreadPoolExecutor(
            NUM_OF_CORES,
            NUM_OF_CORES,
            KEEP_ALIVE_TIME,
            KEEP_ALIVE_TIME_UNIT,
            mWorkQueue
    );
    static{
        mInstance = new PermissionRequestManager();
    }

    private Handler mHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {

            }
    };


    class PermissionRequestTask {
        PermissionRequestTask mPermissionRequestTask;
        class PermissionRunnable implements Runnable {
            @Override
            public void run() {

            }
        }
    }


    public static PermissionRequestManager getInstance(){
        return mInstance;
    }
}
