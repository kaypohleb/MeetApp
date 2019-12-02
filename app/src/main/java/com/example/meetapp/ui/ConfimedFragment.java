package com.example.meetapp.ui;


import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.meetapp.R;
import com.example.meetapp.utility.Credentials;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import org.json.JSONArray;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConfimedFragment extends Fragment {
    private ArrayList<ConfirmDetails> actualDetails;

    ConfirmedCardAdapter mAdapter;
    RecyclerView rv;
    TextView emptyConfirmTV;
    SwipeRefreshLayout srl;
    public ConfimedFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_confimed, container, false);
        srl = (SwipeRefreshLayout) rootView.findViewById(R.id.refresh_confirmed);
        rv = (RecyclerView) rootView.findViewById(R.id.confirmed_rv);
        actualDetails = new ArrayList<>();
        emptyConfirmTV = (TextView)rootView.findViewById(R.id.empty_confirmedTv);
        rv.setLayoutManager(new LinearLayoutManager(getActivity()));
        rv.setItemAnimator(new DefaultItemAnimator());
        getDetails(getActivity().getApplicationContext(),getString(R.string.api_get_confirmed));
        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getDetails(getActivity().getApplicationContext(),getString(R.string.api_get_confirmed));

            }
        });
        return rootView;

    }

    private ArrayList<ConfirmDetails> parseJSON(String jsonString) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<ConfirmDetails>>(){}.getType();
        ArrayList<ConfirmDetails> eventList = gson.fromJson(jsonString, type);
        for (ConfirmDetails friend : eventList){
            Log.i("Confirm Details", friend.event + "-" + friend.eventID);
        }
        return eventList;
    }

    private void getDetails(Context context, String s){
        RequestQueue queue = Volley.newRequestQueue(context);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, s+ Credentials.getId(), null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Log.d("Response", response.toString());
                actualDetails = parseJSON(response.toString());
                emptyConfirmTV.setVisibility(View.GONE);
                mAdapter = new ConfirmedCardAdapter(actualDetails);
                rv.setAdapter(mAdapter);
                srl.setRefreshing(false);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Error", error.toString());
                mAdapter = new ConfirmedCardAdapter(actualDetails);
                rv.setAdapter(mAdapter);
                rv.setVisibility(View.GONE);
                emptyConfirmTV.setVisibility(View.VISIBLE);
                srl.setRefreshing(false);
            }
        });
        queue.add(jsonArrayRequest);
    }

    public class ConfirmedCardAdapter extends RecyclerView.Adapter<ConfirmedCardAdapter.ConfirmedHolder> {
        private ArrayList<ConfirmDetails> allConfirmDetails;

        public ConfirmedCardAdapter(ArrayList<ConfirmDetails> dataArgs) {
            allConfirmDetails = dataArgs;
        }

        @Override
        public ConfirmedCardAdapter.ConfirmedHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_confimed, parent, false);
            ConfirmedCardAdapter.ConfirmedHolder viewHolder = new ConfirmedCardAdapter.ConfirmedHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ConfirmedCardAdapter.ConfirmedHolder holder, int position) {
            ConfirmDetails eventDetail = allConfirmDetails.get(position);
            holder.setDetails(eventDetail);

        }

        @Override
        public int getItemCount() {
            return allConfirmDetails.size();
        }


        class ConfirmedHolder extends RecyclerView.ViewHolder {

            private TextView event_txt, date_txt, duration_txt, location_txt;

            ConfirmedHolder(View itemView) {
                super(itemView);
                event_txt = itemView.findViewById(R.id.eventName_tv);
                date_txt = itemView.findViewById(R.id.date_tv);
                duration_txt = itemView.findViewById(R.id.duration_tv);
                location_txt = itemView.findViewById(R.id.location_tv);
            }

            void setDetails(ConfirmDetails confirmDetails) {
                event_txt.setText(confirmDetails.event);
                date_txt.setText(confirmDetails.date);
                duration_txt.setText(confirmDetails.duration);
                location_txt.setText(confirmDetails.location);
            }
        }

    }


    class ConfirmDetails {
        public String event;
        public String eventID;
        public String date;
        public String duration;
        public String location;
        //change all detailsto private and get set methods
    }
}

