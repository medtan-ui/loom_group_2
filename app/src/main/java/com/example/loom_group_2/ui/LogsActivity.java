package com.example.loom_group_2.ui;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.loom_group_2.R;
import com.example.loom_group_2.data.TripLog;
import com.example.loom_group_2.logic.DataPersistenceController;
import java.util.ArrayList;
import java.util.List;

public class LogsActivity extends AppCompatActivity {
    private RecyclerView rvAllLogs;
    private LogAdapter logAdapter;
    private List<TripLog> tripLogs = new ArrayList<>();
    private DataPersistenceController dataController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logs);

        ImageButton btnBack = findViewById(R.id.btnBackLogs);
        rvAllLogs = findViewById(R.id.rvAllLogs);
        dataController = DataPersistenceController.getInstance(this);

        btnBack.setOnClickListener(v -> finish());

        rvAllLogs.setLayoutManager(new LinearLayoutManager(this));
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

        rvAllLogs.setAdapter(logAdapter);

        loadAllLogs();
    }

    private void loadAllLogs() {
        dataController.getAllTripLogs(logs -> runOnUiThread(() -> {
            tripLogs.clear();
            tripLogs.addAll(logs);
            logAdapter.notifyDataSetChanged();
        }));
    }
}
