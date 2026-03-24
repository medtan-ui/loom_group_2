package com.example.loom_group_2.logic;

import android.content.Context;
import com.example.loom_group_2.data.AppDatabase;
import com.example.loom_group_2.data.TripDao;
import com.example.loom_group_2.data.TripLog;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DataPersistenceController {
    private static DataPersistenceController instance;
    private final TripDao tripDao;
    private final ExecutorService executorService;

    private DataPersistenceController(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.tripDao = db.tripDao();
        // Use a fixed pool to handle concurrent DB requests better
        this.executorService = Executors.newFixedThreadPool(2);
    }

    public static synchronized DataPersistenceController getInstance(Context context) {
        if (instance == null) {
            instance = new DataPersistenceController(context.getApplicationContext());
        }
        return instance;
    }

    public void addTripLog(TripLog log, Runnable callback) {
        executorService.execute(() -> {
            tripDao.insert(log);
            if (callback != null) {
                callback.run();
            }
        });
    }

    public void getAllTripLogs(DataCallback<List<TripLog>> callback) {
        executorService.execute(() -> {
            List<TripLog> logs = tripDao.getAllLogs();
            if (callback != null) {
                callback.onDataLoaded(logs);
            }
        });
    }

    public interface DataCallback<T> {
        void onDataLoaded(T data);
    }
}
