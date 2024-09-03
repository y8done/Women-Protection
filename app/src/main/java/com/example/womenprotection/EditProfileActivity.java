package com.example.womenprotection;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditProfileActivity extends AppCompatActivity {

    private EditText editFullName, editEmail, editPhoneNumber, editBirthdate, editEmergencyContact;
    private RadioGroup editGenderGroup;
    private Button saveChangesButton;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize Firebase components
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        // Initialize UI components
        editFullName = findViewById(R.id.editFullName);
        editEmail = findViewById(R.id.editEmail);
        editPhoneNumber = findViewById(R.id.editPhoneNumber);
        editBirthdate = findViewById(R.id.editBirthdate);
        editEmergencyContact = findViewById(R.id.editEmergencyContact);
        editGenderGroup = findViewById(R.id.editGenderGroup);
        saveChangesButton = findViewById(R.id.saveChangesButton);

        // Load current details into fields
        loadCurrentUserDetails();

        // Save changes button click listener
        saveChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveProfileChanges();
            }
        });
    }

    private void loadCurrentUserDetails() {
        // Method to load user details from Firebase Realtime Database
        String uid = mAuth.getCurrentUser().getUid();
        databaseReference.child(uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                    // Populate fields with data from Firebase
                    String fullName = task.getResult().child("fullName").getValue(String.class);
                    String email = task.getResult().child("email").getValue(String.class);
                    String phoneNumber = task.getResult().child("phoneNumber").getValue(String.class);
                    String birthdate = task.getResult().child("birthdate").getValue(String.class);
                    String gender = task.getResult().child("gender").getValue(String.class);
                    String emergencyContact = task.getResult().child("emergencyContact").getValue(String.class);

                    editFullName.setText(fullName);
                    editEmail.setText(email);
                    editPhoneNumber.setText(phoneNumber);
                    editBirthdate.setText(birthdate);
                    editEmergencyContact.setText(emergencyContact);

                    if ("Male".equals(gender)) {
                        editGenderGroup.check(R.id.editMaleRadioButton);
                    } else if ("Female".equals(gender)) {
                        editGenderGroup.check(R.id.editFemaleRadioButton);
                    }
                } else {
                    Toast.makeText(EditProfileActivity.this, "No data found.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(EditProfileActivity.this, "Error loading data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveProfileChanges() {
        String uid = mAuth.getCurrentUser().getUid();

        // Get updated details from the input fields
        String fullName = editFullName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String phoneNumber = editPhoneNumber.getText().toString().trim();
        String birthdate = editBirthdate.getText().toString().trim();
        String emergencyContact = editEmergencyContact.getText().toString().trim();
        int selectedGenderId = editGenderGroup.getCheckedRadioButtonId();
        RadioButton selectedGenderRadioButton = findViewById(selectedGenderId);
        String gender = (selectedGenderRadioButton != null) ? selectedGenderRadioButton.getText().toString() : "";

        // Validate input
        if (fullName.isEmpty() || email.isEmpty() || phoneNumber.isEmpty() || birthdate.isEmpty() || emergencyContact.isEmpty() || gender.isEmpty()) {
            Toast.makeText(this, "Please fill all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a user object
        UserProfile userProfile = new UserProfile(fullName, email, phoneNumber, birthdate, gender, emergencyContact);

        // Save the updated details to Firebase Realtime Database
        databaseReference.child(uid).setValue(userProfile).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(EditProfileActivity.this, "Profile updated successfully.", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(EditProfileActivity.this, MainActivity.class));
            } else {
                Toast.makeText(EditProfileActivity.this, "Error updating profile.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
