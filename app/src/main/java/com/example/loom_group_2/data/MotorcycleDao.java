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

    // New methods for Part 2
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertCustomVehicle(Motorcycle m);

    @Query("SELECT * FROM motorcycles WHERE userUid = :uid AND isCustom = 1 ORDER BY createdAt DESC")
    List<Motorcycle> getCustomVehiclesByUser(String uid);

    @Query("SELECT * FROM motorcycles WHERE id = :id LIMIT 1")
    Motorcycle getVehicleById(int id);

    @Query("DELETE FROM motorcycles WHERE id = :id")
    void deleteVehicleById(int id);
}
