package com.example.syncapp.activities.syncsummary;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.syncapp.activities.sync.executors.SyncExecutor;
import com.example.syncapp.auth.Auth;
import com.example.syncapp.database.DatabaseMethods;
import com.example.syncapp.databinding.ListItemTransactionBinding;
import com.example.syncapp.entity.Transaction;
import com.example.syncapp.util.LogHelper;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class TransactionsAdapter extends RecyclerView.Adapter<TransactionsAdapter.TransactionsViewHolder> {

    public static final String TAG = LogHelper.tag(TransactionsAdapter.class);
    private final Context context;
    private final LayoutInflater inflater;
    private ArrayList<Transaction> transactions;

    private final DatabaseMethods databaseMethods;

    private final Auth auth;

    public TransactionsAdapter(Context context, ArrayList<Transaction> transactions, DatabaseMethods databaseMethods, Auth auth) {
        this.context = context;
        this.transactions = transactions;
        inflater = LayoutInflater.from(context);
        this.databaseMethods = databaseMethods;
        this.auth = auth;
    }

    @NonNull
    @Override
    public TransactionsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TransactionsViewHolder(ListItemTransactionBinding.inflate(inflater, parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull TransactionsViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);
        holder.binding.listItemTransactionTextAction.setText(transaction.getEntity() + " " + transaction.getAction() + " " + transaction.getReference());
        holder.binding.listItemTransactionTextPerformedOn.setText(transaction.getPerformedOn().toString());
        holder.binding.listItemTransactionTextStatus.setText(transaction.getSyncStatus());
        if (transaction.getSyncStatus().equals(Transaction.SyncStatus.Success.name())) {
            holder.binding.listItemTransactionTextStatus.setTextColor(Color.parseColor("#006400"));
        } else {
            holder.binding.listItemTransactionTextStatus.setTextColor(Color.parseColor("#FF8C00"));
        }
        holder.binding.listItemTransactionTextFailedReason.setText(transaction.getErrorMessage());
        holder.binding.getRoot().setOnClickListener(v-> {
            try {
                SyncExecutor.openReference(context, databaseMethods, auth, transaction.getEntity(), transaction.getReference());
            }
            catch (Exception e) {
                Log.d(TAG, "onBindViewHolder: ");
            }
        });
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setItems(ArrayList<Transaction> pendingTransactions) {
        transactions.clear();
        transactions.addAll(pendingTransactions);
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateItem(Transaction transaction) {
        int index = transactions.indexOf(transaction);
        if (index != -1) {
            transactions.set(index, transaction);
            notifyItemChanged(index);
        }
    }

    static class TransactionsViewHolder extends RecyclerView.ViewHolder {
        ListItemTransactionBinding binding;
        public TransactionsViewHolder(ListItemTransactionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
