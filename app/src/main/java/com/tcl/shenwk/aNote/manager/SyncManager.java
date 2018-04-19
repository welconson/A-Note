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
import com.tcl.shenwk.aNote.network.DownloadInfoRequest;
import com.tcl.shenwk.aNote.network.NetworkBase;
import com.tcl.shenwk.aNote.util.Constants;
import com.tcl.shenwk.aNote.util.StringUtil;
import com.tcl.shenwk.aNote.util.UrlSource;

import org.json.JSONException;
import org.json.JSONObject;

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
    private int fullUploadFileNum;
    private int fullUploadFileCount;
    private String cookie;
    private Handler handler;

    public static final int SYNC_RESULT_BUILD_SESSION_SUCCESS = 0;
    public static final int SYNC_RESULT_UPLOAD_SESSION_SUCCESS = 1;

    public static final int SYNC_RESULT_NO_SESSION = 10;
    public static final int SYNC_RESULT_BUILD_SESSION_FAILED = 11;

    private static final int SERVER_SYNC_FULL_STATUS_YES = 1;
    private static final int SERVER_FULL_SYNC_STATUS_NO = 0;

    private static final int MSG_SYNC_FINISH = 0;

    private SyncManager(Context context){
        this.context = context;
        this.networkBase = NetworkBase.getInstance(context);
        handler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case MSG_SYNC_FINISH:{
                        fullUploadFileCount = 0;
                        fullUploadFileNum = 0;
                        synchronized (syncLock){
                            isSynchronizing = false;
                        }
                        cookie = "";
                        Toast.makeText(SyncManager.this.context, R.string.toast_sync_finished, Toast.LENGTH_SHORT).show();
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

    public void startManualSync(){
        if(isSynchronizing){
            Toast.makeText(context, R.string.toast_is_synchronizing_now, Toast.LENGTH_SHORT).show();
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
            sendBuildRequest(jsonObject, buildTime);
        }else {
            Log.i(TAG, "manualSync: no user logged in, can not build session");
        }
    }

    private void sendBuildRequest(final JSONObject jsonObject, final int buildTime){
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
                            checkSyncInfo(serverUpdateCode);
                            break;
                        }
                        case SYNC_RESULT_BUILD_SESSION_FAILED:{
                            Log.i(TAG, "onResponse: build session error times-" + buildTime);
                            if(buildTime < 5){
                                Log.i(TAG, "onResponse: rebuild session again");
                                sendBuildRequest(jsonObject, buildTime + 1);
                            }else{
                                Toast.makeText(context, R.string.toast_failed_build_session_with_server, Toast.LENGTH_SHORT).show();
                            }
                            break;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    cancelSession(null);
                    finishSync(false);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(TAG, "onErrorResponse: " + error);
                if(buildTime < 5)
                    sendBuildRequest(jsonObject, buildTime + 1);
                else {
                    finishSync(false);
                }
            }
        });
        networkBase.addRequest(buildSessionRequest);
    }

    private void checkSyncInfo(long serverUpdateCode){
        // check whether server need a full download or a full upload
        if(localUpdateCode == serverUpdateCode){
            // both ends have no data to complete a full synchronization
            Log.i(TAG, "checkSyncInfo: updateCode matched, nothing to do");
            cancelSession(null);
            finishSync(false);
        }
        else if(localUpdateCode == UPDATE_CODE_BLANK){
            // full download from server
            Log.i(TAG, "checkSyncInfo: fullDownload");
            sendFullDownLoadRequest();
        }
        else if(serverUpdateCode == UPDATE_CODE_BLANK){
            Log.i(TAG, "checkSyncInfo: fullUpload");
            fullUpload();
        }else {
            // get server database url, and to check increment update items.
            Log.i(TAG, "checkSyncInfo: incrementSync");
//            String databaseUrl = response.getString(Constants.JSON_SERVER_DATABASE_PATH);
//            Log.i(TAG, "checkSyncInfo: " + Constants.JSON_SERVER_DATABASE_PATH + " " + databaseUrl);
            incrementSync(serverUpdateCode);
        }
    }

    private void incrementSync(long serverUpdateCode) {
        cancelSession(null);
        finishSync(true);
    }

    private void sendFullDownLoadRequest() {
        Log.i(TAG, "sendFullDownLoadRequest: ");
        Map<String, String> header = new HashMap<>();
        header.put(DownloadInfoRequest.REQUEST_COOKIE, cookie);
        DownloadInfoRequest downloadInfoRequest = new DownloadInfoRequest(
                Request.Method.POST,
                UrlSource.URL_SYNC_FULL_DOWNLOAD,
                header,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, "DownloadInfoRequest onResponse: " + response.toString());
                        fullDownload(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        cancelSession(null);
                        finishSync(false);
                        Log.i(TAG, "DownloadInfoRequest onErrorResponse: " + error);
                    }
                });
        networkBase.addRequest(downloadInfoRequest);
    }

    private void fullUpload(){
        Log.i(TAG, "fullUpload: starting");
        new FullUploadManager(context, cookie, new FullUploadManager.FullUploadResultListener() {
            @Override
            public void onFinished() {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put(Constants.JSON_UPDATE_CODE, localUpdateCode);
                } catch (JSONException e) {
                    e.printStackTrace();
                    jsonObject = null;
                }

                cancelSession(jsonObject);
                finishSync(true);
            }

            @Override
            public void onError(String err) {
                Log.i(TAG, "fullUpload onError: " + err);
                cancelSession(null);
                finishSync(false);
            }
        }).start();
    }

    private void cancelSession(JSONObject jsonObject){
        Map<String, String> header = new HashMap<>();
        header.put(CancelSessionRequest.REQUEST_COOKIE, cookie);
        Request<JSONObject> request = new CancelSessionRequest(Request.Method.POST,
                UrlSource.URL_SYNC_CANCEL_SESSION,
                jsonObject,
                header,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, "cancelSession onResponse: " + response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i(TAG, "cancelSession onErrorResponse: " + error);
                    }
                }
        );
        networkBase.addRequest(request);
    }

    private void finishSync(boolean hasLocalDataModified){
        handler.sendMessage(handler.obtainMessage(MSG_SYNC_FINISH));
        if(hasLocalDataModified) {
            Log.i(TAG, "finishSync: send sync modified broadcast");
            DataProvider.getInstance(context).updateAllTopTagEntity();
            DataProvider.getInstance(context).updateNoteEntity();
            context.sendBroadcast(new Intent(Constants.BROADCAST_ACTION_SYNC_MODIFIED));
        }
    }

    // start asynchronous task for full download
    private void fullDownload(JSONObject jsonObject){
        try {
            new FullDownloadManager(
                    context,
                    new FullDownloadManager.FullDownloadResultListener() {
                        @Override
                        public void onFullDownloadFinish() {
                            Log.i(TAG, "onFullDownloadFinish: full download all done");
                            cancelSession(null);
                            finishSync(true);
                        }

                        @Override
                        public void onFullDownloadError(String error) {
                            Log.i(TAG, "onFullDownloadError: full download error");
                            cancelSession(null);
                            finishSync(true);
                        }
                    },
                    jsonObject.getJSONArray("paths"),
                    cookie
                    ).start();
        } catch (JSONException e) {
            e.printStackTrace();
            cancelSession(null);
            finishSync(false);
        }
    }

    public void increaseUpdateCode(){
        synchronized (updateCodeLock){
            localUpdateCode++;
            if(context.getSharedPreferences(Constants.PREFERENCE_USER_INFO, Context.MODE_PRIVATE)
                    .edit()
                    .putLong(Constants.PREFERENCE_FIELD_UPDATE_CODE, localUpdateCode)
                    .commit()) {
                Log.i(TAG, "increaseUpdateCode: increase local updateCode successfully " + localUpdateCode);
            }else {
                Log.i(TAG, "increaseUpdateCode: increase local updateCode failed " + localUpdateCode);
            }
        }
    }
}
