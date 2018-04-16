package com.tcl.shenwk.aNote.network;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;


/**
 * Volley request for uploading a file, and together with a json response.
 * Created by shenwk on 2018/4/11.
 */
public class UploadFileRequest extends Request<JSONObject> {
    private static final String TAG = "UploadFileRequest";
    protected static final String PROTOCOL_CHARSET = "utf-8";
    public static final String REQUEST_COOKIE = "cookie";

    private final String lineEnd = "\r\n";
    private final String boundary = "a-note-" + System.currentTimeMillis();
    private final String twoHyphens = "--";
    private final String mimeType = "multipart/form-data;boundary=" + boundary;

    private final Response.Listener<JSONObject> mListener;
    private final Response.ErrorListener mErrorListener;
    private final Map<String, String> mHeaders;
    private final File file;
    private final String fileType;
    private final String relativePath;

    /**
     *
     * @param url               url for request.
     * @param headers           headers info for request, such as user info.
     * @param listener
     * @param errorListener
     */
    public UploadFileRequest(String url, Map<String, String> headers, String type, File file, String relativePath,
                             Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(Method.POST, url, errorListener);
        this.mListener = listener;
        this.mErrorListener = errorListener;
        this.mHeaders = headers;
        this.file = file;
        this.fileType = type;
        this.relativePath = relativePath;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return (mHeaders != null) ? mHeaders : super.getHeaders();
    }

    @Override
    public String getBodyContentType() {
        return mimeType;
    }

    @Override
    public byte[] getBody(){
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1024 * 1024;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        try {
            dos.writeBytes(twoHyphens + boundary + lineEnd);

            if(fileType != null) {
                dos.writeBytes("Content-Disposition: form-data; name=\"type\"" + lineEnd);
                dos.writeBytes(lineEnd);
                dos.writeBytes(fileType);
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + lineEnd);
            }

            if(relativePath != null) {
                dos.writeBytes("Content-Disposition: form-data; name=\"relativePath\"" + "" + lineEnd);
                dos.writeBytes(lineEnd);
                dos.writeBytes(relativePath);
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + lineEnd);
            }

            if(file == null)
                return bos.toByteArray();

            dos.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\""
                    + file.getName() + "\"" + lineEnd);
            dos.writeBytes(lineEnd);
            FileInputStream fileInputStream = new FileInputStream(file);
            bytesAvailable = fileInputStream.available();

            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            // read file and write it into form...
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {
                dos.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }

            // send multipart form data necessary after file data...
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            return bos.toByteArray();

        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "getBody: wrong");
        return null;
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET));
            return Response.success(new JSONObject(jsonString), HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException | JSONException var3) {
            return Response.error(new ParseError(var3));
        }
    }

    @Override
    protected void deliverResponse(JSONObject response) {
        mListener.onResponse(response);
    }

    @Override
    public void deliverError(VolleyError error) {
        mErrorListener.onErrorResponse(error);
    }
}
