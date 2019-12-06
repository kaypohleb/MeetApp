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
import android.widget.ProgressBar;
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
import com.example.meetapp.utility.DateConverter;
import com.example.meetapp.utility.IncomingDetails;
import com.example.meetapp.utility.PollDetails;
import com.example.meetapp.utility.Polls;
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
            Log.i("Incoming Details", friend.getEvent_id() + "-" + friend.getStatus());
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
            Log.i("Incoming Details", date.getDate() + "-" + date.getTotal_vote());
        }
        return pollList;
    }

    private void getPollDetails(final Context context, String s, final IncomingDetails current){
        RequestQueue queue = Volley.newRequestQueue(context);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET,
                s+Credentials.getId().concat("/").concat(current.getEvent_id()), null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Log.d("Response", response.toString());
                personalPollDetails = parsePollJSON(response.toString());
                Boolean hasVoted = false;
                for (PollDetails pollD : personalPollDetails) {
                    if (pollD.getVote().equals("1")) {
                        hasVoted = true;
                        break;
                    }
                }
                if (!hasVoted) {
                    Log.d("Convertsion", personalPollDetails.toString());
                    incomingDialog.setContentView(R.layout.dialog_polling);
                    incomingDialog.setTitle("Vote for your preferred dates");
                    TextView title = (TextView) incomingDialog.findViewById(R.id.voteTitle_tv);
                    TextView date = (TextView) incomingDialog.findViewById(R.id.eventDates_tv);
                    title.setText(current.getEvent_name());
                    date.setText(current.getDate_from().concat(" - ").concat(current.getDate_to()));
                    RecyclerView rv = incomingDialog.findViewById(R.id.poll_rv);
                    rv.setLayoutManager(new LinearLayoutManager(context));
                    rv.setAdapter(new PollingCardAdapter(personalPollDetails, getActivity()));
                    rv.setHasFixedSize(true);
                    Button voteBtn = (Button) incomingDialog.findViewById(R.id.vote_btn);
                    voteBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (Polls.getPollVoteList().size() > 0) {
                                JSONObject jsonObject = new JSONObject();
                                try {
                                    jsonObject.put("invite_id", current.getInvite_id());
                                    JSONArray outerjsonArray = new JSONArray();
                                    for (PollDetails details : Polls.getPollVoteList()) {
                                        JSONArray innerjsonArray = new JSONArray();
                                        innerjsonArray.put(details.getDate_id());
                                        innerjsonArray.put(1);
                                        outerjsonArray.put(innerjsonArray);
                                    }

                                    jsonObject.put("date_ids", outerjsonArray);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                Log.d("JSON_submit_poll",jsonObject.toString());
                                RequestQueue queue = Volley.newRequestQueue(context);
                                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                                        getString(R.string.api_post_choice), jsonObject, new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        Log.d("Response", response.toString());
                                        incomingDialog.dismiss();
                                        getDetails(getActivity().getApplicationContext(),getString(R.string.api_get_invited));
                                    }
                                }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Log.d("Error", error.toString());
                                    }
                                });
                                queue.add(jsonObjectRequest);
                            } else {
                                Toast.makeText(getActivity(), "Please select at least one date!", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                    incomingDialog.show();

                }else{
                    Toast.makeText(context,"You've already voted!",Toast.LENGTH_SHORT).show();
                }
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
                getDetails(getActivity().getApplicationContext(),getString(R.string.api_get_invited));
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
                    if(!current.getInterest().equals("2")){
                        getPollDetails(context,getString(R.string.api_get_event_polls),current);


                    }else
                        {

                        incomingDialog.setContentView(R.layout.dialog_card_incoming);
                        incomingDialog.setTitle("Incoming Invitation!");
                        TextView title = (TextView) incomingDialog.findViewById(R.id.details_title);
                        TextView date = (TextView) incomingDialog.findViewById(R.id.details_date);
                        title.setText(current.getEvent_name());
                        date.setText(String.format("%s - %s",
                                DateConverter.dateConvert(current.getDate_from()),
                                DateConverter.dateConvert(current.getDate_to())));
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
            holder.event_txt.setText(eventDetail.getEvent_name());
            holder.date_txt.setText(DateConverter.dateConvert(eventDetail.getDate_from())+" - "+DateConverter.dateConvert(eventDetail.getDate_to()));
            holder.location_txt.setText(eventDetail.getLocation());
            String status;
            switch (Integer.valueOf(eventDetail.getStatus())){
                case 0:
                    switch (Integer.valueOf(eventDetail.getInterest())){
                        case 0:
                            status = getString(R.string.status_rejected);
                            holder.status_txt.setBackground(getResources().getDrawable(R.drawable.button_round_red));
                            break;
                        case 1:
                            status = getString(R.string.status_accepted);
                            holder.status_txt.setBackground(getResources().getDrawable(R.drawable.button_round_green));
                            break;
                        case 2:
                            status = getString(R.string.status_pending);
                            holder.status_txt.setBackground(getResources().getDrawable(R.drawable.button_round_yellow));
                            break;
                        default :
                            status = "";
                            break;

                    }
                    break;
                case 1:
                    status = getString(R.string.status_polling);
                    holder.status_txt.setBackground(getResources().getDrawable(R.drawable.button_round_blue));
                    break;

                default :
                    status ="";
                    break;

            }
            holder.status_txt.setText(status);
        }

        @Override
        public int getItemCount() {
            return allIncomingDetails.size();
        }

    }
    class IncomingHolder extends RecyclerView.ViewHolder {

        public TextView event_txt, date_txt, status_txt,location_txt;
        public CardView overall_incomingcv;
        IncomingHolder(View itemView) {
            super(itemView);
            event_txt = itemView.findViewById(R.id.eventName_tv);
            date_txt = itemView.findViewById(R.id.date_tv);
            status_txt = itemView.findViewById(R.id.eventStatus_tv);
            location_txt = itemView.findViewById(R.id.location_tv);
            overall_incomingcv = itemView.findViewById(R.id.incoming_cv);
        }

    }


    class PollingCardAdapter extends RecyclerView.Adapter<PollingCardAdapter.PollHolder> {
        private ArrayList<PollDetails> allPollingDetails;
        private Context context;

        public PollingCardAdapter(ArrayList<PollDetails> dataArgs, Context context) {
            this.allPollingDetails = dataArgs;
            this.context = context;

            for(PollDetails poll:allPollingDetails){
                poll.setSelected(false);
            }
        }


        @Override
        public PollingCardAdapter.PollHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_polldates,
                    parent, false);
            PollHolder viewHolder = new PollHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(PollingCardAdapter.PollHolder holder, int position) {
            PollDetails eventDetail = allPollingDetails.get(position);
            Log.d("Bind",eventDetail.toString());
            holder.date_txt.setText(eventDetail.getDate());
            if(holder.getChecked()){
                holder.date_txt.setTextColor(Color.RED);
                holder.totalvote_tv.setTextColor(Color.RED);
                Integer i = Integer.valueOf(eventDetail.getTotal_vote())+1;
                holder.totalvote_tv.setText(i.toString());
                Polls.addPollVoteList(eventDetail);
                eventDetail.setSelected(true);
            }else {
                holder.overall_pollingcv.setBackgroundColor(Color.WHITE);
                holder.date_txt.setTextColor(Color.BLACK);
                holder.totalvote_tv.setTextColor(Color.BLACK);
                Polls.removePollVoteList(eventDetail);
                holder.totalvote_tv.setText(eventDetail.getTotal_vote());
                eventDetail.setSelected(false);
            }

        }

        @Override
        public int getItemCount() {
            return allPollingDetails.size();
        }

        public class PollHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            private TextView date_txt, totalvote_tv;
            private CardView overall_pollingcv;
            private Boolean checked;
            PollHolder(View itemView) {
                super(itemView);
                totalvote_tv = itemView.findViewById(R.id.eventDate);
                date_txt = itemView.findViewById(R.id.totalVotes);
                overall_pollingcv = itemView.findViewById(R.id.poll_cv);
                checked=false;
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                checked = !checked;
                notifyDataSetChanged();

            }

            public Boolean getChecked() {
                return checked;
            }
        }


    }


    }



