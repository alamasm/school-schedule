package com.example.pektusin.schoolschedule.Tools.Files;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.example.pektusin.schoolschedule.MainActivity;
import com.example.pektusin.schoolschedule.Tools.Files.DBIO;
import com.example.pektusin.schoolschedule.Tools.HomeWork;
import com.example.pektusin.schoolschedule.Tools.MyDate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by pektusin on 9/15/2016.
 */
public class FileIO {

    public static String FILENAME_SCHEDULE = "Schedule";
    public static String FILENAME_HOMEWORK = "HomeWork";
    public static String FILENAME_FIRST_START = "firstStart";

    public static DBIO dbio;


    public static boolean writeFile(String text, Activity activity, String filename) {
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(activity.openFileOutput(
                    filename, Context.MODE_PRIVATE)));

            bw.write(text);
            bw.close();
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException ex) {
            return false;
        }
        return true;
    }

    public static String readFile(Activity activity, String filename) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    activity.openFileInput(filename)));

            StringBuffer stringBuffer = new StringBuffer();

            String str = "";

            /*
            while ((str += br.readLine()) != null) {
            }
            */
            str = br.readLine();

            //str = stringBuffer.toString();

            br.close();
            return str;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static HashMap<Integer, String>[] getSchedule(Activity activity) {
        HashMap<Integer, String> result[] = new HashMap[5];

        JSONObject jsonObject;
        JSONArray days;

        JSONObject day[] = new JSONObject[5];
        JSONArray subjects[] = new JSONArray[5];
        JSONObject subject[][] = new JSONObject[5][8];
        try {
            jsonObject = new JSONObject(readFile(activity, FILENAME_SCHEDULE));
            days = jsonObject.getJSONArray("days");

            for (int i = 0; i < day.length; i++) {
                day[i] = days.getJSONObject(i);
                subjects[i] = day[i].getJSONArray("subjects");

                result[i] = new HashMap<>();

                for (int j = 0; j < subjects[i].length(); j++) {
                    subject[i][j] = subjects[i].getJSONObject(j);
                    result[i].put(j, subject[i][j].getString("text"));
                }
            }

        } catch (JSONException jsEx) {
            jsEx.printStackTrace();
        }

        return result;
    }

    public static HashMap<MyDate, HashMap<Integer, HomeWork>> getHomeWork(Activity activity) {
        dbio = new DBIO(activity, DBIO.DATABASE_NAME, null, 1);
        dbio.database = dbio.getReadableDatabase();

        HashMap<MyDate, HashMap<Integer, HomeWork>> homeWorkHashMap = new HashMap<>();
        Cursor cursor = dbio.database.rawQuery("SELECT * FROM " + DBIO.DATABASE_NAME, null);
        if (cursor.moveToFirst()) {
            do {
                MyDate date;
                HashMap<Integer, HomeWork> homeWorks = new HashMap<>();

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
                simpleDateFormat.applyPattern("dd.MM.yyyy");
                try {
                    date = new MyDate();
                    date.setTime(simpleDateFormat.parse(cursor.getString(cursor.getColumnIndex("date"))).getTime());

                    int subPos = cursor.getInt(cursor.getColumnIndex("subPos"));
                    String text = cursor.getString(cursor.getColumnIndex("text"));

                    HomeWork homeWork = new HomeWork(text);

                    if (cursor.getInt(cursor.getColumnIndex("done")) == 1)
                        homeWork.markDone();

                    if (homeWorkHashMap.containsKey(date)) {
                        homeWorkHashMap.get(date).put(subPos, homeWork);
                    } else {
                        ((MainActivity) activity).homeWorkDates.add(date);
                        homeWorks.put(subPos, homeWork);
                        homeWorkHashMap.put(date, homeWorks);
                        homeWorks = new HashMap<>();
                    }
                } catch (ParseException e) {
                }

            } while (cursor.moveToNext());
        }

        return homeWorkHashMap;

    }


    public static boolean writeHomeWork(HashMap<MyDate, HashMap<Integer, HomeWork>> homeWork,
                                        ArrayList<MyDate> homeWorkDates, Activity activity, int datesCount) {
        dbio = new DBIO(activity, DBIO.DATABASE_NAME, null, 1);
        dbio.database = dbio.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        for (MyDate date : homeWorkDates) {
            if (homeWork.containsKey(date)) {
                for (int i = 0; i < 8; i++) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);
                    if (homeWork.get(date).containsKey(i)) {

                        contentValues.put("date", date.toString());
                        contentValues.put("subPos", i);
                        contentValues.put("text", homeWork.get(date).get(i).getText());
                    }
                }
            }
        }

        dbio.database.insert(DBIO.DATABASE_NAME, null, contentValues);
        return true;
    }

    public static boolean removeHomeWork(MyDate date, int position, Activity activity) {
        dbio = new DBIO(activity, DBIO.DATABASE_NAME, null, 1);
        dbio.database = dbio.getWritableDatabase();
        dbio.database.delete(DBIO.DATABASE_NAME, "date LIKE " + "'" + date + "'" + " AND subPos = " + position,
                null);
        return true;
    }

    public static boolean markHomeWorkAsDone(MyDate date, int position, Activity activity, boolean done) {
        dbio = new DBIO(activity, DBIO.DATABASE_NAME, null, 1);
        dbio.database = dbio.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("done", done);
        dbio.database.update(DBIO.DATABASE_NAME, contentValues,
                "date LIKE " + "'" + date + "'" + " AND subPos = " + position, null);
        return true;
    }

    public static boolean changeHomeWork(MyDate date, int position, Activity activity, String newText) {
        dbio = new DBIO(activity, DBIO.DATABASE_NAME, null, 1);
        dbio.database = dbio.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("text", newText);
        dbio.database.update(DBIO.DATABASE_NAME, contentValues,
                "date LIKE " + "'" + date + "'" + " AND subPos = " + position, null);
        return true;
    }

    public static boolean createJSON(HashMap<Integer, String>[] schedule, Activity activity) {
        JSONObject result = new JSONObject();
        JSONArray daysArray = new JSONArray();
        JSONObject daysArrayElement = new JSONObject();
        JSONArray subjectsArray = new JSONArray();
        JSONObject subjectsArrayElement = new JSONObject();

        try {
            for (int i = 0; i < schedule.length; i++) {
                daysArrayElement.put("id", i);
                for (int j = 0; j < 8; j++) {
                    subjectsArrayElement.put("subject_id", j);
                    subjectsArrayElement.put("text", schedule[i].get(j));

                    subjectsArray.put(subjectsArrayElement);

                    subjectsArrayElement = new JSONObject();
                }
                daysArrayElement.put("subjects", subjectsArray);
                daysArray.put(daysArrayElement);

                subjectsArray = new JSONArray();
                daysArrayElement = new JSONObject();
            }
            result.put("days", daysArray);
        } catch (JSONException ex) {
            ex.printStackTrace();
            return false;
        }

        writeFile(result.toString(), activity, FILENAME_SCHEDULE);

        return true;
    }

    public static boolean firstStart(Activity activity) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    activity.openFileInput(FILENAME_FIRST_START)));

            StringBuffer stringBuffer = new StringBuffer();

            String str = "";
            while (br.readLine() != null) {
                stringBuffer.append(br.readLine());
            }
            str = stringBuffer.toString();
            if (str.equals("false") || str != null)
                return false;
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public static int getDatesCount(Activity activity) {
        dbio = new DBIO(activity, DBIO.DATABASE_NAME, null, 1);
        int count = 0;
        dbio.database = dbio.getReadableDatabase();
        Cursor cursor = dbio.database.rawQuery("SELECT * FROM " + DBIO.DATABASE_NAME, null);
        if (cursor.moveToFirst()){
            do {
                count++;
            } while (cursor.moveToNext());
        }
        return count;
    }
}
