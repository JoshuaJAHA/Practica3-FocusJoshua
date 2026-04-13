package mx.unam.fc.icat.focusmony.model;

import android.provider.BaseColumns;

public final class SessionContract {
    private SessionContract() {} // Constructor vacío

    public static class SessionEntry implements BaseColumns {
        public static final String TABLE_NAME = "sessions_history";
        public static final String COLUMN_NAME_TYPE = "type";
        public static final String COLUMN_NAME_DATE = "date";
        public static final String COLUMN_NAME_START_TIME = "time";
        public static final String COLUMN_NAME_DURATION = "duration";
        public static final String COLUMN_NAME_COMPLETED = "completed";
    }
}