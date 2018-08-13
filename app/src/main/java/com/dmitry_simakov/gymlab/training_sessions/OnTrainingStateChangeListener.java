package com.dmitry_simakov.gymlab.training_sessions;

public interface OnTrainingStateChangeListener {

    void onStartTrainingSession(String dateTime);

    int onFinishTrainingSession();
}
