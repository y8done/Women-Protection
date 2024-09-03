package com.example.womenprotection;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mapmyindia.sdk.maps.MapmyIndia;
import com.mapmyindia.sdk.maps.MapmyIndiaMap;
import com.mapmyindia.sdk.maps.OnMapReadyCallback;
import com.mapmyindia.sdk.maps.Style;
import com.mapmyindia.sdk.maps.camera.CameraUpdateFactory;
import com.mapmyindia.sdk.maps.geometry.LatLng;
import com.mapmyindia.sdk.maps.location.LocationComponent;
import com.mapmyindia.sdk.maps.location.LocationComponentActivationOptions;
import com.mapmyindia.sdk.maps.location.LocationComponentOptions;
import com.mapmyindia.sdk.maps.location.modes.CameraMode;
import com.mapmyindia.sdk.maps.location.modes.RenderMode;
import com.mapmyindia.sdk.maps.MapView;
import com.mmi.services.account.MapmyIndiaAccountManager;

public class location_sharing extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int SMS_PERMISSION_REQUEST_CODE = 2;

    private MapView mapView;
    private MapmyIndiaMap mapmyIndiaMap;
    private DatabaseReference emergencyContactsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize MapMyIndia SDK
        MapmyIndiaAccountManager.getInstance().setRestAPIKey("b181abe540a6c52b1c9db5de8f077a7a");
        MapmyIndiaAccountManager.getInstance().setMapSDKKey("b181abe540a6c52b1c9db5de8f077a7a");
        MapmyIndiaAccountManager.getInstance().setAtlasClientId("96dHZVzsAusVWRK1OD9yNt-6APg9hmRpDKVFkadtKgb5UcrEMqGVbX9O36jzjjD-yte-AtbA7fa_No3nYs9maw==");
        MapmyIndiaAccountManager.getInstance().setAtlasClientSecret("lrFxI-iSEg_n9EiPF-RoYjoAoP3pRnYREQjqesssD0Z3q39RKIiZTTWSLMH2waLhr3quZy8wqwVaxSxiN1hvBzyJOw0EX8Nd");
        MapmyIndia.getInstance(getApplicationContext());

        setContentView(R.layout.activity_location_sharing);

        mapView = findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);

        // Initialize Firebase Realtime Database
        emergencyContactsRef = FirebaseDatabase.getInstance().getReference("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("emergencyContacts");

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapmyIndiaMap mapmyIndiaMap) {
                location_sharing.this.mapmyIndiaMap = mapmyIndiaMap;
                mapmyIndiaMap.getStyle(style -> {
                    if (style != null) {
                        enableLocation(style);
                    } else {
                        Toast.makeText(location_sharing.this, "Error loading map style.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onMapError(int i, String s) {
                Toast.makeText(location_sharing.this, "Error loading map: " + s, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void enableLocation(@NonNull Style style) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            activateLocationComponent(style);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void activateLocationComponent(@NonNull Style style) {
        if (mapmyIndiaMap != null) {
            LocationComponent locationComponent = mapmyIndiaMap.getLocationComponent();
            LocationComponentActivationOptions locationComponentActivationOptions =
                    LocationComponentActivationOptions.builder(this, style)
                            .locationComponentOptions(
                                    LocationComponentOptions.builder(this)
                                            .trackingGesturesManagement(true)
                                            .accuracyColor(ContextCompat.getColor(this, R.color.mapmyindia_blue))
                                            .build())
                            .build();

            locationComponent.activateLocationComponent(locationComponentActivationOptions);

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                locationComponent.setLocationComponentEnabled(true);
                locationComponent.setCameraMode(CameraMode.TRACKING);
                locationComponent.setRenderMode(RenderMode.COMPASS);

                Location lastKnownLocation = locationComponent.getLastKnownLocation();
                if (lastKnownLocation != null) {
                    LatLng latLng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                    mapmyIndiaMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14.0));
                    sendSms(latLng);
                }
            }
        }
    }

    private void sendSms(LatLng latLng) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            emergencyContactsRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String phoneNumber = null;
                    if (task.getResult().exists()) {
                        // Get the first contact number
                        for (DataSnapshot snapshot : task.getResult().getChildren()) {
                            phoneNumber = snapshot.getValue(String.class);
                            break;
                        }

                        if (phoneNumber != null) {
                            String message = "Emergency! My current location is: https://maps.mapmyindia.com/" + latLng.getLatitude() + "," + latLng.getLongitude();
                            SmsManager smsManager = SmsManager.getDefault();
                            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
                            Toast.makeText(this, "Location shared with emergency contact.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "No emergency contacts found.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Failed to retrieve emergency contacts.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Error retrieving emergency contacts: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS},
                    SMS_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mapmyIndiaMap != null) {
                    mapmyIndiaMap.getStyle(this::enableLocation);
                }
            } else {
                Toast.makeText(this, "Location permission is required to use this feature.", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Location lastKnownLocation = mapmyIndiaMap.getLocationComponent().getLastKnownLocation();
                if (lastKnownLocation != null) {
                    LatLng latLng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                    sendSms(latLng);
                }
            } else {
                Toast.makeText(this, "SMS permission is required to share your location.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
