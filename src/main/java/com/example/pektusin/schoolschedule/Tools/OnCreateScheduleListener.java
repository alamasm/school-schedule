package com.example.pektusin.schoolschedule.Tools;

import java.util.HashMap;

/**
 * Created by pektusin on 9/17/2016.
 */
public interface OnCreateScheduleListener {
    void sendSchedule(HashMap<Integer, String>[] schedule);

    void checkMenuItem(int day);
}
