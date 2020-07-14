package com.ecotioco.gaze.connection;

import android.content.Context;
import android.net.Uri;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ecotioco.gaze.data.Constant;
import com.ecotioco.gaze.model.DeviceInfo;
import com.ecotioco.gaze.model.Order;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class GazeAPI {
    private static GazeAPI sInstance;
    private VolleyController controller;

    private GazeAPI(Context context) {
        controller = VolleyController.getInstance(context);
    }

    public static synchronized GazeAPI getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new GazeAPI(context);
        }
        return sInstance;
    }

    public void cancelRequest(Object tag) {
        controller.getRequestQueue().cancelAll(tag);
    }

    public static <T> T get(JSONObject object, Class<T> type) {
        Gson gson = new Gson();
        return gson.fromJson(object.toString(), type);
    }

    public static <T> T get(JSONArray array, Class<T> type) {
        Gson gson = new Gson();
        return gson.fromJson(array.toString(), type);
    }

    private HashMap<String, String> prepareHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Cache-Control", "max-age=0");
        headers.put("User-Agent", "Gaze");
        headers.put("Security", Constant.SECURITY_CODE);
        return headers;
    }

    private <T> Response.Listener<T> getResponseListener(final VolleyCallback<T> callback) {
        return new Response.Listener<T>() {
            @Override
            public void onResponse(T response) {
                callback.handleResponse(response);
            }
        };
    }

    private <T> Response.ErrorListener getResponseErrorListener(final VolleyCallback<T> callback) {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callback.handleError(error);
            }
        };
    }

    /* Recipe API transaction ------------------------------- */
    // services/info
    public JsonObjectRequest getInfo(int version, final VolleyCallback<JSONObject> callback) {
        String url = "https://jsonplaceholder.typicode.com/todos/1";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url,
                null, getResponseListener(callback), getResponseErrorListener(callback)) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return prepareHeaders();
            }
        };

        controller.addToRequestQueue(request);
        return request;
    }

    /* Fcm API ----------------------------------------------------------- */
    // services/insertOneFcm
    public JsonObjectRequest registerDevice(DeviceInfo deviceInfo, VolleyCallback<JSONObject> callback) {
        JSONObject requestObj = new JSONObject();
        JsonObjectRequest request =
                new JsonObjectRequest(Request.Method.POST, "services/insertOneFcm",
                        requestObj, getResponseListener(callback), getResponseErrorListener(callback)) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        return prepareHeaders();
                    }
                };

        controller.addToRequestQueue(request);
        return request;
    }

    /* News Info API ---------------------------------------------------- */
    // services/listFeaturedNews
    public JsonArrayRequest getFeaturedNews(VolleyCallback<JSONArray> callback) {
        JsonArrayRequest request =
                new JsonArrayRequest(Request.Method.POST, "services/listFeatureNews",
                        null, getResponseListener(callback), getResponseErrorListener(callback)) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        return prepareHeaders();
                    }
                };

        controller.addToRequestQueue(request);
        return request;
    }

    // services/listNews
    public JsonObjectRequest getListNewsInfo(final int page, final int count, String query, VolleyCallback<JSONObject> callback) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, "service/listNews?" + query,
                null, getResponseListener(callback), getResponseErrorListener(callback)) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return prepareHeaders();
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = super.getParams();
                params.put("page", Uri.encode(page + ""));
                params.put("count", Uri.encode(count + ""));
                return params;
            }
        };

        controller.addToRequestQueue(request);
        return request;
    }

    // services/getNewsDetails
    public JsonObjectRequest getNewsDetails(long id, VolleyCallback<JSONObject> callback) {
        String url = String.format("services/getNewDetails?id=%d", id);
        JsonObjectRequest request =
                new JsonObjectRequest(Request.Method.GET, url,
                        null, getResponseListener(callback), getResponseErrorListener(callback)) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        return prepareHeaders();
                    }
                };

        controller.addToRequestQueue(request);
        return request;
    }

    /* Category API ---------------------------------------------------  */
    // services/listCategory
    public JsonArrayRequest getListCategory(VolleyCallback<JSONArray> callback) {
        JsonArrayRequest request = new JsonArrayRequest("", getResponseListener(callback), getResponseErrorListener(callback));
        return request;
    }

    /* Product API ---------------------------------------------------- */
    // services/listProduct
    public JsonObjectRequest getListProduct(int page, int count, String query, long category_id, VolleyCallback<JSONObject> callback) {
        JsonObjectRequest request = new JsonObjectRequest("", null, getResponseListener(callback), getResponseErrorListener(callback));
        return request;
    }

    // services/getProductDetails
    public JsonObjectRequest getProductDetails(long id, VolleyCallback<JSONObject> callback) {
        JsonObjectRequest request = new JsonObjectRequest("", null, getResponseListener(callback), getResponseErrorListener(callback));
        return request;
    }

    /* Checkout API ---------------------------------------------------- */
    public JsonObjectRequest submitOrder(Order order, VolleyCallback<JSONObject> callback) {
        JsonObjectRequest request = new JsonObjectRequest("", null, getResponseListener(callback), getResponseErrorListener(callback));

        controller.addToRequestQueue(request);
        return request;
    }

}
