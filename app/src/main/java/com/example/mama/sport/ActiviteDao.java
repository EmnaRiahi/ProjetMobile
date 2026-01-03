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
}