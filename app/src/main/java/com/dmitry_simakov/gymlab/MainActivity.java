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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorTreeAdapter;
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
                intent.putExtra(ExercisesEntry._ID, (int)id);
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

            String[] projection = {
                    MusclesEntry._ID,
                    MusclesEntry.COLUMN_NAME,
                    MusclesEntry.COLUMN_IMAGE};

            mCursor = mDatabase.query(MusclesEntry.TABLE_NAME, projection,
                    null, null, null, null, null);

            return true;
        }

        protected void onPostExecute(Boolean param) {
            Log.d(LOG_TAG, "onPostExecute");

            super.onPostExecute(param);
            if(param) {
                String[] groupFrom = { MusclesEntry.COLUMN_NAME, MusclesEntry.COLUMN_IMAGE };
                int[] groupTo = { R.id.muscle_name, R.id.muscle_image };
                String[] childFrom = { ExercisesEntry.COLUMN_NAME, ExercisesEntry.COLUMN_IMAGE };
                int[] childTo = { R.id.exercise_name, R.id.exercise_image };

                mCursorAdapter = new ExerciseCursorTreeAdapter(MainActivity.this, mDatabase, mCursor,
                        R.layout.exercise_expandable_list_item, groupFrom, groupTo,
                        R.layout.exercise_list_item, childFrom, childTo);

                mCursorAdapter.setViewBinder(new SimpleCursorTreeAdapter.ViewBinder() {
                    @Override
                    public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                        if (view.getId() == R.id.muscle_image || view.getId() == R.id.exercise_image) {
                            ImageView imageView = (ImageView) view;
                            //String name = cursor.getString(1);
                            String imageName = cursor.getString(columnIndex);
                            if (imageName != null) {
                                int resID = getApplicationContext().getResources().getIdentifier(imageName, "drawable", getApplicationContext().getPackageName());
                                if (resID != 0) {
                                    imageView.setImageDrawable(getApplicationContext().getResources().getDrawable(resID));
                                    return true;
                                }
                            }

                            imageView.setImageResource(R.drawable.no_image);
                            return true;
                        }
                        return false;
                    }
                });

                mExerciseElv.setAdapter(mCursorAdapter);
            } else {
                Toast.makeText(MainActivity.this, "Проблемы с загрузкой базы данных", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
