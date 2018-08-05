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
import android.widget.Toast;

import com.dmitry_simakov.gymlab.R;
import com.dmitry_simakov.gymlab.database.DatabaseContract;
import com.dmitry_simakov.gymlab.database.DatabaseHelper;

public class TrainingSessionSetDialog extends AppCompatDialogFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String CLASS_NAME = TrainingSessionSetDialog.class.getSimpleName();

    private static final class TSE extends DatabaseContract.TrainingSessionExerciseEntry {}
    private static final class TSS extends DatabaseContract.TrainingSessionSetEntry {}

    public static final String EXERCISE_ID = "exercise_id";
    public static final String SET_ID = "set_id";

    public static final int EDIT_SET_LOADER_ID = 1;

    private AlertDialog mDialog;

    private EditText mWeightET;
    private EditText mRepsET;
    private EditText mTimeET;
    private EditText mDistanceET;

    private SQLiteDatabase mDatabase;

    private int mExerciseId, mSetId;

    public TrainingSessionSetDialog() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(CLASS_NAME, "onAttach");

        mDatabase = DatabaseHelper.getInstance(context).getWritableDatabase();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d(CLASS_NAME, "onCreateDialog");

        Activity activity = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.DialogTheme);

        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_training_session_set, null);
        builder.setView(view);

        mWeightET = view.findViewById(R.id.weight_et);
        mRepsET = view.findViewById(R.id.reps_et);
        mTimeET = view.findViewById(R.id.time_et);
        mDistanceET = view.findViewById(R.id.distance_et);

        boolean[] arr = getArguments().getBooleanArray(TSE.PARAMS_BOOL_ARR);
        int[] labels = new int[] { R.id.weight_tv, R.id.reps_tv, R.id.time_tv, R.id.distance_tv };
        int[] layouts = new int[] { R.id.weight_ll, R.id.reps_ll, R.id.time_ll, R.id.distance_ll };
        for (int i = 0; i < 4; i++) {
            if (!arr[i]) {
                view.findViewById(labels[i]).setVisibility(View.GONE);
                view.findViewById(layouts[i]).setVisibility(View.GONE);
            }
        }

        builder.setPositiveButton("ОК", null);
        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(CLASS_NAME, "negativeButton onClick");
                dialog.cancel();
            }
        });

        return builder.create();
    }



    @Override
    public void onStart() {
        super.onStart();

        mDialog = (AlertDialog)getDialog();

        Bundle args = getArguments();
        if (args != null) {
            mExerciseId = args.getInt(EXERCISE_ID);
            if (args.containsKey(SET_ID)) {
                mSetId = args.getInt(SET_ID);
                getLoaderManager().initLoader(EDIT_SET_LOADER_ID, null, this);
            } else {
                newSetInit();
            }
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        return new EditCursorLoader(getContext(), mDatabase, mSetId);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        editSetInit(cursor);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {}

    private static class EditCursorLoader extends CursorLoader {

        private SQLiteDatabase mDatabase;
        private int mSetId;

        EditCursorLoader(Context context, SQLiteDatabase db, int setId) {
            super(context);
            mDatabase = db;
            mSetId = setId;
        }

        @Override
        public Cursor loadInBackground() {
            return mDatabase.rawQuery("SELECT "+
                            TSS.WEIGHT +", "+ TSS.REPS +", "+ TSS.TIME +", "+ TSS.DISTANCE +" "+
                            " FROM "+ TSS.TABLE_NAME +" WHERE "+ TSS._ID +" = ?",
                    new String[]{ String.valueOf(mSetId) });
        }
    }

    private void newSetInit() {
        mDialog.getButton(Dialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(CLASS_NAME, "positiveButton onClick");
                DatabaseHelper.insertSet(mExerciseId, 0,
                        getValue(mWeightET), getValue(mRepsET), getValue(mTimeET), getValue(mDistanceET));
                mDialog.dismiss();
            }
        });
    }

    private void editSetInit(Cursor c) {
        if (c.moveToFirst()) {
            mWeightET.setText(String.valueOf(c.getInt(c.getColumnIndex(TSS.WEIGHT))));
            mRepsET.setText(String.valueOf(c.getInt(c.getColumnIndex(TSS.REPS))));
            mTimeET.setText(String.valueOf(c.getInt(c.getColumnIndex(TSS.TIME))));
            mDistanceET.setText(String.valueOf(c.getInt(c.getColumnIndex(TSS.DISTANCE))));
        }

        mDialog.getButton(Dialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(CLASS_NAME, "positiveButton onClick");
                DatabaseHelper.updateSet(mSetId, getValue(mWeightET), getValue(mRepsET), getValue(mTimeET), getValue(mDistanceET));
                mDialog.dismiss();
            }
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
}
