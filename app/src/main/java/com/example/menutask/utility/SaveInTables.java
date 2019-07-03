package com.example.menutask.utility;

import android.content.ContentValues;
import android.content.Context;

import com.example.menutask.handlers.DatabaseHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SaveInTables {
    Context context;
    public SaveInTables (Context context){
        this.context=context;
    }
    DatabaseHandler db = new DatabaseHandler(context);
    public long saveInTable(String tableName, HashMap<String, String> insertHashList){
        ContentValues insertValues = new ContentValues();
        if(!insertHashList.isEmpty()){
            for (Map.Entry<String, String> entry : insertHashList.entrySet()){
                insertValues.put(entry.getKey(), entry.getValue() );
            }
        }
        return  db.insertIntoTableorThrow(tableName, null, insertValues );
    }
}
