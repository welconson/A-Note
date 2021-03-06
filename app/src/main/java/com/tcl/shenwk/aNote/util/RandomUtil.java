package com.tcl.shenwk.aNote.util;

import android.util.Log;

import java.util.Date;

/**
 * RandomUtil for generate random values.
 * Created by shenwk on 2018/2/7.
 */

public class RandomUtil {
    private static String TAG = "RandomUtil";
    public static String randomString(int length){
        long millisecond =  DateUtil.getInstance().getTime();
        String randomString = String.valueOf(millisecond);
        randomString.substring(randomString.length() - length);
        Log.i(TAG, "randomString: " + randomString);
        return randomString;
    }
}
