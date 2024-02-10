package com.example.project;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.example.project.databinding.ActivityMapsBinding;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    MarkerOptions marker;
    Vector<MarkerOptions> markerOptions;

    LatLng alorsetar;

    private String URL = "https://exposable-jewels.000webhostapp.com/all.php";
    RequestQueue requestQueue;
    Gson gson;
    Healthcare[] Healthcares;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        gson = new GsonBuilder().create();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        markerOptions = new Vector<>();

        alorsetar = new LatLng(6.12, 100.3755);


        Button btnShowNearbyHealthcare = findViewById(R.id.btnShowNearbyHealthcare);
        btnShowNearbyHealthcare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNearbyHealthcare();
            }
        });
    }

    private void showCurrentUserLocation() {
        if (isLocationPermissionGranted()) {
            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null && mMap != null) {
                                LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

                                // Update the user's location in the database
                                updateUserLocation(currentLocation.latitude, currentLocation.longitude);

                                // Move the camera to the current location
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 10));

                                // Save the user's username and current location in SharedPreferences
                                SharedPreferences sharedPref = getSharedPreferences("userSession", Context.MODE_PRIVATE);
                                String username = sharedPref.getString("username", null);
                                if (username != null) {
                                    saveUserUsername(username, currentLocation.latitude, currentLocation.longitude);
                                }
                            }
                        }
                    });
        } else {
            // If permission is not granted, request it
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 200);
        }
    }

    private boolean isLocationPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        for (MarkerOptions mark : markerOptions) {
            mMap.addMarker(mark);
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(alorsetar, 8));
        enableMyLocation();
        sendRequest();
        showCurrentUserLocation();

    }

    private void enableMyLocation() {
        String[] perms = {"android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_NETWORK_STATE"};
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
                Log.d("project2", "permission granted");
            }
        } else {
            Log.d("project2", "permission denied");
            ActivityCompat.requestPermissions(this, perms, 200);
        }
    }

    public void sendRequest() {
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL, onSuccess, onError);

        requestQueue.add(stringRequest);
    }

    public Response.Listener<String> onSuccess = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            Healthcares = gson.fromJson(response, Healthcare[].class);

            Log.e("Healthcare", "Number of Healthcare Data Point : " + Healthcares.length);

            if (Healthcares.length < 1) {
                Toast.makeText(getApplicationContext(), "Problem retrieving JSON data", Toast.LENGTH_LONG).show();
                return;
            }

            for (Healthcare info : Healthcares) {
                Double lat = Double.parseDouble(info.lat);
                Double lng = Double.parseDouble(info.lng);
                String title = info.name;
                String snippet = info.state;

                MarkerOptions marker = new MarkerOptions().position(new LatLng(lat, lng))
                        .title(title)
                        .snippet(snippet)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));

                mMap.addMarker(marker);
            }
        }
    };

    public Response.ErrorListener onError = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
        }
    };

    public void showNearbyHealthcare() {
        if (mMap != null && mMap.getMyLocation() != null) {
            LatLng myLocation = new LatLng(mMap.getMyLocation().getLatitude(), mMap.getMyLocation().getLongitude());
            mMap.clear(); // Clear existing markers



            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 8));

            int markerCount = 0; // Counter variable to limit the number of markers

            Vector<MarkerOptions> nearbyMarkers = new Vector<>();

            for (Healthcare info : Healthcares) {
                LatLng healthcareLocation = new LatLng(Double.parseDouble(info.lat), Double.parseDouble(info.lng));

                // Calculate distance between user location and healthcare location
                double distance = distanceBetweenTwoPoints(myLocation, healthcareLocation);

                if (distance <= 80000.0 && markerCount < 15) { // 80 km in meters
                    String title = info.name;
                    String snippet = info.state;

                    MarkerOptions marker = new MarkerOptions().position(healthcareLocation)
                            .title(title)
                            .snippet(snippet)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));

                    nearbyMarkers.add(marker);
                    markerCount++;
                }
            }

            // Add nearby healthcare markers to the map
            for (MarkerOptions mark : nearbyMarkers) {
                mMap.addMarker(mark);
            }
        } else {
            Toast.makeText(getApplicationContext(), "Unable to retrieve current location", Toast.LENGTH_SHORT).show();
        }
    }

    private double distanceBetweenTwoPoints(LatLng point1, LatLng point2) {
        // Using Haversine formula to calculate distance between two LatLng points
        double R = 6371000; // Radius of the Earth in meters
        double dLat = Math.toRadians(point2.latitude - point1.latitude);
        double dLon = Math.toRadians(point2.longitude - point1.longitude);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(point1.latitude)) * Math.cos(Math.toRadians(point2.latitude)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }
    private void updateUserLocation(double latitude, double longitude) {
        SharedPreferences sharedPref = getSharedPreferences("userSession", Context.MODE_PRIVATE);
        String username = sharedPref.getString("username", null);

        if (username != null) {
            String updateLocationURL = "https://exposable-jewels.000webhostapp.com/update_location.php";

            StringRequest locationRequest = new StringRequest(Request.Method.POST, updateLocationURL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            // Handle response if needed
                            Log.d("LocationUpdate", "Location updated successfully");
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // Handle error if needed
                            Log.e("LocationUpdate", "Error updating location: " + error.getMessage());
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() {
                    // Add parameters to the request
                    Map<String, String> params = new HashMap<>();
                    params.put("username", username);
                    params.put("latitude", String.valueOf(latitude));
                    params.put("longitude", String.valueOf(longitude));
                    return params;
                }
            };

            requestQueue.add(locationRequest);
        }
    }
    private void saveUserUsername(String username, double latitude, double longitude) {
        SharedPreferences sharedPref = getSharedPreferences("userSession", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("username", username);
        editor.putString("latitude", String.valueOf(latitude));
        editor.putString("longitude", String.valueOf(longitude));
        editor.apply();
    }


}

