package com.dmitry_simakov.gymlab.database;

import android.provider.BaseColumns;

public class DbContract {

    private DbContract() {};

    public static final class ExercisesEntry implements BaseColumns {
        public final static String TABLE_NAME = "exercises";

        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_NAME ="name";
        public final static String COLUMN_IMAGE ="image";
        public final static String COLUMN_MAIN_MUSCLE_ID ="main_muscle_id";
        public final static String COLUMN_MECHANICS_TYPE ="mechanics_type";
        public final static String COLUMN_EXERCISE_TYPE_ID ="exercise_type_id";
        public final static String COLUMN_EQUIPMENT_ID ="equipment_id";
        public final static String COLUMN_DESCRIPTION ="description";
        public final static String COLUMN_TECHNIQUE ="technique";
    }

    public static final class MusclesEntry implements BaseColumns {
        public final static String TABLE_NAME = "muscles";

        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_NAME ="name";
        public final static String COLUMN_IMAGE ="image";
    }

    public static final class TargetedMusclesEntry implements BaseColumns {
        public final static String TABLE_NAME = "targeted_muscles";

        public final static String COLUMN_EXERCISE_ID ="exercise_id";
        public final static String COLUMN_MUSCLE_ID ="muscle_id";
        public final static String COLUMN_TYPE ="type";
    }

    public static final class ExerciseTypesEntry implements BaseColumns {
        public final static String TABLE_NAME = "exercise_types";

        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_NAME ="name";
    }

    public static final class EquipmentEntry implements BaseColumns {
        public final static String TABLE_NAME = "equipment";

        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_NAME ="name";
    }
}
