package com.example.loom_group_2.data;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DatabaseInitializer {
    private static final String TAG = "DatabaseInitializer";

    public static void initializeDatabase(Context context, AppDatabase db) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            if (db.motorcycleDao().getAllMotorcycles().isEmpty()) {
                Log.d(TAG, "Initializing motorcycle database...");
                List<Motorcycle> motorcycles = loadMotorcyclesFromCsv(context);
                if (!motorcycles.isEmpty()) {
                    db.motorcycleDao().insertAll(motorcycles);
                    Log.d(TAG, "Inserted " + motorcycles.size() + " motorcycles into Room.");
                }
            } else {
                Log.d(TAG, "Motorcycle database already initialized.");
            }
        });
    }

    private static List<Motorcycle> loadMotorcyclesFromCsv(Context context) {
        List<Motorcycle> motorcycles = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(context.getAssets().open("vehicles_expanded.csv")))) {
            String line;
            reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(",");
                if (tokens.length >= 9) {
                    try {
                        String make = tokens[0].trim();
                        String model = tokens[1].trim();
                        int year = Integer.parseInt(tokens[2].trim());
                        String fuelType = tokens[3].trim();
                        String transmission = tokens[4].trim();
                        double mpgCity = Double.parseDouble(tokens[5].trim());
                        double mpgHigh = Double.parseDouble(tokens[6].trim());
                        double kplCity = Double.parseDouble(tokens[7].trim());
                        double kplHighway = Double.parseDouble(tokens[8].trim());

                        motorcycles.add(new Motorcycle(make, model, year, fuelType, transmission,
                                mpgCity, mpgHigh, kplCity, kplHighway));
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "Error parsing line: " + line, e);
                    }
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error reading CSV file", e);
        }
        return motorcycles;
    }
}
