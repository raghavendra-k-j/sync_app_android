package com.example.syncapp.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.syncapp.auth.Auth;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    Auth auth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = new Auth(this);

        Intent intent;
        if(!auth.isLoggedIn()) {
            intent = new Intent(this, LoginActivity.class);
        }
        else {
            intent = new Intent(this, HomeActivity.class);
        }
        startActivity(intent);
        finish();
    }
}
