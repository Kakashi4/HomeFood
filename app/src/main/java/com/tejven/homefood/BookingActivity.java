package com.tejven.homefood;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.TaskStackBuilder;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;


import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;



import android.util.Log;

import java.util.Arrays;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;




import android.os.Looper;
import android.provider.Settings;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class BookingActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    int PERMISSION_ID = 44;
    private FusedLocationProviderClient mFusedLocationClient;

    Location CurrentLoc;

    Location Location_final;

    ImageButton current_button;

    EditText phone;
    Button default_btn, current_loc, order;
    RadioButton size;
    RadioGroup ordersize;
    Map<String, Object> user_deets = new HashMap<>();
    Map<String, Object> booking_deets = new HashMap<>();
    Spinner size_spinner;
    PlacesClient placesClient;
    Map<String, Integer> order_status = new HashMap<>();



    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        booking_deets.put("Package size", pos + 1);
    }

    public void onNothingSelected(AdapterView<?> parent) {
        booking_deets.put("Package size", 1);
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.veg:
                if (checked)
                    booking_deets.put("Veg/Nonveg", 0);
                    break;
            case R.id.nonveg:
                if (checked)
                    booking_deets.put("Veg/Nonveg", 1);
                    break;
        }
    }
    public void onRadioButtonClicked2(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.regular:
                if (checked)
                    booking_deets.put("Regular", 1);
                break;
            case R.id.occasional:
                if (checked)
                    booking_deets.put("Regular", 0);
                break;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);
        final FirebaseFirestore db;
        db = FirebaseFirestore.getInstance();

        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //code for picker
        String apiKey= "AIzaSyC8rAf70dQ8tXgwo61bnFoR9kw_Fzbxn-E";

        if (!Places.isInitialized()) {
            Places.initialize(BookingActivity.this, apiKey);
        }
// Retrieve a PlacesClient (previously initialized - see MainActivity)
        placesClient = Places.createClient(this);
        Location_final = new Location("final");
        final AutocompleteSupportFragment autocompleteSupportFragment =
                (AutocompleteSupportFragment)
                        getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        assert autocompleteSupportFragment != null;
        autocompleteSupportFragment.setHint("Enter Location");
        autocompleteSupportFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG,Place.Field.NAME));

        autocompleteSupportFragment.setOnPlaceSelectedListener(
                new PlaceSelectionListener() {
                    @Override
                    public void onPlaceSelected(@NonNull Place place) {
                        final LatLng latLng = place.getLatLng();

                        Log.i("PlacesApi", "onPlaceSelected: "+latLng.latitude+ "\n"+latLng.longitude);
                        Location_final.setLatitude(place.getLatLng().latitude);
                        Location_final.setLongitude(place.getLatLng().longitude);
                        //Toast.makeText(UpdateProfileActivity.this, ""+latLng.latitude, Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onError(Status status) {
                        Toast.makeText(BookingActivity.this, ""+status.getStatusMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        current_button = findViewById(R.id.current_button);

        current_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLastLocation();
                Location_final.setLatitude(CurrentLoc.getLatitude());
                Location_final.setLongitude(CurrentLoc.getLongitude());
                Toast.makeText(BookingActivity.this, "Using Current Location...", Toast.LENGTH_SHORT).show();

            }
        });



        size_spinner = findViewById(R.id.package_size);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(BookingActivity.this,
                R.array.size_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        size_spinner.setAdapter(adapter);
        phone = findViewById(R.id.phone);
        default_btn = findViewById(R.id.usedefaults);
        order = findViewById(R.id.orderplace);
        booking_deets.put("Veg/Nonveg", 0);
        booking_deets.put("Regular", 0);



        final String name = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();

        size_spinner.setOnItemSelectedListener(this);

        default_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DocumentReference docRef = db.collection("users").document(uid);
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                user_deets = document.getData();
                                phone.setText(user_deets.get("Default number").toString());
                                Location_final.setLatitude(Double.parseDouble(user_deets.get("Default Location Latitude").toString()));
                                Location_final.setLongitude(Double.parseDouble(user_deets.get("Default Location Longitude").toString()));

                            } else {
                                Toast.makeText(BookingActivity.this, "No default values set", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(BookingActivity.this, "Error in retrieving values", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });



        order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Location_final != null && !phone.getText().toString().isEmpty()) {
                    booking_deets.put("Location Latitude", Location_final.getLatitude());
                    booking_deets.put("Location Longitude", Location_final.getLongitude());
                    booking_deets.put("Phone", phone.getText().toString());
                    booking_deets.put("Name", name);
                    order_status.put("Order Status", 0);
                    db.collection("bookings")
                            .document(uid)
                            .set(booking_deets)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(BookingActivity.this, "Booking details uploaded succesfully", Toast.LENGTH_SHORT).show();
                                    db.collection("bookings")
                                            .document(uid)
                                            .set(order_status, SetOptions.merge())
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Intent intent = new Intent(BookingActivity.this, PricingOTPActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(BookingActivity.this, "Error in booking: " + e, Toast.LENGTH_SHORT).show();
                                }
                            });

                }
                else {
                    Toast.makeText(BookingActivity.this, "Some fields are empty, please check", Toast.LENGTH_SHORT).show();
                }
            }
            });


    }

    @SuppressLint("MissingPermission")
    private void getLastLocation(){
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.getLastLocation().addOnCompleteListener(
                        new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                Location location = task.getResult();
                                if (location == null) {
                                    requestNewLocationData();
                                } else {
                                    CurrentLoc=location;

                                }
                            }
                        }
                );
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            requestPermissions();
        }
    }


    @SuppressLint("MissingPermission")
    private void requestNewLocationData(){

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(0);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
        );

    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            CurrentLoc=mLastLocation;

        }
    };

    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSION_ID
        );
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        if (checkPermissions()) {
            getLastLocation();
        }

    }
}
