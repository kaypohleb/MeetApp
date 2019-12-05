package com.example.meetapp;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.meetapp.utility.BusyDates;
import com.example.meetapp.utility.Credentials;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class CalendarActivity extends Activity {
    GoogleAccountCredential mCredential;
    ProgressDialog mProgress;
    String timestr;
    String timeend;
    String startS;
    String endS;
    List<String> eventStrings;
    ArrayAdapter<String> ar=null;
    String[] selected = new String[2];


    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    public static final String DATESTR = "start_date";
    public static final String DATEEND = "end_date";

    private static final String BUTTON_TEXT = "Upcoming events";
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { CalendarScopes.CALENDAR_READONLY };

    /**
     * Create the main activity.
     * @param savedInstanceState previously saved instance data.
     */

    //RESTRICT range of
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mProgress=new ProgressDialog(this);
        mProgress.setTitle("Uploading Schedule");
        ;
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
        selected[0] = getIntent().getStringExtra(DATESTR);
        selected[1] = getIntent().getStringExtra(DATEEND);
        Log.v("startS",selected[0]);
        Log.v("endS",selected[1]);
        getResultsFromApi();




    }


    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    private void getResultsFromApi() {
        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (!isDeviceOnline()) {
            Log.d("Calendar","No network available");
        } else {
            new MakeRequestTask(mCredential).execute();
        }
    }

    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */

    private void chooseAccount() {
        String accountName = Credentials.getEmail();
        if (accountName != null) {
            mCredential.setSelectedAccountName(accountName);
            getResultsFromApi();
        } else {
            // Start a dialog from which the user can choose an account
            startActivityForResult(
                    mCredential.newChooseAccountIntent(),
                    REQUEST_ACCOUNT_PICKER);
        }
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode code indicating the result of the incoming
     *     activity result.
     * @param data Intent (containing result data) returned by incoming
     *     activity result.
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    Log.d("Calendar","Google Play Services not installed");
                } else {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
        }
    }

    /**
     * Checks whether the device currently has a network connection.
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     * @return true if Google Play Services is available and up to
     *     date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }


    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                CalendarActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    /**
     * An asynchronous task that handles the Google Calendar API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {
        private com.google.api.services.calendar.Calendar mService = null;
        private Exception mLastError = null;

        MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Meet")
                    .build();
        }

        /**
         * Background task to call Google Calendar API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<String> doInBackground(Void... params) {
            try {
                return getDataFromApi();
            } catch (Exception e) {
                e.printStackTrace();
                mLastError = e;
                cancel(true);
                return null;
            }
        }
        private boolean isnotDateOnly(String s ){
            return s.contains("00.000")&&s.contains("T");
        }
        /**
         * Fetch a list of the next 10 events from the primary calendar.
         * @return List of Strings describing returned events.
         * @throws IOException
         */


        private List<String> getDataFromApi() throws IOException, ParseException {
            // List the next 10 events from the primary calendar.
            DateTime now = new DateTime(System.currentTimeMillis());
            eventStrings = new ArrayList<String>();
            Events events = mService.events().list("primary")
                    .setMaxResults(50)
                    .setTimeMin(now)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();
            List<Event> items = events.getItems();


            for (Event event : items) {
                DateTime start = event.getStart().getDateTime();
                DateTime end = event.getEnd().getDateTime();
                boolean wholeDay=false;
                boolean dateonly=false;
                timeend = String.valueOf(end);
                timestr = String.valueOf(start);
                String[] dateTimeS = new String[2];
                String[] dateTimeE = new String[2];
                //Log.v("startTime",timestr);
                //Log.v("endTime",timeend);

                if(isnotDateOnly(timestr))
                {
                    startS = timestr.replace(".000","").replace("+08:00","");
                    dateTimeS = startS.split("T");

                }

                if(isnotDateOnly(timeend))
                {
                    endS = timeend.replaceAll(".000","").replace("+08:00","");
                    dateTimeE = endS.split("T");

                }
                Log.v("start",Arrays.toString(dateTimeS));
                Log.v("end",Arrays.toString(dateTimeE));


                eventStrings.add(dateTimeS[0]+" "+dateTimeS[1]+ ","+dateTimeE[0]+" "+dateTimeE[1]);


            }
            return eventStrings;
        }

        @Override
        protected void onPreExecute() {
            mProgress.show();
        }

        @Override
        protected void onPostExecute(List<String> output) {


            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date selectedStart  = null;
            try {
                selectedStart = sdf.parse(selected[0]);

            Date selectedEnd  = sdf.parse(selected[1]);
            for(String out: output){
                if(!out.contains("null")) {

                        String[] startend = out.split(",");
                        if(sdf.parse(startend[0]).compareTo(selectedStart)>=0 && sdf.parse(startend[0]).compareTo(selectedEnd)<=0){
                            if(sdf.parse(startend[1]).compareTo(selectedEnd)<=0){
                                BusyDates.addDates(new String[] {startend[0],startend[1]});
                            }else{
                                BusyDates.addDates(new String[] {startend[0],selected[1].concat(" 23:59:59")});
                            }
                        }
                        else if(sdf.parse(startend[0]).compareTo(selectedStart)<0){
                            if(sdf.parse(startend[1]).compareTo(selectedStart)>0){
                                if(sdf.parse(startend[1]).compareTo(selectedEnd)<=0){
                                    BusyDates.addDates(new String[] {selected[0].concat(" 00:00:00"),startend[1]});
                                }else{
                                    BusyDates.addDates(new String[] {selected[0].concat(" 00:00:00"),selected[1].concat(" 23:59:59")});
                                }
                            }
                        }

                }
            }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Log.d("Busy", BusyDates.toJSON().toString());
            sendBusyDates(CalendarActivity.this);

        }
        private void sendBusyDates(final Context context){
            RequestQueue queue = Volley.newRequestQueue(context);
            JsonObjectRequest eventReq = new JsonObjectRequest(Request.Method.POST,getString(R.string.api_post_schedule),BusyDates.toJSON(),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d("Response",response.toString());
                            mProgress.dismiss();
                            startActivity(new Intent(CalendarActivity.this,MainActivity.class));
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
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            MainActivity.REQUEST_AUTHORIZATION);
                } else {
                    Log.d("Calendar","Error Occurred: " + mLastError.getMessage());
                }
            } else {
                Log.d("Calendar","Request Cancelled");
            }
        }

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mProgress.dismiss();
        this.finish();
    }
}
