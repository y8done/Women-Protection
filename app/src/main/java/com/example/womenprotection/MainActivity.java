package com.example.womenprotection;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    private DatabaseReference databaseReference;
    private Button shareMyLocationbtn;
    private ImageView hotspotsView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase components
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
        shareMyLocationbtn = findViewById(R.id.button);
        imageView = findViewById(R.id.imageView);
        hotspotsView= findViewById(R.id.imageView3);
        // Assuming you have the current user's UID from FirebaseAuth
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    // Handle Home action
                    return true;
                } else if (id == R.id.nav_search) {
                    // Handle Search action
                    return true;
                } else if (id == R.id.nav_notifications) {
                    // Handle Notifications action
                    return true;
                } else if (id == R.id.nav_profile) {
                    // Navigate to ProfileActivity
                    Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });

        // Retrieve image URL from Firebase Realtime Database
        databaseReference.child(uid).child("photoUrl").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String imageUrl = snapshot.getValue(String.class);
                    if (imageUrl != null) {
                        // Load image using Glide
                        Glide.with(MainActivity.this)
                                .load(imageUrl)
                                .into(imageView);
                    } else {
                        Toast.makeText(MainActivity.this, "Image URL is null.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "No data found for the user.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Failed to retrieve image URL: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        shareMyLocationbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, location_sharing.class);
                startActivity(intent);
            }
        });
        hotspotsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, HotspotMapActivity.class);
                startActivity(intent);
            }
        });
    }
}
