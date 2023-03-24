package com.example.travelbuddy;

import static android.content.ContentValues.TAG;
import com.example.travelbuddy.CustomInfoWindowAdapter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.content.pm.ApplicationInfo;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.tasks.Task;

import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.PlacesClient;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MapRecommendationActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final String PLACES_API_BASE_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json";

    private GoogleMap mMap;
    private String category;
    CustomInfoWindowAdapter customInfoWindowAdapter;
    PlacesClient placesClient;

    RectangularBounds bounds;
    TypeFilter typeFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_recommendation);

        // Get the intent and the category
        Intent intent = getIntent();
        category = intent.getStringExtra("category");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }

    @SuppressLint("PotentialBehaviorOverride")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        requestLocationPermissions();

        assert mMap != null;
        customInfoWindowAdapter = new CustomInfoWindowAdapter(this);
        mMap.setInfoWindowAdapter(customInfoWindowAdapter);

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                // Do nothing since we will handle button clicks separately
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                marker.showInfoWindow();
                Button addToTripButton = customInfoWindowAdapter.getAddToTripButton();

                if (addToTripButton != null) {
                    addToTripButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!marker.getTitle().equals("You are here")) {
                                addToMyTrips(new TripPlace(marker.getTitle(), marker.getSnippet(), marker.getPosition()));
                            }
                        }
                    });
                }

                return true;
            }
        });
    }
    private void addToMyTrips(TripPlace tripPlace) {
        // Your implementation to save the tripPlace to the user's trips
        Toast.makeText(this, "Added " + tripPlace.getName() + " to your trip", Toast.LENGTH_SHORT).show();
    }


    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private void requestLocationPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private FusedLocationProviderClient fusedLocationClient;

    private void getCurrentLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermissions();
            return;
        }

        Task<Location> locationTask = fusedLocationClient.getLastLocation();
        locationTask.addOnSuccessListener(location -> {
            if (location != null) {
                // Use the user's current location
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                LatLng currentLatLng = new LatLng(latitude, longitude);

                // Add a marker on the user's current location
                mMap.addMarker(new MarkerOptions().position(currentLatLng).title("You are here"));

                // Move the camera to the user's current location
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                // fetchNearbyPlaces(currentLatLng);
                try {
                    getNearbyPlacesBasedOnCategory(latitude, longitude);
                } catch (PackageManager.NameNotFoundException e) {
                    throw new RuntimeException(e);
                }

            } else {
                requestLocationPermissions();
                Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getNearbyPlacesBasedOnCategory(double latitude, double longitude) throws PackageManager.NameNotFoundException {
        LatLng currentLatLng = new LatLng(latitude, longitude);
        double radiusInMeters = 5000; // 5 km

        // Use the Nearby Search API to search for nearby restaurants
        ApplicationInfo appInfo = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
        String apiKey = appInfo.metaData.getString("com.google.android.geo.API_KEY");
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" +
                currentLatLng.latitude + "," + currentLatLng.longitude +
                "&radius=" + radiusInMeters +
                "&type=" + category +
                "&key=" + apiKey;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }

                String responseBody = response.body().string();
                try {
                    JSONObject json = new JSONObject(responseBody);
                    JSONArray results = json.getJSONArray("results");
                    for (int i = 0; i < results.length(); i++) {
                        JSONObject result = results.getJSONObject(i);
                        JSONObject location = result.getJSONObject("geometry").getJSONObject("location");
                        String name = result.getString("name");
                        String address = result.getString("vicinity");
                        double lat = location.getDouble("lat");
                        double lng = location.getDouble("lng");
                        LatLng placeLatLng = new LatLng(lat, lng);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mMap.addMarker(new MarkerOptions()
                                        .position(placeLatLng)
                                        .title(name)
                                        .snippet(address));
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}