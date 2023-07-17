package com.example.syncapp.auth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.example.syncapp.activities.LoginActivity;
import com.example.syncapp.database.DatabaseMethods;

import org.json.JSONException;
import org.json.JSONObject;

public class Auth {
    private final SharedPreferences preferences;

    public Auth(Context context) {
        preferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
    }

    public boolean isLoggedIn() {
        int userId = getUserId();
        String email = getEmail();
        String name = getName();
        String password = getPassword();
        return userId != 0 && email != null && !email.isEmpty() && name != null && !name.isEmpty() && password != null && !password.isEmpty();
    }

    @SuppressLint("ApplySharedPref")
    public void login(JSONObject userObj) throws JSONException {
        int userId = userObj.getInt("id");
        String email = userObj.getString("email");
        String name = userObj.getString("name");
        String password = userObj.getString("password");

        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("user_id", userId);
        editor.putString("email", email);
        editor.putString("name", name);
        editor.putString("password", password);
        editor.apply();
    }

    @SuppressLint("ApplySharedPref")
    public void logout() {
        preferences.edit().clear().apply();
    }

    public void logout(Activity activity) {
        logout();
        Intent intent = new Intent(activity, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finishAffinity();
    }

    public int getUserId() {
        return preferences.getInt("user_id", 0);
    }

    public String getName() {
        return preferences.getString("name", null);
    }

    public String getEmail() {
        return preferences.getString("email", null);
    }

    public String getPassword() {
        return preferences.getString("password", null);
    }
}
