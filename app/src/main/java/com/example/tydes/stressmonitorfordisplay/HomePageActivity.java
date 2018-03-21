package com.example.tydes.stressmonitorfordisplay;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;

import java.io.BufferedReader;

public class HomePageActivity extends AppCompatActivity {

    private Button connectButton;
    private Intent mainIntent;
    public String buttonText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        connectButton = (Button) findViewById(R.id.connectButton);
        connectButton.setText("Connect to Device");

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonText = "Connecting...";
                connectButton.setText(buttonText);

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        mainIntent = new Intent(HomePageActivity.this, MainActivity.class);
                        startActivity(mainIntent);
                    }
                }, 2500);   //5 seconds


            }
        });


    }
}

