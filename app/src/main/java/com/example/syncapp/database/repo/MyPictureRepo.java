package com.example.syncapp.database.repo;

import android.content.Context;
import android.net.Uri;

import com.example.syncapp.database.DatabaseMethods;
import com.example.syncapp.entity.MyPicture;
import com.example.syncapp.util.CryptoSystem;
import com.example.syncapp.util.LogHelper;
import com.j256.ormlite.stmt.QueryBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MyPictureRepo {

    public static final String TAG = LogHelper.tag(MyPictureRepo.class);

    public static final String ENTITY = "MyPicture";

    private final DatabaseMethods databaseMethods;

    public MyPictureRepo(DatabaseMethods databaseMethods) {
        this.databaseMethods = databaseMethods;
    }

    public MyPicture add(Context context, String citizenShipId, Uri fileUri) throws Exception {
        if (citizenShipId == null || citizenShipId.isEmpty()) {
            throw new Exception("Citizenship ID is required");
        }

        if (fileUri == null) {
            throw new Exception("File Uri is required");
        }

        File dir = new File(context.getFilesDir(), "myPictures");
        if(!dir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
        }

        String fileName = citizenShipId + ".jpg";
        File destinationFile = new File(context.getFilesDir(), "myPictures/" + fileName);

        try {
            InputStream inputStream = context.getContentResolver().openInputStream(fileUri);
            if (inputStream == null) {
                throw new Exception("Failed to open the input stream for the file Uri");
            }

            //noinspection IOStreamConstructor
            OutputStream outputStream = new FileOutputStream(destinationFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
            throw new Exception("Failed to copy the picture file: " + e.getMessage());
        }

        MyPicture myPicture = new MyPicture(citizenShipId, fileName);

        // Check if the citizenship ID is unique
        if (existsByCitizenshipId(citizenShipId)) {
            throw new Exception("MyPicture with the same citizenship ID already exists");
        }

        int rowsAffected = databaseMethods.getDatabaseHelper().getMyPictureDao().create(myPicture);
        if (rowsAffected == 1) {
            databaseMethods.getTransactionRepo().addTransaction(ENTITY, "add", citizenShipId, new Date());
            return find(context, citizenShipId);
        } else {
            return null;
        }
    }

    public boolean existsByCitizenshipId(String citizenshipId) {
        try {
            QueryBuilder<MyPicture, String> queryBuilder = databaseMethods.getDatabaseHelper().getMyPictureDao().queryBuilder();
            queryBuilder.where().eq("citizenshipId", citizenshipId);
            return queryBuilder.queryForFirst() != null;
        } catch (SQLException e) {
            throw new RuntimeException("Error checking existence by citizenship ID: " + e.getMessage());
        }
    }

    public MyPicture find(Context context, String citizenshipId) {
        List<MyPicture> myPicture =  databaseMethods.getDatabaseHelper().getMyPictureDao().queryForEq("citizenshipId", citizenshipId);
        if(myPicture == null || myPicture.size() == 0) {
            return  null;
        }
        else {
            myPicture.get(0).setBitmap(context);
            return myPicture.get(0);
        }
    }

    public ArrayList<MyPicture> findAll() {
        try {
            QueryBuilder<MyPicture, String> queryBuilder = databaseMethods.getDatabaseHelper().getMyPictureDao().queryBuilder();
            return (ArrayList<MyPicture>) queryBuilder.query();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all MyPictures: " + e.getMessage());
        }
    }

}
