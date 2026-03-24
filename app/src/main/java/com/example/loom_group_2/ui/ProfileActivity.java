package com.example.loom_group_2.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.loom_group_2.R;
import com.example.loom_group_2.data.FirebaseUtil;
import com.example.loom_group_2.data.Motorcycle;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {
    private TextView tvName, tvEmail, tvCurrentVehicle;
    private Button btnLogout, btnSelectVehicle;
    private ImageButton btnBack;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        tvName = findViewById(R.id.tvProfileName);
        tvEmail = findViewById(R.id.tvProfileEmail);
        tvCurrentVehicle = findViewById(R.id.tvCurrentVehicle);
        btnLogout = findViewById(R.id.btnLogout);
        btnSelectVehicle = findViewById(R.id.btnSelectVehicle);
        btnBack = findViewById(R.id.btnBackProfile);

        if (user != null) {
            tvName.setText(user.getDisplayName() != null ? user.getDisplayName() : "No Name");
            tvEmail.setText(user.getEmail());
            loadUserVehicle();
        }

        btnBack.setOnClickListener(v -> finish());
        
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        btnSelectVehicle.setOnClickListener(v -> showMakeSelection());
    }

    private void loadUserVehicle() {
        FirebaseUtil.getUserVehicle(motorcycle -> {
            if (motorcycle != null) {
                String vehicleInfo = motorcycle.getYear() + " " + motorcycle.getMake() + " " + motorcycle.getModel();
                tvCurrentVehicle.setText(vehicleInfo);
            }
        });
    }

    private void showMakeSelection() {
        FirebaseUtil.getMakes(makes -> {
            if (makes.isEmpty()) {
                Toast.makeText(this, "No vehicles found in database", Toast.LENGTH_SHORT).show();
                return;
            }
            String[] items = makes.toArray(new String[0]);
            new AlertDialog.Builder(this)
                    .setTitle("Select Make")
                    .setItems(items, (dialog, which) -> showModelSelection(items[which]))
                    .show();
        });
    }

    private void showModelSelection(String make) {
        FirebaseUtil.getModels(make, models -> {
            String[] items = models.toArray(new String[0]);
            new AlertDialog.Builder(this)
                    .setTitle("Select Model")
                    .setItems(items, (dialog, which) -> showYearSelection(make, items[which]))
                    .show();
        });
    }

    private void showYearSelection(String make, String model) {
        FirebaseUtil.getYears(make, model, yearIds -> {
            String[] items = yearIds.toArray(new String[0]);
            // Format labels for display (e.g. "2023_Manual" -> "2023 (Manual)")
            String[] labels = new String[items.length];
            for (int i = 0; i < items.length; i++) {
                labels[i] = items[i].replace("_", " (") + ")";
            }

            new AlertDialog.Builder(this)
                    .setTitle("Select Year & Transmission")
                    .setItems(labels, (dialog, which) -> fetchAndSaveVehicle(make, model, items[which]))
                    .show();
        });
    }

    private void fetchAndSaveVehicle(String make, String model, String yearId) {
        FirebaseUtil.getMotorcycleDetails(make, model, yearId, motorcycle -> {
            if (motorcycle != null) {
                FirebaseUtil.saveUserVehicle(motorcycle);
                String vehicleInfo = motorcycle.getYear() + " " + motorcycle.getMake() + " " + motorcycle.getModel();
                tvCurrentVehicle.setText(vehicleInfo);
                Toast.makeText(this, "Vehicle updated successfully!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
