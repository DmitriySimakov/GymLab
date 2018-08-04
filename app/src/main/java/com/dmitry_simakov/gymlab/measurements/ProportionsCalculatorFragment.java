package com.dmitry_simakov.gymlab.measurements;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
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

public class ProportionsCalculatorFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {

    public static final String CLASS_NAME = ProportionsCalculatorFragment.class.getSimpleName();

    private static final class BM extends DatabaseContract.BodyMeasurementEntry {}
    private static final class BMP extends DatabaseContract.BodyMeasurementParamEntry {}

    private SQLiteDatabase mDatabase;
    private Cursor mCursor;
    private MyAdapter mAdapter;


    public ProportionsCalculatorFragment() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(CLASS_NAME, "onAttach");

        mDatabase = DatabaseHelper.getInstance(context).getWritableDatabase();
        mAdapter = new MyAdapter();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(CLASS_NAME, "onCreateView");

        View view = inflater.inflate(R.layout.fragment_proportions_calculator, container, false);

        ListView listView = view.findViewById(R.id.list_view);
        ProgressBar progressBar = view.findViewById(R.id.progressBar);
        listView.setEmptyView(progressBar);
        listView.setAdapter(mAdapter);

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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(CLASS_NAME, "onActivityCreated");

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onClick(View v) {
        Log.d(CLASS_NAME, "onClick");
        switch (v.getId()) {
            case R.id.reset_btn:
                mAdapter.reset();
                break;
            case R.id.fill_btn:
                mAdapter.fill(mCursor);
                break;
            case R.id.count_btn:
                mAdapter.count();
                break;
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        Log.d(CLASS_NAME, "onCreateLoader id: "+ id);
        return new MyCursorLoader(getContext(), mDatabase);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        Log.d(CLASS_NAME, "onLoadFinished id: "+ loader.getId());
        mCursor = cursor;
        mAdapter.fill(cursor);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {}

    private static class MyCursorLoader extends CursorLoader {

        public final String CLASS_NAME = ProportionsCalculatorFragment.CLASS_NAME +"."+ MyCursorLoader.class.getSimpleName();

        private final ForceLoadContentObserver mObserver = new ForceLoadContentObserver();

        private SQLiteDatabase mDatabase;


        MyCursorLoader(Context context, SQLiteDatabase db) {
            super(context);
            mDatabase = db;
            setUri(BM.CONTENT_URI);
        }

        @Override
        public Cursor loadInBackground() {
            Log.d(CLASS_NAME, "loadInBackground id: "+ getId());

            Cursor cursor = mDatabase.rawQuery("SELECT "+ BMP._ID +", "+ BMP.NAME +","+ BMP.COEFFICIENT +","+
                            " (SELECT "+ BM.VALUE +" FROM "+ BM.TABLE_NAME +
                            " WHERE "+ BM.BODY_PARAM_ID +" = bp."+ BMP._ID +
                            " ORDER BY "+ BM.DATE +" DESC LIMIT 0, 1) AS "+ BM.VALUE +
                            " FROM "+ BMP.TABLE_NAME +" AS bp"+
                            " WHERE "+ BM._ID +" > '2'"+
                            " ORDER BY "+ BMP._ID,
                    null);

            if (cursor != null) {
                cursor.registerContentObserver(mObserver);
                cursor.setNotificationUri(getContext().getContentResolver(), getUri());
            }
            return cursor;
        }
    }

    private class MyAdapter extends BaseAdapter {

        public final String CLASS_NAME = ProportionsCalculatorFragment.CLASS_NAME +"."+ MyAdapter.class.getSimpleName();

        private LayoutInflater mInflater;
        private ArrayList<ListItem> mItems = new ArrayList<>();

        MyAdapter() {
            mInflater = LayoutInflater.from(getContext());
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public ListItem getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.proportions_calculator_list_item, parent, false);
                holder = new ViewHolder();
                holder.parameter = convertView.findViewById(R.id.parameter);
                holder.actualValue = convertView.findViewById(R.id.actual_value);
                holder.expectedValue = convertView.findViewById(R.id.expected_value);
                holder.percent = convertView.findViewById(R.id.percent);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            // TODO It's for debug. DELETE LATER
            convertView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus){
                        Log.d(CLASS_NAME, "view lost focus");
                    } else {
                        Log.d(CLASS_NAME, "view takes focus");
                    }
                }
            });

            final ListItem item = getItem(position);
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

        void fill(Cursor c) {
            Log.d(CLASS_NAME, "fill");

            mItems.clear();
            c.moveToFirst();
            do {
                ListItem item = new ListItem();
                item.parameter = c.getString(c.getColumnIndex(BMP.NAME));
                item.coefficient = c.getDouble(c.getColumnIndex(BMP.COEFFICIENT));
                item.actualValue = c.getDouble(c.getColumnIndex(BM.VALUE));
                item.expectedValue = 0;
                item.percent = 0;
                mItems.add(item);
            } while (c.moveToNext());
            
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