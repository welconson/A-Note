package com.tcl.shenwk.aNote.task;

import android.os.AsyncTask;
import android.util.Log;

import com.tcl.shenwk.aNote.util.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadAsyncTask extends AsyncTask<DownloadAsyncTask.Params, Void, DownloadAsyncTask.Result> {
    private static final String TAG = "DownloadAsyncTask";
    private static final String PROTOCOL_CHARSET = "utf-8";
    private static final String PROTOCOL_CONTENT_TYPE =
            String.format("application/json; charset=%s", PROTOCOL_CHARSET);
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String HEADER_REQUEST_COOKIE = "cookie";

    private static final int RESULT_TYPE_SUCCESS = 1;
    private static final int RESULT_TYPE_403 = 2;
    private static final int RESULT_TYPE_404 = 3;
    private static final int RESULT_TYPE_EXCEPTION = 4;

    @Override
    protected Result doInBackground(Params... params) {
        Params param = params[0];
        try {
            HttpURLConnection connection = (HttpURLConnection) param.url.openConnection();
            connection.setDoInput(true);
            connection.addRequestProperty(HEADER_REQUEST_COOKIE, param.cookie);

            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.addRequestProperty(
                    HEADER_CONTENT_TYPE, PROTOCOL_CONTENT_TYPE);
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(Constants.JSON_DOWNLOAD_PATH, param.sourcePath);
            out.write(jsonObject.toString().getBytes(PROTOCOL_CHARSET));
            out.close();

            Log.i(TAG, "run: responseCode " + connection.getResponseCode());
            int responseCode = connection.getResponseCode();
            if(responseCode == 403){
                return new Result(RESULT_TYPE_403, "try to build session again");
            }else if(responseCode == 404){
                return new Result(RESULT_TYPE_404, "server has no file on path");
            }
            // download the file
            InputStream input = new BufferedInputStream(connection.getInputStream());

            // Output stream
            OutputStream output = new FileOutputStream(param.destPath);

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
            return new Result(RESULT_TYPE_SUCCESS, "");
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return new Result(RESULT_TYPE_EXCEPTION, e.getMessage());
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Result result) {
        String info;
        switch (result.resultType){
            case RESULT_TYPE_SUCCESS:{
                info = "";
                break;
            }
            case RESULT_TYPE_403:{
                info = "";
                break;
            }
            case RESULT_TYPE_404:{
                info = "";
                break;
            }
            case RESULT_TYPE_EXCEPTION:{
                info = "";
                break;
            }
        }
    }

    class Params{
        URL url;
        String cookie;
        String sourcePath;
        String destPath;
    }

    class Result{
        int resultType;
        String info;

        Result(int resultType, String info) {
            this.resultType = resultType;
            this.info = info;
        }
    }
}
