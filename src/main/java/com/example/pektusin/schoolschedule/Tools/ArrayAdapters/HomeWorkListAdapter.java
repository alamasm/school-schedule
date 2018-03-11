package com.example.pektusin.schoolschedule.Tools.ArrayAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.pektusin.schoolschedule.MainActivity;
import com.example.pektusin.schoolschedule.R;

/**
 * Created by pektusin on 9/28/2016.
 */
public class HomeWorkListAdapter extends ArrayAdapter<String> {
    Context context;
    String[] homeWorks;

    public HomeWorkListAdapter(Context context, int resource, String[] homeWorks) {
        super(context, resource, homeWorks);

        this.context = context;
        this.homeWorks = homeWorks;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View row = vi.inflate(R.layout.list_item, parent, false);

        if (homeWorks[position].startsWith("DONE ")) {
            row = vi.inflate(R.layout.home_work_list_item_done, parent, false);
            TextView textView = (TextView) row.findViewById(R.id.text_view_home_work_done);
            TextView textViewSubject = (TextView) row.findViewById(R.id.text_view_home_work_subject_done);
            TextView textViewNumber = (TextView) row.findViewById(R.id.text_view_home_work_number_done);

            textViewSubject.setText(homeWorks[position].substring("DONE ".length(), homeWorks[position].lastIndexOf("HOME_WORK ")) +
            ", " + (MainActivity.getPosition(homeWorks[position]) + 1));

            textView.setText(homeWorks[position].substring(homeWorks[position].lastIndexOf("HOME_WORK ") + "HOME_WORK ".length(),
                    homeWorks[position].lastIndexOf("NUMBER ")));

            /*
            textViewNumber.setText(homeWorks[position].substring(homeWorks[position].lastIndexOf("NUMBER ") + "NUMBER ".length(),
                    homeWorks[position].lastIndexOf(" DATE ")));
                    */
        }
        else if (homeWorks[position].startsWith(MainActivity.PREFIX)) {
            row = vi.inflate(R.layout.list_group_name_item, parent, false);
            TextView textView = (TextView) row.findViewById(R.id.text_view_home_work_date);

            textView.setText(homeWorks[position].substring(MainActivity.PREFIX.length()));
        }
        else if (homeWorks[position].startsWith("NOT_DONE ")) {
            row = vi.inflate(R.layout.home_work_list_item, parent, false);
            TextView textView = (TextView) row.findViewById(R.id.text_view_home_work);
            TextView textViewSubject = (TextView) row.findViewById(R.id.text_view_home_work_subject);
            TextView textViewNumber = (TextView) row.findViewById(R.id.text_view_home_work_number);

            textViewSubject.setText(homeWorks[position].substring("NOT_DONE ".length(), homeWorks[position].lastIndexOf("HOME_WORK ")) +
            ", " + (MainActivity.getPosition(homeWorks[position]) + 1));

            textView.setText(homeWorks[position].substring(homeWorks[position].lastIndexOf("HOME_WORK ") + "HOME_WORK ".length(),
                    homeWorks[position].lastIndexOf("NUMBER ")));

            /*
            textViewNumber.setText(homeWorks[position].substring(homeWorks[position].lastIndexOf("NUMBER ") + "NUMBER ".length(),
                    homeWorks[position].lastIndexOf("DATE ")));
                    */
        }
        return row;
    }
}

