package com.dmitry_simakov.gymlab.training_sessions;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TrainingSessionSetsFragment extends Fragment {

    public static final String CLASS_NAME = TrainingSessionSetsFragment.class.getSimpleName();

    private static final class TSE extends DatabaseContract.TrainingSessionExerciseEntry {}
    private static final class TSS extends DatabaseContract.TrainingSessionSetEntry {}
    private static final class EMP extends DatabaseContract.ExerciseMeasurementParamEntry {}

    private SQLiteDatabase mDatabase;
    private CursorAdapter mCursorAdapter;

    private int mExerciseId;
    private boolean paramsBoolArr[] = new boolean[4];


    public TrainingSessionSetsFragment() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(CLASS_NAME, "onAttach");

        Bundle args = getArguments();
        if (args != null) {
            mExerciseId = args.getInt(TSE._ID);
            int arr = args.getInt(TSE.PARAMS_BOOL_ARR);
            for (int i = 0; i < 4; i++) {
                paramsBoolArr[4 - i - 1] = (arr%10 == 1);
                arr /= 10;
            }
        }

        mDatabase = DatabaseHelper.getInstance(context).getWritableDatabase();
        Cursor c = mDatabase.rawQuery("SELECT "+ TSS._ID +", "+
                        TSS.WEIGHT +", "+
                        TSS.REPS +", "+
                        TSS.TIME +", "+
                        TSS.DISTANCE +" "+
                        " FROM "+ TSS.TABLE_NAME +
                        " WHERE "+ TSS.EXERCISE_ID +" = ?"+
                        " ORDER BY "+ TSS._ID,
                new String[]{ String.valueOf(mExerciseId) });

        mCursorAdapter = new MyCursorAdapter(getContext(), paramsBoolArr);
        mCursorAdapter.swapCursor(c);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_training_session_sets, container, false);

        int[] header = new int[] { R.id.header_weight, R.id.header_reps, R.id.header_time, R.id.header_distance };
        for (int i = 0; i < 4; i++) {
            if (!paramsBoolArr[i]) {
                view.findViewById(header[i]).setVisibility(View.GONE);
            }
        }

        ListView listView = view.findViewById(R.id.list_view);
        listView.setAdapter(mCursorAdapter);
        /*listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Fragment fragment = new TrainingSessionExercisesFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(TS._ID, (int)id);
                fragment.setArguments(bundle);
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });*/

        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "This function is not implemented yet.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        return view;
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
