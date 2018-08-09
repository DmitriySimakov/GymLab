package com.dmitry_simakov.gymlab.training_sessions;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dmitry_simakov.gymlab.Exercise;
import com.dmitry_simakov.gymlab.R;
import com.dmitry_simakov.gymlab.SharedViewModel;
import com.dmitry_simakov.gymlab.database.DatabaseContract;
import com.dmitry_simakov.gymlab.database.DatabaseHelper;
import com.dmitry_simakov.gymlab.exercises.ExercisesListFragment;
import com.dmitry_simakov.gymlab.measurements.MeasurementDialog;

public class TrainingSessionExerciseDialog extends AppCompatDialogFragment {

    public static final String CLASS_NAME = TrainingSessionSetDialog.class.getSimpleName();

    private static final class TSE extends DatabaseContract.TrainingSessionExerciseEntry {}
    private static final class Ex extends DatabaseContract.ExerciseEntry {}

    private Button mChooseExerciseBtn;
    private LinearLayout mChosenExerciseLL;
    private ImageView mExerciseIV, mRemoveExerciseIV;
    private TextView mExerciseNameTV;

    private CheckBox mWeightCB, mRepsCB, mTimeCB, mDistanceCB;

    private int mSessionId;
    private Exercise mExercise;


    public TrainingSessionExerciseDialog() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Bundle args = getArguments();
        if (args != null) {
            mSessionId = args.getInt(TSE.SESSION_ID);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d(CLASS_NAME, "onCreateDialog");

        Activity activity = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_training_session_exercise, null);
        builder.setView(view);

        mChooseExerciseBtn = view.findViewById(R.id.choose_exercise_btn);
        mChooseExerciseBtn.setOnClickListener(v -> {
            Fragment fragment = new ExercisesListFragment();
            Bundle bundle = new Bundle();
            bundle.putInt(TSE.SESSION_ID, mSessionId);
            fragment.setArguments(bundle);
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });
        mChosenExerciseLL = view.findViewById(R.id.chosen_exercise_ll);
        mExerciseIV       = view.findViewById(R.id.exercise_image);
        mExerciseNameTV   = view.findViewById(R.id.exercise_name);
        mRemoveExerciseIV = view.findViewById(R.id.remove_exercise_iv);
        mRemoveExerciseIV.setOnClickListener(v -> {
            mExercise = null;
            mChooseExerciseBtn.setVisibility(View.VISIBLE);
            mChosenExerciseLL.setVisibility(View.GONE);
        });
        mWeightCB   = view.findViewById(R.id.weight_cb);
        mRepsCB     = view.findViewById(R.id.reps_cb);
        mTimeCB     = view.findViewById(R.id.time_cb);
        mDistanceCB = view.findViewById(R.id.distance_cb);

        SharedViewModel model = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);
        model.getExercise().observe(this, exercise -> {
            mChooseExerciseBtn.setVisibility(View.GONE);
            mChosenExerciseLL.setVisibility(View.VISIBLE);
            mExercise = exercise;
            Resources res = getContext().getResources();
            if (exercise.getImageName() != null) {
                int resID = res.getIdentifier(exercise.getImageName(), "drawable", getContext().getPackageName());
                if (resID != 0) {
                    mExerciseIV.setImageDrawable(res.getDrawable(resID));
                }
            }
            mExerciseNameTV.setText(exercise.getExerciseName());
        });

        builder.setTitle("Упражнение");
        builder.setPositiveButton("Добавить", (dialog, which) -> {
            Log.d(CLASS_NAME, "negativeButton onClick");

            if (mExercise == null) {
                Toast.makeText(getContext(), "Выберете упражнение", Toast.LENGTH_LONG).show();
            } else {
                int boolArr = 0;
                if (mWeightCB.isChecked()) boolArr += 1000;
                if (mRepsCB.isChecked()) boolArr += 100;
                if (mTimeCB.isChecked()) boolArr += 10;
                if (mDistanceCB.isChecked()) boolArr += 1;
                DatabaseHelper.insertExerciseIntoSession(mSessionId, (int) mExercise.getId(), 1, boolArr);
                getContext().getContentResolver().notifyChange(TSE.CONTENT_URI, null);
                dialog.cancel();
            }
        });
        builder.setNegativeButton("Отмена", (dialog, which) -> {
            Log.d(CLASS_NAME, "negativeButton onClick");
            ViewModelProviders.of(getActivity()).get(SharedViewModel.class).getExercise().removeObservers(this);
            dialog.cancel();
        });

        return builder.create();
    }
}
