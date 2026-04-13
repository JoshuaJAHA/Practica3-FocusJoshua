package mx.unam.fc.icat.focusmony.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class SessionManager extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "FocusBuddy.db";
    private static final int DATABASE_VERSION = 1;

    public SessionManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Creamos la tabla usando las constantes del contrato
        final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + SessionContract.SessionEntry.TABLE_NAME + " (" +
                        SessionContract.SessionEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        SessionContract.SessionEntry.COLUMN_NAME_TYPE + " TEXT," +
                        SessionContract.SessionEntry.COLUMN_NAME_DATE + " TEXT," +
                        SessionContract.SessionEntry.COLUMN_NAME_START_TIME + " TEXT," +
                        SessionContract.SessionEntry.COLUMN_NAME_DURATION + " INTEGER," +
                        SessionContract.SessionEntry.COLUMN_NAME_COMPLETED + " INTEGER)";
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + SessionContract.SessionEntry.TABLE_NAME);
        onCreate(db);
    }

    // Operación Create
    public void addSession(Session session) {
        if (session != null) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put(SessionContract.SessionEntry.COLUMN_NAME_TYPE, session.getType());
            values.put(SessionContract.SessionEntry.COLUMN_NAME_DATE, session.getDate());
            values.put(SessionContract.SessionEntry.COLUMN_NAME_START_TIME, session.getStartTime());
            values.put(SessionContract.SessionEntry.COLUMN_NAME_DURATION, session.getDuration());
            //usamos 1 para true y 0 para false
            values.put(SessionContract.SessionEntry.COLUMN_NAME_COMPLETED, session.isCompleted() ? 1 : 0);

            try {
                db.insert(SessionContract.SessionEntry.TABLE_NAME, null, values);
            } catch (Exception e) {
                e.printStackTrace(); //bloques try-catch
            } finally {
                db.close();
            }
        }
    }

    // Operación Read
    public List<Session> getHistory() {
        List<Session> sessionList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Leemos todo ordenado por ID descendente
        String sortOrder = SessionContract.SessionEntry._ID + " DESC";

        try (Cursor cursor = db.query(
                SessionContract.SessionEntry.TABLE_NAME,
                null, null, null, null, null, sortOrder)) {

            while (cursor.moveToNext()) {
                String type = cursor.getString(cursor.getColumnIndexOrThrow(SessionContract.SessionEntry.COLUMN_NAME_TYPE));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(SessionContract.SessionEntry.COLUMN_NAME_DATE));
                String time = cursor.getString(cursor.getColumnIndexOrThrow(SessionContract.SessionEntry.COLUMN_NAME_START_TIME));
                int duration = cursor.getInt(cursor.getColumnIndexOrThrow(SessionContract.SessionEntry.COLUMN_NAME_DURATION));
                boolean completed = cursor.getInt(cursor.getColumnIndexOrThrow(SessionContract.SessionEntry.COLUMN_NAME_COMPLETED)) == 1;

                sessionList.add(new Session(type, date, time, duration, completed));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sessionList;
    }
}