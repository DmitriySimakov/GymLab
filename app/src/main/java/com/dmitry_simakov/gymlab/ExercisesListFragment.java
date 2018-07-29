package com.dmitry_simakov.gymlab;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
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

import java.util.HashMap;

public class ExercisesListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String CLASS_NAME = ExercisesListFragment.class.getSimpleName();

    private static class Ex extends DbContract.ExercisesEntry{}
    private static class M extends DbContract.MusclesEntry{}

    private Context mContext;

    private GymLabDbHelper mDbHelper;
    private SQLiteDatabase mDatabase;
    private MyCursorTreeAdapter mCursorTreeAdapter;
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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(CLASS_NAME, "onActivityCreated");

        mDbHelper = new GymLabDbHelper(mContext);
        mDatabase = mDbHelper.getReadableDatabase();

        mCursorTreeAdapter = new MyCursorTreeAdapter(mContext, this);
        mExerciseElv.setAdapter(mCursorTreeAdapter);

        getLoaderManager().initLoader(-1, null, this);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(CLASS_NAME, "onCreateLoader for loader_id " + id);
        return new MyCursorLoader(mContext, mDatabase);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        int id = loader.getId();
        Log.d(CLASS_NAME, "onLoadFinished() for loader_id " + id);
        if (id != -1) { // child loader
            if (!cursor.isClosed()) {
                try {
                    int groupPos = mCursorTreeAdapter.getGroupMap().get(id);
                    mCursorTreeAdapter.setChildrenCursor(groupPos, cursor);
                } catch (NullPointerException e) {
                    Log.w(CLASS_NAME,"Adapter expired, try again on the next query: " + e.getMessage());
                }
            }
        } else { // group loader
            mCursorTreeAdapter.setGroupCursor(cursor);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        int id = loader.getId();
        Log.d(CLASS_NAME, "onLoaderReset() for loader_id " + id);
        if (id != -1) { // child loader
            try {
                mCursorTreeAdapter.setChildrenCursor(id, null);
            } catch (NullPointerException e) {
                Log.w("TAG", "Adapter expired, try again on the next query: " + e.getMessage());
            }
        } else { // group loader
            mCursorTreeAdapter.setGroupCursor(null);
        }
    }

    private static class MyCursorLoader extends CursorLoader {

        private final ForceLoadContentObserver mObserver = new ForceLoadContentObserver();

        private SQLiteDatabase mDatabase;

        public MyCursorLoader(Context context, SQLiteDatabase db) {
            super(context);
            mDatabase = db;
            if (getId() != -1) { // child loader
                setUri(Uri.parse("content://child"));
            } else { // group loader
                setUri(Uri.parse("content://group"));
            }
        }

        @Override
        public Cursor loadInBackground() {
            Cursor cursor;
            int id = getId();
            if (id != -1) { // child loader
                cursor = mDatabase.rawQuery("SELECT "+ Ex._ID +", "+ Ex.NAME +", "+ Ex.IMAGE +
                                " FROM "+ Ex.TABLE_NAME +" WHERE "+ Ex.MAIN_MUSCLE_ID +" = ?",
                        new String[]{ String.valueOf(id) });
            } else { // group loader
                cursor = mDatabase.rawQuery("SELECT "+ M._ID +", "+ M.NAME +", "+ M.IMAGE +
                        " FROM "+ M.TABLE_NAME + " ORDER BY "+ M._ID, null);
            }

            if (cursor != null) {
                cursor.registerContentObserver(mObserver);
            }

            cursor.setNotificationUri(getContext().getContentResolver(), getUri());
            return cursor;
        }
    }

    private static class MyCursorTreeAdapter extends SimpleCursorTreeAdapter {

        private static final int GROUP_LAYOUT = R.layout.muscle_expandable_list_item;
        private static final String[] GROUP_FROM = { M.NAME, M.IMAGE };
        private static final int[] GROUP_TO = { R.id.muscle_name, R.id.muscle_image };
        private static final int CHILD_LAYOUT = R.layout.exercise_list_item;
        private static final String[] CHILD_FROM = { Ex.NAME, Ex.IMAGE };
        private static final int[] CHILD_TO = { R.id.exercise_name, R.id.exercise_image };

        private Context mContext;
        private ExercisesListFragment mFragment;
        private final HashMap<Integer, Integer> mGroupMap;

        MyCursorTreeAdapter(Context context, ExercisesListFragment fragment) {
            super(context, null, GROUP_LAYOUT, GROUP_FROM, GROUP_TO, CHILD_LAYOUT, CHILD_FROM, CHILD_TO);

            mContext = context;
            mFragment = fragment;
            mGroupMap = new HashMap<Integer, Integer>();

            setViewBinder(new SimpleCursorTreeAdapter.ViewBinder() {
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
        }

        @Override
        protected Cursor getChildrenCursor(Cursor groupCursor) {
            int groupPos = groupCursor.getPosition();
            int groupId = groupCursor.getInt(groupCursor.getColumnIndex(Ex._ID));
            mGroupMap.put(groupId, groupPos);

            mFragment.getLoaderManager().initLoader(groupId, null, mFragment);
            return null;
        }

        public HashMap<Integer, Integer> getGroupMap() {
            return mGroupMap;
        }
    }
}