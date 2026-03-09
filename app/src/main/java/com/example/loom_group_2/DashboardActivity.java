package com.example.loom_group_2;

import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

public class DashboardActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize the Map Fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Coordinates for the trip
        LatLng startPoint = new LatLng(37.7749, -122.4194);
        LatLng endPoint = new LatLng(37.7849, -122.4094);

        // Draw the blue trip line to match your design
        mMap.addPolyline(new PolylineOptions()
                .add(startPoint, endPoint)
                .width(10f)
                .color(Color.BLUE)
                .geodesic(true));

        // Move camera and zoom in on the trip
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startPoint, 14f));
    }
}