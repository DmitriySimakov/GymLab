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
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.dmitry_simakov.gymlab.database.DbContract.*;
import com.dmitry_simakov.gymlab.database.DbHelper;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private SQLiteDatabase mDatabase;
    private DbHelper mDbHelper;
    private Cursor mCursor;
    private ExerciseCursorTreeAdapter mCursorAdapter;
    private ExpandableListView mExerciseElv;

    public static final String APP_PREFERENCES = "AppPreferences";
    public static final String IS_DB_COPIED = "isDbCopied";
    SharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPreferences = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        initElv();
        new LoadExercisesTask().execute();
    }

    private void initElv() {
        mExerciseElv = findViewById(R.id.elv);

        ProgressBar progressBar = findViewById(R.id.progressBar);
        mExerciseElv.setEmptyView(progressBar);

        mExerciseElv.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v,int groupPosition, int childPosition, long id) {
                Intent intent = new Intent(MainActivity.this, ExerciseDescriptionActivity.class);
                intent.putExtra(ExerciseEntry._ID, (int)id);
                startActivity(intent);
                /* Если мы возвращаем true – это значит, мы сообщаем, что сами полностью обработали
                событие и оно не пойдет в дальнейшие обработчики (если они есть).
                Если возвращаем false – значит, мы позволяем событию идти дальше. */
                return true;
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

            mDbHelper = new DbHelper(MainActivity.this);
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

            String[] projection = {MuscleEntry._ID, MuscleEntry.COLUMN_NAME};

            mCursor = mDatabase.query(MuscleEntry.TABLE_NAME, projection,
                    null, null, null, null, null);

            return true;
        }

        protected void onPostExecute(Boolean param) {
            Log.d(LOG_TAG, "onPostExecute");

            super.onPostExecute(param);
            if(param) {
                String[] groupFrom = { MuscleEntry.COLUMN_NAME };
                int[] groupTo = { android.R.id.text1 };
                String[] childFrom = { ExerciseEntry.COLUMN_NAME };
                int[] childTo = { android.R.id.text1 };

                mCursorAdapter = new ExerciseCursorTreeAdapter(MainActivity.this, mDatabase, mCursor,
                        android.R.layout.simple_expandable_list_item_1, groupFrom, groupTo,
                        android.R.layout.simple_list_item_1, childFrom, childTo);

                mExerciseElv.setAdapter(mCursorAdapter);
            } else {
                Toast.makeText(MainActivity.this, "Проблемы с загрузкой базы данных", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
