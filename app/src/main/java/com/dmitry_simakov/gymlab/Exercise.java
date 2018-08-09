package com.dmitry_simakov.gymlab;

public class Exercise {

    private long mId;
    private String mImageName, mExerciseName;

    public Exercise(long id, String imageName, String name) {
        mId = id;
        mImageName = imageName;
        mExerciseName = name;
    }

    public long getId() {
        return mId;
    }

    public String getImageName() {
        return mImageName;
    }

    public String getExerciseName() {
        return mExerciseName;
    }
}
