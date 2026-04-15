package com.example.loom_group_2.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.loom_group_2.R;
import com.example.loom_group_2.data.FirebaseUtil;
import com.example.loom_group_2.data.Motorcycle;
import com.example.loom_group_2.logic.ActiveVehiclePrefs;
import com.example.loom_group_2.logic.GoogleMapsService;
import com.example.loom_group_2.logic.NavigationManager;
import com.example.loom_group_2.logic.PolylineDecoder;
import com.example.loom_group_2.logic.VehicleRepository;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DestinationActivity extends AppCompatActivity implements OnMapReadyCallback, RoutesFragment.OnRouteSelectedListener {

    private EditText etSearch;
    private RecyclerView rvResults;
    private ProgressBar pbLoading;
    private PlaceAdapter adapter;
    private List<GoogleMapsService.PlaceSearchResponse.PlaceResult> resultsList = new ArrayList<>();
    private GoogleMap mMap;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private String origin;
    private String mode; // "PLAN" or "NAVIGATE"
    
    private LinearLayout searchResultLayout;
    private FrameLayout routeFragmentContainer;
    private List<Polyline> polylines = new ArrayList<>();
    private String selectedPlaceName;
    private String selectedPlaceCoordinate;

    // Vehicle Info Views
    private CardView vehicleInfoCard;
    private TextView tvVehicleName, tvVehicleKpl;
    private ActiveVehiclePrefs vehiclePrefs;
    private VehicleRepository vehicleRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destination);

        origin = getIntent().getStringExtra("origin");
        mode = getIntent().getStringExtra("mode");
        if (mode == null) mode = "PLAN";

        etSearch = findViewById(R.id.etSearch);
        rvResults = findViewById(R.id.rvPlaceResults);
        pbLoading = findViewById(R.id.pbLoading);
        searchResultLayout = findViewById(R.id.searchResultLayout);
        routeFragmentContainer = findViewById(R.id.routeFragmentContainer);
        
        vehicleInfoCard = findViewById(R.id.vehicleInfoCard);
        tvVehicleName = findViewById(R.id.tvVehicleName);
        tvVehicleKpl = findViewById(R.id.tvVehicleKpl);
        
        vehiclePrefs = new ActiveVehiclePrefs(this);
        vehicleRepository = VehicleRepository.getInstance(this);
        
        ImageButton btnBack = findViewById(R.id.btnBack);
        View bottomSheet = findViewById(R.id.bottomSheetResults);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        rvResults.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PlaceAdapter(resultsList, place -> {
            selectedPlaceName = place.name;
            selectedPlaceCoordinate = place.geometry.location.lat + "," + place.geometry.location.lng;
            showRoutes(selectedPlaceCoordinate, selectedPlaceName);
        });
        rvResults.setAdapter(adapter);

        btnBack.setOnClickListener(v -> {
            if (routeFragmentContainer.getVisibility() == View.VISIBLE) {
                showSuggestions();
            } else {
                finish();
            }
        });

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(etSearch.getText().toString());
                return true;
            }
            return false;
        });

        loadActiveVehicle();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) mapFragment.getMapAsync(this);
    }

    private void loadActiveVehicle() {
        if (!vehiclePrefs.hasActiveVehicle()) return;

        String source = vehiclePrefs.getActiveSource();
        if ("room".equals(source)) {
            vehicleRepository.getVehicleById(vehiclePrefs.getActiveVehicleId(), vehicle -> {
                runOnUiThread(() -> updateVehicleUI(vehicle));
            });
        } else {
            FirebaseUtil.getUserVehicle(this::updateVehicleUI);
        }
    }

    private void updateVehicleUI(Motorcycle vehicle) {
        if (vehicle != null) {
            tvVehicleName.setText(vehicle.getModel());
            tvVehicleKpl.setText(String.format(Locale.US, "%.1f km/L", vehicle.getKpl()));
        }
    }

    private void performSearch(String query) {
        if (query.isEmpty()) return;

        showSuggestions();
        pbLoading.setVisibility(View.VISIBLE);
        String apiKey = getString(R.string.google_maps_key);
        GoogleMapsService.getInstance().searchPlaces(query, apiKey, new Callback<GoogleMapsService.PlaceSearchResponse>() {
            @Override
            public void onResponse(@NonNull Call<GoogleMapsService.PlaceSearchResponse> call, @NonNull Response<GoogleMapsService.PlaceSearchResponse> response) {
                pbLoading.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().results != null) {
                    resultsList.clear();
                    resultsList.addAll(response.body().results);
                    adapter.notifyDataSetChanged();
                    
                    if (resultsList.isEmpty()) {
                        Toast.makeText(DestinationActivity.this, "No results found", Toast.LENGTH_SHORT).show();
                    } else {
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        updateMapMarkers();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<GoogleMapsService.PlaceSearchResponse> call, @NonNull Throwable t) {
                pbLoading.setVisibility(View.GONE);
                Toast.makeText(DestinationActivity.this, "Search failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateMapMarkers() {
        if (mMap == null || resultsList.isEmpty()) return;
        mMap.clear();
        for (Polyline p : polylines) p.remove();
        polylines.clear();

        for (GoogleMapsService.PlaceSearchResponse.PlaceResult result : resultsList) {
            LatLng pos = new LatLng(result.geometry.location.lat, result.geometry.location.lng);
            mMap.addMarker(new MarkerOptions().position(pos).title(result.name));
        }
        
        GoogleMapsService.PlaceSearchResponse.PlaceResult first = resultsList.get(0);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(first.geometry.location.lat, first.geometry.location.lng), 12f));
    }

    private void showRoutes(String destination, String destName) {
        searchResultLayout.setVisibility(View.GONE);
        routeFragmentContainer.setVisibility(View.VISIBLE);
        vehicleInfoCard.setVisibility(View.GONE); // Hide when viewing routes
        
        RoutesFragment fragment = new RoutesFragment();
        Bundle args = new Bundle();
        args.putString("origin", origin);
        args.putString("destination", destination);
        args.putString("destination_name", destName);
        args.putString("mode", mode);
        fragment.setArguments(args);
        fragment.setOnRouteSelectedListener(this);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.routeFragmentContainer, fragment);
        transaction.commit();
        
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private void showSuggestions() {
        searchResultLayout.setVisibility(View.VISIBLE);
        routeFragmentContainer.setVisibility(View.GONE);
        vehicleInfoCard.setVisibility(View.VISIBLE);
        if (mMap != null) {
            mMap.clear();
            for (Polyline p : polylines) p.remove();
            polylines.clear();
            updateMapMarkers();
        }
    }

    @Override
    public void onRoutesFetched(GoogleMapsService.DirectionsResponse response, Motorcycle vehicle) {
        if (mMap == null) return;
        
        mMap.clear();
        for (Polyline p : polylines) p.remove();
        polylines.clear();

        if (response.routes == null || response.routes.isEmpty()) return;

        int colorPrimary = ContextCompat.getColor(this, R.color.route_primary);
        int colorSecondary = ContextCompat.getColor(this, R.color.route_alternative);

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (int i = 0; i < response.routes.size(); i++) {
            GoogleMapsService.DirectionsResponse.Route route = response.routes.get(i);
            List<LatLng> decodedPath = PolylineDecoder.decodePoly(route.overviewPolyline.points);
            
            PolylineOptions polyOptions = new PolylineOptions()
                    .addAll(decodedPath)
                    .color(i == 0 ? colorPrimary : colorSecondary)
                    .width(12)
                    .zIndex(i == 0 ? 1 : 0);
            polylines.add(mMap.addPolyline(polyOptions));
            
            for (LatLng point : decodedPath) boundsBuilder.include(point);
        }

        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 150));
    }

    @Override
    public void onRouteClicked(int position) {
        if (polylines.size() > position) {
            int colorPrimary = ContextCompat.getColor(this, R.color.route_primary);
            int colorSecondary = ContextCompat.getColor(this, R.color.route_alternative);

            for (int i = 0; i < polylines.size(); i++) {
                polylines.get(i).setColor(i == position ? colorPrimary : colorSecondary);
                polylines.get(i).setZIndex(i == position ? 1 : 0);
            }
        }
    }

    @Override
    public void onStartNavigation(RouteModel selectedRoute, String destinationName, String destinationCoord, List<String> waypoints) {
        if ("PLAN".equals(mode)) {
            NavigationManager.getInstance().planTrip(selectedRoute, destinationName, destinationCoord, waypoints);
            setResult(RESULT_OK);
            finish();
        } else {
            // Immediate Start mode (from Search Bar) - Start navigation without overwriting/starting the planned trip
            NavigationManager.getInstance().startImmediateNavigation(selectedRoute, destinationName, destinationCoord, waypoints);
            Intent intent = new Intent(this, NavigationActivity.class);
            startActivity(intent);
            setResult(RESULT_OK);
            finish();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        if (origin != null && !origin.isEmpty()) {
            String[] parts = origin.split(",");
            LatLng originLatLng = new LatLng(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(originLatLng, 15f));
            mMap.addMarker(new MarkerOptions().position(originLatLng).title("Your Location"));
        }
    }

    @Override
    public void onBackPressed() {
        if (routeFragmentContainer.getVisibility() == View.VISIBLE) {
            showSuggestions();
        } else {
            super.onBackPressed();
        }
    }
}
