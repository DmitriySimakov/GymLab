package com.dmitry_simakov.gymlab.training_sessions;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.ListView;

import com.dmitry_simakov.gymlab.R;
import com.dmitry_simakov.gymlab.database.DatabaseContract;
import com.dmitry_simakov.gymlab.database.DatabaseHelper;

public class TrainingSessionExercisesFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String CLASS_NAME = TrainingSessionExercisesFragment.class.getSimpleName();

    private static final int SESSION_LOADER_ID = 0;
    private static final int PROGRAM_DAY_LOADER_ID = 1;

    private static final class TS extends DatabaseContract.TrainingSessionEntry {}
    private static final class TSE extends DatabaseContract.TrainingSessionExerciseEntry {}
    private static final class TPE extends DatabaseContract.TrainingProgramExerciseEntry {}
    private static final class E extends DatabaseContract.ExerciseEntry {}

    private CursorAdapter mCursorAdapter;

    private boolean mProgramWasLoaded = false;
    private int mSessionId;


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

        initListView(view);
        initFAB(view);

        return view;
    }

    private void initListView(View v) {
        ListView listView = v.findViewById(R.id.list_view);
        listView.setAdapter(mCursorAdapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Cursor c = mCursorAdapter.getCursor();
            c.moveToPosition(position);

            Bundle bundle = new Bundle();
            bundle.putInt(TSE._ID, (int)id);
            bundle.putInt(TSE.PARAMS_BOOL_ARR, c.getInt(c.getColumnIndex(TSE.PARAMS_BOOL_ARR)));

            Fragment fragment = new TrainingSessionSetsFragment();
            fragment.setArguments(bundle);

            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void initFAB(View v) {
        FloatingActionButton fab = v.findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Bundle bundle = new Bundle();
            bundle.putInt(TSE.SESSION_ID, mSessionId);

            TrainingSessionExerciseDialog dialog = new TrainingSessionExerciseDialog();
            dialog.setArguments(bundle);
            dialog.show(getChildFragmentManager(), null);
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mSessionId = getArguments().getInt(TSE.SESSION_ID);
        // !mProgramWasLoaded prevent reload when returned from the back stack
        if (getArguments().containsKey(TS.TRAINING_DAY_ID) && !mProgramWasLoaded) {
            getLoaderManager().initLoader(PROGRAM_DAY_LOADER_ID, null, this);
        } else {
            getLoaderManager().initLoader(SESSION_LOADER_ID, null, this);
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        Loader loader = null;
        switch (id) {
            case PROGRAM_DAY_LOADER_ID:
                loader = new MyCursorLoader(getContext(), getArguments().getInt(TS.TRAINING_DAY_ID));
                break;
            case SESSION_LOADER_ID:
                loader = new MyCursorLoader(getContext(), mSessionId);
                break;
        }
        return loader;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case PROGRAM_DAY_LOADER_ID:
                insertTrainingProgramExercises(cursor);
                mProgramWasLoaded = true;
                getLoaderManager().destroyLoader(loader.getId());
                getLoaderManager().initLoader(SESSION_LOADER_ID, null, this);
                break;
            case SESSION_LOADER_ID:
                mCursorAdapter.swapCursor(cursor);
                break;
        }
    }

    private void insertTrainingProgramExercises(Cursor c) {
        if (c.moveToFirst()) {
            do {
                int exerciseId = c.getInt(c.getColumnIndex(TPE.EXERCISE_ID));
                int number = c.getInt(c.getColumnIndex(TPE.NUMBER));
                DatabaseHelper.insertExerciseIntoSession(mSessionId, exerciseId, number, 1100);
            } while (c.moveToNext());
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
