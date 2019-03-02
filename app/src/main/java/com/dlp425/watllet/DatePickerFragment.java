package com.dlp425.watllet;

import android.app.DatePickerDialog;
import android.app.DialogFragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.widget.TextView;
import android.widget.DatePicker;
import android.app.Dialog;
import android.widget.Toast;

import java.util.Calendar;

import static android.content.Context.MODE_PRIVATE;


public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }
    public void onDateSet(DatePicker view, int year, int month, int day) {
        final SharedPreferences sp1 = getActivity().getSharedPreferences("Login", MODE_PRIVATE);
        SharedPreferences.Editor edit = sp1.edit();
        edit.putString("LastDay", String.format("%d/%d/%d",day, month, year));
        edit.apply();
    }
}