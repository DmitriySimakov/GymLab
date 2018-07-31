package com.dmitry_simakov.gymlab;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
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
import android.widget.Toast;

import com.dmitry_simakov.gymlab.database.DatabaseHelper;
import com.dmitry_simakov.gymlab.measurements.MeasurementsTabFragment;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        FragmentManager.OnBackStackChangedListener {

    public static final String CLASS_NAME = MainActivity.class.getSimpleName();

    public static final String APP_PREFERENCES = "AppPreferences";
    public static final String IS_DB_COPIED = "isDbCopied";
    private SharedPreferences mPreferences;

    private DrawerLayout mDrawer;
    private Toolbar mToolbar;
    private ActionBarDrawerToggle mToggle;
    private FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(CLASS_NAME, "onCreate");

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

        mPreferences = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        initDB();

        mFragmentManager = getSupportFragmentManager();
        mFragmentManager.addOnBackStackChangedListener(this);
        setFragment(new ExercisesListFragment(), navigationView.getMenu().findItem(R.id.nav_exercises));
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
            case R.id.nav_measures:
                setFragment(new MeasurementsTabFragment(), item);
                break;
            case R.id.nav_share:
                break;
            case R.id.nav_send:
                break;
        }

        mDrawer = findViewById(R.id.drawer_layout);
        mDrawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackStackChanged() {
        Log.d(CLASS_NAME, "onBackStackChanged");

        if (mFragmentManager.getBackStackEntryCount() > 0) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // show back button
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        } else {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false); //show hamburger
            mToggle.syncState();
            setTitle(R.string.exercises);
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDrawer.openDrawer(GravityCompat.START);
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        Log.d(CLASS_NAME, "onBackPressed");

        mDrawer = findViewById(R.id.drawer_layout);
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void initDB() {
        Log.d(CLASS_NAME, "initDB");

        // Copy database at the first launch
        if(!mPreferences.contains(IS_DB_COPIED)) {
            DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
            dbHelper.getReadableDatabase(); // Need to open a connection
            if(dbHelper.copyDatabase()) {
                SharedPreferences.Editor editor = mPreferences.edit();
                editor.putBoolean(IS_DB_COPIED, true);
                editor.apply();
            } else {
                Toast.makeText(this, "Проблемы с загрузкой базы данных", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setFragment(Fragment fragment, MenuItem item) {
        Log.d(CLASS_NAME, "setFragment");

        mFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit();
        item.setChecked(true); // Выделяем выбранный пункт меню в шторке
        setTitle(item.getTitle()); // Выводим выбранный пункт в заголовке
    }
}
