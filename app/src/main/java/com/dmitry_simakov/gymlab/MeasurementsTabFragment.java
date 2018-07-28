package com.dmitry_simakov.gymlab;

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

public class MeasurementsTabFragment extends Fragment {

    public static final String CLASS_NAME = MeasurementsTabFragment.class.getSimpleName();

    TabLayout mTabLayout;
    ViewPager mViewPager;

    public MeasurementsTabFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_measurements_tab, container, false);

        mViewPager = view.findViewById(R.id.view_pager);
        mTabLayout = view.findViewById(R.id.tabs);

        mViewPager.setAdapter(new MeasurementsPagerAdapter(getChildFragmentManager()));
        mTabLayout.post(new Runnable() {
            @Override
            public void run() {
                mTabLayout.setupWithViewPager(mViewPager);
            }
        });

        return view;
    }

    public int getContainer() {
        return R.id.fragment_container;
    }

    public class MeasurementsPagerAdapter extends FragmentPagerAdapter {

        private static final int ADD_MEASUREMENT_POS = 0;
        private static final int HISTORY_POS = 1;

        MeasurementsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case ADD_MEASUREMENT_POS:
                    return new MeasurementsListFragment();
                case HISTORY_POS:
                    return new MeasurementsHistoryListFragment();
            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case ADD_MEASUREMENT_POS:
                    return getString(R.string.add_measurement);
                case HISTORY_POS:
                    return getString(R.string.measurement_history);
            }
            return null;
        }
    }
}
