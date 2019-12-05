package com.example.meetapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import org.mortbay.jetty.Main;

import androidx.appcompat.app.AppCompatActivity;

public class IntroductionActivity extends AppCompatActivity {
    Button next;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); //full screen activity

        setContentView(R.layout.activity_introduction);


        next = findViewById(R.id.signinBtn);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enter();
            }
        });


    }

    private void enter(){
        Intent i = new Intent(IntroductionActivity.this, MainActivity.class);
        i.putExtra(MainActivity.TUTORIAL,false);
        startActivity(i);
    }


}
