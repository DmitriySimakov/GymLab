package com.dmitry_simakov.gymlab;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.dmitry_simakov.gymlab.database.DbContract;
import com.dmitry_simakov.gymlab.database.MeasuresDbHelper;

public class MeasurementsListFragment extends ListFragment {

    public static final String CLASS_NAME = MeasurementsListFragment.class.getSimpleName();

    private static class BM extends DbContract.BodyMeasurementsEntry {}
    private static class BP extends DbContract.BodyParametersEntry {}

    private Context mContext;

    private boolean isOneDay;

    private MeasuresDbHelper mDbHelper;
    private SQLiteDatabase mDatabase;
    private Cursor mCursor;

    public MeasurementsListFragment() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(CLASS_NAME, "onAttach");
        mContext = context;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(CLASS_NAME, "onActivityCreated");

        mDbHelper = new MeasuresDbHelper(mContext);
        mDatabase = mDbHelper.getWritableDatabase();

        String date = null;
        Bundle args = getArguments();
        if (args != null) {
            date = getArguments().getString(BM.DATE);
        }

        if (date == null) {
            isOneDay = false;
            mCursor = mDatabase.rawQuery(
                    "SELECT " +
                            BP._ID + ", " +
                            BP.NAME +" AS "+ BM.BODY_PARAMETER + ", " +
                            "(SELECT " + BM.VALUE + " FROM " + BM.TABLE_NAME + " WHERE " + BM.BODY_PARAMETER_ID + " = BP." + BP._ID + ") AS " + BM.VALUE + " " +
                            "FROM " + BP.TABLE_NAME + " AS BP " +
                            "ORDER BY " + BP._ID,
                    null
            );
        } else {
            isOneDay = true;
            mCursor = mDatabase.rawQuery(
                    "SELECT " +
                            BM._ID + ", " +
                            "(SELECT " + BP.NAME + " FROM " + BP.TABLE_NAME + " WHERE " + BP._ID + " = BM." + BM.BODY_PARAMETER_ID + ") AS " + BM.BODY_PARAMETER + ", " +
                            BM.VALUE + " " +
                            "FROM " + BM.TABLE_NAME + " AS BM " +
                            "WHERE " + BM.DATE + " = '" + date + "'",
                    null
            );
        }

        String[] groupFrom = { BM.BODY_PARAMETER, BM.VALUE };
        int[] groupTo = { R.id.measure_parameter, R.id.measure_value };
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(mContext,
                R.layout.day_measurement_list_item, mCursor, groupFrom, groupTo, 0);
        setListAdapter(adapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Log.d(CLASS_NAME, "onListItemClick");

        MeasurementDialog dialog = new MeasurementDialog();
        Bundle args = new Bundle();
        if (isOneDay) {
            args.putInt(MeasurementDialog.MEASUREMENT_ID, (int)id);
        } else {
            // Put position+1 cuz SQL counts from 1 while the position in list is counted from 0
            args.putInt(MeasurementDialog.PARAMETER_ID, position+1);
        }
        dialog.setArguments(args);
        dialog.show(getChildFragmentManager(), "MEASUREMENT_DIALOG");

    }
}
