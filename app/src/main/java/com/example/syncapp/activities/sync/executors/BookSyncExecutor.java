package com.example.syncapp.activities.sync.executors;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.RequestFuture;
import com.example.syncapp.activities.books.UpdateBookActivity;
import com.example.syncapp.auth.Auth;
import com.example.syncapp.config.Api;
import com.example.syncapp.database.DatabaseMethods;
import com.example.syncapp.entity.Book;
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
public class BookSyncExecutor extends SyncExecutor {

    public static final String TAG = LogHelper.tag(BookSyncExecutor.class);

    public BookSyncExecutor(Context context, DatabaseMethods databaseMethods, Auth auth) {
        super(context, databaseMethods, auth);
    }

    public boolean add(Transaction transaction) {
        Book book = getDatabaseMethods().getBookRepo().find(transaction.getReference());
        JSONObject jsonObject = book.toJSONObject();
        RequestFuture<JSONObject> requestFuture = RequestFuture.newFuture();
        DefaultJsonObjectRequest request = new DefaultJsonObjectRequest(Request.Method.POST, Api.getRootUrl("books/add"), jsonObject, requestFuture, requestFuture);
        request.setAuth(getAuth());
        int timeoutMillis = 1000 * 60;
        request.setRetryPolicy(new DefaultRetryPolicy(timeoutMillis, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueueSingleton.add(getContext(), request);

        JSONObject response;
        try {
            response = requestFuture.get(2, TimeUnit.MINUTES);
            Book addedBook = Book.fromJSONObject(response.getJSONObject("book"));
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

    public boolean update(Transaction transaction) {
        Book book = getDatabaseMethods().getBookRepo().find(transaction.getReference());
        JSONObject jsonObject = book.toJSONObject();

        RequestFuture<JSONObject> requestFuture = RequestFuture.newFuture();
        DefaultJsonObjectRequest request = new DefaultJsonObjectRequest(Request.Method.POST, Api.getRootUrl("books/update"), jsonObject, requestFuture, requestFuture);
        request.setAuth(getAuth());
        int timeoutMillis = 1000 * 60;
        request.setRetryPolicy(new DefaultRetryPolicy(timeoutMillis, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueueSingleton.add(getContext(), request);

        JSONObject response;
        try {
            response = requestFuture.get(2, TimeUnit.MINUTES);
            Book updatedBook = Book.fromJSONObject(response.getJSONObject("book"));
            Log.d(TAG, "update(response): " + response);
            getDatabaseMethods().getTransactionRepo().updateTransactionAsSuccess(transaction.getId());
            return true;
        } catch (InterruptedException | ExecutionException | TimeoutException | JSONException exception) {
            return handleException(transaction, exception);
        }
    }

    public boolean delete(Transaction transaction) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", transaction.getReference());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        RequestFuture<JSONObject> requestFuture = RequestFuture.newFuture();
        DefaultJsonObjectRequest request = new DefaultJsonObjectRequest(Request.Method.DELETE, Api.getRootUrl("books/delete"), jsonObject, requestFuture, requestFuture);
        request.setAuth(getAuth());
        int timeoutMillis = 1000 * 60;
        request.setRetryPolicy(new DefaultRetryPolicy(timeoutMillis, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueueSingleton.add(getContext(), request);

        JSONObject response;
        try {
            response = requestFuture.get(2, TimeUnit.MINUTES);
            Log.d(TAG, "delete(response): " + response);
            getDatabaseMethods().getTransactionRepo().updateTransactionAsSuccess(transaction.getId());
            return true;
        } catch (InterruptedException | ExecutionException | TimeoutException exception) {
            return handleException(transaction, exception);
        }
    }

    public void openReference(Context context, String reference) {
        context.startActivity(new Intent(context, UpdateBookActivity.class).putExtra("id", reference));
    }


    private boolean handleException(Transaction transaction, Exception e) {
        Log.e(TAG, "handleException(transaction -" + transaction.getId() + " is not synced because " + e.getMessage());
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
