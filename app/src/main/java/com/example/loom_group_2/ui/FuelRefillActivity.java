package com.example.loom_group_2.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.loom_group_2.R;
import com.example.loom_group_2.data.FuelRefill;
import com.example.loom_group_2.logic.FuelRefillRepository;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FuelRefillActivity extends AppCompatActivity {

    private TextInputEditText etLiters, etOdometerBefore, etOdometerAfter, etNotes;
    private Button btnLogRefill;
    private ImageButton btnBack;
    private TextView tvEmptyRefills;
    private RecyclerView rvRefills;
    private FuelRefillAdapter adapter;
    private FuelRefillRepository repository;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fuel_refill);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please log in again", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        uid = user.getUid();
        repository = FuelRefillRepository.getInstance(this);

        etLiters = findViewById(R.id.etLiters);
        etOdometerBefore = findViewById(R.id.etOdometerBefore);
        etOdometerAfter = findViewById(R.id.etOdometerAfter);
        etNotes = findViewById(R.id.etNotes);
        btnLogRefill = findViewById(R.id.btnLogRefill);
        btnBack = findViewById(R.id.btnBack);
        tvEmptyRefills = findViewById(R.id.tvEmptyRefills);
        rvRefills = findViewById(R.id.rvRefills);

        btnBack.setOnClickListener(v -> finish());

        setupRecyclerView();
        loadRefills();
        preFillOdometerAfter();

        btnLogRefill.setOnClickListener(v -> logRefill());
    }

    private void setupRecyclerView() {
        rvRefills.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FuelRefillAdapter(new ArrayList<>());
        adapter.setOnItemLongClickListener(this::showDeleteConfirmation);
        rvRefills.setAdapter(adapter);
    }

    private void preFillOdometerAfter() {
        repository.getLatestRefill(uid, refill -> {
            if (refill != null) {
                runOnUiThread(() -> etOdometerAfter.setText(String.format(Locale.US, "%.2f", refill.getOdometerBefore())));
            }
        });
    }

    private void logRefill() {
        String litersStr = etLiters.getText().toString();
        String odoBeforeStr = etOdometerBefore.getText().toString();
        String odoAfterStr = etOdometerAfter.getText().toString();
        String notes = etNotes.getText().toString();

        if (TextUtils.isEmpty(litersStr) || TextUtils.isEmpty(odoBeforeStr) || TextUtils.isEmpty(odoAfterStr)) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double liters = Double.parseDouble(litersStr);
        double odoBefore = Double.parseDouble(odoBeforeStr);
        double odoAfter = Double.parseDouble(odoAfterStr);

        if (liters <= 0) {
            etLiters.setError("Liters must be > 0");
            return;
        }
        if (odoBefore <= 0) {
            etOdometerBefore.setError("Current odometer must be > 0");
            return;
        }
        if (odoAfter < 0) {
            etOdometerAfter.setError("Odometer must be >= 0");
            return;
        }
        if (odoBefore <= odoAfter) {
            etOdometerBefore.setError("Current odometer must be greater than last fill-up odometer");
            return;
        }

        double kpl = (odoBefore - odoAfter) / liters;
        String date = DateFormat.getDateInstance().format(new Date());

        FuelRefill refill = new FuelRefill(uid, liters, odoBefore, odoAfter, kpl, date, notes);

        repository.insertRefill(refill, () -> runOnUiThread(() -> {
            etLiters.setText("");
            etOdometerBefore.setText("");
            etOdometerAfter.setText("");
            etNotes.setText("");
            etLiters.clearFocus();
            etOdometerBefore.clearFocus();
            etOdometerAfter.clearFocus();
            etNotes.clearFocus();
            
            loadRefills();
            preFillOdometerAfter();
            showKplResult(kpl);
        }));
    }

    private void showKplResult(double kpl) {
        new AlertDialog.Builder(this)
                .setTitle("Real-World KPL Result")
                .setMessage(String.format(Locale.US, "Based on your last fill-up:\n\n%.2f km/L\n\nThis is your actual fuel efficiency for that stretch.", kpl))
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void loadRefills() {
        repository.getRefillsByUser(uid, refills -> runOnUiThread(() -> {
            if (refills == null || refills.isEmpty()) {
                tvEmptyRefills.setVisibility(View.VISIBLE);
                rvRefills.setVisibility(View.GONE);
            } else {
                tvEmptyRefills.setVisibility(View.GONE);
                rvRefills.setVisibility(View.VISIBLE);
                adapter.setRefills(refills);
            }
        }));
    }

    private void showDeleteConfirmation(FuelRefill refill) {
        new AlertDialog.Builder(this)
                .setTitle("Delete this refill log?")
                .setMessage("This action cannot be undone.")
                .setPositiveButton("Yes", (dialog, which) -> {
                    repository.deleteRefill(refill.getId(), () -> runOnUiThread(this::loadRefills));
                })
                .setNegativeButton("No", null)
                .show();
    }
}
