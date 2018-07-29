package com.dmitry_simakov.gymlab;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.dmitry_simakov.gymlab.database.DbContract;
import com.dmitry_simakov.gymlab.database.MeasuresDbHelper;

public class MeasurementsHistoryListFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String CLASS_NAME = MeasurementsHistoryListFragment.class.getSimpleName();

    private static class BM extends DbContract.BodyMeasurementsEntry{}

    private Context mContext;

    private MeasuresDbHelper mDbHelper;
    private SQLiteDatabase mDatabase;
    private Cursor mCursor;
    SimpleCursorAdapter mCursorAdapter;

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

        String[] groupFrom = { BM.DATE };
        int[] groupTo = { android.R.id.text1 };
        mCursorAdapter = new SimpleCursorAdapter(mContext,
                android.R.layout.simple_list_item_1, null, groupFrom, groupTo, 0);
        setListAdapter(mCursorAdapter);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Log.d(CLASS_NAME, "onListItemClick");

        Fragment fragment = new MeasurementsListFragment();
        Bundle args = new Bundle();
        String date = mCursor.getString(mCursor.getColumnIndex(BM.DATE));
        args.putString(BM.DATE, date);
        fragment.setArguments(args);

        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        return new MyCursorLoader(mContext, mDatabase);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        mCursor = cursor;
        mCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {}

    private static class MyCursorLoader extends CursorLoader {

        private final ForceLoadContentObserver mObserver = new ForceLoadContentObserver();

        private SQLiteDatabase mDatabase;

        public MyCursorLoader(Context context, SQLiteDatabase db) {
            super(context);
            mDatabase = db;
            setUri(Uri.parse("content://measurements"));
        }

        @Override
        public Cursor loadInBackground() {
            Cursor cursor = mDatabase.rawQuery("SELECT "+ BM._ID +", "+ BM.DATE +" "+
                            " FROM "+ BM.TABLE_NAME +
                            " GROUP BY "+ BM.DATE +
                            " ORDER BY "+ BM.DATE +" DESC",
                    null);

            if (cursor != null) {
                cursor.registerContentObserver(mObserver);
            }

            cursor.setNotificationUri(getContext().getContentResolver(), getUri());
            return cursor;
        }
    }
}
