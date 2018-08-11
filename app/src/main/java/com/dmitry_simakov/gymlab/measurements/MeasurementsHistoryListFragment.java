package com.dmitry_simakov.gymlab.measurements;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.dmitry_simakov.gymlab.R;
import com.dmitry_simakov.gymlab.database.DatabaseContract;
import com.dmitry_simakov.gymlab.database.DatabaseHelper;

public class MeasurementsHistoryListFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String CLASS_NAME = MeasurementsHistoryListFragment.class.getSimpleName();

    private static final class BM extends DatabaseContract.BodyMeasurementEntry {}

    SimpleCursorAdapter mCursorAdapter;


    public MeasurementsHistoryListFragment() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(CLASS_NAME, "onAttach");

        String[] groupFrom = { BM.DATE };
        int[] groupTo = { android.R.id.text1 };
        mCursorAdapter = new SimpleCursorAdapter(context,
                android.R.layout.simple_list_item_1, null, groupFrom, groupTo, 0);
        setListAdapter(mCursorAdapter);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(CLASS_NAME, "onActivityCreated");

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Log.d(CLASS_NAME, "onListItemClick");

        Cursor c = mCursorAdapter.getCursor();
        Fragment fragment = new MeasurementsListFragment();
        Bundle args = new Bundle();
        String date = c.getString(c.getColumnIndex(BM.DATE));
        args.putString(BM.DATE, date);
        fragment.setArguments(args);

        FragmentActivity activity = getActivity();
        activity.getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
        activity.setTitle(date);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        Log.d(CLASS_NAME, "onCreateLoader id: "+ id);
        return new MyCursorLoader(getContext());
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        Log.d(CLASS_NAME, "onLoadFinished id: "+ loader.getId());
        mCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

    private static class MyCursorLoader extends CursorLoader {

        public final String CLASS_NAME = MeasurementsHistoryListFragment.CLASS_NAME +"."+ MyCursorLoader.class.getSimpleName();

        private final ForceLoadContentObserver mObserver = new ForceLoadContentObserver();

        MyCursorLoader(Context context) {
            super(context);
            setUri(BM.CONTENT_URI);
        }

        @Override
        public Cursor loadInBackground() {
            SQLiteDatabase db = DatabaseHelper.getInstance(getContext()).getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT "+ BM._ID +", "+ BM.DATE +" "+
                            " FROM "+ BM.TABLE_NAME +
                            " GROUP BY "+ BM.DATE +
                            " ORDER BY "+ BM.DATE +" DESC",
                    null);

            if (cursor != null) {
                cursor.getCount(); // Fill cursor window
                cursor.registerContentObserver(mObserver);
                cursor.setNotificationUri(getContext().getContentResolver(), getUri());
            }
            return cursor;
        }
    }
}
