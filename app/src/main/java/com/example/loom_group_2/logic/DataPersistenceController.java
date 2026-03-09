package com.example.loom_group_2.logic;

import android.content.Context;
import com.example.loom_group_2.data.AppDatabase;
import com.example.loom_group_2.data.TripDao;
import com.example.loom_group_2.data.TripLog;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DataPersistenceController {
    private TripDao tripDao;
    private ExecutorService executorService;

    public DataPersistenceController(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.tripDao = db.tripDao();
        this.executorService = Executors.newSingleThreadExecutor();
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
