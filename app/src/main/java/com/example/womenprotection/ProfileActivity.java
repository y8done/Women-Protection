package com.example.womenprotection;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.bumptech.glide.Glide;

public class ProfileActivity extends AppCompatActivity {

    private TextView fullNameText, emailText, phoneNumberText, birthdateText, genderText;
    private TextView emergencyContact1Text, emergencyContact2Text, emergencyContact3Text;
    private ShapeableImageView profileImage;
    private Button editButton;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase components
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        // Initialize UI components
        fullNameText = findViewById(R.id.fullNameText);
        emailText = findViewById(R.id.emailText);
        phoneNumberText = findViewById(R.id.phoneNumberText);
        birthdateText = findViewById(R.id.birthdateText);
        genderText = findViewById(R.id.genderText);
        emergencyContact1Text = findViewById(R.id.emergencyContact1Text);
        emergencyContact2Text = findViewById(R.id.emergencyContact2Text);
        emergencyContact3Text = findViewById(R.id.emergencyContact3Text);
        profileImage = findViewById(R.id.profileImage);
        editButton = findViewById(R.id.editButton);

        // Fetch user details from Firebase
        fetchUserDetails();

        // Set up bottom navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_profile); // Set to profile
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    // Navigate to Home Activity
                    startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                    return true;
                } else if (id == R.id.nav_search) {
                    // Navigate to Search Activity
                    startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                    return true;
                } else if (id == R.id.nav_notifications) {
                    // Navigate to Notifications Activity
                    startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                    return true;
                } else if (id == R.id.nav_profile) {
                    // Profile is already selected
                    return true;
                }
                return false;
            }
        });

        // Edit button click listener
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
                startActivity(intent);
            }
        });
    }

    private void fetchUserDetails() {
        String uid = mAuth.getCurrentUser().getUid();
        databaseReference.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String fullName = snapshot.child("fullName").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String phoneNumber = snapshot.child("phoneNumber").getValue(String.class);
                    String birthdate = snapshot.child("birthDate").getValue(String.class);
                    String gender = snapshot.child("gender").getValue(String.class);
                    String profileImageUrl = snapshot.child("photoUrl").getValue(String.class);

                    fullNameText.setText("Name: " + fullName);
                    emailText.setText("Email: " + email);
                    phoneNumberText.setText("Phone Number: " + phoneNumber);
                    birthdateText.setText("Birthdate: " + birthdate);
                    genderText.setText("Gender: " + gender);

                    // Emergency Contacts (assumed structure: users -> uid -> emergencyContacts -> contact1, contact2, contact3)
                    DataSnapshot emergencyContactsSnapshot = snapshot.child("emergencyContacts");
                    String emergencyContact1 = emergencyContactsSnapshot.child("contact1").getValue(String.class);
                    String emergencyContact2 = emergencyContactsSnapshot.child("contact2").getValue(String.class);
                    String emergencyContact3 = emergencyContactsSnapshot.child("contact3").getValue(String.class);

                    emergencyContact1Text.setText("Contact 1: " + emergencyContact1);
                    emergencyContact2Text.setText("Contact 2: " + emergencyContact2);
                    emergencyContact3Text.setText("Contact 3: " + emergencyContact3);

                    // Load profile image using Glide
                    if (profileImageUrl != null) {
                        Glide.with(ProfileActivity.this)
                                .load(profileImageUrl)
                                .into(profileImage);
                    } else {
                        Toast.makeText(ProfileActivity.this, "Profile image URL is null.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ProfileActivity.this, "No data found for the user.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Failed to retrieve user details: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
