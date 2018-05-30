package com.dmitry_simakov.gymlab.database;

import android.provider.BaseColumns;

public class DbContract {

    private DbContract() {};

    public static final class ExerciseEntry implements BaseColumns {
        public final static String TABLE_NAME = "exercises";

        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_NAME ="name";
        public final static String COLUMN_MUSCLE_TARGETED ="muscle_targeted";
        public final static String COLUMN_DESCRIPTION ="description";
        public final static String COLUMN_TECHNIQUE ="technique";
    }

    public static final class MuscleEntry implements BaseColumns {
        public final static String TABLE_NAME = "muscles";

        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_NAME ="name";
    }
}
