package com.dmitry_simakov.gymlab;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
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

    public static final String CLASS_NAME = MeasurementDialog.class.getSimpleName();

    public static final String PARAMETER_ID = "parameter_id";
    public static final String MEASUREMENT_ID = "measurement_id";

    private static class BM extends DbContract.BodyMeasurementsEntry {}
    private static class BP extends DbContract.BodyParametersEntry {}

    private Context mContext;

    private TextView mDateTextView;
    private ImageView mImageView;
    private TextView mInstructionTextView;
    private EditText mValueEditText;

    private MeasuresDbHelper mDbHelper;
    private SQLiteDatabase mDatabase;
    private Cursor mCursor;

    private AlertDialog mDialog;
    private int mYear, mMonth, mDay;

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
        Log.d(CLASS_NAME, "onCreateDialog");
        Activity activity = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_measurement_dialog, null);
        builder.setView(view);

        mDateTextView = view.findViewById(R.id.date);
        mDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(CLASS_NAME, "mDateTextView onClick");

                DatePickerDialog dialog = new DatePickerDialog(mContext, R.style.DialogTheme, new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        Log.d(CLASS_NAME, "DatePickerDialog onDateSet");

                        mYear = year;
                        mMonth = monthOfYear;
                        mDay = dayOfMonth;
                        mDateTextView.setText(mYear +"-"+ mMonth +"-"+ mDay);
                    }
                }, mYear, mMonth, mDay);

                DatePicker datePicker = dialog.getDatePicker();
                datePicker.setMinDate(0);
                datePicker.setMaxDate(new Date().getTime());
                dialog.show();
            }
        });

        mImageView = view.findViewById(R.id.image);
        mInstructionTextView = view.findViewById(R.id.description);
        mValueEditText = view.findViewById(R.id.value);

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
        Log.d(CLASS_NAME, "onStart");

        mDbHelper = new MeasuresDbHelper(mContext);
        mDatabase = mDbHelper.getWritableDatabase();

        mDialog = (AlertDialog)getDialog();

        Bundle args = getArguments();
        if (args != null) {
            if (args.containsKey(PARAMETER_ID)) {
                newMeasurementInit(args.getInt(PARAMETER_ID));
            } else {
                editMeasurementInit(args.getInt(MEASUREMENT_ID));
            }
        }
    }

    @Override
    public void onDestroy() {
        Log.d(CLASS_NAME, "onDestroy");
        super.onDestroy();

        if (mCursor != null) mCursor.close();
        if (mDatabase != null) mDatabase.close();
    }

    private void newMeasurementInit(final int parameterId) {
        Log.d(CLASS_NAME, "newMeasurementInit");

        mCursor = mDatabase.rawQuery(
                "SELECT "+
                        BP.NAME +", " +
                        BP.IMAGE +", " +
                        BP.INSTRUCTION +" " +
                        "FROM "+ BP.TABLE_NAME +" " +
                        "WHERE "+ BP._ID +" = ?",
                new String[]{ String.valueOf(parameterId) });

        if (mCursor.moveToFirst()) {
            int nameColumnIndex        = mCursor.getColumnIndex(BP.NAME);
            int imageColumnIndex       = mCursor.getColumnIndex(BP.IMAGE);
            int instructionColumnIndex = mCursor.getColumnIndex(BP.INSTRUCTION);
            final String name  = mCursor.getString(nameColumnIndex);
            int image          = mCursor.getInt(imageColumnIndex);
            String instruction = mCursor.getString(instructionColumnIndex);

            if(mDialog != null) {
                Calendar calendar = Calendar.getInstance();
                mYear = calendar.get(Calendar.YEAR);
                mMonth = calendar.get(Calendar.MONTH);
                mDay = calendar.get(Calendar.DAY_OF_MONTH);
                mDateTextView.setText(getISO8601(mYear, mMonth, mDay));

                mDialog.setTitle(name);
                mImageView.setImageResource(image);
                mInstructionTextView.setText(instruction);

                mDialog.getButton(Dialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(CLASS_NAME, "positiveButton onClick");

                        final double value;
                        try {
                            value = Double.parseDouble(String.valueOf(mValueEditText.getText()));
                        } catch(Exception e){
                            Toast.makeText(mContext, "Неверно введено значение", Toast.LENGTH_LONG).show();
                            return;
                        }

                        final String date = getISO8601(mYear, mMonth, mDay);
                        Cursor cursor = mDatabase.rawQuery("SELECT "+ BM._ID +" " +
                                        "FROM "+ BM.TABLE_NAME +" " +
                                        "WHERE "+ BM.DATE +" = ? " +
                                        "AND "+ BM.BODY_PARAMETER_ID +" = ?",
                                new String[]{ date, String.valueOf(parameterId) });

                        if (cursor.moveToFirst()) {
                            final int id = cursor.getInt(0);
                            cursor.close();

                            AlertDialog.Builder alert = getParamAlreadyExistAlert(date, name);
                            alert.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int arg1) {
                                    Log.d(CLASS_NAME, "alertDialog positiveButton onClick");

                                    MeasuresDbHelper.updateMeasurement(mDatabase, id, date, value);
                                    mDialog.dismiss();
                                }
                            });
                            alert.show();
                        } else {
                            cursor.close();
                            MeasuresDbHelper.insertMeasurement(mDatabase, date, parameterId, value);
                            mDialog.dismiss();
                        }
                    }
                });
            }
        }
        mCursor.close();
    }

    private void editMeasurementInit(final int measurementId) {
        Log.d(CLASS_NAME, "editMeasurementInit");

        mCursor = mDatabase.rawQuery(
                "SELECT "+
                        "(SELECT "+ BP.NAME        +" FROM "+ BP.TABLE_NAME +" WHERE "+ BP._ID +" = BM."+ BM.BODY_PARAMETER_ID +") AS "+ BM.BODY_PARAMETER +", "+
                        BM.DATE +", "+
                        "(SELECT "+ BP.IMAGE       +" FROM "+ BP.TABLE_NAME +" WHERE "+ BP._ID +" = BM."+ BM.BODY_PARAMETER_ID +") AS "+ BP.IMAGE +", "+
                        "(SELECT "+ BP.INSTRUCTION +" FROM "+ BP.TABLE_NAME +" WHERE "+ BP._ID +" = BM."+ BM.BODY_PARAMETER_ID +") AS "+ BP.INSTRUCTION +", "+
                        BM.BODY_PARAMETER_ID +", "+
                        BM.VALUE +" "+
                        "FROM "+ BM.TABLE_NAME +" AS BM "+
                        "WHERE "+ BM._ID +" = ?",
                new String[]{ String.valueOf(measurementId) });

        if (mCursor.moveToFirst()) {
            int nameColumnIndex        = mCursor.getColumnIndex(BM.BODY_PARAMETER);
            int dateColumnIndex        = mCursor.getColumnIndex(BM.DATE);
            int imageColumnIndex       = mCursor.getColumnIndex(BP.IMAGE);
            int instructionColumnIndex = mCursor.getColumnIndex(BP.INSTRUCTION);
            int parameterIdColumnIndex = mCursor.getColumnIndex(BM.BODY_PARAMETER_ID);
            int valueColumnIndex       = mCursor.getColumnIndex(BM.VALUE);
            final String name     = mCursor.getString(nameColumnIndex);
            String date           = mCursor.getString(dateColumnIndex);
            int image             = mCursor.getInt(imageColumnIndex);
            String instruction    = mCursor.getString(instructionColumnIndex);
            final int parameterId = mCursor.getInt(parameterIdColumnIndex);
            double value          = mCursor.getDouble(valueColumnIndex);

            mYear  = Integer.parseInt(date.substring(0, 4));
            mMonth = Integer.parseInt(date.substring(6, 7));
            mDay   = Integer.parseInt(date.substring(9));

            if(mDialog != null) {
                mDialog.setTitle(name);
                mDateTextView.setText(date);
                mImageView.setImageResource(image);
                mInstructionTextView.setText(instruction);
                mValueEditText.setText(String.valueOf(value));

                mDialog.getButton(Dialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(CLASS_NAME, "positiveButton onClick");

                        final double value;
                        try {
                            value = Double.parseDouble(String.valueOf(mValueEditText.getText()));
                        } catch(Exception e){
                            Toast.makeText(mContext, "Неверно введено значение", Toast.LENGTH_LONG).show();
                            return;
                        }

                        final String date = getISO8601(mYear, mMonth, mDay);
                        Cursor cursor = mDatabase.rawQuery("SELECT "+ BM._ID +" " +
                                        "FROM "+ BM.TABLE_NAME +" " +
                                        "WHERE "+ BM.DATE +" = ? " +
                                        "AND "+ BM.BODY_PARAMETER_ID +" = ? " +
                                        "AND "+ BM._ID +" <> ?",
                                new String[]{ date, String.valueOf(parameterId), String.valueOf(measurementId) });
                        if (cursor.moveToFirst()) {
                            final int id = cursor.getInt(0);
                            cursor.close();

                            AlertDialog.Builder alert = getParamAlreadyExistAlert(date, name);
                            alert.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int arg1) {
                                    Log.d(CLASS_NAME, "alertDialog positiveButton onClick");

                                    MeasuresDbHelper.updateMeasurement(mDatabase, id, date, value);
                                    MeasuresDbHelper.deleteMeasurement(mDatabase, measurementId);
                                    mDialog.dismiss();
                                }
                            });
                            alert.show();
                        } else {
                            cursor.close();
                            MeasuresDbHelper.updateMeasurement(mDatabase, measurementId, date, value);
                            mDialog.dismiss();
                        }
                    }
                });
            }
        }
        mCursor.close();
    }

    private static String getISO8601(int year, int month, int day) {
        Log.d(CLASS_NAME, "getISO8601");

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        Date date = calendar.getTime();

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        return df.format(date);
    }

    private AlertDialog.Builder getParamAlreadyExistAlert(String date, String param) {
        Log.d(CLASS_NAME, "getParamAlreadyExistAlert");

        AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
        alert.setTitle(date +" уже задан параметр "+ param);
        alert.setMessage("Хотите заменить его новым?"); // сообщение
        alert.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface d, int arg1) {
                d.cancel();
            }
        });
        alert.setCancelable(false);
        return alert;
    }
}
