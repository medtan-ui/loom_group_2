package com.example.loom_group_2.logic;

public class FuelCalculator {
    public static double computeEfficiency(double distanceKm, double fuelLiters) {
        if (fuelLiters <= 0) return 0;
        return distanceKm / fuelLiters;
    }

    public static double estimateFuelConsumption(double distanceKm, double efficiencyKmL) {
        if (efficiencyKmL <= 0) return 0;
        return distanceKm / efficiencyKmL;
    }
}
