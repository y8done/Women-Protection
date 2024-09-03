package com.example.womenprotection;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

public class phone_verification extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private EditText countryCode, phoneNumber, otpCode;
    private Button sendOtpButton, verifyOtpButton;
    private ProgressBar progressBar;
    private String verificationId;

    private FirebaseAuth mAuth;
    private DatabaseReference usersDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_verification);

        countryCode = findViewById(R.id.countryCode);
        phoneNumber = findViewById(R.id.phoneNumber);
        otpCode = findViewById(R.id.otpCode);
        sendOtpButton = findViewById(R.id.sendOtpButton);
        verifyOtpButton = findViewById(R.id.verifyOtpButton);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        mAuth = FirebaseAuth.getInstance();
        usersDatabaseRef = FirebaseDatabase.getInstance().getReference("users");

        // Request location permission if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }

        sendOtpButton.setOnClickListener(v -> {
            String code = countryCode.getText().toString().trim();
            String phone = phoneNumber.getText().toString().trim();
            if (isValidPhoneNumber(code, phone)) {
                String fullPhoneNumber = "+" + code + phone;
                sendVerificationCode(fullPhoneNumber);
            } else {
                Toast.makeText(phone_verification.this, "Please enter a valid country code and 10-digit phone number.", Toast.LENGTH_LONG).show();
            }
        });

        verifyOtpButton.setOnClickListener(v -> {
            String code = otpCode.getText().toString().trim();
            if (!TextUtils.isEmpty(code)) {
                verifyCode(code);
            } else {
                Toast.makeText(phone_verification.this, "Please enter the OTP.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean isValidPhoneNumber(String countryCode, String phoneNumber) {
        return countryCode.matches("\\d{1,3}") && phoneNumber.matches("\\d{10}");
    }

    private void sendVerificationCode(String phoneNumber) {
        progressBar.setVisibility(View.VISIBLE);
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallbacks)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(phone_verification.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

                @Override
                public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                    super.onCodeSent(verificationId, token);
                    phone_verification.this.verificationId = verificationId;
                    progressBar.setVisibility(View.GONE);
                    otpCode.setVisibility(View.VISIBLE);
                    verifyOtpButton.setVisibility(View.VISIBLE);
                }
            };

    private void verifyCode(String code) {
        FirebaseUser user = mAuth.getCurrentUser();
        progressBar.setVisibility(View.VISIBLE);
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {

                        if (user != null) {
                            // Save phone number to Realtime Database
                            String phoneNumber = "+" + countryCode.getText().toString().trim() + this.phoneNumber.getText().toString().trim();
                            usersDatabaseRef.child(user.getUid()).child("phoneNumber").setValue(phoneNumber)
                                    .addOnCompleteListener(dbTask -> {
                                        progressBar.setVisibility(View.GONE);
                                        if (dbTask.isSuccessful()) {
                                            Toast.makeText(phone_verification.this, "OTP verified and phone number saved successfully!", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(phone_verification.this, EmergencyContactsActivity.class));
                                            finish();
                                        } else {
                                            Toast.makeText(phone_verification.this, "Failed to save phone number: " + dbTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(phone_verification.this, "OTP verification failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
    private void checkProfileCompletion() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            usersDatabaseRef.child(user.getUid()).child("profile_completed").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Boolean profileCompleted = dataSnapshot.getValue(Boolean.class);
                    if (profileCompleted != null && profileCompleted) {
                        // Redirect to main activity if profile is completed
                        startActivity(new Intent(phone_verification.this, MainActivity.class));
                        finish(); // Close the current activity
                    } else {
                        // Continue showing the phone verification activity
                        otpCode.setVisibility(View.VISIBLE);
                        verifyOtpButton.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(phone_verification.this, "Error checking profile status: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Location permission granted.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Location permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
