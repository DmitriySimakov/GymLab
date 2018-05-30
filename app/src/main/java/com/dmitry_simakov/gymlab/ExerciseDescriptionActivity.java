package com.dmitry_simakov.gymlab;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.dmitry_simakov.gymlab.database.DbContract.ExerciseEntry;
import com.dmitry_simakov.gymlab.database.DbHelper;

public class ExerciseDescriptionActivity extends AppCompatActivity {

    private static final String LOG_TAG = ExerciseDescriptionActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_description);

        TextView mExerciseNameTextView = findViewById(R.id.exercise_name);
        TextView mMajorMusclesTextView = findViewById(R.id.major_muscles);
        TextView mDescriptionTextView = findViewById(R.id.description);


        int exerciseId = (Integer)getIntent().getExtras().get(ExerciseEntry._ID);

        try {
            DbHelper mDbHelper = new DbHelper(this);
            SQLiteDatabase db = mDbHelper.getWritableDatabase();

            String[] projection = {
                    ExerciseEntry.COLUMN_NAME,
                    ExerciseEntry.COLUMN_MUSCLE_TARGETED,
                    ExerciseEntry.COLUMN_DESCRIPTION };

            Cursor cursor = db.query(ExerciseEntry.TABLE_NAME,
                    projection,
                    "_id = ?",
                    new String[]{Integer.toString(exerciseId)},
                    null,
                    null,
                    null);

            if (cursor.moveToFirst()) {
                int nameColumnIndex = cursor.getColumnIndex(ExerciseEntry.COLUMN_NAME);
                int majorMusclesColumnIndex = cursor.getColumnIndex(ExerciseEntry.COLUMN_MUSCLE_TARGETED);
                int descriptionColumnIndex = cursor.getColumnIndex(ExerciseEntry.COLUMN_DESCRIPTION);

                String name = cursor.getString(nameColumnIndex);
                int majorMuscles = cursor.getInt(majorMusclesColumnIndex);
                String description = cursor.getString(descriptionColumnIndex);

                setTitle(name);

                mExerciseNameTextView.setText(name);
                mMajorMusclesTextView.setText(Integer.toString(majorMuscles));
                mDescriptionTextView.setText(description);
            }
            cursor.close();
            db.close();
        } catch(SQLiteException e) {
            Toast.makeText(this, "Database unavailable", Toast.LENGTH_SHORT).show();
        }
    }
}

