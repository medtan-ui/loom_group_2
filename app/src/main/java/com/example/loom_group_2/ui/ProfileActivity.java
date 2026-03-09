package com.example.loom_group_2.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.loom_group_2.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        TextView tvName = findViewById(R.id.tvProfileName);
        TextView tvEmail = findViewById(R.id.tvProfileEmail);
        Button btnLogout = findViewById(R.id.btnLogout);

        if (user != null) {
            tvName.setText(user.getDisplayName() != null ? user.getDisplayName() : "No Name");
            tvEmail.setText(user.getEmail());
        }

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
