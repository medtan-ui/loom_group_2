package com.example.loom_group_2.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MotorcycleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Motorcycle> motorcycles);

    @Query("SELECT DISTINCT make FROM motorcycles")
    List<String> getAllMakes();

    @Query("SELECT DISTINCT model FROM motorcycles WHERE make = :make")
    List<String> getModelsByMake(String make);

    @Query("SELECT DISTINCT year FROM motorcycles WHERE make = :make AND model = :model")
    List<Integer> getYearsByMakeAndModel(String make, String model);

    @Query("SELECT * FROM motorcycles WHERE make = :make AND model = :model AND year = :year LIMIT 1")
    Motorcycle getMotorcycle(String make, String model, int year);
    
    @Query("SELECT * FROM motorcycles")
    List<Motorcycle> getAllMotorcycles();
}
