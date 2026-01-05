package com.example.mama.sport;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ActiviteDao {
    @Insert
    void insert(ActiviteEntity activite);

    @Update
    void update(ActiviteEntity activite);

    @Delete
    void delete(ActiviteEntity activite);

    @Query("SELECT * FROM activite ORDER BY id DESC")
    List<ActiviteEntity> getAllActivities();

    @Query("SELECT * FROM activite WHERE isAchieved = 0 ORDER BY id DESC LIMIT 1")
    ActiviteEntity getActiveSession();

    @Query("SELECT * FROM activite WHERE isAchieved = 1 ORDER BY id DESC")
    List<ActiviteEntity> getAchievedSessions();

    @Query("SELECT * FROM activite WHERE isAchieved = 0 ORDER BY id DESC")
    List<ActiviteEntity> getUnachievedSessions();

    @Query("SELECT SUM(steps) FROM activite")
    int getTotalSteps();

    @Query("SELECT COUNT(*) FROM activite")
    int getCount();

    @Query("SELECT SUM(distance) FROM activite WHERE isAchieved = 1")
    double getTotalDistanceAchieved();

    @Query("SELECT SUM(calories) FROM activite WHERE isAchieved = 1")
    double getTotalCaloriesAchieved();
}