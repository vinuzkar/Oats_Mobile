package com.project.oats;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class DayPickerFragment extends DialogFragment {
	
	private boolean isChecked[];
	
	private Activity caller;
	
	public DayPickerFragment() {
		isChecked = new boolean[7];
		for(int i = 0; i < 7; i++) {
			isChecked[i] = false;
		}
	}
	
	public void setIsChecked(String days) {
		isChecked = new boolean[7];
		for(int i = 0; i < 7; i++) {
			isChecked[i] = (days.charAt(i) == '1');
		}
	}
	
	public void setCaller(Activity p) {
		caller = p;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    final boolean selectedDays[] = isChecked;
	    
	    // Set the dialog title
	    builder.setTitle("Repeat")
	           .setMultiChoiceItems(R.array.days, isChecked, new OnMultiChoiceClickListener() {
	               @Override
	               public void onClick(DialogInterface dialog, int which,
	                       boolean isChecked) {
	                   if (isChecked) {
	                	   selectedDays[which] = true;
	                   } else {
	                	   selectedDays[which] = false;
	                   }
	               }
	           })
	    // Set the action buttons
	           .setPositiveButton("OK", new DialogInterface.OnClickListener() {
	               @Override
	               public void onClick(DialogInterface dialog, int id) {
	                   isChecked = selectedDays;
	                   ((NotificationActivity)caller).setRepeatingDayButton(isChecked);
	                   ((NotificationActivity)caller).setRepeatingDay(isChecked);
	               }
	           })
	           .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	               @Override
	               public void onClick(DialogInterface dialog, int id) {
	                   dialog.cancel();
	               }
	           });

	    return builder.create();
	}
}
