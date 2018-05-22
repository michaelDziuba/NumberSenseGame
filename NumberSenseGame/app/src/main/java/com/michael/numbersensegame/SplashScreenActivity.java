package com.michael.numbersensegame;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        //Redirects to main activity
        //This is done to show a splash screen image, while the app is starting up, and then
        //show functional user interface, once the app is ready
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
