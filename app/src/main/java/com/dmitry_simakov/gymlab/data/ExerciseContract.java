package com.dmitry_simakov.gymlab.data;

import android.provider.BaseColumns;

public class ExerciseContract {

    private ExerciseContract() {};

    public static final class ExerciseEntry implements BaseColumns {

        public final static String TABLE_NAME = "exercises";

        public final static String _ID = BaseColumns._ID;

        public final static String COLUMN_EXERCISE_NAME ="name";
        public final static String COLUMN_MAJOR_MUSCLES ="major_muscles";
        public final static String COLUMN_DESCRIPTION ="description";
        public final static String COLUMN_TECHNIQUE ="technique";
    }
}
