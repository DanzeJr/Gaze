package com.ecotioco.gaze.connection;

import android.content.Context;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.Volley;
import com.ecotioco.gaze.data.Constant;

import java.util.HashMap;
import java.util.Map;

public class VolleyController {
    private static VolleyController sInstance;
    private RequestQueue mRequestQueue;
    private static Context sContext;

    private VolleyController(Context context) {
        sContext = context.getApplicationContext();
        mRequestQueue = getRequestQueue();
    }

    public static synchronized VolleyController getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new VolleyController(context);
        }
        return sInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(sContext);

            /* Another way with caching response
            // Instantiate the cache
            Cache cache = new DiskBasedCache(sContext.getApplicationContext().getCacheDir(), 1024 * 1024); // 1MB cap

            // Set up the network to use HttpURLConnection as the HTTP client.
            Network network = new BasicNetwork(new HurlStack());

            // Instantiate the RequestQueue with the cache and network.
            mRequestQueue = new RequestQueue(cache, network);

            // Start the queue
            mRequestQueue.start();
            */
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
}
