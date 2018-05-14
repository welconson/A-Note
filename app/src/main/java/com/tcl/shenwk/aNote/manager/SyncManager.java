package com.tcl.shenwk.aNote.manager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.tcl.shenwk.aNote.R;
import com.tcl.shenwk.aNote.data.DataProvider;
import com.tcl.shenwk.aNote.network.BuildSessionRequest;
import com.tcl.shenwk.aNote.network.CancelSessionRequest;
import com.tcl.shenwk.aNote.network.CustomJsonRequest;
import com.tcl.shenwk.aNote.network.NetworkBase;
import com.tcl.shenwk.aNote.task.DownloadTask;
import com.tcl.shenwk.aNote.util.Constants;
import com.tcl.shenwk.aNote.util.DateUtil;
import com.tcl.shenwk.aNote.util.FileUtil;
import com.tcl.shenwk.aNote.util.StringUtil;
import com.tcl.shenwk.aNote.util.UrlSource;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Synchronization manager, it will check the sync status both server and client,
 * the determine direction of each data item which need to be synchronized.
 * Created by shenwk on 2018/4/11.
 */
public class SyncManager {
    private static final String TAG = "SyncManager";
    private static SyncManager mInstance;
    public static final long UPDATE_CODE_BLANK = 0;

    private long localUpdateCode;

    private Context context;
    private NetworkBase networkBase;
    private boolean isSynchronizing = false;
    private final Object syncLock = new Object();
    private final Object updateCodeLock = new Object();
    private String cookie;
    private Handler handler;

    public static final int SYNC_RESULT_BUILD_SESSION_SUCCESS = 0;
    public static final int SYNC_RESULT_UPLOAD_SESSION_SUCCESS = 1;
    public static final int SYNC_RESULT_DELETE_SUCCESS = 0;

    public static final int SYNC_RESULT_NO_SESSION = 10;
    public static final int SYNC_RESULT_BUILD_SESSION_FAILED = 11;

    private static final int SERVER_SYNC_FULL_STATUS_YES = 1;
    private static final int SERVER_FULL_SYNC_STATUS_NO = 0;

    private static final int MSG_SYNC_FINISH = 0;
    private static final int MSG_SYNC_BUILD_FAILED = 1;
    private static final int MSG_SYNC_DOING = 2;
    private static final int MSG_SYNC_ERROR = 3;

    private SyncManager(Context context){
        this.context = context;
        this.networkBase = NetworkBase.getInstance(context);
        handler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case MSG_SYNC_FINISH:{
                        synchronized (syncLock){
                            isSynchronizing = false;
                        }
                        cookie = "";
                        Toast.makeText(SyncManager.this.context, R.string.toast_sync_finished, Toast.LENGTH_SHORT).show();
                        break;
                    }
                    case MSG_SYNC_BUILD_FAILED:{
                        Toast.makeText(SyncManager.this.context, R.string.toast_failed_build_session_with_server, Toast.LENGTH_SHORT).show();
                        break;
                    }
                    case MSG_SYNC_DOING:{
                        Toast.makeText(SyncManager.this.context, R.string.toast_is_synchronizing_now, Toast.LENGTH_SHORT).show();
                        break;
                    }
                    case MSG_SYNC_ERROR:{
                        Toast.makeText(SyncManager.this.context, R.string.toast_sync_error, Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
            }
        };
        localUpdateCode = context.getSharedPreferences(Constants.PREFERENCE_USER_INFO, Context.MODE_PRIVATE)
                .getLong(Constants.PREFERENCE_FIELD_UPDATE_CODE, UPDATE_CODE_BLANK);
    }

