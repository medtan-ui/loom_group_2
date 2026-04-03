package com.example.loom_group_2.ui;

import java.util.Locale;

public class RouteModel {
    private String name;
    private int durationSeconds;
    private double distanceMeters;
    private double vehicleKpl;

    public RouteModel(String name, int durationSeconds, double distanceMeters, double vehicleKpl) {
        this.name = name;
        this.durationSeconds = durationSeconds;
        this.distanceMeters = distanceMeters;
        this.vehicleKpl = vehicleKpl;
    }

    public String getName() { return name; }

    public String getDurationText() {
        return (durationSeconds / 60) + " mins";
    }

    public String getFuelText() {
        // Convert meters to KM
        double distanceKm = distanceMeters / 1000.0;
        // Calculate fuel based on vehicle efficiency
        double fuelNeeded = distanceKm / vehicleKpl;
        return String.format(Locale.US, "%.1f L", fuelNeeded);
    }
}