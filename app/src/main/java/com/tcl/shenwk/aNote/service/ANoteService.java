package com.tcl.shenwk.aNote.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SeekBar;

import com.tcl.shenwk.aNote.R;
import com.tcl.shenwk.aNote.view.activity.EditNoteActivity;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by shenwk on 2018/3/30.
 */

public class ANoteService extends Service {
    private static final String TAG = "ANoteService";
    private ANoteBinder aNoteBinder = new ANoteBinder();
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private Uri playingUri = null;
    private Timer timer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind: ");
        return aNoteBinder;
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
        return super.onStartCommand(intent, flags, startId);
    }

    public class ANoteBinder extends Binder{
        public void playAudio(){
            NotificationCompat.Builder b = new NotificationCompat.Builder(getApplicationContext(), "A-Note");

            b.setAutoCancel(true)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.mipmap.icon)
                    .setTicker("Hearty365")
                    .setOngoing(true)
                    .setContentTitle("Default notification")
                    .setContentText("Lorem ipsum dolor sit amet, consectetur adipiscing elit.")
                    .setDefaults(Notification.DEFAULT_LIGHTS| Notification.DEFAULT_SOUND)
                    .setContentInfo("Info");


            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            if(notificationManager != null)
                notificationManager.notify(1, b.build());
        }
    }
}
