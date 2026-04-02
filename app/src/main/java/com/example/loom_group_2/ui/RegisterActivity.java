package com.example.loom_group_2.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.loom_group_2.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";
    private static final int RC_SIGN_IN = 9001;
    private static final int PICK_IMAGE_REQUEST = 1;
    
    private EditText etEmail, etPassword, etName;
    private Button btnRegister;
    private com.google.android.gms.common.SignInButton btnGoogleSignIn;
    private TextView tvSignIn;
    private ShapeableImageView ivRegisterProfile;
    private ProgressBar progressBar;
    private Uri imageUri;
    
    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        
        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        ivRegisterProfile = findViewById(R.id.ivRegisterProfile);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        tvSignIn = findViewById(R.id.tvSignIn);
        progressBar = findViewById(R.id.progressBar);

        ivRegisterProfile.setOnClickListener(v -> openGallery());
        tvSignIn.setOnClickListener(v -> finish());
        btnRegister.setOnClickListener(v -> registerUser());
        btnGoogleSignIn.setOnClickListener(v -> signIn());
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
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
        } else if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            ivRegisterProfile.setImageURI(imageUri);
            ivRegisterProfile.setPadding(0, 0, 0, 0);
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        if (idToken == null) return;
        setLoading(true);
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        mAuth.signOut();
                        Toast.makeText(RegisterActivity.this, "Registration successful! Please login.", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Google auth failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void registerUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password should be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        Toast.makeText(this, "Creating account...", Toast.LENGTH_SHORT).show();
        
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    sendVerificationEmail(user, name);
                }
            } else {
                setLoading(false);
                Toast.makeText(this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void sendVerificationEmail(FirebaseUser user, String name) {
        Toast.makeText(this, "Sending verification email...", Toast.LENGTH_SHORT).show();
        user.sendEmailVerification().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "Email verification sent successfully");
                if (imageUri != null) {
                    uploadImageAndSaveProfile(user, name, true);
                } else {
                    updateProfile(user, name, null, true);
                }
            } else {
                Log.e(TAG, "Failed to send verification email", task.getException());
                // ROLLBACK: Delete the user if the email fails to send
                user.delete().addOnCompleteListener(deleteTask -> {
                    setLoading(false);
                    mAuth.signOut();
                    Toast.makeText(this, "Failed to send email. Check if email is real and try again.", Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void uploadImageAndSaveProfile(FirebaseUser user, String name, boolean isNewRegistration) {
        Toast.makeText(this, "Uploading profile picture...", Toast.LENGTH_SHORT).show();
        StorageReference profileRef = mStorage.getReference().child("profile_pics/" + user.getUid() + ".jpg");
        profileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            profileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                updateProfile(user, name, uri, isNewRegistration);
            });
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Image upload failed, continuing with profile update", e);
            updateProfile(user, name, null, isNewRegistration);
        });
    }

    private void updateProfile(FirebaseUser user, String name, Uri photoUri, boolean isNewRegistration) {
        UserProfileChangeRequest.Builder builder = new UserProfileChangeRequest.Builder().setDisplayName(name);
        if (photoUri != null) builder.setPhotoUri(photoUri);

        user.updateProfile(builder.build()).addOnCompleteListener(profileTask -> {
            setLoading(false);
            if (isNewRegistration) {
                // Stay logged in for VerificationActivity to check status
                startActivity(new Intent(RegisterActivity.this, VerificationActivity.class));
                finish();
            }
        });
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnRegister.setEnabled(false);
            btnGoogleSignIn.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btnRegister.setEnabled(true);
            btnGoogleSignIn.setEnabled(true);
        }
    }
}
