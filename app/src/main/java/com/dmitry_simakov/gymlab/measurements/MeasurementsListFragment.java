package com.dmitry_simakov.gymlab.measurements;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.dmitry_simakov.gymlab.R;
import com.dmitry_simakov.gymlab.database.DbContract;
import com.dmitry_simakov.gymlab.database.MeasuresDbHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MeasurementsListFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String CLASS_NAME = MeasurementsListFragment.class.getSimpleName();

    private static final class BM extends DbContract.BodyMeasurementsEntry {}
    private static final class BP extends DbContract.BodyParametersEntry {}

    private MeasuresDbHelper mDbHelper;
    private SQLiteDatabase mDatabase;
    private SimpleCursorAdapter mCursorAdapter;

    private String mDate;

    public MeasurementsListFragment() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(CLASS_NAME, "onAttach");

        mDbHelper = new MeasuresDbHelper(context);
        mDatabase = mDbHelper.getWritableDatabase();

        Bundle args = getArguments();
        if (args != null) {
            mDate = args.getString(BM.DATE);
        }

        mCursorAdapter = new MyCursorAdapter(context);
        setListAdapter(mCursorAdapter);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(CLASS_NAME, "onActivityCreated");

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Log.d(CLASS_NAME, "onListItemClick");

        MeasurementDialog dialog = new MeasurementDialog();
        Bundle args = new Bundle();
        if (mDate == null) {
            // Put position+1 cuz SQL counts from 1 while the position in list is counted from 0
            args.putInt(MeasurementDialog.PARAMETER_ID, position+1);
        } else {
            args.putInt(MeasurementDialog.MEASUREMENT_ID, (int)id);
        }
        dialog.setArguments(args);
        dialog.show(getChildFragmentManager(), "MEASUREMENT_DIALOG");
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        return new MyCursorLoader(getContext(), mDatabase, mDate);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        mCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

    private static class MyCursorLoader extends CursorLoader {

        private final ForceLoadContentObserver mObserver = new ForceLoadContentObserver();

        private SQLiteDatabase mDatabase;
        private String mDate;

        MyCursorLoader(Context context, SQLiteDatabase db, String date) {
            super(context);
            mDatabase = db;
            mDate = date;
            setUri(BM.CONTENT_URI);
        }

        @Override
        public Cursor loadInBackground() {
            Cursor cursor;
            if (mDate == null) {
                cursor = mDatabase.rawQuery("SELECT "+ BP._ID +", "+ BP.NAME +","+
                                " (SELECT "+ BM.VALUE +" FROM "+ BM.TABLE_NAME +
                                " WHERE "+ BM.BODY_PARAMETER_ID +" = bp."+ BP._ID +
                                " ORDER BY "+ BM.DATE +" DESC LIMIT 0, 1) AS "+ BM.VALUE +","+
                                " (SELECT "+ BM.VALUE +" FROM "+ BM.TABLE_NAME +
                                " WHERE "+ BM.BODY_PARAMETER_ID +" = bp."+ BP._ID +
                                " ORDER BY "+ BM.DATE +" DESC LIMIT 1, 1) AS prevVal,"+
                                " (SELECT "+ BM.DATE +" FROM "+ BM.TABLE_NAME +
                                " WHERE "+ BM.BODY_PARAMETER_ID +" = bp."+ BP._ID +
                                " ORDER BY "+ BM.DATE +" DESC LIMIT 0, 1) AS "+ BM.DATE +", "+
                                " (SELECT "+ BM.DATE +" FROM "+ BM.TABLE_NAME +
                                " WHERE "+ BM.BODY_PARAMETER_ID +" = bp."+ BP._ID +
                                " ORDER BY "+ BM.DATE +" DESC LIMIT 1, 1) AS prevDate"+
                                " FROM "+ BP.TABLE_NAME +" AS bp"+
                                " ORDER BY "+ BP._ID,
                        null
                );
            } else {
                cursor = mDatabase.rawQuery("SELECT bm."+ BM._ID +", bp."+ BP.NAME +", bm."+ BM.VALUE +","+
                                " (SELECT "+ BM.VALUE +" FROM "+ BM.TABLE_NAME +
                                " WHERE "+ BM.BODY_PARAMETER_ID +" = bp."+ BP._ID +
                                " AND julianday("+ BM.DATE +") < julianday(?)"+
                                " ORDER BY "+ BM.DATE +" DESC LIMIT 0, 1) AS prevVal, "+
                                BM.DATE +"," +
                                " (SELECT "+ BM.DATE +" FROM "+ BM.TABLE_NAME +
                                " WHERE "+ BM.BODY_PARAMETER_ID +" = bp."+ BP._ID +
                                " AND julianday("+ BM.DATE +") < julianday(?)"+
                                " ORDER BY "+ BM.DATE +" DESC LIMIT 0, 1) AS prevDate"+
                                " FROM "+ BM.TABLE_NAME +" AS bm LEFT JOIN "+ BP.TABLE_NAME +" AS bp" +
                                " ON bm."+ BM.BODY_PARAMETER_ID +" = bp."+ BP._ID +
                                " WHERE bm."+ BM.DATE +" = ?"+
                                " ORDER BY bp."+ BP._ID,
                        new String[]{ mDate, mDate, mDate }
                );
            }
            if (cursor != null) {
                cursor.registerContentObserver(mObserver);
                cursor.setNotificationUri(getContext().getContentResolver(), getUri());
            }

            return cursor;
        }
    }

    private static class MyCursorAdapter extends SimpleCursorAdapter {

        private static final int LAYOUT = R.layout.measurement_list_item;
        private static final String[] FROM = { BP.NAME, BM.VALUE, "prevVal", BM.DATE, "prevDate" };
        private static final int[] TO = { R.id.measure_parameter, R.id.measure_value,
                R.id.measure_difference, R.id.measure_difference, R.id.measure_difference, };

        MyCursorAdapter(Context context) {
            super(context, LAYOUT, null, FROM, TO, 0);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            super.bindView(view, context, cursor);
            Log.d(CLASS_NAME, "bindView");

            TextView differenceTextView = view.findViewById(R.id.measure_difference);

            int prevValColumnIndex = cursor.getColumnIndexOrThrow("prevVal");
            int curValColumnIndex = cursor.getColumnIndexOrThrow(BM.VALUE);
            int prevDateColumnIndex = cursor.getColumnIndexOrThrow("prevDate");
            int curDateColumnIndex = cursor.getColumnIndexOrThrow(BM.DATE);

            double prevVal = cursor.getDouble(prevValColumnIndex);
            if (prevVal == 0) return;
            double curVal = cursor.getDouble(curValColumnIndex);

            double valDiff = curVal - prevVal;

            String prevDateISO = cursor.getString(prevDateColumnIndex);
            String curDateISO = cursor.getString(curDateColumnIndex);

            Calendar prevDate = Calendar.getInstance();
            Calendar curDate = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date date;
            int dateDiff = 0;
            try {
                date = sdf.parse(prevDateISO);
                prevDate.setTime(date);
                date = sdf.parse(curDateISO);
                curDate.setTime(date);
                dateDiff = (int)((curDate.getTimeInMillis() - prevDate.getTimeInMillis())/(1000*60*60*24));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            String valDiffStr = String.valueOf(curVal - prevVal);
            String dateDiffStr = String.valueOf(dateDiff);
            if (valDiff < 0) {
                differenceTextView.setTextColor(Color.parseColor("#e53935"));
            } else if (valDiff > 0) {
                valDiffStr = "+" + valDiffStr;
                differenceTextView.setTextColor(Color.parseColor("#43A047"));
            }
            differenceTextView.setText(valDiffStr +" за "+ dateDiffStr +" дн.");
        }
    }
}
