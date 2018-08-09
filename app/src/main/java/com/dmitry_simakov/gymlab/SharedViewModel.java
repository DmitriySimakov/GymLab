package com.dmitry_simakov.gymlab;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {

    private final MutableLiveData<Exercise> exercise = new MutableLiveData<Exercise>();

    public void setExercise(Exercise exercise) {
        this.exercise.setValue(exercise);
    }

    public LiveData<Exercise> getExercise() {
        return exercise;
    }

    @Override
    protected void onCleared() {
        super.onCleared();

    }
}
