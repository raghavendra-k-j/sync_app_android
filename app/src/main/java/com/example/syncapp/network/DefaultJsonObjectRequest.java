package com.example.syncapp.network;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.syncapp.auth.Auth;
import com.example.syncapp.util.LogHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class DefaultJsonObjectRequest extends JsonObjectRequest {

    private static final String TAG = LogHelper.tag(DefaultJsonObjectRequest.class);

    private final Map<String, String> headers;
    private JSONObject requestBody;

    public DefaultJsonObjectRequest(int method, String url, JSONObject jsonRequest, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener);
        headers = new HashMap<>();
    }

    public static ErrorResponse getErrorResponse(Exception exception) {
        if(exception instanceof VolleyError || exception.getCause() instanceof VolleyError) {
            NetworkResponse networkResponse = null;
            if(exception instanceof VolleyError) {
                networkResponse = ((VolleyError) exception).networkResponse;
            }
            else if(exception.getCause() instanceof VolleyError) {
                networkResponse = ((VolleyError) exception.getCause()).networkResponse;
            }
            if(networkResponse != null && networkResponse.data != null) {
                String errorJson = new String(networkResponse.data);
                Log.e(TAG, "getErrorResponse: networkResponse: " + errorJson);
                try {
                    ErrorResponse errorResponse = new ErrorResponse();
                    JSONObject errorJsonObject = new JSONObject(errorJson).getJSONObject("error");
                    errorResponse.setMessage(errorJsonObject.getString("message"));
                    errorResponse.setCanRetry(errorJsonObject.getBoolean("canRetry"));
                    errorResponse.setUserFriendly(errorJsonObject.getBoolean("isUserFriendly"));
                    return errorResponse;
                }
                catch (JSONException e) {
                    Log.d(TAG, "getErrorResponse: Json exception: " + e.getMessage());
                    return null;
                }
            }
            else {
                Log.d(TAG, "getErrorResponse: no network response found");
                return null;
            }
        }
        else {
            Log.d(TAG, "getErrorResponse: exception is not an instance of volley error");
            return null;
        }
    }

    public static String getNetworkResponseString(Exception e) {
        Throwable cause = e.getCause();
        if (cause instanceof VolleyError) {
            VolleyError volleyError = (VolleyError) cause;
            if(volleyError.networkResponse != null && volleyError.networkResponse.data != null) {
                return new String(volleyError.networkResponse.data);
            }
            else {
                return null;
            }
        }
        else {
            Log.d(TAG, "getNetworkResponse: exception is not an instance of volley error");
            return null;
        }
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    public void setRequestBody(JSONObject requestBody) {
        this.requestBody = requestBody;
    }

    public void addRequestBodyParameter(String key, Object value) {
        if (requestBody == null) {
            requestBody = new JSONObject();
        }
        try {
            requestBody.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setAuth(Auth auth) {
        addHeader("user_id", String.valueOf(auth.getUserId()));
        addHeader("password", auth.getPassword());
    }

    @Override
    public byte[] getBody() {
        if (requestBody != null) {
            return requestBody.toString().getBytes();
        }
        return super.getBody();
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> defaultHeaders = super.getHeaders();
        if (defaultHeaders == null || defaultHeaders.isEmpty()) {
            return headers;
        } else {
            Map<String, String> combinedHeaders = new HashMap<>(defaultHeaders);
            combinedHeaders.putAll(headers);
            return combinedHeaders;
        }
    }

    @SuppressWarnings("unused")
    public void setHeaders(Map<String, String> headers) {
        this.headers.putAll(headers);
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            return Response.success(new JSONObject(jsonString), HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException | JSONException e) {
            return Response.error(new ParseError(e));
        }
    }
}
