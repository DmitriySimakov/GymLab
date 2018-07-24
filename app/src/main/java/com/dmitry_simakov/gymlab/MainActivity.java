package com.dmitry_simakov.gymlab;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
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
import android.widget.Toast;

import com.dmitry_simakov.gymlab.database.DbHelper;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    public static final String APP_PREFERENCES = "AppPreferences";
    public static final String IS_DB_COPIED = "isDbCopied";
    SharedPreferences mPreferences;

    private DrawerLayout mDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Init Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Init DrawerLayout
        mDrawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();

        // Init NavigationView
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mPreferences = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        initDB();

        setFragment(new ExerciseListFragment(), navigationView.getMenu().findItem(R.id.nav_exercises));
    }

    private void initDB() {
        Log.d(LOG_TAG, "initDB");

        //Копируем базу данных при первом запуске приложение
        if(!mPreferences.contains(IS_DB_COPIED)) {
            DbHelper dbHelper = new DbHelper(this);
            SQLiteDatabase database = dbHelper.getReadableDatabase();
            boolean success = dbHelper.copyDatabase(this);
            if(success) {
                SharedPreferences.Editor editor = mPreferences.edit();
                editor.putBoolean(IS_DB_COPIED, true);
                editor.apply();
            } else {
                Toast.makeText(this, "Проблемы с загрузкой базы данных", Toast.LENGTH_SHORT).show();
            }
            database.close();
        }
    }

    @Override
    public void onBackPressed() {
        Log.d(LOG_TAG, "onBackPressed");

        mDrawer = findViewById(R.id.drawer_layout);
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(LOG_TAG, "onCreateOptionsMenu");

        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(LOG_TAG, "onOptionsItemSelected");

        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Log.d(LOG_TAG, "onNavigationItemSelected");

        switch (item.getItemId()) {
            case R.id.nav_exercises:
                setFragment(new ExerciseListFragment(), item);
                break;
            case R.id.nav_training_programs:
                setFragment(new TrainingProgramsFragment(), item);
                break;
            case R.id.nav_measures:
                setFragment(new MeasuresFragment(), item);
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

    private void setFragment(Fragment fragment, MenuItem item) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit();
        item.setChecked(true); // Выделяем выбранный пункт меню в шторке
        setTitle(item.getTitle()); // Выводим выбранный пункт в заголовке
    }
}
