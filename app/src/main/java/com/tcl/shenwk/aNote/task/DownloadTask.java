package com.tcl.shenwk.aNote.task;

import android.util.Log;

import com.tcl.shenwk.aNote.util.Constants;
import com.tcl.shenwk.aNote.util.UrlSource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadTask {
    private static final String TAG = "DownloadTask";
    private static final String PROTOCOL_CHARSET = "utf-8";
    private static final String PROTOCOL_CONTENT_TYPE =
            String.format("application/json; charset=%s", PROTOCOL_CHARSET);
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String HEADER_REQUEST_COOKIE = "cookie";
    private Thread thread;
    private OnFinishListener onFinishListener;

    public DownloadTask(final URL url, final String downloadPath, final String storePath, final String cookie, final OnFinishListener onFinishListener) {
        this.onFinishListener = onFinishListener;
        this.thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.addRequestProperty(HEADER_REQUEST_COOKIE, cookie);

                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    connection.addRequestProperty(
                            HEADER_CONTENT_TYPE, PROTOCOL_CONTENT_TYPE);
                    DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(Constants.JSON_DOWNLOAD_PATH, downloadPath);
                    out.write(jsonObject.toString().getBytes(PROTOCOL_CHARSET));
                    out.close();

                    Log.i(TAG, "run: responseCode " + connection.getResponseCode());
                    int responseCode = connection.getResponseCode();
                    if(responseCode == 403){
                        if(onFinishListener != null){
                            onFinishListener.onError("try to build session again");
                        }
                        return;
                    }else if(responseCode == 404){
                        if(onFinishListener != null){
                            onFinishListener.onError("server has no file on path");
                        }
                    }
                    // download the file
                    InputStream input = new BufferedInputStream(connection.getInputStream());

                    // Output stream
                    OutputStream output = new FileOutputStream(storePath);

                    byte data[] = new byte[1024];

                    int count;

                    while ((count = input.read(data)) != -1) {
                        // writing data to file
                        output.write(data, 0, count);
                    }

                    // flushing output
                    output.flush();

                    // closing streams
                    output.close();
                    input.close();
                    if(onFinishListener != null){
                        onFinishListener.onSuccess();
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    if (onFinishListener != null) {
                        onFinishListener.onError(e.getMessage());
                    }
                }
            }
        });
    }

    public interface OnFinishListener{
        void onSuccess();
        void onError(String err);
    }

    public void start(){
        thread.start();
    }
}
