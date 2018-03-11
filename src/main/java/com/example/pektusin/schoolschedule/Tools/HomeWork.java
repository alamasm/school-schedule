package com.example.pektusin.schoolschedule.Tools;

/**
 * Created by pektusin on 9/15/2016.
 */
public class HomeWork {
    private boolean done;
    private String text;

    public HomeWork(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public boolean isDone() {
        return done;
    }

    public void markDone() {
        done = true;
    }

    public void unmarkDone() {done = false;}

    public void setText(String text) {
        this.text = text;
    }
}
