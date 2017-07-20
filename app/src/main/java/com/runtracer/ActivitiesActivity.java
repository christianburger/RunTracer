package com.runtracer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class ActivitiesActivity extends AppCompatActivity implements View.OnClickListener {

	private static final String TAG = "runtracer";
	private static final int SHOW_CHART = 1001;

	//GUI elements
	FloatingActionButton mEmail;
	FloatingActionButton mShowChart;

	private ExpandableListView mActivitiesList;
	private ExpandableListAdapter mActivityListAdapter;
	private Toolbar mToolbar;

	private TextView mActivitySummary;

	List<String> listDataHeader;
	HashMap<String, List<String>> listDataChild;
	HashMap<Long, Long> listDataChildToRunIdCorrelation;

	private HashMap activityInfoMap;
	private HashMap filteredActivityInfoMap;
	/**
	 * ATTENTION: This was auto-generated to implement the App Indexing API.
	 * See https://g.co/AppIndexing/AndroidStudio for more information.
	 */
	private GoogleApiClient client;
	private Long selected_run_id;
	private UserData user_data;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_activities);

		mEmail = (FloatingActionButton) findViewById(R.id.fab);
		mEmail.setOnClickListener(this);

		mShowChart = (FloatingActionButton) findViewById(R.id.fab_show_chart);
		mShowChart.setOnClickListener(this);

		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		mToolbar.setTitle("Activity Details");

		setSupportActionBar(mToolbar);

		activityInfoMap = new HashMap<Long, RunData>();
		filteredActivityInfoMap = new HashMap<Long, RunData>();
		activityInfoMap = (HashMap) getIntent().getSerializableExtra("RunInfo");
		user_data = (UserData) getIntent().getSerializableExtra("UserData");
		listDataChildToRunIdCorrelation = new HashMap<>();

		getActivities();

		mActivitySummary = (TextView) findViewById(R.id.activity_summary);
		mActivityListAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);
		mActivitiesList = (ExpandableListView) findViewById(R.id.activities_list1);
		prepareListData();

		mActivityListAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);
		mActivitiesList.setAdapter(mActivityListAdapter);
		mActivitiesList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

			@Override
			public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
				return false;
			}
		});

		// Listview Group expanded listener
		mActivitiesList.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

			@Override
			public void onGroupExpand(int groupPosition) {
				//Toast.makeText(getApplicationContext(), listDataHeader.get(groupPosition) + " Expanded", Toast.LENGTH_SHORT).show();
			}
		});
		mActivitiesList.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
			@Override
			public void onGroupCollapse(int groupPosition) {
				//Toast.makeText(getApplicationContext(), listDataHeader.get(groupPosition) + " Collapsed", Toast.LENGTH_SHORT).show();
			}
		});

		mActivitiesList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
				//Toast.makeText(getApplicationContext(), listDataHeader.get(groupPosition) + " : " + listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition), Toast.LENGTH_SHORT).show();
				//showActivity(listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition));
				showActivity(childPosition);
				return false;
			}
		});
		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		client = new GoogleApiClient.Builder(this).build();

		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
	}

	public ActivitiesActivity() {
	}

	private int getScaleY(int pic_height) {
		Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		int height = display.getHeight();
		Double val;
		val = (double) height / (double) pic_height;
		val = val * 100d;
		return val.intValue();
	}

	private int getScaleX(int pic_width) {
		Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		int width = display.getWidth();
		Double val;
		val = (double) width / (double) pic_width;
		val = val * 100d;
		return val.intValue();
	}

	public boolean showActivity(int childposition) {
		writeLog(String.format(Locale.US, "showActivity: received: %d", childposition));
		if (!listDataChildToRunIdCorrelation.isEmpty() && listDataChildToRunIdCorrelation.containsKey(childposition) ) {
			writeLog(String.format(Locale.US, "showActivity: before this.selected_run_id: %d", this.selected_run_id));
			this.selected_run_id = listDataChildToRunIdCorrelation.get(childposition);
			writeLog(String.format(Locale.US, "showActivity: after this.selected_run_id: %d", this.selected_run_id));
			mActivitySummary.setText(String.format(Locale.US, "Run ID: %d", this.selected_run_id));
		} else {
			return false;
		}
		return true;
	}

	public String getActivityInfo(long ckey) {
		String info = null;

		if (filteredActivityInfoMap.containsKey(ckey)) {
			RunData lrun;
			lrun = (RunData) filteredActivityInfoMap.get(ckey);
			lrun.getValues();
			info = String.format(Locale.US, "%s\ndistance: %.2f \ncalories: %.2f", lrun.getRun_date_start(), lrun.getDistance_km_v(), lrun.getCalories_v_distance());
		}
		writeLog(String.format("getActivityInfo: %s", info));
		return info;
	}

	public void writeLog(String msg) {
		String date = (DateFormat.format("dd-MM-yyyy hh:mm:ss a", new java.util.Date()).toString());
		Log.e(TAG, date + ": " + msg);
	}

	public int getActivities() {
		List keys = (List<Long>) new ArrayList(activityInfoMap.keySet());
		Integer ckey;

		RunData lruninfo;
		Iterator itk = keys.iterator();

		for (; itk.hasNext(); ) {
			ckey = (Integer) itk.next();
			writeLog(String.format(Locale.US, "activityInfoMap: ckey: %d", ckey));
			lruninfo = (RunData) activityInfoMap.get(ckey);
			writeLog(String.format("activityInfoMap: run_date_start: %s", lruninfo.getRun_date_start()));
			writeLog(String.format("activityInfoMap: run_id: %s", lruninfo.getRun_id_v()));
			writeLog(String.format("activityInfoMap: calories: %s", lruninfo.getCalories_v_distance()));

			if ((lruninfo.getRun_id_v()> 0)) {
				/*
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.CANADA);
				lruninfo.run_date_start_v= new Date();
				lruninfo.run_date_start_v= format.parse(lruninfo.run_date_start);
				lruninfo.run_date_end_v= new Date();
				lruninfo.run_date_end_v= format.parse(lruninfo.run_date_end);
				if (lruninfo.run_date_start_v.getTime() > 10000000) {
					*/
				if (lruninfo.getRun_date_start_v().getTime() > 10000000) {
					writeLog(String.format(Locale.US, "adding to filteredActivityInfoMap: ckey: %d", ckey));
					filteredActivityInfoMap.put(ckey, lruninfo);
				}
			}
		}
		return (0);
	}

	private void prepareListData() {
		List keys = (List<Long>) new ArrayList(activityInfoMap.keySet());
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.CANADA);

		Collections.sort(keys);
		Integer ckey;

		listDataHeader = new ArrayList<String>();
		listDataChild = new HashMap<String, List<String>>();

		// Adding child data
		listDataHeader.add("This Month");
		listDataHeader.add("Last Month");
		listDataHeader.add("All Activity..");

		// Adding child data
		List<String> thisMonth = new ArrayList<String>();
		List<String> lastMonth = new ArrayList<String>();
		List<String> allActivity = new ArrayList<String>();

		RunData lruninfo;
		Iterator itv = keys.iterator();
		for (; itv.hasNext(); ) {
			ckey = (Integer) itv.next();
			writeLog(String.format(Locale.US, "ckey: %d", ckey));
			if (filteredActivityInfoMap.containsKey(ckey)) {
				lruninfo = (RunData) filteredActivityInfoMap.get(ckey);
				writeLog(String.format(Locale.US, "filteredActivityInfoMap.get(%d): run_date_start: %s", ckey, lruninfo.getRun_date_start()));
				if ((lruninfo.getRun_id_v()> 0)) {
					String data_info = getActivityInfo(lruninfo.getRun_id_v());
					allActivity.add(data_info);
					long pos = allActivity.indexOf(data_info);
					listDataChildToRunIdCorrelation.put(pos, lruninfo.getRun_id_v());
					try {
						Calendar cdate = Calendar.getInstance();
						String thismonthcomparedate = String.format(Locale.US, "%04d-%02d-%02d %02d:%02d:%02d am", cdate.get(Calendar.YEAR), cdate.get(Calendar.MONTH) + 1, 1, 0, 0, 0);
						String lastmonthcomparedate = String.format(Locale.US, "%04d-%02d-%02d %02d:%02d:%02d am", cdate.get(Calendar.YEAR), cdate.get(Calendar.MONTH), 1, 0, 0, 0);
						//if(lruninfo.run_date_start_v.after(format.parse(thismonthcomparedate))) {
						if (lruninfo.getRun_date_start_v().after(format.parse(thismonthcomparedate))) {
							//thisMonth.add(String.format("run_id: %d, %s", lruninfo.run_id_v, lruninfo.run_date_start));
							thisMonth.add(getActivityInfo(lruninfo.getRun_id_v()));
						}
						if (lruninfo.getRun_date_start_v().after(format.parse(lastmonthcomparedate)) && lruninfo.getRun_date_start_v().before(format.parse(thismonthcomparedate))) {
							//lastMonth.add(String.format("run_id: %d, %s", lruninfo.run_id_v, lruninfo.run_date_start));
							lastMonth.add(getActivityInfo(lruninfo.getRun_id_v()));
						}
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
			} else {
				writeLog(String.format(Locale.US, "filteredActivityInfoMap: ckey:%d: missing", ckey));
			}
		}
		listDataChild.put(listDataHeader.get(0), thisMonth); // Header, Child data
		listDataChild.put(listDataHeader.get(1), lastMonth);
		listDataChild.put(listDataHeader.get(2), allActivity);
	}

	@Override
	public void onStart() {
		super.onStart();

		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		client.connect();
	}

	@Override
	public void onStop() {
		super.onStop();
		client.disconnect();
	}

	protected int sendEmail() {

		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("message/rfc822");
		i.putExtra(Intent.EXTRA_EMAIL, new String[]{"admin@runtracer.com"});
		i.putExtra(Intent.EXTRA_SUBJECT, "Suggestion");
		i.putExtra(Intent.EXTRA_TEXT, "");
		try {
			startActivity(Intent.createChooser(i, "Send mail..."));
		} catch (android.content.ActivityNotFoundException ex) {
			Toast.makeText(ActivitiesActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
		}
		return (0);
	}

	public void showRunChart() {
		Intent intent = new Intent(this, RunChartActivity.class);
		intent.putExtra("run_data", (RunData) activityInfoMap.get(this.selected_run_id));
		intent.putExtra("user_data", user_data);
		startActivityForResult(intent, SHOW_CHART);
	}

	/**
	 * Called when a view has been clicked.
	 *
	 * @param v The view that was clicked.
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.fab_show_chart:
				Snackbar.make(v, "Showing the chart for selected activity now.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
				showRunChart();
				break;

			case R.id.fab:
				sendEmail();
				Snackbar.make(v, "Sending email", Snackbar.LENGTH_LONG).setAction("Action", null).show();
				break;
		}
	}

	public class WebAppInterface {
		Context mContext;

		/**
		 * Instantiate the interface and set the context
		 */
		WebAppInterface(Context c) {
			mContext = c;
		}

		/**
		 * Show a toast from the web page
		 */
		@JavascriptInterface
		public void showToast(String toast) {
			Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
		}
	}
}
