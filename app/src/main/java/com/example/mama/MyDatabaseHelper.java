package com.example.mama;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Mama.db";
    // VERSION UNIFIÉE : 5 (Pour forcer la mise à jour chez tout le monde)
    private static final int DATABASE_VERSION = 5;

    // --- TABLE UTILISATEURS ---
    private static final String TABLE_NAME = "users";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_FULLNAME = "fullname";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_WEEK = "pregnancy_week";
    private static final String COLUMN_SYMPTOMS = "symptoms";

    // --- TABLE RENDEZ-VOUS ---
    private static final String TABLE_RDV = "appointments";
    private static final String COL_RDV_ID = "id";
    private static final String COL_RDV_TITLE = "title";
    private static final String COL_RDV_DATE = "date";
    private static final String COL_RDV_TIME = "time";

    // --- TABLE HEALTH METRICS (Collègue) ---
    public static final String TABLE_HEALTH_METRICS = "health_metrics";
    public static final String COL_HEALTH_ID = "id";
    public static final String COL_HEALTH_DATE = "date";
    public static final String COL_HEALTH_WEIGHT = "weight";
    public static final String COL_HEALTH_SYSTOLIC = "systolic";
    public static final String COL_HEALTH_DIASTOLIC = "diastolic";

    // --- TABLE SLEEP SESSIONS (Collègue) ---
    private static final String TABLE_SLEEP_SESSIONS = "sleep_sessions";
    private static final String COL_SLEEP_ID = "id";
    private static final String COL_SLEEP_DATE = "date";
    private static final String COL_SLEEP_DEEP_MINUTES = "deep_sleep_minutes";
    private static final String COL_SLEEP_LIGHT_MINUTES = "light_sleep_minutes";
    private static final String COL_SLEEP_SOUND_EVENTS = "sound_events";

    public MyDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 1. Création Users
        String queryUser = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_FULLNAME + " TEXT, " +
                COLUMN_EMAIL + " TEXT, " +
                COLUMN_PASSWORD + " TEXT, " +
                COLUMN_WEEK + " INTEGER, " +
                COLUMN_SYMPTOMS + " TEXT);";
        db.execSQL(queryUser);

        // 2. Création RDV
        String queryRdv = "CREATE TABLE " + TABLE_RDV + " (" +
                COL_RDV_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_RDV_TITLE + " TEXT, " +
                COL_RDV_DATE + " TEXT, " +
                COL_RDV_TIME + " TEXT);";
        db.execSQL(queryRdv);

        // 3. Création Health
        String queryHealth = "CREATE TABLE " + TABLE_HEALTH_METRICS + " (" +
                COL_HEALTH_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_HEALTH_DATE + " TEXT, " +
                COL_HEALTH_WEIGHT + " REAL, " +
                COL_HEALTH_SYSTOLIC + " INTEGER, " +
                COL_HEALTH_DIASTOLIC + " INTEGER);";
        db.execSQL(queryHealth);

        // 4. Création Sleep
        String querySleep = "CREATE TABLE " + TABLE_SLEEP_SESSIONS + " (" +
                COL_SLEEP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_SLEEP_DATE + " TEXT, " +
                COL_SLEEP_DEEP_MINUTES + " INTEGER, " +
                COL_SLEEP_LIGHT_MINUTES + " INTEGER, " +
                COL_SLEEP_SOUND_EVENTS + " INTEGER);";
        db.execSQL(querySleep);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RDV);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HEALTH_METRICS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SLEEP_SESSIONS);
        onCreate(db);
    }

    // ==========================================
    // PARTIE GESTION UTILISATEURS
    // ==========================================
    public void addUser(String fullname, String email, String password, int week, String symptoms){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_FULLNAME, fullname);
        cv.put(COLUMN_EMAIL, email);
        cv.put(COLUMN_PASSWORD, password);
        cv.put(COLUMN_WEEK, week);
        cv.put(COLUMN_SYMPTOMS, symptoms);
        db.insert(TABLE_NAME, null, cv);
    }

    public boolean checkUser(String email, String password){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_EMAIL + " = ? AND " + COLUMN_PASSWORD + " = ?", new String[]{email, password});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public boolean checkEmail(String email){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_EMAIL + " = ?", new String[]{email});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public boolean updatePassword(String email, String newPassword){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_PASSWORD, newPassword);
        long result = db.update(TABLE_NAME, cv, COLUMN_EMAIL + " = ?", new String[]{email});
        return result != -1;
    }

    // ==========================================
    // PARTIE GESTION RENDEZ-VOUS
    // ==========================================
    public void addAppointment(String title, String date, String time){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_RDV_TITLE, title);
        cv.put(COL_RDV_DATE, date);
        cv.put(COL_RDV_TIME, time);
        db.insert(TABLE_RDV, null, cv);
    }

    public Cursor getAllAppointments(){
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_RDV + " ORDER BY " + COL_RDV_ID + " DESC", null);
    }

    public void deleteAppointment(String id){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_RDV, COL_RDV_ID + "=?", new String[]{id});
    }

    // NOUVEAU : MISE À JOUR RDV
    public void updateAppointment(String id, String title, String date, String time){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_RDV_TITLE, title);
        cv.put(COL_RDV_DATE, date);
        cv.put(COL_RDV_TIME, time);
        db.update(TABLE_RDV, cv, COL_RDV_ID + "=?", new String[]{id});
    }

    // ==========================================
    // PARTIE GESTION SANTÉ (HEALTH METRICS)
    // ==========================================
    public void addHealthMetric(String date, float weight, int systolic, int diastolic){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_HEALTH_DATE, date);
        cv.put(COL_HEALTH_WEIGHT, weight);
        cv.put(COL_HEALTH_SYSTOLIC, systolic);
        cv.put(COL_HEALTH_DIASTOLIC, diastolic);
        db.insert(TABLE_HEALTH_METRICS, null, cv);
    }

    public Cursor getAllHealthMetrics(){
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_HEALTH_METRICS + " ORDER BY " + COL_HEALTH_DATE + " ASC", null);
    }

    public void updateHealthMetric(String id, String date, float weight, int systolic, int diastolic){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_HEALTH_DATE, date);
        cv.put(COL_HEALTH_WEIGHT, weight);
        cv.put(COL_HEALTH_SYSTOLIC, systolic);
        cv.put(COL_HEALTH_DIASTOLIC, diastolic);
        db.update(TABLE_HEALTH_METRICS, cv, COL_HEALTH_ID + "=?", new String[]{id});
    }

    public void deleteHealthMetric(String id){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_HEALTH_METRICS, COL_HEALTH_ID + "=?", new String[]{id});
    }

    // ==========================================
    // PARTIE SOMMEIL (SLEEP)
    // ==========================================
    public void addSleepSession(String date, int deepSleepMinutes, int lightSleepMinutes, int soundEvents){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_SLEEP_DATE, date);
        cv.put(COL_SLEEP_DEEP_MINUTES, deepSleepMinutes);
        cv.put(COL_SLEEP_LIGHT_MINUTES, lightSleepMinutes);
        cv.put(COL_SLEEP_SOUND_EVENTS, soundEvents);
        db.insert(TABLE_SLEEP_SESSIONS, null, cv);
    }

    public Cursor getAllSleepSessions(){
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_SLEEP_SESSIONS + " ORDER BY " + COL_SLEEP_ID + " DESC", null);
    }
    // 👇 AJOUTE CETTE MÉTHODE À LA FIN DE TA CLASSE 👇
    public SleepSession getLastSleepSession() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_SLEEP_SESSIONS + " ORDER BY " + COL_SLEEP_ID + " DESC LIMIT 1", null);
        SleepSession session = null;

        if (cursor != null && cursor.moveToFirst()) {
            session = new SleepSession(
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_SLEEP_DATE)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_SLEEP_DEEP_MINUTES)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_SLEEP_LIGHT_MINUTES)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_SLEEP_SOUND_EVENTS))
            );
            cursor.close();
        }
        return session;
    }
}