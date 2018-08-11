package com.dmitry_simakov.gymlab.training_sessions;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dmitry_simakov.gymlab.R;
import com.dmitry_simakov.gymlab.database.DatabaseContract;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ActiveTrainingSessionFragment extends Fragment {

    public static final String CLASS_NAME = ActiveTrainingSessionFragment.class.getSimpleName();

    private static final class TS extends DatabaseContract.TrainingSessionEntry {}
    private static final class TSE extends DatabaseContract.TrainingSessionExerciseEntry {}

    private Timer mTimer;
    private long mStartMillis;
    private TextView mDurationTV, mRestTV;


    public ActiveTrainingSessionFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_active_training_session, container, false);

        mDurationTV = view.findViewById(R.id.duraton);
        mRestTV = view.findViewById(R.id.rest);

        Fragment fragment = new TrainingSessionExercisesFragment();

        Bundle args = new Bundle();
        args.putInt(TSE.SESSION_ID, getArguments().getInt(TSE.SESSION_ID));
        args.putInt(TS.TRAINING_DAY_ID, getArguments().getInt(TS.TRAINING_DAY_ID));
        fragment.setArguments(args);
        try {
            String dateTime = getArguments().getString(TS.DATE_TIME);
            mStartMillis = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    .parse(dateTime).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.training_session_container, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();

        return view;
    }


    @Override
    public void onStart() {
        super.onStart();

        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(() -> {
                    long diffMillis = new Date().getTime() - mStartMillis;
                    int hours   = (int) (diffMillis / (1000 * 60 * 60)) % 24;
                    int minutes = (int) (diffMillis / (1000 * 60)) % 60;
                    int seconds = (int) (diffMillis / 1000) % 60;
                    mDurationTV.setText(hours +":"+ minutes +":"+ seconds);
                });
            }

        },0,1000);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }
}
