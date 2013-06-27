package com.project.oats;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class MasterActivity extends Activity {

	private final String PREFS_NAME = "OatsPref";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
        super.onResume();
        
        SharedPreferences pref = getSharedPreferences(PREFS_NAME, 0);
        Intent intent;
        
        if(pref.getString("token", null) != null && !pref.getString("token", null).equals("")) {
        	intent = new Intent(this, CheckActivity.class);
		} else {
			intent = new Intent(this, LoginActivity.class);
		}
        startActivity(intent);
        finish();
    }
}
