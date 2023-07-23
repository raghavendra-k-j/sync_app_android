package com.example.syncapp.database;

import android.content.Context;

import com.example.syncapp.database.repo.BookRepo;
import com.example.syncapp.database.repo.MyPictureRepo;
import com.example.syncapp.database.repo.TransactionRepo;
import com.example.syncapp.util.LogHelper;
import com.j256.ormlite.android.apptools.OpenHelperManager;

public class DatabaseMethods {

    public static final String TAG = LogHelper.tag(DatabaseHelper.class);
    private final Context context;
    private final DatabaseHelper databaseHelper;
    private final TransactionRepo transactionRepo;
    private final BookRepo bookRepo;
    private final MyPictureRepo myPictureRepo;

    public DatabaseMethods(Context context) {
        this.context = context;
        databaseHelper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
        transactionRepo = new TransactionRepo(this);
        bookRepo = new BookRepo(this);
        myPictureRepo = new MyPictureRepo(this);
    }

    public Context getContext() {
        return context;
    }

    public DatabaseHelper getDatabaseHelper() {
        return databaseHelper;
    }

    public TransactionRepo getTransactionRepo() {
        return transactionRepo;
    }

    public BookRepo getBookRepo() {
        return bookRepo;
    }

    public MyPictureRepo getMyPictureRepo() {
        return myPictureRepo;
    }
}
