package com.example.womenprotection;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EmergencyContactsActivity extends AppCompatActivity {

    private EditText contact1, contact2, contact3;
    private Button saveContactsButton;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contacts);

        contact1 = findViewById(R.id.contact1);
        contact2 = findViewById(R.id.contact2);
        contact3 = findViewById(R.id.contact3);
        saveContactsButton = findViewById(R.id.saveContactsButton);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(uid);

        saveContactsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveContacts();
            }
        });

        // Optionally, check if profile is already completed
        checkProfileCompletion();
    }

    private void saveContacts() {
        String contact1Text = contact1.getText().toString().trim();
        String contact2Text = contact2.getText().toString().trim();
        String contact3Text = contact3.getText().toString().trim();

        if (TextUtils.isEmpty(contact1Text) || !isValidPhoneNumber(contact1Text)) {
            Toast.makeText(this, "Primary contact is required and must be 10 digits", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save contacts to Firebase
        databaseReference.child("emergencyContacts").child("contact1").setValue(contact1Text);
        if (!TextUtils.isEmpty(contact2Text) && isValidPhoneNumber(contact2Text)) {
            databaseReference.child("emergencyContacts").child("contact2").setValue(contact2Text);
        } else if (!TextUtils.isEmpty(contact2Text)) {
            Toast.makeText(this, "Secondary contact must be 10 digits", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!TextUtils.isEmpty(contact3Text) && isValidPhoneNumber(contact3Text)) {
            databaseReference.child("emergencyContacts").child("contact3").setValue(contact3Text);
        } else if (!TextUtils.isEmpty(contact3Text)) {
            Toast.makeText(this, "Tertiary contact must be 10 digits", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save profile completion status to Firebase Realtime Database
        databaseReference.child("profile_completed").setValue(true)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Contacts saved and profile completed status updated successfully", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(EmergencyContactsActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to update profile completion status: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }


    private boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber.matches("\\d{10}");
    }

    private void checkProfileCompletion() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserProfile", Context.MODE_PRIVATE);
        boolean isProfileCompleted = sharedPreferences.getBoolean("profile_completed", false);

        if (isProfileCompleted) {
            // Profile is already completed, handle accordingly
            // You can redirect the user or disable the UI elements as needed
        }
    }
}
