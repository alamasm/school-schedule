package com.example.pektusin.schoolschedule.Tools.Files;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by pektusin on 9/21/2016.
 */
public class DBIO extends SQLiteOpenHelper {
    public SQLiteDatabase database;
    public static String DATABASE_NAME = "homeWorkDB";

    public DBIO(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + DATABASE_NAME + " (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "date TEXT, subPos Integer, text TEXT, done NUMERIC);");
        database = db;

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
