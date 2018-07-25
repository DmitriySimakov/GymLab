package com.dmitry_simakov.gymlab.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MeasuresDbHelper extends SQLiteOpenHelper {

    private static final String LOG_TAG = GymLabDbHelper.class.getSimpleName();

    private static class BME extends com.dmitry_simakov.gymlab.database.DbContract.BodyMeasurementsEntry{}
    private static class BPE extends DbContract.BodyParametersEntry {}

    public static final String DB_NAME = "body_measurements.db";
    private static final int DB_VERSION = 1;

    private Context mContext;

    public MeasuresDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);

        Log.d(LOG_TAG, "constructor");
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(LOG_TAG, "onCreate");

        db.execSQL("CREATE TABLE "+ BPE.TABLE_NAME +" ("+
                BPE._ID +" INTEGER PRIMARY KEY AUTOINCREMENT, "+
                BPE.NAME +" TEXT NOT NULL UNIQUE);"
        );

        db.execSQL("CREATE TABLE "+ BME.TABLE_NAME +" ("+
                BME._ID +" INTEGER PRIMARY KEY AUTOINCREMENT, "+
                BME.DATE +" TEXT NOT NULL, "+
                BME.BODY_PARAMETER_ID +" INTEGER NOT NULL, " +
                BME.VALUE +" INTEGER NOT NULL, " +
                "FOREIGN KEY("+ BME.BODY_PARAMETER_ID +") " +
                "REFERENCES "+ BPE.TABLE_NAME +"("+ BPE._ID +"));"
        );

        insertBodyParameter(db, "Рост");
        insertBodyParameter(db, "Вес");
        insertBodyParameter(db, "Шея");
        insertBodyParameter(db, "Бицепс");
        insertBodyParameter(db, "Предплечье");
        insertBodyParameter(db, "Грудь");
        insertBodyParameter(db, "Талия");
        insertBodyParameter(db, "Таз");
        insertBodyParameter(db, "Бедро");
        insertBodyParameter(db, "Голень");

        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        insertMeasurement(db, date, 1, 175);
        insertMeasurement(db, date, 2, 75);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public static void insertMeasurement(SQLiteDatabase db, String date, int body_parameter_id, int value) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(BME.DATE, date);
        contentValues.put(BME.BODY_PARAMETER_ID, body_parameter_id);
        contentValues.put(BME.VALUE, value);
        db.insert(BME.TABLE_NAME, null, contentValues);
    }

    private static void insertBodyParameter(SQLiteDatabase db, String name) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(BPE.NAME, name);
        db.insert(BPE.TABLE_NAME, null, contentValues);
    }

}
