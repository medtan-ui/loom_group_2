package com.example.loom_group_2.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "fuel_refills")
public class FuelRefill {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String userUid;
    private double litersAdded;
    private double odometerBefore;
    private double odometerAfter;
    private double calculatedKpl;
    private String date;
    private String notes;

    public FuelRefill() {
    }

    public FuelRefill(String userUid, double litersAdded, double odometerBefore, double odometerAfter, double calculatedKpl, String date, String notes) {
        this.userUid = userUid;
        this.litersAdded = litersAdded;
        this.odometerBefore = odometerBefore;
        this.odometerAfter = odometerAfter;
        this.calculatedKpl = calculatedKpl;
        this.date = date;
        this.notes = notes;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserUid() {
        return userUid;
    }

    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }

    public double getLitersAdded() {
        return litersAdded;
    }

    public void setLitersAdded(double litersAdded) {
        this.litersAdded = litersAdded;
    }

    public double getOdometerBefore() {
        return odometerBefore;
    }

    public void setOdometerBefore(double odometerBefore) {
        this.odometerBefore = odometerBefore;
    }

    public double getOdometerAfter() {
        return odometerAfter;
    }

    public void setOdometerAfter(double odometerAfter) {
        this.odometerAfter = odometerAfter;
    }

    public double getCalculatedKpl() {
        return calculatedKpl;
    }

    public void setCalculatedKpl(double calculatedKpl) {
        this.calculatedKpl = calculatedKpl;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
