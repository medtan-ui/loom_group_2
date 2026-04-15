package com.example.loom_group_2.logic;

import android.content.Context;
import com.example.loom_group_2.data.AppDatabase;
import com.example.loom_group_2.data.Motorcycle;
import com.example.loom_group_2.data.MotorcycleDao;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VehicleRepository {
    private static VehicleRepository instance;
    private final MotorcycleDao motorcycleDao;
    private final ExecutorService executorService;

    public interface Callback<T> {
        void onResult(T result);
    }

    private VehicleRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        motorcycleDao = db.motorcycleDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public static synchronized VehicleRepository getInstance(Context context) {
        if (instance == null) {
            instance = new VehicleRepository(context);
        }
        return instance;
    }

    public void insertCustomVehicle(Motorcycle m, Callback<Long> callback) {
        executorService.execute(() -> {
            long id = motorcycleDao.insertCustomVehicle(m);
            if (callback != null) callback.onResult(id);
        });
    }

    public void getCustomVehicles(String uid, Callback<List<Motorcycle>> callback) {
        executorService.execute(() -> {
            List<Motorcycle> vehicles = motorcycleDao.getCustomVehiclesByUser(uid);
            if (callback != null) callback.onResult(vehicles);
        });
    }

    public void getVehicleById(int id, Callback<Motorcycle> callback) {
        executorService.execute(() -> {
            Motorcycle vehicle = motorcycleDao.getVehicleById(id);
            if (callback != null) callback.onResult(vehicle);
        });
    }

    public void deleteVehicleById(int id, Runnable onDone) {
        executorService.execute(() -> {
            motorcycleDao.deleteVehicleById(id);
            if (onDone != null) onDone.run();
        });
    }
}
