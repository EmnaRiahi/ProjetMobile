package com.example.mama.sport;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface SleepDao {
    @Query("SELECT * FROM sleep WHERE date = :date LIMIT 1")
    SleepEntity getSleepForDate(String date);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(SleepEntity sleep);
}
