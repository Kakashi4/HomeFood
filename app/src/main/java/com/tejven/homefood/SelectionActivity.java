package com.tejven.homefood;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;


public class SelectionActivity extends AppCompatActivity {

    Boolean useasuser = true;
    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.useasdriver:
                if (checked)
                    useasuser = false;
                break;
            case R.id.useasuser:
                if (checked)
                    useasuser = true;
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);
        Button btn_choose = findViewById(R.id.chooseapp_btn);
        btn_choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (useasuser == true){
                    Intent intent = new Intent(SelectionActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                else {
                    Intent intent = new Intent(SelectionActivity.this, DriverActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }
}
