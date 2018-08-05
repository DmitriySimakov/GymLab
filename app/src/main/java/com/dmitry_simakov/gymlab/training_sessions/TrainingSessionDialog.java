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
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.dmitry_simakov.gymlab.R;
import com.dmitry_simakov.gymlab.database.DatabaseContract;
import com.dmitry_simakov.gymlab.database.DatabaseHelper;

public class TrainingSessionDialog extends AppCompatDialogFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String CLASS_NAME = TrainingSessionSetDialog.class.getSimpleName();

    private static final class TS extends DatabaseContract.TrainingSessionEntry {}
    private static final class TP extends DatabaseContract.TrainingProgramEntry {}
    private static final class TPD extends DatabaseContract.TrainingProgramDayEntry {}

    private AlertDialog mDialog;

    private TextView mDateTV, mTimeTV, mProgramTV, mProgramDayTV;
    private Switch mSwitch;
    private LinearLayout mChoseProgramLL;

    public TrainingSessionDialog() {}

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d(CLASS_NAME, "onCreateDialog");

        Activity activity = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.DialogTheme);

        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_training_session, null);
        builder.setView(view);

        mDateTV = view.findViewById(R.id.date_tv);
        mTimeTV = view.findViewById(R.id.time_tv);
        mProgramTV = view.findViewById(R.id.program_tv);
        mProgramDayTV = view.findViewById(R.id.program_day_tv);
        mSwitch = view.findViewById(R.id.program_switch);
        mChoseProgramLL = view.findViewById(R.id.chose_program_ll);

        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mChoseProgramLL.setVisibility(View.VISIBLE);
                } else {
                    mChoseProgramLL.setVisibility(View.GONE);
                }
            }
        });

        builder.setTitle("Тренировка");
        builder.setPositiveButton("Начать", null);
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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mDialog = (AlertDialog)getDialog();
        getLoaderManager().initLoader(0, null, this);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        return new TrainingProgramCursorLoader(getContext());
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, final Cursor c) {
        if (c.moveToFirst()) {
            mSwitch.setChecked(true);
            mProgramTV.setText(c.getString(c.getColumnIndex(TPD.PROGRAM)));
            mProgramDayTV.setText(c.getString(c.getColumnIndex(TS.TRAINING_DAY)));
        } else {
            mChoseProgramLL.setVisibility(View.GONE);
        }

        mDialog.getButton(Dialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(CLASS_NAME, "positiveButton onClick");

                Fragment fragment = new TrainingSessionExercisesFragment();
                Bundle bundle;
                if (c.moveToFirst()) {
                    bundle = new Bundle();
                    int id = c.getInt(c.getColumnIndex(TS.TRAINING_DAY_ID));
                    bundle.putInt(TS.TRAINING_DAY_ID, (int)id);
                    fragment.setArguments(bundle);
                }
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
                mDialog.dismiss();
            }
        });
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {}

    private static class TrainingProgramCursorLoader extends CursorLoader {

        TrainingProgramCursorLoader(Context context) {
            super(context);
        }

        @Override
        public Cursor loadInBackground() {
            SQLiteDatabase db = DatabaseHelper.getInstance(getContext()).getReadableDatabase();
            return db.rawQuery("SELECT tp."+ TP.NAME +" AS "+ TPD.PROGRAM +","+
                            " tpd."+ TPD.NAME +" AS "+ TS.TRAINING_DAY +","+
                            " ts."+ TS.TRAINING_DAY_ID +
                            " FROM "+ TS.TABLE_NAME +" AS ts"+
                            " LEFT JOIN "+ TPD.TABLE_NAME +" AS tpd"+
                            " ON ts."+ TS.TRAINING_DAY_ID +" = tpd."+ TPD._ID +
                            " LEFT JOIN "+ TP.TABLE_NAME +" AS tp"+
                            " WHERE ts."+ TS.TRAINING_DAY_ID +" IS NOT NULL"+
                            " ORDER BY "+ TS.DATE_TIME +" DESC LIMIT 1",
                    null);
        }
    }
}
