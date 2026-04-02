package com.example.loom_group_2.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.loom_group_2.R;
import com.example.loom_group_2.data.FirebaseUtil;
import com.example.loom_group_2.data.Motorcycle;
import com.example.loom_group_2.data.TripLog;
import com.example.loom_group_2.logic.DataPersistenceController;
import com.example.loom_group_2.logic.GoogleMapsService;
import com.example.loom_group_2.logic.PolylineDecoder;
import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity implements OnMapReadyCallback, RoutesFragment.OnRouteSelectedListener {
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
    private FusedLocationProviderClient fusedLocationClient;
    private FrameLayout fragmentContainer;
    private List<Polyline> polylines = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        
        mAuth = FirebaseAuth.getInstance();
        dataController = DataPersistenceController.getInstance(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        
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
        fragmentContainer = findViewById(R.id.fragmentContainer);

        setupRecyclerView();
        updateUserInfo();
        loadRecentLogs();
        loadUserVehicle();
        
        searchBar.setOnClickListener(v -> promptForDestination());
        ivProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        btnViewAllLogs.setOnClickListener(v -> startActivity(new Intent(this, LogsActivity.class)));

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) mapFragment.getMapAsync(this);
    }

    private void promptForDestination() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                String origin = location.getLatitude() + "," + location.getLongitude();
                showDestinationInputDialog(origin);
            } else {
                Toast.makeText(this, "Could not get current location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDestinationInputDialog(String origin) {
        EditText input = new EditText(this);
        input.setHint("Enter destination");
        new AlertDialog.Builder(this)
                .setTitle("Plan Route")
                .setView(input)
                .setPositiveButton("Search", (dialog, which) -> {
                    String destination = input.getText().toString();
                    if (!destination.isEmpty()) {
                        showRoutesFragment(origin, destination);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showRoutesFragment(String origin, String destination) {
        fragmentContainer.setVisibility(View.VISIBLE);
        RoutesFragment fragment = new RoutesFragment();
        Bundle args = new Bundle();
        args.putString("origin", origin);
        args.putString("destination", destination);
        fragment.setArguments(args);
        fragment.setOnRouteSelectedListener(this);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onRoutesFetched(GoogleMapsService.DirectionsResponse response, Motorcycle vehicle) {
        if (mMap == null) return;
        
        for (Polyline p : polylines) p.remove();
        polylines.clear();

        if (response.routes == null || response.routes.isEmpty()) return;

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (int i = 0; i < response.routes.size(); i++) {
            GoogleMapsService.DirectionsResponse.Route route = response.routes.get(i);
            List<LatLng> decodedPath = PolylineDecoder.decodePoly(route.overviewPolyline.points);
            
            PolylineOptions polyOptions = new PolylineOptions()
                    .addAll(decodedPath)
                    .color(i == 0 ? Color.BLUE : Color.GRAY)
                    .width(10);
            polylines.add(mMap.addPolyline(polyOptions));
            
            for (LatLng point : decodedPath) boundsBuilder.include(point);
        }

        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100));
        
        // Update stats with the primary route
        if (!response.routes.isEmpty()) {
            GoogleMapsService.DirectionsResponse.Leg leg = response.routes.get(0).legs.get(0);
            updateTripStats(leg.distance.value / 1000.0, leg.duration.value / 60);
        }
    }

    @Override
    public void onRouteClicked(int position) {

    }

    private void updateTripStats(double distanceKm, int durationMins) {
        if (currentVehicle != null) {
            double fuelNeeded = distanceKm / currentVehicle.getKpl();
            tvTimeEst.setText(String.valueOf(durationMins));
            tvTripDuration.setText(durationMins + " min");
            tvFuelEfficiency.setText(String.format(Locale.US, "%.1f", currentVehicle.getKpl()));
            
            progressFuel.setProgress((int) Math.min(100, (fuelNeeded * 10))); 
            progressTime.setProgress(Math.min(100, durationMins));
        }
    }

    @Override
    public void onStartNavigation(RouteModel selectedRoute) {
        fragmentContainer.setVisibility(View.GONE);
        getSupportFragmentManager().popBackStack();
        Toast.makeText(this, "Starting navigation to " + selectedRoute.getName(), Toast.LENGTH_SHORT).show();
        
        // Log the trip
        String date = java.text.DateFormat.getDateInstance().format(new java.util.Date());
        TripLog newLog = new TripLog(date, "Route: " + selectedRoute.getName(), 
                selectedRoute.getDurationText(), selectedRoute.getFuelText());
        dataController.addTripLog(newLog, this::loadRecentLogs);
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
            if (name != null && !name.isEmpty()) {
                String firstName = name.split(" ")[0];
                tvGreeting.setText("Hello, " + firstName + "!");
            }
            if (user.getPhotoUrl() != null) {
                Glide.with(this).load(user.getPhotoUrl()).into(ivProfile);
            }
        }
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(14.6253, 121.0619), 15f));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserVehicle();
    }

    @Override
    public void onBackPressed() {
        if (fragmentContainer.getVisibility() == View.VISIBLE) {
            fragmentContainer.setVisibility(View.GONE);
            super.onBackPressed();
        } else {
            super.onBackPressed();
        }
    }
}
