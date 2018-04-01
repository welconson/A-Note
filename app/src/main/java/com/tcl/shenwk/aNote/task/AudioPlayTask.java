package com.tcl.shenwk.aNote.task;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.tcl.shenwk.aNote.util.DateUtil;
import com.tcl.shenwk.aNote.util.FileUtil;
import com.tcl.shenwk.aNote.util.MediaPlayAndRecordUtil;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by shenwk on 2018/4/1.
 */

public class AudioPlayTask implements MediaPlayer.OnPreparedListener{
    private static final String TAG = "AudioPlayTask";
    private static final String INIT_TIME = "00:00";
    private static final int UPDATE_PLAYED_TIME_TEXT = 0;
    private static final int INIT_VIEW = 1;
    private static final int CLEAN = 2;
    private MediaPlayer mediaPlayer;
    private Uri uri;
    private TextView playedTimeText;
    private TextView totalTimeText;
    private SeekBar seekBar;
    private Context context;
    private Timer timer;

    public AudioPlayTask(TextView playedTimeText, TextView totalTimeText, SeekBar seekBar, Context context, MediaPlayer.OnCompletionListener onCompletionListener) {
        this.playedTimeText = playedTimeText;
        this.totalTimeText = totalTimeText;
        this.seekBar = seekBar;
        this.context = context;
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(onCompletionListener);
        mediaPlayer.setOnPreparedListener(this);
    }

    private Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case UPDATE_PLAYED_TIME_TEXT:{
                    Log.i(TAG, "handleMessage: update play time");
                    playedTimeText.setText(DateUtil.getInstance().getMMSSTime(
                            MediaPlayAndRecordUtil.getStableTimeOffset(mediaPlayer.getCurrentPosition())));
                    seekBar.setProgress(MediaPlayAndRecordUtil.getStableProgress(mediaPlayer.getCurrentPosition()));
                    break;
                }
                case INIT_VIEW:{
                    seekBar.setProgress(0);
                    totalTimeText.setText(DateUtil.getInstance().getMMSSTime(mediaPlayer.getDuration()));
                    playedTimeText.setText(INIT_TIME);
                    break;
                }
                case CLEAN:{
                    seekBar.setProgress(0);
                    playedTimeText.setText(INIT_TIME);
                    break;
                }
            }
        }
    };

    public void playNewUriAudio(Uri uri){
        this.uri = uri;
        if(uri != null){
            try {
                mediaPlayer.setDataSource(FileUtil.getAssetFileDescriptorFromUri(context, uri).getFileDescriptor());
                mediaPlayer.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
                Log.i(TAG, "AudioPlayTask: read file content from uri error");
            }
        }
    }

    public void start(){
        cleanTimer();
        timer = new Timer();
        mediaPlayer.start();
        timer.schedule(new UpdateViewTask(), MediaPlayAndRecordUtil.getResumeDelay(mediaPlayer.getCurrentPosition()), 1000);
    }

    public void stop(){
        cleanTimer();
        mediaPlayer.reset();
        uri = null;
        new Timer().schedule(new CleanTask(), 0);
    }

    public void pause(){
        cleanTimer();
        mediaPlayer.pause();
    }

    public void release(){
        cleanTimer();
        mediaPlayer.release();
    }

    public boolean isPlaying(){
        return mediaPlayer.isPlaying();
    }

    private void cleanTimer(){
        if(timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        seekBar.setMax(mediaPlayer.getDuration() / 1000);
        handler.sendMessage(handler.obtainMessage(INIT_VIEW));
        start();
    }

    private class UpdateViewTask extends TimerTask{
        @Override
        public void run() {
            handler.sendMessage(handler.obtainMessage(UPDATE_PLAYED_TIME_TEXT));
        }
    }

    private class CleanTask extends TimerTask{

        @Override
        public void run() {
            handler.sendMessage(handler.obtainMessage(CLEAN));
        }
    }

    public Uri getUri() {
        return uri;
    }
}
