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

import com.dmitry_simakov.gymlab.database.DbHelper;

import java.util.ArrayList;
import java.util.StringJoiner;

public class ExerciseDescriptionActivity extends AppCompatActivity {

    private static final String LOG_TAG = ExerciseDescriptionActivity.class.getSimpleName();

    private static class ExE extends com.dmitry_simakov.gymlab.database.DbContract.ExercisesEntry{}
    private static class ME extends com.dmitry_simakov.gymlab.database.DbContract.MusclesEntry{}
    private static class TME extends com.dmitry_simakov.gymlab.database.DbContract.TargetedMusclesEntry{}
    private static class MTE extends com.dmitry_simakov.gymlab.database.DbContract.MechanicsTypesEntry{}
    private static class ETE extends com.dmitry_simakov.gymlab.database.DbContract.ExerciseTypesEntry{}
    private static class EqE extends com.dmitry_simakov.gymlab.database.DbContract.EquipmentEntry{}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_description);

        TextView mExerciseNameTextView = findViewById(R.id.exercise_name);
        ImageView mImageView = findViewById(R.id.images);
        TextView mMainMuscleTextView = findViewById(R.id.main_muscle);
        TextView mTargetedMusclesTextView = findViewById(R.id.targeted_muscles);
        TextView mMechanicsTypeTextView = findViewById(R.id.mechanics_type);
        TextView mExerciseTypeTextView = findViewById(R.id.exercise_type);
        TextView mEquipmentTextView = findViewById(R.id.equipment);
        TextView mDescriptionTextView = findViewById(R.id.description);
        TextView mTechniqueTextView = findViewById(R.id.technique);


        int exerciseId = (Integer)getIntent().getExtras().get(ExE._ID);

        try {
            DbHelper mDbHelper = new DbHelper(this);
            SQLiteDatabase db = mDbHelper.getWritableDatabase();

            Cursor cursor = db.rawQuery(
                    "SELECT "+ ExE.NAME +", "+
                            ExE.IMAGE +", "+
                            "(SELECT "+ ME.NAME  +" FROM "+ ME.TABLE_NAME  +" WHERE "+ ME._ID  +" = "+ ExE.MAIN_MUSCLE_ID    +") AS "+ ExE.MAIN_MUSCLE    +", "+
                            "(SELECT "+ MTE.NAME +" FROM "+ MTE.TABLE_NAME +" WHERE "+ MTE._ID +" = "+ ExE.MECHANICS_TYPE_ID +") AS "+ ExE.MECHANICS_TYPE +", "+
                            "(SELECT "+ ETE.NAME +" FROM "+ ETE.TABLE_NAME +" WHERE "+ ETE._ID +" = "+ ExE.EXERCISE_TYPE_ID  +") AS "+ ExE.EXERCISE_TYPE  +", "+
                            "(SELECT "+ EqE.NAME +" FROM "+ EqE.TABLE_NAME +" WHERE "+ EqE._ID +" = "+ ExE.EQUIPMENT_ID      +") AS "+ ExE.EQUIPMENT      +", "+
                            ExE.DESCRIPTION +", "+
                            ExE.TECHNIQUE +" "+
                            "FROM "+ ExE.TABLE_NAME +" "+
                            "WHERE "+ ExE._ID +" = ?",
                    new String[]{Integer.toString(exerciseId)});

            if (cursor.moveToFirst()) {
                int nameColumnIndex = cursor.getColumnIndex(ExE.NAME);
                int imageColumnIndex = cursor.getColumnIndex(ExE.IMAGE);
                int mainMuscleColumnIndex = cursor.getColumnIndex(ExE.MAIN_MUSCLE);
                int mechanicsTypeColumnIndex = cursor.getColumnIndex(ExE.MECHANICS_TYPE);
                int exerciseTypeColumnIndex = cursor.getColumnIndex(ExE.EXERCISE_TYPE);
                int equipmentColumnIndex = cursor.getColumnIndex(ExE.EQUIPMENT);
                int descriptionColumnIndex = cursor.getColumnIndex(ExE.DESCRIPTION);
                int techniqueColumnIndex = cursor.getColumnIndex(ExE.TECHNIQUE);

                String name = cursor.getString(nameColumnIndex);
                String imageName = cursor.getString(imageColumnIndex);
                String mainMuscle = cursor.getString(mainMuscleColumnIndex);
                String mechanicsType = cursor.getString(mechanicsTypeColumnIndex);
                String exerciseType = cursor.getString(exerciseTypeColumnIndex);
                String equipment = cursor.getString(equipmentColumnIndex);
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
                mMainMuscleTextView.setText(mainMuscle);
                mMechanicsTypeTextView.setText(mechanicsType);
                mExerciseTypeTextView.setText(exerciseType);
                mEquipmentTextView.setText(equipment);
                mDescriptionTextView.setText(description);
                mTechniqueTextView.setText(technique);
            }

            cursor = db.rawQuery("SELECT (SELECT "+ ME.NAME +" FROM "+ ME.TABLE_NAME +" WHERE "+ ME._ID +" = "+ TME.MUSCLE_ID +") AS "+ TME.MUSCLE +" " +
                            "FROM "+ TME.TABLE_NAME +" " +
                            "WHERE "+ TME.EXERCISE_ID +" = ?",
                    new String[]{Integer.toString(exerciseId)});

            if (cursor.moveToFirst()) {
                int muscleColumnIndex = cursor.getColumnIndex(TME.MUSCLE);
                StringBuilder sb = new StringBuilder();
                do {
                    sb.append(cursor.getString(muscleColumnIndex));
                    sb.append(", ");
                } while (cursor.moveToNext());
                sb.setLength(sb.length() - 2); // Delete last delimiter

                mTargetedMusclesTextView.setText(sb.toString());
            }

            cursor.close();
            db.close();
        } catch(SQLiteException e) {
            Toast.makeText(this, "Database unavailable", Toast.LENGTH_SHORT).show();
        }
    }
}

