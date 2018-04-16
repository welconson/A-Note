package com.tcl.shenwk.aNote.manager;

import android.content.Context;
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
import com.tcl.shenwk.aNote.network.BuildSessionRequest;
import com.tcl.shenwk.aNote.network.CancelSessionRequest;
import com.tcl.shenwk.aNote.network.DownloadInfoRequest;
import com.tcl.shenwk.aNote.network.NetworkBase;
import com.tcl.shenwk.aNote.network.UploadFileRequest;
import com.tcl.shenwk.aNote.task.DownloadTask;
import com.tcl.shenwk.aNote.task.FullDownloadManager;
import com.tcl.shenwk.aNote.util.Constants;
import com.tcl.shenwk.aNote.util.FileUtil;
import com.tcl.shenwk.aNote.util.StringUtil;
import com.tcl.shenwk.aNote.util.UrlSource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Synchronization manager, it will check the sync status both server and client,
 * the determine direction of each data item which need to be synchronized.
 * Created by shenwk on 2018/4/11.
 */
public class SyncManager {
    private static final String TAG = "SyncManager";
    private static SyncManager mInstance;

    private Context context;
    private NetworkBase networkBase;
    private boolean isSynchronizing = false;
    private final Object mLock = new Object();
    private int fullUploadFileNum;
    private int fullUploadFileCount;
    private String cookie;
    private Handler handler;

    private static final int SYNC_RESULT_BUILD_SESSION_SUCCESS = 0;
    private static final int SYNC_RESULT_UPLOAD_SESSION_SUCCESS = 1;

