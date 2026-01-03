package com.example.mama.Nutrition;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "nutrition_tracker.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_MEALS = "meals";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_TIME = "time";
    private static final String COLUMN_NOTES = "notes";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_MEALS_TABLE = "CREATE TABLE " + TABLE_MEALS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_DATE + " TEXT,"
                + COLUMN_TIME + " TEXT,"
                + COLUMN_NOTES + " TEXT"
                + ")";
        db.execSQL(CREATE_MEALS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEALS);
        onCreate(db);
    }

    // Create
    public long addMeal(Meal meal) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, meal.getName());
        values.put(COLUMN_DATE, meal.getDate());
        values.put(COLUMN_TIME, meal.getTime());
        values.put(COLUMN_NOTES, meal.getNotes());

        long id = db.insert(TABLE_MEALS, null, values);
        db.close();
        return id;
    }

    // Read
    public Meal getMeal(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_MEALS,
                new String[]{COLUMN_ID, COLUMN_NAME, COLUMN_DATE, COLUMN_TIME, COLUMN_NOTES},
                COLUMN_ID + "=?",
                new String[]{String.valueOf(id)},
                null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
            Meal meal = new Meal(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4)
            );
            cursor.close();
            return meal;
        }
        return null;
    }

    public List<Meal> getAllMeals() {
        List<Meal> mealList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_MEALS + " ORDER BY " + COLUMN_DATE + " DESC, " + COLUMN_TIME + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Meal meal = new Meal();
                meal.setId(cursor.getInt(0));
                meal.setName(cursor.getString(1));
                meal.setDate(cursor.getString(2));
                meal.setTime(cursor.getString(3));
                meal.setNotes(cursor.getString(4));
                mealList.add(meal);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return mealList;
    }

    // Update
    public int updateMeal(Meal meal) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, meal.getName());
        values.put(COLUMN_DATE, meal.getDate());
        values.put(COLUMN_TIME, meal.getTime());
        values.put(COLUMN_NOTES, meal.getNotes());

        return db.update(TABLE_MEALS, values, COLUMN_ID + "=?",
                new String[]{String.valueOf(meal.getId())});
    }

    // Delete
    public void deleteMeal(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MEALS, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public int getMealsCount() {
        String countQuery = "SELECT * FROM " + TABLE_MEALS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }
}