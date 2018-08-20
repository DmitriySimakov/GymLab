package com.dmitry_simakov.gymlab.exercises;

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
import android.widget.ImageView;
import android.widget.TextView;

import com.dmitry_simakov.gymlab.R;
import com.dmitry_simakov.gymlab.Utils;
import com.dmitry_simakov.gymlab.database.DatabaseContract;
import com.dmitry_simakov.gymlab.database.DatabaseHelper;

public class ExerciseDescriptionFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String CLASS_NAME = ExerciseDescriptionFragment.class.getSimpleName();

    private static final class Ex extends DatabaseContract.ExerciseEntry {}
    private static final class M extends DatabaseContract.MuscleEntry {}
    private static final class TM extends DatabaseContract.TargetedMuscleEntry {}
    private static final class MT extends DatabaseContract.MechanicsTypeEntry {}
    private static final class ET extends DatabaseContract.ExerciseTypeEntry {}
    private static final class Eq extends DatabaseContract.EquipmentEntry{}

    private ImageView mImageView1, mImageView2;
    private TextView mMainMuscleTextView, mTargetedMusclesTextView, mMechanicsTypeTextView,
            mExerciseTypeTextView, mEquipmentTextView, mDescriptionTextView;

    public ExerciseDescriptionFragment() {}

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(CLASS_NAME, "onCreateView");

        View view = inflater.inflate(R.layout.activity_exercise_description, container, false);

        mImageView1              = view.findViewById(R.id.image_1);
        mImageView2              = view.findViewById(R.id.image_2);
        mMainMuscleTextView      = view.findViewById(R.id.main_muscle);
        mTargetedMusclesTextView = view.findViewById(R.id.targeted_muscles);
        mMechanicsTypeTextView   = view.findViewById(R.id.mechanics_type);
        mExerciseTypeTextView    = view.findViewById(R.id.exercise_type);
        mEquipmentTextView       = view.findViewById(R.id.equipment);
        mDescriptionTextView     = view.findViewById(R.id.description);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(CLASS_NAME, "onActivityCreated");
        getLoaderManager().initLoader(0, null, this);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        return new MyCursorLoader(getContext(), getArguments().getInt(Ex._ID));
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor c) {
        if (c.moveToFirst()) {
            String name            = c.getString(c.getColumnIndex(Ex.NAME));
            String muscleId        = c.getString(c.getColumnIndex(Ex.MAIN_MUSCLE_ID));
            String id              = c.getString(c.getColumnIndex(Ex._ID));
            String mainMuscle      = c.getString(c.getColumnIndex(Ex.MAIN_MUSCLE));
            String targetedMuscles = c.getString(c.getColumnIndex(Ex.TARGETED_MUSCLES));
            String mechanicsType   = c.getString(c.getColumnIndex(Ex.MECHANICS_TYPE));
            String exerciseType    = c.getString(c.getColumnIndex(Ex.EXERCISE_TYPE));
            String equipment       = c.getString(c.getColumnIndex(Ex.EQUIPMENT));
            String description     = c.getString(c.getColumnIndex(Ex.DESCRIPTION));

            getActivity().setTitle(name);

            String imagePath = "exercises/" + muscleId +"/"+ id;
            Utils.setImageFromAssets(getContext(), mImageView1,  imagePath +"_1.jpeg");
            Utils.setImageFromAssets(getContext(), mImageView2, imagePath +"_2.jpeg");

            mMainMuscleTextView.setText(mainMuscle);
            mTargetedMusclesTextView.setText(targetedMuscles);
            mMechanicsTypeTextView.setText(mechanicsType);
            mExerciseTypeTextView.setText(exerciseType);
            mEquipmentTextView.setText(equipment);
            mDescriptionTextView.setText(description);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {}

    private static class MyCursorLoader extends CursorLoader {

        private final ForceLoadContentObserver mObserver = new ForceLoadContentObserver();

        private int mExerciseId;

        MyCursorLoader(Context context, int exerciseId) {
            super(context);
            mExerciseId = exerciseId;
            setUri(Ex.CONTENT_URI);
        }

        @Override
        public Cursor loadInBackground() {
            SQLiteDatabase db = DatabaseHelper.getInstance(getContext()).getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT "+
                            Ex._ID +", "+
                            Ex.NAME +", "+
                            Ex.MAIN_MUSCLE_ID +", "+
                            " (SELECT "+ M.NAME +" FROM "+ M.TABLE_NAME +" WHERE "+ M._ID +" = Ex."+ Ex.MAIN_MUSCLE_ID +") AS "+ Ex.MAIN_MUSCLE +", "+
                            " (SELECT group_concat(m."+ M.NAME +", ', ')"+
                                " FROM "+ TM.TABLE_NAME +" AS tm LEFT JOIN "+ M.TABLE_NAME +" AS m"+
                                " ON tm."+ TM.MUSCLE_ID +" = m." + M._ID +
                                " WHERE tm." + TM.EXERCISE_ID + " = ?) AS "+ Ex.TARGETED_MUSCLES +","+
                            " (SELECT "+ MT.NAME +" FROM "+ MT.TABLE_NAME +" WHERE "+ MT._ID +" = Ex."+ Ex.MECHANICS_TYPE_ID +") AS "+ Ex.MECHANICS_TYPE +", "+
                            " (SELECT "+ ET.NAME +" FROM "+ ET.TABLE_NAME +" WHERE "+ ET._ID +" = Ex."+ Ex.EXERCISE_TYPE_ID +") AS "+ Ex.EXERCISE_TYPE +", "+
                            " (SELECT "+ Eq.NAME +" FROM "+ Eq.TABLE_NAME +" WHERE "+ Eq._ID +" = Ex."+ Ex.EQUIPMENT_ID +") AS "+ Ex.EQUIPMENT +", "+
                            Ex.DESCRIPTION +
                            " FROM "+ Ex.TABLE_NAME +" AS Ex "+
                            " WHERE "+ Ex._ID +" = ?",
                    new String[]{ String.valueOf(mExerciseId), String.valueOf(mExerciseId) });

            if (cursor != null) {
                cursor.getCount(); // Fill cursor window
                cursor.registerContentObserver(mObserver);
                cursor.setNotificationUri(getContext().getContentResolver(), getUri());
            }

            return cursor;
        }
    }
}

