package com.example.menutask.utility;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class ParseJson {
    @NonNull
    public  static HashMap<Integer, ArrayList<String>> parseJsonToHash(String json, @NonNull String[] keys) {
        HashMap<Integer, ArrayList<String>> hashList=new HashMap<>();
        try {
            JSONObject obj = new JSONObject(json);
            JSONArray jsonarray =obj.getJSONArray("result");
            for (int i = 0; i < jsonarray.length(); i++) {
                ArrayList<String> arrayList = new ArrayList<>();
                JSONObject jsonobject = jsonarray.getJSONObject(i);
                for (String key: keys){
                    String value = jsonobject.getString(key);
                    arrayList.add(value);
                }
                hashList.put(i, arrayList);
            }
        }
        catch(JSONException e){
            e.printStackTrace();
        }
        return hashList;
    }
}
