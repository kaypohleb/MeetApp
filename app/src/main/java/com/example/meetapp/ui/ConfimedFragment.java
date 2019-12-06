package com.example.meetapp.ui;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
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
import com.example.meetapp.utility.DateConverter;
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
    private ConfirmedCardAdapter mAdapter;

    RecyclerView rv;
    TextView emptyView;
    View rootView;
    SwipeRefreshLayout srl;




    public ConfimedFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_confimed, container, false);
        srl = (SwipeRefreshLayout) rootView.findViewById(R.id.refresh_confirmed);
        emptyView = (TextView)rootView.findViewById(R.id.empty_view);
        rv = (RecyclerView) rootView.findViewById(R.id.confirmed_rv);
        actualDetails = new ArrayList<>();
        rv.setLayoutManager(new LinearLayoutManager(getActivity()));
        rv.setAdapter(new ConfirmedCardAdapter(actualDetails,getActivity()));
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
            Log.i("Confirm Details", friend.getEvent_name() + "-" + friend.getEvent_id());
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
                emptyView.setVisibility((actualDetails.size() > 0? View.GONE : View.VISIBLE));
                mAdapter = new ConfirmedCardAdapter(actualDetails,getActivity());
                rv.setAdapter(mAdapter);
                srl.setRefreshing(false);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Error", error.toString());
                actualDetails = new ArrayList<>();
                emptyView.setVisibility((actualDetails.size() > 0? View.GONE : View.VISIBLE));
                srl.setRefreshing(false);
            }
        });
        queue.add(jsonArrayRequest);
    }

    public class ConfirmedCardAdapter extends RecyclerView.Adapter<ConfirmedCardAdapter.ConfirmedHolder> {
        private ArrayList<ConfirmDetails> allConfirmDetails;
        private Context context;
        public void setCards(ArrayList<ConfirmDetails> details) {
            this.allConfirmDetails = details;
            notifyDataSetChanged();
        }

        public ConfirmedCardAdapter(ArrayList<ConfirmDetails> dataArgs,Context c) {
            allConfirmDetails = dataArgs;
            context = c;
        }

        @Override
        public ConfirmedCardAdapter.ConfirmedHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_confimed, parent, false);
            ConfirmedCardAdapter.ConfirmedHolder viewHolder = new ConfirmedCardAdapter.ConfirmedHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ConfirmedCardAdapter.ConfirmedHolder holder, int position) {
            final ConfirmDetails eventDetail = allConfirmDetails.get(position);
            holder.event_txt.setText(eventDetail.getEvent_name());
            holder.date_txt.setText(DateConverter.dateConvert(eventDetail.getConfirm_date()));
            holder.date_txt.setBackground(getResources().getDrawable(R.drawable.button_round_green));
            holder.duration_txt.setText(eventDetail.getDuration()+" Hours");
            holder.location_txt.setText(eventDetail.getLocation());
            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Dialog confirmDialog = new Dialog(context);
                    confirmDialog.setTitle("Meeting Details");
                    confirmDialog.setContentView(R.layout.dialog_card_confirmed);
                    TextView title = confirmDialog.findViewById(R.id.details_title);
                    title.setText(eventDetail.getEvent_name());
                    TextView date = confirmDialog.findViewById(R.id.details_date);
                    date.setText(eventDetail.getConfirm_date());
                    TextView duration = confirmDialog.findViewById(R.id.details_duration);
                    duration.setText(eventDetail.getDuration()+" Hours");
                    TextView location = confirmDialog.findViewById(R.id.details_location);
                    location.setText(eventDetail.getLocation());
                    confirmDialog.show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return allConfirmDetails==null ? 0 : allConfirmDetails.size();
        }


        class ConfirmedHolder extends RecyclerView.ViewHolder {

            private TextView event_txt, date_txt, duration_txt, location_txt;
            private CardView cardView;

            ConfirmedHolder(View itemView) {
                super(itemView);
                event_txt = itemView.findViewById(R.id.eventName_tv);
                date_txt = itemView.findViewById(R.id.eventConfirmDate);
                duration_txt = itemView.findViewById(R.id.duration_tv);
                location_txt = itemView.findViewById(R.id.location_tv);
                cardView = itemView.findViewById(R.id.confirm_cv);
            }

        }
    }


    class ConfirmDetails {
        private String event_name;
        private String event_id;
        private String date_to;
        private String date_from;
        private String confirm_date;
        private String duration;
        private String location;
        private String description;

        public String getEvent_id() {
            return event_id;
        }

        public String getConfirm_date() {
            return confirm_date;
        }

        public String getEvent_name() {
            return event_name;
        }

        public String getDescription() {
            return description;
        }

        public String getDate_to() {
            return date_to;
        }

        public String getDate_from() {
            return date_from;
        }

        public String getDuration() {
            return duration;
        }

        public String getLocation() {
            return location;
        }


    }
}

