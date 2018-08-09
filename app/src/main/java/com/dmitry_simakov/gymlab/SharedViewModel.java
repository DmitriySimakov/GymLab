package com.dmitry_simakov.gymlab;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.concurrent.atomic.AtomicBoolean;

public class SharedViewModel extends ViewModel {

    private final SingleLiveEvent<Exercise> exercise = new SingleLiveEvent<>();

    public void setExercise(Exercise exercise) {
        this.exercise.setValue(exercise);
    }

    public LiveData<Exercise> getExercise() {
        return exercise;
    }


    public class SingleLiveEvent<T> extends MutableLiveData<T> {

        private static final String TAG = "SingleLiveEvent";

        private final AtomicBoolean mPending = new AtomicBoolean(false);

        @Override
        public void observe(@NonNull LifecycleOwner owner, @NonNull final Observer<T> observer) {

            if (hasActiveObservers()) {
                Log.w(TAG, "Multiple observers registered but only one will be notified of changes.");
            }

            super.observe(owner, t -> {
                if (mPending.compareAndSet(true, false)) {
                    observer.onChanged(t);
                }
            });
        }

        @Override
        public void setValue(T t) {
            mPending.set(true);
            super.setValue(t);
        }
    }
}
