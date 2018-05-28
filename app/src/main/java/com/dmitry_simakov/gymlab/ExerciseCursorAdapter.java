package com.dmitry_simakov.gymlab;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.dmitry_simakov.gymlab.data.ExerciseContract.ExerciseEntry;

public class ExerciseCursorAdapter extends CursorAdapter {

    private static final String LOG_TAG = ExerciseCursorAdapter.class.getSimpleName();

    public ExerciseCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
        Log.d(LOG_TAG, "constructor");
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        Log.d(LOG_TAG, "newView");
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Log.d(LOG_TAG, "bindView");
        TextView nameTextView = view.findViewById(R.id.name);
        int nameColumnIndex = cursor.getColumnIndex(ExerciseEntry.COLUMN_EXERCISE_NAME);
        String exerciseName = cursor.getString(nameColumnIndex);
        nameTextView.setText(exerciseName);
    }
}
