package com.tcl.shenwk.aNote.util;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import static android.content.Context.INPUT_METHOD_SERVICE;

/**
 * Used to control input method soft input keyboard.
 * Created by shenwk on 2018/2/1.
 */

public class ImeController {
    private static ImeController mInstance = null;
    private InputMethodManager inputMethodManager;

    private ImeController(Context context) {
        this.inputMethodManager = (InputMethodManager) context.getSystemService(INPUT_METHOD_SERVICE);
    }

    public static ImeController getInstance(Context context){
        if(mInstance == null)
            mInstance = new ImeController(context.getApplicationContext());
        return mInstance;
    }

    public boolean toggleSoftInput(){
        boolean ret = false;
        if(inputMethodManager != null){
            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS);
            ret = true;
        }
        return ret;
    }

    public boolean showSoftInput(View v){
        boolean ret = false;
        if(inputMethodManager != null) {
            inputMethodManager.showSoftInput(v, InputMethodManager.HIDE_NOT_ALWAYS);
            ret = true;
        }
        return ret;
    }

    public boolean hideSoftInput(View v){
        boolean ret = false;
        if(inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            ret = true;
        }
        return ret;
    }
}
