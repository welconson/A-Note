package com.tcl.shenwk.aNote.manager;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.tcl.shenwk.aNote.R;
import com.tcl.shenwk.aNote.util.Constants;
import com.tcl.shenwk.aNote.util.DateUtil;
import com.tcl.shenwk.aNote.view.activity.HomePageActivity;
import com.tcl.shenwk.aNote.view.activity.LoginActivity;
import com.tcl.shenwk.aNote.view.fragment.SignUpFragment;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by shenwk on 2018/4/3.
 */

public class LoginManager {
    private static final String TAG = "LoginManager";

    public boolean hasLoggedIn(Context context){
        return context.getSharedPreferences(Constants.USER_INFO, Context.MODE_PRIVATE).getBoolean(Constants.LOGIN_STATUS, false);
    }

    public void switchToFragment(FragmentManager fragmentManager, String tag){
        Class fragmentType;
        switch (tag){
            case LoginActivity.FRAGMENT_TAG_LOGIN:{
                fragmentType = SignUpFragment.class;
                break;
            }
            default:fragmentType = null;
        }
        if(fragmentType != null){
            Fragment fragment = null;
            try {
                fragment = ((Fragment) fragmentType.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
                Log.e(TAG, "switchToFragment: fragment class not found");
            }
            if(fragment != null) {
                fragmentManager.beginTransaction()
                        .add(R.id.fragment, fragment, tag)
                        .commit();
                Log.i(TAG, "switchToFragment: switch to " + tag + " fragment");
            }
        }
    }

    public void saveUserInfo(Context context, JSONObject jsonObject){
        if(jsonObject == null)
            return;
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.USER_INFO, Context.MODE_PRIVATE);
        try {
            sharedPreferences.edit()
                    .putString(Constants.USER_NAME, jsonObject.isNull("name") ? "" : ((String) jsonObject.get("name")))
                    .putString(Constants.USER_EMAIL, jsonObject.isNull("email") ? "" : ((String) jsonObject.get("email")))
                    .putLong(Constants.ACCOUNT_CREATE_TIME, jsonObject.isNull("createTime") ? DateUtil.getInstance().getTime() : ((long) jsonObject.get("createTime")))
                    .putBoolean(Constants.LOGIN_STATUS, true)
                    .apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void toHomePage(Context context){
        context.startActivity(new Intent(context, HomePageActivity.class));
    }
}

