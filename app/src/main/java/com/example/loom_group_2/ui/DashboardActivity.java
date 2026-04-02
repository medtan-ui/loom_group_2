package com.example.loom_group_2.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.loom_group_2.R;
import com.example.loom_group_2.data.FirebaseUtil;
import com.example.loom_group_2.data.Motorcycle;
import com.example.loom_group_2.data.TripLog;
import com.example.loom_group_2.logic.DataPersistenceController;
import com.example.loom_group_2.logic.MockRouteService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private TextView tvGreeting, tvFuelEfficiency, tvTimeEst, tvTripDuration;
    private ProgressBar progressFuel, progressTime;
    private FirebaseAuth mAuth;
    private RecyclerView rvLogs;
    private LogAdapter logAdapter;
    private List<TripLog> tripLogs = new ArrayList<>();
    private DataPersistenceController dataController;
    private LinearLayout searchBar;
    private ShapeableImageView ivProfile;
    private TextView btnViewAllLogs;
    private Motorcycle currentVehicle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        
        mAuth = FirebaseAuth.getInstance();
        dataController = DataPersistenceController.getInstance(this);
        
        tvGreeting = findViewById(R.id.tvGreeting);
        tvFuelEfficiency = findViewById(R.id.tvFuelEfficiency);
        tvTimeEst = findViewById(R.id.tvTimeEst);
        tvTripDuration = findViewById(R.id.tvTripDuration);
        rvLogs = findViewById(R.id.rvLogs);
        searchBar = findViewById(R.id.searchBar);
        progressFuel = findViewById(R.id.progressFuel);
        progressTime = findViewById(R.id.progressTime);
        ivProfile = findViewById(R.id.ivProfile);
        btnViewAllLogs = findViewById(R.id.btnViewAllLogs);

        setupRecyclerView();
        updateUserInfo();
        loadRecentLogs();
        loadUserVehicle();
        
        searchBar.setOnClickListener(v -> simulateNewRoute());
        ivProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        btnViewAllLogs.setOnClickListener(v -> startActivity(new Intent(this, LogsActivity.class)));

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) mapFragment.getMapAsync(this);
    }

    private void loadUserVehicle() {
        FirebaseUtil.getUserVehicle(motorcycle -> {
            if (motorcycle != null) {
                this.currentVehicle = motorcycle;
                tvFuelEfficiency.setText(String.format(Locale.US, "%.1f", motorcycle.getKpl()));
            }
        });
    }

    private void setupRecyclerView() {
        rvLogs.setLayoutManager(new LinearLayoutManager(this));
        logAdapter = new LogAdapter(tripLogs);
        rvLogs.setAdapter(logAdapter);
    }

    private void updateUserInfo() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String name = user.getDisplayName();
<<<<<<< HEAD
            String firstName = "User";

            if (name != null && !name.trim().isEmpty()) {
                firstName = name.trim().split("\\s+")[0];
            }

            tvGreeting.setText("Hello, " + firstName + "!");

=======
            tvGreeting.setText("Hello, " + (name != null ? name : "User") + "!");
            
>>>>>>> dbcd34ef036bb085e8ad3e3ec00a4d122fd3c4d9
            if (user.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(user.getPhotoUrl())
                        .placeholder(R.drawable.ic_launcher_background)
                        .into(ivProfile);
            } else {
                ivProfile.setImageResource(R.drawable.ic_launcher_background);
            }
        }
    }

    private void simulateNewRoute() {
        if (currentVehicle == null) {
            Toast.makeText(this, "Please select a vehicle in Profile first!", Toast.LENGTH_LONG).show();
            return;
        }

        Toast.makeText(this, "Calculating route...", Toast.LENGTH_SHORT).show();
        MockRouteService.calculateRoute("A", "B", (distanceKm, durationMins, fuelEstimateLiters) -> {
            runOnUiThread(() -> {
                // Use actual vehicle data for more accurate dummy calculation
                double realFuelNeeded = distanceKm / currentVehicle.getKpl();
                
                tvFuelEfficiency.setText(String.format(Locale.US, "%.1f", currentVehicle.getKpl()));
                tvTimeEst.setText(String.valueOf(durationMins));
                tvTripDuration.setText(durationMins + " min");
                
                progressFuel.setProgress((int) Math.min(100, (realFuelNeeded * 10))); 
                progressTime.setProgress(durationMins > 100 ? 100 : durationMins);
                
                String date = java.text.DateFormat.getDateInstance().format(new java.util.Date());
                TripLog newLog = new TripLog(date, "Trip with " + currentVehicle.getModel(), 
                        durationMins + " mins", String.format(Locale.US, "%.2f L", realFuelNeeded));
                dataController.addTripLog(newLog, this::loadRecentLogs);
            });
        });
    }

    private void loadRecentLogs() {
        dataController.getAllTripLogs(logs -> runOnUiThread(() -> {
            tripLogs.clear();
            if (logs.size() > 3) {
                tripLogs.addAll(logs.subList(0, 3));
            } else {
                tripLogs.addAll(logs);
            }
            logAdapter.notifyDataSetChanged();
        }));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(14.6253, 121.0619), 15f));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserVehicle(); // Refresh if they changed vehicle in Profile
        updateUserInfo();  // Refresh profile picture if changed
    }
}
