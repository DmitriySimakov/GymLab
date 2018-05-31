package com.dmitry_simakov.gymlab;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.SimpleCursorTreeAdapter;

import com.dmitry_simakov.gymlab.database.DbContract.*;

public class ExerciseCursorTreeAdapter extends SimpleCursorTreeAdapter {

    private SQLiteDatabase mDatabase;

    public ExerciseCursorTreeAdapter(
            Context context, SQLiteDatabase db, Cursor cursor, int groupLayout, String[] groupFrom, int[] groupTo,
            int childLayout, String[] childFrom, int[] childTo) {
        super(context, cursor, groupLayout, groupFrom, groupTo,
                childLayout, childFrom, childTo);

        mDatabase = db;
    }

    @Override
    protected Cursor getChildrenCursor(Cursor groupCursor) {
        int idColumn = groupCursor.getColumnIndex(MuscleEntry._ID);
        int id = groupCursor.getInt(idColumn);

        String[] projection = {
                ExerciseEntry._ID,
                ExerciseEntry.COLUMN_NAME,
                ExerciseEntry.COLUMN_IMAGE};

        return mDatabase.query(ExerciseEntry.TABLE_NAME,
                projection,
                ExerciseEntry.COLUMN_MUSCLE_TARGETED + " = ?",
                new String[]{Integer.toString(id)},
                null, null, null);
    }
}
