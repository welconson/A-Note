package com.tcl.shenwk.aNote.manager;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SearchRecentSuggestionsProvider;
import android.content.SharedPreferences;
import android.util.Log;

import com.tcl.shenwk.aNote.R;
import com.tcl.shenwk.aNote.data.ANoteContentProvider;
import com.tcl.shenwk.aNote.data.ANoteDBManager;
import com.tcl.shenwk.aNote.data.ContentProviderConstants;
import com.tcl.shenwk.aNote.util.Constants;
import com.tcl.shenwk.aNote.util.DateUtil;
import com.tcl.shenwk.aNote.util.FileUtil;
import com.tcl.shenwk.aNote.util.StringUtil;
import com.tcl.shenwk.aNote.util.UrlSource;
import com.tcl.shenwk.aNote.view.activity.HomePageActivity;
import com.tcl.shenwk.aNote.view.activity.LoginActivity;
import com.tcl.shenwk.aNote.view.fragment.SignUpFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * Created by shenwk on 2018/4/3.
 */

public class LoginManager {
    private static final String TAG = "LoginManager";
    private static LoginManager mInstance;
    public static String userFolder = null;
    private Context context;

    private LoginManager(Context context) {
        this.context = context;
        userFolder = context.getSharedPreferences(Constants.PREFERENCE_USER_INFO,
                Context.MODE_PRIVATE).getString("email", "anonymous");
    }

    public static LoginManager getInstance(Context context){
        if(mInstance == null)
            mInstance = new LoginManager(context.getApplicationContext());
        return mInstance;
    }

    // check whether there is user logged in.
    public boolean hasLoggedIn(Context context){
        return context.getSharedPreferences(Constants.PREFERENCE_USER_INFO, Context.MODE_PRIVATE).getBoolean(Constants.PREFERENCE_FIELD_LOGIN_STATUS, false);
    }

    // switch to fragment inside LoginActivity.
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
                        .addToBackStack(null)
                        .commit();
                Log.i(TAG, "switchToFragment: switch to " + tag + " fragment");
            }
        }
    }

    public void saveUserInfo(Context context, JSONObject jsonObject){
        if(jsonObject == null)
            return;
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.PREFERENCE_USER_INFO, Context.MODE_PRIVATE);
        try {
            userFolder = jsonObject.isNull("email") ? "" : ((String) jsonObject.get("email"));
            if(StringUtil.equal(userFolder, "")) {
                Log.i(TAG, "saveUserInfo: get user email failed");
                return;
            }
            else FileUtil.createDir(context.getFilesDir() + File.separator + userFolder);
            sharedPreferences.edit()
                    .putLong(Constants.PREFERENCE_FIELD_USER_ID, jsonObject.getLong("userId"))
                    .putString(Constants.PREFERENCE_FIELD_USER_NAME, jsonObject.isNull("name") ? "" : ((String) jsonObject.get("name")))
                    .putString(Constants.PREFERENCE_FIELD_USER_EMAIL, userFolder)
                    .putLong(Constants.PREFERENCE_FIELD_ACCOUNT_CREATE_TIME, jsonObject.isNull("createTime") ? DateUtil.getInstance().getTime() : ((long) jsonObject.get("createTime")))
                    .putBoolean(Constants.PREFERENCE_FIELD_LOGIN_STATUS, true)
                    .apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // navigate to HomePageActivity from LoginActivity.
    public void toHomePage(Context context){
        SyncManager.getInstance(context).startSync();
        context.startActivity(new Intent(context, HomePageActivity.class));
    }

    public void moveOriginalFileToUserStorage(String appRootPath, String userFolder){

    }

    public void logOut(Context context){
        // clear login status, set next time for login to run a full download if possible
        context.getSharedPreferences(Constants.PREFERENCE_USER_INFO, Context.MODE_PRIVATE).edit().putBoolean(Constants.PREFERENCE_FIELD_LOGIN_STATUS, false)
                .putBoolean(Constants.PREFERENCE_FIELD_NEED_FULL_DOWNLOAD, true)
                .apply();
        // close database so the database file can be delete
        ContentProviderClient contentProviderClient = context.getContentResolver().acquireContentProviderClient(ContentProviderConstants.NOTE_TABLE_URI);
        ANoteContentProvider aNoteContentProvider = null;
        if(contentProviderClient != null) {
            aNoteContentProvider = ((ANoteContentProvider) contentProviderClient.getLocalContentProvider());
            contentProviderClient.release();
            if(aNoteContentProvider != null){
                aNoteContentProvider.closeDatabase();
            }
        }

        // delete user files and database
        FileUtil.deleteDirectoryAndFiles(FileUtil.getUserDirPath(context));
        String databasePath = context.getDatabasePath(Constants.A_NOTE_DATA_DATABASE_NAME).getPath();
        if(FileUtil.isFileOrDirectoryExist(databasePath))
            FileUtil.deleteFile(databasePath);
        if(FileUtil.isFileOrDirectoryExist(databasePath + "-journal"))
            FileUtil.deleteFile(databasePath + "-journal");

        if(aNoteContentProvider != null){
            aNoteContentProvider.resetDBHelper();
        }
        context.startActivity(new Intent(context, LoginActivity.class));
    }
}

