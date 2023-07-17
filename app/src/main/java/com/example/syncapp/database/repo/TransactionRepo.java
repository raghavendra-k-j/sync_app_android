package com.example.syncapp.database.repo;

import android.util.Log;

import com.example.syncapp.database.DatabaseMethods;
import com.example.syncapp.entity.Transaction;
import com.example.syncapp.util.LogHelper;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

@SuppressWarnings("unused")
public class TransactionRepo {

    public static final String TAG = LogHelper.tag(TransactionRepo.class);
    private final DatabaseMethods databaseMethods;

    public TransactionRepo(DatabaseMethods databaseMethods) {
        this.databaseMethods = databaseMethods;
    }

    public Transaction addTransaction(String entity, String action, String reference, Date performedOn) {
        Transaction transaction = Transaction.newTransaction(entity, action, reference, performedOn);
        int rowsAffected = databaseMethods.getDatabaseHelper().getTransactionDao().create(transaction);
        if (rowsAffected == 1) {
            return databaseMethods.getDatabaseHelper().getTransactionDao().queryForId(transaction.getId());
        } else {
            return null;
        }
    }

    public Transaction updateTransactionAsSuccess(int id, String errorMessage) {
        Transaction transaction = findById(id);
        transaction.setSyncStatus(Transaction.SyncStatus.Success.name());
        transaction.setErrorMessage(errorMessage);
        databaseMethods.getDatabaseHelper().getTransactionDao().update(transaction);
        return transaction;
    }

    public Transaction updateTransactionAsSuccess(int id) {
        return updateTransactionAsSuccess(id, null);
    }

    public Transaction updateTransactionAsFailed(int id, boolean canRetry, String errorMessage) {
        Transaction transaction = findById(id);
        transaction.setSyncStatus(Transaction.SyncStatus.Failed.name());
        transaction.setCanRetry(canRetry);
        transaction.setErrorMessage(errorMessage);
        databaseMethods.getDatabaseHelper().getTransactionDao().update(transaction);
        return transaction;
    }

    public Transaction updateTransactionAsFailed(int id, boolean canRetry) {
        return updateTransactionAsFailed(id, canRetry, null);
    }

    public Transaction findById(int id) {
        return databaseMethods.getDatabaseHelper().getTransactionDao().queryForId(id);
    }

    public Transaction mergeTransaction(String entity, String action, String reference, Date performedOn) {
        try {
            QueryBuilder<Transaction, Integer> queryBuilder = databaseMethods.getDatabaseHelper().getTransactionDao().queryBuilder();
//            queryBuilder.where().eq("syncStatus", Transaction.SyncStatus.Pending).and().eq("entity", entity).and().eq("action", action).and().eq("reference", reference);
            Where<Transaction, Integer> where = queryBuilder.where();
            where.or(
                    where.eq("syncStatus", Transaction.SyncStatus.Pending),
                    where.and(
                            where.eq("syncStatus", Transaction.SyncStatus.Failed),
                            where.eq("canRetry", true)
                    )
            );

            Transaction previousTransaction = queryBuilder.queryForFirst();
            if (previousTransaction != null) {
                Log.d(TAG, "mergeTransaction: updating");
                previousTransaction.setPerformedOn(performedOn);
                previousTransaction.setSyncStatus(Transaction.SyncStatus.Pending.name());
                previousTransaction.setErrorMessage(null);
                databaseMethods.getDatabaseHelper().getTransactionDao().update(previousTransaction);
                return previousTransaction;
            } else {
                Log.d(TAG, "mergeTransaction: creating");
                Transaction newTransaction = Transaction.newTransaction(entity, action, reference, performedOn);
                databaseMethods.getDatabaseHelper().getTransactionDao().create(newTransaction);
                return findById(newTransaction.getId());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int getSyncableTransactionCount() {
        try {
            QueryBuilder<Transaction, Integer> queryBuilder = databaseMethods.getDatabaseHelper().getTransactionDao().queryBuilder();
            Where<Transaction, Integer> where = queryBuilder.where();
            where.or(
                where.eq("syncStatus", Transaction.SyncStatus.Pending),
                where.and(
                    where.eq("syncStatus", Transaction.SyncStatus.Failed),
                    where.eq("canRetry", true)
                )
            );
            return (int) queryBuilder.countOf();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ArrayList<Transaction> getSyncableTransactions() {
        try {
            QueryBuilder<Transaction, Integer> queryBuilder = databaseMethods.getDatabaseHelper().getTransactionDao().queryBuilder();
            Where<Transaction, Integer> where = queryBuilder.where();
            where.or(
                    where.eq("syncStatus", Transaction.SyncStatus.Pending),
                    where.and(
                            where.eq("syncStatus", Transaction.SyncStatus.Failed),
                            where.eq("canRetry", true)
                    )
            );
            queryBuilder.orderBy("performedOn", true);

            return new ArrayList<>(queryBuilder.query());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ArrayList<Transaction> getFailedAndCanRetryTransactions(Transaction.SyncStatus syncStatus, boolean showCanRetry) {
        try {
            QueryBuilder<Transaction, Integer> queryBuilder = databaseMethods.getDatabaseHelper().getTransactionDao().queryBuilder();
            Where<Transaction, Integer> where = queryBuilder.where();
            where.and
            (
                    where.eq("syncStatus", syncStatus),
                    where.eq("canRetry", showCanRetry)
            );
            queryBuilder.orderBy("performedOn", true);
            return new ArrayList<>(queryBuilder.query());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ArrayList<Transaction> getAllTransactions(Transaction.SyncStatus syncStatus) {
        try {
            QueryBuilder<Transaction, Integer> queryBuilder = databaseMethods.getDatabaseHelper().getTransactionDao().queryBuilder();
            Where<Transaction, Integer> where = queryBuilder.where();
            where.eq("syncStatus", syncStatus);
            queryBuilder.orderBy("performedOn", true);
            return new ArrayList<>(queryBuilder.query());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


}
