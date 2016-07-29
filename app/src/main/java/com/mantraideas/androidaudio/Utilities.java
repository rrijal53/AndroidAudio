package com.mantraideas.androidaudio;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.Toast;


/**
 * Created by Pandit Ji on 3/24/2016.
 */
public class Utilities {
    public static final String BASE_URL = "http://mp3.hemshrestha.com.np/api.php";

    public static void log(String string){
        if(BuildConfig.DEBUG)
            Log.i("Utils Log", string);
    }

public static String removeTags(String in)
{
    int index=0;
    int index2=0;
    while(index!=-1)
    {
        index = in.indexOf("<");
        index2 = in.indexOf(">", index);
        if(index!=-1 && index2!=-1)
        {
            in = in.substring(0, index).concat(in.substring(index2+1, in.length()));
        }
    }
    return in;
}

    public static void shareApp(Context context) {
        context.startActivity(Intent.createChooser(getDefaultShareIntent("Share "+context.getString(R.string.app_name),
                "http://play.google.com/store/apps/details?id="
                        + context.getPackageName()), "Share this App"));
    }
    public static String getYoutubeLink(String id,String hq) {
        return "https://img.youtube.com/vi/"+id+"/"+hq+"default.jpg";
    }
    public static void toast(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
    public interface DynamicHeight {
        void HeightChange(int position, int height);
    }
    public static String getVersionName(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {
            return false;
        } else
            return true;
    }

    public static void doRate(Context context) {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri
                    .parse("market://details?id=" + context.getPackageName()))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } catch (ActivityNotFoundException anfe) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri
                    .parse("http://play.google.com/store/apps/details?id="
                            + context.getPackageName()))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }

    }


    public static String getRelativeTime(long timestamp) {
        long nowtime = System.currentTimeMillis();
        if(timestamp < nowtime){
            return (String) DateUtils.getRelativeTimeSpanString(timestamp, nowtime, 0);
        }
        return "Just now";
    }

    public static Intent getDefaultShareIntent(String title, String content) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TITLE, title);
        intent.putExtra(Intent.EXTRA_TEXT, content);
        return Intent.createChooser(intent, title);
    }
}
