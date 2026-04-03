package com.example.loom_group_2.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.loom_group_2.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int RC_SIGN_IN = 9001;
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private SignInButton btnGoogleSignIn;
    private TextView tvForgotPassword, tvSignUp;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            if (currentUser.isEmailVerified()) {
                startActivity(new Intent(this, DashboardActivity.class));
                finish();
            } else {
                checkStaleAccount(currentUser);
            }
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvSignUp = findViewById(R.id.tvSignUp);
        
        tvForgotPassword.setOnClickListener(v -> startActivity(new Intent(this, ForgotPasswordActivity.class)));
        tvSignUp.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
        
        btnLogin.setOnClickListener(v -> loginUser());
        btnGoogleSignIn.setOnClickListener(v -> signIn());
    }

    private void checkStaleAccount(FirebaseUser user) {
        if (user.getMetadata() == null) return;
        
        long creationTime = user.getMetadata().getCreationTimestamp();
        long currentTime = System.currentTimeMillis();
        long tenMinutesInMillis = 10 * 60 * 1000;

        if (currentTime - creationTime > tenMinutesInMillis) {
            // Account is older than 10 mins and still unverified. Delete it.
            user.delete().addOnCompleteListener(task -> {
                mAuth.signOut();
                Toast.makeText(MainActivity.this, "Verification period expired. Account deleted.", Toast.LENGTH_LONG).show();
            });
        } else {
            // Still in the window, but we sign them out to force a fresh login/verification check
            mAuth.signOut();
        }
    }

    private void signIn() {
        mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account.getIdToken());
                }
            } catch (ApiException e) {
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        AuthResult result = task.getResult();
                        boolean isNewUser = result != null && result.getAdditionalUserInfo() != null && result.getAdditionalUserInfo().isNewUser();
                        
                        if (isNewUser) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                user.delete().addOnCompleteListener(deleteTask -> {
                                    mAuth.signOut();
                                    mGoogleSignInClient.signOut();
                                    Toast.makeText(MainActivity.this, "Account not found. Please Register first.", Toast.LENGTH_LONG).show();
                                });
                            }
                        } else {
                            startActivity(new Intent(MainActivity.this, DashboardActivity.class));
                            finish();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    if (user.isEmailVerified()) {
                        startActivity(new Intent(this, DashboardActivity.class));
                        finish();
                    } else {
                        // Check if it's stale before signing them out
                        long creationTime = user.getMetadata().getCreationTimestamp();
                        if (System.currentTimeMillis() - creationTime > (10 * 60 * 1000)) {
                            user.delete().addOnCompleteListener(t -> {
                                mAuth.signOut();
                                Toast.makeText(this, "Verification expired. Account deleted.", Toast.LENGTH_LONG).show();
                            });
                        } else {
                            mAuth.signOut();
                            Toast.makeText(this, "Please verify your email first.", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
