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
    private String fuel_type;
    private String transmission;
    private double mpg_city;
    private double mpg_highway;
    private double kpl_city;
    private double kpl_highway;

    public Motorcycle() {
        // Required for Firebase
    }

    public Motorcycle(String make, String model, int year, String fuel_type, String transmission, 
                      double mpg_city, double mpg_highway, double kpl_city, double kpl_highway) {
        this.make = make;
        this.model = model;
        this.year = year;
        this.fuel_type = fuel_type;
        this.transmission = transmission;
        this.mpg_city = mpg_city;
        this.mpg_highway = mpg_highway;
        this.kpl_city = kpl_city;
        this.kpl_highway = kpl_highway;
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
    
    public String getFuel_type() { return fuel_type; }
    public void setFuel_type(String fuel_type) { this.fuel_type = fuel_type; }
    
    public String getTransmission() { return transmission; }
    public void setTransmission(String transmission) { this.transmission = transmission; }
    
    public double getMpg_city() { return mpg_city; }
    public void setMpg_city(double mpg_city) { this.mpg_city = mpg_city; }
    
    public double getMpg_highway() { return mpg_highway; }
    public void setMpg_highway(double mpg_highway) { this.mpg_highway = mpg_highway; }
    
    public double getKpl_city() { return kpl_city; }
    public void setKpl_city(double kpl_city) { this.kpl_city = kpl_city; }
    
    public double getKpl_highway() { return kpl_highway; }
    public void setKpl_highway(double kpl_highway) { this.kpl_highway = kpl_highway; }
}
