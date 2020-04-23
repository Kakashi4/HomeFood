package com.tejven.homefood;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.grpc.CallCredentials;

public class DriverRegularFinalActivity extends AppCompatActivity {
    String[] id,id_btns;
    public static String[] path_list;
    String[] order_ids = new String[5];
    private static final int MAKE_CALL_PERMISSION_REQUEST_CODE = 1;
    TextView[] t = new TextView[5];
    Button[][] b = new Button[5][3];
    String[] phoneNumbers = new String[5];
    Double[] loc_lats = new Double[5];
    Location CurrentLoc;
    Double[] loc_longs = new Double[5];
    public static int fact = 0;
    Button clearOrders;
    Button idealPath;
    int PERMISSION_ID = 44;
    int num_orders;
    private FusedLocationProviderClient mFusedLocationClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_final);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final String uid = FirebaseAuth.getInstance().getUid();
        int temp;
        clearOrders = findViewById(R.id.b7);
        idealPath = findViewById(R.id.b6);
        id = new String[]{"t1", "t2", "t3", "t4", "t5"};
        for(int i = 0; i < id.length; i++) {
            temp = getResources().getIdentifier(id[i], "id", getPackageName());
            t[i] = findViewById(temp);
            t[i].setVisibility(View.GONE);
        }
        id_btns = new String[]{"b11", "b12", "b13", "b21", "b22", "b23", "b31", "b32", "b33", "b41", "b42", "b43", "b51", "b52","b53"};
        for(int i = 0; i < 5; i++){
            for(int j = 0; j<3; j++){
                temp = getResources().getIdentifier(id_btns[3 * i + j], "id", getPackageName());
                b[i][j] = findViewById(temp);
                b[i][j].setVisibility(View.GONE);
            }
        }
        t[0].setVisibility(View.VISIBLE);
        t[0].setText("Waiting for orders...");
        db.collection("bookings")
                .whereEqualTo("driver", uid).whereEqualTo("Order Status", 1)
                .whereEqualTo("Regular", 1).limit(5)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w("tag", "Listen failed.", e);
                            return;
                        }
                        if(value != null) {
                            int count = 0;
                            for (QueryDocumentSnapshot document : value) {
                                if (document != null) {
                                    Map<String, Object> data = document.getData();
                                    String order_id = document.getId().substring(12);
                                    String booking_name = data.get("Name").toString();
                                    String booking_number = data.get("Phone").toString();
                                    Double price = (Double) data.get("price");
                                    String parcel_size = "";
                                    switch(((Long)data.get("Package size")).intValue()){
                                        case 1:
                                            parcel_size = "Small";
                                            break;
                                        case 2:
                                            parcel_size = "Medium";
                                            break;
                                        case 3:
                                            parcel_size = "Large";
                                            break;
                                    }
                                    String veg = "";
                                    switch (((Long) data.get("Veg/Nonveg")).intValue()){
                                        case 0:
                                            veg = "Veg";
                                            break;
                                        case 1:
                                            veg = "Non-Veg";
                                            break;
                                    }
                                    t[count].setVisibility(View.VISIBLE);
                                    for(int kl = 0; kl<3; kl++){
                                        b[count][kl].setVisibility(View.VISIBLE);
                                    }

                                    t[count].setText("Order ID: " + order_id + "\nName: " + booking_name +
                                            "\nPhone: " + booking_number + "\nParcel details: " + parcel_size + " " + veg +
                                            "\nPrice: " + price);
                                    setPhone(count, booking_number);
                                    Double book_lat = (Double) data.get("Location Latitude");
                                    Double book_long = (Double) data.get("Location Longitude");
                                    setLoc(count, book_lat, book_long);
                                    setID(count, document.getId());
                                    count += 1;
                                    setNum_orders(count);
                                    if(count <= 1){
                                        idealPath.setVisibility(View.GONE);
                                    }
                                    else {
                                        idealPath.setVisibility(View.VISIBLE);
                                    }




                                }
                            }
                        }
                    }
                });
        b[0][0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = phoneNumbers[0];

                if (!TextUtils.isEmpty(phoneNumber)) {
                    if (checkPermission(Manifest.permission.CALL_PHONE)) {
                        String dial = "tel:" + phoneNumber;
                        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse(dial)));
                    } else {
                        ActivityCompat.requestPermissions(DriverRegularFinalActivity.this, new String[]{Manifest.permission.CALL_PHONE}, MAKE_CALL_PERMISSION_REQUEST_CODE);
                        Toast.makeText(DriverRegularFinalActivity.this, "Permission required to perform phone calls", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(DriverRegularFinalActivity.this, "Enter a phone number", Toast.LENGTH_SHORT).show();
                }
            }
        });
        b[1][0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = phoneNumbers[1];

                if (!TextUtils.isEmpty(phoneNumber)) {
                    if (checkPermission(Manifest.permission.CALL_PHONE)) {
                        String dial = "tel:" + phoneNumber;
                        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse(dial)));
                    } else {
                        ActivityCompat.requestPermissions(DriverRegularFinalActivity.this, new String[]{Manifest.permission.CALL_PHONE}, MAKE_CALL_PERMISSION_REQUEST_CODE);
                        Toast.makeText(DriverRegularFinalActivity.this, "Permission required to perform phone calls", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(DriverRegularFinalActivity.this, "Enter a phone number", Toast.LENGTH_SHORT).show();
                }
            }
        });
        b[2][0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = phoneNumbers[2];

                if (!TextUtils.isEmpty(phoneNumber)) {
                    if (checkPermission(Manifest.permission.CALL_PHONE)) {
                        String dial = "tel:" + phoneNumber;
                        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse(dial)));
                    } else {
                        ActivityCompat.requestPermissions(DriverRegularFinalActivity.this, new String[]{Manifest.permission.CALL_PHONE}, MAKE_CALL_PERMISSION_REQUEST_CODE);
                        Toast.makeText(DriverRegularFinalActivity.this, "Permission required to perform phone calls", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(DriverRegularFinalActivity.this, "Enter a phone number", Toast.LENGTH_SHORT).show();
                }
            }
        });
        b[3][0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = phoneNumbers[3];

                if (!TextUtils.isEmpty(phoneNumber)) {
                    if (checkPermission(Manifest.permission.CALL_PHONE)) {
                        String dial = "tel:" + phoneNumber;
                        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse(dial)));
                    } else {
                        ActivityCompat.requestPermissions(DriverRegularFinalActivity.this, new String[]{Manifest.permission.CALL_PHONE}, MAKE_CALL_PERMISSION_REQUEST_CODE);
                        Toast.makeText(DriverRegularFinalActivity.this, "Permission required to perform phone calls", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(DriverRegularFinalActivity.this, "Enter a phone number", Toast.LENGTH_SHORT).show();
                }
            }
        });
        b[4][0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = phoneNumbers[4];

                if (!TextUtils.isEmpty(phoneNumber)) {
                    if (checkPermission(Manifest.permission.CALL_PHONE)) {
                        String dial = "tel:" + phoneNumber;
                        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse(dial)));
                    } else {
                        ActivityCompat.requestPermissions(DriverRegularFinalActivity.this, new String[]{Manifest.permission.CALL_PHONE}, MAKE_CALL_PERMISSION_REQUEST_CODE);
                        Toast.makeText(DriverRegularFinalActivity.this, "Permission required to perform phone calls", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(DriverRegularFinalActivity.this, "Enter a phone number", Toast.LENGTH_SHORT).show();
                }
            }
        });

        b[0][1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri gmmIntentUri = Uri.parse("google.navigation:q="+loc_lats[0]+","+loc_longs[0]);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);

            }
        });
        b[1][1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri gmmIntentUri = Uri.parse("google.navigation:q="+loc_lats[1]+","+loc_longs[1]);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);

            }
        });
        b[2][1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri gmmIntentUri = Uri.parse("google.navigation:q="+loc_lats[2]+","+loc_longs[2]);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);

            }
        });
        b[3][1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri gmmIntentUri = Uri.parse("google.navigation:q="+loc_lats[3]+","+loc_longs[3]);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);

            }
        });
        b[4][1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri gmmIntentUri = Uri.parse("google.navigation:q="+loc_lats[4]+","+loc_longs[4]);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);

            }
        });
        b[0][2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> confirm = new HashMap<>();
                confirm.put("Order Status", 2);
                db.collection("bookings").document(order_ids[0])
                        .set(confirm, SetOptions.merge())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(DriverRegularFinalActivity.this, "Order completed!", Toast.LENGTH_SHORT).show();
                                t[0].setVisibility(View.GONE);
                                for(Button b : b[0]){
                                    b.setVisibility(View.GONE);
                                }
                            }
                        });
            }
        });
        b[1][2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> confirm = new HashMap<>();
                confirm.put("Order Status", 2);
                db.collection("bookings").document(order_ids[1])
                        .set(confirm, SetOptions.merge())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(DriverRegularFinalActivity.this, "Order completed!", Toast.LENGTH_SHORT).show();
                                t[1].setVisibility(View.GONE);
                                for(Button b : b[1]){
                                    b.setVisibility(View.GONE);
                                }
                            }
                        });
            }
        });
        b[2][2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> confirm = new HashMap<>();
                confirm.put("Order Status", 2);
                db.collection("bookings").document(order_ids[2])
                        .set(confirm, SetOptions.merge())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(DriverRegularFinalActivity.this, "Order completed!", Toast.LENGTH_SHORT).show();
                                t[2].setVisibility(View.GONE);
                                for(Button b : b[2]){
                                    b.setVisibility(View.GONE);
                                }
                            }
                        });
            }
        });
        b[3][2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> confirm = new HashMap<>();
                confirm.put("Order Status", 2);
                db.collection("bookings").document(order_ids[3])
                        .set(confirm, SetOptions.merge())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(DriverRegularFinalActivity.this, "Order completed!", Toast.LENGTH_SHORT).show();
                                t[3].setVisibility(View.GONE);
                                for(Button b : b[3]){
                                    b.setVisibility(View.GONE);
                                }
                            }
                        });
            }
        });
        b[4][2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> confirm = new HashMap<>();
                confirm.put("Order Status", 2);
                db.collection("bookings").document(order_ids[4])
                        .set(confirm, SetOptions.merge())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(DriverRegularFinalActivity.this, "Order completed!", Toast.LENGTH_SHORT).show();
                                t[4].setVisibility(View.GONE);
                                for(Button b : b[4]){
                                    b.setVisibility(View.GONE);
                                }
                            }
                        });
            }
        });
        clearOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean ordersFlag = false;
                for(TextView text : t){
                    if (text.getVisibility() == View.VISIBLE){
                        ordersFlag = true;
                        break;
                    }
                }
                if(ordersFlag){
                    Toast.makeText(DriverRegularFinalActivity.this,
                            "There are still orders to be completed!",Toast.LENGTH_SHORT).show();
                }
                else if(!ordersFlag){
                    Map<String, Object> active_bookings = new HashMap<>();
                    active_bookings.put("Active Regular Bookings", 0);
                    db.collection("drivers").document(uid)
                            .set(active_bookings, SetOptions.merge())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    startActivity(new Intent(DriverRegularFinalActivity.this, DriverActivity.class));
                                    finish();
                                }
                            });

                }
            }
        });
        idealPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Double driverlat = CurrentLoc.getLatitude();
                Double driverlon = CurrentLoc.getLongitude();
                Location loc1= new Location("dummy");
                Location loc2= new Location("dummy");
                int count = num_orders;
                String[][] a = new String[count][count];
                String[] B = new String[count];
                Double[] Lat = loc_lats;
                Double[] Lon = loc_longs;
                for(int i=0; i<count;i++)
                {
                    loc1.setLatitude(driverlat);
                    loc1.setLongitude(driverlon);
                    loc2.setLatitude(Lat[i]);
                    loc2.setLongitude(Lon[i]);
                    double distance = loc1.distanceTo(loc2);
                    B[i]=String.valueOf(distance);
                }
                for(int i=0; i<count;i++)
                {
                    for(int j=0; j<count;j++)
                    {
                        loc1.setLatitude(Lat[i]);
                        loc1.setLongitude(Lon[i]);
                        loc2.setLatitude(Lat[j]);
                        loc2.setLongitude(Lon[j]);
                        double distance = loc1.distanceTo(loc2);
                        a[i][j] = String.valueOf(distance);
                    }
                }
                int factcount=1;
                for(int i=count;i>0;i--)
                {
                    factcount=factcount*i;
                }
                String s = "";
                for(int i=0;i<count;i++)
                {
                    s=s+i;
                }
                getArrayList(getPermutation(s));

                Map< String,String> hm = new HashMap< String,String>();
                for(int i=0;i<count;i++)
                {
                    for(int j=0;j<count;j++)
                    {
                        hm.put(Integer.toString(i)+ j, a[i][j]);

                    }
                }
                Set< Map.Entry< String,String> > st = hm.entrySet();
                String[] path_dists = new String[factcount];
                int num=0;

                for(int x=0;x<factcount;x++)
                {
                    String[] dummy=new String[count];

                    for(int y=0;y<count;y++)
                    {
                        dummy[y] = String.valueOf(path_list[x].charAt(y));
                    }
                    for(int i=0;i<count-1;i++)
                    {
                        path_dists[num]=B[Integer.parseInt(dummy[0])];

                    }
                    num = num +1;
                }
                num=0;
                for(int x=0;x<factcount;x++)
                {
                    String[] dummy=new String[count];

                    for(int y=0;y<count;y++)
                    {
                        dummy[y] = String.valueOf(path_list[x].charAt(y));
                    }

                    for(int i=0;i<count-1;i++)
                    {
                        path_dists[num]=String.valueOf(Float.parseFloat(path_dists[num])+Float.parseFloat(hm.get(dummy[i]+dummy[i+1])));


                    }
                    num = num +1;
                }
                Float[] arr = new Float[factcount];
                for(int i=0;i<factcount;i++)
                {
                    arr[i]=Float.parseFloat(path_dists[i]);
                }

                //System.out.println(path_list[argMin(arr)]);
                String path = path_list[argMin(arr)];
                String toast = "";
                int tempo;
                for(int ggg = 0; ggg< path.length(); ggg++){
                    tempo = Character.getNumericValue(path.charAt(ggg));
                    tempo++;
                    toast += " --> " + tempo;
                }
                Toast.makeText(DriverRegularFinalActivity.this, "Ideal path is" + toast, Toast.LENGTH_SHORT).show();
            }
        });


    }
    static void getArrayList(ArrayList<String> arrL)
    {
        arrL.remove("");
        path_list = new String[arrL.size()];
        for (int i = 0; i < arrL.size(); i++)
        {
            path_list[i]=arrL.get(i) ;
            System.out.println(path_list[i]);
        }
    }
    public static ArrayList<String> getPermutation(String str)
    {

        // If string is empty
        if (str.length() == 0) {

            // Return an empty arraylist
            ArrayList<String> empty = new ArrayList<>();
            empty.add("");
            return empty;
        }

        // Take first character of str
        char ch = str.charAt(0);

        // Take sub-string starting from the
        // second character
        String subStr = str.substring(1);

        // Recurvise call
        ArrayList<String> prevResult = getPermutation(subStr);

        // Store the generated permutations
        // into the resultant arraylist
        ArrayList<String> Res = new ArrayList<>();

        for (String val : prevResult) {
            for (int i = 0; i <= val.length(); i++) {
                Res.add(val.substring(0, i) + ch + val.substring(i));
            }
        }

        // Return the resultant arraylist
        return Res;
    }
    public static int argMin(Float[] a) {
        Float v = Float.MAX_VALUE;
        int ind = -1;
        for (int i = 0; i < a.length; i++) {
            if (a[i] < v) {
                v = a[i];
                ind = i;
            }
        }
        return ind;
    }
    public void setPhone(int index, String number){
        phoneNumbers[index] = number;
    }
    public void setLoc(int index, Double loc_lat, Double loc_long){
        loc_lats[index] = loc_lat;
        loc_longs[index] = loc_long;
    }
    public void setID(int index, String id){
        order_ids[index] = id;
    }
    public void setNum_orders(int index){
        num_orders = index;
    }
    private boolean checkPermission(String permission) {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
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
}
