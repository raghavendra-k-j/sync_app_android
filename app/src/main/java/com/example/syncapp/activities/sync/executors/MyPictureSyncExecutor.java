package com.example.syncapp.activities.sync.executors;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.RequestFuture;
import com.example.syncapp.activities.books.UpdateBookActivity;
import com.example.syncapp.activities.mypictures.MyPicturesActivity;
import com.example.syncapp.auth.Auth;
import com.example.syncapp.config.Api;
import com.example.syncapp.database.DatabaseMethods;
import com.example.syncapp.entity.Book;
import com.example.syncapp.entity.MyPicture;
import com.example.syncapp.entity.Transaction;
import com.example.syncapp.network.DefaultJsonObjectRequest;
import com.example.syncapp.network.ErrorResponse;
import com.example.syncapp.network.RequestQueueSingleton;
import com.example.syncapp.util.LogHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@SuppressWarnings("unused")
public class MyPictureSyncExecutor extends SyncExecutor {

    public static final String TAG = LogHelper.tag(MyPictureSyncExecutor.class);

    public MyPictureSyncExecutor(Context context, DatabaseMethods databaseMethods, Auth auth) {
        super(context, databaseMethods, auth);
    }

    public boolean add(Transaction transaction) {
        MyPicture myPicture = getDatabaseMethods().getMyPictureRepo().find(getContext(), transaction.getReference());
        JSONObject jsonObject = myPicture.toJSONObject(getContext());
        RequestFuture<JSONObject> requestFuture = RequestFuture.newFuture();
        DefaultJsonObjectRequest request = new DefaultJsonObjectRequest(Request.Method.POST, Api.getRootUrl("sync/file"), jsonObject, requestFuture, requestFuture);
        request.setAuth(getAuth());
        int timeoutMillis = 1000 * 60;
        request.setRetryPolicy(new DefaultRetryPolicy(timeoutMillis, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueueSingleton.add(getContext(), request);

        JSONObject response;
        try {
            response = requestFuture.get(2, TimeUnit.MINUTES);
            Log.d(TAG, "add(response): " + response);
            String errorMessage = null;
            if(response.has("errorMessage")) errorMessage = response.getString("errorMessage");
            getDatabaseMethods().getTransactionRepo().updateTransactionAsSuccess(transaction.getId(), errorMessage);
            return true;
        }
        catch (InterruptedException | ExecutionException | TimeoutException | JSONException exception) {
            return handleException(transaction, exception);
        }
    }

    public void openReference(Context context, String reference) {
        context.startActivity(new Intent(context, MyPicturesActivity.class));
    }
    private boolean handleException(Transaction transaction, Exception e) {
        Log.e(TAG, "handleException(transaction :" + transaction.getId() + " is not synced because " + e.getMessage());
        Log.e(TAG, "handleException(network response): " + DefaultJsonObjectRequest.getNetworkResponseString(e));
        ErrorResponse errorResponse = DefaultJsonObjectRequest.getErrorResponse(e);
        if(errorResponse != null) {
            getDatabaseMethods().getTransactionRepo().updateTransactionAsFailed(transaction.getId(), errorResponse.isCanRetry(), errorResponse.getMessage());
        }
        else {
            getDatabaseMethods().getTransactionRepo().updateTransactionAsFailed(transaction.getId(), true,  "Something went wrong");
        }
        return false;
    }
}
