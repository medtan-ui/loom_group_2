package com.example.loom_group_2.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.loom_group_2.R;
import com.example.loom_group_2.data.TripLog;
import com.example.loom_group_2.logic.DataPersistenceController;
import com.example.loom_group_2.logic.NavigationManager;
import com.example.loom_group_2.logic.PolylineDecoder;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;

public class NavigationActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private TextView tvDestName, tvNavTime, tvNavDistance, tvNavFuel;
    private Button btnEndNav, btnStartTravel, btnFinishNav;
    private ImageButton btnBackToDash;
    private FloatingActionButton fabRecenter;
    private CardView cardDirectionBanner;
    private ProgressBar pbStartTimer;
    private FrameLayout startBtnContainer;
    
    private RouteModel activeRoute;
    private String destinationName;
    private DataPersistenceController dataController;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    
    private boolean followUser = true;
    private CountDownTimer startTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        dataController = DataPersistenceController.getInstance(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        
        activeRoute = NavigationManager.getInstance().getActiveRoute();
        destinationName = NavigationManager.getInstance().getDestinationName();

        if (activeRoute == null) {
            finish();
            return;
        }

        tvDestName = findViewById(R.id.tvDestName);
        tvNavTime = findViewById(R.id.tvNavTime);
        tvNavDistance = findViewById(R.id.tvNavDistance);
        tvNavFuel = findViewById(R.id.tvNavFuel);
        
        btnEndNav = findViewById(R.id.btnEndNav);
        btnStartTravel = findViewById(R.id.btnStartTravel);
        btnFinishNav = findViewById(R.id.btnFinishNav);
        btnBackToDash = findViewById(R.id.btnBackToDash);
        fabRecenter = findViewById(R.id.fabRecenter);
        cardDirectionBanner = findViewById(R.id.cardDirectionBanner);
        pbStartTimer = findViewById(R.id.pbStartTimer);
        startBtnContainer = findViewById(R.id.startBtnContainer);

        tvDestName.setText(destinationName);
        tvNavTime.setText(activeRoute.getDurationText());
        tvNavDistance.setText(activeRoute.getDistanceText());
        tvNavFuel.setText(activeRoute.getFuelText());

        btnEndNav.setOnClickListener(v -> {
            cancelTimer();
            NavigationManager.getInstance().stopNavigation();
            Toast.makeText(this, "Navigation Cancelled", Toast.LENGTH_SHORT).show();
            finish();
        });

        btnStartTravel.setOnClickListener(v -> {
            cancelTimer();
            startTraveling();
        });
        
        btnFinishNav.setOnClickListener(v -> saveTripAndFinish());

        btnBackToDash.setOnClickListener(v -> {
            finish();
        });
        
        fabRecenter.setOnClickListener(v -> {
            followUser = true;
            fabRecenter.setVisibility(View.GONE);
            moveCameraToUser();
        });

        setupLocationCallback();

        // Resume state check
        if (NavigationManager.getInstance().isTraveling()) {
            applyTravelingUI();
            startLocationUpdates();
        } else {
            startAutoStartTimer();
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.navMapFragment);
        if (mapFragment != null) mapFragment.getMapAsync(this);
    }

    private void startAutoStartTimer() {
        long startTime = NavigationManager.getInstance().getNavigationStartTimeMillis();
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - startTime;
        long remaining = 5000 - elapsed;

        if (remaining <= 0) {
            startTraveling();
            return;
        }

        pbStartTimer.setVisibility(View.VISIBLE);
        pbStartTimer.setMax(5000);

        if (startTimer != null) startTimer.cancel();

        startTimer = new CountDownTimer(remaining, 50) {
            public void onTick(long millisUntilFinished) {
                long totalElapsed = 5000 - millisUntilFinished;
                pbStartTimer.setProgress((int) totalElapsed);
                btnStartTravel.setText("Start (" + (millisUntilFinished / 1000 + 1) + "s)");
            }
            public void onFinish() {
                pbStartTimer.setProgress(5000);
                startTraveling();
            }
        }.start();
    }

    private void cancelTimer() {
        if (startTimer != null) {
            startTimer.cancel();
            startTimer = null;
        }
        pbStartTimer.setVisibility(View.GONE);
        btnStartTravel.setText("Start Travel");
        // Ensure the button is visible and background is set if we want it to look normal after cancel
        btnStartTravel.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.yellow));
    }

    private void startTraveling() {
        NavigationManager.getInstance().setTraveling(true);
        applyTravelingUI();
        moveCameraToUser();
        startLocationUpdates();
    }

    private void applyTravelingUI() {
        startBtnContainer.setVisibility(View.GONE);
        btnFinishNav.setVisibility(View.VISIBLE);
        cardDirectionBanner.setVisibility(View.VISIBLE);
        followUser = true;
    }

    private void moveCameraToUser() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return;
        
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null && mMap != null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(location.getLatitude(), location.getLongitude()), 18f));
            }
        });
    }

    private void setupLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (!NavigationManager.getInstance().isTraveling() || !followUser || mMap == null) return;
                
                for (android.location.Location location : locationResult.getLocations()) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(location.getLatitude(), location.getLongitude()), 18f));
                }
            }
        };
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create()
                .setInterval(2000)
                .setFastestInterval(1000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    private void saveTripAndFinish() {
        String date = java.text.DateFormat.getDateInstance().format(new java.util.Date());
        TripLog newLog = new TripLog(date, "To: " + destinationName, 
                activeRoute.getDurationText(), activeRoute.getFuelText(), activeRoute.getDistanceText());
        
        dataController.addTripLog(newLog, () -> {
            runOnUiThread(() -> {
                NavigationManager.getInstance().stopNavigation();
                Toast.makeText(this, "Trip Logged Successfully!", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
        
        mMap.setOnCameraMoveStartedListener(reason -> {
            if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE && NavigationManager.getInstance().isTraveling()) {
                followUser = false;
                fabRecenter.setVisibility(View.VISIBLE);
            }
        });

        drawRouteOnMap();
    }

    private void drawRouteOnMap() {
        if (mMap == null || activeRoute.getPolylinePoints() == null) return;

        List<LatLng> points = PolylineDecoder.decodePoly(activeRoute.getPolylinePoints());
        if (points.isEmpty()) return;

        int colorPrimary = ContextCompat.getColor(this, R.color.route_primary);
        mMap.addPolyline(new PolylineOptions()
                .addAll(points)
                .color(colorPrimary)
                .width(14));

        if (!NavigationManager.getInstance().isTraveling()) {
            LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
            for (LatLng point : points) boundsBuilder.include(point);
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 150));
        } else {
            moveCameraToUser();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (startTimer != null) {
            startTimer.cancel();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!NavigationManager.getInstance().isTraveling() && NavigationManager.getInstance().isNavigating()) {
            startAutoStartTimer();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
}
