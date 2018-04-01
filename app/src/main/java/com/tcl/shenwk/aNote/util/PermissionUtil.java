package com.tcl.shenwk.aNote.util;

import android.Manifest;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.webkit.PermissionRequest;

import static android.content.Context.MODE_PRIVATE;

/**
 *
 * Created by shenwk on 2018/3/13.
 */

public class PermissionUtil {private static final String TAG = "PermissionUtils";
    private static boolean isRuntimePermissionRequired() {
        return (Build.VERSION.SDK_INT >= 23);
    }

    public static boolean checkPermission(Activity activity, String permission) {
        boolean isGranted = true;
        Log.d(TAG, "checkPermission");
        if (!isRuntimePermissionRequired()) {
            /*
                Runtime permission not required,
                THE DEVICE IS RUNNING ON < 23, So, no runtime permission required..
                Simply call **** permissionAskListener.onPermissionGranted() ****
            */
            Log.i(TAG, "checkPermission: permission already granted");
        } else {
            //runtime permission required here...
            //check if the permission is already granted, i.e the application was launched earlier too, and the user had "allowed" the permission then.
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                isGranted = false;
            } else {
                Log.d(TAG, "checkPermission: Permission already granted");
            }
        }
        return isGranted;
    }

    public static void requestPermission(Activity activity, String permission, int requestCode){
        ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
    }

    public interface PermissionRequestCode{
        int REQUEST_STORAGE_AT_RUNTIME = 0;
        int REQUEST_RECORD_AUDIO_AT_RUNTIME = 1;
    }
}
