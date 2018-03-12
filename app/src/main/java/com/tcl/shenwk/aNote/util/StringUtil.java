package com.tcl.shenwk.aNote.util;

import android.annotation.SuppressLint;

/**
 * String utilities organized here.
 * Created by shenwk on 2018/2/2.
 */

public class StringUtil {
    public static boolean equal(String s1, String s2){
        if(s1 == null || s2 == null)
            return false;
        else return s1.equals(s2);
    }

    @SuppressLint("DefaultLocale")
    public static String DurationFormat(int duration){
        int durationSecond = duration / 1000;
        return String.format("%2d:%02d", durationSecond / 60, durationSecond % 60);
    }
}
