package com.dmitry_simakov.gymlab.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.dmitry_simakov.gymlab.database.DatabaseContract.ExercisesEntry;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String LOG_TAG = DatabaseHelper.class.getSimpleName();

    private static final class BM extends DatabaseContract.BodyMeasurementsEntry{}

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
        contentValues.put(ExercisesEntry.NAME, name);
        contentValues.put(ExercisesEntry.MAIN_MUSCLE_ID, majorMuscles);
        contentValues.put(ExercisesEntry.DESCRIPTION, description);

        sInstance.getWritableDatabase().insert(ExercisesEntry.TABLE_NAME, null, contentValues);
    }

    public static void insertMeasurement(String date, int body_parameter_id, double value) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(BM.DATE, date);
        contentValues.put(BM.BODY_PARAMETER_ID, body_parameter_id);
        contentValues.put(BM.VALUE, value);

        sInstance.getWritableDatabase().insert(BM.TABLE_NAME, null, contentValues);
    }

    public static void updateMeasurement(int id, String date, double value) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(BM.DATE, date);
        contentValues.put(BM.VALUE, value);

        sInstance.getWritableDatabase().update(BM.TABLE_NAME, contentValues, BM._ID +" = ?", new String[]{ String.valueOf(id) });
    }

    public static void deleteMeasurement(int id) {
        sInstance.getWritableDatabase().delete(BM.TABLE_NAME, BM._ID +" = ?", new String[]{ String.valueOf(id) });
    }
}
