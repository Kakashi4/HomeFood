package com.tejven.homefood;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class UserBookingConfirmed extends AppCompatActivity {
    TextView order_id, driver_name, driver_number, order_price, order_deets;
    private static final int MAKE_CALL_PERMISSION_REQUEST_CODE = 1;
    private Button dial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_booking_confirmed);
        order_id = findViewById(R.id.textView7);
        driver_name = findViewById(R.id.textView8);
        driver_number = findViewById(R.id.textView9);
        order_price = findViewById(R.id.textView10);
        order_deets = findViewById(R.id.textView11);
        dial = findViewById(R.id.btn_calldriver);
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final String uid = FirebaseAuth.getInstance().getUid();
        db.collection("bookings").document(uid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot doc = task.getResult();
                            order_id.setText("Order ID: " + uid.substring(12));
                            String parcel_size = "";
                            switch(((Long)doc.get("Package size")).intValue()){
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
                            switch (((Long) doc.get(FieldPath.of("Veg/Nonveg"))).intValue()){
                                case 0:
                                    veg = "Veg";
                                    break;
                                case 1:
                                    veg = "Non-Veg";
                                    break;
                            }
                            order_deets.setText("Parcel: " + parcel_size + " " + veg + " thali");

                            db.collection("prices").document(uid)
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            String price = task.getResult().get("price").toString();
                                            order_price.setText("Price: Rs." + price);
                                        }
                                    });
                            String driver_id = (String) doc.get("driver");
                            db.collection("drivers").document(driver_id)
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            driver_name.setText("Driver name: " + task.getResult().get("Name"));
                                            driver_number.setText("Driver number: " + task.getResult().get("Default number"));
                                        }
                                    });
                        }
                    }
                });
        dial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = driver_number.getText().toString().substring(15);

                if (!TextUtils.isEmpty(phoneNumber)) {
                    if (checkPermission(Manifest.permission.CALL_PHONE)) {
                        String dial = "tel:" + phoneNumber;
                        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse(dial)));
                    } else {
                        ActivityCompat.requestPermissions(UserBookingConfirmed.this, new String[]{Manifest.permission.CALL_PHONE}, MAKE_CALL_PERMISSION_REQUEST_CODE);
                        Toast.makeText(UserBookingConfirmed.this, "Permission required to perform phone calls", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(UserBookingConfirmed.this, "Enter a phone number", Toast.LENGTH_SHORT).show();
                }
            }
        });

        db.collection("bookings")
                .document(uid)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot snapshot,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w("tag", "Listen failed.", e);
                            return;
                        }

                        if (snapshot != null && snapshot.exists()) {
                            if((Long) snapshot.get("Order Status") == 2) {
                                Toast.makeText(UserBookingConfirmed.this, "Order completed!", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(UserBookingConfirmed.this, RatingActivity.class));
                                finish();
                            }

                        } else {
                            Log.d("tag", "Current data: null");
                        }
                    }
                });
    }
    private boolean checkPermission(String permission) {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case MAKE_CALL_PERMISSION_REQUEST_CODE :
                if (grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    dial.setEnabled(true);
                    Toast.makeText(this, "You can call the number by clicking on the button", Toast.LENGTH_SHORT).show();
                }
                return;
        }
    }
}
