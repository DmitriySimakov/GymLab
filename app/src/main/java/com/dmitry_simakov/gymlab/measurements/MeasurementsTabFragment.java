package com.dmitry_simakov.gymlab.measurements;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dmitry_simakov.gymlab.R;

public class MeasurementsTabFragment extends Fragment {

    public static final String CLASS_NAME = MeasurementsTabFragment.class.getSimpleName();

    private static final int CALCULATOR_POS = 0;
    private static final int ADD_MEASUREMENT_POS = 1;
    private static final int HISTORY_POS = 2;

    TabLayout mTabLayout;
    ViewPager mViewPager;

    public MeasurementsTabFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_measurements_tab, container, false);

        mViewPager = view.findViewById(R.id.view_pager);
        mTabLayout = view.findViewById(R.id.tabs);

        mViewPager.setAdapter(new MyPagerAdapter(getChildFragmentManager()));
        mTabLayout.post(new Runnable() {
            @Override
            public void run() {
                mTabLayout.setupWithViewPager(mViewPager);
            }
        });

        mViewPager.setCurrentItem(ADD_MEASUREMENT_POS);

        return view;
    }

    public int getContainer() {
        return R.id.fragment_container;
    }

    private class MyPagerAdapter extends FragmentPagerAdapter {

        MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case CALCULATOR_POS:
                    return new ProportionsCalculatorFragment();
                case ADD_MEASUREMENT_POS:
                    return new MeasurementsListFragment();
                case HISTORY_POS:
                    return new MeasurementsHistoryListFragment();
            }
            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case CALCULATOR_POS:
                    return getString(R.string.calculator);
                case ADD_MEASUREMENT_POS:
                    return getString(R.string.add_measurement);
                case HISTORY_POS:
                    return getString(R.string.measurement_history);
            }
            return null;
        }
    }
}
