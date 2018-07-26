package com.dmitry_simakov.gymlab;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dmitry_simakov.gymlab.database.DbContract;
import com.dmitry_simakov.gymlab.database.MeasuresDbHelper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MeasurementDialog extends AppCompatDialogFragment {

    public static final String CLASS_NAME = MeasurementsListFragment.class.getSimpleName();

    public static final String PARAMETER_ID = "parameter_id";
    public static final String MEASUREMENT_ID = "measurement_id";

    private static class BM extends DbContract.BodyMeasurementsEntry {}
    private static class BP extends DbContract.BodyParametersEntry {}

    private Context mContext;

    private AlertDialog.Builder mBuilder;
    private ImageView mImageView;
    private TextView mInstructionTextView;
    private DatePicker mDatePicker;
    private EditText mValueEditText;

    private MeasuresDbHelper mDbHelper;
    private SQLiteDatabase mDatabase;
    private Cursor mCursor;

    public MeasurementDialog() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(CLASS_NAME, "onAttach");
        mContext = context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Activity activity = getActivity();
        mBuilder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_measurement_dialog, null);

        mImageView = view.findViewById(R.id.image);
        mInstructionTextView = view.findViewById(R.id.description);
        mDatePicker = view.findViewById(R.id.date_picker);
        mValueEditText = view.findViewById(R.id.value);

        mDbHelper = new MeasuresDbHelper(mContext);
        mDatabase = mDbHelper.getWritableDatabase();

        Bundle args = getArguments();
        if (args != null) {
            if (args.containsKey(PARAMETER_ID)) {
                newMeasurementInit(args.getInt(PARAMETER_ID));
            } else {
                editMeasurementInit(args.getInt(MEASUREMENT_ID));
            }
        }

        return mBuilder.setView(view).create();
    }

    private void newMeasurementInit(final int parameterId) {
        mCursor = mDatabase.rawQuery(
                "SELECT "+
                        BP.NAME +", " +
                        BP.IMAGE +", " +
                        BP.INSTRUCTION +" " +
                        "FROM "+ BP.TABLE_NAME +" " +
                        "WHERE "+ BP._ID +" = ?",
                new String[]{ Integer.toString(parameterId) });

        if (mCursor.moveToFirst()) {
            int nameColumnIndex        = mCursor.getColumnIndex(BP.NAME);
            int imageColumnIndex       = mCursor.getColumnIndex(BP.IMAGE);
            int instructionColumnIndex = mCursor.getColumnIndex(BP.INSTRUCTION);
            String name        = mCursor.getString(nameColumnIndex);
            String imageName   = mCursor.getString(imageColumnIndex);
            String instruction = mCursor.getString(instructionColumnIndex);

            mBuilder.setTitle(name);
            if (imageName != null) {
                Resources res = mContext.getResources();
                int resID = res.getIdentifier(imageName, "drawable", mContext.getPackageName());
                if (resID != 0) {
                    mImageView.setImageDrawable(res.getDrawable(resID));
                }
            }
            mInstructionTextView.setText(instruction);

            mBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();

                    try {
                        int value = Integer.parseInt(String.valueOf(mValueEditText.getText()));
                        String date = getISO8601FromDatePicker(mDatePicker);
                        MeasuresDbHelper.insertMeasurement(mDatabase, date, parameterId, value);
                    } catch(Exception e){
                        Toast.makeText(mContext, "Неверно введено значение", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            mBuilder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
        }
    }

    private static String getISO8601FromDatePicker(DatePicker datePicker){
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth();
        int year =  datePicker.getYear();

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        Date date = calendar.getTime();

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        return df.format(date);
    }

    private void editMeasurementInit(int measurementId) {

    }
}
