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
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.meetapp.CalendarActivity;
import com.example.meetapp.InviteActivity;
import com.example.meetapp.MainActivity;
import com.example.meetapp.R;
import com.example.meetapp.utility.BusyDates;
import com.example.meetapp.utility.Credentials;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

/**
 * A simple {@link Fragment} subclass.
 */
public class IncomingFragment extends Fragment {
    private ArrayList<IncomingDetails> actualDetails;
    private ArrayList<PollDetails> personalPollDetails;
    Dialog incomingDialog;
    IncomingCardAdapter mAdapter;
    RecyclerView rv;
    SwipeRefreshLayout srl;
    HashMap responseMap;

    public IncomingFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_incoming, container, false);
        srl = (SwipeRefreshLayout) rootView.findViewById(R.id.refresh_confirmed);
        rv = (RecyclerView) rootView.findViewById(R.id.confirmed_rv);
        actualDetails = new ArrayList<>();
        rv.setLayoutManager(new LinearLayoutManager(getActivity()));
        rv.setItemAnimator(new DefaultItemAnimator());
        getDetails(getActivity().getApplicationContext(),getString(R.string.api_get_invited));
        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getDetails(getActivity().getApplicationContext(),getString(R.string.api_get_invited));

            }
        });
        return rootView;

    }

    private ArrayList<IncomingDetails> parseJSON(String jsonString) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<IncomingDetails>>(){}.getType();
        ArrayList<IncomingDetails> eventList = gson.fromJson(jsonString, type);
        for (IncomingDetails friend : eventList){
            Log.i("Incoming Details", friend.event_id + "-" + friend.status);
        }
        return eventList;
    }
    private void getDetails(Context context, String s){
        RequestQueue queue = Volley.newRequestQueue(context);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET,
                s+Credentials.getId(), null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Log.d("Response", response.toString());
                actualDetails = parseJSON(response.toString());
                mAdapter = new IncomingCardAdapter(actualDetails,getActivity());
                rv.setAdapter(mAdapter);
                srl.setRefreshing(false);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Error", error.toString());
                actualDetails = new ArrayList<>();
                mAdapter = new IncomingCardAdapter(actualDetails,getActivity());
                srl.setRefreshing(false);
            }
        });
        queue.add(jsonArrayRequest);
    }
    private ArrayList<PollDetails> parsePollJSON(String jsonString) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<PollDetails>>(){}.getType();
        ArrayList<PollDetails> pollList = gson.fromJson(jsonString, type);
        for (PollDetails date : pollList){
            Log.i("Incoming Details", date.date + "-" + date.total_vote);
        }
        return pollList;
    }

    private void getPollDetails(Context context, String s, String user_id, String event_id){
        RequestQueue queue = Volley.newRequestQueue(context);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET,
                s+Credentials.getId(), null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Log.d("Response", response.toString());
                personalPollDetails = parsePollJSON(response.toString());


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Error", error.toString());
                personalPollDetails = new ArrayList<>();

            }
        });
        queue.add(jsonArrayRequest);
    }


    private void updateInviteeDetails(Context context, String s, String invite_id, int interest){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("invite_id",invite_id);
            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("interest",String.valueOf(interest));
            jsonObject.put("data",jsonObject1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestQueue queue = Volley.newRequestQueue(context);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT,
                s, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("Response", response.toString());

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Error", error.toString());
            }
        });
        queue.add(jsonObjectRequest);
    }

    public class IncomingCardAdapter extends RecyclerView.Adapter<IncomingHolder> {
        private ArrayList<IncomingDetails> allIncomingDetails;
        private Context context;
        public IncomingCardAdapter(ArrayList<IncomingDetails> dataArgs,Context context){
            this.allIncomingDetails = dataArgs;
            this.context = context;
        }
        private void sendBusyDates(final Context context, final String date_from,final String date_to){
            RequestQueue queue = Volley.newRequestQueue(context);
            JsonObjectRequest eventReq = new JsonObjectRequest(Request.Method.POST,getString(R.string.api_post_schedule), BusyDates.toJSON(),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d("Response",response.toString());

                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //Handle Errors here
                }
            });
            queue.add(eventReq);
        }

        @Override
        public IncomingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_incoming,
                    parent, false);
            final IncomingHolder viewHolder = new IncomingHolder(view);

            viewHolder.overall_incomingcv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final IncomingDetails current = allIncomingDetails.get(viewHolder.getAdapterPosition());

                    incomingDialog = new Dialog(context);
                    incomingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    if(current.status.equals("2")){
                        incomingDialog.setContentView(R.layout.dialog_polling);
                        TextView title = (TextView) incomingDialog.findViewById(R.id.details_title);
                        TextView date = (TextView) incomingDialog.findViewById(R.id.details_date);
                        title.setText(current.getEvent_name());
                        title.setText(current.getDate_from().concat(" - ").concat(current.date_to));
                        RecyclerView rv = incomingDialog.findViewById(R.id.poll_rv);
                        rv.setAdapter(new PollingCardAdapter(personalPollDetails,getActivity()));
                        Button voteBtn = (Button)incomingDialog.findViewById(R.id.vote_btn);

                    }else
                        {

                        incomingDialog.setContentView(R.layout.dialog_card_incoming);
                        TextView title = (TextView) incomingDialog.findViewById(R.id.details_title);
                        TextView date = (TextView) incomingDialog.findViewById(R.id.details_date);
                        title.setText(current.getEvent_name());
                        date.setText(String.format("%s - %s",
                                current.getDate_from(),
                                current.getDate_to()));
                        Button acceptBtn = (Button)incomingDialog.findViewById(R.id.accept_btn);
                        Button rejectBtn = (Button)incomingDialog.findViewById(R.id.reject_btn);
                        final String inviteID = current.getInvite_id();
                        acceptBtn.setOnClickListener(new View.OnClickListener() {
//

                            @Override
                            public void onClick(View v) {
                                updateInviteeDetails(context,getString(R.string.api_put_update_invitees),inviteID,1);
                                Toast.makeText(context,"Accepted!" ,Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(context, CalendarActivity.class);
                                intent.putExtra(CalendarActivity.DATESTR,current.getDate_from());
                                intent.putExtra(CalendarActivity.DATEEND,current.getDate_to());
                                startActivity(intent);

                            }
                        });
                        rejectBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                updateInviteeDetails(context,getString(R.string.api_put_update_invitees),inviteID,0);
                                Toast.makeText(context,"Rejected!" ,Toast.LENGTH_SHORT).show();

                            }
                        });

                    }
                    incomingDialog.show();
                }
            });


            return viewHolder;
        }

        @Override
        public void onBindViewHolder(IncomingHolder holder, int position) {
            IncomingDetails eventDetail = allIncomingDetails.get(position);
            holder.setDetails(eventDetail);
        }

        @Override
        public int getItemCount() {
            return allIncomingDetails.size();
        }

    }
    class IncomingHolder extends RecyclerView.ViewHolder {

        private TextView event_txt, date_txt, status_txt,location_txt;
        private CardView overall_incomingcv;
        IncomingHolder(View itemView) {
            super(itemView);
            event_txt = itemView.findViewById(R.id.eventName_tv);
            date_txt = itemView.findViewById(R.id.date_tv);
            status_txt = itemView.findViewById(R.id.eventStatus_tv);
            location_txt = itemView.findViewById(R.id.location_tv);
            overall_incomingcv = itemView.findViewById(R.id.incoming_cv);
        }

        void setDetails(IncomingDetails incomingDetails) {
            event_txt.setText(incomingDetails.getEvent_name());
            date_txt.setText(incomingDetails.getDate_from()+" - "+incomingDetails.getDate_to());
            location_txt.setText(incomingDetails.getLocation());
            String status;
            switch (Integer.valueOf(incomingDetails.getStatus())){
                case 0:
                    status = getString(R.string.status_rejected);
                    break;
                case 1:
                    status = getString(R.string.status_accepted);
                    break;
                case 2:
                    status = getString(R.string.status_pending);
                    break;
                default :
                    status="";
                    break;

            }
            status_txt.setText(status);

        }
    }
    public class IncomingDetails {
        private String event_name;
        private String user_id;
        private String event_id;
        private String date_from;
        private String date_to;
        private String description;
        private String duration;
        private String location;
        private String confirm_date;
        private String status;
        private String invite_id;

        public IncomingDetails() {
        }

        public String getLocation() {
            return location;
        }

        public String getDuration() {
            return duration;
        }

        public String getDate_from() {
            return date_from;
        }

        public String getDate_to() {
            return date_to;
        }

        public String getEvent_id() {
            return event_id;
        }

        public String getDescription() {
            return description;
        }

        public String getEvent_name() {
            return event_name;
        }

        public String getConfirm_date() {
            return confirm_date;
        }

        public String getUser_id() {
            return user_id;
        }

        public String getInvite_id(){
            return invite_id;
        }

        public String getStatus() {
            return status;
        }
    }

    public class PollingCardAdapter extends RecyclerView.Adapter<PollHolder> {
        private ArrayList<PollDetails> allPollingDetails;
        private Context context;

        public PollingCardAdapter(ArrayList<PollDetails> dataArgs, Context context) {
            this.allPollingDetails = dataArgs;
            this.context = context;
        }


        @Override
        public PollHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_polldates,
                    parent, false);
            PollHolder viewHolder = new PollHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(PollHolder holder, int position) {
            PollDetails eventDetail = allPollingDetails.get(position);
            holder.setDetails(eventDetail);
        }

        @Override
        public int getItemCount() {
            return allPollingDetails.size();
        }


    }
    class PollHolder extends RecyclerView.ViewHolder {

        private TextView date_txt, totalvote_tv;
        private CardView overall_pollingcv;
        PollHolder(View itemView) {
            super(itemView);
            totalvote_tv = itemView.findViewById(R.id.eventDate);
            date_txt = itemView.findViewById(R.id.totalVotes);
            overall_pollingcv = itemView.findViewById(R.id.poll_cv);
        }

        void setDetails(PollDetails pollDetails) {
            totalvote_tv.setText(pollDetails.getTotal_vote());
            date_txt.setText(pollDetails.getDate());

        }
    }

    public class PollDetails{
        private String date_id;
        private String event_id;
        private String date;
        private String vote;
        private String total_vote;

        public String getEvent_id() {
            return event_id;
        }

        public String getDate() {
            return date;
        }

        public String getDate_id() {
            return date_id;
        }

        public String getTotal_vote() {
            return total_vote;
        }

        public String getVote() {
            return vote;
        }
    }
}


