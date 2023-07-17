package com.example.syncapp.activities.sync.executors;

import android.content.Context;
import com.example.syncapp.auth.Auth;
import com.example.syncapp.database.DatabaseMethods;
import com.example.syncapp.entity.Transaction;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class SyncExecutor {

    private Context context;
    private DatabaseMethods databaseMethods;
    private Auth auth;

    public SyncExecutor(Context context, DatabaseMethods databaseMethods, Auth auth) {
        this.context = context;
        this.databaseMethods = databaseMethods;
        this.auth = auth;
    }

    private SyncExecutor() {

    }

    public final Context getContext() {
        return context;
    }

    public final DatabaseMethods getDatabaseMethods() {
        return databaseMethods;
    }

    public final Auth getAuth() {
        return auth;
    }


    public static void openReference(Context context, DatabaseMethods databaseMethods, Auth auth,   String entity, String reference) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        Class<?> clazz = Class.forName("com.example.syncapp.activities.sync.executors." + entity + "SyncExecutor");

        Constructor<?> constructor = clazz.getDeclaredConstructor(Context.class, DatabaseMethods.class, Auth.class);
        Object obj = constructor.newInstance(context, databaseMethods, auth);

        Method method = clazz.getMethod("openReference", Context.class, String.class);
        method.invoke(obj, context, reference);
    }
}
