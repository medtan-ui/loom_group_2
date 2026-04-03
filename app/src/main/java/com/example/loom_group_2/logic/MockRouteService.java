package com.example.loom_group_2.logic;

import android.os.Handler;
import android.os.Looper;
import java.util.Random;

public class MockRouteService {
    public interface RouteCallback {
        void onRouteCalculated(double distanceKm, int durationMins, double fuelEstimateLiters);
    }

    public static void calculateRoute(String origin, String destination, RouteCallback callback) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Random r = new Random();
            double distance = 5 + (25 - 5) * r.nextDouble();
            int duration = (int) (distance * 1.5 + r.nextInt(10));
            double fuel = FuelCalculator.estimateFuelConsumption(distance, 12.5);
            callback.onRouteCalculated(distance, duration, fuel);
        }, 1500);
    }
}
