package com.example.mama.sport;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {ActiviteEntity.class}, version = 2, exportSchema = true)
public abstract class ActiviteDatabase extends RoomDatabase {

    private static ActiviteDatabase instance;

    public abstract ActiviteDao activiteDao();

    static final androidx.room.migration.Migration MIGRATION_1_2 = new androidx.room.migration.Migration(1, 2) {
        @Override
        public void migrate(androidx.sqlite.db.SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE activite ADD COLUMN targetSteps INTEGER NOT NULL DEFAULT 6000");
            database.execSQL("ALTER TABLE activite ADD COLUMN isAchieved INTEGER NOT NULL DEFAULT 0");
        }
    };

    public static synchronized ActiviteDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    ActiviteDatabase.class,
                    "activite_db"
            )
            .addMigrations(MIGRATION_1_2)
            .allowMainThreadQueries()
            .build();
        }
        return instance;
    }
}
