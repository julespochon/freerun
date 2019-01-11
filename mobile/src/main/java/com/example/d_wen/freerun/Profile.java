package com.example.d_wen.freerun;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataMap;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;

public class Profile implements Serializable {

    protected String username;
    protected String password;
    protected int height;
    protected float weight;
    protected String photoPath;

    public Profile(String username, String password) {
        this.username = username;
        this.password = password;
    }

    DataMap toDataMap() {
        DataMap dataMap = new DataMap();
        dataMap.putString("username", username);
        dataMap.putString("password", password);
        dataMap.putInt("height", height);
        dataMap.putFloat("weight", weight);
        final InputStream imageStream;
        try {
            imageStream = new FileInputStream(photoPath);
            final Bitmap userImage = BitmapFactory.decodeStream(imageStream);
            Asset asset = WearService.createAssetFromBitmap(userImage);
            dataMap.putAsset("photo", asset);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return dataMap;
    }
}