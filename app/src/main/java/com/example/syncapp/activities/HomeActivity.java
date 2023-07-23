package com.example.syncapp.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.example.syncapp.R;
import com.example.syncapp.activities.books.BooksActivity;
import com.example.syncapp.activities.mypictures.MyPicturesActivity;
import com.example.syncapp.activities.syncsummary.SyncSummaryActivity;
import com.example.syncapp.auth.Auth;
import com.example.syncapp.database.DatabaseMethods;
import com.example.syncapp.databinding.ActivityHomeBinding;

import java.io.IOException;
import java.text.MessageFormat;



public class HomeActivity extends AppCompatActivity {



    private ActivityHomeBinding binding;
    private Auth auth;
    private DatabaseMethods databaseMethods;

    private Handler handler;
    private Runnable updateSyncableTransactionCountTask;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



        auth = new Auth(this);
        databaseMethods = new DatabaseMethods(this);

        Drawable overflowIcon = binding.homeToolbar.getOverflowIcon();
        if (overflowIcon != null) {
            Drawable tintedOverflowIcon = DrawableCompat.wrap(overflowIcon);
            DrawableCompat.setTint(tintedOverflowIcon, ContextCompat.getColor(this, R.color.onPrimary));
            binding.homeToolbar.setOverflowIcon(tintedOverflowIcon);
        }

        binding.homeToolbar.setTitle(auth.getName());
        binding.homeToolbar.setSubtitle(auth.getEmail());

        binding.homeToolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.main_menu_item_logout) {
                auth.logout(this);
                return true;
            }
            return false;
        });

        binding.homeBooksButton.setOnClickListener(v -> {
            startActivity(new Intent(this, BooksActivity.class));
        });

        binding.homeMyPicturesButton.setOnClickListener(v -> {
            startActivity(new Intent(this, MyPicturesActivity.class));
        });



        binding.homeSyncReset.setOnClickListener(v -> {
            Runtime runtime = Runtime.getRuntime();
            try {
                runtime.exec("pm clear com.example.syncapp");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        binding.homeSyncButton.setOnClickListener(v -> {
            startActivity(new Intent(this, SyncSummaryActivity.class).putExtra("start_sync", true));
        });

        handler = new Handler();
        updateSyncableTransactionCountTask = new Runnable() {
            @Override
            public void run() {
                int syncableTransactionCount = databaseMethods.getTransactionRepo().getSyncableTransactionCount();
                updateSyncableTransactionCountUI(syncableTransactionCount);
                handler.postDelayed(this, 1000);
            }
        };

        handler.postDelayed(updateSyncableTransactionCountTask, 1000);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] {Manifest.permission.POST_NOTIFICATIONS}, 898);
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateSyncableTransactionCountTask);
    }

    private void updateSyncableTransactionCountUI(int count) {
        binding.homeSyncButton.setText(MessageFormat.format("Sync({0})", count));
    }
}
