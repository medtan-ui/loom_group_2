package com.example.loom_group_2.ui;

import java.util.Locale;

public class RouteModel {
    private String name;
    private int durationSeconds;
    private double distanceMeters;
    private double vehicleKpl;
    private String polylinePoints;

    public RouteModel(String name, int durationSeconds, double distanceMeters, double vehicleKpl) {
        this.name = name;
        this.durationSeconds = durationSeconds;
        this.distanceMeters = distanceMeters;
        this.vehicleKpl = vehicleKpl;
    }

    public RouteModel(String name, int durationSeconds, double distanceMeters, double vehicleKpl, String polylinePoints) {
        this.name = name;
        this.durationSeconds = durationSeconds;
        this.distanceMeters = distanceMeters;
        this.vehicleKpl = vehicleKpl;
        this.polylinePoints = polylinePoints;
    }

    public String getName() { return name; }

    public String getDurationText() {
        return (durationSeconds / 60) + " mins";
    }

    public String getDistanceText() {
        return String.format(Locale.US, "%.1f km", distanceMeters / 1000.0);
    }

    public String getFuelText() {
        double distanceKm = distanceMeters / 1000.0;
        double fuelNeeded = distanceKm / vehicleKpl;
        return String.format(Locale.US, "%.1f L", fuelNeeded);
    }

    public String getPolylinePoints() {
        return polylinePoints;
    }

    public void setPolylinePoints(String polylinePoints) {
        this.polylinePoints = polylinePoints;
    }
}
