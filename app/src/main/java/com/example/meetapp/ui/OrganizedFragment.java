package com.example.meetapp.ui;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.meetapp.FreeTimeGenerator;
import com.example.meetapp.R;
import com.example.meetapp.utility.Credentials;
import com.example.meetapp.utility.Friends;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;


import org.json.JSONArray;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

/**
 * A simple {@link Fragment} subclass.
 */
public class OrganizedFragment extends Fragment {
    private ArrayList<OrganizedDetails> actualDetails;
    Dialog organizedDialog;
    OrganizedCardAdapter mAdapter;
    RecyclerView rv;
    SwipeRefreshLayout srl;

    public OrganizedFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_organized, container, false);
        srl = (SwipeRefreshLayout) rootView.findViewById(R.id.refresh_confirmed);
        rv = (RecyclerView) rootView.findViewById(R.id.confirmed_rv);
        actualDetails = new ArrayList<>();
        rv.setLayoutManager(new LinearLayoutManager(getActivity()));
        rv.setItemAnimator(new DefaultItemAnimator());
        getDetails(getActivity(),getString(R.string.api_get_organized));
        Friends.updateFriendList(getActivity(),getString(R.string.api_get_friends));
        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getDetails(getActivity().getApplicationContext(),getString(R.string.api_get_organized));

            }
        });

        return rootView;

    }
    private ArrayList<OrganizedDetails> parseJSON(String jsonString) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<OrganizedDetails>>(){}.getType();
        ArrayList<OrganizedDetails> eventList = gson.fromJson(jsonString, type);
        for (OrganizedDetails event : eventList){
            Log.i("Organized Details", event.event_id + "-" + event.date_to);
        }
        return eventList;
    }

    private void getDetails(Context context, String s){
        RequestQueue queue = Volley.newRequestQueue(context);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET,
                s+ Credentials.getId(), null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Log.d("Response", response.toString());
                mAdapter = new OrganizedCardAdapter(parseJSON(response.toString()),getActivity());
                rv.setAdapter(mAdapter);
                srl.setRefreshing(false);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Error", error.toString());
            }
        });
        queue.add(jsonArrayRequest);
    }






    public class OrganizedCardAdapter extends RecyclerView.Adapter<OrganizedHolder> {
        private ArrayList<OrganizedDetails> allOrganizedDetails;
        private Context context;

        public OrganizedCardAdapter(ArrayList<OrganizedDetails> dataArgs, Context c) {
            this.allOrganizedDetails = dataArgs;
            this.context = c;

        }

        @Override
        public OrganizedHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_organized,
                    parent, false);
            final OrganizedHolder viewHolder = new OrganizedHolder(view);

            organizedDialog = new Dialog(context);
            organizedDialog.setContentView(R.layout.dialog_card_organized);
            organizedDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            viewHolder.overall_organizedcv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final OrganizedDetails current = allOrganizedDetails.get(viewHolder.getAdapterPosition());
                    Friends.updateInviteList(getActivity(), getString(R.string.api_get_invitees),
                            current.getEvent_id());

                    TextView title = (TextView) organizedDialog.findViewById(R.id.details_title);
                    TextView date = (TextView) organizedDialog.findViewById(R.id.details_date);
                    title.setText(current.getEvent_name());
                    date.setText(String.format("%s - %s",
                            current.getDate_from(),
                            current.getDate_to()));
                    LinearLayout linearLayout = organizedDialog.findViewById(R.id.details_scroll_linear);
                    linearLayout.removeAllViews();
                    for(Friends.Invitee invitee: Friends.getInvitees()){
                        if(invitee.interest.equals("0")){
                            continue;
                        }
                        TextView tv1 = new TextView(getActivity());

                        tv1.setText(Friends.swapIdForName(invitee.user_id));
                        tv1.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT));
                        if(invitee.interest.equals("1")){
                            tv1.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                            tv1.setTextColor(getResources().getColor(R.color.white));
                        }
                        linearLayout.addView(tv1);
                    }
                    Button choseButton = organizedDialog.findViewById(R.id.btn_date_suggest);
                    choseButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(getActivity(), FreeTimeGenerator.class);
                            i.putExtra("event_id",current.getEvent_id());
                            i.putExtra("date_from",current.getDate_from());
                            i.putExtra("date_to",current.getDate_to());
                            i.putExtra("duration",current.getDuration());
                            startActivity(i);
                        }
                    });
                    organizedDialog.show();
                }
            }

            );
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(OrganizedHolder holder, int position) {
            OrganizedDetails eventDetail = allOrganizedDetails.get(position);
            holder.setDetails(eventDetail);
        }



        @Override
        public int getItemCount() {
            return allOrganizedDetails.size();
        }
    }
    class OrganizedHolder extends RecyclerView.ViewHolder {
        private TextView event_txt, date_txt, location_txt,status_txt;
        private CardView overall_organizedcv;


        OrganizedHolder(View itemView) {
            super(itemView);
            event_txt = itemView.findViewById(R.id.eventName_tv);
            date_txt = itemView.findViewById(R.id.date_tv);
            status_txt = itemView.findViewById(R.id.eventStatus_tv);
            location_txt = itemView.findViewById(R.id.location_tv);
            overall_organizedcv = itemView.findViewById(R.id.organizing_cv);

        }


        void setDetails(OrganizedDetails organizedDetails) {
            event_txt.setText(organizedDetails.event_name);
            date_txt.setText(organizedDetails.getDate_from()+" - "+organizedDetails.getDate_to());
            String status;
            switch (Integer.valueOf(organizedDetails.getStatus())){
                case 0:
                    status = getString(R.string.status_invited);
                    break;
                case 1:
                    status = getString(R.string.status_polling);
                    break;
                default :
                    status="";
                    break;

            }
            status_txt.setText(status);
            location_txt.setText(organizedDetails.location);

        }
    }

    public class OrganizedDetails {
        private String event_name;
        private String event_id;
        private String date_from;
        private String date_to;
        private String description;
        private String duration;
        private String location;
        private String status;

        public String getEvent_name() {
            return event_name;
        }

        public String getDescription() {
            return description;
        }

        public String getEvent_id() {
            return event_id;
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

        public String getStatus() {
            return status;
        }
    }

}


