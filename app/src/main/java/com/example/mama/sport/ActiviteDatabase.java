package com.example.mama.sport;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {ActiviteEntity.class, SleepEntity.class}, version = 4, exportSchema = true)
public abstract class ActiviteDatabase extends RoomDatabase {

    private static ActiviteDatabase instance;

    public abstract ActiviteDao activiteDao();
    public abstract SleepDao sleepDao();

    static final androidx.room.migration.Migration MIGRATION_1_2 = new androidx.room.migration.Migration(1, 2) {
        @Override
        public void migrate(androidx.sqlite.db.SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE activite ADD COLUMN targetSteps INTEGER NOT NULL DEFAULT 6000");
            database.execSQL("ALTER TABLE activite ADD COLUMN isAchieved INTEGER NOT NULL DEFAULT 0");
        }
    };

    static final androidx.room.migration.Migration MIGRATION_2_3 = new androidx.room.migration.Migration(2, 3) {
        @Override
        public void migrate(androidx.sqlite.db.SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE activite ADD COLUMN distance REAL NOT NULL DEFAULT 0.0");
            database.execSQL("ALTER TABLE activite ADD COLUMN calories REAL NOT NULL DEFAULT 0.0");
        }
    };

    static final androidx.room.migration.Migration MIGRATION_3_4 = new androidx.room.migration.Migration(3, 4) {
        @Override
        public void migrate(androidx.sqlite.db.SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `sleep` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `date` TEXT, `hours` REAL NOT NULL)");
        }
    };

    public static synchronized ActiviteDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    ActiviteDatabase.class,
                    "activite_db"
            )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
            .allowMainThreadQueries()
            .build();
        }
        return instance;
    }
}
