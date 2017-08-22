package com.runtracer;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import com.runtracer.model.RunData;
import com.runtracer.model.RunInstant;
import com.runtracer.model.UserData;
import com.runtracer.sqlitedb.SqliteHandler;
import com.runtracer.utilities.TypeCheck;

import java.io.UnsupportedEncodingException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Locale;

public class RunChartActivity extends AppCompatActivity implements View.OnClickListener {

	private static final String TAG = "runchart";
	private SqliteHandler sqliteHandler;

	String jscript = "";
	int no_xpoints = 0;
	int orientation = 0;
	boolean showingMap;

	//GUI elements
	WebView mChartView;
	WebSettings webSettings;

	FloatingActionButton mDrawMap;
	private RunData run_data;
	private UserData user_data;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_run_chart);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		sqliteHandler = MainActivity.sqliteHandler;
		user_data = MainActivity.user_bio;

		mDrawMap = (FloatingActionButton) findViewById(R.id.fab_draw_map);
		mDrawMap.setOnClickListener(this);
		/*
		mDrawChart= (FloatingActionButton) findViewById(R.id.fab_draw_chart);
		mDrawChart.setOnClickListener(this);
		*/

		mChartView = (WebView) findViewById(R.id.webView);
		mChartView.setBackgroundColor(Color.LTGRAY);
		mChartView.setPadding(0, 0, 0, 0);
		webSettings = mChartView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		mChartView.addJavascriptInterface(new WebAppInterface(this), "Android");
		mChartView.getSettings().setBuiltInZoomControls(true);
		mChartView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.CLOSE);

		orientation = getResources().getConfiguration().orientation;

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			String runid = extras.getString("run_data");
			long runid_v;
			if (TypeCheck.isNumber(runid)) {
				runid_v = Long.parseLong(runid);
				run_data = sqliteHandler.getRunData(runid_v);
				writeLog(String.format(Locale.CANADA, "RunChartActivity: received: runid: %s", runid));
				writeLog(String.format(Locale.CANADA, "RunChartActivity: received: runid_v: %d", runid_v));
				writeLog(String.format(Locale.CANADA, "RunChartActivity: received: run_data: %s", run_data.toString()));
			}
			//(UserData) extras.getSerializable("user_data");
			writeLog(String.format(Locale.CANADA, "RunChartActivity: calling updateWebView: uid: %s", run_data.getUid()));
			updateWebview();
		}
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
	}

	boolean updateWebview() {
		writeLog(String.format(Locale.CANADA, "RunChartActivity: updateWebview: uid: %s", run_data.getUid()));
		if (run_data != null) {
			try {
				writeLog(String.format(Locale.CANADA, "RunChartActivity: updateWebview: getDateValues: %s", run_data.getDateValues()));
				no_xpoints = run_data.getDateValues();
				writeLog(String.format(Locale.CANADA, "RunChartActivity: updateWebview: no_xpoints: %s", no_xpoints));
				if (showingMap) {
					jscript = generateJSMapCode(1200 + no_xpoints, 800);
					mChartView.setInitialScale(getScaleY(1200));
				} else {
					jscript = generateJSCode(1200 + no_xpoints, 800);
					mChartView.setInitialScale(getScaleY(1200 * orientation));
				}
				mChartView.getSettings().setLoadWithOverviewMode(true);
				mChartView.getSettings().setUseWideViewPort(true);
				mChartView.loadData(jscript, "text/html", null);
			} catch (ParseException | UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return (true);
	}

	private String generateMaterialChart(int c_width, int c_height) throws UnsupportedEncodingException {
		long starting_time = run_data.getRun_date_start_v().getTime();
		NumberFormat nf = NumberFormat.getInstance(Locale.CANADA);

		String code = "";
		code += "\n";
		code += "<script type=\"text/javascript\" src=\"https://www.gstatic.com/charts/loader.js\"></script>";
		code += "\n";
		code += "<br><br>";
		code += "\n";
		code += "<div id=\"chart_div\"></div>";
		code += "\n";

		code += "<script type=\"text/javascript\">";
		code += "\n";
		code += "google.charts.load('current', {'packages':['line', 'corechart']});";
		code += "\n";
		code += "google.charts.setOnLoadCallback(drawChart);";
		code += "\n";

		code += "function drawChart() {";
		code += "\n";
		code += "  var button = document.getElementById('change-chart');";
		code += "\n";
		code += "  var chartDiv = document.getElementById('chart_div');";
		code += "\n";
		code += "  var data = new google.visualization.DataTable();";
		code += "\n";
		code += "  data.addColumn('number', 'Time');";
		code += "\n";
		code += "  data.addColumn('number', 'Speed');";
		code += "\n";
		code += "  data.addColumn('number', \"Heart Rate\");";
		code += "\n";
		code += "  data.addColumn('number', \"Calories from distance)\");";
		code += "\n";
		code += "  data.addColumn('number', \"Calories from heart rate)\");";
		code += "\n";
		code += "  data.addRows([";
		code += "\n";


		ArrayList<RunInstant> list = sqliteHandler.getRunInstants(run_data.getRun_id_v());
		for (RunInstant runInstant : list) {
			double speed;
			if (user_data.getMetric().compareToIgnoreCase("metric") == 0) {
				speed = runInstant.getCurrent_motion_speed_km_h_v();
			} else {
				speed = runInstant.getCurrent_motion_speed_km_h_v() * run_data.getConv_km_miles();
			}
			writeLog(String.format(Locale.CANADA, "RunChartActivity: speed: %.4f  nf.format(speed): %s", speed, nf.format(speed)));
			code += String.format(Locale.CANADA, "\n[%d, %s, %s, %s, %s],", (runInstant.getCtime() - starting_time) / 1000, nf.format(speed), runInstant.getCurrent_heart_rate(), nf.format(runInstant.getCalories_v_distance()), nf.format(runInstant.getCalories_v_heart_beat()));
		}


		code += "\n";
		code += "  ]);";
		code += "\n";
		code += "  var materialOptions = {";
		code += "\n";
		code += "    chart: {";

		code += "\n";
		code += String.format("			title: 'Details of your run on %s', ", run_data.getRun_date_start());
		code += "\n";

		code += "    },";

		code += "\n";
		code += String.format(Locale.CANADA, "		width:%d,", c_width);
		code += "\n";
		code += String.format(Locale.CANADA, "		height:%d,", c_height);
		code += "\n";


		code += "\n";
		code += "    series: {";
		code += "\n";

		// Gives each series an axis name that matches the Y-axis below.";
		code += "      0: {axis: 'Speed'},";
		code += "\n";
		code += "      1: {axis: 'HeartRate'},";
		code += "\n";
		code += "      2: {axis: 'CaloriesDistance'},";
		code += "\n";
		code += "      3: {axis: 'CaloriesHeart'}";
		code += "\n";
		code += "    },";
		code += "\n";
		code += "    axes: {";
		code += "\n";

		// Adds labels to each axis; they don't have to match the axis names.";
		code += "      y: {";
		code += "\n";
		code += "        Speed: {label: 'Speed (km/h)'},";
		code += "\n";
		code += "        HeartRate: {label: 'Heart Rate (Hz)'},";
		code += "\n";
		code += "        CaloriesDistance: {label: 'Calories from Distance (kCal)'},";
		code += "\n";
		code += "        CaloriesHeart: {label: 'Calories from Heart Rate (kCal)'}";
		code += "\n";
		code += "      },";
		code += "\n";
		code += "    }";
		code += "\n";
		code += "  };";
		code += "\n";
		code += "  function drawMaterialChart() {";
		code += "\n";
		code += "    var materialChart = new google.charts.Line(chartDiv);";
		code += "\n";
		code += "    materialChart.draw(data, materialOptions);";
		code += "\n";
		code += "  }";
		code += "\n";
		code += "  drawMaterialChart();";
		code += "\n";
		code += "}";
		code += "\n";
		code += "</script>";
		code += "\n";

		writeLog(String.format(Locale.CANADA, "RunChartActivity: generateMaterialChart(%d, %d): code: %s", c_width, c_height, code));
		return code;
	}

	private String generateJSCode(int c_width, int c_height) throws UnsupportedEncodingException {
		String code = "";
		String material = "";
		writeLog(String.format(Locale.CANADA, "RunChartActivity: generateJSCode(%d, %d)", c_width, c_height));
		material = this.generateMaterialChart(c_width, c_height);
		long starting_time = run_data.getRun_date_start_v().getTime();
		NumberFormat nf = NumberFormat.getInstance(Locale.CANADA);
		code += "\n";
		code += "<html>";
		code += "\n";
		code += "<head>";
		code += "\n";
		code += "<script type=\"text/javascript\" src=\"https://www.google.com/jsapi\"></script>";
		code += "\n";
		code += "	<script type=\"text/javascript\">";
		code += "\n";
		code += "		google.load('visualization', '1.1', {packages: ['line']});";
		code += "\n";
		code += "	google.setOnLoadCallback(drawChart);";
		code += "\n";
		code += "	function drawChart() {";
		code += "\n";
		code += "		var data = new google.visualization.DataTable();";
		code += "\n";
		code += "		data.addColumn('number', 'Time (seconds)');";
		code += "\n";
		code += "		data.addColumn('number', 'Heart Beat (Hz)');";
		code += "\n";
		if (user_data.getMetric().compareToIgnoreCase("metric") == 0) {
			code += "		data.addColumn('number', 'Speed (km/h)');";
		} else {
			code += "		data.addColumn('number', 'Speed (miles/h)');";
		}
		code += "\n";
		code += "		data.addColumn('number', 'Calories (distance) (KCal)');";
		code += "\n";
		code += "		data.addColumn('number', 'Calories (heart rate) (KCal)');";
		code += "\n";
		code += "		data.addRows([";
		code += "\n";

		ArrayList<RunInstant> list = sqliteHandler.getRunInstants(run_data.getRun_id_v());
		for (RunInstant runInstant : list) {
			double speed;
			if (user_data.getMetric().compareToIgnoreCase("metric") == 0) {
				speed = runInstant.getCurrent_motion_speed_km_h_v();
			} else {
				speed = runInstant.getCurrent_motion_speed_km_h_v() * run_data.getConv_km_miles();
			}
			writeLog(String.format(Locale.CANADA, "RunChartActivity: speed: %.4f  nf.format(speed): %s", speed, nf.format(speed)));
			code += String.format(Locale.CANADA, "\n[%d, %d, %s, %s, %s],", (runInstant.getCtime() - starting_time) / 1000, runInstant.getCurrent_heart_rate(), nf.format(speed), nf.format(runInstant.getCalories_v_distance()), nf.format(runInstant.getCalories_v_heart_beat()));
		}

		code += "\n";
		code += "		]);";
		code += "\n";
		code += "		var options = {";
		code += "\n";
		code += "			chart: {";
		code += "\n";
		code += String.format("			title: 'Details of your run on %s', ", run_data.getRun_date_start());
		code += "\n";
		code += "   curveType: 'function'";
		code += "\n";
		code += "		},";
		code += "\n";
		code += String.format(Locale.CANADA, "		width:%d,", c_width);
		code += "\n";
		code += String.format(Locale.CANADA, "		height:%d,", c_height);
		code += "\n";
		code += "			axes: {";
		code += "\n";
		code += "			x: {";
		code += "\n";
		code += "				0: {side: 'bottom'}";
		code += "\n";
		code += "			}";
		code += "\n";
		code += "		}";
		code += "\n";
		code += "		};";
		code += "\n";
		code += "		var chart = new google.charts.Line(document.getElementById('line_top_x'));";
		code += "\n";
		code += "		chart.draw(data, options);";
		code += "\n";
		code += "	}";
		code += "\n";
		code += "	</script>";
		code += "\n";
		code += "	</head>";
		code += "\n";
		code += "	<body>";
		code += "\n";
		code += "	<div id=\"line_top_x\"></div>";
		code += "\n";
		code += "	</body>";
		code += "\n";
		code += "	</html>";
		code += "\n";

		writeLog(String.format("code: %s", code));
		return (material);
	}

	private String generateJSMapCode(int c_width, int c_height) throws UnsupportedEncodingException {
		writeLog(String.format(Locale.CANADA, "RunChartActivity: generateJSMapCode(%d, %d)", c_width, c_height));
		String code = "";
		NumberFormat nf = NumberFormat.getInstance(Locale.CANADA);
		code += "\n";
		code += "	<html> ";
		code += "\n";
		code += "	<head> ";
		code += "\n";
		code += "	<script type=\"text/javascript\" src=\"https://www.google.com/jsapi\"></script> ";
		code += "\n";
		code += "	<script type=\"text/javascript\"> ";
		code += "\n";
		code += "		google.load(\"visualization\", \"1\", {packages:[\"map\"]}); ";
		code += "\n";
		code += "	google.setOnLoadCallback(drawChart); ";
		code += "\n";
		code += "	function drawChart() { ";
		code += "\n";
		code += "		var data = google.visualization.arrayToDataTable([ ";
		code += "\n";
		code += "		['Lat', 'Long', 'Name'], ";
		code += "\n";
		ArrayList<RunInstant> list = sqliteHandler.getRunInstants(run_data.getRun_id_v());
		for (RunInstant runInstant : list) {
			code += String.format(Locale.CANADA, "\n[%s, %s, 'time: %d" + "s'],", nf.format(runInstant.getLatitude()), nf.format(runInstant.getLongitude()), (runInstant.getCtime() - run_data.getRun_date_start_v().getTime()) / 1000);
		}
		//	code += "		[37.4422, -122.1731, 'Shopping'] ";
		code += "\n";
		code += "		]); ";
		code += "\n";
		code += "		var map = new google.visualization.Map(document.getElementById('map_div')); ";
		code += "\n";
		code += "		map.draw(data, {showTip: true}); ";
		code += "\n";
		code += "	} ";
		code += "\n";
		code += "	</script> ";
		code += "\n";
		code += "	</head> ";
		code += "\n";
		code += "	<body> ";
		code += "\n";
		code += String.format(Locale.CANADA, "	<div id=\"map_div\" style=\"width: %dpx; height: %dpx\"></div> ", c_width, c_height);
		code += "\n";
		code += "	</body> ";
		code += "\n";
		code += "	</html> ";
		code += "\n";

		writeLog(String.format("code: %s", code));
		return (code);
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

	public void writeLog(String msg) {
		String date = (DateFormat.format("dd-MM-yyyy hh:mm:ss", new java.util.Date()).toString());
		Log.e(TAG, date + ": " + msg);
	}

	/**
	 * Called when a view has been clicked.
	 *
	 * @param v The view that was clicked.
	 */
	@Override
	public void onClick(View v) {
		try {
			switch (v.getId()) {
				/*
				case R.id.fab_draw_chart:
					jscript = generateJSCode(1200 + no_xpoints, 800);
					mChartView.setInitialScale(getScaleY(1200));
					mChartView.loadData(jscript, "text/html", null);
					break;
				*/
				case R.id.fab_draw_map:
					showingMap = !showingMap;
					updateWebview();
					break;

				default:
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private class WebAppInterface {
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
