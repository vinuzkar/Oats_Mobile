package com.project.oats;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.RingtonePreference;

public class NotificationSettingsActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {
	
	private NotificationPreferenceFragment fragment;

	@Override
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 11) {
        	fragment = new NotificationPreferenceFragment();
        	getFragmentManager().beginTransaction().replace(android.R.id.content, fragment).commit();
        } else { //Api < 11
        	addPreferencesFromResource(R.xml.notification_settings);
        }
    }
     
    @Override
    @SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
    protected void onResume(){
        super.onResume();
        
        if (Build.VERSION.SDK_INT >= 11) {
        	fragment.getPreferenceScreen().getSharedPreferences()
        		.registerOnSharedPreferenceChangeListener(this);
        } else {
        	getPreferenceScreen().getSharedPreferences()
    			.registerOnSharedPreferenceChangeListener(this);
        }
    }
 
    @Override
    @SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
    protected void onPause() {
        super.onPause();

        if (Build.VERSION.SDK_INT >= 11) {
        	fragment.getPreferenceScreen().getSharedPreferences()
        		.unregisterOnSharedPreferenceChangeListener(this);
        } else {
        	getPreferenceScreen().getSharedPreferences()
    			.unregisterOnSharedPreferenceChangeListener(this);
        }
    }
 
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updatePreference(key);
    }
    
    @SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
    private void updatePreference(String key){
    	Preference pref;
    	if (Build.VERSION.SDK_INT >= 11) {
    		pref = fragment.findPreference(key);
    	} else {
    		pref = findPreference(key);
    	}
        
        if (pref instanceof RingtonePreference) {
            Uri ringtoneUri = Uri.parse(OatsMobile.getRingtone());
            Ringtone ringtone = RingtoneManager.getRingtone(this, ringtoneUri);
            if (ringtone != null) pref.setSummary(ringtone.getTitle(this));
        }
    }
    
    @TargetApi(11)
    public static class NotificationPreferenceFragment extends PreferenceFragment {
    	@Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.notification_settings);
        }
    }
}
