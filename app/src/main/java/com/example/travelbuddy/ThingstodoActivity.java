package com.example.travelbuddy;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Bundle;
import android.util.Log;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.Arrays;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

public class ThingstodoActivity extends AppCompatActivity {


    PlacesClient placesClient;
    private RecyclerView recyclerView;
    private ThingsToDoCategoryAdapter thingsToDoCategoryAdapter;
    private List<String> categories;

    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thingstodo);
        try {
            ApplicationInfo appInfo = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            String apiKey = appInfo.metaData.getString("com.google.android.geo.API_KEY");
            Places.initialize(getApplicationContext(), apiKey);

            placesClient = Places.createClient(this);

            final AutocompleteSupportFragment autocompleteSupportFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

            autocompleteSupportFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME));
            autocompleteSupportFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onError(@NonNull Status status) {

                }

                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    final LatLng latLng = place.getLatLng();
                    Log.i("on place selected", "wow" + latLng.latitude);
                }
            });

        }
        catch (PackageManager.NameNotFoundException e) {
            Log.e("TAG", "Failed to load meta-data, NameNotFound: " + e.getMessage());
        } catch (NullPointerException e) {
            Log.e("TAG", "Failed to load meta-data, NullPointer: " + e.getMessage());
        }
        List<String> categories = Arrays.asList(
                "accounting", "airport", "amusement_park", "aquarium", "art_gallery", "atm", "bakery", "bank", "bar", "beauty_salon", "bicycle_store", "book_store", "bowling_alley", "bus_station", "cafe", "campground", "car_dealer", "car_rental", "car_repair", "car_wash", "casino", "cemetery", "church", "city_hall", "clothing_store", "convenience_store", "courthouse", "dentist", "department_store", "doctor", "drugstore", "electrician", "electronics_store", "embassy", "fire_station", "florist", "funeral_home", "furniture_store", "gas_station", "gym", "hair_care", "hardware_store", "hindu_temple", "home_goods_store", "hospital", "insurance_agency", "jewelry_store", "laundry", "lawyer", "library", "light_rail_station", "liquor_store", "local_government_office", "locksmith", "lodging", "meal_delivery", "meal_takeaway", "mosque", "movie_rental", "movie_theater", "moving_company", "museum", "night_club", "painter", "park", "parking", "pet_store", "pharmacy", "physiotherapist", "plumber", "police", "post_office", "primary_school", "real_estate_agency", "restaurant", "roofing_contractor", "rv_park", "school", "secondary_school", "shoe_store", "shopping_mall", "spa", "stadium", "storage", "store", "subway_station", "supermarket", "synagogue", "taxi_stand", "tourist_attraction", "train_station", "transit_station", "travel_agency", "university", "veterinary_care", "zoo"
        );

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true); // Set this if your RecyclerView items have a fixed size

        ThingsToDoCategoryAdapter adapter = new ThingsToDoCategoryAdapter(categories, new ThingsToDoCategoryAdapter.OnCategoryClickListener() {
            @Override
            public void onCategoryClick(int position) {
                String selectedCategory = categories.get(position);
                Intent intent = new Intent(ThingstodoActivity.this, MapRecommendationActivity.class);
                intent.putExtra("category", selectedCategory);
                ThingstodoActivity.this.startActivity(intent);
            }

        });
        recyclerView.setAdapter(adapter);}


}

