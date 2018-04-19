package com.tcl.shenwk.aNote.network;

import android.support.annotation.NonNull;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;


/**
 * Volley request for a specifical user to connect to server, together with a json response.
 * Created by shenwk on 2018/4/11.
 */
public class BuildSessionRequest extends JsonRequest<JSONObject> {
    private static final String TAG = "BuildSessionRequest";
    public static final String SET_COOKIES = "set-cookie";

    public BuildSessionRequest(int method, String url, JSONObject jsonRequest, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(method, url, (jsonRequest == null) ? null : jsonRequest.toString(), listener,
                errorListener);
        this.setRetryPolicy(new DefaultRetryPolicy(5000, 0, 1));
    }

    // Override to acquire the header field of set-cookie, which used in later request to show
    // they are a serial request of a user's synchronization.
    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET));
            JSONObject jsonObject = new JSONObject(jsonString);
            jsonObject.put(SET_COOKIES, response.headers.get(SET_COOKIES));
            return Response.success(jsonObject, HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException | JSONException e) {
            return Response.error(new ParseError(e));
        }
    }
}
