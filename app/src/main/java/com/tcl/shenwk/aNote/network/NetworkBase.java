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
    private static NetworkBase mInstance;
    private RequestQueue requestQueue;

    public static NetworkBase getInstance(Context context){
        if(mInstance == null){
            mInstance = new NetworkBase(context.getApplicationContext());
        }
        return mInstance;
    }

    private NetworkBase(Context context){
        // Instantiate the RequestQueue.
        requestQueue = Volley.newRequestQueue(context);
    }

    public void addRequest(Request request){
        // Add the request to the RequestQueue.
        requestQueue.add(request);
    }
}
