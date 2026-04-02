package com.example.loom_group_2.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "trip_logs")
public class TripLog {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String date;
    private String title;
    private String time;
    private String fuel;

    public TripLog(String date, String title, String time, String fuel) {
        this.date = date;
        this.title = title;
        this.time = time;
        this.fuel = fuel;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public String getFuel() { return fuel; }
    public void setFuel(String fuel) { this.fuel = fuel; }
}