    private static final int SYNC_RESULT_NO_SESSION = 10;
    private static final int SYNC_RESULT_BUILD_SESSION_FAILED = 11;

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
                        synchronized (mLock){
                            isSynchronizing = false;
                        }
                        cookie = "";
                        Toast.makeText(SyncManager.this.context, R.string.toast_sync_finished, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };
    }

    public static SyncManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SyncManager(context.getApplicationContext());
        }
        return mInstance;
    }

    public void startSync(){
        if(isSynchronizing){
            Toast.makeText(context, R.string.toast_is_synchronizing_now, Toast.LENGTH_SHORT).show();
        }
        else {
            synchronized (mLock){
                isSynchronizing = true;
            }
            sync();
        }
    }

    private void sync(){
        JSONObject jsonObject = new JSONObject();
        int buildTime = 0;
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.PREFERENCE_USER_INFO, Context.MODE_PRIVATE);
        if(sharedPreferences.getBoolean(Constants.PREFERENCE_FIELD_LOGIN_STATUS, false)) {
            String email = sharedPreferences.getString(Constants.PREFERENCE_FIELD_USER_EMAIL, "");
            long userId = sharedPreferences.getLong(Constants.PREFERENCE_FIELD_USER_ID, -1);
            if (StringUtil.equal(email, "")) {
                Log.i(TAG, "sync: set json --- no email");
            }
            try {
                jsonObject.put(Constants.JSON_USER_ID, userId);
                jsonObject.put(Constants.JSON_USER_EMAIL, email);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            sendBuildRequest(jsonObject, buildTime);
        }else {
            Log.i(TAG, "sync: no user logged in, can not build session");
        }
    }

    private void sendBuildRequest(final JSONObject jsonObject, final int buildTime){
        BuildSessionRequest jsonRequest = new BuildSessionRequest(Request.Method.POST, UrlSource.URL_SYNC_BUILD_SESSION,
                jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i(TAG, "onResponse: " + response);
                try {
                    int result = response.getInt(Constants.JSON_REQUEST_RESULT);
                    switch (result){
                        case SYNC_RESULT_BUILD_SESSION_SUCCESS:{
                            Log.i(TAG, "onResponse: build session successfully");
                            boolean needFullDownload = context.getSharedPreferences(Constants.PREFERENCE_USER_INFO, Context.MODE_PRIVATE)
                                    .getBoolean(Constants.PREFERENCE_FIELD_NEED_FULL_DOWNLOAD, true);
                            cookie = response.getString(BuildSessionRequest.SET_COOKIES);
                            int serverFullSyncStatus = response.getInt(Constants.JSON_SERVER_FULL_SYNC_STATUS);
                            // check whether server need a full download or a full upload
                            if(needFullDownload){
                                if(serverFullSyncStatus == SERVER_FULL_SYNC_STATUS_NO) {
                                    // both ends have no data to complete a full synchronization
                                    Log.i(TAG, "build session onResponse: no data on both ends");
                                    context.getSharedPreferences(Constants.PREFERENCE_USER_INFO, Context.MODE_PRIVATE)
                                            .edit().putBoolean(Constants.PREFERENCE_FIELD_NEED_FULL_DOWNLOAD, false)
                                            .apply();
                                    cancelSession(null);
                                    finishSync();
                                }
                                else {
                                    // full download from server
                                    Log.i(TAG, "build session onResponse: fullDownload");
                                    sendFullDownLoadRequest();
                                }
                            }
                            else if(serverFullSyncStatus == SERVER_FULL_SYNC_STATUS_NO){
                                Log.i(TAG, "build session onResponse: fullUpload");
                                fullUpload();
                            }else {
                                // get server database url, and to check increment update items.
                                Log.i(TAG, "build session onResponse: incrementSync");
                                String databaseUrl = response.getString(Constants.JSON_SERVER_DATABASE_URL);
                                Log.i(TAG, "onResponse: databaseUrl " + databaseUrl);
                                incrementSync(databaseUrl);
                            }
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
                    finishSync();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(TAG, "onErrorResponse: " + error);
                if(buildTime < 5)
                    sendBuildRequest(jsonObject, buildTime + 1);
                else {
                    finishSync();
                }
            }
        });
        networkBase.addRequest(jsonRequest);
    }

    private void incrementSync(String databaseUrl) {

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
                        finishSync();
                        Log.i(TAG, "DownloadInfoRequest onErrorResponse: " + error);
                    }
                });
        networkBase.addRequest(downloadInfoRequest);
    }

    private void fullUpload(){
        Log.i(TAG, "fullUpload: starting");
        File databasePath = context.getDatabasePath(Constants.A_NOTE_DATA_DATABASE_NAME);
        if(databasePath == null || !databasePath.exists()){
            Log.i(TAG, "fullUpload: no database local");
            cancelSession(null);
            finishSync();
            return;
        }
        Map<String, String> header = new HashMap<>();
        header.put(UploadFileRequest.REQUEST_COOKIE, cookie);
        List<FileItem> fileItems = new ArrayList<>();
        fileItems.add(new FileItem(databasePath, Constants.SYNC_FILE_TYPE_DATABASE, null));
        File userDir = new File(context.getFilesDir() + File.separator + LoginManager.userFolder);
        if(userDir.exists()) {
            for(File file : userDir.listFiles()){
                if(file.isDirectory()){
                    acquireFileItem(fileItems, file, file.getName());
                }
            }
        }
        Log.i(TAG, "fullUpload:  cookie " + cookie);
        fullUploadFileNum = fileItems.size();
        fullUploadFileCount = 0;
        for(FileItem fileItem : fileItems) {
            UploadFileRequest uploadFileRequest = new UploadFileRequest(UrlSource.URL_SYNC_UPLOAD,
                    header,
                    fileItem.type,
                    fileItem.file,
                    fileItem.relativePath,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                switch (response.getInt(Constants.JSON_REQUEST_RESULT)) {
                                    case SYNC_RESULT_UPLOAD_SESSION_SUCCESS: {
                                        Log.i(TAG, "fullUpload onResponse: upload a file success");
                                        break;
                                    }
                                    case SYNC_RESULT_NO_SESSION: {
                                        Log.i(TAG, "fullUpload onResponse: no session info");
                                        break;
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            onFullUploadItemResponse();
                            Log.i(TAG, "onResponse: " + response);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    onFullUploadItemResponse();
                    Log.i(TAG, "onErrorResponse: " + error);
                }
            });
            networkBase.addRequest(uploadFileRequest);
        }
    }

    private void acquireFileItem(List<FileItem> fileItems, File dir, String nowDir){
        for(File file : dir.listFiles()){
            if(file.isDirectory()){
                acquireFileItem(fileItems, file, nowDir + File.separator + file.getName());
            }else {
                String type;
                if(StringUtil.equal(dir.getName(), Constants.RESOURCE_DIR))
                    type = Constants.SYNC_FILE_TYPE_RESOURCE;
                else type = Constants.SYNC_FILE_TYPE_CONTENT;
                fileItems.add(new FileItem(file, type, nowDir));
            }
        }
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

    private void onFullUploadItemResponse(){
        if(fullUploadFileNum > ++fullUploadFileCount){
            Log.i(TAG, "onFullUploadItemResponse: total = " + fullUploadFileNum + ", finished = " + fullUploadFileCount);
        }else {
            Log.i(TAG, "onFullUploadItemResponse: full upload done");
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(Constants.JSON_SERVER_FULL_SYNC_STATUS, SERVER_SYNC_FULL_STATUS_YES);
            } catch (JSONException e) {
                e.printStackTrace();
                jsonObject = null;
            }
            cancelSession(jsonObject);
            finishSync();
        }
    }

    private void finishSync(){
        handler.sendMessage(handler.obtainMessage(MSG_SYNC_FINISH));
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
                            finishSync();
                        }

                        @Override
                        public void onFullDownloadError(String error) {
                            Log.i(TAG, "onFullDownloadError: full download error");
                            finishSync();
                        }
                    },
                    jsonObject.getJSONArray("paths"),
                    cookie
                    ).start();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // upload file item class
    class FileItem {
        File file;
        String type;
        String relativePath;

        private FileItem(File file, String type, String relativePath) {
            this.file = file;
            this.type = type;
            this.relativePath = relativePath;
        }
    }
}
