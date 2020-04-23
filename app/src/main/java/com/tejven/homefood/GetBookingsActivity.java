package com.tejven.homefood;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import java.util.HashMap;
import java.util.Map;
import java.lang.*;

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
import androidx.core.content.ContextCompat;

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
import java.lang.Math;
import java.util.Random;


public class GetBookingsActivity extends AppCompatActivity {
    int count = 0;
    int PERMISSION_ID = 44;
    private FusedLocationProviderClient mFusedLocationClient;
    String booking_id;
    String user_name;
    double booking_dist;
    int i;
    Long veg, size;
    double price;
    Location CurrentLoc;
    Location Booking = new Location("dummy");
    Map<String, String> dr_bookings = new HashMap<>();
    Map<String, String> booking_set = new HashMap<>();
    Button send_otp;
    TextView book1, book2, book3, book4, book5;

    int iter=5;
    int numused=iter;
    String[] numArray;
    int otp;


    private Button btnSms;
    int MY_PERMISSIONS_REQUEST_SEND_SMS=1;

    String SENT = "SMS_SENT!";
    String DELIVERED = "SMS_DELIVERED";
    PendingIntent sentPI, deliveredPI;
    BroadcastReceiver smsSentReceiver,smsDeliveredReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_bookings);

        book1 = findViewById(R.id.textView2);
        book2 = findViewById(R.id.textView3);
        book3 = findViewById(R.id.textView4);
        book4 = findViewById(R.id.textView5);
        book5 = findViewById(R.id.textView6);
        send_otp = findViewById(R.id.btn_sendotp);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();

        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //TODO: make variables for user location, booking location and maximum distance
        final double max_dist = 10;



        db.collection("bookings")
                .whereEqualTo("Order Status", 0).whereEqualTo("Regular", 0)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot document : task.getResult()) {
                                String id = document.getId();
                                Log.d("check", id);
                                //TODO: get longitude and latitude and use distance matrix output in if statement

                                Double book_lat = (Double) document.get("Location Latitude");
                                Double book_long = (Double) document.get("Location Longitude");
                                veg = (Long) document.get(FieldPath.of("Veg/Nonveg"));
                                size = (Long) document.get("Package size");
                                user_name = (String) document.get("Name");
                                Log.d("locations", book_lat.toString() + "," + book_long.toString());
                                Booking.setLatitude(book_lat);
                                Booking.setLongitude(book_long);
                                float distance = CurrentLoc.distanceTo(Booking);
                                distance = distance/1000;
                                Log.d("locations", "e" + distance);
                                if (distance <= max_dist) {
                                    count = count + 1;
                                    dr_bookings.put("booking" + count, id);
                                    dr_bookings.put("booking" + count + "dist", String.valueOf(distance));
                                    dr_bookings.put("count", String.valueOf(count));
                                    db.collection("drivers").document(uid)
                                            .set(dr_bookings, SetOptions.merge());
                                    booking_set.put("driver", uid);
                                    booking_set.put("distance", String.valueOf(distance));
                                    price = (80 + distance * 5e-3 * size +  20 * size) * (veg * 0.2 + 1);
                                    price = Math.round(price);
                                    Log.d("prices", String.valueOf(price));
                                    Map <String, Double> prices = new HashMap<>();
                                    prices.put("price", price);
                                    db.collection("prices").document(id)
                                            .set(prices);
                                    db.collection("bookings").document(id)
                                            .set(booking_set, SetOptions.merge());
                                    db.collection("bookings").document(id)
                                            .set(prices, SetOptions.merge());

                                    switch (count){
                                        case 1:
                                            book1.setText("Name: " + user_name + "\n" + "Distance: " + Math.round(distance) + " km");
                                            break;
                                        case 2:
                                            book2.setText("Name: " + user_name + "\n" + "Distance: " + Math.round(distance) + " km");
                                            break;
                                        case 3:
                                            book3.setText("Name: " + user_name + "\n" + "Distance: " + Math.round(distance) + " km");
                                            break;
                                        case 4:
                                            book4.setText("Name: " + user_name + "\n" + "Distance: " + Math.round(distance) + " km");
                                            break;
                                        case 5:
                                            book5.setText("Name: " + user_name + "\n" + "Distance: " + Math.round(distance) +" km");
                                            break;

                                    }

                                    if (count == 5) {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                });
        sentPI = PendingIntent.getBroadcast(this,0,new Intent(SENT),0);
        deliveredPI = PendingIntent.getBroadcast(this,0,new Intent(DELIVERED),0);

        send_otp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                db.collection("bookings")
                        .whereEqualTo("driver", uid).whereEqualTo("Regular", 0).whereEqualTo("Order Status", 0)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if(task.isSuccessful()){
                                    for(QueryDocumentSnapshot document : task.getResult()){
                                        count = count + 1;
                                        otp = new Random().nextInt(999999 - 100000) + 100000;
                                        String number = (String) document.get("Phone");
                                        send(number, otp);
                                        String id = document.getId();
                                        Map<String, Integer> otp_map = new HashMap<>();
                                        otp_map.put("driver_otp", otp);
                                        db.collection("OTP").document(id)
                                                .set(otp_map);
                                        if(count == 5)
                                            break;
                                    }
                                    Map <String, Object> active_bookings = new HashMap<>();
                                    active_bookings.put("Active Bookings", 1);
                                    db.collection("drivers").document(uid).set(active_bookings, SetOptions.merge());
                                    startActivity(new Intent(GetBookingsActivity.this, DriverFinalActivity.class));
                                    finish();
                                }
                            }
                        });
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
    protected void onResume(){
        super.onResume();
        if (checkPermissions()) {
            getLastLocation();
        }
        smsSentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(GetBookingsActivity.this,"SMS sent!", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(GetBookingsActivity.this,"Generic Failure", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(GetBookingsActivity.this,"No Service", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(GetBookingsActivity.this,"Null PDU", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(GetBookingsActivity.this,"Radio Off", Toast.LENGTH_SHORT).show();
                        break;

                }
            }
        };
        smsDeliveredReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(GetBookingsActivity.this,"SMS delivered!", Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(GetBookingsActivity.this,"SMS not Delivered", Toast.LENGTH_SHORT).show();
                        break;


                }
            }
        };

        registerReceiver(smsSentReceiver, new IntentFilter(SENT));
        registerReceiver(smsDeliveredReceiver,new IntentFilter(DELIVERED));
    }

    @Override
    protected  void onPause(){
        super.onPause();

        unregisterReceiver(smsDeliveredReceiver);
        unregisterReceiver(smsSentReceiver);
    }




    public void send(String phone, int otp) {
        String message = "The OTP for your HomeFood order is " + otp;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS},
                    MY_PERMISSIONS_REQUEST_SEND_SMS);
        }
        else
        {
            SmsManager sms= SmsManager.getDefault();
            sms.sendTextMessage(phone,null,message,sentPI,deliveredPI);
        }
    }
}
