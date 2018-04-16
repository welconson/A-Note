package com.tcl.shenwk.aNote.network;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.Map;

public class CancelSessionRequest extends JsonObjectRequest {
    private static final String TAG = "CancelSessionRequest";
    public static final String REQUEST_COOKIE = "cookie";
    private Map<String, String> header;

    public CancelSessionRequest(int method, String url, JSONObject jsonRequest, Map<String, String> header, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener);
        this.header = header;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return header == null ? super.getHeaders() : header;
    }
}
