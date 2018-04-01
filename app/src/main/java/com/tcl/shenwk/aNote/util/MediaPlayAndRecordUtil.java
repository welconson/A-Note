package com.tcl.shenwk.aNote.util;

/**
 * Created by shenwk on 2018/3/29.
 */

public class MediaPlayAndRecordUtil {
    public static int getStableProgress(long position){
        return (int) ((position % 1000) > 500 ? position / 1000 + 1 : position / 1000);
    }

    public static long getStableTimeOffset(long time){
        return time % 1000 > 500 ? time + 500 : time;
    }

    public static int getResumeDelay(long position){
        return (int) (1000 - (position % 1000));
    }
}
