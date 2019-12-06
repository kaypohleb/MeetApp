package com.example.meetapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

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
    public static final String TUTORIAL = "ranBefore";
    FloatingActionButton fab;
    BottomNavigationView navView;
    NavController navController;
    View topLevelLayout;
    static final int REQUEST_AUTHORIZATION = 1001;
    int tutorialState = 1;

    private AppBarConfiguration mAppBarConfiguration;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent i = getIntent();
        Boolean ranBefore = i.getBooleanExtra(TUTORIAL,true);
        Log.d(TUTORIAL,String.valueOf(ranBefore));
        topLevelLayout = findViewById(R.id.top_layout);
        if(ranBefore){
            topLevelLayout.setVisibility(View.GONE);

        }else{
            topLevelLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ImageView instructions =  topLevelLayout.findViewById(R.id.ivInstruction);

                    switch (tutorialState){

                        case 1: instructions.setImageDrawable(getDrawable(R.drawable.tutorial2));
                            break;
                        case 2: instructions.setImageDrawable(getDrawable(R.drawable.tutorial3));
                            break;
                        case 3: instructions.setImageDrawable(getDrawable(R.drawable.tutorial4));
                            break;
                        case 4: instructions.setImageDrawable(getDrawable(R.drawable.tutorial5));
                            break;
                        case 5: topLevelLayout.setVisibility(View.GONE);
                            break;
                    }
                    tutorialState+=1;
                }
            });
        }
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
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

}
