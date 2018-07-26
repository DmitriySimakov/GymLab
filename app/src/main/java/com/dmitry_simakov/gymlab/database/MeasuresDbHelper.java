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

    private static class BM extends DbContract.BodyMeasurementsEntry{}
    private static class BP extends DbContract.BodyParametersEntry {}

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

        db.execSQL("CREATE TABLE "+ BP.TABLE_NAME +" ("+
                BP._ID +" INTEGER PRIMARY KEY AUTOINCREMENT, "+
                BP.NAME +" TEXT NOT NULL UNIQUE, "+
                BP.IMAGE +" TEXT, "+
                BP.INSTRUCTION +" TEXT);"
        );

        db.execSQL("CREATE TABLE "+ BM.TABLE_NAME +" ("+
                BM._ID +" INTEGER PRIMARY KEY AUTOINCREMENT, "+
                BM.DATE +" TEXT NOT NULL, "+
                BM.BODY_PARAMETER_ID +" INTEGER NOT NULL, " +
                BM.VALUE +" INTEGER NOT NULL, " +
                "FOREIGN KEY("+ BM.BODY_PARAMETER_ID +") " +
                "REFERENCES "+ BP.TABLE_NAME +"("+ BP._ID +"));"
        );

        insertBodyParameter(db, "Рост", null, null);
        insertBodyParameter(db, "Вес", null, null);
        insertBodyParameter(db, "Шея", null, null);
        insertBodyParameter(db, "Бицепс", null, "Согните руку в локте на 90 градусов. Максимально напрягите бицепс. Измерьте самую выступающую часть.");
        insertBodyParameter(db, "Предплечье", null, null);
        insertBodyParameter(db, "Грудь", null, null);
        insertBodyParameter(db, "Талия", null, null);
        insertBodyParameter(db, "Таз", null, null);
        insertBodyParameter(db, "Бедро", null, null);
        insertBodyParameter(db, "Голень", null, null);

        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        insertMeasurement(db, date, 1, 175);
        insertMeasurement(db, date, 2, 75);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public static void insertMeasurement(SQLiteDatabase db, String date, int body_parameter_id, int value) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(BM.DATE, date);
        contentValues.put(BM.BODY_PARAMETER_ID, body_parameter_id);
        contentValues.put(BM.VALUE, value);
        db.insert(BM.TABLE_NAME, null, contentValues);
    }

    private static void insertBodyParameter(SQLiteDatabase db, String name, String image, String instruction) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(BP.NAME, name);
        contentValues.put(BP.IMAGE, image);
        contentValues.put(BP.INSTRUCTION, instruction);
        db.insert(BP.TABLE_NAME, null, contentValues);
    }

}
