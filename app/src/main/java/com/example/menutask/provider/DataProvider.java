package com.example.menutask.provider;

import java.util.HashMap;

public class DataProvider {

    private HashMap<String, String> hashList = new HashMap<>();

    //an empty constructor
    public DataProvider(){}

    //setter
    public void setAttributes(String key, String value) {
        hashList.put(key, value);
    }
    //getter
    public String getAttributes(String key) {
        return hashList.get(key);
    }

}
