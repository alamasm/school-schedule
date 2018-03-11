package com.example.pektusin.schoolschedule.Tools;

import android.app.Activity;

import com.example.pektusin.schoolschedule.Tools.Files.FileIO;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by pektusin on 9/15/2016.
 */
public class Schedule {
    private HashMap<Integer, String>[] schedule = new HashMap[5];
    private HashMap<MyDate, HashMap<Integer, HomeWork>> homeWork = new HashMap<>();
    private ArrayList<MyDate> homeWorkDates = new ArrayList<>();
    private int datesCount = 0;

    public Schedule(HashMap<Integer, String>[] schedule, int datesCount, HashMap<MyDate, HashMap<Integer, HomeWork>> homeWork) {
        this.schedule = schedule;
        this.datesCount = datesCount;
        this.homeWork = homeWork;
    }

    public HashMap<MyDate, HashMap<Integer, HomeWork>> getHomeWorkHashMap() {
        return homeWork;
    }

    public HashMap<Integer, String> getSchedule(int dayOfWeek) {
        if (dayOfWeek < 5)
            return schedule[dayOfWeek];
        else return null;
    }

    public HashMap<Integer, String>[] getSchedule() {
        return schedule;
    }

    public String getLesson(int dayOfWeek, int subjectPosition) {
        if (schedule[dayOfWeek].containsKey(subjectPosition))
            return schedule[dayOfWeek].get(subjectPosition);
        return "NULL";
    }

    public boolean dateIsEmpty(MyDate date) {
        if (homeWork.containsKey(date))
            if (homeWork.get(date).isEmpty())
                return true;
            else
                return false;
        return false;
    }

    //-1 equals , that item is currently in HashMap
    public int putHomework(String homework, MyDate date, int subjectPosition, Activity activity) {
        if (homeWork.containsKey(date)) {
            if (!homeWork.get(date).containsKey(subjectPosition)) {
                homeWork.get(date).put(subjectPosition, new HomeWork(homework));
                datesCount++;
                FileIO.writeHomeWork(homeWork, homeWorkDates, activity, datesCount);
                return 0;
            } else
                return -1;
        } else {
            HashMap<Integer, HomeWork> homeWorkHashMap = new HashMap<>();
            homeWorkHashMap.put(subjectPosition, new HomeWork(homework));
            homeWork.put(date, homeWorkHashMap);
            homeWorkDates.add(date);
            datesCount++;
            FileIO.writeHomeWork(homeWork, homeWorkDates, activity, datesCount);
            return 0;
        }
    }

    public HomeWork getHomeWork(MyDate date, int subjectPosition) {
        if (homeWork.containsKey(date))
            if (homeWork.get(date).containsKey(subjectPosition))
                return homeWork.get(date).get(subjectPosition);
        return null;
    }

    public boolean changeHomeWork(MyDate date, int subjectPosition, String text, Activity activity) {
        if (homeWork.containsKey(date))
            if (homeWork.get(date).containsKey(subjectPosition)) {
                homeWork.get(date).get(subjectPosition).setText(text);
                FileIO.changeHomeWork(date, subjectPosition, activity, text);
                return true;
            }
        return false;
    }

    public boolean markHomeWorkAsDone(MyDate date, int subjectPosition, Activity activity, boolean done) {
        if (homeWork.containsKey(date))
            if (homeWork.get(date).containsKey(subjectPosition)) {
                if (done)
                    homeWork.get(date).get(subjectPosition).markDone();
                else
                    homeWork.get(date).get(subjectPosition).unmarkDone();
                FileIO.markHomeWorkAsDone(date, subjectPosition, activity, done);
                return true;
            }
        return false;
    }

    public boolean removeHomeWork(MyDate date, int subjectPosition, Activity activity) {
        if (homeWork.containsKey(date))
            if (homeWork.get(date).containsKey(subjectPosition)) {
                homeWork.get(date).remove(subjectPosition);
                if (homeWork.get(date).isEmpty()) {
                    homeWork.remove(date);
                    homeWorkDates.remove(date);
                }
                FileIO.removeHomeWork(date, subjectPosition, activity);
                return true;
            }
        return false;
    }

    public ArrayList<MyDate> getHomeWorkDates() {
        return homeWorkDates;
    }

    public void setHomeWorkDates(ArrayList<MyDate> homeWorkDates) {
        this.homeWorkDates = homeWorkDates;
    }
}

