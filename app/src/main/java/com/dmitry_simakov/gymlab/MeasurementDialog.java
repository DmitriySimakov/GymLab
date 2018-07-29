package com.dmitry_simakov.gymlab;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
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

    private String mName, mDate, mInstruction;
    private int mImage, mParameterId, mMeasurementId;
    private double mValue;

    private MeasuresDbHelper mDbHelper;
    private SQLiteDatabase mDatabase;
    private Cursor mCursor;

    private AlertDialog mDialog;

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
                mParameterId = args.getInt(PARAMETER_ID);
                newMeasurementInit();
            } else {
                mMeasurementId = args.getInt(MEASUREMENT_ID);
                editMeasurementInit();
            }
        }
        if (mCursor != null) mCursor.close();
    }

    @Override
    public void onDestroy() {
        Log.d(CLASS_NAME, "onDestroy");
        super.onDestroy();

        if (mCursor != null) mCursor.close();
        if (mDatabase != null) mDatabase.close();
    }

    private void newMeasurementInit() {
        Log.d(CLASS_NAME, "newMeasurementInit");

        mCursor = mDatabase.rawQuery("SELECT "+ BP.NAME +", "+ BP.IMAGE +", "+ BP.INSTRUCTION +
                        " FROM "+ BP.TABLE_NAME +
                        " WHERE "+ BP._ID +" = ?",
                new String[]{ String.valueOf(mParameterId) });

        if (mCursor.moveToFirst()) {
            int nameColumnIndex        = mCursor.getColumnIndex(BP.NAME);
            int imageColumnIndex       = mCursor.getColumnIndex(BP.IMAGE);
            int instructionColumnIndex = mCursor.getColumnIndex(BP.INSTRUCTION);
            mName        = mCursor.getString(nameColumnIndex);
            mImage       = mCursor.getInt(imageColumnIndex);
            mInstruction = mCursor.getString(instructionColumnIndex);

            Calendar calendar = Calendar.getInstance();
            int year  = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1; //  month = monthOfYear + 1
            int day   = calendar.get(Calendar.DAY_OF_MONTH);
            setDatePickerDialog(year, month, day);

            mDate = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
            mDateTextView.setText(mDate);

            mDialog.setTitle(mName);
            mImageView.setImageResource(mImage);
            mInstructionTextView.setText(mInstruction);

            mDialog.getButton(Dialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(CLASS_NAME, "positiveButton onClick");

                    if (!validateValue()) return;

                    Cursor cursor = mDatabase.rawQuery("SELECT "+ BM._ID +" FROM "+ BM.TABLE_NAME +
                                    " WHERE "+ BM.DATE +" = ? AND "+ BM.BODY_PARAMETER_ID +" = ?",
                            new String[]{ mDate, String.valueOf(mParameterId) });

                    if (cursor.moveToFirst()) {
                        mMeasurementId = cursor.getInt(0);

                        AlertDialog.Builder alert = getParamAlreadyExistAlert(mDate, mName);
                        alert.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int arg1) {
                                Log.d(CLASS_NAME, "alertDialog positiveButton onClick");

                                MeasuresDbHelper.updateMeasurement(mDatabase, mMeasurementId, mDate, mValue);
                                mContext.getContentResolver().notifyChange(Uri.parse("content://measurements"), null);
                                mDialog.dismiss();
                            }
                        });
                        alert.show();
                    } else {
                        MeasuresDbHelper.insertMeasurement(mDatabase, mDate, mParameterId, mValue);
                        mContext.getContentResolver().notifyChange(Uri.parse("content://measurements"), null);
                        mDialog.dismiss();
                    }
                    cursor.close();
                }
            });
        }
    }

    private void editMeasurementInit() {
        Log.d(CLASS_NAME, "editMeasurementInit");

        mCursor = mDatabase.rawQuery("SELECT" +
                        " bp."+ BP.NAME +", bm."+ BM.DATE +", bp."+ BP.IMAGE +"," +
                        " bp."+ BP.INSTRUCTION +", bm."+ BM.VALUE +", bm."+ BM.BODY_PARAMETER_ID +
                        " FROM "+ BM.TABLE_NAME +" AS bm LEFT JOIN "+ BP.TABLE_NAME +" AS bp" +
                        " ON bm."+ BM.BODY_PARAMETER_ID +" = bp."+ BP._ID +
                        " WHERE bm."+ BM._ID +" = ?",
                new String[]{ String.valueOf(mMeasurementId) });

        if (mCursor.moveToFirst()) {
            int nameColumnIndex        = mCursor.getColumnIndex(BP.NAME);
            int dateColumnIndex        = mCursor.getColumnIndex(BM.DATE);
            int imageColumnIndex       = mCursor.getColumnIndex(BP.IMAGE);
            int instructionColumnIndex = mCursor.getColumnIndex(BP.INSTRUCTION);
            int valueColumnIndex       = mCursor.getColumnIndex(BM.VALUE);
            int parameterIdColumnIndex = mCursor.getColumnIndex(BM.BODY_PARAMETER_ID);
            mName        = mCursor.getString(nameColumnIndex);
            mDate        = mCursor.getString(dateColumnIndex);
            mImage       = mCursor.getInt(imageColumnIndex);
            mInstruction = mCursor.getString(instructionColumnIndex);
            mValue       = mCursor.getDouble(valueColumnIndex);
            mParameterId = mCursor.getInt(parameterIdColumnIndex);

            int year  = Integer.parseInt(mDate.substring(0, 4));
            int month = Integer.parseInt(mDate.substring(5, 7));
            int day   = Integer.parseInt(mDate.substring(8, 10));
            setDatePickerDialog(year, month, day);

            mDateTextView.setText(mDate);

            mDialog.setTitle(mName);
            mImageView.setImageResource(mImage);
            mInstructionTextView.setText(mInstruction);
            mValueEditText.setText(String.valueOf(mValue));

            mDialog.getButton(Dialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(CLASS_NAME, "positiveButton onClick");

                    if (!validateValue()) return;

                    Cursor cursor = mDatabase.rawQuery("SELECT "+ BM._ID +
                                    " FROM "+ BM.TABLE_NAME +
                                    " WHERE "+ BM.DATE +" = ?"+
                                    " AND "+ BM.BODY_PARAMETER_ID +" = ?"+
                                    " AND "+ BM._ID +" <> ?",
                            new String[]{ mDate, String.valueOf(mParameterId), String.valueOf(mMeasurementId) });
                    if (cursor.moveToFirst()) {
                        final int id = cursor.getInt(0);

                        AlertDialog.Builder alert = getParamAlreadyExistAlert(mDate, mName);
                        alert.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int arg1) {
                                Log.d(CLASS_NAME, "alertDialog positiveButton onClick");

                                MeasuresDbHelper.updateMeasurement(mDatabase, id, mDate, mValue);
                                MeasuresDbHelper.deleteMeasurement(mDatabase, mMeasurementId);
                                mContext.getContentResolver().notifyChange(Uri.parse("content://measurements"), null);
                                mDialog.dismiss();
                            }
                        });
                        alert.show();
                    } else {
                        MeasuresDbHelper.updateMeasurement(mDatabase, mMeasurementId, mDate, mValue);
                        mContext.getContentResolver().notifyChange(Uri.parse("content://measurements"), null);
                        mDialog.dismiss();
                    }
                    cursor.close();
                }
            });

        }
    }

    private void setDatePickerDialog(final int year, final int month, final int day) {
        mDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(CLASS_NAME, "mDateTextView onClick");

                DatePickerDialog dialog = new DatePickerDialog(mContext, R.style.DialogTheme, new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        Log.d(CLASS_NAME, "DatePickerDialog onDateSet");

                        Calendar calendar = Calendar.getInstance();
                        calendar.set(year, monthOfYear, dayOfMonth);
                        mDate = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
                        mDateTextView.setText(mDate);
                    }
                }, year, month - 1, day); // monthOfYear = month - 1

                DatePicker datePicker = dialog.getDatePicker();
                datePicker.setMinDate(0);
                datePicker.setMaxDate(new Date().getTime() + 60*60*1000);
                dialog.show();
            }
        });
    }

    private boolean validateValue() {
        try {
            mValue = Double.parseDouble(String.valueOf(mValueEditText.getText()));
            if (mValue == 0) {
                Toast.makeText(mContext, "Параметр не может быть равен 0", Toast.LENGTH_LONG).show();
                return false;
            }
        } catch(Exception e){
            Toast.makeText(mContext, "Неверно введено значение", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
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
