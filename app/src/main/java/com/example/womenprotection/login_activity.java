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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class login_activity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private EditText editTextUsername;
    private EditText editTextPassword;
    private Button loginBtn;
    private TextView textView;
    private ProgressBar progress;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        // Initialize UI elements
        progress = findViewById(R.id.progressbar);
        editTextUsername = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        loginBtn = findViewById(R.id.loginButton);
        textView = findViewById(R.id.signupText);

        // Request location permission if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }

        // Navigate to SignupActivity if "Sign up" is clicked
        textView.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
            startActivity(intent);
            finish();
        });

        // Handle login button click
        loginBtn.setOnClickListener(view -> {
            progress.setVisibility(View.VISIBLE);
            String email = editTextUsername.getText().toString();
            String password = editTextPassword.getText().toString();

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(login_activity.this, "Enter the email first!", Toast.LENGTH_SHORT).show();
                progress.setVisibility(View.GONE);
                return;
            }
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(login_activity.this, "Enter a valid password!", Toast.LENGTH_SHORT).show();
                progress.setVisibility(View.GONE);
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        progress.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                checkUserProfile(user.getUid());
                            }
                        } else {
                            Toast.makeText(login_activity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void checkUserProfile(String userId) {
        databaseReference.child(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot dataSnapshot = task.getResult();
                if (dataSnapshot.exists()) {
                    Boolean isProfileCompleted = dataSnapshot.child("profile_completed").getValue(Boolean.class);
                    if (isProfileCompleted != null && isProfileCompleted) {
                        // Profile is completed, navigate to MainActivity
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    } else {
                        // Profile is not completed, navigate to profileDetails
                        startActivity(new Intent(getApplicationContext(), profileDetails.class));
                    }
                } else {
                    // If user data doesn't exist, navigate to profileDetails
                    Toast.makeText(login_activity.this, "User data does not exist.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), profileDetails.class));
                }
                finish();
            } else {
                Toast.makeText(login_activity.this, "Failed to retrieve profile data: " + task.getException(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                Toast.makeText(this, "Location permission granted.", Toast.LENGTH_SHORT).show();
            } else {
                // Permission denied
                Toast.makeText(this, "Location permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
