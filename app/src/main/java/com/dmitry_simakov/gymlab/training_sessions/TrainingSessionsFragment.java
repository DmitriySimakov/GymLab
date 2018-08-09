package com.dmitry_simakov.gymlab.training_sessions;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.dmitry_simakov.gymlab.R;
import com.dmitry_simakov.gymlab.database.DatabaseContract;
import com.dmitry_simakov.gymlab.database.DatabaseHelper;
import com.dmitry_simakov.gymlab.exercises.ExerciseDescriptionFragment;

public class TrainingSessionsFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String CLASS_NAME = TrainingSessionsFragment.class.getSimpleName();

    private static final class TS extends DatabaseContract.TrainingSessionEntry {}
    private static final class TSE extends DatabaseContract.TrainingSessionExerciseEntry {}

    private CursorAdapter mCursorAdapter;

    public TrainingSessionsFragment() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(CLASS_NAME, "onAttach");

        String[] groupFrom = { TS.DATE_TIME };
        int[] groupTo = { android.R.id.text1 };
        mCursorAdapter = new SimpleCursorAdapter(context,
                android.R.layout.simple_list_item_1, null, groupFrom, groupTo, 0);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(CLASS_NAME, "onCreateView");

        View view = inflater.inflate(R.layout.fragment_training_sessions, container, false);

        ListView listView = view.findViewById(R.id.list_view);
        listView.setAdapter(mCursorAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Fragment fragment = new TrainingSessionExercisesFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(TSE.SESSION_ID, (int)id);
                fragment.setArguments(bundle);
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new TrainingSessionDialog().show(getChildFragmentManager(), null);
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        return new MyCursorLoader(getContext());
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        mCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {}

    private static class MyCursorLoader extends CursorLoader {

        public final String CLASS_NAME = TrainingSessionsFragment.CLASS_NAME +"."+ MyCursorLoader.class.getSimpleName();

        private final ForceLoadContentObserver mObserver = new ForceLoadContentObserver();

        MyCursorLoader(Context context) {
            super(context);
            setUri(TS.CONTENT_URI);
        }

        @Override
        public Cursor loadInBackground() {
            Log.d(CLASS_NAME, "loadInBackground id: "+ getId());

            SQLiteDatabase db = DatabaseHelper.getInstance(getContext()).getWritableDatabase();
            Cursor cursor = db.rawQuery("SELECT "+ TS._ID +", "+ TS.DATE_TIME +" "+
                    " FROM "+ TS.TABLE_NAME +
                    " ORDER BY "+ TS.DATE_TIME +" DESC", null);

            if (cursor != null) {
                cursor.getCount(); // Fill cursor window
                cursor.registerContentObserver(mObserver);
                cursor.setNotificationUri(getContext().getContentResolver(), getUri());
            }
            return cursor;
        }
    }
}
