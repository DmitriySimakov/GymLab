package com.dmitry_simakov.gymlab;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dmitry_simakov.gymlab.database.GymLabDbHelper;

public class ExerciseDescriptionFragment extends Fragment {

    public static final String CLASS_NAME = ExerciseDescriptionFragment.class.getSimpleName();

    private static class ExE extends com.dmitry_simakov.gymlab.database.DbContract.ExercisesEntry{}
    private static class ME  extends com.dmitry_simakov.gymlab.database.DbContract.MusclesEntry{}
    private static class TME extends com.dmitry_simakov.gymlab.database.DbContract.TargetedMusclesEntry{}
    private static class MTE extends com.dmitry_simakov.gymlab.database.DbContract.MechanicsTypesEntry{}
    private static class ETE extends com.dmitry_simakov.gymlab.database.DbContract.ExerciseTypesEntry{}
    private static class EqE extends com.dmitry_simakov.gymlab.database.DbContract.EquipmentEntry{}

    private Context mContext;

    public ExerciseDescriptionFragment() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mContext = context;

        FloatingActionButton fab = getActivity().findViewById(R.id.fab);
        fab.setVisibility(View.GONE);
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(CLASS_NAME, "onCreateView");

        View view = inflater.inflate(R.layout.activity_exercise_description, container, false);

        TextView mExerciseNameTextView    = view.findViewById(R.id.exercise_name);
        ImageView mImageView              = view.findViewById(R.id.images);
        TextView mMainMuscleTextView      = view.findViewById(R.id.main_muscle);
        TextView mTargetedMusclesTextView = view.findViewById(R.id.targeted_muscles);
        TextView mMechanicsTypeTextView   = view.findViewById(R.id.mechanics_type);
        TextView mExerciseTypeTextView    = view.findViewById(R.id.exercise_type);
        TextView mEquipmentTextView       = view.findViewById(R.id.equipment);
        TextView mDescriptionTextView     = view.findViewById(R.id.description);
        TextView mTechniqueTextView       = view.findViewById(R.id.technique);

        int exerciseId = getArguments().getInt(ExE._ID);

        try {
            GymLabDbHelper mDbHelper = new GymLabDbHelper(mContext);
            SQLiteDatabase db = mDbHelper.getWritableDatabase();

            Cursor cursor = db.rawQuery(
                    "SELECT "+
                            ExE.NAME +", "+
                            ExE.IMAGE +", "+
                            "(SELECT "+ ME.NAME  +" FROM "+ ME.TABLE_NAME  +" WHERE "+ ME._ID  +" = "+ ExE.MAIN_MUSCLE_ID    +") AS "+ ExE.MAIN_MUSCLE    +", "+
                            "(SELECT "+ MTE.NAME +" FROM "+ MTE.TABLE_NAME +" WHERE "+ MTE._ID +" = "+ ExE.MECHANICS_TYPE_ID +") AS "+ ExE.MECHANICS_TYPE +", "+
                            "(SELECT "+ ETE.NAME +" FROM "+ ETE.TABLE_NAME +" WHERE "+ ETE._ID +" = "+ ExE.EXERCISE_TYPE_ID  +") AS "+ ExE.EXERCISE_TYPE  +", "+
                            "(SELECT "+ EqE.NAME +" FROM "+ EqE.TABLE_NAME +" WHERE "+ EqE._ID +" = "+ ExE.EQUIPMENT_ID      +") AS "+ ExE.EQUIPMENT      +", "+
                            ExE.DESCRIPTION +", "+
                            ExE.TECHNIQUE +" "+
                            "FROM "+ ExE.TABLE_NAME +" "+
                            "WHERE "+ ExE._ID +" = ?",
                    new String[]{Integer.toString(exerciseId)});

            if (cursor.moveToFirst()) {
                int nameColumnIndex          = cursor.getColumnIndex(ExE.NAME);
                int imageColumnIndex         = cursor.getColumnIndex(ExE.IMAGE);
                int mainMuscleColumnIndex    = cursor.getColumnIndex(ExE.MAIN_MUSCLE);
                int mechanicsTypeColumnIndex = cursor.getColumnIndex(ExE.MECHANICS_TYPE);
                int exerciseTypeColumnIndex  = cursor.getColumnIndex(ExE.EXERCISE_TYPE);
                int equipmentColumnIndex     = cursor.getColumnIndex(ExE.EQUIPMENT);
                int descriptionColumnIndex   = cursor.getColumnIndex(ExE.DESCRIPTION);
                int techniqueColumnIndex     = cursor.getColumnIndex(ExE.TECHNIQUE);

                String name          = cursor.getString(nameColumnIndex);
                String imageName     = cursor.getString(imageColumnIndex);
                String mainMuscle    = cursor.getString(mainMuscleColumnIndex);
                String mechanicsType = cursor.getString(mechanicsTypeColumnIndex);
                String exerciseType  = cursor.getString(exerciseTypeColumnIndex);
                String equipment     = cursor.getString(equipmentColumnIndex);
                String description   = cursor.getString(descriptionColumnIndex);
                String technique     = cursor.getString(techniqueColumnIndex);

                getActivity().setTitle(name);

                mExerciseNameTextView.setText(name);
                if (imageName != null) {
                    Resources res = mContext.getResources();
                    int resID = res.getIdentifier(imageName, "drawable", mContext.getPackageName());
                    if (resID != 0) {
                        mImageView.setImageDrawable(res.getDrawable(resID));
                    }
                }
                mMainMuscleTextView.setText(mainMuscle);
                mMechanicsTypeTextView.setText(mechanicsType);
                mExerciseTypeTextView.setText(exerciseType);
                mEquipmentTextView.setText(equipment);
                mDescriptionTextView.setText(description);
                mTechniqueTextView.setText(technique);
            }

            cursor = db.rawQuery(
                    "SELECT " +
                            "(SELECT "+ ME.NAME +" FROM "+ ME.TABLE_NAME +" WHERE "+ ME._ID +" = "+ TME.MUSCLE_ID +") AS "+ TME.MUSCLE +" " +
                            "FROM "+ TME.TABLE_NAME +" " +
                            "WHERE "+ TME.EXERCISE_ID +" = ?",
                    new String[]{Integer.toString(exerciseId)});

            if (cursor.moveToFirst()) {
                int muscleColumnIndex = cursor.getColumnIndex(TME.MUSCLE);
                StringBuilder sb = new StringBuilder();
                do {
                    sb.append(cursor.getString(muscleColumnIndex));
                    sb.append(", ");
                } while (cursor.moveToNext());
                sb.setLength(sb.length() - 2); // Delete last delimiter

                mTargetedMusclesTextView.setText(sb.toString());
            }

            cursor.close();
            db.close();
        } catch(SQLiteException e) {
            Toast.makeText(mContext, "Ошибка при обращении к базе данных", Toast.LENGTH_SHORT).show();
        }
        return view;
    }
}

