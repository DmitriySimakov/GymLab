package com.dmitry_simakov.gymlab.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.dmitry_simakov.gymlab.database.DbContract.ExerciseEntry;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DbHelper extends SQLiteOpenHelper {

    private static final String LOG_TAG = DbHelper.class.getSimpleName();

    public static final String DB_NAME = "gymlab.db";
    private static final int DB_VERSION = 1;
    private static final String DB_PATH = "/data/data/com.dmitry_simakov.gymlab/databases/";

    private Context mContext;

    public DbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        Log.d(LOG_TAG, "constructor");
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(LOG_TAG, "onCreate");
    }

    private static void insertExercise(
            SQLiteDatabase db, String name, int majorMuscles, String description) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ExerciseEntry.COLUMN_NAME, name);
        contentValues.put(ExerciseEntry.COLUMN_MUSCLE_TARGETED, majorMuscles);
        contentValues.put(ExerciseEntry.COLUMN_DESCRIPTION, description);

        db.insert(ExerciseEntry.TABLE_NAME, null, contentValues);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(LOG_TAG, "onUpgrade");
        copyDatabase(mContext);
    }

    public boolean copyDatabase(Context context) {
        try {
            InputStream inputStream = context.getAssets().open("databases/" + DB_NAME);
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
}
