package com.tcl.shenwk.aNote.util;

import android.text.TextUtils;
import android.util.Patterns;

public class TextUtil {
    public static boolean isValidEmail(CharSequence email){
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isPasswordMatched(CharSequence password, CharSequence verifiedPassword){
        return StringUtil.equal(password.toString(), verifiedPassword.toString());
    }
}
