package com.example.pektusin.schoolschedule.Tools;

/**
 * Created by pektusin on 9/23/2016.
 */
public interface ContainsSchedule {
    Schedule getSchedule();
    MyDate getCurrentDayDate();
    void markAsDone(int position);
    void changeHomeWork(MyDate date, int position);
}
