package com.example.menutask.handlers;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.content.res.AssetManager.ACCESS_STREAMING;

public class DatabaseHandler extends SQLiteOpenHelper {
    // All Static variables
    private final Context ctx;
    private static final int DATABASE_VERSION = 1;
    private String DB_PATH;
    private static final String DB_NAME = "article.db";
    private SQLiteDatabase myDataBase;

    public DatabaseHandler(Context context) {
        super(context, DB_NAME, null, DATABASE_VERSION);
        this.ctx = context;
        DB_PATH = "/data/data/" + ctx.getPackageName() + "/databases/";

        if (checkDataBase()) {
            openDataBase();
        } else {
            try {
                createDataBase();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void createDataBase() throws IOException {
        boolean dbExist = checkDataBase();
        if (!dbExist) {
            this.getReadableDatabase();
            try {
                copyDataBase();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private boolean checkDataBase() {
        File dbFile = new File(DB_PATH + DB_NAME);
        return dbFile.exists();
    }

    private void copyDataBase() throws IOException {
        AssetManager am = ctx.getAssets();
        InputStream myInput = am.open(DB_NAME, ACCESS_STREAMING);
        String outFileName = DB_PATH + DB_NAME;
        OutputStream myOutput = new FileOutputStream(outFileName);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }
        myOutput.flush();
        myOutput.close();
        myInput.close();
    }

    public boolean openDataBase() throws SQLException {
        String mPath = DB_PATH + DB_NAME;
        myDataBase = SQLiteDatabase.openDatabase(mPath, null, SQLiteDatabase.CREATE_IF_NECESSARY);
        return myDataBase != null;

    }

    @Override
    public synchronized void close() {
        if (myDataBase != null)
            myDataBase.close();
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /**
     * Called everytime the database is opened by getReadableDatabase or
     * getWritableDatabase. This is called after onCreate or onUpgrade is
     * called.
     */
    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }


    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    /** Functions for inserting values */

    public long insertIntoTableorThrow(String table, String nullColumnHack, ContentValues values) {
        long id = 0;
        try {
            id = myDataBase.insertOrThrow(table, null, values);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return id;
    }

    public long insertIntoTableOnConflict(String table, String nullColumnHack, ContentValues values) {
        return myDataBase.insertWithOnConflict(table, nullColumnHack, values, SQLiteDatabase.CONFLICT_IGNORE);
    }


    /** Delete rows from table */
    public int deleteFromTable(String tableName, String where, String[] whereArgs) {
        return myDataBase.delete(tableName, where, whereArgs);
    }

    /** Update table */
    int updateTable(String table, ContentValues values, String where, String[] whereArgs) {
        return myDataBase.update(table, values, where, whereArgs);
    }

    @NonNull
    public HashMap<String, List<String>> getMultipleValues(Boolean keyword, String[] table, @NonNull String[] aColumn, @Nullable String joinedby, String where,
                                                           String[] whereArgs, String groupBy, String having, String orderBy) {
        HashMap<String, List<String>> list = new HashMap<>();
        String joinedtables;
        int nmbField = aColumn.length;
        ArrayList<String>[] group = (ArrayList<String>[]) new ArrayList[nmbField];
        for (int i = 0; i < nmbField; i++) {
            group[i] = new ArrayList<>();
        }
        if (joinedby != null) {
            joinedtables = joinedby;

        } else {
            joinedtables = table[0];
        }
        Cursor cursor = myDataBase.query(keyword, joinedtables, aColumn, where, whereArgs, groupBy, having, orderBy, null);
        if (cursor.moveToFirst()) {
            do {
                for (int i = 0; i < nmbField; i++) {
                    group[i].add(cursor.getString(i));
                }
            }
            while (cursor.moveToNext());
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        for (int i = 0; i < nmbField; i++) {
            list.put(aColumn[i], group[i]);
        }
        return list;
    }


}
