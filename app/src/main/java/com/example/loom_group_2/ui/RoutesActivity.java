package com.example.loom_group_2.ui;

import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.loom_group_2.R;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class RoutesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RouteAdapter adapter;
    private List<RouteModel> routes = new ArrayList<>();
    private Button btnStartNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

        recyclerView = findViewById(R.id.rvRoutes);
        btnStartNavigation = findViewById(R.id.btnStartNavigation);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RouteAdapter(routes);
        recyclerView.setAdapter(adapter);
    }

    // Call this method when your Directions API returns data
    private void displayRoutes(JSONObject jsonResponse) {
        routes.clear();
        try {
            JSONArray jsonRoutes = jsonResponse.getJSONArray("routes");

            for (int i = 0; i < jsonRoutes.length(); i++) {
                JSONObject routeObj = jsonRoutes.getJSONObject(i);
                JSONObject leg = routeObj.getJSONArray("legs").getJSONObject(0);

                String summary = routeObj.getString("summary");
                int duration = leg.getJSONObject("duration").getInt("value"); // seconds
                double distance = leg.getJSONObject("distance").getDouble("value"); // meters

                // Fetch KPL from your user's current vehicle (Budgetarian data)
                // double kpl = currentVehicle != null ? currentVehicle.getKpl() : 15.0;
                double kpl = 15.0;

                routes.add(new RouteModel(summary, duration, distance, kpl));
            }

            runOnUiThread(() -> {
                adapter.notifyDataSetChanged();
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}