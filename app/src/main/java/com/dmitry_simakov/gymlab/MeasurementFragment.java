package com.dmitry_simakov.gymlab;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MeasurementFragment extends Fragment {

    public static final String CLASS_NAME = MeasurementFragment.class.getSimpleName();

    public MeasurementFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_measurement, container, false);
    }

}
