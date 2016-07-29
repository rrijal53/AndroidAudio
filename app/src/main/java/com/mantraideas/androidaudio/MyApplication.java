package com.mantraideas.androidaudio;

import android.app.Application;
import android.content.Context;
import android.graphics.Point;
import android.os.Handler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rowsun on 7/8/16.
 */
public class MyApplication extends Application {
    public ArrayList<SongDetail> songsList = new ArrayList<SongDetail>();
    public static Context applicationContext = null;
    public static volatile Handler applicationHandler = null;
    public static Point displaySize = new Point();
    public static float density = 1;
    public static List<SongDetail> audiosList;
    @Override
    public void onCreate() {
        super.onCreate();
        audiosList = new ArrayList<>();
        applicationContext = getApplicationContext();
        applicationHandler =  new Handler(applicationContext.getMainLooper());
    }
}
