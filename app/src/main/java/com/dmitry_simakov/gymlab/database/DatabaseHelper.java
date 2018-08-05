package com.dmitry_simakov.gymlab.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.dmitry_simakov.gymlab.database.DatabaseContract.ExerciseEntry;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String LOG_TAG = DatabaseHelper.class.getSimpleName();

    private static final class TSS extends DatabaseContract.TrainingSessionSetEntry {}
    private static final class BM extends DatabaseContract.BodyMeasurementEntry {}

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

    private static void insertExercise(String name, int majorMuscles, String description) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ExerciseEntry.NAME, name);
        contentValues.put(ExerciseEntry.MAIN_MUSCLE_ID, majorMuscles);
        contentValues.put(ExerciseEntry.DESCRIPTION, description);

        sInstance.getWritableDatabase().insert(ExerciseEntry.TABLE_NAME, null, contentValues);
    }

    public static void insertSet(int exercise_id, int secsSinceStart, int weight, int reps, int time, int distance) {
        ContentValues cv = new ContentValues();
        cv.put(TSS.EXERCISE_ID, exercise_id);
        cv.put(TSS.SECS_SINCE_START, secsSinceStart);
        cv.put(TSS.WEIGHT, weight);
        cv.put(TSS.REPS, reps);
        cv.put(TSS.TIME, time);
        cv.put(TSS.DISTANCE, distance);
        sInstance.getWritableDatabase().insert(TSS.TABLE_NAME, null, cv);
    }

    public static void updateSet(int id, int weight, int reps, int time, int distance) {
        ContentValues cv = new ContentValues();
        cv.put(TSS.WEIGHT, weight);
        cv.put(TSS.REPS, reps);
        cv.put(TSS.TIME, time);
        cv.put(TSS.DISTANCE, distance);
        sInstance.getWritableDatabase().update(TSS.TABLE_NAME, cv, TSS._ID +" = ?", new String[]{ String.valueOf(id) });
    }

    public static void deleteSet(int id) {
        sInstance.getWritableDatabase().delete(TSS.TABLE_NAME, TSS._ID +" = ?", new String[]{ String.valueOf(id) });
    }

    public static void insertMeasurement(String date, int body_parameter_id, double value) {
        ContentValues cv = new ContentValues();
        cv.put(BM.DATE, date);
        cv.put(BM.BODY_PARAM_ID, body_parameter_id);
        cv.put(BM.VALUE, value);
        sInstance.getWritableDatabase().insert(BM.TABLE_NAME, null, cv);
    }

    public static void updateMeasurement(int id, String date, double value) {
        ContentValues cv = new ContentValues();
        cv.put(BM.DATE, date);
        cv.put(BM.VALUE, value);
        sInstance.getWritableDatabase().update(BM.TABLE_NAME, cv, BM._ID +" = ?", new String[]{ String.valueOf(id) });
    }

    public static void deleteMeasurement(int id) {
        sInstance.getWritableDatabase().delete(BM.TABLE_NAME, BM._ID +" = ?", new String[]{ String.valueOf(id) });
    }
}
