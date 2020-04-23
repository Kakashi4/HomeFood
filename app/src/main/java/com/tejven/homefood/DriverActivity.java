package com.tejven.homefood;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class DriverActivity extends AppCompatActivity {

    FirebaseFirestore db;
    Button btn_sign_out, update_profile, get_bookings, get_default_bookings;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);
        btn_sign_out = findViewById(R.id.btn_driversignout);
        update_profile = findViewById(R.id.btn_updatedriverprofile);
        get_bookings = findViewById(R.id.btn_getbookings);
        get_default_bookings = findViewById(R.id.btn_getdefaultbookings);
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        final String uid = currentUser.getUid();

        update_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DriverActivity.this, UpdateDriverProfileActivity.class);
                startActivity(intent);
            }
        });

        get_bookings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DocumentReference docRef = db.collection("drivers").document(uid);
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                if((Long) document.get("Active Bookings") == 0) {
                                    Intent intent = new Intent(DriverActivity.this, GetBookingsActivity.class);
                                    startActivity(intent);
                                }
                                else if((Long) document.get("Active Bookings") == 1){
                                    startActivity(new Intent(DriverActivity.this, DriverFinalActivity.class));
                                }
                            } else {
                                Toast.makeText(DriverActivity.this, "Set default values before receiving bookings", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(DriverActivity.this, "Error in retrieving driver account confirmation", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });

        get_default_bookings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DocumentReference docRef = db.collection("drivers").document(uid);
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                if((Long) document.get("Active Regular Bookings") == 0) {
                                    Intent intent = new Intent(DriverActivity.this, GetRegularBookingsActivity.class);
                                    startActivity(intent);
                                }
                                else if((Long) document.get("Active Regular Bookings") == 1) {
                                    startActivity(new Intent(DriverActivity.this, DriverRegularFinalActivity.class));
                                }
                            } else {
                                Toast.makeText(DriverActivity.this, "Set default values before receiving bookings", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(DriverActivity.this, "Error in retrieving driver account confirmation", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });

        btn_sign_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AuthUI.getInstance()
                        .signOut(DriverActivity.this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                signOut();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(DriverActivity.this, ""+ e.getMessage(), Toast.LENGTH_LONG).show();
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
