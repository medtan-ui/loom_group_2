package com.example.loom_group_2.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.loom_group_2.R;
import com.example.loom_group_2.data.FirebaseUtil;
import com.example.loom_group_2.data.Motorcycle;
import com.example.loom_group_2.logic.GoogleMapsService;
import com.example.loom_group_2.logic.NavigationManager;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RoutesFragment extends Fragment {

    private RecyclerView recyclerView;
    private RouteAdapter adapter;
    private List<RouteModel> routes = new ArrayList<>();
    private Button btnStartNavigation;
    private Motorcycle currentVehicle;
    private OnRouteSelectedListener listener;
    private String currentOrigin, initialDestinationCoord, selectedDestinationCoord;
    private List<String> pendingWaypoints = new ArrayList<>();
    private String displayDestinationName;
    private String mode = "NAVIGATE";

    public interface OnRouteSelectedListener {
        void onRoutesFetched(GoogleMapsService.DirectionsResponse response, Motorcycle vehicle);
        void onRouteClicked(int position);
        void onStartNavigation(RouteModel selectedRoute, String destinationName, String destinationCoord, List<String> waypoints);
    }

    public void setOnRouteSelectedListener(OnRouteSelectedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_routes, container, false);
        recyclerView = view.findViewById(R.id.rvRoutes);
        btnStartNavigation = view.findViewById(R.id.btnStartNavigation);

        if (getArguments() != null) {
            mode = getArguments().getString("mode", "NAVIGATE");
        }

        if ("PLAN".equals(mode)) {
            btnStartNavigation.setText("Add Next Trip");
        } else {
            btnStartNavigation.setText(R.string.start_navigation);
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RouteAdapter(routes, position -> {
            if (listener != null) {
                listener.onRouteClicked(position);
            }
        });
        recyclerView.setAdapter(adapter);

        FirebaseUtil.getUserVehicle(motorcycle -> {
            this.currentVehicle = motorcycle;
            if (getArguments() != null) {
                currentOrigin = getArguments().getString("origin");
                initialDestinationCoord = getArguments().getString("destination");
                selectedDestinationCoord = initialDestinationCoord;
                displayDestinationName = getArguments().getString("destination_name");
                
                if (NavigationManager.getInstance().isNavigating()) {
                    showNavigationConflictDialog();
                } else {
                    fetchRoutes(currentOrigin, selectedDestinationCoord, null);
                }
            }
        });

        btnStartNavigation.setOnClickListener(v -> {
            if (listener != null && !routes.isEmpty()) {
                RouteModel selectedRoute = routes.get(adapter.getSelectedPosition());
                String name = displayDestinationName != null ? displayDestinationName : "Destination";
                listener.onStartNavigation(selectedRoute, name, selectedDestinationCoord, pendingWaypoints);
            }
        });

        return view;
    }

    private void showNavigationConflictDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Ongoing Navigation")
                .setMessage("You already have an active navigation. Would you like to add this as a stop or start a new route?")
                .setPositiveButton("Add as Stop", (dialog, which) -> {
                    pendingWaypoints.clear();
                    pendingWaypoints.addAll(NavigationManager.getInstance().getWaypoints());
                    pendingWaypoints.add(initialDestinationCoord);
                    
                    selectedDestinationCoord = NavigationManager.getInstance().getDestinationCoordinate();
                    displayDestinationName = NavigationManager.getInstance().getDestinationName();
                    
                    fetchRoutes(currentOrigin, selectedDestinationCoord, TextUtils.join("|", pendingWaypoints));
                })
                .setNeutralButton("New Route", (dialog, which) -> {
                    NavigationManager.getInstance().stopNavigation();
                    pendingWaypoints.clear();
                    selectedDestinationCoord = initialDestinationCoord;
                    fetchRoutes(currentOrigin, selectedDestinationCoord, null);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    if (getActivity() != null) getActivity().onBackPressed();
                })
                .setCancelable(false)
                .show();
    }

    private void fetchRoutes(String origin, String destination, @Nullable String waypoints) {
        String apiKey = getString(R.string.google_maps_key);
        Callback<GoogleMapsService.DirectionsResponse> callback = new Callback<GoogleMapsService.DirectionsResponse>() {
            @Override
            public void onResponse(@NonNull Call<GoogleMapsService.DirectionsResponse> call, @NonNull Response<GoogleMapsService.DirectionsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    displayRoutes(response.body());
                    if (listener != null) {
                        listener.onRoutesFetched(response.body(), currentVehicle);
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to get routes", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<GoogleMapsService.DirectionsResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        if (waypoints != null) {
            GoogleMapsService.getInstance().getRouteWithWaypoints(origin, destination, waypoints, apiKey, callback);
        } else {
            GoogleMapsService.getInstance().getRoute(origin, destination, apiKey, callback);
        }
    }

    private void displayRoutes(GoogleMapsService.DirectionsResponse response) {
        routes.clear();
        if (response.routes == null || response.routes.isEmpty()) return;

        for (int i = 0; i < response.routes.size(); i++) {
            GoogleMapsService.DirectionsResponse.Route route = response.routes.get(i);
            
            long totalDuration = 0;
            double totalDistance = 0;
            for (GoogleMapsService.DirectionsResponse.Leg leg : route.legs) {
                totalDuration += leg.duration.value;
                totalDistance += leg.distance.value;
            }

            double kpl = (currentVehicle != null) ? currentVehicle.getKpl() : 15.0;
            String name = (route.summary != null && !route.summary.isEmpty()) ? route.summary : "Route " + (i + 1);
            RouteModel model = new RouteModel(name, (int)totalDuration, totalDistance, kpl);
            if (route.overviewPolyline != null) {
                model.setPolylinePoints(route.overviewPolyline.points);
            }
            routes.add(model);
        }
        adapter.notifyDataSetChanged();
    }
}
