package com.example.loom_group_2.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.loom_group_2.R;
import com.example.loom_group_2.data.FirebaseUtil;
import com.example.loom_group_2.data.Motorcycle;
import com.example.loom_group_2.logic.GoogleMapsService;
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

    public interface OnRouteSelectedListener {
        void onRoutesFetched(GoogleMapsService.DirectionsResponse response, Motorcycle vehicle);
        void onRouteClicked(int position);
        void onStartNavigation(RouteModel selectedRoute);
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
                String origin = getArguments().getString("origin");
                String destination = getArguments().getString("destination");
                fetchRoutes(origin, destination);
            }
        });

        btnStartNavigation.setOnClickListener(v -> {
            if (listener != null && !routes.isEmpty()) {
                listener.onStartNavigation(routes.get(adapter.getSelectedPosition()));
            }
        });

        return view;
    }

    private void fetchRoutes(String origin, String destination) {
        String apiKey = getString(R.string.google_maps_key);
        GoogleMapsService.getInstance().getRoute(origin, destination, apiKey, new Callback<GoogleMapsService.DirectionsResponse>() {
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
        });
    }

    private void displayRoutes(GoogleMapsService.DirectionsResponse response) {
        routes.clear();
        if (response.routes == null || response.routes.isEmpty()) return;

        for (int i = 0; i < response.routes.size(); i++) {
            GoogleMapsService.DirectionsResponse.Route route = response.routes.get(i);
            GoogleMapsService.DirectionsResponse.Leg leg = route.legs.get(0);
            double kpl = (currentVehicle != null) ? currentVehicle.getKpl() : 15.0;
            String name = (route.summary != null && !route.summary.isEmpty()) ? route.summary : "Route " + (i + 1);
            routes.add(new RouteModel(name, leg.duration.value, (double) leg.distance.value, kpl));
        }
        adapter.notifyDataSetChanged();
    }
}
