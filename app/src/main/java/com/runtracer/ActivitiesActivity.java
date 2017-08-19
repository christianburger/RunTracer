package com.runtracer;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.appindexing.Action;
import com.google.firebase.appindexing.FirebaseUserActions;
import com.google.firebase.appindexing.builders.Actions;
import com.runtracer.model.RunData;
import com.runtracer.model.UserData;
import com.runtracer.sqlitedb.SqliteHandler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class ActivitiesActivity extends AppCompatActivity implements View.OnClickListener {
	private static final String TAG = "activities";
	private static final int SHOW_CHART = 1001;

	FloatingActionButton mEmail;
	FloatingActionButton mShowChart;

	private ExpandableListView mActivitiesList;
	private ExpandableListAdapter mActivityListAdapter;

	private AppBarLayout mAppBarLayout;
	private CollapsingToolbarLayout mCollapsingToolbar;
	private Toolbar mToolbar;
	private TextView mActivitySummary;

	private long selected_run_id;

	List<String> listDataHeader;
	HashMap<String, List<String>> listDataChild;
	HashMap<Long, Long> listDataChildToRunIdCorrelation;

	private HashMap activityInfoMap;
	private HashMap filteredActivityInfoMap;

	private SqliteHandler sqliteHandler;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_activities);
		this.setTitle(null);
		sqliteHandler = MainActivity.sqliteHandler;
		writeLog(String.format("ActivitiesActivity: onCreate(): sqlite size: %s", sqliteHandler.getAllRunSummaries(SqliteHandler.field_runid).size()));
		mEmail = (FloatingActionButton) findViewById(R.id.fab);
		mEmail.setOnClickListener(this);
		mEmail.hide();

		mShowChart = (FloatingActionButton) findViewById(R.id.fab_show_chart);
		mShowChart.setOnClickListener(this);
		mShowChart.hide();

		mAppBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
		mCollapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
		mCollapsingToolbar.setTitle(null);
		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		mToolbar.setTitle(null);
		mToolbar.setSubtitle(null);
		setSupportActionBar(mToolbar);
		activityInfoMap = new HashMap<>();
		filteredActivityInfoMap = new HashMap<>();
		UserData user_data = MainActivity.user_bio;
		listDataChildToRunIdCorrelation = new HashMap<>();
		getActivities();
		mActivitySummary = (TextView) findViewById(R.id.activity_summary);
		mActivitySummary.setText(R.string.title_activity_activities);
		mActivitySummary.setTextSize(32);
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
				showActivity((long) childPosition);
				return false;
			}
		});
		//getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		updateStats();
	}

	public ActivitiesActivity() {
	}

	public boolean showActivity(long childposition) {
		writeLog(String.format(Locale.US, "showActivity: received: %d", childposition));
		if (!listDataChildToRunIdCorrelation.isEmpty() && listDataChildToRunIdCorrelation.containsKey(childposition)) {
			writeLog(String.format(Locale.US, "showActivity: before this.selected_run_id: %d", this.selected_run_id));
			this.selected_run_id = listDataChildToRunIdCorrelation.get(childposition);
			writeLog(String.format(Locale.US, "showActivity: after this.selected_run_id: %d", this.selected_run_id));
			String info = "Selected Activity\n\n";
			info += this.getActivityInfo(this.selected_run_id);
			String fontSettings;
			mActivitySummary.setTextSize(16);
			mActivitySummary.setText(info);
			mAppBarLayout.setExpanded(false);
			mAppBarLayout.setFitsSystemWindows(true);
			mToolbar.setTitle(null);
			mToolbar.setSubtitle(null);
			mEmail.show();
			mShowChart.show();
		} else {
			return false;
		}
		return true;
	}

	public String getActivityInfo(long ckey) {
		String info = "";
		if (filteredActivityInfoMap.containsKey(ckey)) {
			//info += String.format(Locale.CANADA, "\tRunID: \t%d", ckey);
			RunData lrun;
			lrun = (RunData) filteredActivityInfoMap.get(ckey);
			lrun.getValues();
			String duration;
			long duration_v = (lrun.getRun_date_end_v().getTime() - lrun.getRun_date_start_v().getTime()) / 1000;
			duration = String.format(Locale.CANADA, "%d s", duration_v);
			//info += String.format(Locale.US, "\n\tTime: \t%s", lrun.getRun_date_start());
			info += String.format(Locale.US, "\t%s", lrun.getRun_date_start());
			info += String.format(Locale.US, "\n\tCalories: \t%.2f KCal ", lrun.getCalories_v_distance());
			info += String.format(Locale.US, "\n\tDuration: \t%s", duration);
			info += String.format(Locale.US, "\n\tDistance: %.2f Km ", lrun.getDistance_km_v());
		}
		writeLog(String.format("getActivityInfo: %s", info));
		return info;
	}

	public void writeLog(String msg) {
		String date = (DateFormat.format("dd-MM-yyyy hh:mm:ss a", new Date()).toString());
		Log.e(TAG, date + ": " + msg);
	}

	public int getActivities() {
		activityInfoMap = new HashMap();
		writeLog("getActivities: activityInfoMap.size(): " + activityInfoMap.size());
		if (sqliteHandler != null) {
			ArrayList<String> dataset = sqliteHandler.getAllRunSummaries(SqliteHandler.field_runid);
			writeLog(String.format("getActivities: dataset: %s", dataset));
			for (int i = 0; i < dataset.size(); i++) {
				String runid = dataset.get(i);
				long runid_v = Long.parseLong(runid);
				writeLog(String.format(Locale.CANADA, "getActivities: runid: %s     runid_v: %d      i: %d", runid, runid_v, i));
				RunData runData = sqliteHandler.getRunData(runid_v);
				writeLog(String.format(Locale.CANADA, "getActivities: runData: : %s", runData));
				if (runData != null && runData.getRun_id_v() == runid_v) {
					activityInfoMap.put(runid_v, runData);
					writeLog(String.format(Locale.CANADA, "getActivities: ADDING to activityInfoMap.put(runid_v: %d, runData: %s", runid_v, runData));
				}
			}
		}

		List keys = (List<Long>) new ArrayList(activityInfoMap.keySet());
		long ckey;
		RunData lruninfo;
		Iterator itk = keys.iterator();
		for (; itk.hasNext(); ) {
			ckey = (Long) itk.next();
			writeLog(String.format(Locale.US, "activityInfoMap: ckey: %d", ckey));
			lruninfo = sqliteHandler.getRunData(ckey);
			writeLog(String.format("activityInfoMap: run_date_start: %s", lruninfo.getRun_date_start()));
			writeLog(String.format("activityInfoMap: run_id: %s", lruninfo.getRun_id_v()));
			writeLog(String.format("activityInfoMap: calories: %s", lruninfo.getCalories_v_distance()));
			if (lruninfo.getRun_id_v() > 0) {
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
		long ckey;

		listDataHeader = new ArrayList<>();
		listDataChild = new HashMap<>();

		// Adding child data
		listDataHeader.add("This Month");
		listDataHeader.add("Last Month");
		listDataHeader.add("All Activity..");

		// Adding child data
		List<String> thisMonth = new ArrayList<>();
		List<String> lastMonth = new ArrayList<>();
		List<String> allActivity = new ArrayList<>();

		RunData lruninfo;
		Iterator itv = keys.iterator();
		for (; itv.hasNext(); ) {
			ckey = (long) itv.next();
			writeLog(String.format(Locale.US, "ckey: %d", ckey));
			if (filteredActivityInfoMap.containsKey(ckey)) {
				lruninfo = (RunData) filteredActivityInfoMap.get(ckey);
				writeLog(String.format(Locale.US, "filteredActivityInfoMap.get(%d): run_date_start: %s", ckey, lruninfo.getRun_date_start()));
				if ((lruninfo.getRun_id_v() > 0)) {
					String data_info = getActivityInfo(lruninfo.getRun_id_v());
					allActivity.add(data_info);
					long pos = (long) (allActivity.indexOf(data_info));
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

	private void updateStats() {
		try {
			ArrayList<String> results;
			results = sqliteHandler.getAllRunInstants(SqliteHandler.field_uid);
			results = sqliteHandler.getAllRunInstants(SqliteHandler.field_runid);
			results = sqliteHandler.getAllRunInstants(SqliteHandler.field_ctime);
			results = sqliteHandler.getAllRunInstants(SqliteHandler.field_current_motion_speed_km_h_v);
			results = sqliteHandler.getAllRunInstants(SqliteHandler.field_current_motion_distance_km_v);
			results = sqliteHandler.getAllRunInstants(SqliteHandler.field_current_gps_speed_km_h);
			results = sqliteHandler.getAllRunInstants(SqliteHandler.field_current_gps_distance_km);
			results = sqliteHandler.getAllRunInstants(SqliteHandler.field_calories_v_distance);
			results = sqliteHandler.getAllRunInstants(SqliteHandler.field_calories_v_heart_beat);
			results = sqliteHandler.getAllRunInstants(SqliteHandler.field_current_heart_rate);
			results = sqliteHandler.getAllRunInstants(SqliteHandler.field_longitude);
			results = sqliteHandler.getAllRunInstants(SqliteHandler.field_latitude);
			results = sqliteHandler.getAllRunInstants(SqliteHandler.field_altitude);
		} catch (NumberFormatException e) {
			writeLog("MainActivity: updateStats(): NUMBER FORMAT EXCEPTION: " + e.toString());
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		FirebaseUserActions.getInstance().start(getIndexApiAction());
	}

	@Override
	public void onStop() {
		super.onStop();// ATTENTION: This was auto-generated to implement the App Indexing API.
// See https://g.co/AppIndexing/AndroidStudio for more information.
		FirebaseUserActions.getInstance().end(getIndexApiAction());
	}

	protected int sendEmail() {

		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("message/rfc822");
		i.putExtra(Intent.EXTRA_EMAIL, new String[]{"admin@runtracer.com"});
		i.putExtra(Intent.EXTRA_SUBJECT, "Suggestion");
		i.putExtra(Intent.EXTRA_TEXT, "");
		try {
			startActivity(Intent.createChooser(i, "Send mail..."));
		} catch (ActivityNotFoundException ex) {
			Toast.makeText(ActivitiesActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
		}
		return (0);
	}

	public void showRunChart() {
		if (this.selected_run_id > 0) {
			Intent intent = new Intent(this, RunChartActivity.class);
			intent.putExtra("run_data", String.valueOf(this.selected_run_id));
			startActivityForResult(intent, SHOW_CHART);
		}
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

	/**
	 * ATTENTION: This was auto-generated to implement the App Indexing API.
	 * See https://g.co/AppIndexing/AndroidStudio for more information.
	 */
	public Action getIndexApiAction() {
		return Actions.newView("Activities", "http://[ENTER-YOUR-URL-HERE]");
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
