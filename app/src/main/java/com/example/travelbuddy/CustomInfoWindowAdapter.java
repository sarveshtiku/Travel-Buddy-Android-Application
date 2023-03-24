package com.example.travelbuddy;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
    private final View mWindow;
    private final Context mContext;

    public CustomInfoWindowAdapter(Context context) {
        mContext = context;
        mWindow = LayoutInflater.from(context).inflate(R.layout.trip_add_popup, null);
    }

    public Button getAddToTripButton(){
        return mWindow.findViewById(R.id.add_to_trip_button);
    }

    private void renderWindowText(Marker marker, View view) {
        String title = marker.getTitle();
        TextView titleTextView = view.findViewById(R.id.title);


        titleTextView.setText(title);
        

        String snippet = marker.getSnippet();
        TextView snippetTextView = view.findViewById(R.id.snippet);

        snippetTextView.setText(snippet);

    }

    @Override
    public View getInfoWindow(Marker marker) {
        renderWindowText(marker, mWindow);
        return mWindow;
    }

    @Override
    public View getInfoContents(Marker marker) {
        renderWindowText(marker, mWindow);
        return mWindow;
    }
}