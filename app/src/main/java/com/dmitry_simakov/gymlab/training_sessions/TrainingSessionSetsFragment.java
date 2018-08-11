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
import android.widget.TextView;

import com.dmitry_simakov.gymlab.R;
import com.dmitry_simakov.gymlab.database.DatabaseContract;
import com.dmitry_simakov.gymlab.database.DatabaseHelper;

public class TrainingSessionSetsFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String CLASS_NAME = TrainingSessionSetsFragment.class.getSimpleName();

    private static final class TSE extends DatabaseContract.TrainingSessionExerciseEntry {}
    private static final class TSS extends DatabaseContract.TrainingSessionSetEntry {}

    private CursorAdapter mCursorAdapter;

    private int mExerciseId;
    private boolean mParamsBoolArr[] = new boolean[4];


    public TrainingSessionSetsFragment() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(CLASS_NAME, "onAttach");

        mExerciseId = getArguments().getInt(TSE._ID);
        int arr = getArguments().getInt(TSE.PARAMS_BOOL_ARR);
        for (int i = 0; i < 4; i++) {
            mParamsBoolArr[4 - i - 1] = (arr % 10 == 1);
            arr /= 10;
        }

        mCursorAdapter = new MyCursorAdapter(getContext(), mParamsBoolArr);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_training_session_sets, container, false);

        initListView(view);
        configureListHeader(view);
        initFAB(view);

        return view;
    }

    private void initListView(View v) {
        ListView listView = v.findViewById(R.id.list_view);
        listView.setAdapter(mCursorAdapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            TrainingSessionSetDialog dialog = new TrainingSessionSetDialog();
            Bundle args = new Bundle();
            args.putInt(TrainingSessionSetDialog.EXERCISE_ID, mExerciseId);
            args.putBooleanArray(TSE.PARAMS_BOOL_ARR, mParamsBoolArr);
            args.putInt(TrainingSessionSetDialog.SET_ID, (int)id);
            dialog.setArguments(args);
            dialog.show(getChildFragmentManager(), null);
        });
    }

    private void configureListHeader(View v) {
        int[] header = new int[] { R.id.header_weight, R.id.header_reps, R.id.header_time, R.id.header_distance };
        for (int i = 0; i < 4; i++) {
            if (!mParamsBoolArr[i]) {
                v.findViewById(header[i]).setVisibility(View.GONE);
            }
        }
    }

    private void initFAB(View v) {
        FloatingActionButton fab = v.findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            TrainingSessionSetDialog dialog = new TrainingSessionSetDialog();
            Bundle args = new Bundle();
            args.putInt(TrainingSessionSetDialog.EXERCISE_ID, mExerciseId);
            args.putBooleanArray(TSE.PARAMS_BOOL_ARR, mParamsBoolArr);
            dialog.setArguments(args);
            dialog.show(getChildFragmentManager(), null);
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        return new MyCursorLoader(getContext(), mExerciseId);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        mCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {}

    private static class MyCursorLoader extends CursorLoader {

        public final String CLASS_NAME = TrainingSessionSetsFragment.CLASS_NAME +"."+ MyCursorLoader.class.getSimpleName();

        private final ForceLoadContentObserver mObserver = new ForceLoadContentObserver();

        private int mExerciseId;

        MyCursorLoader(Context context, int exerciseId) {
            super(context);
            mExerciseId = exerciseId;
            setUri(TSS.CONTENT_URI);
        }

        @Override
        public Cursor loadInBackground() {
            Log.d(CLASS_NAME, "loadInBackground id: "+ getId());

            SQLiteDatabase db = DatabaseHelper.getInstance(getContext()).getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT "+ TSS._ID +", "+
                            TSS.WEIGHT +", "+
                            TSS.REPS +", "+
                            TSS.TIME +", "+
                            TSS.DISTANCE +" "+
                            " FROM "+ TSS.TABLE_NAME +
                            " WHERE "+ TSS.EXERCISE_ID +" = ?"+
                            " ORDER BY "+ TSS._ID,
                    new String[]{ String.valueOf(mExerciseId) });

            if (cursor != null) {
                cursor.getCount(); // Fill cursor window
                cursor.registerContentObserver(mObserver);
                cursor.setNotificationUri(getContext().getContentResolver(), getUri());
            }
            return cursor;
        }
    }

    private static class MyCursorAdapter extends SimpleCursorAdapter {

        public final String CLASS_NAME = TrainingSessionSetsFragment.CLASS_NAME +"."+ MyCursorAdapter.class.getSimpleName();

        private static final int LAYOUT = R.layout.training_session_set_list_item;
        private static final String[] FROM = { TSS.WEIGHT, TSS.REPS, TSS.TIME, TSS.DISTANCE };
        private static final int[] TO = { R.id.weight, R.id.reps, R.id.time, R.id.distance };

        private boolean[] mArr;

        MyCursorAdapter(Context context, boolean[] arr) {
            super(context, LAYOUT, null, FROM, TO, 0);
            mArr = arr;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            super.bindView(view, context, cursor);
            Log.d(CLASS_NAME, "bindView");

            TextView numberTextView = view.findViewById(R.id.num);
            int num = cursor.getPosition() + 1;
            numberTextView.setText(num +".");

            for (int i = 0; i < 4; i++) {
                if (!mArr[i]) {
                    view.findViewById(TO[i]).setVisibility(View.GONE);
                }
            }
        }
    }
}
