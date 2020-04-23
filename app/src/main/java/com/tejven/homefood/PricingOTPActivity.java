package com.tejven.homefood;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import org.w3c.dom.Document;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import java.lang.*;


public class PricingOTPActivity extends AppCompatActivity {
    double distance;

    TextView price;
    EditText otpenter;
    Button checkotp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pricing_o_t_p);
        price = findViewById(R.id.price);
        otpenter = findViewById(R.id.otp_enter);
        checkotp = findViewById(R.id.btn_submitotp);

        final String uid = FirebaseAuth.getInstance().getUid();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        final DocumentReference docRef = db.collection("prices").document(uid);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("tag", "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    if(snapshot.get("price") != null) {
                        price.setText("Your order costs Rs. " + snapshot.get("price").toString());
                    }
                    else {
                        price.setText("Waiting for driver to accept...");
                    }
                } else {
                    Log.d("tag", "Current data: null");
                }
            }
        });

        checkotp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.collection("OTP").document(uid)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if(task.isSuccessful()){
                                    String entered_otp = otpenter.getText().toString();
                                    Log.d("checking", entered_otp);
                                    DocumentSnapshot doc = task.getResult();
                                    if(doc.exists()) {
                                        String driver_otp = doc.get("driver_otp").toString();

                                        if (entered_otp == null) {
                                            Toast.makeText(PricingOTPActivity.this, "Please enter an OTP", Toast.LENGTH_SHORT).show();
                                        } else if (entered_otp.equals(driver_otp)) {

                                            Toast.makeText(PricingOTPActivity.this, "Confirmed!", Toast.LENGTH_SHORT).show();
                                            Map<String, Integer> order_status = new HashMap<>();
                                            order_status.put("Order Status", 1);
                                            db.collection("bookings").document(uid)
                                                    .set(order_status, SetOptions.merge());
                                            db.collection("OTP").document(uid).delete();
                                            Intent intent = new Intent(PricingOTPActivity.this, UserBookingConfirmed.class);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Toast.makeText(PricingOTPActivity.this, "Incorrect OTP entered", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            }
                        });
            }
        });
    }
}
