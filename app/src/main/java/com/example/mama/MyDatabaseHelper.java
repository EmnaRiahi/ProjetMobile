package com.example.mama;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Mama.db";
    // IMPORTANT : J'ai changé la version à 2 pour forcer la mise à jour de la structure
    private static final int DATABASE_VERSION = 2;

    // --- CONSTANTES TABLE UTILISATEURS ---
    private static final String TABLE_NAME = "users";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_FULLNAME = "fullname";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD = "password";

    // --- CONSTANTES TABLE RENDEZ-VOUS ---
    private static final String TABLE_RDV = "appointments";
    private static final String COL_RDV_ID = "id";
    private static final String COL_RDV_TITLE = "title";
    private static final String COL_RDV_DATE = "date";
    private static final String COL_RDV_TIME = "time";

    public MyDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 1. Création de la table Users
        String queryUser = "CREATE TABLE " + TABLE_NAME +
                " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_FULLNAME + " TEXT, " +
                COLUMN_EMAIL + " TEXT, " +
                COLUMN_PASSWORD + " TEXT);";
        db.execSQL(queryUser);

        // 2. Création de la table Rendez-vous
        String queryRdv = "CREATE TABLE " + TABLE_RDV +
                " (" + COL_RDV_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_RDV_TITLE + " TEXT, " +
                COL_RDV_DATE + " TEXT, " +
                COL_RDV_TIME + " TEXT);";
        db.execSQL(queryRdv);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // En cas de mise à jour, on supprime tout et on recrée
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RDV);
        onCreate(db);
    }

    // ---------------------------------------------------
    // PARTIE GESTION UTILISATEURS (USER)
    // ---------------------------------------------------

    public void addUser(String fullname, String email, String password){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_FULLNAME, fullname);
        cv.put(COLUMN_EMAIL, email);
        cv.put(COLUMN_PASSWORD, password);
        db.insert(TABLE_NAME, null, cv);
    }

    public boolean checkUser(String email, String password){
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME +
                " WHERE " + COLUMN_EMAIL + " = ? AND " + COLUMN_PASSWORD + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email, password});
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

    // ---------------------------------------------------
    // PARTIE GESTION RENDEZ-VOUS (APPOINTMENTS)
    // ---------------------------------------------------

    // Ajouter un RDV
    public void addAppointment(String title, String date, String time){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_RDV_TITLE, title);
        cv.put(COL_RDV_DATE, date);
        cv.put(COL_RDV_TIME, time);
        db.insert(TABLE_RDV, null, cv);
    }

    // Récupérer tous les RDV (du plus récent au plus ancien)
    public Cursor getAllAppointments(){
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_RDV + " ORDER BY " + COL_RDV_ID + " DESC", null);
    }

    // Supprimer un RDV par son ID
    public void deleteAppointment(String id){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_RDV, COL_RDV_ID + "=?", new String[]{id});
    }
}