package com.dmitry_simakov.gymlab;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorTreeAdapter;

import com.dmitry_simakov.gymlab.database.DbContract;
import com.dmitry_simakov.gymlab.database.GymLabDbHelper;

public class ExercisesListFragment extends Fragment {

    public static final String CLASS_NAME = ExercisesListFragment.class.getSimpleName();

    private static class Ex extends DbContract.ExercisesEntry{}
    private static class M extends DbContract.MusclesEntry{}

    private Context mContext;

    private GymLabDbHelper mDbHelper;
    private SQLiteDatabase mDatabase;
    private Cursor mCursor;
    private ExerciseCursorTreeAdapter mCursorAdapter;
    private ExpandableListView mExerciseElv;


    public ExercisesListFragment() {}

    @Override
    public void onAttach(Context context) {
        Log.d(CLASS_NAME, "onAttach");
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(CLASS_NAME, "onCreateView");

        View view = inflater.inflate(R.layout.fragment_exercise_list, container, false);

        mExerciseElv = view.findViewById(R.id.elv);

        ProgressBar progressBar = view.findViewById(R.id.progressBar);
        mExerciseElv.setEmptyView(progressBar);

        mExerciseElv.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v,int groupPosition, int childPosition, long id) {
                Fragment fragment = new ExerciseDescriptionFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(Ex._ID, (int)id);
                fragment.setArguments(bundle);
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();

                /* Если мы возвращаем true – это значит, мы сообщаем, что сами полностью обработали
                событие и оно не пойдет в дальнейшие обработчики (если они есть).
                Если возвращаем false – значит, мы позволяем событию идти дальше. */
                return true;
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        Log.d(CLASS_NAME, "onStart");
        super.onStart();

        mDbHelper = new GymLabDbHelper(mContext);
        mDatabase = mDbHelper.getReadableDatabase();

        mCursor = mDatabase.rawQuery("SELECT "+ M._ID +", "+ M.NAME +", "+ M.IMAGE +
                " FROM "+ M.TABLE_NAME +
                " ORDER BY "+ M._ID, null);

        String[] groupFrom = { M.NAME, M.IMAGE };
        int[] groupTo = { R.id.muscle_name, R.id.muscle_image };
        String[] childFrom = { Ex.NAME, Ex.IMAGE };
        int[] childTo = { R.id.exercise_name, R.id.exercise_image };

        mCursorAdapter = new ExerciseCursorTreeAdapter(mContext, mDatabase, mCursor,
                R.layout.muscle_expandable_list_item, groupFrom, groupTo,
                R.layout.exercise_list_item, childFrom, childTo);

        mCursorAdapter.setViewBinder(new SimpleCursorTreeAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (view.getId() == R.id.muscle_image || view.getId() == R.id.exercise_image) {
                    ImageView imageView = (ImageView) view;

                    String imageName = cursor.getString(columnIndex);
                    if (imageName != null) {
                        Resources res = mContext.getResources();
                        int resID = res.getIdentifier(imageName, "drawable", mContext.getPackageName());
                        if (resID != 0) {
                            imageView.setImageDrawable(res.getDrawable(resID));
                        }
                    }
                    return true;
                }
                return false;
            }
        });

        mExerciseElv.setAdapter(mCursorAdapter);
    }

    @Override
    public void onDestroy() {
        Log.d(CLASS_NAME, "onDestroy");
        super.onDestroy();

        if (mCursor != null) mCursor.close();
        if (mDatabase != null) mDatabase.close();
    }
}