package com.tcl.shenwk.aNote.network;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

/**
 * Created by shenwk on 2018/4/4.
 */

public class NetworkBase {
    private RequestQueue requestQueue;

    public NetworkBase(Context context){
        // Instantiate the RequestQueue.
        requestQueue = Volley.newRequestQueue(context);


    }

    public void sendRequest(String url, JSONObject json, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener){
        // Request a string response from the provided URL.
        JsonRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, json, listener, errorListener);
//        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, listener, errorListener);

        // Add the request to the RequestQueue.
        requestQueue.add(jsonRequest);
    }
}
