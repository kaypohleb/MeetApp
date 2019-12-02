package com.example.meetapp.ui;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
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
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, s+Credentials.getId(), null, new Response.Listener<JSONArray>() {
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

            incomingDialog = new Dialog(context);
            incomingDialog.setContentView(R.layout.dialog_card_incoming);
            viewHolder.overall_incomingcv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final IncomingDetails current = allIncomingDetails.get(viewHolder.getAdapterPosition());
                    TextView title = (TextView) incomingDialog.findViewById(R.id.details_title);
                    final TextView date = (TextView) incomingDialog.findViewById(R.id.details_date);
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

        private TextView event_txt, date_txt, duration_txt, location_txt;
        private CardView overall_incomingcv;
        IncomingHolder(View itemView) {
            super(itemView);
            event_txt = itemView.findViewById(R.id.eventName_tv);
            date_txt = itemView.findViewById(R.id.date_tv);
            duration_txt = itemView.findViewById(R.id.duration_tv);
            location_txt = itemView.findViewById(R.id.location_tv);
            overall_incomingcv = itemView.findViewById(R.id.incoming_cv);
        }

        void setDetails(IncomingDetails incomingDetails) {
            event_txt.setText(incomingDetails.getEvent_name());
            date_txt.setText(incomingDetails.getDate_from());
            duration_txt.setText(incomingDetails.getDuration());
            location_txt.setText(incomingDetails.getLocation());
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
    }
}


