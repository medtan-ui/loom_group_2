package com.example.loom_group_2.data;

import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseUtil {
    private static final String TAG = "FirebaseUtil";
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface DataCallback<T> {
        void onCallback(T data);
    }

    public static void getMakes(DataCallback<List<String>> callback) {
        db.collection("makes").get().addOnCompleteListener(task -> {
            List<String> makes = new ArrayList<>();
            if (task.isSuccessful() && task.getResult() != null) {
                for (DocumentSnapshot doc : task.getResult()) {
                    makes.add(doc.getId());
                }
                Log.d(TAG, "Fetched " + makes.size() + " makes");
            } else {
                Log.e(TAG, "Error fetching makes", task.getException());
            }
            callback.onCallback(makes);
        });
    }

    public static void getModels(String make, DataCallback<List<String>> callback) {
        db.collection("makes").document(make).collection("models").get().addOnCompleteListener(task -> {
            List<String> models = new ArrayList<>();
            if (task.isSuccessful() && task.getResult() != null) {
                for (DocumentSnapshot doc : task.getResult()) {
                    models.add(doc.getId());
                }
                Log.d(TAG, "Fetched " + models.size() + " models for " + make);
            } else {
                Log.e(TAG, "Error fetching models", task.getException());
            }
            callback.onCallback(models);
        });
    }

    public static void getYears(String make, String model, DataCallback<List<String>> callback) {
        db.collection("makes").document(make).collection("models").document(model)
                .collection("years").get().addOnCompleteListener(task -> {
                    List<String> yearIds = new ArrayList<>();
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (DocumentSnapshot doc : task.getResult()) {
                            // Returns the yearId (e.g., "2023_Manual")
                            yearIds.add(doc.getId());
                        }
                    } else {
                        Log.e(TAG, "Error fetching years", task.getException());
                    }
                    callback.onCallback(yearIds);
                });
    }

    public static void getMotorcycleDetails(String make, String model, String yearId, DataCallback<Motorcycle> callback) {
        db.collection("makes").document(make).collection("models").document(model)
                .collection("years").document(yearId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                Motorcycle motorcycle = task.getResult().toObject(Motorcycle.class);
                callback.onCallback(motorcycle);
            } else {
                Log.e(TAG, "Details not found for yearId: " + yearId);
                callback.onCallback(null);
            }
        });
    }

    public static void saveUserVehicle(Motorcycle motorcycle) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null && motorcycle != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("currentVehicle", motorcycle);
            db.collection("users").document(uid).set(data, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d(TAG, "User vehicle saved successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to save user vehicle", e));
        }
    }

    public static void getUserVehicle(DataCallback<Motorcycle> callback) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            db.collection("users").document(uid).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                    Motorcycle motorcycle = task.getResult().get("currentVehicle", Motorcycle.class);
                    callback.onCallback(motorcycle);
                } else {
                    callback.onCallback(null);
                }
            });
        } else {
            callback.onCallback(null);
        }
    }
}
