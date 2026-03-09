package com.example.loom_group_2.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import com.example.loom_group_2.R;

public class VerificationActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);
        Button btnVerify = findViewById(R.id.btnVerify);
        btnVerify.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }
}
