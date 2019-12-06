package com.example.meetapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.SearchView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.meetapp.utility.Credentials;
import com.example.meetapp.utility.Friends;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class InviteActivity extends AppCompatActivity {


    String event_title;
    String date_from;
    String date_to;
    String details;
    String event_duration;
    ProgressDialog progressDialog;
    SwipeRefreshLayout srl;
    InviteCardAdapter adapter;
    SearchView searchView;
    RecyclerView rv;
    ArrayList<String> inviteList;
    ArrayList<Integer> selectedInvited;
    Button inviteBtn;
    boolean api_setup = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite);
        Intent intent = getIntent();
        srl = (SwipeRefreshLayout) findViewById(R.id.refresh_invite);
        rv = (RecyclerView) findViewById(R.id.invite_rv);
        inviteBtn = (Button) findViewById(R.id.invite_button);
        event_title = intent.getStringExtra(AddNewEventDialog.STRING_TITLE);
        event_duration = intent.getStringExtra(AddNewEventDialog.STRING_DURATION);
        date_from = intent.getStringExtra(AddNewEventDialog.STRING_DATEFROM);
        date_to = intent.getStringExtra(AddNewEventDialog.STRING_DATETO);
        details = intent.getStringExtra(AddNewEventDialog.STRING_DETAILS);
        searchView = findViewById(R.id.invite_sv);

        selectedInvited =  new ArrayList<>();

        progressDialog = new ProgressDialog(InviteActivity.this);
        progressDialog.setTitle(R.string.loading_event);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setIndeterminate(true);
        getDetails(getApplicationContext(),getString(R.string.api_get_friends));
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        rv.setLayoutManager(llm);
        inviteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(api_setup && selectedInvited!=null) {
                    progressDialog.show();
                    sendEventInvite(InviteActivity.this);
                }
            }
        });
        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getDetails(getApplicationContext(),getString(R.string.api_get_friends));
                srl.setRefreshing(false);
            }
        });


    }

    private void getDetails(Context context, String s){
        Friends.updateFriendList(context,s);
        inviteList = Friends.getUser_name();
        adapter = new InviteCardAdapter(inviteList,InviteActivity.this);
        rv.setAdapter(adapter);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String text) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String text) {
                adapter.getFilter().filter(text);
                adapter.notifyDataSetChanged();
                return true;
            }
        });

        api_setup = true;

    }
    private void sendEventInvite(final Context context){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("user_id", Credentials.getId());
            jsonObject.put("event_name", event_title);
            jsonObject.put("duration", event_duration);
            jsonObject.put("date_from", date_from);
            jsonObject.put("date_to",date_to);
            jsonObject.put("description", details);
        }catch (JSONException e){
            e.printStackTrace();
        }

        RequestQueue queue = Volley.newRequestQueue(context);
        JsonObjectRequest eventReq = new JsonObjectRequest(Request.Method.POST,getString(R.string.api_post_new_event),jsonObject ,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Response",response.toString());
                        try {
                            int event_id = Integer.valueOf(response.get("event_id").toString());
                            progressDialog.setTitle(R.string.loading_invitees);
                            setInvitees(context,event_id);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Handle Errors here
            }
        });
        queue.add(eventReq);
    }

    private void setInvitees(Context context, int event_id){
        JSONObject jsonObject = new JSONObject();
        JSONArray invitationList = new JSONArray();
        for(int pos: selectedInvited){
            JSONArray jsonArray = new JSONArray();
            jsonArray.put(Friends.swapNameForId(inviteList.get(pos)));
            jsonArray.put(String.valueOf(2));
            invitationList.put(jsonArray);
        }
        try {
            jsonObject.put("event_id",event_id);
            jsonObject.put("invitation",invitationList);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("invitees",jsonObject.toString());
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        JsonObjectRequest invReq = new JsonObjectRequest(Request.Method.POST,getString(R.string.api_post_new_invitees),jsonObject ,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Response",response.toString());
                        progressDialog.dismiss();
                        Intent intent = new Intent(InviteActivity.this, CalendarActivity.class);
                        intent.putExtra(CalendarActivity.DATESTR,date_from);
                        intent.putExtra(CalendarActivity.DATEEND,date_to);
                        startActivity(intent);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Handle Errors here
            }
        });
        queue.add(invReq);
    }




    public class InviteCardAdapter extends RecyclerView.Adapter<InviteCardAdapter.IncomingHolder> implements Filterable {
        private ArrayList<String> allInviteDetails;
        private ArrayList<String> originalDetails;
        private Context context;
        public InviteCardAdapter(ArrayList<String> dataArgs, Context c){
            allInviteDetails = dataArgs;
            originalDetails = dataArgs;
            context = c;
        }

        @Override
        public IncomingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_invite, parent, false);
            IncomingHolder viewHolder = new IncomingHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(IncomingHolder holder, int position) {
            String eventDetail = allInviteDetails.get(position);
            holder.setDetails(eventDetail);
        }

        @Override
        public int getItemCount() {
            return allInviteDetails.size();
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    ArrayList<String> filteredResults = null;
                    if(constraint.length()==0){
                        filteredResults = originalDetails;
                    }else{
                        filteredResults = getFilteredResults(constraint.toString().toLowerCase());

                    }
                    FilterResults results = new FilterResults();
                    results.values = filteredResults;

                    return results;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {

                }
            };
        }
        private ArrayList<String> getFilteredResults(String constraint) {
            ArrayList<String> results = new ArrayList<>();

            for (String item : originalDetails) {
                if (item.toLowerCase().contains(constraint)) {
                    results.add(item);
                }
            }
            return results;
        }

        class IncomingHolder extends RecyclerView.ViewHolder {
            private CardView cardView;
            private TextView name_txt;
            private boolean clicked=false;

            IncomingHolder(View itemView) {
                super(itemView);
                 name_txt = itemView.findViewById(R.id.eventName_tv);
                 cardView = itemView.findViewById(R.id.invite_cv);
                 name_txt.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         if(!clicked) {
                             cardView.setBackgroundColor(getColor(R.color.colorPrimaryDark));
                             name_txt.setTextColor(getColor(R.color.white));
                             selectedInvited.add(getAdapterPosition());
                             clicked=true;
                         }
                         else{
                             cardView.setBackgroundColor(getColor(R.color.white));
                             name_txt.setTextColor(getColor(R.color.colorPrimaryDark));
                             selectedInvited.remove(getAdapterPosition());
                             clicked=false;
                         }
                     }
                 });

            }

            void setDetails(String invitee) {
                name_txt.setText(invitee);
            }
        }

    }


}
