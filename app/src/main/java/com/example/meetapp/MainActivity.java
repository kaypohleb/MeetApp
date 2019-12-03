package com.example.meetapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.example.meetapp.ui.CalendarFragment;
import com.example.meetapp.ui.ProfileFragment;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements ProfileFragment.OnFragmentInteractionListener,
        CalendarFragment.OnFragmentInteractionListener {
    public GoogleSignInOptions gso;
    FloatingActionButton fab;
    BottomNavigationView navView;
    NavController navController;

    static final int REQUEST_AUTHORIZATION = 1001;


    private AppBarConfiguration mAppBarConfiguration;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        navView = findViewById(R.id.nav_view);
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_confirmed, R.id.nav_outgoing, R.id.nav_incoming,
                R.id.nav_profile)
                .build();

        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,AddNewEventDialog.class));
            }
        });
    }




    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
