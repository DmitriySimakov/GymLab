package com.dmitry_simakov.gymlab.measurements;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
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
    private SQLiteDatabase mDatabase;

    private ListView mListView;
    private MyAdapter mListAdapter;

    public ProportionsCalculatorFragment() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(CLASS_NAME, "onAttach");

        mDatabase = DatabaseHelper.getInstance(context).getWritableDatabase();

        Cursor cursor = mDatabase.rawQuery("SELECT "+ BP._ID +", "+ BP.NAME +","+ BP.COEFFICIENT +","+
                        " (SELECT "+ BM.VALUE +" FROM "+ BM.TABLE_NAME +
                        " WHERE "+ BM.BODY_PARAMETER_ID +" = bp."+ BP._ID +
                        " ORDER BY "+ BM.DATE +" DESC LIMIT 0, 1) AS "+ BM.VALUE +
                        " FROM "+ BP.TABLE_NAME +" AS bp"+
                        " WHERE "+ BM._ID +" > '2'"+
                        " ORDER BY "+ BP._ID,
                null
        );
        mListAdapter = new MyAdapter(cursor);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_proportions_calculator, container, false);

        mListView = view.findViewById(R.id.list_view);
        ProgressBar progressBar = view.findViewById(R.id.progressBar);
        mListView.setEmptyView(progressBar);

        mListView.setAdapter(mListAdapter);

        Button resetBtn = view.findViewById(R.id.reset_btn);
        resetBtn.setOnClickListener(this);
        Button fillBtn = view.findViewById(R.id.fill_btn);
        fillBtn.setOnClickListener(this);
        Button countBtn = view.findViewById(R.id.count_btn);
        countBtn.setOnClickListener(this);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //initLoader
    }

    @Override
    public void onClick(View v) {
        Log.d(CLASS_NAME, "onClick");
        switch (v.getId()) {
            case R.id.reset_btn:
                mListAdapter.reset();
                break;
            case R.id.fill_btn:
                mListAdapter.fill();
                break;
            case R.id.count_btn:
                mListAdapter.count();
                break;
        }
    }

    private class MyAdapter extends BaseAdapter {

        public final String CLASS_NAME = ProportionsCalculatorFragment.CLASS_NAME +"."+ MyAdapter.class.getSimpleName();

        private LayoutInflater mInflater;
        private Cursor mCursor;
        private ArrayList<ListItem> mItems = new ArrayList<>();

        MyAdapter(Cursor cursor) {
            mCursor = cursor;
            mInflater = LayoutInflater.from(getContext());
            mCursor.moveToFirst();
            do {
                ListItem listItem = new ListItem();
                listItem.parameter = mCursor.getString(mCursor.getColumnIndex(BP.NAME));
                listItem.coefficient = mCursor.getDouble(mCursor.getColumnIndex(BP.COEFFICIENT));
                listItem.actualValue = mCursor.getDouble(mCursor.getColumnIndex(BM.VALUE));
                mItems.add(listItem);
            } while (mCursor.moveToNext());
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public Object getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.proportions_calculator_list_item, parent, false);
                holder.parameter = convertView.findViewById(R.id.parameter);
                holder.actualValue = convertView.findViewById(R.id.actual_value);
                holder.expectedValue = convertView.findViewById(R.id.expected_value);
                holder.percent = convertView.findViewById(R.id.percent);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            //Fill EditText with the value you have in data source
            holder.parameter.setText(mItems.get(position).parameter);

            if (mItems.get(position).actualValue == 0) {
                holder.actualValue.setText("");
            } else {
                holder.actualValue.setText(String.valueOf(mItems.get(position).actualValue));
            }

            if (mItems.get(position).expectedValue == 0) {
                holder.expectedValue.setText("");
            } else {
                holder.expectedValue.setText(String.valueOf(mItems.get(position).expectedValue));
            }

            if (mItems.get(position).percent == 0) {
                holder.percent.setText("");
            } else {
                holder.percent.setText(mItems.get(position).percent + "%");
                double difference = 100 - mItems.get(position).percent;
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

            holder.actualValue.setId(position);

            //we need to update adapter once we finish with editing
            holder.actualValue.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus){
                        int position = v.getId();
                        EditText actualValueET = (EditText) v;
                        try {
                            mItems.get(position).actualValue = Double.parseDouble(String.valueOf(actualValueET.getText()));
                        } catch(Exception e){
                            Toast.makeText(getContext(), "Неверно введено значение", Toast.LENGTH_LONG).show();
                            mItems.get(position).actualValue = 0;
                        }
                    }
                }
            });
            return convertView;
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

            mCursor.moveToFirst();
            for (ListItem i : mItems) {
                i.actualValue = mCursor.getDouble(mCursor.getColumnIndex(BM.VALUE));
                i.expectedValue = 0;
                i.percent = 0;
                mCursor.moveToNext();
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

        private class ListItem {
            String parameter;
            double coefficient;
            double actualValue;
            double expectedValue;
            double percent;
        }
    }
}
