package com.dmitry_simakov.gymlab.training_sessions;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import android.widget.AdapterView;
import android.widget.ListView;

import com.dmitry_simakov.gymlab.R;
import com.dmitry_simakov.gymlab.database.DatabaseContract;
import com.dmitry_simakov.gymlab.database.DatabaseHelper;

public class TrainingSessionExercisesFragment extends Fragment {

    public static final String CLASS_NAME = TrainingSessionExercisesFragment.class.getSimpleName();

    private static final class TS extends DatabaseContract.TrainingSessionEntry {}
    private static final class TSE extends DatabaseContract.TrainingSessionExerciseEntry {}
    private static final class E extends DatabaseContract.ExerciseEntry {}

    private SQLiteDatabase mDatabase;
    private Cursor mCursor;
    private CursorAdapter mCursorAdapter;

    private int mSessionId;

    public TrainingSessionExercisesFragment() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(CLASS_NAME, "onAttach");

        Bundle args = getArguments();
        if (args != null) {
            mSessionId = args.getInt(TS._ID);
        }

        mDatabase = DatabaseHelper.getInstance(context).getWritableDatabase();
        mCursor = mDatabase.rawQuery("SELECT tse."+ TSE._ID +", tse."+ TSE.PARAMS_BOOL_ARR +", "+
                        "e."+ E.IMAGE +", e."+ E.NAME +
                        " FROM "+ TSE.TABLE_NAME +" AS tse LEFT JOIN "+ E.TABLE_NAME +" AS e"+
                        " ON tse."+ TSE.EXERCISE_ID +" = e."+ E._ID +
                        " WHERE "+ TSE.SESSION_ID +" = ?"+
                        " ORDER BY tse."+ TSE.NUMBER,
                new String[]{ String.valueOf(mSessionId) });

        String[] groupFrom = { E.NAME };
        int[] groupTo = { android.R.id.text1 };
        mCursorAdapter = new SimpleCursorAdapter(context,
                android.R.layout.simple_list_item_1, mCursor, groupFrom, groupTo, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_training_session_exercises, container, false);

        ListView listView = view.findViewById(R.id.list_view);
        listView.setAdapter(mCursorAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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
            }
        });

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

}
