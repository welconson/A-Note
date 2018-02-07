package com.tcl.shenwk.aNote.util;

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
}
