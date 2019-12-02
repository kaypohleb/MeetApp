package com.example.meetapp.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.meetapp.CalendarActivity;
import com.example.meetapp.R;
import com.example.meetapp.utility.BusyDates;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.HashMap;

import androidx.fragment.app.Fragment;


public class CalendarFragment extends Fragment {


    Button apiBtn;
    Button sendBtn;
    private Context mContext;
    public static HashMap responsemap;


    private OnFragmentInteractionListener mListener;

    public CalendarFragment(){
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MeetingFragment.
     */
    public static CalendarFragment newInstance(String param1, String param2) {
        CalendarFragment fragment = null;
        fragment = new CalendarFragment();

        Bundle args = new Bundle();

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
        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.fragment_calendarcheck, container, false);
        apiBtn = rootView.findViewById(R.id.eventsBtn);
        apiBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getActivity(), CalendarActivity.class);
                intent.putExtra(CalendarActivity.DATESTR,"20/10/19");
                intent.putExtra(CalendarActivity.DATEEND,"22/11/19");
                startActivity(intent);
            }
        });
        sendBtn = rootView.findViewById(R.id.eventsSendBtn);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(BusyDates.getBusyTimes()!=null){
                    sendDetails(getActivity().getApplicationContext(),getString(R.string.api_post_schedule));
                }
            }

        });

        return rootView;
    }
    private void sendDetails(Context context,String s){
//        if(BusyDates.toJSON().get("time").to)
        RequestQueue queue = Volley.newRequestQueue(context);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, s, BusyDates.toJSON(), new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Response", response.toString());
                        responsemap = new Gson().fromJson(response.toString(),HashMap.class);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error",error.toString());

                    }
                });

        // Access the RequestQueue through your singleton class.
        queue.add(jsonObjectRequest);
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
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


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
