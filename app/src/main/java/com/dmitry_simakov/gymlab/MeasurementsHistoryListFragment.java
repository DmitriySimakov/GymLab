package com.dmitry_simakov.gymlab;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.dmitry_simakov.gymlab.database.DbContract;
import com.dmitry_simakov.gymlab.database.MeasuresDbHelper;

public class MeasurementsHistoryListFragment extends ListFragment {

    public static final String CLASS_NAME = MeasurementsHistoryListFragment.class.getSimpleName();

    private static class BM extends DbContract.BodyMeasurementsEntry{}

    private Context mContext;

    private MeasuresDbHelper mDbHelper;
    private SQLiteDatabase mDatabase;
    private Cursor mCursor;

    public MeasurementsHistoryListFragment() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mDbHelper = new MeasuresDbHelper(mContext);
        mDatabase = mDbHelper.getWritableDatabase();

        mCursor = mDatabase.rawQuery("SELECT "+ BM._ID +", "+ BM.DATE +" "+
                        " FROM "+ BM.TABLE_NAME +
                        " GROUP BY "+ BM.DATE +
                        " ORDER BY "+ BM.DATE +" DESC",
                null
        );

        String[] groupFrom = { BM.DATE };
        int[] groupTo = { android.R.id.text1 };
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(mContext,
                android.R.layout.simple_list_item_1, mCursor, groupFrom, groupTo, 0);
        setListAdapter(adapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Log.d(CLASS_NAME, "onListItemClick");

        Fragment fragment = new MeasurementsListFragment();
        Bundle bundle = new Bundle();
        String date = mCursor.getString(mCursor.getColumnIndex(BM.DATE));
        bundle.putString(BM.DATE, date);
        bundle.putString("class_name", CLASS_NAME);
        fragment.setArguments(bundle);

        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onDestroy() {
        Log.d(CLASS_NAME, "onDestroy");
        super.onDestroy();

        if (mCursor != null) mCursor.close();
        if (mDatabase != null) mDatabase.close();
    }
}
