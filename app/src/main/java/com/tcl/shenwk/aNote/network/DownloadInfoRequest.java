package com.tcl.shenwk.aNote.network;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

public class DownloadInfoRequest extends JsonObjectRequest {
    private static final String TAG = "DownloadInfoRequest";
    public static final String REQUEST_COOKIE = "cookie";
    private Map<String, String> header;

    public DownloadInfoRequest(int method, String url, Map<String, String> header, JSONObject jsonRequest, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener);
        this.header = header;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return header == null ? super.getHeaders() : header;
    }
}
