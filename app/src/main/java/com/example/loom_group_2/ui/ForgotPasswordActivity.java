package com.example.loom_group_2.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.loom_group_2.R;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {
    private EditText etEmail;
    private Button btnSendCode;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.verification_email);
        mAuth = FirebaseAuth.getInstance();
        etEmail = findViewById(R.id.etEmailForgot);
        btnSendCode = findViewById(R.id.btnSendCode);
        btnSendCode.setOnClickListener(v -> sendResetEmail());
    }

    private void sendResetEmail() {
        String email = etEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email)) return;
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                startActivity(new Intent(this, VerificationActivity.class));
            }
        });
    }
}
