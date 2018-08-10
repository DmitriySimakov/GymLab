package com.dmitry_simakov.gymlab.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.dmitry_simakov.gymlab.database.DatabaseContract.*;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String LOG_TAG = DatabaseHelper.class.getSimpleName();

    private static final String DB_NAME = "gymlab.db";
    private static final int DB_VERSION = 1;
    private static final String DB_PATH = "/data/data/com.dmitry_simakov.gymlab/databases/";

    private static DatabaseHelper sInstance = null; // No memory leaks cuz we put the ApplicationContext
    private Context mContext;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    private DatabaseHelper(Context appContext) {
        super(appContext, DB_NAME, null, DB_VERSION);
        Log.d(LOG_TAG, "constructor");
        mContext = appContext;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(LOG_TAG, "onCreate");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(LOG_TAG, "onUpgrade");
    }

    public boolean copyDatabase() {
        try {
            InputStream inputStream = mContext.getAssets().open("databases/" + DB_NAME);
            String outFileName = DB_PATH + DB_NAME;
            OutputStream outputStream = new FileOutputStream(outFileName);
            byte[] buff = new byte[1024];
            int length = 0;
            while ((length = inputStream.read(buff)) > 0) {
                outputStream.write(buff, 0, length);
            }
            inputStream.close();
            outputStream.flush();
            outputStream.close();

            Log.d(LOG_TAG, "DB copied");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static long insertExercise(String name, int majorMuscles, String description) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ExerciseEntry.NAME, name);
        contentValues.put(ExerciseEntry.MAIN_MUSCLE_ID, majorMuscles);
        contentValues.put(ExerciseEntry.DESCRIPTION, description);
        return sInstance.getWritableDatabase().insert(ExerciseEntry.TABLE_NAME, null, contentValues);
    }

    public static long insertTrainingSession(ContentValues cv) {
        return sInstance.getWritableDatabase().insert(TrainingSessionEntry.TABLE_NAME, null, cv);
    }

    public static void finishTrainingSession(int id, int duration) {
        ContentValues cv = new ContentValues();
        cv.put(TrainingSessionEntry.DURATION, duration);
        sInstance.getWritableDatabase().update(TrainingSessionEntry.TABLE_NAME, cv,
                TrainingSessionEntry._ID +" = ?", new String[]{ String.valueOf(id) });
    }

    public static long insertExerciseIntoSession(int session_id, int exercise_id, int number, int paramsBoolArr) {
        ContentValues cv = new ContentValues();
        cv.put(TrainingSessionExerciseEntry.SESSION_ID, session_id);
        cv.put(TrainingSessionExerciseEntry.EXERCISE_ID, exercise_id);
        cv.put(TrainingSessionExerciseEntry.NUMBER, number);
        cv.put(TrainingSessionExerciseEntry.PARAMS_BOOL_ARR, paramsBoolArr);
        return sInstance.getWritableDatabase().insert(TrainingSessionExerciseEntry.TABLE_NAME, null, cv);
    }

    public static long insertSet(int exercise_id, int secsSinceStart, int weight, int reps, int time, int distance) {
        ContentValues cv = new ContentValues();
        cv.put(TrainingSessionSetEntry.EXERCISE_ID, exercise_id);
        cv.put(TrainingSessionSetEntry.SECS_SINCE_START, secsSinceStart);
        cv.put(TrainingSessionSetEntry.WEIGHT, weight);
        cv.put(TrainingSessionSetEntry.REPS, reps);
        cv.put(TrainingSessionSetEntry.TIME, time);
        cv.put(TrainingSessionSetEntry.DISTANCE, distance);
        return sInstance.getWritableDatabase().insert(TrainingSessionSetEntry.TABLE_NAME, null, cv);
    }

    public static void updateSet(int id, int weight, int reps, int time, int distance) {
        ContentValues cv = new ContentValues();
        cv.put(TrainingSessionSetEntry.WEIGHT, weight);
        cv.put(TrainingSessionSetEntry.REPS, reps);
        cv.put(TrainingSessionSetEntry.TIME, time);
        cv.put(TrainingSessionSetEntry.DISTANCE, distance);
        sInstance.getWritableDatabase().update(TrainingSessionSetEntry.TABLE_NAME, cv,
                TrainingSessionSetEntry._ID +" = ?", new String[]{ String.valueOf(id) });
    }

    public static void deleteSet(int id) {
        sInstance.getWritableDatabase().delete(TrainingSessionSetEntry.TABLE_NAME,
                TrainingSessionSetEntry._ID +" = ?", new String[]{ String.valueOf(id) });
    }

    public static long insertMeasurement(String date, int body_parameter_id, double value) {
        ContentValues cv = new ContentValues();
        cv.put(BodyMeasurementEntry.DATE, date);
        cv.put(BodyMeasurementEntry.BODY_PARAM_ID, body_parameter_id);
        cv.put(BodyMeasurementEntry.VALUE, value);
        return sInstance.getWritableDatabase().insert(BodyMeasurementEntry.TABLE_NAME, null, cv);
    }

    public static void updateMeasurement(int id, String date, double value) {
        ContentValues cv = new ContentValues();
        cv.put(BodyMeasurementEntry.DATE, date);
        cv.put(BodyMeasurementEntry.VALUE, value);
        sInstance.getWritableDatabase().update(BodyMeasurementEntry.TABLE_NAME, cv,
                BodyMeasurementEntry._ID +" = ?", new String[]{ String.valueOf(id) });
    }

    public static void deleteMeasurement(int id) {
        sInstance.getWritableDatabase().delete(BodyMeasurementEntry.TABLE_NAME,
                BodyMeasurementEntry._ID +" = ?", new String[]{ String.valueOf(id) });
    }
}
