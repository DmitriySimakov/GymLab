package com.dmitry_simakov.gymlab;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dmitry_simakov.gymlab.database.DbContract.ExercisesEntry;
import com.dmitry_simakov.gymlab.database.DbHelper;

public class ExerciseDescriptionActivity extends AppCompatActivity {

    private static final String LOG_TAG = ExerciseDescriptionActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_description);

        TextView mExerciseNameTextView = findViewById(R.id.exercise_name);
        ImageView mImageView = findViewById(R.id.images);
        TextView mMainMuscleTextView = findViewById(R.id.main_muscle);
        TextView mMechanicsTypeTextView = findViewById(R.id.mechanics_type);
        TextView mDescriptionTextView = findViewById(R.id.description);
        TextView mTechniqueTextView = findViewById(R.id.technique);


        int exerciseId = (Integer)getIntent().getExtras().get(ExercisesEntry._ID);

        try {
            DbHelper mDbHelper = new DbHelper(this);
            SQLiteDatabase db = mDbHelper.getWritableDatabase();

            String[] projection = {
                    ExercisesEntry.COLUMN_NAME,
                    ExercisesEntry.COLUMN_IMAGE,
                    ExercisesEntry.COLUMN_MAIN_MUSCLE_ID,
                    ExercisesEntry.COLUMN_MECHANICS_TYPE,
                    ExercisesEntry.COLUMN_DESCRIPTION,
                    ExercisesEntry.COLUMN_TECHNIQUE };

            Cursor cursor = db.query(ExercisesEntry.TABLE_NAME,
                    projection,
                    "_id = ?",
                    new String[]{Integer.toString(exerciseId)},
                    null,
                    null,
                    null);

            if (cursor.moveToFirst()) {
                int nameColumnIndex = cursor.getColumnIndex(ExercisesEntry.COLUMN_NAME);
                int imageColumnIndex = cursor.getColumnIndex(ExercisesEntry.COLUMN_IMAGE);
                int majorMusclesColumnIndex = cursor.getColumnIndex(ExercisesEntry.COLUMN_MAIN_MUSCLE_ID);
                int mechanicsTypeColumnIndex = cursor.getColumnIndex(ExercisesEntry.COLUMN_MECHANICS_TYPE);
                int descriptionColumnIndex = cursor.getColumnIndex(ExercisesEntry.COLUMN_DESCRIPTION);
                int techniqueColumnIndex = cursor.getColumnIndex(ExercisesEntry.COLUMN_TECHNIQUE);

                String name = cursor.getString(nameColumnIndex);
                String imageName = cursor.getString(imageColumnIndex);
                int majorMuscles = cursor.getInt(majorMusclesColumnIndex);
                int mechanicsType = cursor.getInt(mechanicsTypeColumnIndex);
                String description = cursor.getString(descriptionColumnIndex);
                String technique = cursor.getString(techniqueColumnIndex);

                setTitle(name);

                mExerciseNameTextView.setText(name);
                if (imageName != null) {
                    int resID = getApplicationContext().getResources().getIdentifier(imageName, "drawable", getApplicationContext().getPackageName());
                    if (resID != 0) {
                        mImageView.setImageDrawable(getApplicationContext().getResources().getDrawable(resID));
                    }
                }
                mMainMuscleTextView.setText(Integer.toString(majorMuscles));
                if (mechanicsType == 1) mMechanicsTypeTextView.setText("Базовое");
                else if (mechanicsType == 0) mMechanicsTypeTextView.setText("Изолирующее");
                mDescriptionTextView.setText(description);
                mTechniqueTextView.setText(technique);
            }
            cursor.close();
            db.close();
        } catch(SQLiteException e) {
            Toast.makeText(this, "Database unavailable", Toast.LENGTH_SHORT).show();
        }
    }
}

