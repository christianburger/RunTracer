package com.runtracer;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.runtracer.model.RunData;
import com.runtracer.model.RunInstant;
import com.runtracer.model.UserData;
import com.runtracer.services.BluetoothLeService;
import com.runtracer.sqlitedb.SqliteHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;


public class RunActivity extends Activity implements View.OnClickListener, SensorEventListener, LoaderManager.LoaderCallbacks<Cursor>, View.OnLongClickListener, LocationListener {

	private SqliteHandler sqliteHandler;
	private static final int REQUEST_PERMISSIONS = 1334;
	private static final String[] permissions = {
		Manifest.permission.ACCESS_FINE_LOCATION,
		Manifest.permission.ACCESS_COARSE_LOCATION
	};

	String status = "----";
	Sensor accelerometer;
	SensorManager sm;

	Vector accelerometer_vector;

	private LocationManager locationManager;
	Location previousLocation;

	private static final String TAG = "runtracer";

	private UserData user_bio;
	private RunData run_data;

	public int heart_rate_at_peak;
	public int heart_rate_at_rest;
	public boolean bMeasuredPeak;

	long last_time = 0;
	long this_time = 0;

	private long time_calories_now;
	private long time_calories_last;

	public double threshold_modulus = 2.00;
	public double threshold_begin = 0;
	public double threshold_end = 0;

	public final int fifo_sz_angle = 200;
	public final int fifo_sz_speed = 200;
	public final int fifo_sz_accel = 400;

	public final int STATE_INITIAL = 0;
	public final int STATE_OUTSIDE = 10;
	public final int STATE_INSIDE = 20;

	private final int IDLE = 0;
	private final int WALKING = 1;
	private final int RUNNING = 2;
	private final int PAUSED = 3;

	boolean isRegistered;

	public int state = STATE_INITIAL;
	private int user_status;

	private long time_start = 0;
	private long time_end = 0;

	private double accelerometer_last_value = 0;

	//GUI elements
	private Button btn_start;
	private Button btn_measure_inclination;
	private Button btn_calibrate;

	private Switch btn_indoor;

	private Chronometer mChronometer;
	private ProgressBar mAccelerationBar;

	private TextView acceleration;

	private TextView mCurrentInclination;
	private TextView mRunInclination;

	private TextView mSpeedMotion;
	private TextView mSpeedGPS;

	private TextView mCaloriesDistance;
	private TextView mCaloriesHeartBeat;

	private TextView mDistanceGPS;
	private TextView mDistanceMotion;

	private TextView mDistanceUnitsMotion;
	private TextView mSpeedUnitsMotion;

	private TextView mDistanceUnitsGPS;
	private TextView mSpeedUnitsGPS;

	TableLayout mTableLayout0;
	TableLayout mTableLayout1;
	TableLayout mTableLayout2;
	TableLayout mTableLayout3;
	TableLayout mTableLayout4;
	TableLayout mTableLayout5;

	TableRow mMotionDistanceRow;
	TableRow mMotionSpeedRow;

	TableRow mGPSDistanceRow;
	TableRow mGPSSpeedRow;

	private String mDeviceAddress;
	private BluetoothLeService mBluetoothLeService;

	private LinkedList<Double> fifo_grade = new LinkedList<>();
	private LinkedList<Double> fifo_acceleration = new LinkedList<>();
	private LinkedList<Double> fifo_speed = new LinkedList<>();

	private double longitude;
	private double latitude;
	private double altitude;
	private double gps_speed;

	public long availableMemory() {
		ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
		ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		activityManager.getMemoryInfo(mi);
		return (mi.availMem);
	}

