package com.mantraideas.androidaudio;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rowsun on 6/17/16.
 */
public class Audios {
    public String id, name, path, type, views_count, comments_count;
    public boolean isPlaying;
    public boolean fav = false;

    Audios(JSONObject jObj) {
        id = jObj.optString("id");
        name = jObj.optString("name");
        type = jObj.optString("type");
        path = jObj.optString("path");
        fav = jObj.optString("favorite").equals("1");
        views_count = jObj.optString("views_count");
        comments_count = jObj.optString("comments_count");

    }

    public static List<Audios> audioListFromJSON(String json) {

        try {
            JSONObject jObj = new JSONObject(json);
            JSONArray jArr = jObj.getJSONArray("data");
            return audioListFromJSON(jArr);
        } catch (Exception e) {
        }
        return new ArrayList<>();
    }

    public static List<Audios> audioListFromJSON(JSONArray jArr) {
        List<Audios> list = new ArrayList<>();
        for (int i = 0; i < jArr.length(); i++) {
            list.add(new Audios(jArr.optJSONObject(i)));
        }
        return list;
    }
}
