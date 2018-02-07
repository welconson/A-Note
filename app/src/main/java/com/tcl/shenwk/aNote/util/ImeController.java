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
    private InputMethodManager imm;

    public ImeController(Context context) {
        this.imm = (InputMethodManager)context.getSystemService(INPUT_METHOD_SERVICE);
    }

    public boolean toggleSoftInput(){
        boolean ret = false;
        if(imm != null){
            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS);
            ret = true;
        }
        return ret;
    }

    public boolean showSoftInput(View v){
        boolean ret = false;
        if(imm != null) {
            imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
            ret = true;
        }
        return ret;
    }

    public boolean hideSoftInput(View v){
        boolean ret = false;
        if(imm != null) {
            imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            ret = true;
        }
        return ret;
    }
}
