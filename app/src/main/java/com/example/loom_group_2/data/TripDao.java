package com.example.loom_group_2.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface TripDao {
    @Insert
    void insert(TripLog tripLog);

    @Delete
    void delete(TripLog tripLog);

    @Query("SELECT * FROM trip_logs ORDER BY id DESC")
    List<TripLog> getAllLogs();

    @Query("DELETE FROM trip_logs WHERE id = :id")
    void deleteById(int id);
}
