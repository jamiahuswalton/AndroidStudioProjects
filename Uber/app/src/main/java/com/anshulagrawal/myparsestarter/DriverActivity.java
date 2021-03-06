package com.anshulagrawal.myparsestarter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DriverActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private ListView driverListView;
    private ArrayAdapter<Float> adapter;
    private LocationListener locationListener;
    private LocationManager locationManager;
    private CameraUpdate cameraUpdate;

    private List<Float> distances = new ArrayList<>();
    private List<ParseObject> userRequests = new ArrayList<>();
    ParseUser driver;

    private LatLng driverLatLng;
    private LatLng requestLatLng;

    private double driverLat;
    private double driverLong;
    private String username;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        driver = ParseUser.getCurrentUser();

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                saveDriverLocation(location);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };
        if (Build.VERSION.SDK_INT < 23) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 0, locationListener);
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1, locationListener);
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastKnownLocation != null) {
                    saveDriverLocation(lastKnownLocation);
                }
            }
        }

        driverListView = findViewById(R.id.listView);
        adapter = new ArrayAdapter<Float>(getApplicationContext(), android.R.layout.simple_list_item_1, distances);

        driverListView.setAdapter(adapter);
        driverListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
//
                ParseObject user = userRequests.get(i);
                username = user.getString("user");

                driverLatLng = new LatLng(driverLat, driverLong);
                requestLatLng = new LatLng(user.getDouble("latitude"), user.getDouble("longitude"));
                builder.include(driverLatLng);
                builder.include(requestLatLng);
                LatLngBounds bounds = builder.build();
                Log.i("bounds: ", bounds.toString());
                findViewById(R.id.listviewFrame).setVisibility(View.GONE);
                findViewById(R.id.mapFrame).setVisibility(View.VISIBLE);
                cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 40);

                ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Requests");
                query.whereEqualTo("user", username);
                query.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {
                        if (e == null && objects.size() > 0) {
                            ParseObject obj = objects.get(0);
                            obj.put("driver", ParseUser.getCurrentUser().getUsername());
                            obj.saveInBackground();
                        }
                    }
                });
            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

//        final View activityRootView = findViewById(R.id.mapFrame);
//        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//
//            @Override
//            public void onGlobalLayout() {
//                mMap.addMarker(new MarkerOptions().position(driverLatLng).title("Your location"));
//                mMap.addMarker(new MarkerOptions().position(requestLatLng).title("Rider's location"));
//                mMap.animateCamera(cameraUpdate);
//            }
//        });
        // Another way to do the same thing as below


        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                mMap.addMarker(new MarkerOptions().position(driverLatLng).title("Your location"));
                mMap.addMarker(new MarkerOptions().position(requestLatLng).title("Rider's location"));
                mMap.animateCamera(cameraUpdate);
            }
        });
    }

    public void openGoogleMaps(View view) {
        String mapUrl = new StringBuilder("http://maps.google.com/maps?saddr=").append(driverLat).append(',').
                append(driverLong).append("&addr=").append(requestLatLng.latitude).append(',').append(requestLatLng.longitude).toString();
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?saddr=20.344,34.34&daddr=20.5666,45.345"));
        startActivity(intent);
    }

    private void saveDriverLocation(Location location) {
        driverLat = location.getLatitude();
        driverLong = location.getLongitude();
        driver.put("latitude", driverLat);
        driver.put("longitude", driverLong);
        driver.saveInBackground();
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Requests");
        query.setLimit(10);
        query.whereDoesNotExist("driver");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null && objects.size() > 0) {
                    Log.i("Driver's Latitude", Double.toString(driverLat));
                    Log.i("Driver's Longitude", Double.toString(driverLong));
                    userRequests = objects;
                    for (ParseObject object : objects) {
                        float[] distanceResults = new float[1];
                        Location.distanceBetween(driverLat, driverLong, object.getDouble("latitude"), object.getDouble("longitude"), distanceResults);
                        distances.add((float) (Math.round(distanceResults[0] / 100) / 10.0));
                    }
                    Collections.sort(distances);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }
}
