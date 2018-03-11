package com.example.pektusin.schoolschedule.Fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.example.pektusin.schoolschedule.MainActivity;
import com.example.pektusin.schoolschedule.R;
import com.example.pektusin.schoolschedule.Tools.OnCreateScheduleListener;

import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CreateScheduleFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CreateScheduleFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateScheduleFragment extends Fragment {
    public boolean firstCreate = true;
    private OnCreateScheduleListener createScheduleListener;
    private int currentDay = 0;
    private String title;
    private HashMap<Integer, String>[] schedule = new HashMap[5];
    private ListView listView;
    private ArrayAdapter<String> arrayAdapter;
    private String[][] scheduleArray = new String[5][8];
    private Button nextButton;
    private Button backButton;
    private Context context;
    private Fragment thisFragment;
    private String[][] backup;

    public CreateScheduleFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment DayFragment.
     */
    public static CreateScheduleFragment newInstance(String[][] scheduleArray) {
        CreateScheduleFragment fragment = new CreateScheduleFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);

        fragment.initArray(scheduleArray);
        fragment.firstCreate = false;
        fragment.backup = scheduleArray;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (firstCreate)
            initArray();
        arrayAdapter = new ArrayAdapter<>(container.getContext(), android.R.layout.simple_list_item_1, scheduleArray[currentDay]);
        title = container.getContext().getString(R.string.monday_string);

        context = container.getContext();

        thisFragment = this;

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(title);


        return inflater.inflate(R.layout.fragment_day, container, false);
    }

    private void initArray() {
        for (int i = 0; i < scheduleArray.length; i++) {
            for (int j = 0; j < scheduleArray[i].length; j++) {
                scheduleArray[i][j] = (j + 1) + " Предмет";
            }
        }
    }

    private void initArray(String[][] schedule) {
        initArray();
        for (int i = 0; i < scheduleArray.length; i++) {
            for (int j = 0; j < schedule[i].length; j++) {
                scheduleArray[i][j] = schedule[i][j];
            }
        }
    }

    private void createSchedule(String[][] scheduleArray) {
        for (int i = 0; i < scheduleArray.length; i++) {
            schedule[i] = new HashMap<>();
            for (int j = 0; j < scheduleArray[i].length; j++) {
                if (scheduleArray[i][j].equals((j + 1) + context.getString(R.string.subject_text)))
                    schedule[i].put(j, "");
                else
                    schedule[i].put(j, scheduleArray[i][j]);
            }
        }
    }


    private void setSubject(final int position) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(context);

        alert.setTitle(context.getString(R.string.alert_title_string));
        alert.setMessage(context.getString(R.string.alert_message_string));

        final EditText input = new EditText(context);
        alert.setView(input);

        alert.setPositiveButton(context.getString(R.string.alert_ok_text), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if (!input.getText().toString().equals(""))
                    scheduleArray[currentDay][position] = (position + 1) + ". " + input.getText().toString();
                else
                    scheduleArray[currentDay][position] = "";

                arrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, scheduleArray[currentDay]);
                listView.setAdapter(arrayAdapter);
            }
        });

        alert.setNegativeButton(context.getString(R.string.alert_cancel_text), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });

        alert.show();
        input.post(new Runnable() {
            public void run() {
                input.requestFocusFromTouch();
                InputMethodManager lManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                lManager.showSoftInput(input, 0);
            }
        });
    }

    public void sendSchedule(HashMap<Integer, String> schedule[]) {
        if (createScheduleListener != null) {
            createScheduleListener.sendSchedule(schedule);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnCreateScheduleListener) {
            createScheduleListener = (OnCreateScheduleListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        createScheduleListener = null;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (view.getContext() instanceof OnCreateScheduleListener)
            createScheduleListener = (OnCreateScheduleListener) view.getContext();

        listView = (ListView) view.findViewById(R.id.listViewFragment);
        nextButton = (Button) getView().findViewById(R.id.next_button);
        backButton = (Button) getView().findViewById(R.id.back_button);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDay(++currentDay);
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDay(--currentDay);
            }
        });

        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setSubject(position);
            }
        });
    }

    public void setDay(int day) {
        if (day >= 0 && day < 5) {
            currentDay = day;
            updateArray();
        } else if (currentDay >= 5) {
            onScheduleCreated();
        }

        createScheduleListener.checkMenuItem(currentDay);
    }

    private void updateArray() {
        arrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, scheduleArray[currentDay]);
        listView.setAdapter(arrayAdapter);
        setTitle(currentDay);
    }

    private void setTitle(int day) {
        String title = "";
        switch (day) {
            case 0:
                title = context.getString(R.string.monday_string);
                break;
            case 1:
                title = context.getString(R.string.tuesday_string);
                break;
            case 2:
                title = context.getString(R.string.wednesday_string);
                break;
            case 3:
                title = context.getString(R.string.thursday_string);
                break;
            case 4:
                title = context.getString(R.string.friday_string);
        }
        if (getActivity() != null)
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(title);
    }

    private void onScheduleCreated() {
        createSchedule(scheduleArray);
        sendSchedule(schedule);
        getActivity().getFragmentManager().beginTransaction().remove(thisFragment).commit();
    }

    public void cancel() {
        getActivity().getFragmentManager().beginTransaction().remove(thisFragment).commit();
        ((MainActivity) getActivity()).cancel();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
