package com.project.oats;

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.widget.Button;
import android.widget.DatePicker;

public class DatePickerFragment extends DialogFragment implements
		OnDateSetListener {
	
	private PerformanceActivity caller;
	
	private Button fromToButton;
	
	private int mYear, mMonth, mDate;
	
	public DatePickerFragment() {
		mYear = 0;
		mMonth = 0;
		mDate = 0;
		fromToButton = null;
	}
	
	public Calendar getDate() {
		return new GregorianCalendar(mYear, mMonth, mDate);
	}
	
	public void setCaller(PerformanceActivity p) {
		caller = p;
	}
	
	public void setDate(Calendar c) {
		mYear = c.get(Calendar.YEAR);
		mMonth = c.get(Calendar.MONTH);
		mDate = c.get(Calendar.DATE);
	}
	
	public void setButton(Button b) {
		fromToButton = b;
	}
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		DatePickerDialog d = new DatePickerDialog(getActivity(), this, mYear, mMonth, mDate) {
			@Override
			public void onDateChanged(DatePicker view, int year, int month, int day) {
			};
		};
		int datePickerId = fromToButton.getId();
		
		if(datePickerId == R.id.from) {
			d.setTitle("From");
		} else if(datePickerId == R.id.to) {
			d.setTitle("To");
		}
        return d;
    }
	
	

	@Override
	public void onDateSet(DatePicker view, int year, int month, int date) {
		String months[] = {"January ", "February ", "March ", "April ", "May ", "June ",
				"July ", "August ", "September ", "October ", "November ", "December "
		};
		
		int datePickerId = fromToButton.getId();
		if(datePickerId == R.id.from) {
			fromToButton.setText(Html.fromHtml("<small>From:</small> " +
					months[month] + date + ", " + year));
			caller.setFromDate(new GregorianCalendar(year, month, date));
		} else if(datePickerId == R.id.to) {
			fromToButton.setText(Html.fromHtml("<small>To:</small> " +
					months[month] + date + ", " + year));
			caller.setToDate(new GregorianCalendar(year, month, date));
		}
	}

}
