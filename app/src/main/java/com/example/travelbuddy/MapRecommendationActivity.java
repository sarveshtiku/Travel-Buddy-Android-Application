package com.example.travelbuddy;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;
//import com.google.android.libraries.places.api.model.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.Place.Field;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.model.LocationBias;
import com.google.android.libraries.places.api.model.LocationRestriction;

import java.util.Arrays;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MapRecommendationActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final String PLACES_API_BASE_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json";

    private GoogleMap mMap;
    private String category;
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
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        requestLocationPermissions();
        // Use the Places API to search for nearby places based on the selected category
        // and add markers to the map
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
                fetchNearbyPlaces(currentLatLng);

            } else {
                Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void fetchNearbyPlaces(LatLng currentLatLng) {
        List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);

        bounds = RectangularBounds.newInstance(currentLatLng, currentLatLng);
        typeFilter = TypeFilter.valueOf(category.toUpperCase());


//        FindCurrentPlaceRequest request = FindCurrentPlaceRequest.builder(placeFields)
//                .build();

        // Check if the category is valid
        TypeFilter typeFilter = null;
        try {
            typeFilter = TypeFilter.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        FindCurrentPlaceRequest request = FindCurrentPlaceRequest.builder(placeFields)
                .setLocationBias(bounds)
                .setTypeFilter(typeFilter)
                .build();


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermissions();
            return;
        }

        Task<FindCurrentPlaceResponse> placeResponseTask = placesClient.findCurrentPlace(request);
        placeResponseTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FindCurrentPlaceResponse response = task.getResult();
                for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
                    Place place = placeLikelihood.getPlace();
                    LatLng placeLatLng = place.getLatLng();
                    if (placeLatLng != null) {
                        String markerTitle = place.getName() + ": " + place.getAddress();
                        mMap.addMarker(new MarkerOptions().position(placeLatLng).title(markerTitle));
                    }
                }
            } else {
                Exception exception = task.getException();
                if (exception instanceof ApiException) {
                    ApiException apiException = (ApiException) exception;
                    Toast.makeText(this, "Error fetching nearby places: " + apiException.getStatusCode(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


}
