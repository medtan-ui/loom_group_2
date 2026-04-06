package com.example.loom_group_2.logic;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ActiveVehiclePrefs {
    private static final String PREF_NAME = "loom_active_vehicle";
    private static final String KEY_USER_UID = "user_uid";
    private static final String KEY_SOURCE = "source";
    private static final String KEY_VEHICLE_ID = "vehicle_id";
    
    private final SharedPreferences prefs;

    public ActiveVehiclePrefs(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        checkUserConsistency();
    }

    private void checkUserConsistency() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String currentUid = (user != null) ? user.getUid() : null;
        String storedUid = prefs.getString(KEY_USER_UID, null);

        if (currentUid == null || (storedUid != null && !storedUid.equals(currentUid))) {
            clear();
        }
    }

    public void setActiveRoomVehicle(int id) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        
        prefs.edit()
                .putString(KEY_USER_UID, user.getUid())
                .putString(KEY_SOURCE, "room")
                .putInt(KEY_VEHICLE_ID, id)
                .apply();
    }

    public void setActiveFirestoreVehicle() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        prefs.edit()
                .putString(KEY_USER_UID, user.getUid())
                .putString(KEY_SOURCE, "firestore")
                .remove(KEY_VEHICLE_ID)
                .apply();
    }

    public String getActiveSource() {
        checkUserConsistency();
        return prefs.getString(KEY_SOURCE, null);
    }

    public int getActiveVehicleId() {
        return prefs.getInt(KEY_VEHICLE_ID, -1);
    }

    public boolean hasActiveVehicle() {
        checkUserConsistency();
        return prefs.contains(KEY_SOURCE);
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}
