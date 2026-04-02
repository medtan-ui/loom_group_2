package com.example.loom_group_2.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.loom_group_2.R;
import com.example.loom_group_2.data.FirebaseUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ProfileActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    
    private TextView tvName, tvEmail, tvCurrentVehicle;
    private ImageView ivProfileLarge;
    private Button btnLogout, btnSelectVehicle;
    private ImageButton btnBack;
    
    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        ivProfileLarge = findViewById(R.id.ivProfileLarge);
        tvName = findViewById(R.id.tvProfileName);
        tvEmail = findViewById(R.id.tvProfileEmail);
        tvCurrentVehicle = findViewById(R.id.tvCurrentVehicle);
        btnLogout = findViewById(R.id.btnLogout);
        btnSelectVehicle = findViewById(R.id.btnSelectVehicle);
        btnBack = findViewById(R.id.btnBackProfile);

        if (user != null) {
            tvName.setText(user.getDisplayName() != null ? user.getDisplayName() : "No Name");
            tvEmail.setText(user.getEmail());
            loadProfileImage(user.getPhotoUrl());
            loadUserVehicle();
        }

        // Click to Edit, Long Click to View Full
        ivProfileLarge.setOnClickListener(v -> openGallery());
        ivProfileLarge.setOnLongClickListener(v -> {
            if (user != null && user.getPhotoUrl() != null) {
                showFullImage(user.getPhotoUrl());
            } else {
                Toast.makeText(this, "No profile picture to view", Toast.LENGTH_SHORT).show();
            }
            return true;
        });

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

    private void loadProfileImage(Uri photoUrl) {
        if (photoUrl != null) {
            Glide.with(this)
                    .load(photoUrl)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .circleCrop()
                    .into(ivProfileLarge);
        }
    }

    private void showFullImage(Uri photoUrl) {
        Dialog builder = new Dialog(this);
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        builder.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        
        ImageView imageView = new ImageView(this);
        Glide.with(this).load(photoUrl).into(imageView);
        builder.addContentView(imageView, new android.widget.RelativeLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT));
        builder.show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            uploadImageAndUpdateProfile();
        }
    }

    private void uploadImageAndUpdateProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || imageUri == null) return;

        Toast.makeText(this, "Uploading...", Toast.LENGTH_SHORT).show();
        StorageReference profileRef = mStorage.getReference().child("profile_pics/" + user.getUid() + ".jpg");
        
        profileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            profileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setPhotoUri(uri)
                        .build();

                user.updateProfile(profileUpdates).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        loadProfileImage(uri);
                        Toast.makeText(this, "Profile picture updated", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
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
