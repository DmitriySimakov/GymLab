package com.dmitry_simakov.gymlab;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.dmitry_simakov.gymlab.data.ExerciseContract.ExerciseEntry;
import com.dmitry_simakov.gymlab.data.ExerciseDbHelper;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private SQLiteDatabase mDatabase;
    private ExerciseDbHelper mDbHelper;
    private Cursor mCursor;
    private ListView mExerciseListView;
    private ExerciseCursorAdapter mCursorAdapter;

    public static final String APP_PREFERENCES = "AppPreferences";
    public static final String IS_DB_COPIED = "isDbCopied";
    SharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPreferences = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        mExerciseListView = findViewById(R.id.list);
        ProgressBar progressBar = findViewById(R.id.progressBar);
        mExerciseListView.setEmptyView(progressBar);

        mCursorAdapter = new ExerciseCursorAdapter(this, null);
        mExerciseListView.setAdapter(mCursorAdapter);
        new LoadExercisesTask().execute();

        mExerciseListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, ExerciseDescriptionActivity.class);
                intent.putExtra(ExerciseEntry._ID, (int)id);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        if (mCursor != null) mCursor.close();
        if (mDbHelper != null) mDatabase.close();
    }

    private class LoadExercisesTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            Log.d(LOG_TAG, "doInBackground");

            mDbHelper = new ExerciseDbHelper(MainActivity.this);
            mDatabase = mDbHelper.getReadableDatabase();

            //Копируем базу данных при первом запуске приложение
            if(!mPreferences.contains(IS_DB_COPIED)) {
                boolean success = mDbHelper.copyDatabase(MainActivity.this);
                if(success) {
                    SharedPreferences.Editor editor = mPreferences.edit();
                    editor.putBoolean(IS_DB_COPIED, true);
                    editor.apply();
                } else {
                    return false;
                }
            }

            String[] projection = {
                    ExerciseEntry._ID,
                    ExerciseEntry.COLUMN_EXERCISE_NAME };

            mCursor = mDatabase.query(
                    ExerciseEntry.TABLE_NAME,
                    projection,
                    null,
                    null,
                    null,
                    null,
                    null);

            return true;
        }

        protected void onPostExecute(Boolean param) {
            Log.d(LOG_TAG, "onPostExecute");

            super.onPostExecute(param);
            if(param) {
                mCursorAdapter.changeCursor(mCursor);
            } else {
                Toast.makeText(MainActivity.this, "Проблемы с загрузкой базы данных", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
