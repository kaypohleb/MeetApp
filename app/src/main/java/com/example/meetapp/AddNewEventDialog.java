package com.example.meetapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class AddNewEventDialog extends AppCompatActivity {
    static final String STRING_TITLE="title";
    static final String STRING_DATETO="end_date";
    static final String STRING_DATEFROM="start_date";
    static final String STRING_DURATION="duration";
    static final String STRING_DETAILS="details";

    Calendar c;
    Button sendInviteBtn;
    EditText dateTo;
    EditText dateFrom;
    EditText meetingTitle;
    EditText meetingDetails;
    EditText duration;
    String toDate;
    String fromDate;
    Intent a;
    boolean startSet,endSet;
    int startYear,starthMonth,startDay;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_event_dialog);
        c = Calendar.getInstance();
        meetingTitle=(EditText)findViewById(R.id.meeting_title);
        meetingDetails=(EditText)findViewById(R.id.meeting_details);
        dateFrom=(EditText)findViewById(R.id.fromDateEditText);
        dateTo=(EditText)findViewById(R.id.toDateEditText);
        duration=(EditText)findViewById(R.id.meeting_duration);
        startYear = c.get(Calendar.YEAR);
        starthMonth = c.get(Calendar.MONTH);
        startDay = c.get(Calendar.DAY_OF_MONTH);
        sendInviteBtn = findViewById(R.id.btn_next);
        dateFrom.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    DatePickerDialog datePickerDialog = new DatePickerDialog(AddNewEventDialog.this, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                            dateFrom.setText(String.format("%d-%d-%d", dayOfMonth, month+1, year));
                            fromDate = String.format("%d-%d-%d", year, month+1, dayOfMonth);
                            startSet=true;
                        }
                    }, startYear, starthMonth, startDay);
                    datePickerDialog.show();
                }
            }
        });
        dateTo.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override



            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    DatePickerDialog datePickerDialog2 = new DatePickerDialog(AddNewEventDialog.this, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                            dateTo.setText(String.format("%d/%d/%d", dayOfMonth, month+1, year));
                            toDate = String.format("%d-%d-%d", year, month+1, dayOfMonth);
                            endSet=true;
                        }
                    }, startYear, starthMonth, startDay);
                    datePickerDialog2.show();
                }
            }
        });
        sendInviteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(startSet&&endSet){
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                    Date strDate = null;
                    Date eDate = null;
                    try {
                        strDate = sdf.parse(dateFrom.getText().toString());
                        eDate = sdf.parse(dateTo.getText().toString());
                        if (eDate.compareTo(strDate)>=0) {
                            Toast.makeText(getApplicationContext(),"Correct ",Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(AddNewEventDialog.this, InviteActivity.class);
                            intent.putExtra("title",meetingTitle.getText().toString());
                            intent.putExtra("duration",duration.getText().toString());
                            intent.putExtra("details",meetingDetails.getText().toString());
                            intent.putExtra("start_date",fromDate);
                            intent.putExtra("end_date",toDate);
                            startActivity(intent);
                        }else {
                            Toast.makeText(getApplicationContext(),"End Date not after ",Toast.LENGTH_SHORT).show();
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }



                }else {
                    Toast.makeText(getApplicationContext(), "Either or both dates not set", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
