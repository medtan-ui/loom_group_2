package com.example.loom_group_2.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "motorcycles")
public class Motorcycle {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String make;
    private String model;
    private int year;
    private double kpl;

    public Motorcycle() {
        // Required for Firebase
    }

    public Motorcycle(String make, String model, int year, double kpl) {
        this.make = make;
        this.model = model;
        this.year = year;
        this.kpl = kpl;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getMake() { return make; }
    public void setMake(String make) { this.make = make; }
    
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    
    public double getKpl() { return kpl; }
    public void setKpl(double kpl) { this.kpl = kpl; }
}
