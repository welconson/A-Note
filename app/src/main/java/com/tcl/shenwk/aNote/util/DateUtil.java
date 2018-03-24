package com.tcl.shenwk.aNote.util;


import java.util.Date;

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
}
