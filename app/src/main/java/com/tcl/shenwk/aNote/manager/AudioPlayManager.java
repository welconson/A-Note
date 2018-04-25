package com.tcl.shenwk.aNote.manager;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.tcl.shenwk.aNote.task.DownloadTask;
import com.tcl.shenwk.aNote.util.DateUtil;
import com.tcl.shenwk.aNote.util.FileUtil;
import com.tcl.shenwk.aNote.util.MediaPlayAndRecordUtil;
import com.tcl.shenwk.aNote.util.UrlSource;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by shenwk on 2018/4/1.
 */

public class AudioPlayManager implements MediaPlayer.OnPreparedListener{
    private static final String TAG = "AudioPlayManager";
    private static final String INIT_TIME = "00:00";
    private static final int UPDATE_PLAYED_TIME_TEXT = 0;
    private static final int INIT_VIEW = 1;
    private static final int CLEAN = 2;
    private static final int DOWNLOAD_SUCCESS = 3;
    private MediaPlayer mediaPlayer;
    private Uri uri;
    private TextView playedTimeText;
    private TextView totalTimeText;
    private SeekBar seekBar;
    private Context context;
    private Timer timer;
    private View playingView;
    private View pendingView;

    public AudioPlayManager(TextView playedTimeText, TextView totalTimeText, SeekBar seekBar, Context context, MediaPlayer.OnCompletionListener onCompletionListener) {
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
                case DOWNLOAD_SUCCESS:{
                    ((View) msg.obj).performClick();
                }
            }
        }
    };

    public void playNewUriAudio(Uri uri, View view){
        this.uri = uri;
        this.playingView = view;
        this.pendingView = null;
        if(uri != null){
            try {
                mediaPlayer.setDataSource(FileUtil.getAssetFileDescriptorFromUri(context, uri).getFileDescriptor());
                mediaPlayer.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
                Log.i(TAG, "AudioPlayManager: read file content from uri error");
            }
        }
    }

    public void start(){
        cleanTimer();
        timer = new Timer();
        mediaPlayer.start();
        timer.schedule(new UpdateViewTask(), MediaPlayAndRecordUtil.getResumeDelay(mediaPlayer.getCurrentPosition()), 1000);
    }

    public void reset(){
        cleanTimer();
        mediaPlayer.reset();
        uri = null;
        new CleanTask().run();
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

    public View getPlayingView() {
        return playingView;
    }

    public void downloadAudio(String path, final View view) {
        pendingView = view;
        try {
            SyncManager.getInstance(context).realTimeDownload(
                    new URL(UrlSource.URL_SYNC_DOWNLOAD),
                    path.substring(path.lastIndexOf(LoginManager.userFolder) + LoginManager.userFolder.length() + 1),
                    path,
                    new DownloadTask.OnFinishListener() {
                        @Override
                        public void onSuccess() {
                            if(view == pendingView){
                                Message message = handler.obtainMessage();
                                message.obj = view;
                                message.what = DOWNLOAD_SUCCESS;
                                handler.sendMessage(message);
                            }
                        }

                        @Override
                        public void onError(String err) {
                            Log.i(TAG, "AudioPlayManager download onError: " + err);
                        }
                    }
            );
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
