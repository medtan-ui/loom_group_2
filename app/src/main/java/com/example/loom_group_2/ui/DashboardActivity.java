package com.example.loom_group_2.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.loom_group_2.R;
import com.example.loom_group_2.data.FirebaseUtil;
import com.example.loom_group_2.data.Motorcycle;
import com.example.loom_group_2.data.TripLog;
import com.example.loom_group_2.logic.ActiveVehiclePrefs;
import com.example.loom_group_2.logic.DataPersistenceController;
import com.example.loom_group_2.logic.GoogleMapsService;
import com.example.loom_group_2.logic.NavigationManager;
import com.example.loom_group_2.logic.VehicleRepository;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity implements RoutesFragment.OnRouteSelectedListener {
    private TextView tvGreeting, tvFuelEfficiency, tvFuelUnit, tvTimeEst;
    private ProgressBar progressFuel, progressTime;
    private FirebaseAuth mAuth;
    private RecyclerView rvLogs;
    private LogAdapter logAdapter;
    private List<TripLog> tripLogs = new ArrayList<>();
    private DataPersistenceController dataController;
    private LinearLayout searchBar;
    private ShapeableImageView ivProfile;
    private TextView btnViewAllLogs;
    private FloatingActionButton fabAI;
    private Motorcycle currentVehicle;
    private FusedLocationProviderClient fusedLocationClient;
    private FrameLayout fragmentContainer;
    private ActiveVehiclePrefs vehiclePrefs;
    private VehicleRepository vehicleRepository;

    // Navigation Notification
    private CardView cardNavPopup;
    private TextView tvNavNextTurn, tvNavDestination;
    private ImageButton btnCancelNav;

    // Next Trip Planner Views
    private View layoutPlannedTrip;
    private TextView tvNoTripPlanned, btnPlanTrip;
    private ImageButton btnStartTrip, btnDeleteTrip;
    private TextView tvPlannedDestName, tvPlannedTripStats;

    // Vehicle Dashboard Card
    private CardView cardVehicleProfile;
    private TextView tvVehicleDashboardName, tvVehicleDashboardDetail, tvVehicleDashboardKpl, tvVehicleDashboardTrans;
    private ImageView ivVehicleIcon;

    private final ActivityResultLauncher<Intent> destinationLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                updateNavigationUI();
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        
        mAuth = FirebaseAuth.getInstance();
        dataController = DataPersistenceController.getInstance(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        vehiclePrefs = new ActiveVehiclePrefs(this);
        vehicleRepository = VehicleRepository.getInstance(this);
        
        tvGreeting = findViewById(R.id.tvGreeting);
        tvFuelEfficiency = findViewById(R.id.tvFuelEfficiency);
        tvFuelUnit = findViewById(R.id.tvFuelUnit);
        tvTimeEst = findViewById(R.id.tvTimeEst);
        rvLogs = findViewById(R.id.rvLogs);
        searchBar = findViewById(R.id.searchBar);
        progressFuel = findViewById(R.id.progressFuel);
        progressTime = findViewById(R.id.progressTime);
        ivProfile = findViewById(R.id.ivProfile);
        btnViewAllLogs = findViewById(R.id.btnViewAllLogs);
        fabAI = findViewById(R.id.fabAI);
        fragmentContainer = findViewById(R.id.fragmentContainer);

        // Navigation Notification
        cardNavPopup = findViewById(R.id.cardNavPopup);
        tvNavNextTurn = findViewById(R.id.tvNavNextTurn);
        tvNavDestination = findViewById(R.id.tvNavDestination);
        btnCancelNav = findViewById(R.id.btnCancelNav);

        // Next Trip Planner
        tvNoTripPlanned = findViewById(R.id.tvNoTripPlanned);
        btnPlanTrip = findViewById(R.id.btnPlanTrip);
        layoutPlannedTrip = findViewById(R.id.layoutPlannedTrip);
        btnStartTrip = findViewById(R.id.btnStartTrip);
        btnDeleteTrip = findViewById(R.id.btnDeleteTrip);
        tvPlannedDestName = findViewById(R.id.tvPlannedDestName);
        tvPlannedTripStats = findViewById(R.id.tvPlannedTripStats);

        // Vehicle Card
        cardVehicleProfile = findViewById(R.id.cardVehicleProfile);
        tvVehicleDashboardName = findViewById(R.id.tvVehicleDashboardName);
        tvVehicleDashboardDetail = findViewById(R.id.tvVehicleDashboardDetail);
        tvVehicleDashboardKpl = findViewById(R.id.tvVehicleDashboardKpl);
        tvVehicleDashboardTrans = findViewById(R.id.tvVehicleDashboardTrans);
        ivVehicleIcon = findViewById(R.id.ivVehicleIcon);

        setupRecyclerView();
        updateUserInfo();
        loadRecentLogs();
        loadUserVehicle();
        
        searchBar.setOnClickListener(v -> launchDestinationPicker("NAVIGATE"));
        btnPlanTrip.setOnClickListener(v -> launchDestinationPicker("PLAN"));

        btnStartTrip.setOnClickListener(v -> {
            NavigationManager.getInstance().startNavigationFromPlan();
            startActivity(new Intent(this, NavigationActivity.class));
            updateNavigationUI();
        });

        btnDeleteTrip.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Trip Plan")
                    .setMessage("Are you sure you want to delete your planned trip?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        NavigationManager.getInstance().stopPlan();
                        updateNavigationUI();
                        Toast.makeText(this, "Trip Plan Deleted", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        ivProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        cardVehicleProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        btnViewAllLogs.setOnClickListener(v -> startActivity(new Intent(this, LogsActivity.class)));
        fabAI.setOnClickListener(v -> startActivity(new Intent(this, AIChatActivity.class)));

        cardNavPopup.setOnClickListener(v -> {
            startActivity(new Intent(this, NavigationActivity.class));
        });

        btnCancelNav.setOnClickListener(v -> {
            NavigationManager.getInstance().stopNavigation();
            updateNavigationUI();
            Toast.makeText(this, "Navigation Cancelled", Toast.LENGTH_SHORT).show();
        });
    }

    private void launchDestinationPicker(String mode) {
        if (currentVehicle == null) {
            new AlertDialog.Builder(this)
                    .setTitle("Vehicle Required")
                    .setMessage("You must choose a vehicle in your profile first before you can plan a trip.")
                    .setPositiveButton("Go to Profile", (dialog, which) -> {
                        startActivity(new Intent(this, ProfileActivity.class));
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            String originStr = "";
            if (location != null) {
                originStr = location.getLatitude() + "," + location.getLongitude();
            }
            Intent intent = new Intent(this, DestinationActivity.class);
            intent.putExtra("origin", originStr);
            intent.putExtra("mode", mode);
            destinationLauncher.launch(intent);
        });
    }

    private void updateNavigationUI() {
        NavigationManager navManager = NavigationManager.getInstance();
        boolean isNavigating = navManager.isNavigating();
        boolean isPlanned = navManager.isPlanned();

        // 1. Navigation Notification
        if (isNavigating) {
            cardNavPopup.setVisibility(View.VISIBLE);
            tvNavDestination.setText("To: " + navManager.getActiveDestName());
            tvNavNextTurn.setText("Navigating...");
        } else {
            cardNavPopup.setVisibility(View.GONE);
        }

        // 2. Next Trip Planner Card
        if (isPlanned) {
            tvNoTripPlanned.setVisibility(View.GONE);
            layoutPlannedTrip.setVisibility(View.VISIBLE);
            tvPlannedDestName.setText(navManager.getPlannedDestName());

            RouteModel route = navManager.getPlannedRoute();
            if (route != null) {
                tvPlannedTripStats.setText(route.getDistanceText() + " • " + route.getDurationText());
            }
        } else {
            tvNoTripPlanned.setVisibility(View.VISIBLE);
            layoutPlannedTrip.setVisibility(View.GONE);
        }

        // 3. Stats respond to what's currently active (Navigating priority, then Planned)
        RouteModel activeDisplayRoute = isNavigating ? navManager.getActiveRoute() : (isPlanned ? navManager.getPlannedRoute() : null);
        
        if (activeDisplayRoute != null) {
            tvFuelEfficiency.setText(activeDisplayRoute.getFuelText().replace(" L", ""));
            tvFuelUnit.setText(getString(R.string.fuel_unit_liters));
            tvTimeEst.setText(activeDisplayRoute.getDurationText().replace(" mins", ""));
            progressFuel.setProgress(50);
            progressTime.setProgress(50);
        } else {
            tvFuelEfficiency.setText(getString(R.string.dash_placeholder));
            tvTimeEst.setText(getString(R.string.dash_placeholder));
            progressFuel.setProgress(0);
            progressTime.setProgress(0);
        }
    }

    @Override
    public void onRoutesFetched(GoogleMapsService.DirectionsResponse response, Motorcycle vehicle) { }

    @Override
    public void onRouteClicked(int position) { }

    @Override
    public void onStartNavigation(RouteModel selectedRoute, String destinationName, String destinationCoord, List<String> waypoints) { }

    private void loadUserVehicle() {
        if (!vehiclePrefs.hasActiveVehicle()) {
            this.currentVehicle = null;
            updateVehicleDashboardUI(null);
            return;
        }

        String source = vehiclePrefs.getActiveSource();
        if ("room".equals(source)) {
            int id = vehiclePrefs.getActiveVehicleId();
            vehicleRepository.getVehicleById(id, vehicle -> {
                runOnUiThread(() -> {
                    this.currentVehicle = vehicle;
                    updateVehicleDashboardUI(vehicle);
                });
            });
        } else if ("firestore".equals(source)) {
            FirebaseUtil.getUserVehicle(motorcycle -> {
                runOnUiThread(() -> {
                    this.currentVehicle = motorcycle;
                    updateVehicleDashboardUI(motorcycle);
                });
            });
        }
    }

    private void updateVehicleDashboardUI(Motorcycle vehicle) {
        if (vehicle != null) {
            tvVehicleDashboardName.setText(String.format(Locale.US, "%s (%d)", vehicle.getModel(), vehicle.getYear()));
            tvVehicleDashboardDetail.setText(vehicle.getMake());
            tvVehicleDashboardKpl.setText(String.format(Locale.US, "%.1f km/L", vehicle.getKpl()));
            tvVehicleDashboardTrans.setText(vehicle.getTransmission() != null ? vehicle.getTransmission() : "Manual");
            ivVehicleIcon.setImageResource(R.drawable.ic_car);
        } else {
            tvVehicleDashboardName.setText("No Vehicle Selected");
            tvVehicleDashboardDetail.setText("Tap to select in profile");
            tvVehicleDashboardKpl.setText("-- km/L");
            tvVehicleDashboardTrans.setText("--");
            ivVehicleIcon.setImageResource(R.drawable.ic_road);
        }
    }

    private void setupRecyclerView() {
        rvLogs.setLayoutManager(new LinearLayoutManager(this));
        logAdapter = new LogAdapter(tripLogs);
        logAdapter.setOnLogDeleteListener((log, position) -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Log")
                    .setMessage("Are you sure you want to delete this trip log?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        dataController.deleteTripLog(log, () -> {
                            runOnUiThread(() -> {
                                tripLogs.remove(position);
                                logAdapter.notifyItemRemoved(position);
                                logAdapter.notifyItemRangeChanged(position, tripLogs.size());
                                Toast.makeText(this, "Log Deleted", Toast.LENGTH_SHORT).show();
                            });
                        });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
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
    protected void onResume() {
        super.onResume();
        loadUserVehicle();
        updateNavigationUI();
        loadRecentLogs();
    }

    @Override
    public void onBackPressed() {
        if (fragmentContainer.getVisibility() == View.VISIBLE) {
            fragmentContainer.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }
}
