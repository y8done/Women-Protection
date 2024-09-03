package com.example.womenprotection;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class profileDetails extends AppCompatActivity {
    private Calendar calendar;
    private DatePickerDialog datePickerDialog;
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int GALLERY_REQUEST_CODE = 101;
    private EditText birthdateEditText;
    private EditText fullnameEditText;
    private RadioGroup genderGroup;
    private ImageView recentPhoto;
    private Button selectImageButton;
    private Button submitDetailsButton;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private Uri imageUri;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private StorageReference mStorage;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_details);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("users");
        mStorage = FirebaseStorage.getInstance().getReference("profile_pictures");
        progress = findViewById(R.id.progress);
        birthdateEditText = findViewById(R.id.birthdate);
        fullnameEditText = findViewById(R.id.fullname);
        genderGroup = findViewById(R.id.genderGroup);
        recentPhoto = findViewById(R.id.recentPhoto);
        selectImageButton = findViewById(R.id.selectImageButton);
        submitDetailsButton = findViewById(R.id.submitDetailsButton);

        // Check if the profile is already completed
        checkProfileCompletion();

        // Set up the calendar and date picker dialog
        calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    birthdateEditText.setText(date);
                }, year, month, day);

        birthdateEditText.setOnClickListener(v -> datePickerDialog.show());

        // Register the launcher for camera
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        assert data != null;
                        Bitmap image = (Bitmap) data.getExtras().get("data");
                        recentPhoto.setImageBitmap(image);
                        imageUri = getImageUriFromBitmap(image); // Convert Bitmap to Uri
                    }
                });

        // Register the launcher for gallery
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        assert data != null;
                        imageUri = data.getData();
                        recentPhoto.setImageURI(imageUri);
                    }
                });

        selectImageButton.setOnClickListener(v -> showImagePickerDialog());
        submitDetailsButton.setOnClickListener(v -> submitUserDetails());
    }

    private void showImagePickerDialog() {
        String[] options = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Image From");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                openCamera();
            } else if (which == 1) {
                openGallery();
            }
        });
        builder.show();
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(cameraIntent);
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(galleryIntent);
    }

    private void submitUserDetails() {
        String fullName = fullnameEditText.getText().toString();
        String birthdate = birthdateEditText.getText().toString();
        int selectedId = genderGroup.getCheckedRadioButtonId();
        RadioButton selectedRadioButton = findViewById(selectedId);
        String gender = selectedRadioButton != null ? selectedRadioButton.getText().toString() : "";

        // Validate inputs
        if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(birthdate) || recentPhoto.getDrawable() == null) {
            Toast.makeText(this, "All fields are required, including the profile photo.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate age
        String[] dateParts = birthdate.split("/");
        if (dateParts.length == 3) {
            int birthYear = Integer.parseInt(dateParts[2]);
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
            int age = currentYear - birthYear;
            if (age > 120) {
                Toast.makeText(this, "Age cannot be more than 120 years!", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Check if imageUri is not null
        if (imageUri == null) {
            Toast.makeText(profileDetails.this, "Please select a profile photo.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress
        progress.setVisibility(View.VISIBLE);

        // Upload the image to Firebase Storage
        StorageReference fileRef = mStorage.child("profile_pictures/" + mAuth.getCurrentUser().getUid() + ".jpg");
        fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot ->
                fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();

                    // Prepare user details
                    Map<String, Object> userDetails = new HashMap<>();
                    userDetails.put("fullName", fullName);
                    userDetails.put("birthDate", birthdate);
                    userDetails.put("gender", gender);
                    userDetails.put("photoUrl", imageUrl);

                    // Update user details in Firebase Realtime Database
                    mDatabase.child(mAuth.getCurrentUser().getUid()).updateChildren(userDetails)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    // Redirect to MainActivity
                                    Toast.makeText(profileDetails.this, "Details submitted successfully!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(profileDetails.this, phone_verification.class));
                                    finish();
                                } else {
                                    Toast.makeText(profileDetails.this, "Failed to submit details.", Toast.LENGTH_SHORT).show();
                                }
                                progress.setVisibility(View.GONE);
                            });
                })
        ).addOnFailureListener(e -> {
            progress.setVisibility(View.GONE);
            Toast.makeText(profileDetails.this, "Failed to upload image.", Toast.LENGTH_SHORT).show();
        });
    }

    private void checkProfileCompletion() {
        String userId = mAuth.getCurrentUser().getUid();
        mDatabase.child(userId).child("profile_completed").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Boolean isProfileCompleted = dataSnapshot.getValue(Boolean.class);

                if (Boolean.TRUE.equals(isProfileCompleted)) {
                    // Redirect to MainActivity if the profile is already completed
                    startActivity(new Intent(profileDetails.this, MainActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(profileDetails.this, "Failed to check profile completion status.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Uri getImageUriFromBitmap(Bitmap bitmap) {
        // Convert Bitmap to Uri
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }
}
