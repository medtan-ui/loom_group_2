package com.example.loom_group_2.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
                tvCurrentVehicle.setText(motorcycle.getYear() + " " + motorcycle.getMake() + " " + motorcycle.getModel());
            }
        });
    }

    private void showMakeSelection() {
        FirebaseUtil.getMakes(makes -> {
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
        FirebaseUtil.getYears(make, model, years -> {
            String[] items = years.toArray(new String[0]);
            new AlertDialog.Builder(this)
                    .setTitle("Select Year")
                    .setItems(items, (dialog, which) -> fetchAndSaveVehicle(make, model, items[which]))
                    .show();
        });
    }

    private void fetchAndSaveVehicle(String make, String model, String year) {
        FirebaseUtil.getMotorcycleDetails(make, model, year, motorcycle -> {
            if (motorcycle != null) {
                FirebaseUtil.saveUserVehicle(motorcycle);
                tvCurrentVehicle.setText(motorcycle.getYear() + " " + motorcycle.getMake() + " " + motorcycle.getModel());
                Toast.makeText(this, "Vehicle updated!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
