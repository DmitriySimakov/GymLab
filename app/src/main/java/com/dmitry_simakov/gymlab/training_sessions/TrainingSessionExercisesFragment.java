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
import com.dmitry_simakov.gymlab.exercises.ExercisesListFragment;
import com.dmitry_simakov.gymlab.measurements.MeasurementDialog;

public class TrainingSessionExercisesFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String CLASS_NAME = TrainingSessionExercisesFragment.class.getSimpleName();

    private static final int SESSION_LOADER_ID = 0;
    private static final int PROGRAM_DAY_LOADER_ID = 1;

    private static final class TS extends DatabaseContract.TrainingSessionEntry {}
    private static final class TSE extends DatabaseContract.TrainingSessionExerciseEntry {}
    private static final class TPE extends DatabaseContract.TrainingProgramExerciseEntry {}
    private static final class E extends DatabaseContract.ExerciseEntry {}

    private Cursor mCursor;
    private CursorAdapter mCursorAdapter;

    private int mTrainingDayId, mSessionId;

    public TrainingSessionExercisesFragment() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(CLASS_NAME, "onAttach");

        String[] groupFrom = { E.NAME };
        int[] groupTo = { android.R.id.text1 };
        mCursorAdapter = new SimpleCursorAdapter(context,
                android.R.layout.simple_list_item_1, null, groupFrom, groupTo, 0);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_training_session_exercises, container, false);

        ListView listView = view.findViewById(R.id.list_view);
        listView.setAdapter(mCursorAdapter);
        listView.setOnItemClickListener((parent, view1, position, id) -> {
            Fragment fragment = new TrainingSessionSetsFragment();
            Bundle bundle = new Bundle();
            bundle.putInt(TSE._ID, (int)id);
            mCursor.moveToPosition(position);
            int paramsBoolArr = mCursor.getInt(mCursor.getColumnIndex(TSE.PARAMS_BOOL_ARR));
            bundle.putInt(TSE.PARAMS_BOOL_ARR, paramsBoolArr);
            fragment.setArguments(bundle);
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(view2 -> {
            TrainingSessionExerciseDialog dialog = new TrainingSessionExerciseDialog();
            Bundle args = new Bundle();
            args.putInt(TSE.SESSION_ID, mSessionId);
            dialog.setArguments(args);
            dialog.show(getChildFragmentManager(), null);
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            mSessionId = getArguments().getInt(TSE.SESSION_ID);
            if (args.containsKey(TS.TRAINING_DAY_ID)) {
                mTrainingDayId = args.getInt(TS.TRAINING_DAY_ID);
                getLoaderManager().initLoader(PROGRAM_DAY_LOADER_ID, null, this);
            } else {
                getLoaderManager().initLoader(SESSION_LOADER_ID, null, this);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCursor != null) mCursor.close();
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        Loader loader = null;
        switch (id) {
            case PROGRAM_DAY_LOADER_ID:
                loader = new MyCursorLoader(getContext(), mTrainingDayId);
                break;
            case SESSION_LOADER_ID:
                loader = new MyCursorLoader(getContext(), mSessionId);
                break;
        }
        return loader;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor c) {
        switch (loader.getId()) {
            case PROGRAM_DAY_LOADER_ID:
                // insert exercise from program to the session
                if (c.moveToFirst()) {
                    do {
                        int exerciseId = c.getInt(c.getColumnIndex(TPE.EXERCISE_ID));
                        int number = c.getInt(c.getColumnIndex(TPE.NUMBER));
                        DatabaseHelper.insertExerciseIntoSession(mSessionId, exerciseId, number, 1100);
                    } while (c.moveToNext());
                }

                Bundle args = getArguments();
                if (args.containsKey(TS._ID)) {
                    getLoaderManager().initLoader(SESSION_LOADER_ID, null, this);
                }
                break;
            case SESSION_LOADER_ID:
                mCursor = c;
                mCursorAdapter.swapCursor(c);
                break;
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {}

    private static class MyCursorLoader extends CursorLoader {

        public final String CLASS_NAME = TrainingSessionExercisesFragment.CLASS_NAME +"."+ MyCursorLoader.class.getSimpleName();

        private final ForceLoadContentObserver mObserver = new ForceLoadContentObserver();

        private int mId;

        MyCursorLoader(Context context, int id) {
            super(context);
            mId = id;
            setUri(TSE.CONTENT_URI);
        }

        @Override
        public Cursor loadInBackground() {
            Log.d(CLASS_NAME, "loadInBackground id: "+ getId());

            SQLiteDatabase db = DatabaseHelper.getInstance(getContext()).getWritableDatabase();
            Cursor cursor = null;
            switch (getId()) {
                case PROGRAM_DAY_LOADER_ID:
                    cursor = db.rawQuery("SELECT tpe."+ TPE._ID +", tpe."+ TPE.EXERCISE_ID +"," +
                                    " tpe."+ TPE.NUMBER +", tpe."+ TPE.PARAMS_BOOL_ARR +"," +
                                    " e."+ E.IMAGE +", e."+ E.NAME +
                                    " FROM "+ TPE.TABLE_NAME +" AS tpe LEFT JOIN "+ E.TABLE_NAME +" AS e"+
                                    " ON tpe."+ TPE.EXERCISE_ID +" = e."+ E._ID +
                                    " WHERE "+ TPE.TRAINING_DAY_ID +" = ?"+
                                    " ORDER BY tpe."+ TPE.NUMBER,
                            new String[]{ String.valueOf(mId) });
                    break;
                case SESSION_LOADER_ID:
                    cursor = db.rawQuery("SELECT tse."+ TSE._ID +", tse."+ TSE.PARAMS_BOOL_ARR +", "+
                                    "e."+ E.IMAGE +", e."+ E.NAME +
                                    " FROM "+ TSE.TABLE_NAME +" AS tse LEFT JOIN "+ E.TABLE_NAME +" AS e"+
                                    " ON tse."+ TSE.EXERCISE_ID +" = e."+ E._ID +
                                    " WHERE "+ TSE.SESSION_ID +" = ?"+
                                    " ORDER BY tse."+ TSE.NUMBER,
                            new String[]{ String.valueOf(mId) });
                    break;
            }
            if (cursor != null) {
                cursor.getCount(); // Fill cursor window
                cursor.registerContentObserver(mObserver);
                cursor.setNotificationUri(getContext().getContentResolver(), getUri());
            }
            return cursor;
        }
    }
}
