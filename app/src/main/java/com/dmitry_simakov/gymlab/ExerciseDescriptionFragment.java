package com.dmitry_simakov.gymlab;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
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

import com.dmitry_simakov.gymlab.database.DbContract;
import com.dmitry_simakov.gymlab.database.GymLabDbHelper;

public class ExerciseDescriptionFragment extends Fragment {

    public static final String CLASS_NAME = ExerciseDescriptionFragment.class.getSimpleName();

    private static class Ex extends DbContract.ExercisesEntry{}
    private static class M extends DbContract.MusclesEntry{}
    private static class TM extends DbContract.TargetedMusclesEntry{}
    private static class MT extends DbContract.MechanicsTypesEntry{}
    private static class ET extends DbContract.ExerciseTypesEntry{}
    private static class Eq extends DbContract.EquipmentEntry{}

    private Context mContext;

    public ExerciseDescriptionFragment() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
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

        int exerciseId = getArguments().getInt(Ex._ID);

        try {
            GymLabDbHelper mDbHelper = new GymLabDbHelper(mContext);
            SQLiteDatabase db = mDbHelper.getWritableDatabase();

            Cursor cursor = db.rawQuery(
                    "SELECT "+
                            Ex.NAME +", "+
                            Ex.IMAGE +", "+
                            "(SELECT "+ M.NAME  +" FROM "+ M.TABLE_NAME  +" WHERE "+ M._ID  +" = Ex."+ Ex.MAIN_MUSCLE_ID    +") AS "+ Ex.MAIN_MUSCLE    +", "+
                            "(SELECT "+ MT.NAME +" FROM "+ MT.TABLE_NAME +" WHERE "+ MT._ID +" = Ex."+ Ex.MECHANICS_TYPE_ID +") AS "+ Ex.MECHANICS_TYPE +", "+
                            "(SELECT "+ ET.NAME +" FROM "+ ET.TABLE_NAME +" WHERE "+ ET._ID +" = Ex."+ Ex.EXERCISE_TYPE_ID  +") AS "+ Ex.EXERCISE_TYPE  +", "+
                            "(SELECT "+ Eq.NAME +" FROM "+ Eq.TABLE_NAME +" WHERE "+ Eq._ID +" = Ex."+ Ex.EQUIPMENT_ID      +") AS "+ Ex.EQUIPMENT      +", "+
                            Ex.DESCRIPTION +", "+
                            Ex.TECHNIQUE +" "+
                            "FROM "+ Ex.TABLE_NAME +" AS Ex "+
                            "WHERE "+ Ex._ID +" = ?",
                    new String[]{ String.valueOf(exerciseId) });

            if (cursor.moveToFirst()) {
                int nameColumnIndex          = cursor.getColumnIndex(Ex.NAME);
                int imageColumnIndex         = cursor.getColumnIndex(Ex.IMAGE);
                int mainMuscleColumnIndex    = cursor.getColumnIndex(Ex.MAIN_MUSCLE);
                int mechanicsTypeColumnIndex = cursor.getColumnIndex(Ex.MECHANICS_TYPE);
                int exerciseTypeColumnIndex  = cursor.getColumnIndex(Ex.EXERCISE_TYPE);
                int equipmentColumnIndex     = cursor.getColumnIndex(Ex.EQUIPMENT);
                int descriptionColumnIndex   = cursor.getColumnIndex(Ex.DESCRIPTION);
                int techniqueColumnIndex     = cursor.getColumnIndex(Ex.TECHNIQUE);

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
                            "(SELECT "+ M.NAME +" FROM "+ M.TABLE_NAME +" WHERE "+ M._ID +" = TM."+ TM.MUSCLE_ID +") AS "+ TM.MUSCLE +" " +
                            "FROM "+ TM.TABLE_NAME +" AS TM " +
                            "WHERE "+ TM.EXERCISE_ID +" = ?",
                    new String[]{ String.valueOf(exerciseId) });

            if (cursor.moveToFirst()) {
                int muscleColumnIndex = cursor.getColumnIndex(TM.MUSCLE);
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

