package com.tcl.shenwk.aNote.task;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.tcl.shenwk.aNote.util.DateUtil;
import com.tcl.shenwk.aNote.util.FileUtil;
import com.tcl.shenwk.aNote.util.MediaPlayAndRecordUtil;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by shenwk on 2018/4/1.
 */

public class AudioRecorder {
    private static final String TAG = "AudioRecorder";
    private static final int UPDATE_RECORD_TIME_TEXT = 0;
    private static final String INIT_TIME = "00:00";
    private static final int INIT_VIEW = 1;
    private MediaRecorder mediaRecorder;
    private long startTime;
    private Context context;
    private TextView recordTimeText;
    private Timer timer;
    private String fileName;

    public AudioRecorder(Context context, TextView recordTimeText) {
        mediaRecorder = new MediaRecorder();
        this.context = context;
        this.recordTimeText = recordTimeText;
    }

    public void prepare(String fileName){
        this.fileName = fileName;
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_WB);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
        mediaRecorder.setOutputFile(fileName);
        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG, "prepare: error");
        }
    }

    public void start(){
        if(timer != null){
            timer.cancel();
            timer = null;
        }
        timer = new Timer();
        mediaRecorder.start();
        startTime = DateUtil.getInstance().getTime();
        handler.sendMessage(handler.obtainMessage(INIT_VIEW));
        timer.schedule(new UpdateRecordTimeTask(), 0, 1000);
    }

    public void stop(){
        if(timer != null){
            timer.cancel();
            timer = null;
        }
        mediaRecorder.stop();
        mediaRecorder.reset();
    }

    public String getFileName() {
        return fileName;
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case UPDATE_RECORD_TIME_TEXT:{
                    Log.i(TAG, "handleMessage: update record time");
                    recordTimeText.setText(DateUtil.getInstance().getMMSSTime(DateUtil.getInstance().getTime() - startTime));
                    break;
                }
                case INIT_VIEW:{
                    recordTimeText.setText(INIT_TIME);
                    break;
                }
            }
        }
    };

    class UpdateRecordTimeTask extends TimerTask{
        @Override
        public void run() {
            handler.sendMessage(handler.obtainMessage(UPDATE_RECORD_TIME_TEXT));
        }
    };
}
