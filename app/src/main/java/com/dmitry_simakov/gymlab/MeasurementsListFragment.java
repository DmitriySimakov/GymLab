package com.dmitry_simakov.gymlab;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.dmitry_simakov.gymlab.database.DbContract;
import com.dmitry_simakov.gymlab.database.MeasuresDbHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MeasurementsListFragment extends ListFragment
        implements MeasurementDialog.onDatabaseChangeListener {

    public static final String CLASS_NAME = MeasurementsListFragment.class.getSimpleName();

    private static class BM extends DbContract.BodyMeasurementsEntry {}
    private static class BP extends DbContract.BodyParametersEntry {}

    private Context mContext;

    private String mDate;

    private MeasuresDbHelper mDbHelper;
    private SQLiteDatabase mDatabase;
    private Cursor mCursor;
    private SimpleCursorAdapter mAdapter;

    public MeasurementsListFragment() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(CLASS_NAME, "onAttach");
        mContext = context;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(CLASS_NAME, "onActivityCreated");

        mDbHelper = new MeasuresDbHelper(mContext);
        mDatabase = mDbHelper.getWritableDatabase();

        Bundle args = getArguments();
        if (args != null) {
            mDate = getArguments().getString(BM.DATE);
        }

        changeCursor();

        String[] groupFrom = { BP.NAME, BM.VALUE, "prevVal", BM.DATE, "prevDate" };
        int[] groupTo = { R.id.measure_parameter, R.id.measure_value,
                R.id.measure_difference, R.id.measure_difference, R.id.measure_difference, };
        mAdapter = new MeasurementsCursorAdapter(mContext,
                R.layout.measurement_list_item, mCursor, groupFrom, groupTo, 0);
        setListAdapter(mAdapter);
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

    @Override
    public void onDestroy() {
        Log.d(CLASS_NAME, "onDestroy");
        super.onDestroy();

        if (mCursor != null) mCursor.close();
        if (mDatabase != null) mDatabase.close();
    }

    @Override
    public void onDatabaseChange() {
        Log.d(CLASS_NAME, "onDatabaseChange");
        changeCursor();
        mAdapter.swapCursor(mCursor);
    }

    private void changeCursor() {
        Log.d(CLASS_NAME, "changeCursor");
        if (mDate == null) {
            mCursor = mDatabase.rawQuery("SELECT "+ BP._ID +", "+ BP.NAME +"," +
                            " (SELECT "+ BM.VALUE +" FROM "+ BM.TABLE_NAME +
                            " WHERE "+ BM.BODY_PARAMETER_ID +" = bp."+ BP._ID +
                            " ORDER BY "+ BM.DATE +" DESC LIMIT 0, 1) AS "+ BM.VALUE +"," +
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
            mCursor = mDatabase.rawQuery("SELECT bm."+ BM._ID +", bp."+ BP.NAME +", bm."+ BM.VALUE +","+
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
    }


    private class MeasurementsCursorAdapter extends SimpleCursorAdapter {

        public final String CLASS_NAME = MeasurementsListFragment.class.getSimpleName();

        private Context context;
        private int layout;
        private Cursor cursor;
        private final LayoutInflater inflater;

        MeasurementsCursorAdapter(Context context, int layout, Cursor cursor, String[] from, int[] to, int flags) {
            super(context, layout, cursor, from, to, flags);
            this.layout = layout;
            this.context = context;
            this.inflater = LayoutInflater.from(context);
            this.cursor = cursor;
        }

        @Override
        public View newView (Context context, Cursor cursor, ViewGroup parent) {
            return inflater.inflate(layout, null);
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
