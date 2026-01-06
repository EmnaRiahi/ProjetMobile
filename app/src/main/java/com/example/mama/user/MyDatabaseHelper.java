package com.example.mama.user;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.mama.sante.SleepSession;

public class MyDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Mama.db";
    private static final int DATABASE_VERSION = 9; // Version augmentée pour le log des médicaments

    // --- TABLE UTILISATEURS ---
    private static final String TABLE_NAME = "users";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_FULLNAME = "fullname";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_WEEK = "pregnancy_week";
    private static final String COLUMN_SYMPTOMS = "symptoms";
    private static final String COLUMN_WEIGHT = "weight";
    private static final String COLUMN_HEIGHT = "height";
    private static final String COLUMN_AGE = "age";
    private static final String COLUMN_IMAGE = "image";

    // --- TABLE RENDEZ-VOUS ---
    private static final String TABLE_RDV = "appointments";
    private static final String COL_RDV_ID = "id";
    private static final String COL_RDV_TITLE = "title";
    private static final String COL_RDV_DATE = "date";
    private static final String COL_RDV_TIME = "time";

    // --- TABLE HEALTH METRICS ---
    public static final String TABLE_HEALTH_METRICS = "health_metrics";
    public static final String COL_HEALTH_ID = "id";
    public static final String COL_HEALTH_DATE = "date";
    public static final String COL_HEALTH_WEIGHT = "weight";
    public static final String COL_HEALTH_SYSTOLIC = "systolic";
    public static final String COL_HEALTH_DIASTOLIC = "diastolic";

    // --- TABLE SLEEP SESSIONS ---
    private static final String TABLE_SLEEP_SESSIONS = "sleep_sessions";
    private static final String COL_SLEEP_ID = "id";
    private static final String COL_SLEEP_DATE = "date";
    private static final String COL_SLEEP_DEEP_MINUTES = "deep_sleep_minutes";
    private static final String COL_SLEEP_LIGHT_MINUTES = "light_sleep_minutes";
    private static final String COL_SLEEP_SOUND_EVENTS = "sound_events";

    // --- TABLE MÉDICAMENTS ---
    private static final String TABLE_MEDS = "medications";
    private static final String COL_MED_ID = "id";
    private static final String COL_MED_NAME = "name";
    private static final String COL_MED_GENERIC = "generic";
    private static final String COL_MED_TIME = "time";
    private static final String COL_MED_DOSES = "doses";
    private static final String COL_MED_FREQUENCY = "frequency";
    private static final String COL_MED_SCHEDULE = "schedule";

    // --- TABLE LOGS MÉDICAMENTS (NOUVEAU) ---
    private static final String TABLE_MED_LOGS = "medication_logs";
    private static final String COL_LOG_ID = "id";
    private static final String COL_LOG_MED_ID = "med_id";
    private static final String COL_LOG_STATUS = "status"; // "Taken" or "Missed"
    private static final String COL_LOG_TIMESTAMP = "timestamp";
    private static final String COL_LOG_SCHEDULED_TIME = "scheduled_time";

    public MyDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 1. Users
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_FULLNAME + " TEXT, " +
                COLUMN_EMAIL + " TEXT, " +
                COLUMN_PASSWORD + " TEXT, " +
                COLUMN_WEEK + " INTEGER, " +
                COLUMN_SYMPTOMS + " TEXT, " +
                COLUMN_WEIGHT + " REAL, " +
                COLUMN_HEIGHT + " REAL, " +
                COLUMN_AGE + " INTEGER, " +
                COLUMN_IMAGE + " TEXT);");

        // 2. RDV
        db.execSQL("CREATE TABLE " + TABLE_RDV + " (" +
                COL_RDV_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_RDV_TITLE + " TEXT, " +
                COL_RDV_DATE + " TEXT, " +
                COL_RDV_TIME + " TEXT);");

        // 3. Health
        db.execSQL("CREATE TABLE " + TABLE_HEALTH_METRICS + " (" +
                COL_HEALTH_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_HEALTH_DATE + " TEXT, " +
                COL_HEALTH_WEIGHT + " REAL, " +
                COL_HEALTH_SYSTOLIC + " INTEGER, " +
                COL_HEALTH_DIASTOLIC + " INTEGER);");

        // 4. Sleep
        db.execSQL("CREATE TABLE " + TABLE_SLEEP_SESSIONS + " (" +
                COL_SLEEP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_SLEEP_DATE + " TEXT, " +
                COL_SLEEP_DEEP_MINUTES + " INTEGER, " +
                COL_SLEEP_LIGHT_MINUTES + " INTEGER, " +
                COL_SLEEP_SOUND_EVENTS + " INTEGER);");

        // 5. Meds
        db.execSQL("CREATE TABLE " + TABLE_MEDS + " (" +
                COL_MED_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_MED_NAME + " TEXT, " +
                COL_MED_GENERIC + " TEXT, " +
                COL_MED_TIME + " TEXT, " +
                COL_MED_DOSES + " TEXT, " +
                COL_MED_FREQUENCY + " TEXT, " +
                COL_MED_SCHEDULE + " TEXT);");

        // Table logs
        db.execSQL("CREATE TABLE " + TABLE_MED_LOGS + " (" +
                COL_LOG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_LOG_MED_ID + " TEXT, " +
                COL_LOG_STATUS + " TEXT, " +
                COL_LOG_TIMESTAMP + " INTEGER, " +
                COL_LOG_SCHEDULED_TIME + " TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RDV);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HEALTH_METRICS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SLEEP_SESSIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEDS);
        onCreate(db);
    }

    // ==========================================
    // PARTIE 1 : UTILISATEURS
    // ==========================================
    public void addUser(String fullname, String email, String password, int week, String symptoms, double weight, double height, int age){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_FULLNAME, fullname);
        cv.put(COLUMN_EMAIL, email);
        cv.put(COLUMN_PASSWORD, password);
        cv.put(COLUMN_WEEK, week);
        cv.put(COLUMN_SYMPTOMS, symptoms);
        cv.put(COLUMN_WEIGHT, weight);
        cv.put(COLUMN_HEIGHT, height);
        cv.put(COLUMN_AGE, age);
        cv.put(COLUMN_IMAGE, ""); // Initialisé à vide
        db.insert(TABLE_NAME, null, cv);
    }

    public Cursor getUserByEmail(String email){
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_EMAIL + " = ?", new String[]{email});
    }

    public void updateUser(String email, String fullname, int age, double weight, double height, int week, String symptoms, String image){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_FULLNAME, fullname);
        cv.put(COLUMN_AGE, age);
        cv.put(COLUMN_WEIGHT, weight);
        cv.put(COLUMN_HEIGHT, height);
        cv.put(COLUMN_WEEK, week);
        cv.put(COLUMN_SYMPTOMS, symptoms);
        cv.put(COLUMN_IMAGE, image);
        db.update(TABLE_NAME, cv, COLUMN_EMAIL + " = ?", new String[]{email});
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
    // PARTIE 2 : RENDEZ-VOUS (AVEC UPDATE !)
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

    // C'EST CETTE MÉTHODE QUI MANQUAIT :
    public void updateAppointment(String id, String title, String date, String time){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_RDV_TITLE, title);
        cv.put(COL_RDV_DATE, date);
        cv.put(COL_RDV_TIME, time);
        db.update(TABLE_RDV, cv, COL_RDV_ID + "=?", new String[]{id});
    }

    // ==========================================
    // PARTIE 3 : MÉDICAMENTS (AVEC UPDATE !)
    // ==========================================
    public void addMedication(String name, String generic, String time, String doses, String frequency, String schedule){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_MED_NAME, name);
        cv.put(COL_MED_GENERIC, generic);
        cv.put(COL_MED_TIME, time);
        cv.put(COL_MED_DOSES, doses);
        cv.put(COL_MED_FREQUENCY, frequency);
        cv.put(COL_MED_SCHEDULE, schedule);
        db.insert(TABLE_MEDS, null, cv);
    }

    public Cursor getAllMedications(){
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_MEDS + " ORDER BY " + COL_MED_TIME + " ASC", null);
    }

    public void updateMedication(String id, String name, String generic, String time, String doses, String frequency, String schedule){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_MED_NAME, name);
        cv.put(COL_MED_GENERIC, generic);
        cv.put(COL_MED_TIME, time);
        cv.put(COL_MED_DOSES, doses);
        cv.put(COL_MED_FREQUENCY, frequency);
        cv.put(COL_MED_SCHEDULE, schedule);
        db.update(TABLE_MEDS, cv, COL_MED_ID + "=?", new String[]{id});
    }

    public void deleteMedication(String id){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MEDS, COL_MED_ID + "=?", new String[]{id});
    }

    // --- LOGS MÉDICAMENTS ---
    public void logMedicationResponse(String medId, String status, String scheduledTime) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_LOG_MED_ID, medId);
        cv.put(COL_LOG_STATUS, status);
        cv.put(COL_LOG_TIMESTAMP, System.currentTimeMillis());
        cv.put(COL_LOG_SCHEDULED_TIME, scheduledTime);
        db.insert(TABLE_MED_LOGS, null, cv);
    }

    public Cursor getAllMedicationLogs() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_MED_LOGS + " ORDER BY " + COL_LOG_TIMESTAMP + " DESC", null);
    }

    public String getMedicationStatusToday(String medId, String scheduledTime) {
        SQLiteDatabase db = this.getReadableDatabase();
        // Get start and end of TODAY in milliseconds
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        long startOfDay = cal.getTimeInMillis();
        
        cal.set(java.util.Calendar.HOUR_OF_DAY, 23);
        cal.set(java.util.Calendar.MINUTE, 59);
        cal.set(java.util.Calendar.SECOND, 59);
        long endOfDay = cal.getTimeInMillis();

        String query = "SELECT " + COL_LOG_STATUS + " FROM " + TABLE_MED_LOGS + 
                       " WHERE " + COL_LOG_MED_ID + "=? AND " + COL_LOG_SCHEDULED_TIME + "=? AND " +
                       COL_LOG_TIMESTAMP + " BETWEEN ? AND ? ORDER BY " + COL_LOG_TIMESTAMP + " DESC";
        
        Cursor cursor = db.rawQuery(query, new String[]{medId, scheduledTime, String.valueOf(startOfDay), String.valueOf(endOfDay)});
        String status = null;
        if (cursor != null && cursor.moveToFirst()) {
            status = cursor.getString(0);
            cursor.close();
        }
        return status;
    }

    public Cursor getAdherenceStats() {
        SQLiteDatabase db = this.getReadableDatabase();
        // Query to get Medication Name, Count Taken, Count Missed
        // We join TABLE_MEDS with TABLE_MED_LOGS
        String query = "SELECT m." + COL_MED_NAME + ", " +
                "COUNT(CASE WHEN l." + COL_LOG_STATUS + " = 'Taken' THEN 1 END) as taken_count, " +
                "COUNT(CASE WHEN l." + COL_LOG_STATUS + " = 'Missed' THEN 1 END) as missed_count " +
                "FROM " + TABLE_MEDS + " m " +
                "LEFT JOIN " + TABLE_MED_LOGS + " l ON m." + COL_MED_ID + " = l." + COL_LOG_MED_ID + " " +
                "GROUP BY m." + COL_MED_ID;
        return db.rawQuery(query, null);
    }

    // ==========================================
    // PARTIE 4 : SANTÉ & SOMMEIL (Collègue)
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

    // 👇 AJOUTE CETTE MÉTHODE QUI MANQUE 👇
    public Cursor getUserDetails(String email){
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_EMAIL + " = ?", new String[]{email});
    }
}