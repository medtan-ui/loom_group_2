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
    
    // New Fields for Part 1
    private String userUid;
    private boolean isCustom;
    private String transmission;
    private String nickname;
    private long createdAt;

    public Motorcycle() {
        // Required for Firebase
    }

    // Original constructor
    public Motorcycle(String make, String model, int year, double kpl) {
        this.make = make;
        this.model = model;
        this.year = year;
        this.kpl = kpl;
    }

    // New constructor for custom vehicles (Part 1)
    public Motorcycle(String userUid, String make, String model, int year, double kpl, String transmission, String nickname) {
        this.userUid = userUid;
        this.make = make;
        this.model = model;
        this.year = year;
        this.kpl = kpl;
        this.transmission = transmission;
        this.nickname = nickname;
        this.isCustom = true;
        this.createdAt = System.currentTimeMillis();
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

    public String getUserUid() { return userUid; }
    public void setUserUid(String userUid) { this.userUid = userUid; }

    public boolean isCustom() { return isCustom; }
    public void setCustom(boolean custom) { isCustom = custom; }

    public String getTransmission() { return transmission; }
    public void setTransmission(String transmission) { this.transmission = transmission; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    // Helper method (Part 1)
    public String getDisplayName() {
        StringBuilder sb = new StringBuilder();
        sb.append(year).append(" ").append(make).append(" ").append(model);
        
        if (nickname != null && !nickname.isEmpty()) {
            sb.append(" (").append(nickname).append(")");
        }
        
        if (transmission != null && !transmission.isEmpty()) {
            sb.append(" • ").append(transmission);
        }
        
        return sb.toString();
    }
}
