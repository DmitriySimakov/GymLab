package com.dmitry_simakov.gymlab.exercises;

public class Exercise {

    private long mId;
    private String mExerciseName, mImagePath;

    public Exercise(long id, String name, String imagePath) {
        mId = id;
        mExerciseName = name;
        mImagePath = imagePath;
    }

    public long getId() {
        return mId;
    }

    public String getImagePath() {
        return mImagePath;
    }

    public String getExerciseName() {
        return mExerciseName;
    }
}
