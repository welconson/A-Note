package com.tcl.shenwk.aNote.util;


import android.content.Context;
import android.text.format.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Date utility class.
 * Created by shenwk on 2018/3/23.
 */

public class DateUtil {
    private Date date;
    private static DateUtil mInstance = null;

    private DateUtil() {
        date = new Date();
    }

    public static DateUtil getInstance(){
        if(mInstance == null)
            return new DateUtil();
        else return mInstance;
    }

    public long getTime(){
        return date.getTime();
    }

    public String getHumanReadableTimeString(Context context, long time){
        return DateUtils.getRelativeTimeSpanString(context, time, false).toString();
    }

    public String generateFileNameFromNowTime(){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        return formatter.format(date);
    }

    public String getMMSSTime(long time){
        SimpleDateFormat format = new SimpleDateFormat("mm:ss", Locale.getDefault());
        return format.format(time);
    }
}
