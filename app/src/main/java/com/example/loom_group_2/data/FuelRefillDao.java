package com.example.loom_group_2.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface FuelRefillDao {
    @Insert
    void insert(FuelRefill refill);

    @Query("SELECT * FROM fuel_refills WHERE userUid = :uid ORDER BY id DESC")
    List<FuelRefill> getRefillsByUser(String uid);

    @Query("SELECT * FROM fuel_refills WHERE userUid = :uid ORDER BY id DESC LIMIT 1")
    FuelRefill getLatestRefill(String uid);

    @Query("DELETE FROM fuel_refills WHERE id = :id")
    void deleteById(int id);
}
