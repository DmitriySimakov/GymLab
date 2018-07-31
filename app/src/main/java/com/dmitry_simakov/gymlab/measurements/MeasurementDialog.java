package com.dmitry_simakov.gymlab.measurements;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dmitry_simakov.gymlab.R;
import com.dmitry_simakov.gymlab.database.DatabaseContract;
import com.dmitry_simakov.gymlab.database.DatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MeasurementDialog extends AppCompatDialogFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String CLASS_NAME = MeasurementDialog.class.getSimpleName();

    private static final class BM extends DatabaseContract.BodyMeasurementsEntry {}
    private static final class BP extends DatabaseContract.BodyParametersEntry {}

    public static final String PARAMETER_ID = "parameter_id";
    public static final String MEASUREMENT_ID = "measurement_id";

    public static final int NEW_MEASUREMENT_LOADER_ID = 1;
    public static final int NEW_MEASUREMENT_CHECK_LOADER_ID = 11;
    public static final int EDIT_MEASUREMENT_LOADER_ID = 2;
    public static final int EDIT_MEASUREMENT_CHECK_LOADER_ID = 22;

    private TextView mDateTextView;
    private ImageView mImageView;
    private TextView mInstructionTextView;
    private EditText mValueEditText;

    private String mName, mDate, mImageName, mInstruction;
    private int mParameterId, mMeasurementId;
    private double mValue;

    private SQLiteDatabase mDatabase;

    private AlertDialog mDialog;

    public MeasurementDialog() {}

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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(CLASS_NAME, "onActivityCreated");

        mDialog = (AlertDialog)getDialog();

        Bundle args = getArguments();
        if (args != null) {
            if (args.containsKey(PARAMETER_ID)) {
                mParameterId = args.getInt(PARAMETER_ID);
                getLoaderManager().initLoader(NEW_MEASUREMENT_LOADER_ID, null, this);
            } else {
                mMeasurementId = args.getInt(MEASUREMENT_ID);
                getLoaderManager().initLoader(EDIT_MEASUREMENT_LOADER_ID, null, this);
            }
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        CursorLoader loader = null;
        switch (id) {
            case NEW_MEASUREMENT_LOADER_ID:
                loader = new InitCursorLoader(getContext(), mDatabase, mParameterId);
                break;
            case EDIT_MEASUREMENT_LOADER_ID:
                loader = new InitCursorLoader(getContext(), mDatabase, mMeasurementId);
                break;
            case NEW_MEASUREMENT_CHECK_LOADER_ID:
            case EDIT_MEASUREMENT_CHECK_LOADER_ID:
                loader = new CheckCursorLoader(getContext(), mDatabase,
                        String.valueOf(mMeasurementId), String.valueOf(mParameterId), mDate);
                break;
        }
        return loader;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case NEW_MEASUREMENT_LOADER_ID:
                newMeasurementInit(cursor);
                break;
            case EDIT_MEASUREMENT_LOADER_ID:
                editMeasurementInit(cursor);
                break;
            case NEW_MEASUREMENT_CHECK_LOADER_ID:
                newMeasurementCheck(cursor);
                break;
            case EDIT_MEASUREMENT_CHECK_LOADER_ID:
                editMeasurementCheck(cursor);
                break;
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {}

    private static class InitCursorLoader extends CursorLoader {

        private SQLiteDatabase mDatabase;
        private int mId;

        InitCursorLoader(Context context, SQLiteDatabase db, int id) {
            super(context);
            mDatabase = db;
            mId = id;
        }

        @Override
        public Cursor loadInBackground() {
            Cursor cursor = null;
            switch (getId()) {
                case NEW_MEASUREMENT_LOADER_ID:
                    cursor = mDatabase.rawQuery("SELECT "+ BP.NAME +", "+ BP.IMAGE +", "+ BP.INSTRUCTION +
                                    " FROM "+ BP.TABLE_NAME +
                                    " WHERE "+ BP._ID +" = ?",
                            new String[]{ String.valueOf(mId) });
                    break;
                case EDIT_MEASUREMENT_LOADER_ID:
                    cursor = mDatabase.rawQuery("SELECT" +
                                    " bp."+ BP.NAME +", bm."+ BM.DATE +", bp."+ BP.IMAGE +"," +
                                    " bp."+ BP.INSTRUCTION +", bm."+ BM.VALUE +", bm."+ BM.BODY_PARAMETER_ID +
                                    " FROM "+ BM.TABLE_NAME +" AS bm LEFT JOIN "+ BP.TABLE_NAME +" AS bp" +
                                    " ON bm."+ BM.BODY_PARAMETER_ID +" = bp."+ BP._ID +
                                    " WHERE bm."+ BM._ID +" = ?",
                            new String[]{ String.valueOf(mId) });
                    break;
            }
            return cursor;
        }
    }

    private static class CheckCursorLoader extends CursorLoader {

        private SQLiteDatabase mDatabase;
        private String mMeasurementId;
        private String mParameterId;
        private String mDate;

        CheckCursorLoader(Context context, SQLiteDatabase db, String measurementId, String parameterId, String date) {
            super(context);
            mDatabase = db;
            mMeasurementId = measurementId;
            mParameterId = parameterId;
            mDate = date;
        }

        @Override
        public Cursor loadInBackground() {
            Cursor cursor = null;
            switch (getId()) {
                case NEW_MEASUREMENT_CHECK_LOADER_ID:
                    cursor = mDatabase.rawQuery("SELECT "+ BM._ID +" FROM "+ BM.TABLE_NAME +
                                    " WHERE "+ BM.DATE +" = ? AND "+ BM.BODY_PARAMETER_ID +" = ?",
                            new String[]{ mDate, String.valueOf(mParameterId) });
                    break;
                case EDIT_MEASUREMENT_CHECK_LOADER_ID:
                    cursor = mDatabase.rawQuery("SELECT "+ BM._ID +
                                    " FROM "+ BM.TABLE_NAME +
                                    " WHERE "+ BM.DATE +" = ?"+
                                    " AND "+ BM.BODY_PARAMETER_ID +" = ?"+
                                    " AND "+ BM._ID +" <> ?",
                            new String[]{ mDate, String.valueOf(mParameterId), String.valueOf(mMeasurementId) });
                    break;
            }
            return cursor;
        }
    }

    private void newMeasurementInit(Cursor c) {
        Log.d(CLASS_NAME, "newMeasurementInit");

        if (c.moveToFirst()) {
            mName        = c.getString(c.getColumnIndex(BP.NAME));
            mImageName = c.getString(c.getColumnIndex(BP.IMAGE));
            mInstruction = c.getString(c.getColumnIndex(BP.INSTRUCTION));

            Calendar calendar = Calendar.getInstance();
            int year  = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1; //  month = monthOfYear + 1
            int day   = calendar.get(Calendar.DAY_OF_MONTH);
            setDatePickerDialog(year, month, day);

            mDate = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
            mDateTextView.setText(mDate);

            mDialog.setTitle(mName);
            if (mImageName != null) {
                Resources res = getContext().getResources();
                int resID = res.getIdentifier(mImageName, "drawable", getContext().getPackageName());
                if (resID != 0) {
                    mImageView.setImageDrawable(res.getDrawable(resID));
                }
            }
            mInstructionTextView.setText(mInstruction);

            final LoaderManager.LoaderCallbacks<Cursor> callback = this;
            mDialog.getButton(Dialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(CLASS_NAME, "positiveButton onClick");

                    if (!validateValue()) return;
                    getLoaderManager().initLoader(NEW_MEASUREMENT_CHECK_LOADER_ID, null, callback);
                }
            });
        }
    }

    private void editMeasurementInit(Cursor c) {
        Log.d(CLASS_NAME, "editMeasurementInit");

        if (c.moveToFirst()) {
            mName        = c.getString(c.getColumnIndex(BP.NAME));
            mDate        = c.getString(c.getColumnIndex(BM.DATE));
            mImageName   = c.getString(c.getColumnIndex(BP.IMAGE));
            mInstruction = c.getString(c.getColumnIndex(BP.INSTRUCTION));
            mValue       = c.getDouble(c.getColumnIndex(BM.VALUE));
            mParameterId = c.getInt(c.getColumnIndex(BM.BODY_PARAMETER_ID));

            int year  = Integer.parseInt(mDate.substring(0, 4));
            int month = Integer.parseInt(mDate.substring(5, 7));
            int day   = Integer.parseInt(mDate.substring(8, 10));
            setDatePickerDialog(year, month, day);

            mDateTextView.setText(mDate);

            mDialog.setTitle(mName);
            if (mImageName != null) {
                Resources res = getContext().getResources();
                int resID = res.getIdentifier(mImageName, "drawable", getContext().getPackageName());
                if (resID != 0) {
                    mImageView.setImageDrawable(res.getDrawable(resID));
                }
            }
            mInstructionTextView.setText(mInstruction);
            mValueEditText.setText(String.valueOf(mValue));

            final LoaderManager.LoaderCallbacks<Cursor> callback = this;
            mDialog.getButton(Dialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(CLASS_NAME, "positiveButton onClick");

                    if (!validateValue()) return;
                    getLoaderManager().initLoader(EDIT_MEASUREMENT_CHECK_LOADER_ID, null, callback);
                }
            });
        }
    }

    private void newMeasurementCheck(Cursor c) {
        if (c.moveToFirst()) {
            mMeasurementId = c.getInt(0);

            AlertDialog.Builder alert = getParamAlreadyExistAlert(mDate, mName);
            alert.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface d, int arg1) {
                    Log.d(CLASS_NAME, "alertDialog positiveButton onClick");

                    DatabaseHelper.updateMeasurement(mMeasurementId, mDate, mValue);
                    getContext().getContentResolver().notifyChange(BM.CONTENT_URI, null);
                    mDialog.dismiss();
                }
            });
            alert.show();
        } else {
            DatabaseHelper.insertMeasurement(mDate, mParameterId, mValue);
            getContext().getContentResolver().notifyChange(BM.CONTENT_URI, null);
            mDialog.dismiss();
        }
    }

    private void editMeasurementCheck(Cursor c) {
        if (c.moveToFirst()) {
            final int id = c.getInt(0);

            AlertDialog.Builder alert = getParamAlreadyExistAlert(mDate, mName);
            alert.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface d, int arg1) {
                    Log.d(CLASS_NAME, "alertDialog positiveButton onClick");

                    DatabaseHelper.updateMeasurement(id, mDate, mValue);
                    DatabaseHelper.deleteMeasurement(mMeasurementId);
                    getContext().getContentResolver().notifyChange(BM.CONTENT_URI, null);
                    mDialog.dismiss();
                }
            });
            alert.show();
        } else {
            DatabaseHelper.updateMeasurement(mMeasurementId, mDate, mValue);
            getContext().getContentResolver().notifyChange(BM.CONTENT_URI, null);
            mDialog.dismiss();
        }
    }

    private void setDatePickerDialog(final int year, final int month, final int day) {
        mDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(CLASS_NAME, "mDateTextView onClick");

                DatePickerDialog dialog = new DatePickerDialog(getContext(), R.style.DialogTheme, new DatePickerDialog.OnDateSetListener() {
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
                Toast.makeText(getContext(), "Параметр не может быть равен 0", Toast.LENGTH_LONG).show();
                return false;
            }
        } catch(Exception e){
            Toast.makeText(getContext(), "Неверно введено значение", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private AlertDialog.Builder getParamAlreadyExistAlert(String date, String param) {
        Log.d(CLASS_NAME, "getParamAlreadyExistAlert");

        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
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