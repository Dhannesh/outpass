package com.example.dhanesh.outpass2;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;


/**
 * A simple {@link Fragment} subclass.
 */
public class OutpassFragment extends Fragment {

    private DatePicker datePicker;
    private Calendar calendar;
    private TextView dateView;
    private int year,month,day;

    public OutpassFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_outpass, container, false);
        dateView = view.findViewById(R.id.editDate);
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        showDate(year,month+1,day);

        dateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDate(view);
            }
        });
        return view;
    }

    public void setDate(View view){
        showDialog(999);
        Toast.makeText(getContext(), "ca", Toast.LENGTH_SHORT).show();

    }

    protected Dialog onCreateDialog(int id){
        if(id==999){
            return new DatePickerDialog(this,myDateListener,year,month,day);
        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener myDateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            showDate(dayOfMonth,month,year);
        }
    };

    private void showDate(int year,int month,int day){
        dateView.setText(new StringBuilder().append(day).append("/").append(month).append("/").append(year));
    }

}
