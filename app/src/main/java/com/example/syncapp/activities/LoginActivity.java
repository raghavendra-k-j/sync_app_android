package com.example.syncapp.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.example.syncapp.auth.Auth;
import com.example.syncapp.config.Api;
import com.example.syncapp.databinding.ActivityLoginBinding;
import com.example.syncapp.network.DefaultJsonObjectRequest;
import com.example.syncapp.network.ErrorResponse;
import com.example.syncapp.network.RequestQueueSingleton;
import com.example.syncapp.util.LogHelper;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LogHelper.tag(LoginActivity.class);
    private ActivityLoginBinding binding;
    private Auth auth;
    private Snackbar snackbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        auth = new Auth(this);
        binding.loginButtonLogin.setOnClickListener(v -> proceedLogin());
    }

    private void proceedLogin() {
        @SuppressWarnings("ConstantConditions") String id = binding.loginEditTextUserId.getText().toString().trim();
        @SuppressWarnings("ConstantConditions") String password = binding.loginEditTextPassword.getText().toString().trim();

        if (id.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please enter a valid user id.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please enter a valid password.", Toast.LENGTH_SHORT).show();
            return;
        }

        @SuppressLint("ApplySharedPref") DefaultJsonObjectRequest request = new DefaultJsonObjectRequest(Request.Method.POST, Api.getRootUrl("auth/login"), null, response -> {
            Log.d(TAG, "proceedLogin: Response Listener: " + response);
            try {
                auth.login(response.getJSONObject("user"));
            } catch (JSONException e) {
                auth.logout();
                onLoginError(e);
                return;
            }
            Toast.makeText(getApplicationContext(), "Login Success", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
            startActivity(intent);
            finish();
        }, this::onLoginError);
        request.addRequestBodyParameter("id", id);
        request.addRequestBodyParameter("password", password);
        request.setShouldCache(false);
        RequestQueueSingleton.getInstance(this).add(request);
    }

    private void onLoginError(Exception exception) {
        Log.e(TAG, "onLoginError: " + exception.getMessage(), exception);
        ErrorResponse errorResponse = DefaultJsonObjectRequest.getErrorResponse(exception);
        String errorMessage;
        if (errorResponse == null) {
            errorMessage = "Something went wrong!";
        } else {
            errorMessage = errorResponse.getMessage();
        }
        snackbar = Snackbar.make(findViewById(android.R.id.content), errorMessage, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("OK", v -> snackbar.dismiss());
        snackbar.show();
    }
}
