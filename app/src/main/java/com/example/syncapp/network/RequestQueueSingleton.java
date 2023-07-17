package com.example.syncapp.network;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class RequestQueueSingleton {
    private static RequestQueue instance;

    private RequestQueueSingleton() {
    }

    public static synchronized RequestQueue getInstance(Context context) {
        if (instance == null) {
            instance = Volley.newRequestQueue(context.getApplicationContext());
        }
        return instance;
    }

    public static void add(Context context, Request<JSONObject> request) {
        request.setShouldCache(false);
        getInstance(context).add(request);
    }
}





