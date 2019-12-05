package com.example.meetapp.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.hotspot2.pps.Credential;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.meetapp.R;
import com.example.meetapp.SplashActivity;
import com.example.meetapp.utility.Credentials;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class ProfileFragment extends Fragment {
    private static final String acctName = "param1";
    private static final String acctEmail = "param1";
    private static final String acctID = "param2";
    private GoogleSignInClient mGoogleSignInClient;

    private OnFragmentInteractionListener mListener;

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance(String param1, String param2, String param3) {


        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(acctName, param1);
        args.putString(acctEmail, param2);
        args.putString(acctID, param3);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        TextView idView = rootView.findViewById(R.id.id);
        TextView nameView = rootView.findViewById(R.id.name);
        TextView emailView = rootView.findViewById(R.id.email);
        ImageView profilepic = rootView.findViewById(R.id.photo);
        nameView.setText(Credentials.getName());
        idView.setText(Credentials.getId());
        emailView.setText(Credentials.getEmail());
        Glide.with(getActivity()).load(Credentials.getProfilepic()).fitCenter().circleCrop().into(profilepic);
        mGoogleSignInClient = GoogleSignIn.getClient(Objects.requireNonNull(getActivity()), Credentials.getGso());
        Button signOutButton = rootView.findViewById(R.id.log_out);
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });
        return rootView;

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }

    }
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void signOut() {
        Log.d("SIGNOUT","start");
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(Objects.requireNonNull(getActivity()), new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Credentials.setName(null);
                        Credentials.setEmail(null);
                        Credentials.setId(null);
                    }
                });
        Intent intent = new Intent(getActivity(), SplashActivity.class);
        startActivity(intent);
    }
}