package com.project.oats;

import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

public class DatePickerFragment extends DialogFragment implements
		OnDateSetListener {
	
	private int mYear, mMonth, mDate;
	
	public DatePickerFragment() {
		Calendar c = Calendar.getInstance();
		mYear = c.get(Calendar.YEAR);
		mMonth = c.get(Calendar.MONTH);
		mDate = c.get(Calendar.DATE);
	}
	
	public void setDate(int year, int month, int date) {
		mYear = year;
		mMonth = month;
		mDate = date;
	}
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new DatePickerDialog(getActivity(), this, mYear, mMonth, mDate);
    }


	@Override
	public void onDateSet(DatePicker view, int year, int month, int date) {
		// TODO Auto-generated method stub

	}

}
