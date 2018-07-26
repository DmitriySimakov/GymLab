package com.dmitry_simakov.gymlab;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.SimpleCursorTreeAdapter;

import com.dmitry_simakov.gymlab.database.DbContract.*;

public class ExerciseCursorTreeAdapter extends SimpleCursorTreeAdapter {

    public static final String CLASS_NAME = ExerciseCursorTreeAdapter.class.getSimpleName();

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
        int idColumn = groupCursor.getColumnIndex(MusclesEntry._ID);
        int id = groupCursor.getInt(idColumn);

        String[] columns = {
                ExercisesEntry._ID,
                ExercisesEntry.NAME,
                ExercisesEntry.IMAGE};

        return mDatabase.query(ExercisesEntry.TABLE_NAME,
                columns,
                ExercisesEntry.MAIN_MUSCLE_ID + " = ?",
                new String[]{ String.valueOf(id) },
                null, null, null);
    }
}
