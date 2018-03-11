package com.example.pektusin.schoolschedule.Tools.ArrayAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.pektusin.schoolschedule.R;
import com.example.pektusin.schoolschedule.Tools.ContainsSchedule;

/**
 * Created by pektusin on 9/17/2016.
 */
public class MainListAdapter extends ArrayAdapter<String> {
    Context context;
    String[] objects;
    ContainsSchedule activity;

    public MainListAdapter(Context context, int resource, String[] objects, ContainsSchedule activity) {
        super(context, resource, objects);

        this.context = context;
        this.objects = objects;
        this.activity = activity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);


        View row = vi.inflate(R.layout.list_item, parent, false);
        if (activity.getSchedule().getHomeWork(activity.getCurrentDayDate(), position) != null)
            if (activity.getSchedule().getHomeWork(activity.getCurrentDayDate(), position).isDone())
                row = vi.inflate(R.layout.list_item_done, parent, false);
        TextView title = (TextView) row.findViewById(R.id.textView1);
        TextView homeWork = (TextView) row.findViewById(R.id.textView2);

        if (activity.getSchedule().getHomeWork(activity.getCurrentDayDate(), position) != null) {
            homeWork.setText(activity.getSchedule().getHomeWork(
                    activity.getCurrentDayDate(), position).getText());
        }

        int pos = position + 1;
        title.setText(objects[position]);
        pos++;
        return row;
    }
}
