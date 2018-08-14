package com.dmitry_simakov.gymlab.training_sessions;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
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
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.dmitry_simakov.gymlab.R;
import com.dmitry_simakov.gymlab.database.DatabaseContract;
import com.dmitry_simakov.gymlab.database.DatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TrainingSessionDialog extends AppCompatDialogFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String CLASS_NAME = TrainingSessionSetDialog.class.getSimpleName();

    private static final class TS extends DatabaseContract.TrainingSessionEntry {}
    private static final class TSE extends DatabaseContract.TrainingSessionExerciseEntry {}
    private static final class TP extends DatabaseContract.TrainingProgramEntry {}
    private static final class TPD extends DatabaseContract.TrainingProgramDayEntry {}

    private AlertDialog mDialog;

    private TextView mDateTV, mTimeTV, mProgramTV, mProgramDayTV;
    private Switch mSwitch;
    private LinearLayout mChoseProgramLL;

    private String mDate, mTime;

    private OnTrainingStateChangeListener mListener;


    public TrainingSessionDialog() {}

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

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_training_session, null);
        builder.setView(view);

        mDateTV = view.findViewById(R.id.date_tv);
        mTimeTV = view.findViewById(R.id.time_tv);
        mProgramTV = view.findViewById(R.id.program_tv);
        mProgramDayTV = view.findViewById(R.id.program_day_tv);
        mSwitch = view.findViewById(R.id.program_switch);
        mChoseProgramLL = view.findViewById(R.id.chose_program_ll);

        mSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                mChoseProgramLL.setVisibility(View.VISIBLE);
            } else {
                mChoseProgramLL.setVisibility(View.GONE);
            }
        });

        builder.setTitle("Тренировка");
        builder.setPositiveButton("Начать", null);
        builder.setNegativeButton("Отмена", (dialog, which) -> {
            Log.d(CLASS_NAME, "negativeButton onClick");
            dialog.cancel();
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
        Calendar calendar = Calendar.getInstance();
        mDate = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
        mTime = new SimpleDateFormat("HH:mm:ss").format(calendar.getTime());
        mDateTV.setText(mDate);
        mTimeTV.setText(mTime.substring(0, 5));
        setDatePickerDialog(calendar);
        setTimePickerDialog(calendar);
        setTrainingProgramWindow(c);
        setPositiveButton(c);
    }

    private void setDatePickerDialog(final Calendar calendar) {
        mDateTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(CLASS_NAME, "mDateTV onClick");

                int year  = calendar.get(Calendar.YEAR);
                int monthOfYear = calendar.get(Calendar.MONTH);
                int day   = calendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog dialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        Log.d(CLASS_NAME, "DatePickerDialog onDateSet");

                        Calendar calendar = Calendar.getInstance();
                        calendar.set(year, monthOfYear, dayOfMonth);
                        mDate = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
                        mDateTV.setText(mDate);
                    }
                }, year, monthOfYear, day);

                DatePicker datePicker = dialog.getDatePicker();
                datePicker.setMinDate(0);
                long millisecondsPerMonth = (long)30*24*60*60*1000;
                datePicker.setMaxDate(new Date().getTime() + millisecondsPerMonth);
                dialog.show();
            }
        });
    }

    private void setTimePickerDialog(final Calendar calendar) {
        mTimeTV.setOnClickListener(v -> {
            Log.d(CLASS_NAME, "mTimeTV onClick");

            int hourOfDay  = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            TimePickerDialog dialog = new TimePickerDialog(getContext(), (view, hourOfDay1, minute1) -> {
                Log.d(CLASS_NAME, "DatePickerDialog onDateSet");

                Calendar calendar1 = Calendar.getInstance();
                calendar1.set(1970, 0, 1, hourOfDay1, minute1);
                mTime = new SimpleDateFormat("HH:mm:ss").format(calendar1.getTime());
                mTimeTV.setText(mTime.substring(0, 5));
            }, hourOfDay, minute, true);
            dialog.show();
        });
    }

    private void setTrainingProgramWindow(Cursor c) {
        if (c.moveToFirst()) {
            mSwitch.setChecked(true);
            mProgramTV.setText(c.getString(c.getColumnIndex(TPD.PROGRAM)));
            mProgramDayTV.setText(c.getString(c.getColumnIndex(TS.TRAINING_DAY)));
        } else {
            mChoseProgramLL.setVisibility(View.GONE);
        }
    }

    private void setPositiveButton(Cursor c) {
        mDialog.getButton(Dialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            Log.d(CLASS_NAME, "positiveButton onClick");

            ContentValues cv = new ContentValues();
            Bundle bundle = new Bundle();

            // Put training program day
            if (c.moveToFirst()) {
                int trainingDayId = c.getInt(c.getColumnIndex(TS.TRAINING_DAY_ID)); // TODO get id from TextView
                cv.put(TS.TRAINING_DAY_ID, trainingDayId);
                bundle.putInt(TS.TRAINING_DAY_ID, trainingDayId);
            }

            // Put date_time
            String dateTime = mDate +" "+ mTime;
            cv.put(TS.DATE_TIME, dateTime);
            mListener.onStartTrainingSession(dateTime);

            // Insert training session and put it ID
            long sessionId = DatabaseHelper.insertTrainingSession(cv);
            getContext().getContentResolver().notifyChange(TS.CONTENT_URI, null);
            bundle.putInt(TSE.SESSION_ID, (int)sessionId);

            mDialog.dismiss();

            Fragment fragment = new TrainingSessionExercisesFragment();
            fragment.setArguments(bundle);

            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
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
            Cursor cursor = db.rawQuery(
                    // 4) Find the data for the day you want
                "SELECT" +
                        " tp."+ TP.NAME +" AS "+ TPD.PROGRAM +","+
                        " tpd4."+ TPD.NAME +" AS "+ TS.TRAINING_DAY +","+
                        " tpd4."+ TPD._ID +" AS "+ TS.TRAINING_DAY_ID +
                        " FROM ("+
                            // 3) If there is no such day, take the first day from this program
                        " SELECT"+
                            " last_prog_id,"+
                            " ifnull(last_number_plus_1, 1) AS desired_number"+
                            " FROM ("+
                                // 2) Looking for a training day from the same program, whose number is 1 more.
                                " SELECT" +
                                " last_prog_id," +
                                " tpd2."+ TPD.NUMBER +" AS last_number_plus_1"+
                                " FROM ("+
                                    // 1) Find the training day we trained for the last time
                                    " SELECT" +
                                    " tpd1."+ TPD.PROGRAM_ID +" AS last_prog_id,"+
                                    " tpd1."+ TPD.NUMBER +" AS last_number"+
                                    " FROM "+ TS.TABLE_NAME +" AS ts"+
                                    " LEFT JOIN "+ TPD.TABLE_NAME +" AS tpd1"+
                                    " ON ts."+ TS.TRAINING_DAY_ID +" = tpd1."+ TPD._ID +
                                    " WHERE ts."+ TS.TRAINING_DAY_ID +" IS NOT NULL"+
                                    " ORDER BY ts."+ TS.DATE_TIME +
                                    " DESC LIMIT 1"+
                                " )"+
                                " LEFT JOIN "+ TPD.TABLE_NAME +" AS tpd2"+
                                " ON last_prog_id = tpd2."+ TPD.PROGRAM_ID +
                                " AND last_number + 1 = tpd2."+ TPD.NUMBER +
                            " )"+
                            " LEFT JOIN "+ TPD.TABLE_NAME +" AS tpd3"+
                            " ON last_prog_id = tpd3."+ TPD.PROGRAM_ID +
                            " AND last_number_plus_1 = tpd3."+ TPD.NUMBER +
                        " )"+
                        " INNER JOIN "+ TPD.TABLE_NAME +" AS tpd4"+
                        " ON last_prog_id = tpd4."+ TPD.PROGRAM_ID +
                        " AND desired_number = tpd4."+ TPD.NUMBER +
                        " INNER JOIN "+ TP.TABLE_NAME +" AS tp"+
                        " ON last_prog_id = tp."+ TP._ID,
                    null);
            if (cursor != null) {
                cursor.getCount(); // Fill cursor window
            }
            return cursor;
        }
    }
}
