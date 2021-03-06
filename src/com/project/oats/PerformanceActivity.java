package com.project.oats;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

import com.androidplot.series.XYSeries;
import com.androidplot.xy.BarFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYStepMode;
import com.project.oats.util.SystemUiHider;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class PerformanceActivity extends FragmentActivity {
	
	private long maxPeriod;
	
	private Button from, to;
	
	private ProgressBar loading;
	
	private XYPlot graph;
	
	private Calendar fromDate, toDate;
	
	private PerformanceTask perftask;
	
	private XYSeries series;
	
	private BarFormatter formatter;
	
	private DatePickerFragment fragment;
	
	private final String PERFORMANCE_ADDRESS = "https://oatsdailybeta.herokuapp.com/mobile_graph";
	
	private final String PREFS_NAME = "OatsPref";
	
	private final String DATE_FORMAT = "dd-MM-yyyy";
	
	private final long TEN_DAYS = 777600000;
	
	private final long TWENTY_DAYS = 1728000000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		if(getDeviceDefaultOrientation() == Configuration.ORIENTATION_PORTRAIT) {
			setContentView(R.layout.activity_performance_port);
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			maxPeriod = TEN_DAYS;
		} else {
			setContentView(R.layout.activity_performance_land);
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			maxPeriod = TWENTY_DAYS;
		}
		
		from = (Button)findViewById(R.id.from);
		to = (Button)findViewById(R.id.to);
		loading = (ProgressBar)findViewById(R.id.performanceLoading);
		graph = (XYPlot)findViewById(R.id.graph);
		perftask = null;
		fragment = new DatePickerFragment();
		fragment.setCaller(this);
		
		Paint filler = new Paint();
		filler.setAlpha(200);
		filler.setShader(new LinearGradient(0, 0, 0, 100, Color.rgb(102, 153, 0), Color.rgb(146, 197, 0), Shader.TileMode.MIRROR));
		
		formatter = new BarFormatter(Color.argb(200, 0, 0, 0), Color.rgb(146, 197, 0));
		formatter.setFillPaint(filler);
		
		Number ordinat[] = {0,0,0,0,0,0,0,0,0,0};
		series = new SimpleXYSeries(
                Arrays.asList(ordinat),
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY,
                "Worktime");
		graph.addSeries(series, formatter);
		graph.setTicksPerRangeLabel(2);
		graph.setGridPadding(10, 5, 10, 0);
		
		graph.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 1);
		graph.setDomainLabel("Date Offset");
		graph.getDomainLabelWidget().pack();
		graph.setDomainRightMin(9);
		
		graph.setRangeLabel("Worktime (hr)");
		graph.getRangeLabelWidget().pack();
		graph.setRangeBottomMax(0);
		graph.setRangeTopMin(8);
		
		graph.getGraphWidget().getGridBackgroundPaint().setColor(Color.WHITE);
		graph.getGraphWidget().getGridDomainLinePaint().setColor(Color.WHITE);
        graph.getGraphWidget().getRangeOriginLinePaint().setColor(Color.BLACK);
        graph.getGraphWidget().setMarginTop(15);
        graph.getGraphWidget().setMarginBottom(10);
        graph.getLegendWidget().setVisible(false);
        
        graph.setDomainValueFormat(new DecimalFormat("0"));
		
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		fromDate = c;
		toDate = c;
	    from.setText(Html.fromHtml("<small>From:</small> " +
	    		c.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US) + ' ' +
	    		c.get(Calendar.DATE) + ", " + c.get(Calendar.YEAR)));
	    to.setText(Html.fromHtml("<small>To:</small> " +
	    		c.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US) + ' ' +
	    		c.get(Calendar.DATE) + ", " + c.get(Calendar.YEAR)));
	}

	public void setFromDate(Calendar c) {
		fromDate = c;
	}
	
	public void setToDate(Calendar c) {
		toDate = c;
	}
	
	private int getDeviceDefaultOrientation() {

	    WindowManager windowManager =  (WindowManager) getSystemService(WINDOW_SERVICE);

	    Configuration config = getResources().getConfiguration();

	    int rotation = windowManager.getDefaultDisplay().getRotation();

	    if ( ((rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) &&
	            config.orientation == Configuration.ORIENTATION_LANDSCAPE)
	        || ((rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) &&    
	            config.orientation == Configuration.ORIENTATION_PORTRAIT))
	      return Configuration.ORIENTATION_LANDSCAPE;
	    else 
	      return Configuration.ORIENTATION_PORTRAIT;
	}
	
	public void showDatePicker(View view) {
		int id = view.getId();
		fragment.setButton((Button)view);
        if(id == R.id.from) {
        	fragment.setDate(fromDate);
        	fragment.show(getSupportFragmentManager(), "datePicker");
        } else if(id == R.id.to) {
        	fragment.setDate(toDate);
        	fragment.show(getSupportFragmentManager(), "datePicker");
        }
    }
	
	public void onShowClicked(View view) {
		long fromEpoch = fromDate.getTimeInMillis();
		long toEpoch = toDate.getTimeInMillis();
		if(toEpoch - fromEpoch >= 0 && toEpoch - fromEpoch <= maxPeriod) {
			perftask = new PerformanceTask(PERFORMANCE_ADDRESS);
    		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
    			perftask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, fromDate.getTime(), toDate.getTime());
    		} else {
    			perftask.execute(fromDate.getTime(), toDate.getTime());
    		}
		} else if(toEpoch - fromEpoch < 0) {
			showInfoDialog("Error", "The 'to' date must be later than or equals the 'from' date.");
		} else if(toEpoch - fromEpoch > maxPeriod) {
			showInfoDialog("Error", "The period exceeds the limit.");
		}
	}
	
	public void setButtonText(int id, CharSequence text) {
		Button b = (Button)findViewById(id);
		b.setText(text);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_help_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.help_menu:
			DialogFragment dialog = new HelpDialogFragment();
	        dialog.show(getSupportFragmentManager(), "HelpDialogFragment");
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	protected void onPause() {
        super.onPause();
        
        if(perftask != null) {
        	perftask.cancel(true);
        }
    }
	
	private void processResponse(JSONObject obj) {
		try {
			//showInfoDialog("Info", obj.toString());
			if(obj != null) {
				switch(obj.getInt("code")) {
					case 200:
						LinkedList<Number> worktime = new LinkedList<Number>();
						JSONArray performance = obj.getJSONArray("performance");
						for(int i = 0; i < performance.length(); i++) {
							worktime.add(performance.getJSONObject(i).getDouble("worktime"));
						}
						graph.removeSeries(series);
						series = new SimpleXYSeries(
				                worktime,
				                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY,
				                "Worktime");
						graph.addSeries(series, formatter);
						graph.redraw();
						break;
					case 501:
						showLoginAgainDialog();
						break;
					case 503:
						showInfoDialog("Error", "There is a problem when accessing database in the server.");
						break;
					default:
						showInfoDialog("Error", "Unknown error.");
						break;
				}
				loading.setVisibility(View.INVISIBLE);
			} else {
				loading.setVisibility(View.INVISIBLE);
				showConnectionSettingsDialog();
			}
		} catch(Exception e) {
			loading.setVisibility(View.INVISIBLE);
			showInfoDialog("Exception", e.getMessage());
		}
	}
	
	public void showConnectionSettingsDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
      
        // Setting Dialog Title
        alertDialog.setTitle("Connection Problem");
  
        // Setting Dialog Message
        alertDialog.setMessage("Your phone is not connected to the Internet, please turn on your WiFi or Mobile Network.");
  
        // On pressing Settings button
        alertDialog.setPositiveButton("WiFi", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
            	Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                startActivity(intent);
            }
        });
        
        // On pressing Settings button
        alertDialog.setNeutralButton("Mobile Network", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
            	Intent intent = new Intent(android.provider.Settings.ACTION_DATA_ROAMING_SETTINGS);
            	if(!isCallable(intent)) {
            		intent = new Intent(Intent.ACTION_MAIN);
            		intent.setClassName("com.android.phone", "com.android.phone.Settings");
            	}
                startActivity(intent);
            }
        });
  
        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	dialog.cancel();
            }
        });
  
        // Showing Alert Message
        alertDialog.show();
    }
    
    public void showInfoDialog(String title, String message) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
      
        // Setting Dialog Title
        alertDialog.setTitle(title);
  
        // Setting Dialog Message
        alertDialog.setMessage(message);
  
        // on pressing cancel button
        alertDialog.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	dialog.cancel();
            }
        });
  
        // Showing Alert Message
        alertDialog.show();
    }
    
    public void showLoginAgainDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
      
        // Setting Dialog Title
        alertDialog.setTitle("Error");
  
        // Setting Dialog Message
        alertDialog.setMessage("Your phone ID is not found in the server. Please login again");
  
        // on pressing cancel button
        alertDialog.setNegativeButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	SharedPreferences pref = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("token", "");
                editor.commit();
            	dialog.cancel();
            	setResult(CheckActivity.RESPONSE_CODE);
            	startActivity(new Intent(PerformanceActivity.this, LoginActivity.class));
				finish();
            }
        });
  
        // Showing Alert Message
        alertDialog.show();
    }
    
    private boolean isCallable(Intent intent) {
        List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }
    
    private class PerformanceTask extends AsyncTask<Date, Void, JSONObject> {
		
		private final String CHARSET = "UTF-8";
		
		private String ADDRESS;
		
		public PerformanceTask(String s) {
			ADDRESS = s;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			loading.setVisibility(View.VISIBLE);
		}
		
		@Override
		protected JSONObject doInBackground(Date... params) {
			if(isNetworkAvailable()) {
		    	OutputStream output = null;
				JSONObject obj = null;
				HashMap<String, String> error = new HashMap<String, String>();
				
				try {
					URLConnection connection = new URL(ADDRESS).openConnection();
					connection.setConnectTimeout(10000);
		    		connection.setDoInput(true);
		    		connection.setDoOutput(true);
		    		connection.setRequestProperty("Accept-Charset", CHARSET);
		            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + CHARSET);
			    	output = connection.getOutputStream();
			    	
			    	SharedPreferences pref = getSharedPreferences(PREFS_NAME, 0);
			    	SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
		            String query = String.format("access_token=%s&from=%s&to=%s",
		            		URLEncoder.encode(pref.getString("token", ""), CHARSET),
		            		URLEncoder.encode(format.format(params[0]), CHARSET),
		            		URLEncoder.encode(format.format(params[1]), CHARSET));
		            if(query != null)
		                output.write(query.getBytes(CHARSET));
		            
					InputStream response = connection.getInputStream();
			        HttpURLConnection httpConnection = (HttpURLConnection)connection;
			        int status = httpConnection.getResponseCode();
			        BufferedReader reader = null;
			        if(status == 200) {
			        	String message = "";
		                reader = new BufferedReader(new InputStreamReader(response));
		                for (String line; (line = reader.readLine()) != null;) {
		                	message += line;
		                }
		                if (reader != null) {
		                	try { reader.close(); }
		                	catch (Exception e) { e.printStackTrace(); }
		                }
		                obj = new JSONObject(message);
			        }
		        } catch(Exception e) {
		        	error.put("message", e.getMessage());
		        	obj = new JSONObject(error);
		        } finally {
		             if (output != null) {
		            	 try { output.close(); return obj;}
		            	 catch (Exception e) {
		            		 error.put("message", e.getMessage());
		            		 obj = new JSONObject(error);
		            		 return obj;
		            	 }
		             }
		        }
				return obj;
		    }
			return null;
		}
		
		@Override
		protected void onPostExecute(JSONObject obj) {
			super.onPostExecute(obj);
			processResponse(obj);
		}
		
		private boolean isNetworkAvailable() {
	        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
	        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
	        
	        // if no network is available networkInfo will be null
	        // otherwise check if we are connected
	        if (networkInfo != null && networkInfo.isConnected()) {
	            return true;
	        }
	        return false;
	    }
	}
}
