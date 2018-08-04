package com.dmitry_simakov.gymlab.training_sessions;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

public class TrainingSessionsFragment extends Fragment {

    public static final String CLASS_NAME = TrainingSessionsFragment.class.getSimpleName();

    private static final class TS extends DatabaseContract.TrainingSessionEntry {}

    private SQLiteDatabase mDatabase;
    private CursorAdapter mCursorAdapter;

    public TrainingSessionsFragment() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(CLASS_NAME, "onAttach");

        mDatabase = DatabaseHelper.getInstance(context).getWritableDatabase();

        Cursor c = mDatabase.rawQuery("SELECT "+ TS._ID +", "+ TS.DATE_TIME +" "+
                " FROM "+ TS.TABLE_NAME +
                " ORDER BY "+ TS.DATE_TIME +" DESC", null);

        String[] groupFrom = { TS.DATE_TIME };
        int[] groupTo = { android.R.id.text1 };
        mCursorAdapter = new SimpleCursorAdapter(context,
                android.R.layout.simple_list_item_1, c, groupFrom, groupTo, 0);
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
                bundle.putInt(TS._ID, (int)id);
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
