package com.dmitry_simakov.gymlab.measurements;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.dmitry_simakov.gymlab.R;
import com.dmitry_simakov.gymlab.database.DbContract;
import com.dmitry_simakov.gymlab.database.MeasuresDbHelper;

public class ProportionsCalculatorFragment extends Fragment {

    public static final String CLASS_NAME = ProportionsCalculatorFragment.class.getSimpleName();

    private static final class BM extends DbContract.BodyMeasurementsEntry {}
    private static final class BP extends DbContract.BodyParametersEntry {}

    private MeasuresDbHelper mDbHelper;
    private SQLiteDatabase mDatabase;
    private SimpleCursorAdapter mCursorAdapter;
    private ListView mListView;

    public ProportionsCalculatorFragment() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(CLASS_NAME, "onAttach");
        mDbHelper = new MeasuresDbHelper(context);
        mDatabase = mDbHelper.getWritableDatabase();

        Cursor cursor = mDatabase.rawQuery("SELECT "+ BP._ID +", "+ BP.NAME +","+
                        " (SELECT "+ BM.VALUE +" FROM "+ BM.TABLE_NAME +
                        " WHERE "+ BM.BODY_PARAMETER_ID +" = bp."+ BP._ID +
                        " ORDER BY "+ BM.DATE +" DESC LIMIT 0, 1) AS "+ BM.VALUE +
                        " FROM "+ BP.TABLE_NAME +" AS bp"+
                        " WHERE "+ BM._ID +" > '2'"+
                        " ORDER BY "+ BP._ID,
                null
        );

        String[] groupFrom = { BP.NAME, BM.VALUE };
        int[] groupTo = { R.id.parameter, R.id.actual_value };
        mCursorAdapter = new SimpleCursorAdapter(context,
                R.layout.proportions_calculator_list_item, cursor, groupFrom, groupTo, 0);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_proportions_calculator, container, false);

        mListView = view.findViewById(R.id.list_view);
        ProgressBar progressBar = view.findViewById(R.id.progressBar);
        mListView.setEmptyView(progressBar);
        mListView.setAdapter(mCursorAdapter);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //initLoader
    }

    /*private static class MyCursorAdapter extends SimpleCursorAdapter {

        private static final int LAYOUT = R.layout.proportions_calculator_list_item;
        private static final String[] FROM = { BP.NAME, BM.VALUE };
        private static final int[] TO = { R.id.measure_parameter, R.id.measure_value };

        MyCursorAdapter(Context context) {
            super(context, LAYOUT, null, FROM, TO, 0);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            super.bindView(view, context, cursor);
            Log.d(CLASS_NAME, "bindView");

            TextView differenceTextView = view.findViewById(R.id.measure_difference);

            int prevValColumnIndex = cursor.getColumnIndexOrThrow("prevVal");
            int curValColumnIndex = cursor.getColumnIndexOrThrow(MeasurementsListFragment.BM.VALUE);
            int prevDateColumnIndex = cursor.getColumnIndexOrThrow("prevDate");
            int curDateColumnIndex = cursor.getColumnIndexOrThrow(MeasurementsListFragment.BM.DATE);

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
    */
}
