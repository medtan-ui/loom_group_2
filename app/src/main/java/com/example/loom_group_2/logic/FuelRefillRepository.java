package com.example.loom_group_2.logic;

import android.content.Context;
import com.example.loom_group_2.data.AppDatabase;
import com.example.loom_group_2.data.FuelRefill;
import com.example.loom_group_2.data.FuelRefillDao;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FuelRefillRepository {
    private static FuelRefillRepository instance;
    private final FuelRefillDao fuelRefillDao;
    private final ExecutorService executorService;

    public interface Callback<T> {
        void onResult(T result);
    }

    private FuelRefillRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        fuelRefillDao = db.fuelRefillDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public static synchronized FuelRefillRepository getInstance(Context context) {
        if (instance == null) {
            instance = new FuelRefillRepository(context);
        }
        return instance;
    }

    public void insertRefill(FuelRefill refill, Runnable onDone) {
        executorService.execute(() -> {
            fuelRefillDao.insert(refill);
            if (onDone != null) onDone.run();
        });
    }

    public void getRefillsByUser(String uid, Callback<List<FuelRefill>> callback) {
        executorService.execute(() -> {
            List<FuelRefill> refills = fuelRefillDao.getRefillsByUser(uid);
            if (callback != null) callback.onResult(refills);
        });
    }

    public void getLatestRefill(String uid, Callback<FuelRefill> callback) {
        executorService.execute(() -> {
            FuelRefill refill = fuelRefillDao.getLatestRefill(uid);
            if (callback != null) callback.onResult(refill);
        });
    }

    public void deleteRefill(int id, Runnable onDone) {
        executorService.execute(() -> {
            fuelRefillDao.deleteById(id);
            if (onDone != null) onDone.run();
        });
    }
}
