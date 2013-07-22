package com.project.oats;

import java.util.Calendar;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.Button;
import android.widget.TimePicker;

public class TimePickerFragment extends DialogFragment implements
		OnTimeSetListener {
	
	private int mHour, mMinute;
	
	boolean is24h;
	
	private Button timePickerButton;
	
	private Activity caller;
	
	public TimePickerFragment() {
		mHour = 0;
		mMinute = 0;
		is24h = false;
		timePickerButton = null;
	}
	
	public void setCaller(Activity p) {
		caller = p;
	}
	
	public void setTime(Calendar c) {
		mHour = c.get(Calendar.HOUR_OF_DAY);
		mMinute = c.get(Calendar.MINUTE);
	}
	
	public void setIs24H(boolean is24hour) {
		is24h = is24hour;
	}
	
	public void setButton(Button b) {
		timePickerButton = b;
	}
	
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		TimePickerDialog d = new TimePickerDialog(getActivity(), this, mHour, mMinute, is24h) {
			@Override
			public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
			};
		};
		int timePickerId = timePickerButton.getId();
		
		if(timePickerId == R.id.check_in_time) {
			d.setTitle("Check-In Notification");
		} else if(timePickerId == R.id.check_out_time) {
			d.setTitle("Check-Out Notification");
		}
        return d;
    }
	
	

	@Override
	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		
		int timePickerId = timePickerButton.getId();
		
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, hourOfDay);
		c.set(Calendar.MINUTE, minute);
		if(timePickerId == R.id.check_in_time) {
			((NotificationActivity)caller).setCheckInTime(c);
			((NotificationActivity)caller).setTimeButtonText(NotificationActivity.CHECK_IN, c, is24h);
		} else if(timePickerId == R.id.check_out_time) {
			((NotificationActivity)caller).setCheckOutTime(c);
			((NotificationActivity)caller).setTimeButtonText(NotificationActivity.CHECK_OUT, c, is24h);
		}
	}

}
