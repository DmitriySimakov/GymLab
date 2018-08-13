package com.dmitry_simakov.gymlab;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dmitry_simakov.gymlab.database.DatabaseHelper;
import com.dmitry_simakov.gymlab.exercises.ExercisesListFragment;
import com.dmitry_simakov.gymlab.measurements.MeasurementsTabFragment;
import com.dmitry_simakov.gymlab.training_programs.TrainingProgramsFragment;
import com.dmitry_simakov.gymlab.training_sessions.OnTrainingStateChangeListener;
import com.dmitry_simakov.gymlab.training_sessions.TrainingSessionDialog;
import com.dmitry_simakov.gymlab.training_sessions.TrainingSessionsFragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        FragmentManager.OnBackStackChangedListener,
        OnTrainingStateChangeListener {

    public static final String CLASS_NAME = MainActivity.class.getSimpleName();

    public static final String APP_PREFERENCES = "app_preferences";
    public static final String IS_DARK_THEME = "is_dark_theme";
    public static final String DB_WAS_COPIED = "db_was_copied";
    public static final String START_MILLIS = "start_millis";
    public static final String TIMER_IS_RUNNING = "timer_is_running";
    private SharedPreferences mPreferences;

    private DrawerLayout mDrawer;
    private Toolbar mToolbar;
    private ActionBarDrawerToggle mToggle;

    private Timer mTimer;
    private long mStartMillis;
    private boolean mTimerIsRunning;
    private LinearLayout mTimerPanel;
    private TextView mDurationTV, mRestTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(CLASS_NAME, "onCreate");

        mPreferences = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        if (mPreferences.getBoolean(IS_DARK_THEME, false)) {
            setTheme(R.style.AppThemeDark);
        }

        setContentView(R.layout.activity_main);

        // Init Toolbar
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        // Init DrawerLayout
        mDrawer = findViewById(R.id.drawer_layout);
        mToggle = new ActionBarDrawerToggle(
                this, mDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(mToggle);
        mToggle.syncState();

        // Init NavigationView
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        initDB();
        initTimer();

        getSupportFragmentManager().addOnBackStackChangedListener(this);
        setFragment(new TrainingSessionsFragment(), navigationView.getMenu().findItem(R.id.nav_training_sessions));
    }

    private void initDB() {
        Log.d(CLASS_NAME, "initDB");

        // Copy database at the first launch
        if (!mPreferences.contains(DB_WAS_COPIED)) {
            DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
            dbHelper.getReadableDatabase(); // Need to open a connection
            if (dbHelper.copyDatabase()) {
                mPreferences.edit().putBoolean(DB_WAS_COPIED, true).apply();
            } else {
                Toast.makeText(this, "Проблемы с загрузкой базы данных", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initTimer() {
        mTimerPanel = findViewById(R.id.timer_panel);
        mDurationTV = findViewById(R.id.duration);
        mRestTV = findViewById(R.id.rest);

        mStartMillis = mPreferences.getLong(START_MILLIS, 0);
        mTimerIsRunning = mPreferences.getBoolean(TIMER_IS_RUNNING, false);

        if (!mTimerIsRunning) {
            mTimerPanel.setVisibility(View.GONE);
        }
    }

    private void setFragment(Fragment fragment, MenuItem item) {
        Log.d(CLASS_NAME, "setFragment");

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
        item.setChecked(true); // Выделяем выбранный пункт меню в шторке
        setTitle(item.getTitle()); // Выводим выбранный пункт в заголовке
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mTimerIsRunning) {
            runTimer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mTimerIsRunning) {
            stopTimer();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(CLASS_NAME, "onCreateOptionsMenu");
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(CLASS_NAME, "onOptionsItemSelected");

        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Log.d(CLASS_NAME, "onNavigationItemSelected");

        switch (item.getItemId()) {
            case R.id.nav_exercises:
                setFragment(new ExercisesListFragment(), item);
                break;
            case R.id.nav_training_programs:
                setFragment(new TrainingProgramsFragment(), item);
                break;
            case R.id.nav_training_sessions:
                setFragment(new TrainingSessionsFragment(), item);
                break;
            case R.id.nav_measures:
                setFragment(new MeasurementsTabFragment(), item);
                break;
            case R.id.nav_settings:
                mPreferences.edit()
                        .putBoolean(IS_DARK_THEME, !mPreferences.getBoolean(IS_DARK_THEME, false))
                        .apply();
                recreate();
                break;
            case R.id.nav_info:
                break;
        }

        mDrawer = findViewById(R.id.drawer_layout);
        mDrawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackStackChanged() {
        Log.d(CLASS_NAME, "onBackStackChanged");

        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // show back button
            mToolbar.setNavigationOnClickListener(v -> onBackPressed());
        } else {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false); //show hamburger
            mToggle.syncState();
            mToolbar.setNavigationOnClickListener(v -> mDrawer.openDrawer(GravityCompat.START));
            setTitle(R.string.exercises);
        }
    }

    @Override
    public void onBackPressed() {
        Log.d(CLASS_NAME, "onBackPressed");

        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onStartTrainingSession(String dateTime) {
        try {
            mStartMillis = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateTime).getTime();
            runTimer();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onFinishTrainingSession() {
        stopTimer();
        return (int) ((new Date().getTime() - mStartMillis) / 60000); // return duration in minutes
    }

    private void runTimer() {
        mTimerIsRunning = true;
        mPreferences.edit()
                .putLong(START_MILLIS, mStartMillis)
                .putBoolean(TIMER_IS_RUNNING, true)
                .apply();

        mTimerPanel.setVisibility(View.VISIBLE);
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    long diffMillis = new Date().getTime() - mStartMillis;
                    int hours   = (int) (diffMillis / (1000 * 60 * 60)) % 24;
                    int minutes = (int) (diffMillis / (1000 * 60)) % 60;
                    int seconds = (int) (diffMillis / 1000) % 60;
                    mDurationTV.setText(hours +":"+ minutes +":"+ seconds);
                });
            }
        },0,1000);
    }

    private void stopTimer() {
        mTimerIsRunning = false;
        mPreferences.edit()
                .putBoolean(TIMER_IS_RUNNING, false)
                .apply();

        mTimerPanel.setVisibility(View.GONE);
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }
}
