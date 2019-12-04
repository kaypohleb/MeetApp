package com.example.meetapp;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.meetapp.utility.AvailableTimes;
import com.example.meetapp.utility.SelectedTiming;
import com.example.meetapp.utility.TimeSlot;
import com.example.meetapp.utility.TimingAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Array;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.stream.Stream;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class FreeTimeGenerator extends AppCompatActivity implements suggestedTimesRecyclerViewAdapter.ItemClickListener{
    String event_id, duration, date_to, date_from;
    JSONArray rParticipants;
    ArrayList<TimeSlot> freeTimes;
    suggestedTimesRecyclerViewAdapter adapter;
    RecyclerView suggestedTimesRecyclerView;
    TextView selectedTime;
    Button confirmPoll;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_free_time_generated);
        Intent intent = getIntent();
        rParticipants = new JSONArray();
        new SelectedTiming();
        suggestedTimesRecyclerView = findViewById(R.id.suggested_times_rv);
        event_id = intent.getStringExtra("event_id");
        duration = intent.getStringExtra("duration");
        date_to = intent.getStringExtra("date_to");
        date_from = intent.getStringExtra("date_from");
        Log.d("event_id",event_id);
        selectedTime = findViewById(R.id.selectTimes_tv);
        confirmPoll = findViewById(R.id.btn_pollConfirm);
        confirmPoll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selSum = SelectedTiming.getTimingSelected().size();
                if(selSum==0){
                    Toast.makeText(getApplicationContext(),"Please Select at least one timing", Toast.LENGTH_SHORT).show();
                }else{
                    if(selSum==1) {
                        String confirmDate = SelectedTiming.getTimingSelected().get(0)[0];
                        Log.d("Confirm", confirmDate);
                        setConfirm(getApplicationContext(), event_id, 2, confirmDate);
                    }else{
                        setConfirm(getApplicationContext(),event_id,1,null);
                        for(String[] pos : SelectedTiming.getTimingSelected()){
                            String date = pos[0];
                            Log.d("Polling",date);
                        }
                        setPoll(getApplicationContext(),event_id,SelectedTiming.getTimingSelected());
                    }
                }
            }
        });
        getDetails(FreeTimeGenerator.this,getString(R.string.api_get_par_busytime),event_id);
    }
    private void getDetails(Context context, String url,String event_id){
        RequestQueue queue = Volley.newRequestQueue(context);
        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Request.Method.GET,
                url+event_id,null, new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d("Response", response.toString());
                        response = rParticipants;
                        populateRv(generateFreeTimes());
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
    private void setConfirm(Context context, String event_id,int status, String confirmdatetime){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("event_id",event_id);
            JSONObject jsonObject1 = new JSONObject();
            if(confirmdatetime!=null){
                jsonObject1.put("confirm_date",confirmdatetime);
            }
            jsonObject1.put("status",String.valueOf(status));
            jsonObject.put("data",jsonObject1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestQueue queue = Volley.newRequestQueue(context);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT,
                getString(R.string.api_put_update_events), jsonObject, new Response.Listener<JSONObject>() {
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

    private void setPoll(Context context, String event_id,ArrayList<String[]> pollDates){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("event_id",event_id);
            JSONArray jsonArray = new JSONArray();
            for(String[]  date:pollDates){
                jsonArray.put(date[0]);
            }
            jsonObject.put("dates",jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestQueue queue = Volley.newRequestQueue(context);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                getString(R.string.api_post_poll), jsonObject, new Response.Listener<JSONObject>() {
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
    public ArrayList<TimeSlot> generateFreeTimes(){

        String startOfDateRange = date_from;
        String endOfDateRange = date_to;
        int minTime = Integer.valueOf(duration);

//        System.out.println("these are the participants");
//        System.out.println(event.getParticipants());
        JSONArray participants = rParticipants;
        ArrayList<TimeSlot> TimeSlots = generateTimeSlots(startOfDateRange, endOfDateRange);
//        System.out.println("These are the time slots");
//        printTimeSlots(TimeSlots);

        ArrayList<TimeSlot> updatedTimeSlots = updateTimeSlotScores(TimeSlots, participants);
//        System.out.println("These are the updated time slot scores");
//        printTimeSlots(updatedTimeSlots);

        ArrayList<TimeSlot> revisedTimeSlots = reviseTimeSlots(updatedTimeSlots);
//        System.out.println("These are the revised time slot scores");
//        printTimeSlots(revisedTimeSlots);

        ArrayList<TimeSlot> sortedTimeSlots = sortTimeSlots(revisedTimeSlots, minTime);
//        System.out.println("These are the sorted time slots");
//        printTimeSlots(sortedTimeSlots);

        return sortedTimeSlots;
    }

    public ArrayList<TimeSlot> generateTimeSlots(String startOfDateRange, String endOfDateRange){
        Calendar startCal = TimeSlot.convertDateToCalendar(startOfDateRange);
        Calendar endCal = TimeSlot.convertDateToCalendar(endOfDateRange);
        endCal.add(Calendar.DATE, +1);
        ArrayList<TimeSlot> r  = new ArrayList<TimeSlot>();
        int x = 0;
        while (!TimeSlot.isCalEqual(startCal, endCal)){
            TimeSlot ts = new TimeSlot(startCal);
            r.add(ts);
            Calendar nStartCal = (Calendar)startCal.clone();
            nStartCal.add(Calendar.MINUTE, +30);
            startCal = nStartCal;
            x++;
        }
        System.out.println("This is final x: " + x);
        return r;
    }
    private ArrayList<TimeSlot> updateTimeSlotScores (ArrayList<TimeSlot> TimeSlots, JSONArray participants){

        for (TimeSlot ts: TimeSlots){
            Calendar TimeSlotStartTime = ts.getStartTime();

            int x = 0;
            while ( x< participants.length()){
                try {
                    JSONObject participant = participants.getJSONObject(x);
                    String name = participant.getString("username");
                    int priority = participant.getInt("priority");
                    JSONArray schedule = participant.getJSONArray("schedule");

                    int s = 0;
                    while (s < schedule.length()){
                        String startTime = schedule.getJSONObject(s).getString("date_from");
                        String endTime = schedule.getJSONObject(s).getString("date_to");
                        Calendar startCal = TimeSlot.convertTimeToCalendar(startTime);
                        Calendar endCal = TimeSlot.convertTimeToCalendar(endTime);

                        if (TimeSlot.isBetween(startCal, TimeSlotStartTime, endCal)){
                            ts.addAbsentee(name, priority);
                        }
                        s++;
                    }
                } catch (JSONException e) {e.printStackTrace();}
                x++;
            }
        }

        return TimeSlots;
    }

    private ArrayList<TimeSlot> reviseTimeSlots(ArrayList<TimeSlot> TimeSlots){
        ArrayList<TimeSlot> revisedTimeSlots = new ArrayList<TimeSlot>();
        int x = 0;
        while (x < TimeSlots.size()){
            int s = revisedTimeSlots.size();
            if (s != 0){  // if the reviseTimeSlots is not empty...
                if (TimeSlots.get(x).getAbsenteesString().equals(revisedTimeSlots.get(s-1).getAbsenteesString())){
                    revisedTimeSlots.get(s-1).setEndTime(TimeSlots.get(x).getEndTime());
                } else {
                    revisedTimeSlots.add(TimeSlots.get(x));
                }
            } else{
                revisedTimeSlots.add(TimeSlots.get(x));
            }
            x++;
        }
        return revisedTimeSlots;
    }

    private ArrayList<TimeSlot> sortTimeSlots (ArrayList<TimeSlot> TimeSlots, int minTime){
        ArrayList<TimeSlot> sortedTimeSlots = new ArrayList<TimeSlot>();
        int x = 0;
        int s = TimeSlots.size();
        while (x < s){
            int maxScore = -99999999;
            boolean addIntoSortedTimeSlots = false;
            TimeSlot maxTimeSlot = new TimeSlot();
            for (TimeSlot ts: TimeSlots){
                if (ts.getScore() > maxScore && ts.getDuration()>= minTime){
                    maxTimeSlot = ts;
                    maxScore = ts.getScore();
                    addIntoSortedTimeSlots = true;
                }
            }
            if (addIntoSortedTimeSlots){
                sortedTimeSlots.add(maxTimeSlot);
                TimeSlots.remove(maxTimeSlot);
            }
            x++;
        }
        return sortedTimeSlots;
    }


    // test method to log all the time slots
    private void printTimeSlots(ArrayList<TimeSlot> TimeSlots){
        System.out.println("Here are the TimeSlots");
        for (TimeSlot ts: TimeSlots){
            System.out.println(ts.toString());
        }
    }
    @Override
    public void onItemClick(View view, int position) {
        final Dialog mDialog = new Dialog(this);
        mDialog.setContentView(R.layout.dialog_choose_timing);
        mDialog.setCanceledOnTouchOutside(true);
        mDialog.setCancelable(true);
        String[] current  = adapter.getItem(position).toString().replace("\n"," ").split(",");
        Log.d("currentclick", Arrays.toString(current));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        ArrayList<String> timings = new ArrayList<>();

        Calendar start = Calendar.getInstance();
        Calendar eventend = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        try {
            start.setTime(sdf.parse(current[0]));
            end.setTime(sdf.parse(current[1]));
            eventend.setTime(sdf.parse(current[0]));
            eventend.add(Calendar.HOUR,Integer.valueOf(duration));

            while( !start.after(end) && !eventend.after(end)){
                String event = sdf.format(start.getTime()) +"," + sdf.format(eventend.getTime());
                Log.v("split timings", event);
                timings.add(event);
                start.add(Calendar.MINUTE, 30);
                eventend.add(Calendar.MINUTE,30);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        new AvailableTimes(timings);
        final Spinner spinner = (Spinner)mDialog.findViewById(R.id.spinner_chooseDates);
        final RecyclerView rvTimes = (RecyclerView) mDialog.findViewById(R.id.timing_rv);
        rvTimes.setHasFixedSize(true);
        rvTimes.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, AvailableTimes.dates);
        spinner.setAdapter(spinnerArrayAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String dateSelected = AvailableTimes.dates.get(position);
                rvTimes.setAdapter(new TimingAdapter(getApplicationContext(), AvailableTimes.getTimings(dateSelected)));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d("Spinner","nothing selected");
            }
        });
        spinner.setSelection(0);
        Button accept = (Button)mDialog.findViewById(R.id.accept_btn);
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int sel = SelectedTiming.getTimingSelected().size();
                selectedTime.setText(String.valueOf(sel)
                        .concat(" Timing Selected"));
                if(sel>1){
                    confirmPoll.setText(getString(R.string.button_poll));
                }else{
                    confirmPoll.setText(getString(R.string.button_confirm));
                }
                mDialog.dismiss();

            }
        });
        mDialog.show();


    }
    public void populateRv(ArrayList<TimeSlot> data) {
        // set up the RecyclerView

        suggestedTimesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new suggestedTimesRecyclerViewAdapter(this, data);
        adapter.setClickListener(this);
        suggestedTimesRecyclerView.setAdapter(adapter);
    }



}
