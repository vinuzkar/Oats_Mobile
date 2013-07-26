package com.project.oats;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;

public class HelpDialogFragment extends DialogFragment {
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.help, null);
		
		WebView text = (WebView)view.findViewById(R.id.sign_in_help);
		text.setVerticalScrollBarEnabled(false);
		text.loadData(getString(R.string.sign_in_help), "text/html", "utf-8");
		text.setBackgroundColor(0x00000000);
		
		text = (WebView)view.findViewById(R.id.check_help);
		text.setVerticalScrollBarEnabled(false);
		text.loadData(getString(R.string.check_help), "text/html; charset=UTF-8", null);
		text.setBackgroundColor(0x00000000);
		
		text = (WebView)view.findViewById(R.id.absence_help);
		text.setVerticalScrollBarEnabled(false);
		text.loadData(getString(R.string.absence_help), "text/html; charset=UTF-8", null);
		text.setBackgroundColor(0x00000000);
		
		text = (WebView)view.findViewById(R.id.performance_help);
		text.setVerticalScrollBarEnabled(false);
		text.loadData(getString(R.string.performance_help), "text/html; charset=UTF-8", null);
		text.setBackgroundColor(0x00000000);
		
		text = (WebView)view.findViewById(R.id.notification_help);
		text.setVerticalScrollBarEnabled(false);
		text.loadData(getString(R.string.notification_help), "text/html; charset=UTF-8", null);
		text.setBackgroundColor(0x00000000);
		
		text = (WebView)view.findViewById(R.id.notification_settings_help);
		text.setVerticalScrollBarEnabled(false);
		text.loadData(getString(R.string.notification_settings_help), "text/html; charset=UTF-8", null);
		text.setBackgroundColor(0x00000000);
		
		builder.setView(view)
			.setTitle(Html.fromHtml("<b>How to Use Oats Mobile App</b>"))
			.setNegativeButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					HelpDialogFragment.this.getDialog().cancel();
				}
			});
		
		return builder.create();
	}
}
