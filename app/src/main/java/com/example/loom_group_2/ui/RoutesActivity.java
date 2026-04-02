package com.example.loom_group_2.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton; // Added Import
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.loom_group_2.R;
import com.example.loom_group_2.data.FirebaseUtil;
import com.example.loom_group_2.data.Motorcycle;
import com.example.loom_group_2.logic.GoogleMapsService;
import com.example.loom_group_2.logic.PolylineDecoder;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RoutesActivity extends AppCompatActivity implements OnMapReadyCallback, RouteAdapter.OnRouteClickListener {

    private GoogleMap mMap;
    private RecyclerView recyclerView;
    private RouteAdapter adapter;
    private List<RouteModel> routes = new ArrayList<>();
    private Button btnStartNavigation;
    private ImageButton btnBack; // Added Variable
    private Motorcycle currentVehicle;
    private List<Polyline> polylines = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

        recyclerView = findViewById(R.id.rvRoutes);
        btnStartNavigation = findViewById(R.id.btnStartNavigation);
        btnBack = findViewById(R.id.btnBack); // Added Initialization

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RouteAdapter(routes, this);
        recyclerView.setAdapter(adapter);

        // Added Back Button Logic
        btnBack.setOnClickListener(v -> {
            finish();
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        FirebaseUtil.getUserVehicle(motorcycle -> {
            this.currentVehicle = motorcycle;
            if (getIntent().hasExtra("origin") && getIntent().hasExtra("destination")) {
                fetchRoutes(getIntent().getStringExtra("origin"), getIntent().getStringExtra("destination"));
            }
        });
    }

    private void fetchRoutes(String origin, String destination) {
        String apiKey = getString(R.string.google_maps_key);
        GoogleMapsService.getInstance().getRoute(origin, destination, apiKey, new Callback<GoogleMapsService.DirectionsResponse>() {
            @Override
            public void onResponse(@NonNull Call<GoogleMapsService.DirectionsResponse> call, @NonNull Response<GoogleMapsService.DirectionsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    displayRoutes(response.body());
                } else {
                    Toast.makeText(RoutesActivity.this, "Failed to get routes", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<GoogleMapsService.DirectionsResponse> call, @NonNull Throwable t) {
                Toast.makeText(RoutesActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayRoutes(GoogleMapsService.DirectionsResponse response) {
        routes.clear();
        for (Polyline p : polylines) p.remove();
        polylines.clear();

        if (response.routes == null || response.routes.isEmpty()) return;

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();

        for (int i = 0; i < response.routes.size(); i++) {
            GoogleMapsService.DirectionsResponse.Route route = response.routes.get(i);
            GoogleMapsService.DirectionsResponse.Leg leg = route.legs.get(0);

            double kpl = (currentVehicle != null) ? currentVehicle.getKpl() : 15.0;
            String name = (route.summary != null && !route.summary.isEmpty()) ? route.summary : "Route " + (i + 1);

            routes.add(new RouteModel(name, leg.duration.value, (double) leg.distance.value, kpl));

            // Draw on map
            List<LatLng> decodedPath = PolylineDecoder.decodePoly(route.overviewPolyline.points);
            PolylineOptions polyOptions = new PolylineOptions()
                    .addAll(decodedPath)
                    .color(i == 0 ? Color.BLUE : Color.GRAY)
                    .width(10);
            polylines.add(mMap.addPolyline(polyOptions));

            for (LatLng point : decodedPath) boundsBuilder.include(point);
        }

        adapter.notifyDataSetChanged();

        if (!response.routes.isEmpty()) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100));
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
    }

    @Override
    public void onRouteClick(int position) {
        for (int i = 0; i < polylines.size(); i++) {
            polylines.get(i).setColor(i == position ? Color.BLUE : Color.GRAY);
            polylines.get(i).setZIndex(i == position ? 1.0f : 0.0f);
        }
    }
}