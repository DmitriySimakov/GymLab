package com.dmitry_simakov.gymlab;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.ListView;

import com.dmitry_simakov.gymlab.database.MeasuresDbHelper;

public class MeasurementDaysListFragment extends ListFragment {

    public static final String CLASS_NAME = MeasurementDaysListFragment.class.getSimpleName();

    private static class BME extends com.dmitry_simakov.gymlab.database.DbContract.BodyMeasurementsEntry{}

    private Context mContext;

    private MeasuresDbHelper mDbHelper;
    private SQLiteDatabase mDatabase;
    private Cursor mCursor;

    public MeasurementDaysListFragment() {}

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

        mCursor = mDatabase.rawQuery(
                "SELECT "+
                        BME._ID +", "+
                        BME.DATE +" "+
                        "FROM "+ BME.TABLE_NAME +" "+
                        "GROUP BY "+ BME.DATE,
                null
        );

        String[] groupFrom = { BME.DATE };
        int[] groupTo = { android.R.id.text1 };
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(mContext,
                android.R.layout.simple_list_item_1, mCursor, groupFrom, groupTo, 0);
        setListAdapter(adapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Fragment fragment = new MeasurementsListFragment();
        Bundle bundle = new Bundle();
        String date = mCursor.getString(mCursor.getColumnIndex(BME.DATE));
        bundle.putString(BME.DATE, date);
        bundle.putString("class_name", CLASS_NAME);
        fragment.setArguments(bundle);
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}
