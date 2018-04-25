package com.tcl.shenwk.aNote.manager;

import android.content.Context;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.tcl.shenwk.aNote.data.FullUploadDBManager;
import com.tcl.shenwk.aNote.manager.LoginManager;
import com.tcl.shenwk.aNote.manager.SyncManager;
import com.tcl.shenwk.aNote.network.NetworkBase;
import com.tcl.shenwk.aNote.network.UploadFileRequest;
import com.tcl.shenwk.aNote.util.Constants;
import com.tcl.shenwk.aNote.util.DateUtil;
import com.tcl.shenwk.aNote.util.StringUtil;
import com.tcl.shenwk.aNote.util.UrlSource;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FullUploadManager {
    private static final String TAG = "FullUploadManager";
    private int fullUploadFileNum;
    private int fullUploadFileCount;
    private Context context;
    private String cookie;
    private FullUploadResultListener fullUploadResultListener;

    public FullUploadManager(Context context, String cookie, FullUploadResultListener fullUploadResultListener) {
        this.context = context;
        this.cookie = cookie;
        this.fullUploadResultListener = fullUploadResultListener;
    }

    public void start(){
        File databasePath = context.getDatabasePath(Constants.A_NOTE_DATA_DATABASE_NAME);
        if(databasePath == null || !databasePath.exists()){
            Log.i(TAG, "fullUpload: no local database");
            if(fullUploadResultListener != null){
                fullUploadResultListener.onError("no local database");
            }
            return;
        }
        setSyncSettingBeforeUpload(DateUtil.getInstance().getTime());
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
                                    case SyncManager.SYNC_RESULT_UPLOAD_SESSION_SUCCESS: {
                                        Log.i(TAG, "fullUpload onResponse: upload a file success");
                                        break;
                                    }
                                    case SyncManager.SYNC_RESULT_NO_SESSION: {
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
            NetworkBase.getInstance(context).addRequest(uploadFileRequest);
        }
    }

    public interface FullUploadResultListener {
        void onFinished();
        void onError(String err);
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

    private void onFullUploadItemResponse(){
        if(fullUploadFileNum > ++fullUploadFileCount){
            Log.i(TAG, "onFullUploadItemResponse: total = " + fullUploadFileNum + ", finished = " + fullUploadFileCount);
        }else {
            Log.i(TAG, "onFullUploadItemResponse: full upload done");
            if(fullUploadResultListener != null){
                fullUploadResultListener.onFinished();
            }
        }
    }

    // before upload database, set sync item field inside local database
    private void setSyncSettingBeforeUpload(long updateTime){
        FullUploadDBManager fullUploadDBManager = new FullUploadDBManager(context);
        fullUploadDBManager.syncItemSetting(updateTime);
    }
}
