package com.example.syncapp.entity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import com.example.syncapp.util.LogHelper;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;

@DatabaseTable(tableName = "myPictures")
public class MyPicture {

    @DatabaseField
    private String citizenshipId;
    @DatabaseField
    private String fileName;

    private Bitmap bitmap;

    public MyPicture(String citizenshipId, String fileName) {
        this.citizenshipId = citizenshipId;
        this.fileName = fileName;
    }

    public MyPicture(String citizenshipId, String fileName, Bitmap bitmap) {
        this.citizenshipId = citizenshipId;
        this.fileName = fileName;
        this.bitmap = bitmap;
    }

    public String getCitizenshipId() {
        return citizenshipId;
    }

    public void setCitizenshipId(String citizenshipId) {
        this.citizenshipId = citizenshipId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Bitmap getBitmap(Context context) {
        File file = new File(context.getFilesDir(), "myPictures/" + fileName);
        if (file.exists()) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            return BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        } else {
            return null;
        }
    }

    public void setBitmap(Context context) {
        this.bitmap = getBitmap(context);
    }

    public MyPicture() {
    }

    public JSONObject toJSONObject(Context context) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("citizenshipId", this.citizenshipId);
            jsonObject.put("fileName", this.fileName);
            if (this.bitmap != null) {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                this.bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                String encodedBitmap = Base64.encodeToString(byteArray, Base64.DEFAULT);
                jsonObject.put("file", encodedBitmap);
            }
        } catch (JSONException e) {
            Log.e(LogHelper.tag(this), "toJSONObject: " + e.getMessage());
        }
        return jsonObject;
    }
}
