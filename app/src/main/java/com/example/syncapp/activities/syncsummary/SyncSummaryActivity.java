package com.example.syncapp.activities.syncsummary;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.example.syncapp.R;
import com.example.syncapp.activities.HomeActivity;
import com.example.syncapp.activities.sync.SyncWorker;
import com.example.syncapp.auth.Auth;
import com.example.syncapp.database.DatabaseMethods;
import com.example.syncapp.databinding.ActivitySyncSummaryBinding;
import com.example.syncapp.entity.Transaction;
import com.example.syncapp.util.LogHelper;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SyncSummaryActivity extends AppCompatActivity {

    private static final String TAG = LogHelper.tag(SyncSummaryActivity.class);
    private ActivitySyncSummaryBinding binding;
    private DatabaseMethods databaseMethods;

    private ExecutorService executorService;

    private ArrayList<Transaction> displayingTransactions;

    private TransactionsAdapter transactionsAdapter;

    private Auth auth;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySyncSummaryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        databaseMethods = new DatabaseMethods(this);

        executorService = Executors.newSingleThreadExecutor();
        binding.syncSummaryToolbar.setNavigationOnClickListener(v-> onBackPressed());


        displayingTransactions = new ArrayList<>();
        transactionsAdapter = new TransactionsAdapter(this, displayingTransactions, databaseMethods, auth);

        binding.syncSummaryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.syncSummaryRecyclerView.setAdapter(transactionsAdapter);

        Intent receivedIntent = getIntent();
        boolean startSyncAutomatically = receivedIntent.getBooleanExtra("start_sync", false);

        if(databaseMethods.getTransactionRepo().getSyncableTransactionCount() > 0 && startSyncAutomatically) {
            enQueueSyncWork();
        }


        binding.syncSummaryBtnCancelSync.setOnClickListener(v-> cancelWork());

        WorkManager.getInstance(this).getWorkInfosForUniqueWorkLiveData("sync-work").observe(this, workInfos -> {
            if (workInfos != null && !workInfos.isEmpty()) {
                WorkInfo workInfo = workInfos.get(0);
                WorkInfo.State state = workInfo.getState();
                if (state == WorkInfo.State.SUCCEEDED) {
                    binding.syncSummarySyncState.setText("Sync Completed");
                    binding.syncSummarySyncStateProgress.setVisibility(View.GONE);
                    Data data = workInfo.getOutputData();
                    int totalPendingTransactions = data.getInt("total_pending_transactions", 0);
                    int totalProcessTransactions = data.getInt("total_processed_transactions", 0);
                    int totalFailedTransactions = data.getInt("total_failed_transactions", 0);
                    int totalSuccessfulTransactions = totalProcessTransactions - totalFailedTransactions;

                    binding.syncSummarySyncTotalTransactions.setText("Total Processed Transactions: " + totalPendingTransactions);
                    binding.syncSummarySyncProcessedTransactions.setText("Processed Transactions: " + totalProcessTransactions);
                    binding.syncSummarySyncSuccessTransactions.setText("Successful Transactions: " + totalSuccessfulTransactions);
                    binding.syncSummarySyncFailedTransactions.setText("Failed Transactions: " + totalFailedTransactions);

                    long newPendingTransactionsCount = databaseMethods.getTransactionRepo().getSyncableTransactionCount();
                    if(newPendingTransactionsCount > 0) {
                        binding.syncSummaryBtnSyncAgain.setText(newPendingTransactionsCount + " UnSynced Transactions Found, Sync Now");
                        binding.syncSummaryBtnSyncAgain.setVisibility(View.VISIBLE);
                        binding.syncSummaryBtnSyncAgain.setOnClickListener(v-> enQueueSyncWork());
                    }
                    else {
                        binding.syncSummaryBtnSyncAgain.setVisibility(View.GONE);
                    }
                    binding.syncSummaryBtnCancelSync.setVisibility(View.GONE);
                    binding.mainSyncSummaryList.setVisibility(View.VISIBLE);
                    binding.syncSummaryRadioFailedRetry.performClick();
                }
                else if (state == WorkInfo.State.RUNNING) {
                    binding.syncSummarySyncState.setText("Syncing...");
                    binding.syncSummarySyncStateProgress.setVisibility(View.VISIBLE);
                    Data data = workInfo.getProgress();
                    int totalPendingTransactions = data.getInt("total_pending_transactions", 0);
                    int totalProcessTransactions = data.getInt("total_processed_transactions", 0);
                    int totalFailedTransactions = data.getInt("total_failed_transactions", 0);
                    int totalSuccessfulTransactions = totalProcessTransactions - totalFailedTransactions;

                    binding.syncSummarySyncTotalTransactions.setText("Total Processed Transactions: " + totalPendingTransactions);
                    binding.syncSummarySyncProcessedTransactions.setText("Processed Transactions: " + totalProcessTransactions);
                    binding.syncSummarySyncSuccessTransactions.setText("Successful Transactions: " + totalSuccessfulTransactions);
                    binding.syncSummarySyncFailedTransactions.setText("Failed Transactions: " + totalFailedTransactions);
                    binding.syncSummarySyncStateProgress.setMax(totalPendingTransactions);
                    binding.syncSummarySyncStateProgress.setProgress(totalProcessTransactions);
                    binding.syncSummaryBtnCancelSync.setVisibility(View.VISIBLE);
                    binding.mainSyncSummaryList.setVisibility(View.GONE);
                }
                else if (state == WorkInfo.State.BLOCKED) {
                    binding.syncSummarySyncState.setText("Sync Blocked");
                    binding.syncSummaryBtnCancelSync.setVisibility(View.VISIBLE);
                    binding.mainSyncSummaryList.setVisibility(View.VISIBLE);
                    binding.syncSummaryRadioPending.performClick();
                }
                else if (state == WorkInfo.State.ENQUEUED) {
                    binding.syncSummarySyncState.setText("Sync Enqueued");
                    binding.syncSummaryBtnCancelSync.setVisibility(View.VISIBLE);
                    binding.mainSyncSummaryList.setVisibility(View.VISIBLE);
                    binding.syncSummaryRadioPending.performClick();
                }
                else if (state == WorkInfo.State.CANCELLED) {
                    binding.syncSummaryBtnCancelSync.setVisibility(View.GONE);
                    binding.syncSummarySyncState.setText("Sync Cancelled");
                    binding.mainSyncSummaryList.setVisibility(View.VISIBLE);
                    binding.syncSummaryRadioPending.performClick();
                    long newPendingTransactionsCount = databaseMethods.getTransactionRepo().getSyncableTransactionCount();
                    if(newPendingTransactionsCount > 0) {
                        binding.syncSummaryBtnSyncAgain.setText(newPendingTransactionsCount + " UnSynced Transactions Found, Sync Now");
                        binding.syncSummaryBtnSyncAgain.setVisibility(View.VISIBLE);
                        binding.syncSummaryBtnSyncAgain.setOnClickListener(v-> enQueueSyncWork());
                    }
                    else {
                        binding.syncSummaryBtnSyncAgain.setVisibility(View.GONE);
                    }
                }
                else if (state == WorkInfo.State.FAILED) {
                    binding.syncSummaryBtnCancelSync.setVisibility(View.GONE);
                    binding.syncSummarySyncState.setText("Sync Failed");
                    binding.mainSyncSummaryList.setVisibility(View.VISIBLE);
                    binding.syncSummaryRadioPending.performClick();
                }
            }
        });

        binding.syncSummaryRadioPending.setOnClickListener(v-> {
            getTransactions(Transaction.SyncStatus.Pending, false);
        });

        binding.syncSummaryRadioFailedRetry.setOnClickListener(v-> {
            getTransactions(Transaction.SyncStatus.Failed, true);
        });

        binding.syncSummaryRadioFailed.setOnClickListener(v-> {
            getTransactions(Transaction.SyncStatus.Failed, false);
        });

        binding.syncSummaryRadioSuccess.setOnClickListener(v-> {
            getTransactions(Transaction.SyncStatus.Success, false);
        });

    }

    private void enQueueSyncWork() {
        Constraints constraints = new Constraints.Builder().build();
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(SyncWorker.class).setConstraints(constraints).build();
        WorkManager.getInstance(this).enqueueUniqueWork("sync-work", ExistingWorkPolicy.KEEP, workRequest);
        Log.d(TAG, "scheduleSyncWork: Work Enqueued");
        Toast.makeText(this, "Work Enqueued", Toast.LENGTH_SHORT).show();
        binding.syncSummaryBtnSyncAgain.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        int backStackCount = getSupportFragmentManager().getBackStackEntryCount();
        if (backStackCount == 0) {
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
            finish();
        } else {
            super.onBackPressed();
        }
    }

    private void cancelWork() {
        WorkManager.getInstance(this).cancelUniqueWork("sync-work");
        Log.d(TAG, "cancelWork: Work Cancelled");
        Toast.makeText(this, "Work Cancelled", Toast.LENGTH_SHORT).show();
    }

    private void getTransactions(Transaction.SyncStatus syncStatus, boolean showCanRetry) {
        executorService.execute(() -> {
            runOnUiThread(() -> {
                binding.syncSummaryRecyclerView.setVisibility(View.GONE);
                binding.syncSummaryProgressBar.setVisibility(View.VISIBLE);
            });
            if(syncStatus == Transaction.SyncStatus.Failed && showCanRetry) {
                displayingTransactions = databaseMethods.getTransactionRepo().getFailedAndCanRetryTransactions(syncStatus, showCanRetry);
            }
            else if(syncStatus == Transaction.SyncStatus.Failed) {
                displayingTransactions = databaseMethods.getTransactionRepo().getFailedAndCanRetryTransactions(syncStatus, showCanRetry);
            }
            else {
                displayingTransactions = databaseMethods.getTransactionRepo().getAllTransactions(syncStatus);
            }
            runOnUiThread(() -> {
                transactionsAdapter.setItems(displayingTransactions);
                binding.syncSummaryRecyclerView.setVisibility(View.VISIBLE);
                binding.syncSummaryProgressBar.setVisibility(View.GONE);
            });
        });
    }

}
