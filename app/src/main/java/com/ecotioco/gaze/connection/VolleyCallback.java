package com.ecotioco.gaze.connection;

import com.android.volley.VolleyError;

public interface VolleyCallback<T> {
    void handleResponse(T result);
    void handleError(VolleyError error);
}
