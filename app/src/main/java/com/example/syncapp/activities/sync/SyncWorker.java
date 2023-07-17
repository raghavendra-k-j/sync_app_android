package com.example.syncapp.activities.sync;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.syncapp.R;
import com.example.syncapp.activities.syncsummary.SyncSummaryActivity;
import com.example.syncapp.auth.Auth;
import com.example.syncapp.database.DatabaseMethods;
import com.example.syncapp.entity.Transaction;
import com.example.syncapp.util.LogHelper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class SyncWorker extends Worker {

    public final String TAG = LogHelper.tag(SyncWorker.class);
    private final int notificationId;
    private final String NOTIFICATION_CHANNEL_ID = "sync_channel";
    private final DatabaseMethods databaseMethods;
    private final Context context;
    private ArrayList<Transaction> pendingTransactions;
    private int totalPendingTransactions;
    private int totalProcessedTransactions;
    private int totalFailedTransactions;
    private final Auth auth;
    private final NotificationManager notificationManager;

    private Intent notificationIntent;

    private PendingIntent notificationPendingIntent;

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
        this.databaseMethods = new DatabaseMethods(context);
        auth = new Auth(context);
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationId = String.valueOf(System.currentTimeMillis()).hashCode();
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "doWork: Started");

        notificationIntent = new Intent(context, SyncSummaryActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        notificationPendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        showNotification();

        pendingTransactions = databaseMethods.getTransactionRepo().getSyncableTransactions();
        totalPendingTransactions = pendingTransactions.size();

        for (int i = 0; i < totalPendingTransactions; i++) {
            Transaction transaction = pendingTransactions.get(i);
            Log.d(TAG, "doWork(loop): Transaction: " + transaction.getId());
            String entity = transaction.getEntity();
            String action = transaction.getAction();
            try {
                Class<?> clazz = Class.forName("com.example.syncapp.activities.sync.executors." + entity + "SyncExecutor");

                Constructor<?> constructor = clazz.getDeclaredConstructor(Context.class, DatabaseMethods.class, Auth.class);
                Object obj = constructor.newInstance(context, databaseMethods, auth);

                Method method = clazz.getMethod(action, Transaction.class);
                @SuppressWarnings("ConstantConditions")
                boolean success = (boolean) method.invoke(obj, transaction);
                if(success) {
                    Log.d(TAG, "doWork(success): " + transaction.getId() + ":" + true);
                }
                else {
                    throw new Exception("Failed to process the transaction");
                }
            }
            catch (Exception e) {
                Log.e(TAG, "doWork(catch): " , e);
                totalFailedTransactions++;
            }
            totalProcessedTransactions++;
            updateNotification();
            Data data = new Data.Builder()
                    .putInt("total_processed_transactions", totalProcessedTransactions)
                    .putInt("total_pending_transactions", totalPendingTransactions)
                    .putInt("total_failed_transactions", totalFailedTransactions)
                    .build();
            setProgressAsync(data);
        }

        Data data = new Data.Builder()
                .putInt("total_processed_transactions", totalProcessedTransactions)
                .putInt("total_pending_transactions", totalPendingTransactions)
                .putInt("total_failed_transactions", totalFailedTransactions)
                .build();
        Log.d(TAG, "doWork: Work Ended");
        return showNotificationAsCompleted(data);
    }

    private void showNotification() {
        createNotificationChannel();

        // Set the click action for the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Syncing")
                .setContentText("Sync in progress")
                .setSmallIcon(R.drawable.ic_upload_on_surface)
                .setContentIntent(notificationPendingIntent)
                .setAutoCancel(true)
                .setProgress(totalPendingTransactions, 0, true);

        Notification notification = builder.build();
        notificationManager.notify(notificationId, notification);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String notificationChannelName = "Sync";
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, notificationChannelName, NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void updateNotification() {
        String contentText =  totalProcessedTransactions + "/" + totalPendingTransactions + " completed";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Syncing")
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_upload_on_surface)
                .setContentIntent(notificationPendingIntent)
                .setAutoCancel(true)
                .setProgress(totalPendingTransactions, totalProcessedTransactions, false);

        Notification notification = builder.build();
        notificationManager.notify(notificationId, notification);
    }

    private Result showNotificationAsCompleted(Data data)  {
        notificationManager.cancel(notificationId);
        String completedCount = String.valueOf((totalPendingTransactions - totalFailedTransactions));
        String failedCount = String.valueOf(totalFailedTransactions);
        String totalPendingCount = String.valueOf(totalPendingTransactions);

        // Add leading zero if count is a single digit
        if (completedCount.length() == 1) {
            completedCount = "0" + completedCount;
        }
        if (failedCount.length() == 1) {
            failedCount = "0" + failedCount;
        }
        if (totalPendingCount.length() == 1) {
            totalPendingCount = "0" + totalPendingCount;
        }

        String totalTransactionsText = "Total transactions: " + totalPendingCount;
        String completedTransactionsText = "Completed transactions: " + completedCount;
        String failedTransactionsText = "Failed transactions: " + failedCount;

        SpannableString contentText = new SpannableString(totalTransactionsText + "\n" +
                completedTransactionsText + "\n" +
                failedTransactionsText);

        // Set total transactions text color to blue
        ForegroundColorSpan totalTransactionsSpan = new ForegroundColorSpan(Color.BLUE);
        contentText.setSpan(totalTransactionsSpan, 0, totalTransactionsText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Set completed transactions text color to dark green
        ForegroundColorSpan completedSpan = new ForegroundColorSpan(Color.parseColor("#008000")); // Dark green color
        int completedStartIndex = contentText.toString().indexOf(completedTransactionsText);
        int completedEndIndex = completedStartIndex + completedTransactionsText.length();
        contentText.setSpan(completedSpan, completedStartIndex, completedEndIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Set failed transactions text color to dark red
        ForegroundColorSpan failedSpan = new ForegroundColorSpan(Color.parseColor("#8B0000")); // Dark red color
        int failedStartIndex = contentText.toString().indexOf(failedTransactionsText);
        int failedEndIndex = failedStartIndex + failedTransactionsText.length();
        contentText.setSpan(failedSpan, failedStartIndex, failedEndIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Sync Complete")
                .setContentText(contentText)
                .setContentIntent(notificationPendingIntent)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_upload_on_surface);

        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle().bigText(contentText);

        builder.setStyle(bigTextStyle);


        Notification notification = builder.build();
        notificationManager.notify(notificationId, notification);
        Log.d(TAG, "showNotificationAsCompleted: " + notificationId + " : " + contentText);
        return Result.success(data);
    }

    private void showCancelledNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Sync Cancelled")
                .setContentText("Sync operation has been cancelled")
                .setContentIntent(notificationPendingIntent)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_upload_on_surface);

        Notification notification = builder.build();
        notificationManager.notify(notificationId, notification);
    }

    @Override
    public void onStopped() {
        Log.d(TAG, "onStopped: ");
        showCancelledNotification();
        super.onStopped();
    }
}