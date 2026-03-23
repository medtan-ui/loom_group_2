package com.example.loom_group_2.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {TripLog.class, Motorcycle.class}, version = 3) // Increased from 2 to 3
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;
    public abstract TripDao tripDao();
    public abstract MotorcycleDao motorcycleDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class, "loom_database")
                    .fallbackToDestructiveMigration() // This will clear the old DB and start fresh
                    .build();
        }
        return instance;
    }
}
