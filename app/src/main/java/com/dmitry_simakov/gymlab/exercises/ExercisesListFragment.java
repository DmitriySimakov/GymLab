package com.dmitry_simakov.gymlab.exercises;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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

import com.dmitry_simakov.gymlab.R;
import com.dmitry_simakov.gymlab.SharedViewModel;
import com.dmitry_simakov.gymlab.Utils;
import com.dmitry_simakov.gymlab.database.DatabaseContract;
import com.dmitry_simakov.gymlab.database.DatabaseHelper;

import java.util.HashMap;

public class ExercisesListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String CLASS_NAME = ExercisesListFragment.class.getSimpleName();

    private static final class E extends DatabaseContract.ExerciseEntry {}
    private static final class M extends DatabaseContract.MuscleEntry {}
    private static final class TSE extends DatabaseContract.TrainingSessionExerciseEntry {}

    private static final int GROUP_LOADER_ID = -1;

    private MyCursorTreeAdapter mCursorTreeAdapter;
    private ExpandableListView mExerciseElv;


    public ExercisesListFragment() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(CLASS_NAME, "onAttach");
        mCursorTreeAdapter = new MyCursorTreeAdapter(context, this);
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(CLASS_NAME, "onCreateView");

        View view = inflater.inflate(R.layout.fragment_exercise_list, container, false);

        mExerciseElv = view.findViewById(R.id.elv);
        ProgressBar progressBar = view.findViewById(R.id.progressBar);
        mExerciseElv.setEmptyView(progressBar);
        mExerciseElv.setAdapter(mCursorTreeAdapter);

        mExerciseElv.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
            Bundle args = getArguments();
            if (args != null && args.containsKey(TSE.SESSION_ID)) {
                Cursor c = mCursorTreeAdapter.getChild(groupPosition, childPosition);
                String name = c.getString(c.getColumnIndex(E.NAME));
                String muscleId = c.getString(c.getColumnIndex(E.MAIN_MUSCLE_ID));
                String exerciseId = c.getString(c.getColumnIndex(E._ID));
                String imagePath = "exercises/"+ muscleId +"/"+ exerciseId +"_1.jpg";
                SharedViewModel model = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);
                model.setExercise(new Exercise(id, name, imagePath));
                getActivity().getSupportFragmentManager().popBackStack();
            } else {
                Fragment fragment = new ExerciseDescriptionFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(E._ID, (int) id);
                fragment.setArguments(bundle);
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            }

            /* Если мы возвращаем true – это значит, мы сообщаем, что сами полностью обработали
            событие и оно не пойдет в дальнейшие обработчики (если они есть).
            Если возвращаем false – значит, мы позволяем событию идти дальше. */
            return true;
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(CLASS_NAME, "onActivityCreated");
        getLoaderManager().initLoader(GROUP_LOADER_ID, null, this);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(CLASS_NAME, "onCreateLoader id: " + id);
        return new MyCursorLoader(getContext());
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        int id = loader.getId();
        Log.d(CLASS_NAME, "onLoadFinished id: " + id);
        if (id != GROUP_LOADER_ID) { // child loader
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
        Log.d(CLASS_NAME, "onLoaderReset id: " + id);
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

        public static final String CLASS_NAME = ExercisesListFragment.CLASS_NAME +"."+ MyCursorLoader.class.getSimpleName();

        private ForceLoadContentObserver mObserver = new ForceLoadContentObserver();

        MyCursorLoader(Context context) {
            super(context);
            Log.d(CLASS_NAME, "constructor");
        }

        @Override
        public Cursor loadInBackground() {
            int id = getId();
            Log.d(CLASS_NAME, "loadInBackground id: "+ id);

            SQLiteDatabase db = DatabaseHelper.getInstance(getContext()).getReadableDatabase();
            Cursor cursor;
            if (id != GROUP_LOADER_ID) { // child loader
                cursor = db.rawQuery("SELECT "+ E._ID +", "+ E.NAME +", "+ E.MAIN_MUSCLE_ID +
                                " FROM "+ E.TABLE_NAME +" WHERE "+ E.MAIN_MUSCLE_ID +" = ?",
                        new String[]{ String.valueOf(id) });
            } else { // group loader
                cursor = db.rawQuery("SELECT "+ M._ID +", "+ M.NAME +
                        " FROM "+ M.TABLE_NAME + " ORDER BY "+ M._ID, null);
            }

            if (cursor != null) {
                cursor.getCount(); // Fill cursor window
                cursor.registerContentObserver(mObserver);
                //cursor.setNotificationUri(getContext().getContentResolver(), getUri());
            }
            return cursor;
        }
    }

    private static class MyCursorTreeAdapter extends SimpleCursorTreeAdapter {

        public static final String CLASS_NAME = ExercisesListFragment.CLASS_NAME +"."+ MyCursorTreeAdapter.class.getSimpleName();

        private static final int GROUP_LAYOUT = R.layout.exercise_list_group_item;
        private static final String[] GROUP_FROM = { M.NAME, M._ID };
        private static final int[] GROUP_TO = { R.id.muscle_name, R.id.muscle_image };
        private static final int CHILD_LAYOUT = R.layout.exercise_list_child_item;
        private static final String[] CHILD_FROM = { E.NAME, E._ID };
        private static final int[] CHILD_TO = { R.id.exercise_name, R.id.exercise_image };

        private Context mContext;
        private ExercisesListFragment mFragment;
        private final HashMap<Integer, Integer> mGroupMap;

        MyCursorTreeAdapter(Context context, ExercisesListFragment fragment) {
            super(context, null, GROUP_LAYOUT, GROUP_FROM, GROUP_TO, CHILD_LAYOUT, CHILD_FROM, CHILD_TO);
            Log.d(CLASS_NAME, "constructor");

            mContext = context;
            mFragment = fragment;
            mGroupMap = new HashMap<>();

            setViewBinder((view, c, columnIndex) -> {
                if (view.getId() == R.id.muscle_image) {
                    ImageView imageView = (ImageView) view;
                    String muscleId   = c.getString(c.getColumnIndex(M._ID));
                    Utils.setImageFromAssets(mContext, imageView,
                            "muscles/"+ muscleId +".png");
                    return true;
                } else if (view.getId() == R.id.exercise_image) {
                    ImageView imageView = (ImageView) view;
                    String muscleId   = c.getString(c.getColumnIndex(E.MAIN_MUSCLE_ID));
                    String exerciseId = c.getString(c.getColumnIndex(E._ID));
                    Utils.setImageFromAssets(mContext, imageView,
                            "exercises/"+ muscleId +"/"+ exerciseId +"_1.jpg");
                    return true;
                }
                return false;
            });
        }

        @Override
        protected Cursor getChildrenCursor(Cursor groupCursor) {
            int groupPos = groupCursor.getPosition();
            int groupId = groupCursor.getInt(groupCursor.getColumnIndex(E._ID));

            Log.d(CLASS_NAME, "getChildrenCursor groupPos: "+ groupPos +", groupId: "+ groupId);
            mGroupMap.put(groupId, groupPos);

            Loader loader = mFragment.getLoaderManager().getLoader(groupId);
            if (loader != null && !loader.isReset()) {
                Log.d(CLASS_NAME, "restartLoader id: "+ groupId);
                mFragment.getLoaderManager().restartLoader(groupId, null, mFragment);
            } else {
                Log.d(CLASS_NAME, "initLoader id: "+ groupId);
                mFragment.getLoaderManager().initLoader(groupId, null, mFragment);
            }
            return null;
        }

        public HashMap<Integer, Integer> getGroupMap() {
            return mGroupMap;
        }
    }
}