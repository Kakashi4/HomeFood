package com.tejven.homefood;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class MainActivity extends AppCompatActivity {

    Button btn_sign_out, update_profile, book;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String uid = FirebaseAuth.getInstance().getUid();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_sign_out = findViewById(R.id.sign_out_button);
        update_profile = findViewById(R.id.addData);
        book = findViewById(R.id.btn_order);
        update_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, UpdateProfileActivity.class);
                startActivity(intent);
            }
        });

        book.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DocumentReference docRef = db.collection("bookings").document(uid);
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                if((Long) document.get("Order Status") == 0){
                                    Intent intent = new Intent(MainActivity.this, PricingOTPActivity.class);
                                    startActivity(intent);
                                }
                                else if((Long) document.get("Order Status") == 1){
                                    Intent intent = new Intent(MainActivity.this, UserBookingConfirmed.class);
                                    startActivity(intent);
                                }
                                else if((Long) document.get("Order Status") == 2) {
                                    Intent intent = new Intent(MainActivity.this, RatingActivity.class);
                                    startActivity(intent);
                                }
                            } else {
                                Log.d("yes", "No such document");
                                Intent intent = new Intent(MainActivity.this, BookingActivity.class);
                                startActivity(intent);
                            }
                        } else {
                            Log.d("yes", "get failed with ", task.getException());
                        }
                    }
                });

            }
        });

        btn_sign_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AuthUI.getInstance()
                        .signOut(MainActivity.this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                signOut();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, ""+ e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void signOut() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}