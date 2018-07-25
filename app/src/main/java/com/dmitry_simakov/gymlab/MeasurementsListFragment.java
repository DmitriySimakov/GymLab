package com.dmitry_simakov.gymlab;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.ListView;

import com.dmitry_simakov.gymlab.database.DbContract;
import com.dmitry_simakov.gymlab.database.MeasuresDbHelper;

public class MeasurementsListFragment extends ListFragment {

    public static final String CLASS_NAME = MeasurementsListFragment.class.getSimpleName();

    private static class BME extends com.dmitry_simakov.gymlab.database.DbContract.BodyMeasurementsEntry {}
    private static class BPE extends DbContract.BodyParametersEntry {}

    private Context mContext;

    private MeasuresDbHelper mDbHelper;
    private SQLiteDatabase mDatabase;
    private Cursor mCursor;

    public MeasurementsListFragment() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mContext = context;

        FloatingActionButton fab = getActivity().findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        fab.setVisibility(View.VISIBLE);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mDbHelper = new MeasuresDbHelper(mContext);
        mDatabase = mDbHelper.getWritableDatabase();

        String date = getArguments().getString(BME.DATE);

        if (date == null) {
            mCursor = mDatabase.rawQuery(
                    "SELECT " +
                            BME._ID + ", " +
                            "(SELECT " + BPE.NAME + " FROM " + BPE.TABLE_NAME + " WHERE " + BPE._ID + " = " + BME.BODY_PARAMETER_ID + ") AS " + BME.BODY_PARAMETER + ", " +
                            BME.VALUE + " " +
                            "FROM " + BME.TABLE_NAME,
                    null
            );
        } else {
            mCursor = mDatabase.rawQuery(
                    "SELECT " +
                            BME._ID + ", " +
                            "(SELECT " + BPE.NAME + " FROM " + BPE.TABLE_NAME + " WHERE " + BPE._ID + " = " + BME.BODY_PARAMETER_ID + ") AS " + BME.BODY_PARAMETER + ", " +
                            BME.VALUE + " " +
                            "FROM " + BME.TABLE_NAME + " " +
                            "WHERE " + BME.DATE + " = '" + date + "'",
                    null
            );
        }

        String[] groupFrom = { BME.BODY_PARAMETER, BME.VALUE };
        int[] groupTo = { R.id.measure_parameter, R.id.measure_value };
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(mContext,
                R.layout.day_measurement_list_item, mCursor, groupFrom, groupTo, 0);
        setListAdapter(adapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);


    }
}
