package com.example.travelbuddy;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Bundle;
import android.util.Log;
import android.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.SupportMapFragment;

public class ThingstodoActivity extends AppCompatActivity {
    private static final String TAG = ThingstodoActivity.class.getSimpleName();
    private SearchView mSearchView;
    private PlacesClient mPlacesClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thingstodo);
        try {
            ApplicationInfo appInfo = getPackageManager().getApplicationInfo(getPackageName(),
                    PackageManager.GET_META_DATA);
            String apiKey = appInfo.metaData.getString("com.google.android.geo.API_KEY");
            Places.initialize(getApplicationContext(), apiKey);
            mPlacesClient = Places.createClient(this);

            // Get a reference to the SearchView widget
            mSearchView = findViewById(R.id.searchView);

            // Set up a listener to handle search queries
            mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    // Perform a Places API search using the user's query
                    performPlaceSearch(query);
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    // Do nothing
                    return false;
                }
            });
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Failed to load meta-data, NameNotFound: " + e.getMessage());
        } catch (NullPointerException e) {
            Log.e(TAG, "Failed to load meta-data, NullPointer: " + e.getMessage());
        }


    }
    private void performPlaceSearch(String query) {
        // Define a bounds for the search request
        LatLngBounds bounds = new LatLngBounds(new LatLng(-90, -180),
                new LatLng(90, 180));

        // Set up a Place Autocomplete request
        FindAutocompletePredictionsRequest request =
                FindAutocompletePredictionsRequest.builder()
                        .setQuery(query)
                        .setLocationBias(RectangularBounds.newInstance(bounds))
                        .setTypeFilter(TypeFilter.ADDRESS)
                        .build();

        mPlacesClient.findAutocompletePredictions(request)
                .addOnSuccessListener((response) -> {
                    // Process the search results
                    List<AutocompletePrediction> predictions = response.getAutocompletePredictions();
                    for (AutocompletePrediction prediction : predictions) {
                        Log.i(TAG, "Place: " + prediction.getPlaceId() + ", " + prediction.getPrimaryText(null));
                    }
                })
                .addOnFailureListener((exception) -> {
                    // Handle the error
                    if (exception instanceof ApiException) {
                        ApiException apiException = (ApiException) exception;
                        Log.e(TAG, "Place not found: " + apiException.getStatusCode());
                    }
                });

    }


}

