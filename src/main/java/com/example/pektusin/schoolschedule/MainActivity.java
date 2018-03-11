package com.example.pektusin.schoolschedule;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.pektusin.schoolschedule.Fragments.CreateScheduleFragment;
import com.example.pektusin.schoolschedule.Tools.ArrayAdapters.HomeWorkListAdapter;
import com.example.pektusin.schoolschedule.Tools.ArrayAdapters.MainListAdapter;
import com.example.pektusin.schoolschedule.Tools.ContainsSchedule;
import com.example.pektusin.schoolschedule.Tools.Files.FileIO;
import com.example.pektusin.schoolschedule.Tools.MyDate;
import com.example.pektusin.schoolschedule.Tools.OnCreateScheduleListener;
import com.example.pektusin.schoolschedule.Tools.Schedule;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.oauth.OAuth20Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnCreateScheduleListener, ContainsSchedule {

    public static String PREFIX = "GROUP_NAME_STARTS";
    public int currentDay;
    public ArrayList<MyDate> homeWorkDates = new ArrayList<>();
    NavigationView navigationView;
    private Schedule schedule;
    private MainListAdapter arrayAdapter;
    private HomeWorkListAdapter homeWorkListAdapter;
    private String[] homeWorks;
    private ListView listView;
    private String[][] scheduleTextArray = new String[5][8];
    private String[][] scheduleArray = new String[5][8];
    private String daysNames[] = new String[5];
    private Menu navigationMenu;
    private MenuItem[] menuItems = new MenuItem[5];
    private FragmentTransaction fragmentTransaction;
    private Fragment createScheduleFragment;
    private MyDate date;
    private Activity thisActivity;
    private MyDate currentWeekStartDate;
    private MyDate currentDayDate;
    private State state;
    private Menu optionsMenu;
    private TextView tvNextLesson;
    private boolean showAllHomeWork = false;

    public static int getPosition(String str) {
        return Integer.parseInt(str.substring(str.lastIndexOf("POSITION ") + "POSITION ".length()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        initNextLessonText();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationMenu = navigationView.getMenu();
        initMenuItems();

        Calendar calendar = Calendar.getInstance();
        date = new MyDate();
        calendar.setTime(date);
        Log.e("currentDay before", " " + calendar.getFirstDayOfWeek());
        calendar.setFirstDayOfWeek(Calendar.MONDAY);

        Calendar weekStartDateCalendar = Calendar.getInstance();
        weekStartDateCalendar.setTime(new Date());
        weekStartDateCalendar.setFirstDayOfWeek(Calendar.MONDAY);

        if (weekStartDateCalendar.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY &&
                weekStartDateCalendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY)
            weekStartDateCalendar.roll(Calendar.DAY_OF_MONTH,
                    -(weekStartDateCalendar.get(Calendar.DAY_OF_WEEK) - 2));

        else if (weekStartDateCalendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)
            weekStartDateCalendar.roll(Calendar.DAY_OF_MONTH, 2);
        else if (weekStartDateCalendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
            weekStartDateCalendar.roll(Calendar.DAY_OF_MONTH, 1);

        currentWeekStartDate = new MyDate();
        currentWeekStartDate.setTime(weekStartDateCalendar.getTime().getTime());

        currentDay = calendar.get(Calendar.DAY_OF_WEEK) - 2;

        if (calendar.get(Calendar.DAY_OF_WEEK) == 7 || calendar.get(Calendar.DAY_OF_WEEK) == 1) {
            //setWeek(true);
            currentDay = 0;
        } else if (calendar.get(Calendar.HOUR_OF_DAY) > 15)
            currentDay++;

        if (FileIO.firstStart(this)) {
            state = State.CREATING_SCHEDULE;
            createSchedule();
        } else {
            state = State.RUNNING;
            create(FileIO.getSchedule(this));
            selectFirstItem();
        }

        initDaysNames();
        setCurrentDayDate();

        initNextLessonText();
    }

    private void changeSchedule() {
        state = State.CREATING_SCHEDULE;
        fragmentTransaction = getFragmentManager().beginTransaction();

        createScheduleFragment = CreateScheduleFragment.newInstance(scheduleArray);

        listView.setVisibility(ListView.INVISIBLE);
        fragmentTransaction.replace(R.id.container, createScheduleFragment).commit();

        navigationMenu.findItem(R.id.nav_monday).setChecked(true);

        onCreateOptionsMenu(optionsMenu);

        setNavViewItemsTextCreating();
    }

    private void setCurrentDayDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentWeekStartDate);
        calendar.roll(Calendar.DAY_OF_MONTH, currentDay);
        currentDayDate = new MyDate();
        currentDayDate.setTime(calendar.getTime().getTime());
    }

    private void initDaysNames() {
        daysNames[0] = getString(R.string.monday_string);
        daysNames[1] = getString(R.string.tuesday_string);
        daysNames[2] = getString(R.string.wednesday_string);
        daysNames[3] = getString(R.string.thursday_string);
        daysNames[4] = getString(R.string.friday_string);
    }

    private void initMenuItems() {
        for (int i = 0; i < menuItems.length; i++) {
            menuItems[i] = navigationMenu.getItem(i);
        }
    }

    private void selectFirstItem() {
        switch (currentDay) {
            case 0:
                onNavigationItemSelected(navigationMenu.findItem(R.id.nav_monday).setChecked(true));
                break;
            case 1:
                onNavigationItemSelected(navigationMenu.findItem(R.id.nav_tuesday).setChecked(true));
                break;
            case 2:
                onNavigationItemSelected(navigationMenu.findItem(R.id.nav_wednesday).setChecked(true));
                break;
            case 3:
                onNavigationItemSelected(navigationMenu.findItem(R.id.nav_thursday).setChecked(true));
                break;
            case 4:
                onNavigationItemSelected(navigationMenu.findItem(R.id.nav_friday).setChecked(true));
                break;
        }
    }

    private void setMenuItemsText() {
        for (int i = 0; i < menuItems.length; i++) {
            String itemText = "";
            switch (i) {
                case 0:
                    itemText = getString(R.string.monday_string);
                    break;
                case 1:
                    itemText = getString(R.string.tuesday_string);
                    break;
                case 2:
                    itemText = getString(R.string.wednesday_string);
                    break;
                case 3:
                    itemText = getString(R.string.thursday_string);
                    break;
                case 4:
                    itemText = getString(R.string.friday_string);
                    break;
            }

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(currentWeekStartDate);
            calendar.roll(Calendar.DAY_OF_MONTH, i);
            MyDate date = new MyDate();
            date.setTime(calendar.getTime().getTime());

            menuItems[i].setTitle(date.toString() + " , " + itemText);
        }
        onNavigationItemSelected(navigationMenu.getItem(currentDay));
    }

    private void createSchedule() {
        fragmentTransaction = getFragmentManager().beginTransaction();
        createScheduleFragment = new CreateScheduleFragment();
        fragmentTransaction.replace(R.id.container, createScheduleFragment).commit();

        setNavViewItemsTextCreating();
    }

    private void parseArray() {
        int count = 0;
        boolean firstLessons = true;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 8; j++) {
                if (!schedule.getSchedule()[i].get(j).equals("") || firstLessons) count++;
                if (!schedule.getSchedule()[i].equals("")) firstLessons = false;
            }
            firstLessons = true;
            scheduleTextArray[i] = new String[count];
            scheduleArray[i] = new String[count];
            count = 0;
        }

        for (int i = 0; i < scheduleTextArray.length; i++) {
            for (int j = 0; j < scheduleTextArray[i].length; j++) {
                if ((schedule.getSchedule(i)) != null && !schedule.getSchedule().equals("")) {
                    scheduleTextArray[i][j] = schedule.getSchedule(i).get(j);
                    scheduleArray[i][j] = schedule.getSchedule(i).get(j);
                }
            }
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo adapterContextMenuInfo =
                (AdapterView.AdapterContextMenuInfo) menuInfo;

        if (scheduleArray[currentDay][adapterContextMenuInfo.position].equals(""))
            return;
        if (homeWorks[adapterContextMenuInfo.position].contains(PREFIX))
            return;

        menu.add(0, 0, 0, getString(R.string.changeString));
        menu.add(0, 1, 1, getString(R.string.deleteString));

        if (schedule.getHomeWork(currentDayDate, adapterContextMenuInfo.position) != null)
            if (schedule.getHomeWork(currentDayDate, adapterContextMenuInfo.position).isDone())
                menu.add(0, 2, 2, getString(R.string.unmarkString));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo adapterContextMenuInfo =
                (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (state == State.RUNNING) {
            switch (item.getItemId()) {
                case 0:
                    changeHomeWork(currentDayDate, adapterContextMenuInfo.position);
                    break;
                case 1:
                    removeHomeWork(adapterContextMenuInfo.position, currentDayDate);
                    break;
                case 2:
                    unmarkDone(adapterContextMenuInfo.position, currentDayDate);
                    break;
            }
            return true;
        } else {
            String o = homeWorkListAdapter.getItem(adapterContextMenuInfo.position);
            switch (item.getItemId()) {
                case 0:

                    changeHomeWork(getDate(o), getPosition(o));
                    break;
                case 1:
                    removeHomeWork(getPosition(o), getDate(o));
                    break;
                case 2:
                    unmarkDone(getPosition(o), getDate(o));
                    break;
            }
        }
        return true;
    }

    private void initListView() {
        parseArray();
        arrayAdapter = new MainListAdapter(this, android.R.layout.simple_list_item_1, scheduleTextArray[currentDay], this);

        listView = (ListView) findViewById(R.id.listView);

        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!scheduleArray[currentDay][position].equals(""))
                    if (schedule.getHomeWork(currentDayDate, position) == null) {
                        addHomeWork(position);
                    } else markAsDone(position);
            }
        });
        registerForContextMenu(listView);
    }

    public void markAsDone(int position) {
        schedule.markHomeWorkAsDone(currentDayDate, position, this, true);
        updateListView();
    }

    private void markAsDone(MyDate date, int position) {
        schedule.markHomeWorkAsDone(date, position, this, true);
        if (state == State.RUNNING)
            updateListView();
        else if (state == State.HOME_WORK)
            updateHomeWorkAdapter();
    }

    public void unmarkDone(int position, MyDate currentDayDate) {
        schedule.markHomeWorkAsDone(currentDayDate, position, this, false);
        if (state == State.RUNNING)
            updateListView();
        else if (state == State.HOME_WORK)
            updateHomeWorkAdapter();
    }

    private void removeHomeWork(int position, MyDate currentDayDate) {
        schedule.removeHomeWork(currentDayDate, position, this);
        if (state == State.RUNNING)
            updateListView();
        else if (state == state.HOME_WORK)
            updateHomeWorkAdapter();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        optionsMenu = menu;
        try {
            menu.getItem(0);
        } catch (IndexOutOfBoundsException e) {
            getMenuInflater().inflate(R.menu.main, menu);
        }
        MenuItem mi = menu.getItem(0);

        if (state == State.RUNNING) {
            mi.setTitle(R.string.change_schedule_string);
            mi.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (state != State.CREATING_SCHEDULE)
                        changeSchedule();
                    return true;
                }
            });
            return true;
        } else if (state == State.CREATING_SCHEDULE && !((CreateScheduleFragment) createScheduleFragment).firstCreate) {
            mi.setTitle(getString(R.string.alert_cancel_text));
            mi.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (state == State.CREATING_SCHEDULE) {
                        state = State.RUNNING;
                        onCreateOptionsMenu(menu);
                        cancelCreating();
                        return true;
                    }
                    return false;
                }
            });

        } else if (state == State.HOME_WORK) {
            if (showAllHomeWork) {
                mi.setTitle(getString(R.string.home_work_show_all));
                mi.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        showAllHomeWork = false;
                        initHomeWorkArray(showAllHomeWork);
                        updateHomeWorkAdapter();
                        onCreateOptionsMenu(optionsMenu);
                        return true;
                    }
                });
            } else {
                mi.setTitle(getString(R.string.home_work_show_only_next));
                mi.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        showAllHomeWork = true;
                        initHomeWorkArray(showAllHomeWork);
                        updateHomeWorkAdapter();
                        onCreateOptionsMenu(optionsMenu);
                        return true;
                    }
                });
            }
        }
        return false;
    }

    private void setNavViewItemsTextCreating() {
        menuItems[0].setTitle(getString(R.string.monday_string));
        menuItems[1].setTitle(getString(R.string.tuesday_string));
        menuItems[2].setTitle(getString(R.string.wednesday_string));
        menuItems[3].setTitle(getString(R.string.thursday_string));
        menuItems[4].setTitle(getString(R.string.friday_string));
        navigationMenu.findItem(R.id.nav_next_week).setVisible(false);
        navigationMenu.findItem(R.id.nav_previous_week).setVisible(false);
        navigationMenu.findItem(R.id.nav_home_work).setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void cancelCreating() {
        ((CreateScheduleFragment) createScheduleFragment).cancel();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        String title = "";
        int id = item.getItemId();

        if (state == State.HOME_WORK) {
            closeHomeWork();
            setStateRunning();
        }

        if (id == R.id.nav_monday) {
            currentDay = 0;
            title = getString(R.string.monday_string);
        } else if (id == R.id.nav_tuesday) {
            currentDay = 1;
            title = getString(R.string.tuesday_string);
        } else if (id == R.id.nav_wednesday) {
            currentDay = 2;
            title = getString(R.string.wednesday_string);
        } else if (id == R.id.nav_thursday) {
            currentDay = 3;
            title = getString(R.string.thursday_string);
        } else if (id == R.id.nav_friday) {
            currentDay = 4;
            title = getString(R.string.friday_string);
        } else if (id == R.id.nav_next_week) {
            setWeek(true);
            title = daysNames[currentDay];
        } else if (id == R.id.nav_previous_week) {
            setWeek(false);
            title = daysNames[currentDay];
        } else if (id == R.id.nav_home_work) {
            viewHomeWorkFragment();
        }


        if (state == State.RUNNING) {
            setCurrentDayDate();

            if (listView != null) {
                arrayAdapter = new MainListAdapter(this, android.R.layout.simple_list_item_1, scheduleTextArray[currentDay], this);
                listView.setAdapter(arrayAdapter);
            }

            Calendar currentDayCalendar = Calendar.getInstance();
            currentDayCalendar.setTime(currentDayDate);

            getSupportActionBar().setTitle(title + ", " + currentDayDate);
        } else if (state == State.CREATING_SCHEDULE) {
            ((CreateScheduleFragment) createScheduleFragment).setDay(currentDay);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    private void closeHomeWork() {
        state = State.RUNNING;

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!scheduleArray[currentDay][position].equals(""))
                    if (schedule.getHomeWork(currentDayDate, position) == null) {
                        addHomeWork(position);
                    } else markAsDone(position);
            }
        });
    }

    private void viewHomeWorkFragment() {
        initHomeWorkArray(showAllHomeWork);
        updateHomeWorkAdapter();
        state = State.HOME_WORK;

        onCreateOptionsMenu(optionsMenu);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!homeWorks[position].contains(PREFIX))
                    if (homeWorks[position].contains("NOT_DONE")) {

                        markAsDone(getDate(homeWorks[position]), getPosition(homeWorks[position]));
                        initHomeWorkArray(showAllHomeWork);
                        updateHomeWorkAdapter();
                    }
            }
        });

        getSupportActionBar().setTitle(getString(R.string.home_work_text));
    }

    private MyDate getDate(String dateString) {
        MyDate date = new MyDate();
        SimpleDateFormat smf = new SimpleDateFormat();
        smf.applyPattern("dd.MM.yyyy");

        try {
            date.setTime(smf.parse(getDateString(dateString)).getTime());
        } catch (ParseException e) {
            return null;
        }
        return date;
    }

    private String getDateString(String date) {
        int d = date.lastIndexOf("DATE ");
        int x = date.lastIndexOf(" POSITION");
        return date.substring(d + "DATE ".length(),
                x);
    }

    private void updateHomeWorkAdapter() {
        initHomeWorkArray(showAllHomeWork);
        homeWorkListAdapter = new HomeWorkListAdapter(this, android.R.layout.simple_list_item_1, homeWorks);
        listView.setAdapter(homeWorkListAdapter);
    }

    private void initHomeWorkArray(boolean showAllHomeWork) {
        MyDate[] dates = new MyDate[schedule.getHomeWorkDates().size()];

        int i = 0;
        for (MyDate date : schedule.getHomeWorkDates()) {
            dates[i] = date;
            i++;
        }

        Arrays.sort(dates);

        ArrayList<String> homeWorksList = new ArrayList<>();

        for (MyDate date : dates) {
            if (showAllHomeWork && date.before(new Date()))
                continue;

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            if (!schedule.dateIsEmpty(date))
                homeWorksList.add(PREFIX + " " + date.toString() + ", " + getDayString(calendar.get(Calendar.DAY_OF_WEEK) - 2));
            else
                continue;

            int count = 0;
            for (int j = 0; j < 8; j++) {
                if (schedule.getHomeWork(date, j) != null) {
                    count++;
                    Calendar dayCalendar = Calendar.getInstance();
                    calendar.setTime(date);
                    String subject = schedule.getLesson(calendar.get(Calendar.DAY_OF_WEEK) - 2, j).substring(3);
                    homeWorksList.add((schedule.getHomeWork(date, j).isDone() ? "DONE " : "NOT_DONE ") +
                            subject + " HOME_WORK " + schedule.getHomeWork(date, j).getText() + " NUMBER " + count
                            + " DATE " + date + " POSITION " + j);
                }
            }
        }

        homeWorks = homeWorksList.toArray(new String[homeWorksList.size()]);

        homeWorkListAdapter = new HomeWorkListAdapter(this, android.R.layout.simple_list_item_1, homeWorks);
    }

    private String getDayString(int day) {
        switch (day) {
            case 0:
                return getString(R.string.monday_string);
            case 1:
                return getString(R.string.tuesday_string);
            case 2:
                return getString(R.string.wednesday_string);
            case 3:
                return getString(R.string.thursday_string);
            case 4:
                return getString(R.string.friday_string);
        }
        return "";
    }

    private void addHomeWork(final int position) {
        thisActivity = this;

        final AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle(getString(R.string.alert_title_home_work_string));

        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton(getString(R.string.alert_ok_text), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                schedule.putHomework(input.getText().toString(),
                        currentDayDate,
                        position, thisActivity);
                updateListView();
            }
        });

        alert.setNegativeButton(getString(R.string.alert_cancel_text), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });

        alert.show();

        input.post(new Runnable() {
            public void run() {
                input.requestFocusFromTouch();
                InputMethodManager lManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                lManager.showSoftInput(input, 0);
            }
        });
    }

    public void changeHomeWork(final MyDate date, final int position) {
        if (schedule.getHomeWork(date, position) != null) {
            thisActivity = this;

            final AlertDialog.Builder alert = new AlertDialog.Builder(this);

            alert.setTitle(getString(R.string.alert_title_home_work_string));

            final EditText input = new EditText(this);
            alert.setView(input);

            alert.setPositiveButton(getString(R.string.alert_ok_text), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    schedule.changeHomeWork(date, position, input.getText().toString(), thisActivity);
                    if (state == State.RUNNING)
                        updateListView();
                    else if (state == State.HOME_WORK)
                        updateHomeWorkAdapter();
                }
            });

            alert.setNegativeButton(getString(R.string.alert_cancel_text), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            });

            input.setText(schedule.getHomeWork(date, position).getText());

            alert.show();

            input.post(new Runnable() {
                public void run() {
                    input.requestFocusFromTouch();
                    InputMethodManager lManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    lManager.showSoftInput(input, 0);
                }
            });
        }
    }

    private void updateListView() {
        arrayAdapter = new MainListAdapter(this, android.R.layout.simple_list_item_1, scheduleTextArray[currentDay], this);
        listView.setAdapter(arrayAdapter);
    }

    private void setWeek(Boolean prevOrNext) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentWeekStartDate);
        calendar.roll(Calendar.WEEK_OF_YEAR, prevOrNext ? 1 : -1);
        currentWeekStartDate.setTime(calendar.getTime().getTime());

        setMenuItemsText();
    }

    @Override
    public void sendSchedule(HashMap<Integer, String>[] schedule) {
        FileIO.createJSON(schedule, this);
        FileIO.writeFile("false", this, FileIO.FILENAME_FIRST_START);
        create(schedule);
        setStateRunning();
    }

    @Override
    public void checkMenuItem(int day) {
        switch (day) {
            case 0:
                navigationMenu.findItem(R.id.nav_monday).setChecked(true);
                break;
            case 1:
                navigationMenu.findItem(R.id.nav_tuesday).setChecked(true);
                break;
            case 2:
                navigationMenu.findItem(R.id.nav_wednesday).setChecked(true);
                break;
            case 3:
                navigationMenu.findItem(R.id.nav_thursday).setChecked(true);
                break;
            case 4:
                navigationMenu.findItem(R.id.nav_friday).setChecked(true);
                break;
        }
    }

    private void create(HashMap<Integer, String>[] schedule) {
        this.schedule = new Schedule(schedule, FileIO.getDatesCount(this), FileIO.getHomeWork(this));
        this.schedule.setHomeWorkDates(homeWorkDates);

        parseArray();

        initListView();

        setMenuItemsText();
    }

    @Override
    public Schedule getSchedule() {
        return schedule;
    }

    @Override
    public MyDate getCurrentDayDate() {
        return currentDayDate;
    }

    public void initNextLessonText() {
        View headerView = navigationView.getHeaderView(0);

        tvNextLesson = (TextView) headerView.findViewById(R.id.tv_next_lesson);
        tvNextLesson.setVisibility(View.INVISIBLE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        if (calendar.get(Calendar.HOUR_OF_DAY) > 8 && calendar.get(Calendar.HOUR_OF_DAY) < 15) {
            if (calendar.get(Calendar.HOUR_OF_DAY) != 14) {
                tvNextLesson.setVisibility(View.VISIBLE);
                tvNextLesson.setText(getNextLesson(calendar));
            } else if (calendar.get(Calendar.MINUTE) < 25) {
                tvNextLesson.setVisibility(View.VISIBLE);
                if (getNextLesson(calendar) != null)
                    tvNextLesson.setText(getNextLesson(calendar));
            }
        }
    }

    private String getNextLesson(Calendar calendar) {
        switch (calendar.get(Calendar.HOUR_OF_DAY)) {
            case 8:
                if (calendar.get(Calendar.MINUTE) < 55)
                    return schedule.getSchedule()[currentDay].get(1);
                else
                    return schedule.getSchedule()[currentDay].get(2);
            case 9:
                if (calendar.get(Calendar.MINUTE) < 50)
                    return schedule.getSchedule()[currentDay].get(2);
                else
                    return schedule.getSchedule()[currentDay].get(3);
            case 10:
                if (calendar.get(Calendar.MINUTE) < 45)
                    return schedule.getSchedule()[currentDay].get(3);
                else
                    return schedule.getSchedule()[currentDay].get(4);
            case 11:
                if (calendar.get(Calendar.MINUTE) < 45)
                    return schedule.getSchedule()[currentDay].get(4);
                else
                    return schedule.getSchedule()[currentDay].get(5);
            case 12:
                if (calendar.get(Calendar.MINUTE) < 40)
                    return schedule.getSchedule()[currentDay].get(5);
                else
                    return schedule.getSchedule()[currentDay].get(6);
            case 13:
                if (calendar.get(Calendar.MINUTE) < 35)
                    return schedule.getSchedule()[currentDay].get(6);
                else
                    return schedule.getSchedule()[currentDay].get(7);
            case 14:
                if (calendar.get(Calendar.MINUTE) < 30)
                    return schedule.getSchedule()[currentDay].get(7);
                else
                    break;
        }
        return "";
    }

    public void cancel() {
        setStateRunning();
    }

    private void setStateRunning() {
        state = State.RUNNING;
        onCreateOptionsMenu(optionsMenu);
        checkMenuItem(currentDay);
        navigationMenu.findItem(R.id.nav_next_week).setVisible(true);
        navigationMenu.findItem(R.id.nav_previous_week).setVisible(true);
        navigationMenu.findItem(R.id.nav_home_work).setVisible(true);
        listView.setVisibility(ListView.VISIBLE);
        setMenuItemsText();
    }

    public enum State {
        CREATING_SCHEDULE, RUNNING, HOME_WORK;
    }

}
