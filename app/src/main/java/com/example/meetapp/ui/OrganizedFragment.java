package com.example.meetapp.ui;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;
import com.example.meetapp.FreeTimeGenerator;
import com.example.meetapp.MainActivity;
import com.example.meetapp.R;
import com.example.meetapp.utility.Credentials;
import com.example.meetapp.utility.DateConverter;
import com.example.meetapp.utility.Friends;
import com.example.meetapp.utility.PollAdapter;
import com.example.meetapp.utility.Polls;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
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
            viewHolder.overall_organizedcv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final OrganizedDetails current = allOrganizedDetails.get(viewHolder.getAdapterPosition());

                    organizedDialog = new Dialog(context);

                    organizedDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    if (current.getStatus().equals("0")) {
                        updateInviteList(context,getString(R.string.api_get_invitees),current);
                        }
                     else {
                        updatePollList(context,getString(R.string.api_get_poll_organizer),current);
                    }
                    Log.d("Progress","show Dialog");

                }
            }

            );
            return viewHolder;
        }
        public void updatePollList(final Context context, String url, final OrganizedDetails current){
            RequestQueue queue = Volley.newRequestQueue(context);
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url + current.getEvent_id(), null, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    Polls.setAllPolls(response.toString());
                    Log.i("Response",response.toString());
                    organizedDialog.setContentView(R.layout.dialog_polling);
                    organizedDialog.setTitle("Choose Dates");
                    TextView title = (TextView) organizedDialog.findViewById(R.id.voteTitle_tv);
                    TextView date = (TextView) organizedDialog.findViewById(R.id.eventDates_tv);
                    title.setText(current.getEvent_name());
                    date.setText(String.format("%s - %s", DateConverter.dateConvert(
                            current.getDate_from()), DateConverter.dateConvert(current.getDate_to())));
                    RecyclerView recyclerView  = organizedDialog.findViewById(R.id.poll_rv);
                    recyclerView.setLayoutManager(new LinearLayoutManager(context));
                    recyclerView.setHasFixedSize(true);
                    final PollAdapter pollAdapter = new PollAdapter(getActivity(),Polls.getAllPolls());
                    recyclerView.setAdapter(pollAdapter);
                    Button choseButton = organizedDialog.findViewById(R.id.vote_btn);
                    choseButton.setText(R.string.button_confirm);
                    choseButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (pollAdapter.getSelected() != null) {
                            Polls.Poll selected = pollAdapter.getSelected();
                            JSONObject jsonObject = new JSONObject();
                            try {
                                jsonObject.put("event_id", current.event_id);
                                JSONObject jsonObject1 = new JSONObject();

                                if (selected != null) {
                                    jsonObject1.put("confirm_date", selected.getDate());
                                }
                                jsonObject1.put("status", String.valueOf(2));
                                jsonObject.put("data", jsonObject1);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            RequestQueue queue = Volley.newRequestQueue(context);
                            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT,
                                    getString(R.string.api_put_update_events), jsonObject, new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    Log.d("Response", response.toString());
                                    organizedDialog.dismiss();
                                    getDetails(context,getString(R.string.api_get_organized));

                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.d("Error", error.toString());
                                }
                            });
                            queue.add(jsonObjectRequest);

                        } else {
                            Toast.makeText(context, "Please select the date to confirm", Toast.LENGTH_SHORT).show();
                        }
                    }

                });
                    organizedDialog.show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
            queue.add(jsonArrayRequest);

    }

        public void updateInviteList(Context context, String url,final OrganizedDetails current){
            RequestQueue queue = Volley.newRequestQueue(context);
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url + current.getEvent_id(), null, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    Friends.setInvitees(response.toString());
                    organizedDialog.setTitle("Current State of Invitations");
                    organizedDialog.setContentView(R.layout.dialog_card_organized);
                    TextView title = (TextView) organizedDialog.findViewById(R.id.details_title);
                    TextView date = (TextView) organizedDialog.findViewById(R.id.details_date);
                    title.setText(current.getEvent_name());
                    date.setText(String.format("%s - %s",
                            DateConverter.dateConvert(current.getDate_from()),
                            DateConverter.dateConvert(current.getDate_to())));
                    LinearLayout linearLayout = organizedDialog.findViewById(R.id.details_scroll_linear);
                    linearLayout.removeAllViews();
                    for (Friends.Invitee invitee : Friends.getInvitees()) {
                        if (invitee.interest.equals("0")) {
                            continue;
                        }
                        Log.d("Friend",invitee.priority);
                        LinearLayout ll = new LinearLayout(getActivity());

                        // Set the CardView layoutParams
                        ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        );
                        params.setMargins(10,10,10,10);
                        ll.setLayoutParams(params);
                        ll.setPadding(5,5,5,5);
                        ll.setElevation(6);
                        ll.setBackground(getActivity().getDrawable(R.drawable.button_round));

                        TextView tv = new TextView(getActivity());
                        tv.setLayoutParams(params);
                        tv.setPadding(20,20,20,20);
                        tv.setText(Friends.swapIdForName(invitee.user_id));
                        if (invitee.interest.equals("1")) {
                            ll.setBackground(getActivity().getDrawable(R.drawable.button_round_green));
                            tv.setTextColor(getResources().getColor(R.color.white));
                        }
                        if (invitee.interest.equals("0")) {
                            ll.setBackground(getActivity().getDrawable(R.drawable.button_round_red));
                            tv.setTextColor(getResources().getColor(R.color.white));
                        }
                        ll.addView(tv);

                        linearLayout.addView(ll);

                    }
                    Button choseButton = organizedDialog.findViewById(R.id.btn_date_suggest);
                    choseButton.setBackground(getResources().getDrawable(R.drawable.button_round_green));
                    choseButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(getActivity(), FreeTimeGenerator.class);
                            i.putExtra("event_id", current.getEvent_id());
                            i.putExtra("date_from", current.getDate_from());
                            i.putExtra("date_to", current.getDate_to());
                            i.putExtra("duration", current.getDuration());
                            startActivity(i);
                        }

                    });
                    organizedDialog.show();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
            queue.add(jsonArrayRequest);

        }


        @Override
        public void onBindViewHolder(OrganizedHolder holder, int position) {
            OrganizedDetails eventDetail = allOrganizedDetails.get(position);
            holder.event_txt.setText(eventDetail.event_name);
            holder.date_txt.setText(DateConverter.dateConvert(eventDetail.getDate_from())+" - "+DateConverter.dateConvert(eventDetail.getDate_to()));
            String status;
            switch (Integer.valueOf(eventDetail.getStatus())){
                case 0:
                    status = getString(R.string.status_invited);
                    holder.status_txt.setBackground(getResources().getDrawable(R.drawable.button_round_yellow));
                    break;
                case 1:
                    status = getString(R.string.status_polling);
                    holder.status_txt.setBackground(getResources().getDrawable(R.drawable.button_round_blue));
                    break;
                default :
                    status="";
                    break;

            }
            holder.status_txt.setText(status);
            holder.location_txt.setText(eventDetail.location);
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


