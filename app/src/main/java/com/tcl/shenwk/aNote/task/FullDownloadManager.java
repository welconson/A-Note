package com.tcl.shenwk.aNote.task;

import android.content.Context;
import android.util.Log;

import com.tcl.shenwk.aNote.util.FileUtil;
import com.tcl.shenwk.aNote.util.UrlSource;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class FullDownloadManager {
    private static final String TAG = "FullDownloadManager";
    private final int totalTaskNum;
    private int finishedTaskNum;
    private int errorTaskNum;
    private FullDownloadResultListener fullDownloadResultListener;
    private JSONArray pathList;
    private String cookie;
    private Context context;
    private final Object lock = new Object();

    public FullDownloadManager(
            Context context, FullDownloadResultListener fullDownloadResultListener,
            JSONArray pathList, String cookie) {
        this.context = context;
        this.totalTaskNum = pathList.length();
        this.fullDownloadResultListener = fullDownloadResultListener;
        this.pathList = pathList;
        this.cookie = cookie;
    }

    public interface FullDownloadResultListener{
        void onFullDownloadFinish();
        void onFullDownloadError(String error);
    }

    public void start(){
        finishedTaskNum = 0;
        errorTaskNum = 0;
        try {
            URL url = new URL(UrlSource.URL_SYNC_DOWNLOAD);
            for(int i = 0; i < pathList.length(); i++){
                String relativePath = pathList.getString(i);
                createDirRecursively(context.getFilesDir() + File.separator +
                        relativePath.substring(0, relativePath.lastIndexOf("/")));
                new DownloadTask(url,
                        relativePath,
                        context.getFilesDir() + File.separator + relativePath,
                        cookie,
                        new DownloadTask.OnFinishListener() {
                            @Override
                            public void onSuccess() {
                                synchronized (lock){
                                    finishedTaskNum++;
                                    onTaskFinish();
                                }
                                Log.i(TAG, "DownloadTask onSuccess: thread " + Thread.currentThread());
                            }
                            @Override
                            public void onError(String err) {
                                synchronized (lock){
                                    finishedTaskNum++;
                                    errorTaskNum++;
                                    onTaskFinish();
                                }
                                Log.i(TAG, "DownloadTask onError: thread " + Thread.currentThread());
                                Log.i(TAG, "DownloadTask onError: " + err);
                            }
                        }).start();
            }
        } catch (JSONException | MalformedURLException e) {
            e.printStackTrace();
            if(fullDownloadResultListener != null){
                fullDownloadResultListener.onFullDownloadError(e.getMessage());
            }
        }
    }

    private void onTaskFinish(){
        if(finishedTaskNum == totalTaskNum){
            if(fullDownloadResultListener != null){
                fullDownloadResultListener.onFullDownloadFinish();
            }
        }
    }

    private void createDirRecursively(String dirPath){
        if(FileUtil.isFileOrDirectoryExist(dirPath))
            return;
        int last = dirPath.lastIndexOf("/");
        if(last == -1){
            FileUtil.createDir(dirPath);
            return;
        }
        String rootPath = dirPath.substring(0,  last);
        if(FileUtil.isFileOrDirectoryExist(rootPath)){
            FileUtil.createDir(dirPath);
        }else {
            createDirRecursively(rootPath);
            FileUtil.createDir(dirPath);
        }
    }
}
