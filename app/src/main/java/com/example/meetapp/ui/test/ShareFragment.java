package com.example.meetapp.ui.test;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.meetapp.CalendarActivity;
import com.example.meetapp.R;
import com.google.api.client.util.DateTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ShareFragment extends Fragment{
    EditText startDate;
    EditText endDate;
    Calendar c;
    Button sendCalendarBtn;
    boolean startSet,endSet;
    int startYear,starthMonth,startDay;
        public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_share, container, false);
        startDate = root.findViewById(R.id.startDateText);
        endDate = root.findViewById(R.id.endDateText);
        c = Calendar.getInstance();
        startYear = c.get(Calendar.YEAR);
        starthMonth = c.get(Calendar.MONTH);
        startDay = c.get(Calendar.DAY_OF_MONTH);
        sendCalendarBtn = root.findViewById(R.id.checkDatebutton);
        startDate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                            startDate.setText(String.format("%d/%d/%d", dayOfMonth, month, year));
                            startSet=true;
                        }
                    }, startYear, starthMonth, startDay);
                    datePickerDialog.show();
                }
            }
        });

        endDate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override



                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                endDate.setText(String.format("%d/%d/%d", dayOfMonth, month, year));
                                endSet=true;
                            }
                        }, startYear, starthMonth, startDay);
                        datePickerDialog.show();
                    }
                }
            });

        sendCalendarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(startSet&&endSet){
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/mm/yyyy");
                    Date strDate = null;
                    Date eDate = null;
                    try {
                        strDate = sdf.parse(startDate.getText().toString());
                        eDate = sdf.parse(endDate.getText().toString());
                        if (eDate.after(strDate)) {
                            Toast.makeText(getContext(),"Correct ",Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getActivity(), CalendarActivity.class);
                            intent.putExtra("start_date",startDate.getText().toString());
                            intent.putExtra("end_date",endDate.getText().toString());
                            startActivity(intent);
                        }else {
                            Toast.makeText(getContext(),"End Date not after ",Toast.LENGTH_SHORT).show();
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }


                }else {
                    Toast.makeText(getContext(), "Either or both dates not set", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return root;
    };


}