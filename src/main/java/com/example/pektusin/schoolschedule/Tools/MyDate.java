package com.example.pektusin.schoolschedule.Tools;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by pektusin on 9/15/2016.
 */
public class MyDate extends Date {
    @Override
    public String toString() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(this);
        return calendar.get(Calendar.DAY_OF_MONTH) + "." + (calendar.get(Calendar.MONTH) + 1) + "." +
                calendar.get(Calendar.YEAR);
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof MyDate) {
            Calendar calendarObj = Calendar.getInstance();
            MyDate thisDate = this;
            Calendar thisCalender = Calendar.getInstance();
            calendarObj.setTime((MyDate) object);
            thisCalender.setTime(this);
            if (calendarObj.get(Calendar.YEAR) == thisCalender.get(Calendar.YEAR))
                if (calendarObj.get(Calendar.MONTH) == thisCalender.get(Calendar.MONTH))
                    if (calendarObj.get(Calendar.DAY_OF_MONTH) == thisCalender.get(Calendar.DAY_OF_MONTH))
                        return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(this);
        return  (calendar.get(Calendar.MONTH) + calendar.get(Calendar.DAY_OF_MONTH) +
                calendar.get(Calendar.YEAR) >>> 32) ^ calendar.get(Calendar.MONTH);
    }
}
