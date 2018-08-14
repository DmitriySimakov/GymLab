package com.dmitry_simakov.gymlab.training_sessions;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.dmitry_simakov.gymlab.R;
import com.dmitry_simakov.gymlab.database.DatabaseContract;
import com.dmitry_simakov.gymlab.database.DatabaseHelper;

public class TrainingSessionSetDialog extends AppCompatDialogFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String CLASS_NAME = TrainingSessionSetDialog.class.getSimpleName();

    private static final class TSE extends DatabaseContract.TrainingSessionExerciseEntry {}
    private static final class TSS extends DatabaseContract.TrainingSessionSetEntry {}

    public static final int EDIT_SET_LOADER_ID = 1;

    private AlertDialog mDialog;

    private EditText mWeightET, mRepsET, mTimeET, mDistanceET;

    private int mExerciseId, mSetId;

    private OnTrainingStateChangeListener mListener;

    public TrainingSessionSetDialog() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnTrainingStateChangeListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnTrainingStateChangeListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d(CLASS_NAME, "onCreateDialog");

        Activity activity = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_training_session_set, null);
        builder.setView(view);

        mWeightET = view.findViewById(R.id.weight_et);
        mRepsET = view.findViewById(R.id.reps_et);
        mTimeET = view.findViewById(R.id.time_et);
        mDistanceET = view.findViewById(R.id.distance_et);

        configureParameterViews(view);

        builder.setPositiveButton("ОК", null);
        builder.setNegativeButton("Отмена", (dialog, which) -> {
            dialog.cancel();
        });

        return builder.create();
    }

    private void configureParameterViews(View v) {
        boolean[] arr = getArguments().getBooleanArray(TSE.PARAMS_BOOL_ARR);
        int[] labels = new int[] { R.id.weight_tv, R.id.reps_tv, R.id.time_tv, R.id.distance_tv };
        int[] layouts = new int[] { R.id.weight_ll, R.id.reps_ll, R.id.time_ll, R.id.distance_ll };
        for (int i = 0; i < 4; i++) {
            if (!arr[i]) {
                v.findViewById(labels[i]).setVisibility(View.GONE);
                v.findViewById(layouts[i]).setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        mDialog = (AlertDialog)getDialog();

        Bundle args = getArguments();
        if (args != null) {
            mExerciseId = args.getInt(TSS.TS_EXERCISE_ID);
            if (args.containsKey(TSS._ID)) {
                mSetId = args.getInt(TSS._ID);
                getLoaderManager().initLoader(EDIT_SET_LOADER_ID, null, this);
            } else {
                newSetInit();
            }
        }
    }

    private void newSetInit() {
        mDialog.getButton(Dialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            Log.d(CLASS_NAME, "positiveButton onClick");
            int secsSinceStart = mListener.onFinishSet();
            DatabaseHelper.insertSet(mExerciseId, secsSinceStart,
                    getValue(mWeightET), getValue(mRepsET), getValue(mTimeET), getValue(mDistanceET));
            getContext().getContentResolver().notifyChange(TSS.CONTENT_URI, null);
            mDialog.dismiss();
        });
    }

    private int getValue(EditText et) {
        int value = 0;
        try {
            value = Integer.parseInt(String.valueOf(et.getText()));
        } catch(Exception e){
            Log.d(CLASS_NAME, "parseDouble failed!");
        }
        return value;
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        return new EditCursorLoader(getContext(), mSetId);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        editSetInit(cursor);
    }

    private void editSetInit(Cursor c) {
        if (c.moveToFirst()) {
            mWeightET.setText(String.valueOf(c.getInt(c.getColumnIndex(TSS.WEIGHT))));
            mRepsET.setText(String.valueOf(c.getInt(c.getColumnIndex(TSS.REPS))));
            mTimeET.setText(String.valueOf(c.getInt(c.getColumnIndex(TSS.TIME))));
            mDistanceET.setText(String.valueOf(c.getInt(c.getColumnIndex(TSS.DISTANCE))));
        }

        mDialog.getButton(Dialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            Log.d(CLASS_NAME, "positiveButton onClick");
            DatabaseHelper.updateSet(mSetId, getValue(mWeightET), getValue(mRepsET), getValue(mTimeET), getValue(mDistanceET));
            getContext().getContentResolver().notifyChange(TSS.CONTENT_URI, null);
            mDialog.dismiss();
        });
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {}

    private static class EditCursorLoader extends CursorLoader {

        private int mSetId;

        EditCursorLoader(Context context, int setId) {
            super(context);
            mSetId = setId;
        }

        @Override
        public Cursor loadInBackground() {
            SQLiteDatabase db = DatabaseHelper.getInstance(getContext()).getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT "+
                            TSS.WEIGHT +", "+ TSS.REPS +", "+ TSS.TIME +", "+ TSS.DISTANCE +" "+
                            " FROM "+ TSS.TABLE_NAME +" WHERE "+ TSS._ID +" = ?",
                    new String[]{ String.valueOf(mSetId) });
            if (cursor != null) {
                cursor.getCount(); // Fill cursor window
            }
            return cursor;
        }
    }
}
