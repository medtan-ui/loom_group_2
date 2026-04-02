package com.example.loom_group_2.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import com.example.loom_group_2.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

public class VerificationActivity extends AppCompatActivity {
    private TextView tvTitle, tvSubTitle, tvResendCode, tvWarning;
    private Button btnVerify;
    private FirebaseAuth mAuth;
    private String flowType;
    private CountDownTimer countDownTimer;
    private CountDownTimer totalTimeoutTimer;
    private boolean canResend = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        mAuth = FirebaseAuth.getInstance();
        tvTitle = findViewById(R.id.tvTitle);
        tvSubTitle = findViewById(R.id.tvSubTitle);
        tvResendCode = findViewById(R.id.tvResendCode);
        tvWarning = findViewById(R.id.tvWarning);
        btnVerify = findViewById(R.id.btnVerify);

        flowType = getIntent().getStringExtra("flow");

        setupUI();
        startResendTimer();
        
        // Only start the 10-minute deletion timer for NEW registrations
        if (!"password_reset".equals(flowType)) {
            startTotalTimeoutTimer();
        } else {
            tvWarning.setVisibility(View.GONE);
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleCancellation();
            }
        });

        tvResendCode.setOnClickListener(v -> {
            if (canResend) {
                resendVerification();
            }
        });

        btnVerify.setOnClickListener(v -> {
            if ("password_reset".equals(flowType)) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else {
                checkVerificationStatus();
            }
        });
    }

    private void handleCancellation() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && !"password_reset".equals(flowType)) {
            user.delete().addOnCompleteListener(task -> {
                mAuth.signOut();
                Toast.makeText(VerificationActivity.this, "Verification cancelled. Account not created.", Toast.LENGTH_LONG).show();
                startActivity(new Intent(VerificationActivity.this, MainActivity.class));
                finish();
            });
        } else {
            mAuth.signOut();
            startActivity(new Intent(VerificationActivity.this, MainActivity.class));
            finish();
        }
    }

    private void setupUI() {
        if ("password_reset".equals(flowType)) {
            tvTitle.setText("Reset Email Sent");
            tvSubTitle.setText("A password reset link has been sent to your email. Please check your inbox and spam folder, then follow the instructions.");
            btnVerify.setText("Back to Login");
        } else {
            tvTitle.setText("Verify Your Email");
            tvSubTitle.setText("We've sent a verification link to your email. Please check your inbox and spam folder, then click the link to activate your account.");
            btnVerify.setText("I've Verified My Email");
        }
    }

    private void startResendTimer() {
        canResend = false;
        tvResendCode.setClickable(false);
        tvResendCode.setTextColor(getResources().getColor(android.R.color.darker_gray));

        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvResendCode.setText(String.format(Locale.getDefault(), "Resend Link in: %ds", millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                canResend = true;
                tvResendCode.setText("Resend Link");
                tvResendCode.setClickable(true);
                tvResendCode.setTextColor(getResources().getColor(R.color.black));
            }
        }.start();
    }

    private void startTotalTimeoutTimer() {
        // 10 minute timer (600,000 milliseconds)
        totalTimeoutTimer = new CountDownTimer(600000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Background tracking, no need to update UI every second
            }

            @Override
            public void onFinish() {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null && !user.isEmailVerified()) {
                    user.delete().addOnCompleteListener(task -> {
                        mAuth.signOut();
                        Toast.makeText(VerificationActivity.this, "Verification period expired. Account deleted.", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(VerificationActivity.this, MainActivity.class));
                        finish();
                    });
                }
            }
        }.start();
    }

    private void resendVerification() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Verification email resent! Check your spam folder.", Toast.LENGTH_LONG).show();
                    startResendTimer();
                } else {
                    Toast.makeText(this, "Failed to resend: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void checkVerificationStatus() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.reload().addOnCompleteListener(task -> {
                if (user.isEmailVerified()) {
                    if (totalTimeoutTimer != null) totalTimeoutTimer.cancel();
                    Toast.makeText(this, "Email verified successfully!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(this, "Email not verified yet. Please check your inbox and spam folder.", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
        if (totalTimeoutTimer != null) totalTimeoutTimer.cancel();
    }
}
