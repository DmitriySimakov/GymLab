package com.dmitry_simakov.gymlab.measurements;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dmitry_simakov.gymlab.R;
import com.dmitry_simakov.gymlab.database.DatabaseContract;
import com.dmitry_simakov.gymlab.database.DatabaseHelper;

import java.util.ArrayList;

public class ProportionsCalculatorFragment extends Fragment implements View.OnClickListener {

    public static final String CLASS_NAME = ProportionsCalculatorFragment.class.getSimpleName();

    private static final class BM extends DatabaseContract.BodyMeasurementsEntry {}
    private static final class BP extends DatabaseContract.BodyParametersEntry {}

    private MyCursorAdapter mCursorAdapter;


    public ProportionsCalculatorFragment() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(CLASS_NAME, "onAttach");

        SQLiteDatabase db = DatabaseHelper.getInstance(context).getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT "+ BP._ID +", "+ BP.NAME +","+ BP.COEFFICIENT +","+
                        " (SELECT "+ BM.VALUE +" FROM "+ BM.TABLE_NAME +
                        " WHERE "+ BM.BODY_PARAMETER_ID +" = bp."+ BP._ID +
                        " ORDER BY "+ BM.DATE +" DESC LIMIT 0, 1) AS "+ BM.VALUE +
                        " FROM "+ BP.TABLE_NAME +" AS bp"+
                        " WHERE "+ BM._ID +" > '2'"+
                        " ORDER BY "+ BP._ID,
                null
        );
        mCursorAdapter = new MyCursorAdapter(getContext(), cursor);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_proportions_calculator, container, false);

        ListView listView = view.findViewById(R.id.list_view);
        ProgressBar progressBar = view.findViewById(R.id.progressBar);
        listView.setEmptyView(progressBar);
        listView.setAdapter(mCursorAdapter);

        // TODO It's for debug. DELETE LATER
        listView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus){
                    Log.d(CLASS_NAME, "list lost focus");
                } else {
                    Log.d(CLASS_NAME, "list takes focus");
                }
            }
        });

        Button resetBtn = view.findViewById(R.id.reset_btn);
        resetBtn.setOnClickListener(this);
        Button fillBtn = view.findViewById(R.id.fill_btn);
        fillBtn.setOnClickListener(this);
        Button countBtn = view.findViewById(R.id.count_btn);
        countBtn.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        Log.d(CLASS_NAME, "onClick");
        switch (v.getId()) {
            case R.id.reset_btn:
                mCursorAdapter.reset();
                break;
            case R.id.fill_btn:
                mCursorAdapter.fill();
                break;
            case R.id.count_btn:
                mCursorAdapter.count();
                break;
        }
    }

    private class MyCursorAdapter extends CursorAdapter {

        public final String CLASS_NAME = ProportionsCalculatorFragment.CLASS_NAME +"."+ MyCursorAdapter.class.getSimpleName();

        private ArrayList<ListItem> mItems = new ArrayList<>();
        private LayoutInflater mInflater;

        public MyCursorAdapter(Context context, Cursor c) {
            super(context, c, 0);
            mInflater = LayoutInflater.from(getContext());

            c.moveToFirst();
            do {
                ListItem listItem = new ListItem();
                listItem.parameter = c.getString(c.getColumnIndex(BP.NAME));
                listItem.coefficient = c.getDouble(c.getColumnIndex(BP.COEFFICIENT));
                listItem.actualValue = c.getDouble(c.getColumnIndex(BM.VALUE));
                mItems.add(listItem);
            } while (c.moveToNext());
            notifyDataSetChanged();
        }

        @Override
        public ListItem getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return mInflater.inflate(R.layout.proportions_calculator_list_item, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            final ViewHolder holder = new ViewHolder();
            holder.parameter = view.findViewById(R.id.parameter);
            holder.actualValue = view.findViewById(R.id.actual_value);
            holder.expectedValue = view.findViewById(R.id.expected_value);
            holder.percent = view.findViewById(R.id.percent);

            // TODO It's for debug. DELETE LATER
            view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus){
                        Log.d(CLASS_NAME, "view lost focus");
                    } else {
                        Log.d(CLASS_NAME, "view takes focus");
                    }
                }
            });

            final ListItem item = getItem(cursor.getPosition());
            holder.parameter.setText(item.parameter);

            if (item.actualValue == 0) {
                holder.actualValue.setText("");
            } else {
                holder.actualValue.setText(String.valueOf(item.actualValue));
            }

            if (item.expectedValue == 0) {
                holder.expectedValue.setText("");
            } else {
                holder.expectedValue.setText(String.valueOf(item.expectedValue));
            }

            if (item.percent == 0) {
                holder.percent.setText("");
            } else {
                holder.percent.setText(item.percent + "%");
                double difference = Math.abs(100 - item.percent);
                String color;
                if (difference > 13) {
                    color = "#f44336"; // red
                } else if (difference > 11) {
                    color = "#FF5722"; // deep orange
                } else if (difference > 9) {
                    color = "#FF9800"; // orange
                } else if (difference > 7) {
                    color = "#FFC107"; // amber
                } else if (difference > 5) {
                    color = "#FFEB3B"; // yellow
                } else if (difference > 3) {
                    color = "#CDDC39"; // lime
                } else if (difference > 1) {
                    color = "#8BC34A"; // light greeen
                } else {
                    color = "#4CAF50"; // green
                }
                holder.percent.setTextColor(Color.parseColor(color));
            }

            holder.actualValue.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus){
                        try {
                            item.actualValue = Double.parseDouble(String.valueOf(holder.actualValue.getText()));
                        } catch(Exception e){
                            item.actualValue = 0;
                        }
                        Log.d(CLASS_NAME, item.parameter +" lost focus");
                    } else {
                        Log.d(CLASS_NAME, item.parameter +" takes focus");
                    }
                }
            });
        }

        @Override
        public Cursor swapCursor(Cursor newCursor) {
            fill();
            return super.swapCursor(newCursor);
        }

        void reset() {
            Log.d(CLASS_NAME, "reset");

            for (ListItem i : mItems) {
                i.actualValue = 0;
                i.expectedValue = 0;
                i.percent = 0;
            }
            notifyDataSetChanged();
        }

        void fill() {
            Log.d(CLASS_NAME, "fill");

            Cursor c = getCursor();
            c.moveToFirst();
            for (ListItem i : mItems) {
                i.actualValue = c.getDouble(c.getColumnIndex(BM.VALUE));
                i.expectedValue = 0;
                i.percent = 0;
                c.moveToNext();
            }
            notifyDataSetChanged();
        }

        void count() {
            Log.d(CLASS_NAME, "count");

            double chestValue = mItems.get(3).actualValue;
            if (chestValue == 0) {
                Toast.makeText(getContext(), "Введите параметр Грудь", Toast.LENGTH_LONG).show();
                return;
            }
            double maxRatio = 0;
            ListItem maxRatioItem = null;
            for (ListItem i : mItems) {
                if (!i.parameter.equals("Талия")){
                    double ratio = i.actualValue / (chestValue * i.coefficient);
                    if (ratio > maxRatio) {
                        maxRatio = ratio;
                        maxRatioItem = i;
                    }
                }
            }
            for (ListItem i : mItems) {
                double expectedValue = i.coefficient * (maxRatioItem.actualValue / maxRatioItem.coefficient);
                i.expectedValue = (double)Math.round(expectedValue * 10d) / 10d;;
                double percent = i.actualValue / i.expectedValue * 100;
                i.percent = (double)Math.round(percent * 10d) / 10d;
            }
            notifyDataSetChanged();
        }

        private class ViewHolder {
            TextView parameter;
            EditText actualValue;
            TextView expectedValue;
            TextView percent;
        }
    }

    private class ListItem {
        String parameter;
        double coefficient;
        double actualValue;
        double expectedValue;
        double percent;
    }
}
