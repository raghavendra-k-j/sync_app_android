package com.example.syncapp.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.syncapp.entity.Book;
import com.example.syncapp.entity.Transaction;
import com.example.syncapp.util.LogHelper;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import java.sql.SQLException;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final String TAG  = LogHelper.tag(DatabaseHelper.class);

    public static final String DB_NAME = "sync_app";

    public static final int DB_VERSION = 1;

    private RuntimeExceptionDao<Transaction, Integer> transactionDao;

    private RuntimeExceptionDao<Book, String> bookDao;


    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, Transaction.class);
            TableUtils.createTable(connectionSource, Book.class);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            TableUtils.dropTable(connectionSource, Transaction.class, false);
            TableUtils.dropTable(connectionSource, Book.class, false);
            onCreate(database, connectionSource);
        }
        catch (SQLException e) {
            e.printStackTrace();
            Log.e(TAG, "onUpgrade: " + e.getMessage());
        }
    }

    public RuntimeExceptionDao<Book, String> getBookDao() {
        if(bookDao == null) bookDao = getRuntimeExceptionDao(Book.class);
        return bookDao;
    }

    public RuntimeExceptionDao<Transaction, Integer> getTransactionDao() {
        if(transactionDao == null) transactionDao = getRuntimeExceptionDao(Transaction.class);
        return transactionDao;
    }



    @Override
    public void close() {
        transactionDao = null;
        bookDao = null;
        super.close();
    }
}
