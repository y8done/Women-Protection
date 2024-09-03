package com.example.womenprotection;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.mapmyindia.sdk.maps.MapView;
import com.mapmyindia.sdk.maps.MapmyIndia;
import com.mapmyindia.sdk.maps.MapmyIndiaMap;
import com.mapmyindia.sdk.maps.OnMapReadyCallback;
import com.mapmyindia.sdk.maps.annotations.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.mapmyindia.sdk.maps.geometry.LatLng;
import com.mmi.services.account.MapmyIndiaAccountManager;

public class HotspotMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private MapmyIndiaMap mapmyIndiaMap;
    private DatabaseReference hotspotsDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapmyIndiaAccountManager.getInstance().setRestAPIKey("b181abe540a6c52b1c9db5de8f077a7a");
        MapmyIndiaAccountManager.getInstance().setMapSDKKey("b181abe540a6c52b1c9db5de8f077a7a");
        MapmyIndiaAccountManager.getInstance().setAtlasClientId("96dHZVzsAusVWRK1OD9yNt-6APg9hmRpDKVFkadtKgb5UcrEMqGVbX9O36jzjjD-yte-AtbA7fa_No3nYs9maw==");
        MapmyIndiaAccountManager.getInstance().setAtlasClientSecret("lrFxI-iSEg_n9EiPF-RoYjoAoP3pRnYREQjqesssD0Z3q39RKIiZTTWSLMH2waLhr3quZy8wqwVaxSxiN1hvBzyJOw0EX8Nd");

        // Initialize MapMyIndia SDK
        MapmyIndia.getInstance(this);
        setContentView(R.layout.activity_hotspot_map);

        mapView = findViewById(R.id.mapView); // Ensure the ID matches your layout
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        hotspotsDatabaseRef = FirebaseDatabase.getInstance().getReference("hotspots");
    }

    @Override
    public void onMapReady(@NonNull MapmyIndiaMap mapmyIndiaMap) {
        this.mapmyIndiaMap = mapmyIndiaMap;
        fetchAndDisplayHotspots();
    }

    @Override
    public void onMapError(int i, String s) {

    }

    private void fetchAndDisplayHotspots() {
        hotspotsDatabaseRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Double latitude = snapshot.child("latitude").getValue(Double.class);
                    Double longitude = snapshot.child("longitude").getValue(Double.class);
                    String name = snapshot.child("name").getValue(String.class);
                    String description = snapshot.child("description").getValue(String.class);

                    if (latitude != null && longitude != null) {
                        mapmyIndiaMap.addMarker(new MarkerOptions()
                                .position(new LatLng(latitude, longitude))
                                .title(name != null ? name : "Unknown Location")
                                .snippet(description != null ? description : ""));
                    }
                }
                // Optionally, move camera to the first hotspot
                if (dataSnapshot.getChildren().iterator().hasNext()) {
                    DataSnapshot firstSnapshot = dataSnapshot.getChildren().iterator().next();
                    Double lat = firstSnapshot.child("latitude").getValue(Double.class);
                    Double lng = firstSnapshot.child("longitude").getValue(Double.class);
                    if (lat != null && lng != null) {
                        mapmyIndiaMap.moveCamera(com.mapmyindia.sdk.maps.camera.CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 12));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(HotspotMapActivity.this, "Failed to load hotspots", Toast.LENGTH_SHORT).show();
                Log.e("HotspotMapActivity", "Error loading hotspots", databaseError.toException());
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
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
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
}
