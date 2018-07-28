package com.dmitry_simakov.gymlab.database;

import android.provider.BaseColumns;

public class DbContract {

    private static final String ID = BaseColumns._ID;

    private DbContract() {};

    // gymlab.db

    public static class ExercisesEntry implements BaseColumns {
        public final static String TABLE_NAME = "exercises";

        // Names for external keys
        public final static String MAIN_MUSCLE = "main_muscle";
        public final static String MECHANICS_TYPE = "mechanics_type";
        public final static String EXERCISE_TYPE = "exercise_type";
        public final static String EQUIPMENT = "equipment";

        // Columns names
        public final static String _ID = ID;
        public final static String NAME = "name";
        public final static String IMAGE = "image";
        public final static String MAIN_MUSCLE_ID = MAIN_MUSCLE + ID;
        public final static String MECHANICS_TYPE_ID = MECHANICS_TYPE + ID;
        public final static String EXERCISE_TYPE_ID = EXERCISE_TYPE + ID;
        public final static String EQUIPMENT_ID = EQUIPMENT + ID;
        public final static String DESCRIPTION = "description";
        public final static String TECHNIQUE = "technique";
    }

    public static class MusclesEntry implements BaseColumns {
        public final static String TABLE_NAME = "muscles";

        // Columns names
        public final static String _ID = ID;
        public final static String NAME = "name";
        public final static String IMAGE = "image";
    }

    public static class TargetedMusclesEntry implements BaseColumns {
        public final static String TABLE_NAME = "targeted_muscles";

        // Names for external keys
        public final static String MUSCLE = "muscle";

        // Columns names
        public final static String EXERCISE_ID = "exercise_id";
        public final static String MUSCLE_ID = MUSCLE + ID;
    }

    public static class MechanicsTypesEntry implements BaseColumns {
        public final static String TABLE_NAME = "mechanics_types";

        // Columns names
        public final static String _ID = ID;
        public final static String NAME = "name";
    }

    public static class ExerciseTypesEntry implements BaseColumns {
        public final static String TABLE_NAME = "exercise_types";

        // Columns names
        public final static String _ID = ID;
        public final static String NAME = "name";
    }

    public static class EquipmentEntry implements BaseColumns {
        public final static String TABLE_NAME = "equipment";

        // Columns names
        public final static String _ID = ID;
        public final static String NAME = "name";
    }

    // body_measures.db

    public static class BodyMeasurementsEntry implements BaseColumns {
        public final static String TABLE_NAME = "body_measurements";

        // Names for external keys
        public final static String BODY_PARAMETER = "body_parameter";

        // Columns names
        public final static String _ID = ID;
        public final static String DATE = "date";
        public final static String BODY_PARAMETER_ID = BODY_PARAMETER + ID;
        public final static String VALUE = "value";
    }

    public static class BodyParametersEntry implements BaseColumns {
        public final static String TABLE_NAME = "body_parameters";

        // Columns names
        public final static String _ID = ID;
        public final static String NAME = "name";
        public final static String IMAGE = "image";
        public final static String INSTRUCTION = "instruction";
    }
}
