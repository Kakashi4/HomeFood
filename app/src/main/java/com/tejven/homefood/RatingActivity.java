package com.tejven.homefood;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class RatingActivity extends AppCompatActivity {
    Button submit_rating;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);
        final RatingBar ratingBar = findViewById(R.id.ratingBar);
        submit_rating = findViewById(R.id.btn_ratingsubmit);
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final String uid = FirebaseAuth.getInstance().getUid();
        submit_rating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.collection("bookings").document(uid)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if(task.isSuccessful()){
                                    final String driver_id = task.getResult().get("driver").toString();
                                    final DocumentReference driver_doc = db.collection("drivers").document(driver_id);
                                    driver_doc
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if(task.isSuccessful()){
                                                        Double driver_rating = (Double) task.getResult().get("rating");
                                                        Long num_orders = (Long) task.getResult().get("orders");
                                                        driver_rating = driver_rating * num_orders + ratingBar.getRating();
                                                        num_orders = num_orders + 1;
                                                        driver_rating = driver_rating / num_orders;
                                                        Map<String, Object> ratings = new HashMap<>();
                                                        ratings.put("rating", driver_rating);
                                                        ratings.put("orders", num_orders);

                                                        driver_doc.set(ratings, SetOptions.merge());


                                                    }
                                                }
                                            });

                                }
                            }
                        });
                db.collection("bookings").document(uid).delete();
                db.collection("prices").document(uid).delete();
                Toast.makeText(RatingActivity.this, "Thank you for ordering from HomeFood!", Toast.LENGTH_LONG).show();
                startActivity(new Intent(RatingActivity.this, MainActivity.class));
                finish();
            }
        });
    }
}
