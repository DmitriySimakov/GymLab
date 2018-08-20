package com.dmitry_simakov.gymlab.database;

import android.net.Uri;
import android.provider.BaseColumns;

public class DatabaseContract {

    public static final String ID = BaseColumns._ID;

    private static final String CONTENT_AUTHORITY = "com.dmitry_simakov.gymlab";
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    private DatabaseContract() {};

    //______________________________ Exercises ______________________________

    public static class ExerciseEntry implements BaseColumns {
        public final static String TABLE_NAME = "exercise";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, TABLE_NAME);

        // Names for external keys
        public final static String MAIN_MUSCLE = "main_muscle";
        public final static String TARGETED_MUSCLES = "targeted_muscles";
        public final static String MECHANICS_TYPE = "mechanics_type";
        public final static String EXERCISE_TYPE = "exercise_type";
        public final static String EQUIPMENT = "equipment";

        // Columns names
        public final static String _ID = ID;
        public final static String NAME = "name";
        public final static String MAIN_MUSCLE_ID = MAIN_MUSCLE + ID;
        public final static String MECHANICS_TYPE_ID = MECHANICS_TYPE + ID;
        public final static String EXERCISE_TYPE_ID = EXERCISE_TYPE + ID;
        public final static String EQUIPMENT_ID = EQUIPMENT + ID;
        public final static String DESCRIPTION = "description";
    }

    public static class MuscleEntry implements BaseColumns {
        public final static String TABLE_NAME = "muscle";

        // Columns names
        public final static String _ID = ID;
        public final static String NAME = "name";
    }

    public static class TargetedMuscleEntry implements BaseColumns {
        public final static String TABLE_NAME = "targeted_muscle";

        // Names for external keys
        public final static String MUSCLE = "muscle";

        // Columns names
        public final static String EXERCISE_ID = "exercise_id";
        public final static String MUSCLE_ID = MUSCLE + ID;
    }

    public static class MechanicsTypeEntry implements BaseColumns {
        public final static String TABLE_NAME = "mechanics_type";

        // Columns names
        public final static String _ID = ID;
        public final static String NAME = "name";
    }

    public static class ExerciseTypeEntry implements BaseColumns {
        public final static String TABLE_NAME = "exercise_type";

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

    //______________________________ Training Programs ______________________________

    public static class TrainingProgramEntry implements BaseColumns {
        public final static String TABLE_NAME = "training_program";

        // Columns names
        public final static String _ID = ID;
        public final static String NAME = "name";
    }

    public static class TrainingProgramDayEntry implements BaseColumns {
        public final static String TABLE_NAME = "training_program_day";

        // Names for external keys
        public final static String PROGRAM = "program";

        // Columns names
        public final static String _ID = ID;
        public final static String NAME = "name";
        public final static String PROGRAM_ID = PROGRAM + ID;
        public final static String NUMBER = "number";
    }

    public static class TrainingProgramExerciseEntry implements BaseColumns {
        public final static String TABLE_NAME = "training_program_exercise";

        // Names for external keys
        public final static String TRAINING_DAY = "training_day";
        public final static String EXERCISE = "exercise";

        // Columns names
        public final static String _ID = ID;
        public final static String TRAINING_DAY_ID = TRAINING_DAY + ID;
        public final static String EXERCISE_ID = EXERCISE + ID;
        public final static String NUMBER = "number";
        public final static String PARAMS_BOOL_ARR = "params_bool_arr";
        public final static String STRATEGY = "strategy";
    }

    //______________________________ Training Sessions ______________________________

    public static class TrainingSessionEntry implements BaseColumns {
        public final static String TABLE_NAME = "training_session";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, TABLE_NAME);

        // Names for external keys
        public final static String TRAINING_DAY = "training_day";

        // Columns names
        public final static String _ID = ID;
        public final static String DATE_TIME = "date_time";
        public final static String TRAINING_DAY_ID = TRAINING_DAY + ID;
        public final static String DURATION = "duration";
    }

    public static class TrainingSessionExerciseEntry implements BaseColumns {
        public final static String TABLE_NAME = "training_session_exercise";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, TABLE_NAME);

        // Names for external keys
        public final static String SESSION = "session";
        public final static String EXERCISE = "exercise";

        // Columns names
        public final static String _ID = ID;
        public final static String SESSION_ID = SESSION + ID;
        public final static String EXERCISE_ID = EXERCISE + ID;
        public final static String NUMBER = "number";
        public final static String PARAMS_BOOL_ARR = "params_bool_arr";
    }

    public static class TrainingSessionSetEntry implements BaseColumns {
        public final static String TABLE_NAME = "training_session_set";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, TABLE_NAME);

        // Names for external keys
        public final static String TS_EXERCISE = "ts_exercise";

        // Columns names
        public final static String _ID = ID;
        public final static String TS_EXERCISE_ID = TS_EXERCISE + ID;
        public final static String SECS_SINCE_START = "secs_since_start";
        public final static String WEIGHT = "weight";
        public final static String REPS = "reps";
        public final static String TIME = "_time";
        public final static String DISTANCE = "distance";
    }

    //______________________________ Measurements ______________________________

    public static class BodyMeasurementEntry implements BaseColumns {
        public final static String TABLE_NAME = "body_measurement";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, TABLE_NAME);

        // Names for external keys
        public final static String BODY_PARAM = "body_param";
        public final static String PREV_DATE = "prev_date";
        public final static String PREV_VALUE = "prev_value";

        // Columns names
        public final static String _ID = ID;
        public final static String DATE = "_date";
        public final static String BODY_PARAM_ID = BODY_PARAM + ID;
        public final static String VALUE = "_value";
    }

    public static class BodyMeasurementParamEntry implements BaseColumns {
        public final static String TABLE_NAME = "body_measurement_param";

        // Columns names
        public final static String _ID = ID;
        public final static String NAME = "name";
        public final static String IMAGE = "image";
        public final static String INSTRUCTION = "instruction";
        public final static String COEFFICIENT = "coefficient";
    }
}
