package com.example.mama.sport;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {ActiviteEntity.class}, version = 1, exportSchema = false)
public abstract class ActiviteDatabase extends RoomDatabase {

    private static ActiviteDatabase instance;

    public abstract ActiviteDao activiteDao();

    public static synchronized ActiviteDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    ActiviteDatabase.class,
                    "activite_db"
            ).allowMainThreadQueries().build();
        }
        return instance;
    }
}