    public static SyncManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SyncManager(context.getApplicationContext());
        }
        return mInstance;
    }

    // start manual synchronization, lock to set isSynchronizing
    public void startManualSync(){
        if(isSynchronizing){
            handler.sendMessage(handler.obtainMessage(MSG_SYNC_DOING));
        }
        else {
            synchronized (syncLock){
                isSynchronizing = true;
            }
            manualSync();
        }
    }

    private void manualSync(){
        JSONObject jsonObject = new JSONObject();
        int buildTime = 0;
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.PREFERENCE_USER_INFO, Context.MODE_PRIVATE);
        if(sharedPreferences.getBoolean(Constants.PREFERENCE_FIELD_LOGIN_STATUS, false)) {
            String email = sharedPreferences.getString(Constants.PREFERENCE_FIELD_USER_EMAIL, "");
            long userId = sharedPreferences.getLong(Constants.PREFERENCE_FIELD_USER_ID, -1);
            if (StringUtil.equal(email, "")) {
                Log.i(TAG, "manualSync: set json --- no email");
            }
            try {
                jsonObject.put(Constants.JSON_USER_ID, userId);
                jsonObject.put(Constants.JSON_USER_EMAIL, email);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            sendBuildRequest(jsonObject, buildTime, new BuildListener() {
                @Override
                public void onSuccess(long serverUpdateCode, long serverLastUpdateTime) {
                    checkSyncInfo(serverUpdateCode, serverLastUpdateTime);
                }

                @Override
                public void onError() {
                    handler.sendMessage(handler.obtainMessage(MSG_SYNC_BUILD_FAILED));
                }
            });
        }else {
            finishSync(MSG_SYNC_ERROR, false);
            Log.i(TAG, "manualSync: no user logged in, can not build session");
        }
    }

    private void sendBuildRequest(final JSONObject jsonObject, final int buildTime, final BuildListener buildListener){
        BuildSessionRequest buildSessionRequest = new BuildSessionRequest(Request.Method.POST, UrlSource.URL_SYNC_BUILD_SESSION,
                jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i(TAG, "onResponse: " + response);
                try {
                    int result = response.getInt(Constants.JSON_REQUEST_RESULT);
                    switch (result){
                        case SYNC_RESULT_BUILD_SESSION_SUCCESS:{
                            Log.i(TAG, "onResponse: build session successfully");
                            cookie = response.getString(BuildSessionRequest.SET_COOKIES);
                            long serverUpdateCode = response.getLong(Constants.JSON_UPDATE_CODE);
                            long serverLastUpdateTime = response.getLong(Constants.JSON_LAST_UPDATE_TIME);
                            if(buildListener != null)
                                buildListener.onSuccess(serverUpdateCode, serverLastUpdateTime);
                            break;
                        }
                        case SYNC_RESULT_BUILD_SESSION_FAILED:{
                            Log.i(TAG, "onResponse: build session error times-" + buildTime);
                            if(buildTime < 5){
                                Log.i(TAG, "onResponse: rebuild session again");
                                sendBuildRequest(jsonObject, buildTime + 1, buildListener);
                            }else{
                                if(buildListener != null)
                                    buildListener.onError();
                            }
                            break;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    if(buildListener != null)
                        buildListener.onError();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(TAG, "onErrorResponse: " + error);
                if(buildTime < 5)
                    sendBuildRequest(jsonObject, buildTime + 1, buildListener);
                else {
                    finishSync(MSG_SYNC_ERROR, false);
                }
            }
        });
        networkBase.addRequest(buildSessionRequest);
    }

    // compare updateCode of local and server:
    // 1.equal, do nothing, just to stop the synchronization
    // 2.local updateCode is blank, full download from server
    // 3.server updateCode is blank, full upload to server
    // 4.others, increment synchronization, need further analysis with both database.
    private void checkSyncInfo(long serverUpdateCode, long serverLastUpdateTime){
        // check whether server need a full download or a full upload
        if(localUpdateCode == serverUpdateCode){
            // both ends have no data to complete a full synchronization
            Log.i(TAG, "checkSyncInfo: updateCode matched, nothing to do");
            finishSync(MSG_SYNC_FINISH, false);
        }
        else if(localUpdateCode == UPDATE_CODE_BLANK){
            // full download from server
            Log.i(TAG, "checkSyncInfo: fullDownload");
            sendFullDownLoadRequest(serverUpdateCode);
        }
        else if(serverUpdateCode == UPDATE_CODE_BLANK){
            Log.i(TAG, "checkSyncInfo: fullUpload");
            fullUpload();
        }else {
            // get server database url, and to check increment update items.
            Log.i(TAG, "checkSyncInfo: incrementSync");
//            String databaseUrl = response.getString(Constants.JSON_SERVER_DATABASE_PATH);
//            Log.i(TAG, "checkSyncInfo: " + Constants.JSON_SERVER_DATABASE_PATH + " " + databaseUrl);
            incrementSync(serverUpdateCode, serverLastUpdateTime);
        }
    }

    private void incrementSync(long serverUpdateCode, long serverLastUpdateTime){
        IncrementSyncManager incrementSyncManager = new IncrementSyncManager(
                context,
                cookie,
                networkBase,
                new IncrementSyncManager.FinishListener() {
                    @Override
                    public void onSuccess(long updateCode) {
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put(Constants.JSON_UPDATE_CODE, updateCode);
                            jsonObject.put(Constants.JSON_LAST_UPDATE_TIME, DateUtil.getInstance().getTime());
                        } catch (JSONException e) {
                            e.printStackTrace();
                            jsonObject = null;
                        }

                        updateServerUpdateCode(jsonObject);
                        finishSync(MSG_SYNC_FINISH, true);
                    }

                    @Override
                    public void onError() {
                        finishSync(MSG_SYNC_ERROR, false);
                    }
                },
                localUpdateCode,
                serverUpdateCode,
                serverLastUpdateTime
        );
        incrementSyncManager.incrementSync();
    }

    private void sendFullDownLoadRequest(final long serverUpdateCode) {
        Log.i(TAG, "sendFullDownLoadRequest: ");
        Map<String, String> header = new HashMap<>();
        header.put(CustomJsonRequest.REQUEST_COOKIE, cookie);
        CustomJsonRequest customJsonRequest = new CustomJsonRequest(
                Request.Method.POST,
                UrlSource.URL_SYNC_FULL_DOWNLOAD,
                header,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, "CustomJsonRequest onResponse: " + response.toString());
                        String serverDatabase = null;
                        try {
                            serverDatabase = response.getString(Constants.JSON_SERVER_DATABASE_PATH);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        fullDownload(serverDatabase, serverUpdateCode);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        finishSync(MSG_SYNC_ERROR, false);
                        Log.i(TAG, "CustomJsonRequest onErrorResponse: " + error);
                    }
                });
        networkBase.addRequest(customJsonRequest);
    }

    private void fullUpload(){
        Log.i(TAG, "fullUpload: starting");
        new FullUploadManager(context, cookie, new FullUploadManager.FullUploadResultListener() {
            @Override
            public void onFinished() {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put(Constants.JSON_UPDATE_CODE, localUpdateCode);
                    jsonObject.put(Constants.JSON_LAST_UPDATE_TIME, DateUtil.getInstance().getTime());
                } catch (JSONException e) {
                    e.printStackTrace();
                    jsonObject = null;
                }

                updateServerUpdateCode(jsonObject);
                finishSync(MSG_SYNC_FINISH, true);
            }

            @Override
            public void onError(String err) {
                Log.i(TAG, "fullUpload onError: " + err);
                finishSync(MSG_SYNC_ERROR, false);
            }
        }).start();
    }

    private void updateServerUpdateCode(JSONObject jsonObject){
        Map<String, String> header = new HashMap<>();
        header.put(CancelSessionRequest.REQUEST_COOKIE, cookie);
        Request<JSONObject> request = new CancelSessionRequest(Request.Method.POST,
                UrlSource.URL_SYNC_UPDATE,
                jsonObject,
                header,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, "updateServerUpdateCode onResponse: " + response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i(TAG, "updateServerUpdateCode onErrorResponse: " + error);
                    }
                }
        );
        networkBase.addRequest(request);
    }

    //
    private void finishSync(int finishMsg, boolean hasLocalDataModified){
        handler.sendMessage(handler.obtainMessage(finishMsg));
        if(hasLocalDataModified) {
            Log.i(TAG, "finishSync: send sync modified broadcast");
            DataProvider.getInstance(context).updateAllTopTagEntity();
            DataProvider.getInstance(context).updateNoteEntity();
            context.sendBroadcast(new Intent(Constants.BROADCAST_ACTION_SYNC_MODIFIED));
        }
    }

    // start asynchronous task for full download
    private void fullDownload(String relativePath, final long serverUpdateCode){
        if(relativePath != null) {
            try {
                new DownloadTask(
                        new URL(UrlSource.URL_SYNC_DOWNLOAD),
                        relativePath,
                        FileUtil.getUserDirPath(context) + File.separator + relativePath,
                        cookie,
                        new DownloadTask.OnFinishListener() {
                            @Override
                            public void onSuccess() {
                                Log.i(TAG, "fullDownload download database: done");
                                synchronized (updateCodeLock) {
                                    localUpdateCode = serverUpdateCode;
                                    context.getSharedPreferences(Constants.PREFERENCE_USER_INFO, Context.MODE_PRIVATE)
                                            .edit()
                                            .putLong(Constants.PREFERENCE_FIELD_UPDATE_CODE, localUpdateCode)
                                            .apply();
                                }
                                finishSync(MSG_SYNC_FINISH, true);
                            }

                            @Override
                            public void onError(String err) {
                                Log.i(TAG, "fullDownload download database: download error");
                                finishSync(MSG_SYNC_ERROR, false);
                            }
                        }
                ).start();
//            new BatchDownloadManager(
//                    context,
//                    new BatchDownloadManager.FullDownloadResultListener() {
//                        @Override
//                        public void onFullDownloadFinish() {
//                            Log.i(TAG, "onFullDownloadFinish: full download all done");
//                            finishSync(true);
//                        }
//
//                        @Override
//                        public void onFullDownloadError(String error) {
//                            Log.i(TAG, "onFullDownloadError: full download error");
//                            finishSync(true);
//                        }
//                    },
//                    jsonObject.getJSONArray("paths"),
//                    cookie
//                    ).start();
            } catch (MalformedURLException e) {
                e.printStackTrace();
                finishSync(MSG_SYNC_ERROR, false);
            }
        }else {
            finishSync(MSG_SYNC_ERROR, false);
        }
    }

    // local component invoke this method to increase updateCode
    public void increaseUpdateCode(){
        synchronized (updateCodeLock){
            localUpdateCode++;
            if(context.getSharedPreferences(Constants.PREFERENCE_USER_INFO, Context.MODE_PRIVATE)
                    .edit()
                    .putLong(Constants.PREFERENCE_FIELD_UPDATE_CODE, localUpdateCode)
                    .putLong(Constants.PREFERENCE_FIELD_LAST_MODIFY_TIME, DateUtil.getInstance().getTime())
                    .commit()) {
                Log.i(TAG, "increaseUpdateCode: increase local updateCode successfully " + localUpdateCode);
            }else {
                Log.i(TAG, "increaseUpdateCode: increase local updateCode failed " + localUpdateCode);
            }
        }
    }

    public void realTimeDownload(final URL url, final String downloadPath, final String storePath, final DownloadTask.OnFinishListener onFinishListener){
        JSONObject jsonObject = new JSONObject();
        int buildTime = 0;
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.PREFERENCE_USER_INFO, Context.MODE_PRIVATE);
        if(sharedPreferences.getBoolean(Constants.PREFERENCE_FIELD_LOGIN_STATUS, false)) {
            String email = sharedPreferences.getString(Constants.PREFERENCE_FIELD_USER_EMAIL, "");
            long userId = sharedPreferences.getLong(Constants.PREFERENCE_FIELD_USER_ID, -1);
            if (StringUtil.equal(email, "")) {
                Log.i(TAG, "manualSync: set json --- no email");
            }
            try {
                jsonObject.put(Constants.JSON_USER_ID, userId);
                jsonObject.put(Constants.JSON_USER_EMAIL, email);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            sendBuildRequest(jsonObject, buildTime, new BuildListener() {
                @Override
                public void onSuccess(long serverUpdateCode, long serverLastUpdateTime) {
                    new DownloadTask(
                            url,
                            downloadPath,
                            storePath,
                            cookie,
                            onFinishListener).start();
                }

                @Override
                public void onError() {
                    handler.sendMessage(handler.obtainMessage(MSG_SYNC_BUILD_FAILED));
                }
            });
        }else {
            Log.i(TAG, "manualSync: no user logged in, can not build session");
        }
    }

    // when login, reset user's synchronization data.
    public void reset(){
        localUpdateCode = context.getSharedPreferences(Constants.PREFERENCE_USER_INFO, Context.MODE_PRIVATE)
                .getLong(Constants.PREFERENCE_FIELD_UPDATE_CODE, UPDATE_CODE_BLANK);
        cookie = null;
        isSynchronizing = false;
    }

    private interface BuildListener{
        void onSuccess(long serverUpdateCode, long serverLastUpdateTime);
        void onError();
    }
}