	public long totalMemory() {
		ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
		ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		activityManager.getMemoryInfo(mi);
		return (mi.totalMem);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		writeLog("onRequestPermissionsResult(): Yey, permissions granted!!!");
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		writeLog("onRequestPermissionsResult() step 02 ...");
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			this.finishRunNow();
			return;
		}
		writeLog("onRequestPermissionResult() step 03 ...");
		previousLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		writeLog("onRequestPermissionResult() step 04 ...");
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 800, 10, this);
		writeLog("onRequestPermissionResult() step 05 ...");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestPermissions(permissions, REQUEST_PERMISSIONS);
		writeLog("onCreate(), starting now...");
		sqliteHandler = MainActivity.sqliteHandler;
		this.sqliteHandler.getReadableDatabase();
		if (this.sqliteHandler.checkDataBase()) {
			ArrayList<String> result = this.sqliteHandler.getAllRunSummaries(SqliteHandler.field_date_start);
			for (String key : result) {
				writeLog(String.format(Locale.CANADA, "RunActivity: onCreate: checking sqlite: key: %s", key));
			}
		}
		accelerometer_vector = new Vector();
		writeLog("onCreate() step 01 ...");
		setContentView(R.layout.activity_run);
		writeLog("onCreate() step 08 ...");
		user_status = IDLE;
		fifo_grade.clear();
		fifo_acceleration.clear();
		fifo_speed.clear();
		writeLog("onCreate(), new RunData()...");
		user_bio = MainActivity.user_bio;
		user_bio.getValues();
		run_data = new RunData();
		writeLog("onCreate(), after new RunData()...");
		run_data.setRun_id_v(new Date().getTime());
		run_data.setCalories_v_distance(0.0);
		run_data.setCalories_v_heart_beat(0.0);
		run_data.setDistance_km_v(0.0);
		run_data.setAverage_speed_km_h_v(0.0);
		run_data.setAverage_speed_miles_h_v(0.0);
		this_time = new Date().getTime();
		last_time = new Date().getTime();
		time_calories_now = new Date().getTime();
		time_calories_last = new Date().getTime();
		run_data.getValues();
		run_data.setCurrent_weight_v(user_bio.getCurrent_weight_v());
		run_data.setCurrent_fat_v(user_bio.getCurrent_fat_v());
		writeLog("onCreate(), before setupGui()...");
		setupGui();
		writeLog("onCreate(), after setupGui()...");
		sm = (SensorManager) getSystemService(SENSOR_SERVICE);
		accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sm.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
		writeLog("onCreate(), end of onCreate()...");
	}

	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
		return intentFilter;
	}

	private boolean measureRecoveryHeartRate() {
		if (run_data.getCurrent_heart_rate() > user_bio.getResting_hr() && user_bio.getRecovery_hr() > user_bio.getRESTING_HR_MIN()) {
			this.bMeasuredPeak = true;
			this.heart_rate_at_peak = run_data.getCurrent_heart_rate();
			this.heart_rate_at_rest = run_data.getCurrent_heart_rate();
		}
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		// set title
		alertDialogBuilder.setTitle("Measure Heart Recovery Rate?");
		alertDialogBuilder.setMessage("Stay idle for 1 minute, measuring your heart rate.").setCancelable(false).setNegativeButton("NO", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// if this button is clicked, close current activity
				RunActivity.this.finishRunNow();
			}
		})
			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					// if this button is clicked, just close the dialog box and do nothing
					dialog.cancel();
				}
			});
		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();
		// show it
		alertDialog.show();
		return false;
	}

	private void finishRun() {
		writeLog("finishRun!!");
		sm.unregisterListener(this);
		locationManager.removeUpdates(this);
		if (run_data.getCurrent_heart_rate() > 10) {
			measureRecoveryHeartRate();
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					if (bMeasuredPeak && run_data.getCurrent_heart_rate() > user_bio.getTarget_hr_moderate() && (heart_rate_at_peak - run_data.getCurrent_heart_rate()) > 0) {
						heart_rate_at_rest = run_data.getCurrent_heart_rate();
						run_data.setRecovery_hr(heart_rate_at_peak - heart_rate_at_rest);
					}
					finishRunNow();
				}
			}, 60000);
		} else {
			writeLog("finishRun 01!!");
			run_data.setRecovery_hr(0);
			finishRunNow();
		}
	}

	private void finishRunNow() {
		writeLog("finishRunNow!!");
		storeRunData();
		if (run_data.getRecovery_hr() > 0) {
			MainActivity.user_bio.setRecovery_hr(run_data.getRecovery_hr());
		}
		this.setResult(RESULT_OK);
		this.finish();
		int RUN_USER_DATA = 1002;
		this.finishActivity(RUN_USER_DATA);
	}

	private boolean storeRunData() {
		JSONObject runinfo = run_data.toJSON();
		try {
			runinfo.put("uid", user_bio.getUid());
			sqliteHandler.addRunSummary(runinfo);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return true;
	}

	protected void onResume() {
		super.onResume();
		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
		isRegistered = true;
		writeLog("RunActivity: registerReceiver: " + mGattUpdateReceiver);

		if (mBluetoothLeService != null) {
			final boolean result = mBluetoothLeService.connect(mDeviceAddress);
			writeLog("RunActivity: Connect request result=" + result);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	protected void onPause() {
		super.onPause();
		writeLog("RunActivity: onPause...");
	}

	protected void onStop() {
		super.onStop();
		if (isRegistered) {
			unregisterReceiver(mGattUpdateReceiver);
			isRegistered = false;
		}
		writeLog("RunActivity: onStop...");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (isRegistered) {
			unregisterReceiver(mGattUpdateReceiver);
			isRegistered = false;
		}
		writeLog("RunActivity: onDestroy...");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_run, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private double getAvgGrade(double current_angle) {
		double sum_grade = 0;
		double avg_grade;
		if (fifo_grade.size() > fifo_sz_angle) {
			fifo_grade.removeFirst();
		}
		fifo_grade.add(current_angle);
		for (int i = 0; i < (fifo_grade.size()); i++) {
			sum_grade = fifo_grade.get(i) + sum_grade;
		}
		avg_grade = sum_grade / fifo_grade.size();
		return avg_grade;
	}

	private double getAvgAcceleration(double current_acceleration) {
		double sum_acceleration = 0;
		double avg_acceleration;
		if (fifo_acceleration.size() > fifo_sz_accel) {
			fifo_acceleration.removeFirst();
		}
		fifo_acceleration.add(current_acceleration);
		for (int i = 0; i < (fifo_acceleration.size()); i++) {
			sum_acceleration = fifo_acceleration.get(i) + sum_acceleration;
		}
		avg_acceleration = sum_acceleration / fifo_acceleration.size();
		return avg_acceleration;
	}

	private double getAvgSpeed(double current_speed) {
		double sum_speed = 0;
		double avg_speed;
		double human_speed_limit = 40;
		if (current_speed > human_speed_limit) {
			current_speed = human_speed_limit;
		}
		if (fifo_speed.size() > fifo_sz_speed) {
			fifo_speed.removeFirst();
		}
		fifo_speed.add(current_speed);
		for (int i = 0; i < (fifo_speed.size()); i++) {
			sum_speed = fifo_speed.get(i) + sum_speed;
		}
		avg_speed = sum_speed / fifo_speed.size();
		return avg_speed;
	}

	private final ServiceConnection mServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName componentName, IBinder service) {
			mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
			if (!mBluetoothLeService.initialize()) {
				finish();
			}
			writeLog("RunActivity: Bluetooth LE Service initialized: " + mDeviceAddress);
			mBluetoothLeService.connect(mDeviceAddress);
			writeLog("RunActivity: Bluetooth LE Service connected: " + mDeviceAddress);
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			mBluetoothLeService = null;
			writeLog("RunActivity: Bluetooth LE Service disconnected: " + mDeviceAddress);
		}
	};

	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (!BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
				if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
					run_data.setCurrent_heart_rate(-1);
				} else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
					displayGattServices(mBluetoothLeService.getSupportedGattServices());
					writeLog((mBluetoothLeService.getSupportedGattServices()).toString());
				} else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
					String data = (intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
					TextView t = findViewById(R.id.heart_rate_value);
					t.setText(data);
					run_data.setCurrent_heart_rate(Integer.parseInt(data));
				}
			}
		}
	};

	private void displayGattServices(List<BluetoothGattService> gattServices) {
		if (gattServices == null) return;
		String uuid;
		String unknownServiceString = getResources().getString(R.string.unknown_service);
		String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
		for (BluetoothGattService gattService : gattServices) {
			HashMap<String, String> currentServiceData = new HashMap<>();
			uuid = gattService.getUuid().toString();
			writeLog("new BluetoothGattService: " + uuid);
			String LIST_NAME = "NAME";
			currentServiceData.put(LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
			String LIST_UUID = "UUID";
			currentServiceData.put(LIST_UUID, uuid);
			writeLog("new gattServiceData: " + currentServiceData.toString());
			List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
			for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
				HashMap<String, String> currentCharaData = new HashMap<>();
				uuid = gattCharacteristic.getUuid().toString();
				currentCharaData.put(LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
				currentCharaData.put(LIST_UUID, uuid);
				writeLog("new item for currentCharaData: " + currentCharaData.toString());
				if (gattCharacteristic.getUuid().toString().matches("00002a37-0000-1000-8000-00805f9b34fb")) {
					mBluetoothLeService.setCharacteristicNotification(gattCharacteristic, true);
					writeLog("found item for gattCharacteristic matching: " + "00002a37-0000-1000-8000-00805f9b34fb: " + gattCharacteristic.getUuid().toString());
				}
			}
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		String bluetoothDeviceName = "";
		writeLog("RunActivity: onActivityResult");
		if (resultCode == RESULT_OK) {
			bluetoothDeviceName = data.getStringExtra("BluetoothDeviceName");
			writeLog("RunActivity: bluetoothDeviceName received from scan activity: " + bluetoothDeviceName);
		}
		try {
			writeLog("RunActivity: bluetoothDeviceName being processed " + bluetoothDeviceName);
			//mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			// Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
			// BluetoothAdapter through BluetoothManager.
			final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
			BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();
			// Checks again if Bluetooth is supported on the device.
			if (mBluetoothAdapter == null) {
				Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
				finish();
				return;
			}
			BluetoothDevice btdevice = mBluetoothAdapter.getRemoteDevice(bluetoothDeviceName);
			mDeviceAddress = btdevice.getAddress();
			writeLog("RunActivity: bluetoothDeviceName parsed from scan activity: " + btdevice.toString());
			Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
			bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
		} catch (Exception e) {
			e.getStackTrace();
		}
		//final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(bluetoothDeviceName);
		//writeLog("RunActivity: bluetoothDeviceName parsed from scan activity: " + device.toString());
	}

	private void setupGui() {

		mTableLayout0 = findViewById(R.id.table_layout_0);
		mTableLayout1 = findViewById(R.id.table_layout_1);
		mTableLayout2 = findViewById(R.id.table_layout_2);
		mTableLayout3 = findViewById(R.id.table_layout_3);
		mTableLayout4 = findViewById(R.id.table_layout_4);
		mTableLayout5 = findViewById(R.id.table_layout_5);

		mMotionDistanceRow = findViewById(R.id.motion_distance_row);
		mMotionSpeedRow = findViewById(R.id.motion_speed_row);

		mGPSDistanceRow = findViewById(R.id.gps_distance_row);
		mGPSSpeedRow = findViewById(R.id.gps_speed_row);

		acceleration = findViewById(R.id.acceleration);

		mSpeedMotion = findViewById(R.id.speed_ui);
		mSpeedGPS = findViewById(R.id.gps_speed_value);

		mCaloriesDistance = findViewById(R.id.calories_distance_value);
		mCaloriesHeartBeat = findViewById(R.id.calories_heart_beat_value);

		mDistanceGPS = findViewById(R.id.gps_distance_value);
		mDistanceMotion = findViewById(R.id.distance_value);

		mDistanceUnitsMotion = findViewById(R.id.distance_units);
		mDistanceUnitsGPS = findViewById(R.id.gps_distance_units);

		mSpeedUnitsMotion = findViewById(R.id.speed_units);
		mSpeedUnitsGPS = findViewById(R.id.gps_speed_units);

		btn_start = findViewById(R.id.btn_start_run);
		btn_start.setOnClickListener(this);
		btn_start.setOnLongClickListener(this);

		btn_start.setEnabled(true);

		Button btn_done_run = findViewById(R.id.btn_done_run);
		btn_done_run.setOnClickListener(this);
		btn_done_run.setEnabled(true);

		btn_indoor = findViewById(R.id.run_indoor_value);
		btn_indoor.setOnClickListener(this);
		btn_indoor.setEnabled(true);

		btn_measure_inclination = findViewById(R.id.measure_inclination_button);
		btn_measure_inclination.setOnClickListener(this);
		btn_measure_inclination.setEnabled(true);

		btn_calibrate = findViewById(R.id.calibrate_button);
		btn_calibrate.setOnClickListener(this);
		btn_calibrate.setEnabled(true);

		mCurrentInclination = findViewById(R.id.measure_inclination_value);
		mCurrentInclination.setOnClickListener(this);
		mCurrentInclination.setEnabled(true);

		mRunInclination = findViewById(R.id.run_inclination_value);
		mRunInclination.setOnClickListener(this);
		mRunInclination.setEnabled(true);

		mChronometer = findViewById(R.id.time_value);

		mCaloriesDistance = findViewById(R.id.calories_distance_value);
		mDistanceMotion = findViewById(R.id.distance_value);

		mDistanceUnitsMotion = findViewById(R.id.distance_units);
		mDistanceUnitsMotion.setEnabled(true);

		mSpeedUnitsMotion = findViewById(R.id.speed_units);
		mSpeedUnitsMotion.setEnabled(true);

		mAccelerationBar = findViewById(R.id.accelerationBar);
		mAccelerationBar.setMax(120);

		this.updateGui();
	}

	public void updateGui() {
		if (user_bio.getMetric().compareToIgnoreCase("metric") == 0) {
			mDistanceUnitsMotion.setText(R.string.unit_km);
			mDistanceUnitsGPS.setText(R.string.unit_km);
			mSpeedUnitsMotion.setText(R.string.unit_km_h);
			mSpeedUnitsGPS.setText(R.string.unit_km_h);
		} else {
			mDistanceUnitsMotion.setText(R.string.unit_miles);
			mDistanceUnitsGPS.setText(R.string.unit_miles);
			mSpeedUnitsMotion.setText(R.string.unit_miles_h);
			mSpeedUnitsGPS.setText(R.string.unit_miles_h);
		}

		if (btn_indoor.isChecked()) {
			btn_indoor.setText(R.string.run_indoor_on);

			mSpeedGPS.setEnabled(false);
			mDistanceGPS.setEnabled(false);
			mSpeedMotion.setEnabled(true);
			mDistanceMotion.setEnabled(true);

			mDistanceUnitsGPS.setEnabled(false);
			mSpeedUnitsGPS.setEnabled(false);

			mDistanceUnitsMotion.setEnabled(true);
			mSpeedUnitsMotion.setEnabled(true);

			mMotionDistanceRow.setBackgroundColor(Color.YELLOW);
			mMotionSpeedRow.setBackgroundColor(Color.YELLOW);

			mGPSDistanceRow.setBackgroundColor(Color.LTGRAY);
			mGPSSpeedRow.setBackgroundColor(Color.LTGRAY);

		} else {
			btn_indoor.setText(R.string.run_indoor_off);

			mSpeedGPS.setEnabled(true);
			mDistanceGPS.setEnabled(true);
			mSpeedMotion.setEnabled(false);
			mDistanceMotion.setEnabled(false);

			mDistanceUnitsGPS.setEnabled(true);
			mSpeedUnitsGPS.setEnabled(true);

			mDistanceUnitsMotion.setEnabled(false);
			mSpeedUnitsMotion.setEnabled(false);

			mMotionDistanceRow.setBackgroundColor(Color.LTGRAY);
			mMotionSpeedRow.setBackgroundColor(Color.LTGRAY);

			mGPSDistanceRow.setBackgroundColor(Color.YELLOW);
			mGPSSpeedRow.setBackgroundColor(Color.YELLOW);
		}
	}

	public boolean onLongClick(View v) {
		try {
			switch (v.getId()) {
				case R.id.btn_start_run:
					writeLog("onLongClick...");
					break;
			}
		} catch (Exception e) {
			writeLog("onLongClick Exception: " + e.getMessage());
		}
		return false;
	}

	public void onClick(View v) {
		try {
			switch (v.getId()) {
				case R.id.btn_start_run:
					startChronometer(v);
					break;

				case R.id.btn_done_run:
					writeLog("Pressed button btn_done_run.");
					run_data.setEndTime();
					writeLog("this.finishRun() being called now.");
					this.finishRun();
					break;

				case R.id.run_indoor_value:
					run_data.setThreadmill_factor((btn_indoor.isChecked()) ? 0 : 0.84);
					writeLog(String.format(Locale.US, "threadmill factor= %.2f", run_data.getThreadmill_factor()));
					updateGui();
					break;

				case R.id.measure_inclination_button:
					writeLog("Pressed button measure inclination.");
					mRunInclination.setText(String.format(Locale.getDefault(), "%.2f", accelerometer_vector.avgGrade - accelerometer_vector.gradeOffset));
					run_data.setInclination((int) (accelerometer_vector.avgGrade - accelerometer_vector.gradeOffset));
					break;

				case R.id.calibrate_button:
					accelerometer_vector.gradeOffset = accelerometer_vector.avgGrade;
					writeLog(String.format(Locale.US, "Calibrating inclination value to %.2f", accelerometer_vector.gradeOffset));
					break;

				case R.id.measure_inclination_value:
					writeLog("Text Inclination Clicked.");
					break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void showElapsedTime() {
	}

	/**
	 * Called when the location has changed.
	 * <p/>
	 * <p> There are no restrictions on the use of the supplied Location object.
	 *
	 * @param location The new location, as a Location object.
	 */
	@Override
	public void onLocationChanged(Location location) {
		if (location != null) {
			this.latitude = location.getLatitude();
			this.longitude = location.getLongitude();
			if (location.hasAltitude()) {
				this.altitude = location.getAltitude();
			}
			if (location.hasSpeed()) {
				this.gps_speed = location.getSpeed() * run_data.getConv_m_s_km_h();
			} else {
				this.gps_speed = 0;
			}
			if (previousLocation != null) {
				if (previousLocation.hasSpeed()) {
					run_data.setGps_distance_km(run_data.getGps_distance_km() + location.distanceTo(previousLocation) / 1000);
				}
			}
			previousLocation = location;
		}
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		String str = String.format(Locale.US, "Latitude: %.2f \nLongitude: %.2f \nStatus: %d", this.latitude, this.longitude, status);
		writeLog(String.format(Locale.US, "RunActivity: onStatusChanged: %s", str));
	}

	@Override
	public void onProviderEnabled(String provider) {
		Toast.makeText(getBaseContext(), "Gps turned on", Toast.LENGTH_LONG).show();
	}

	@Override
	public void onProviderDisabled(String provider) {
		Toast.makeText(getBaseContext(), "Gps turned off ", Toast.LENGTH_LONG).show();
	}

	private class Vector {
		double x = 0.0;
		double y = 0.0;
		double z = 0.0;
		double gradeOffset = 0.0;
		double avgGrade = 0.0;

		Vector() {
			this.x = 0.0;
			this.y = 0.0;
			this.z = 0.0;
		}

		double getGrade() {
			double grade = 0;
			double angle;
			angle = Math.toRadians(this.getAngle(2));
			if (angle < (Math.PI * 0.8)) {
				grade = Math.sin(angle) / Math.cos(angle);
				grade *= 100.00;
			}
			return (grade);
		}

		double getAngle(int angleName) {
			double angle;
			double modulus;
			double cosine_alpha;
			double cosine_beta;
			double cosine_gamma;
			double angle_alpha;
			double angle_beta;
			double angle_gamma;

			modulus = Math.sqrt(Math.pow(this.x, 2) + Math.pow(this.y, 2) + Math.pow(this.z, 2));
			cosine_alpha = this.x / modulus;
			cosine_beta = this.y / modulus;
			cosine_gamma = this.z / modulus;
			angle_alpha = Math.toDegrees(Math.asin(cosine_alpha));
			angle_beta = Math.toDegrees(Math.asin(cosine_beta));
			angle_gamma = Math.toDegrees(Math.asin(cosine_gamma));
			switch (angleName) {
				case 1:
					angle = angle_alpha;
					break;
				case 2:
					angle = angle_beta;
					break;
				case 3:
					angle = angle_gamma;
					break;
				default:
					angle = 0.0;
					break;
			}
			return angle;
		}
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		long time_threshold = 500;
		double accelerometer_value;

		accelerometer_value = Math.sqrt(Math.pow(event.values[0], 2) + Math.pow(event.values[1], 2) + Math.pow(event.values[2], 2));
		accelerometer_vector.x = event.values[0];
		accelerometer_vector.y = event.values[1];
		accelerometer_vector.z = event.values[2];
		accelerometer_vector.avgGrade = getAvgGrade(accelerometer_vector.getGrade());

		if (user_status == IDLE) {
			if (accelerometer_vector.getAngle(2) > -55 && accelerometer_vector.getAngle(2) < 55) {
				mCurrentInclination.setText(String.format(Locale.getDefault(), "%.2f %%", accelerometer_vector.avgGrade - accelerometer_vector.gradeOffset));
				btn_calibrate.setEnabled(true);
				btn_measure_inclination.setEnabled(true);
			} else {
				mCurrentInclination.setText("--");
			}
			return;
		} else {
			mCurrentInclination.setText("");
			btn_calibrate.setEnabled(false);
			btn_measure_inclination.setEnabled(false);

			ViewGroup layout1 = (ViewGroup) btn_measure_inclination.getParent();
			if (null != layout1) {
				layout1.removeView(btn_measure_inclination);
			}
			ViewGroup layout2 = (ViewGroup) btn_calibrate.getParent();
			if (null != layout2) {
				layout2.removeView(btn_calibrate);
			}
		}

		double accelerometer_spurious_change = 8;
		if ((Math.abs(accelerometer_value - accelerometer_last_value) > accelerometer_spurious_change)) {
			//spurious value, will be discarded.
			accelerometer_value = accelerometer_last_value;
		}
		accelerometer_last_value = accelerometer_value;
		double avg_acceleration = getAvgAcceleration(accelerometer_value);

		mCaloriesDistance.setText(String.format(Locale.getDefault(), "%.2f", run_data.getCalories_v_distance()));
		mCaloriesHeartBeat.setText(String.format(Locale.getDefault(), "%.2f", run_data.getCalories_v_heart_beat()));

		mAccelerationBar.setProgress((int) (accelerometer_value * 10));
		acceleration.setText(String.format("Status: %s", status));

		threshold_begin = avg_acceleration + threshold_modulus;
		threshold_end = avg_acceleration - threshold_modulus;


		double avg_distance = 0.3080;
		switch (state) {

			case STATE_INITIAL:
				run_data.setCurrent_speed_m_s_v(0);
				state = STATE_OUTSIDE;
				break;

			case STATE_OUTSIDE:
				long time_now = new Date().getTime();

				long time_difference;
				if ((time_now - time_end) > time_threshold) {
					time_difference = time_now - time_end;
					run_data.setCurrent_speed_m_s_v((avg_distance / time_difference) * 1000);
					run_data.setCurrent_speed_km_h_v(run_data.getCurrent_speed_m_s_v() * run_data.getConv_m_s_km_h());
				}

				if (accelerometer_value > threshold_begin) {
					state = STATE_INSIDE;
					time_start = new Date().getTime();
				}
				break;

			case STATE_INSIDE:
				time_now = new Date().getTime();
				if ((time_now - time_start) > time_threshold) {
					state = STATE_OUTSIDE;
				}

				if (accelerometer_value < threshold_end) {
					state = STATE_OUTSIDE;
					time_end = new Date().getTime();
					double time_diff_min = 40;
					double time_diff_max = 400;
					if (((time_end - time_start) < time_diff_max) && ((time_end - time_start) > time_diff_min)) {
						time_difference = time_end - time_start;
						run_data.setCurrent_speed_m_s_v((avg_distance / time_difference) * 1000);
						run_data.setCurrent_speed_km_h_v(run_data.getCurrent_speed_m_s_v() * run_data.getConv_m_s_km_h());
						run_data.setDistance_m_v(run_data.getDistance_m_v() + avg_distance);
						run_data.setDistance_km_v(run_data.getDistance_m_v() / 1000);
					}
				}
				break;

			default:
				state = STATE_INITIAL;
				break;
		}
		/*  Male: ((-55.0969 + (0.6309 x HR) + (0.1988 x W) + (0.2017 x A))/4.184) x 60 x T
				Female: ((-20.4022 + (0.4472 x HR) - (0.1263 x W) + (0.074 x A))/4.184) x 60 x T */

		run_data.getValues();

		if (user_status == RUNNING || user_status == WALKING) {
			time_calories_now = new Date().getTime();
			long delta_time = (time_calories_now - time_calories_last);
			double delta_time_hours = ((double) delta_time) / 1000 / 60 / 60;

			if (delta_time > run_data.getGranularity_time()) {
				writeLog(String.format(Locale.CANADA, "RunActivity: onSensorChanged(): INSIDE: delta_time: %d", delta_time));
				time_calories_now = new Date().getTime();
				if (run_data.getCurrent_heart_rate() >= user_bio.getResting_hr() && user_bio.getResting_hr() > 20 && user_bio.getResting_hr() < 100) {
					if (user_bio.getGender().compareToIgnoreCase("male") == 0) {
						//Male: ((-55.0969 + (0.6309 x HR) + (0.1988 x W) + (0.2017 x A))/4.184) x 60 x T */
						run_data.setCalories_v_heart_beat(run_data.getCalories_v_heart_beat() + ((-55.0969 + (0.6309 * run_data.getCurrent_heart_rate()) + (0.1988 * user_bio.getCurrent_weight_v()) + (0.2017 * user_bio.getAge())) / 4.184) * 60 * delta_time_hours);
					} else {
						// Female: ((-20.4022 + (0.4472 x HR) - (0.1263 x W) + (0.074 x A))/4.184) x 60 x T */
						run_data.setCalories_v_heart_beat(run_data.getCalories_v_heart_beat() + ((-20.4022 + (0.4472 * run_data.getCurrent_heart_rate()) + (0.1263 * user_bio.getCurrent_weight_v()) + (0.074 * user_bio.getAge())) / 4.184) * 60 * delta_time_hours);
					}
				}
				//-20% ≤ % Grade ≤ - 15%:
				if (run_data.getInclination() >= -20 && run_data.getInclination() <= -15) {
					run_data.setCalories_v_distance((((-0.01 * run_data.getInclination()) + 0.50) * user_bio.getCurrent_weight_v() + run_data.getThreadmill_factor()) * run_data.getDistance_km_v() * user_bio.getCff());
				}
				//-15% < % Grade ≤ - 10%:
				if (run_data.getInclination() >= -15 && run_data.getInclination() <= -10) {
					run_data.setCalories_v_distance((((-0.02 * run_data.getInclination() + 0.35) * user_bio.getCurrent_weight_v() + run_data.getThreadmill_factor()) * run_data.getDistance_km_v() * user_bio.getCff()));
				}
				//10% < % Grade ≤ 0%:
				if (run_data.getInclination() >= -10 && run_data.getInclination() <= 0) {
					run_data.setCalories_v_distance((((0.04 * run_data.getInclination()) + 0.95) * user_bio.getCurrent_weight_v() + run_data.getThreadmill_factor()) * run_data.getDistance_km_v() * user_bio.getCff());
				}
				//0% < % Grade ≤ 10%:
				if (run_data.getInclination() > 0 && run_data.getInclination() <= 10) {
					run_data.setCalories_v_distance((((0.05 * run_data.getInclination()) + 0.95) * user_bio.getCurrent_weight_v() + run_data.getThreadmill_factor()) * run_data.getDistance_km_v() * user_bio.getCff());
				}
				//10% < % Grade ≤ 15%:
				if (run_data.getInclination() > 10 && run_data.getInclination() <= 15) {
					run_data.setCalories_v_distance((((0.07 * run_data.getInclination()) + 0.75) * user_bio.getCurrent_weight_v() + run_data.getThreadmill_factor()) * run_data.getDistance_km_v() * user_bio.getCff());
				}
				time_calories_last = new Date().getTime();

				writeLog(String.format(Locale.CANADA, "RunActivity: onSensorChanged(): INSIDE: run_data.getInclination: %d", run_data.getInclination()));
				writeLog(String.format(Locale.CANADA, "RunActivity: onSensorChanged(): INSIDE: user_bio.getCff(): %f", user_bio.getCff()));
				writeLog(String.format(Locale.CANADA, "RunActivity: onSensorChanged(): INSIDE: time_calories_last: %d", time_calories_last));
				writeLog(String.format(Locale.CANADA, "RunActivity: onSensorChanged(): INSIDE: time_calories_now: %d", time_calories_now));
				writeLog(String.format(Locale.CANADA, "RunActivity: onSensorChanged(): INSIDE: run_data.getCalories_v_distance: %f", run_data.getCalories_v_distance()));
				writeLog(String.format(Locale.CANADA, "RunActivity: onSensorChanged(): INSIDE: run_data.getCalories_v_heart_beat: %f", run_data.getCalories_v_heart_beat()));

				double usedMemoryPercentage = 100 * (double) availableMemory() / (double) totalMemory();
				if (usedMemoryPercentage < 80) {
					RunInstant runInstant = new RunInstant();
					runInstant.setUid(user_bio.getUid());
					runInstant.setRun_id_v(run_data.getRun_id_v());
					runInstant.setCtime(new Date().getTime());
					runInstant.setCurrent_motion_speed_km_h_v(run_data.getAverage_speed_km_h_v());
					runInstant.setCurrent_motion_distance_km_v(avg_distance);
					runInstant.setCurrent_gps_speed_km_h(this.gps_speed);
					runInstant.setCurrent_gps_distance_km(run_data.getGps_distance_km());
					runInstant.setCalories_v_distance(run_data.getCalories_v_distance());
					runInstant.setCalories_v_heart_beat(run_data.getCalories_v_heart_beat());
					runInstant.setCurrent_heart_rate(run_data.getCurrent_heart_rate());
					runInstant.setLongitude(this.longitude);
					runInstant.setLatitude(this.latitude);
					runInstant.setAltitude(this.altitude);
					writeLog(String.format(Locale.CANADA, "RunActivity: ADDING RunInstant.getCurrentTime(): %d", runInstant.getCtime()));
					sqliteHandler.addRunInstant(runInstant.toJSON());
				} else {
					writeLog("RunActivity: ERROR: MEMORY USE ABOVE 80%.");
				}
			}
		}
		run_data.getValues();
		run_data.setAverage_speed_km_h_v(getAvgSpeed(run_data.getCurrent_speed_km_h_v()));
		if (user_bio.getMetric().compareToIgnoreCase("metric") == 0) {
			mDistanceMotion.setText(String.format(Locale.CANADA, "%.2f", run_data.getDistance_km_v()));
			mDistanceGPS.setText(String.format(Locale.CANADA, "%.2f", run_data.getGps_distance_km()));
			mSpeedMotion.setText(String.format(Locale.CANADA, "%.2f", run_data.getAverage_speed_km_h_v()));
			mSpeedGPS.setText(String.format(Locale.CANADA, "%.2f", this.gps_speed));
		} else {
			mDistanceMotion.setText(String.format(Locale.CANADA, "%.2f", run_data.getDistance_miles_v()));
			mDistanceGPS.setText(String.format(Locale.CANADA, "%.2f", run_data.getGps_distance_miles()));
			mSpeedMotion.setText(String.format(Locale.CANADA, "%.2f", run_data.getAverage_speed_miles_h_v()));
			mSpeedGPS.setText(String.format(Locale.CANADA, "%.2f", this.gps_speed));
		}
		double idle_speed = 1;
		double walking_speed = 2;
		if (run_data.getAverage_speed_km_h_v() < idle_speed) {
			status = "Idle";
		} else {
			this_time = new Date().getTime();
			last_time = new Date().getTime();
			if (user_status == PAUSED) {
				status = "Paused";
			} else {
				if (run_data.getAverage_speed_km_h_v() < walking_speed) {
					status = "Walking";
				} else {
					status = "Running";
				}
			}
		}
	}

	public void writeLog(String msg) {
		String date = (DateFormat.format("dd-MM-yyyy hh:mm:ss a", new Date()).toString());
		Log.e(TAG, date + ": " + msg);
	}

	/**
	 * Called when the accuracy of the registered sensor has changed.
	 * <p/>
	 * <p>See the SENSOR_STATUS_* constants in
	 * {@link SensorManager SensorManager} for details.
	 *
	 * @param sensor
	 * @param accuracy The new accuracy of this sensor, one of
	 *                 {@code SensorManager.SENSOR_STATUS_*}
	 */
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	public void startChronometer(View view) {
		switch (user_status) {
			case IDLE:
				updateChronometer();
				showElapsedTime();
				mChronometer.setBase(SystemClock.elapsedRealtime());
				mChronometer.start();
				user_status = RUNNING;
				time_calories_last = new Date().getTime();
				run_data.setStartTime();
				btn_start.setText(R.string.button_message_stop);
				break;

			case PAUSED:
				updateChronometer();
				showElapsedTime();
				mChronometer.start();
				user_status = RUNNING;
				time_calories_last = new Date().getTime();
				btn_start.setText(R.string.button_message_stop);
				break;

			case WALKING:
			case RUNNING:
				updateChronometer();
				showElapsedTime();
				mChronometer.stop();
				user_status = PAUSED;
				btn_start.setText(R.string.button_message_resume);
				break;
		}
	}

	public void updateChronometer() {
		int stoppedMilliseconds = 0;
		String chronoText = mChronometer.getText().toString();
		String array[] = chronoText.split(":");
		if (array.length == 2) {
			stoppedMilliseconds = Integer.parseInt(array[0]) * 60 * 1000 + Integer.parseInt(array[1]) * 1000;
		} else if (array.length == 3) {
			stoppedMilliseconds = Integer.parseInt(array[0]) * 60 * 60 * 1000 + Integer.parseInt(array[1]) * 60 * 1000 + Integer.parseInt(array[2]) * 1000;
		}
		mChronometer.setBase(SystemClock.elapsedRealtime() - stoppedMilliseconds);
		mChronometer.start();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
	}
}
