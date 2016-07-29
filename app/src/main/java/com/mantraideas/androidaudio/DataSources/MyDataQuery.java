package com.mantraideas.androidaudio.DataSources;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by rowsun on 6/17/16.
 */
public class MyDataQuery {
    Context context;
    OnDataReceived action;

    public MyDataQuery(Context context) {
        this.context = context;
    }

    public MyDataQuery(Context context, OnDataReceived action) {
        this.context = context;
        this.action = action;
    }

    public void getRequestData(String table) {
        getRequestData(table, 0);
    }

    public void getRequestData(String table, int skip) {
        getRequestData(table, skip, "");
    }

    public void getRequestData(String table, int skip, String type) {
        if (ServerRequest.isNetworkConnected(context)) {
            getRequestData(table, getRequestParameters(table, skip, type));
        }
    }

    public void getRequestData(final String table, final HashMap<String, String> params) {
        if (!ServerRequest.isNetworkConnected(context)) {
            return;
        }
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                return new ServerRequest(context).httpPostData(params);
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                if (action != null) {
                    action.onSuccess(table, result);
                }
            }
        }.execute();
    }


    public HashMap<String, String> getRequestParameters(String table, int skip, String type) {
        HashMap<String, String> params = new HashMap<>();
        params.put("action", "get_hindi_bhajan_audios");
        params.put("type", type);
        params.put("start", skip + "");
        params.put("limit", "20");
        return params;
    }
}
