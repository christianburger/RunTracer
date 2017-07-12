package com.runtracer;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.format.DateFormat;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.runtracer.services.BluetoothLeService;
import com.runtracer.services.DataBaseExchange;
import com.runtracer.services.ServerDataService;
import com.runtracer.sqlitedb.SqliteHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements OnClickListener, SensorEventListener, GoogleApiClient.OnConnectionFailedListener, FirebaseAuth.AuthStateListener {

	private static final boolean DEVELOPER_MODE = false;
	public static final int minimum_age = 12;
	public static final int MAX_AVAILABLE = 1;
	private static final int MAX_ATTEMPTS = 10;
	private static final String TAG = "runtracer";
	public static final Semaphore available = new Semaphore(MAX_AVAILABLE, true);
	public static DataBaseExchange dbExchange;
	public static UserData user_bio;
	public static String lastHash = null;

	File userdatafile;
	File runmapfile;
	File runinfofile;

	FileOutputStream f_out;

	private BluetoothLeService mBluetoothLeService;

	private FirebaseAuth mAuth;
	private FirebaseAuth.AuthStateListener mAuthListener;
	private FirebaseAnalytics mFirebaseAnalytics;

	private String mDeviceAddress;
	private HashMap activityListMap;
	private HashMap activityInfoMap;

	/* RequestCode for resolutions involving sign-in */
	private static final int RC_SIGN_IN = 0;
	private static final int NEW_USER_DATA = 1;       // The request code
	private static final int RUN_USER_DATA = 2;       // The request code
	private static final int LOGIN_USER_DATA = 3;     // The request code
	private static final int BLUETOOTH_LE = 4;        // The request code
	private static final int USER_PROFILE = 5;        // The request code
	private static final int ACTIVITIES_DATA = 6;     // The request code
	private static final int ABOUT_YOU = 7;            // The request code

	/* Keys for persisting instance variables in savedInstanceState */
	private static final String KEY_IS_RESOLVING = "is_resolving";
	private static final String KEY_SHOULD_RESOLVE = "should_resolve";

	private static final int MEASURING = 1;          // RHR Measuring state
	private static final int READY = 2;              // RHR Measuring state
	private static final int ACQUIRED = 3;           // RHR Measuring state

	private static final int NETWORK_TIMEOUT = 80000;

	//method signature for response at onPostExecute
	private static final int get_user_data = 1001;
	private static final int send_user_data = 1002;
	private static final int change_user_data = 1003;
	private static final int auth_user = 1004;
	private static final int send_run_data = 1005;
	private static final int get_run_ids = 1006;
	private static final int get_run_info = 1007;
	private static final int get_all_run_info = 1008;

	/* View to display current status (signed-in, signed-out, disconnected, etc) */
	private TextView mStatus;
	private TextView mUsername;

	private Switch mMetricSystem;

	private TextView mCalories;
	private TextView mDistance;
	private TextView mTotalRuns;

	private TextView mRestingHeartRate;
	private TextView mRecoveryHeartRate;
	private TextView mHeartRateReserve;

	private TextView mVO2max;
	private TextView mBodyMassIndex;
	private TextView mBodyAdiposityIndex;

	private TextView mUserInfo;
	private TextView mUserMaxHR;

	private TextView mUserCurrentWeight;
	private TextView mUserTargetWeight;
	private TextView mUserCurrentFat;
	private TextView mUserTargetFat;

	private Button mMeasureRHR;
	private Button mSignOutButton;

	/* Is there is a ConnectionResult resolution in progress? */
	private boolean mIsResolving = false;
	/* Is google plus user successfully signed in */
	private boolean mIsSignedIn = false;
	private boolean mIsEmailSignedIn = false;
	private boolean mIsAuthenticated = false;

	/* Should we automatically resolve ConnectionResults when possible? */
	private boolean isBluetoothLeRegistered;

	private boolean isUpdated;
	private SqliteHandler sqlLiteHandler;
	private SignInButton mGoogleSignIn;
	private GoogleApiClient mGoogleApiClient;

	public MainActivity() {
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (DEVELOPER_MODE) {
			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
				.detectDiskReads()
				.detectDiskWrites()
				.detectNetwork()   // or .detectAll() for all detectable problems
				.penaltyLog()
				.build());
			StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
				.detectLeakedSqlLiteObjects()
				.detectLeakedClosableObjects()
				.penaltyLog()
				.penaltyDeath()
				.build());
		}
		super.onCreate(savedInstanceState);

		new SimpleEula(this).show();

// Configure sign-in to request the user's ID, email address, and basic
// profile. ID and basic profile are included in DEFAULT_SIGN_IN.
		GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
			.requestIdToken(getString(R.string.default_web_client_id))
			.requestEmail()
			.build();

		mGoogleApiClient = new GoogleApiClient.Builder(this)
			.enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
			.addApi(Auth.GOOGLE_SIGN_IN_API, gso)
			.build();

		FirebaseApp.initializeApp(this);
		mAuth = FirebaseAuth.getInstance();
		mAuthListener = this;

		// Obtain the FirebaseAnalytics instance.
		mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

		try {
			available.acquire();
			dbExchange = DataBaseExchange.createDataBaseExchange();
			dbExchange.clear();
			available.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		user_bio = new UserData();
		user_bio.getValues();
		user_bio.setStatus("0");
		user_bio.setBMetricSystem(false);

		sqlLiteHandler = new SqliteHandler(MainActivity.this);

		isUpdated = false;

		activityListMap = new HashMap<Long, Long>();
		activityListMap.clear();

		activityInfoMap = new HashMap<Long, RunData>();
		activityInfoMap.clear();
		local_registerReceiver();

		this.setContentView(R.layout.activity_main);
		this.setupGui();
		this.readFile();
		this.updateUI();

		// Restore from saved instance state
		// [START restore_saved_instance_state]
		if (savedInstanceState != null) {
			mIsResolving = savedInstanceState.getBoolean(KEY_IS_RESOLVING);
		}
		// [END restore_saved_instance_state]
	}

	protected boolean writeFile() {
		try {
			user_bio.getValues();
			String path = getPackageDirectory();
			userdatafile = new File(path, "userdata.user");
			f_out = new FileOutputStream(userdatafile.getAbsolutePath(), false);
			ObjectOutputStream obj_out = new ObjectOutputStream(f_out);
			obj_out.writeObject(user_bio);
			writeLog(String.format(Locale.US, "saved file: %s", userdatafile.getAbsolutePath()));
			f_out.close();

			runmapfile = new File(path, "runmap.data");
			f_out = new FileOutputStream(runmapfile.getAbsolutePath(), false);
			ObjectOutputStream objmap_out = new ObjectOutputStream(f_out);
			objmap_out.writeObject(activityListMap);
			writeLog(String.format(Locale.US, "saved file: %s", runmapfile.getAbsolutePath()));
			f_out.close();

			runinfofile = new File(path, "runinfo.data");
			f_out = new FileOutputStream(runinfofile.getAbsolutePath(), false);
			ObjectOutputStream objinfo_out = new ObjectOutputStream(f_out);
			objinfo_out.writeObject(activityInfoMap);
			writeLog(String.format(Locale.US, "saved file: %s", runinfofile.getAbsolutePath()));
			f_out.close();

		} catch (IOException | PackageManager.NameNotFoundException e) {
			writeLog(String.format(Locale.US, "writeFile(): Exception: %s", e.toString()));
			e.printStackTrace();
		}

		return true;
	}

	protected boolean readFile() {
		boolean userdata_ok = false;
		boolean runmapdata_ok = false;
		boolean runinfodata_ok = false;
		try {
			String path = getPackageDirectory();
			userdatafile = new File(path, "userdata.user");
			if (userdatafile.exists()) {
				// Read from disk using FileInputStream
				FileInputStream f_in = new FileInputStream(userdatafile.getAbsolutePath());
				// Read object using ObjectInputStream
				ObjectInputStream obj_in = new ObjectInputStream(f_in);
				// Read an object
				writeLog("File found, reading data now: ");
				user_bio = (UserData) obj_in.readObject();
				user_bio.getValues();
				mMetricSystem.setChecked(user_bio.isBMetricSystem());
				userdata_ok = true;
				f_in.close();
			} else {
				writeLog("File not found: ");
			}

			activityListMap.clear();
			activityInfoMap.clear();

			runmapfile = new File(path, "runmap.data");
			if (runmapfile.exists()) {
				writeLog("found runmapfile...");
				FileInputStream f_in = new FileInputStream(runmapfile.getAbsolutePath());
				// Read object using ObjectInputStream
				ObjectInputStream obj_in = new ObjectInputStream(f_in);
				// Read an object
				activityListMap = (HashMap<Long, Long>) obj_in.readObject();
				runmapdata_ok = true;
				f_in.close();
			} else {
				writeLog("File not found: ");
			}

			runinfofile = new File(path, "runinfo.data");
			if (runinfofile.exists()) {
				writeLog("found runinfofile...");
				FileInputStream f_in = new FileInputStream(runinfofile.getAbsolutePath());
				// Read object using ObjectInputStream
				ObjectInputStream obj_in = new ObjectInputStream(f_in);
				// Read an object
				activityInfoMap = (HashMap<Long, RunData>) obj_in.readObject();
				runinfodata_ok = true;
				f_in.close();
			} else {
				writeLog("File not found: ");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (userdata_ok && runmapdata_ok && runinfodata_ok) {
			int mapsz = activityListMap.size();
			int infosz = activityInfoMap.size();
			writeLog(String.format(Locale.US, "readFile: checking basic file consistency: mapsz: %d, infosz: %d", mapsz, infosz));
			if (mapsz > 0 && infosz > 0 && mapsz == infosz) {
				writeLog("readFile(): isUpdated being set to true.");
				isUpdated = true;
			}
		}
		updateUI();
		return true;
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (mAuthListener != null) {
			mAuth.removeAuthStateListener(mAuthListener);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		JSONObject json = user_bio.createJSON();
		writeLog(String.format(Locale.US, "onPause(): CALLING changeUserData: json: %s", json));
		changeUserData(json);
	}

	protected String getPackageDirectory() throws PackageManager.NameNotFoundException {
		String path;
		PackageManager m = getPackageManager();
		String s = getPackageName();
		PackageInfo p = m.getPackageInfo(s, 0);
		path = p.applicationInfo.dataDir;

		return (path);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		JSONObject json = user_bio.createJSON();
		writeLog(String.format(Locale.US, "onDestroy(): CALLING changeUserData: json: %s", json));
		changeUserData(json);
		this.writeFile();
		if (mBluetoothLeService != null && isBluetoothLeRegistered) {
			unregisterReceiver(mGattUpdateReceiver);
		}
		writeLog(String.format(Locale.US, "onDestroy(): %s", json));
		updateUI();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
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

	public void newRun() {
		Intent intent = new Intent(this, RunActivity.class);
		intent.putExtra("UserData", user_bio);
		startActivityForResult(intent, RUN_USER_DATA);
	}

	public void loginUser(JSONObject userInfo) {
		Intent intent = new Intent(this, LoginActivity.class);
		intent.putExtra("user_info", userInfo.toString());
		startActivityForResult(intent, LOGIN_USER_DATA);
	}

	public void newUser(JSONObject userInfo) throws JSONException {
		if (userInfo.isNull("metric")) {
			userInfo.put("metric", user_bio.isBMetricSystem() ? 1 : 0);
		}
		writeLog(String.format(Locale.US, "userInfo: %s", userInfo.toString()));
		Intent intent = new Intent(this, NewUserActivity.class);
		intent.putExtra("user_info", userInfo.toString());
		startActivityForResult(intent, NEW_USER_DATA);
	}

	public void userProfile(JSONObject userInfo) {
		writeLog(String.format(Locale.US, "userProfile: 00 userInfo: %s", userInfo));
		try {
			userInfo.put("is_signed_in", mIsSignedIn);
			userInfo.put("full_name", user_bio.getFull_name());
			userInfo.put("email", user_bio.getEmail());
			userInfo.put("gender", user_bio.getGender());
			userInfo.put("birthday", user_bio.getBirthday());
			userInfo.put("height", user_bio.getHeight_v());
			userInfo.put("hip_circumference", user_bio.getHip_circumference_v());
			userInfo.put("weight", user_bio.getCurrent_weight_v());
			userInfo.put("target_weight", user_bio.getTarget_weight_v());
			userInfo.put("target_fat", user_bio.getTarget_fat_v());
			userInfo.put("fat_percentage", user_bio.getCurrent_fat_v());
			userInfo.put("metric", user_bio.isBMetricSystem() ? 1 : 0);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		writeLog(String.format(Locale.US, "userProfile: 01 userInfo: %s", userInfo));
		Intent intent = new Intent(this, ProfileActivity.class);
		intent.putExtra("user_info", userInfo.toString());
		startActivityForResult(intent, USER_PROFILE);
	}

	public void userActivity() {
		Intent intent = new Intent(this, ActivitiesActivity.class);
		intent.putExtra("RunInfo", activityInfoMap);
		intent.putExtra("UserData", user_bio);
		startActivityForResult(intent, ACTIVITIES_DATA);
	}

	public void searchHRM(View view) throws ActivityNotFoundException {
		Intent scan_intent = new Intent(this, DeviceScanActivity.class);
		startActivityForResult(scan_intent, BLUETOOTH_LE);
	}

	private void setupGui() {
		mGoogleSignIn = (SignInButton) findViewById(R.id.sign_in_button);
		mGoogleSignIn.setOnClickListener(this);
		mGoogleSignIn.setSize(SignInButton.SIZE_WIDE);
		mGoogleSignIn.setEnabled(true);
		mGoogleSignIn.setVisibility(Button.VISIBLE);
		mGoogleSignIn.setSize(SignInButton.SIZE_STANDARD);

		mSignOutButton = (Button) findViewById(R.id.sign_out_button);
		mSignOutButton.setOnClickListener(this);
		mSignOutButton.setEnabled(false);
		mSignOutButton.setVisibility(Button.INVISIBLE);

		findViewById(R.id.new_user_button).setOnClickListener(this);
		findViewById(R.id.new_user_button).setEnabled(true);
		findViewById(R.id.new_user_button).setVisibility(Button.VISIBLE);

		findViewById(R.id.email_login).setOnClickListener(this);
		findViewById(R.id.email_login).setEnabled(true);
		findViewById(R.id.email_login).setVisibility(Button.VISIBLE);

		findViewById(R.id.new_run).setOnClickListener(this);
		findViewById(R.id.new_run).setEnabled(true);
		findViewById(R.id.new_run).setVisibility(Button.VISIBLE);

		findViewById(R.id.btn_scan).setOnClickListener(this);
		findViewById(R.id.btn_scan).setEnabled(true);
		findViewById(R.id.btn_scan).setVisibility(Button.VISIBLE);

		findViewById(R.id.user_activity_button).setOnClickListener(this);
		findViewById(R.id.user_activity_button).setEnabled(true);
		findViewById(R.id.user_activity_button).setVisibility(Button.VISIBLE);

		findViewById(R.id.user_profile_button).setOnClickListener(this);
		findViewById(R.id.user_profile_button).setEnabled(true);
		findViewById(R.id.user_profile_button).setVisibility(Button.VISIBLE);

		mMeasureRHR = (Button) findViewById(R.id.user_resting_hr_button);
		mMeasureRHR.setOnClickListener(this);

		mMeasureRHR.setEnabled(false);
		mMeasureRHR.setVisibility(Button.VISIBLE);

		Button mAboutYou = (Button) findViewById(R.id.user_about_button);
		mAboutYou.setOnClickListener(this);

		mAboutYou.setEnabled(true);
		mAboutYou.setVisibility(Button.VISIBLE);

		// Set up view instances
		mMetricSystem = (Switch) findViewById(R.id.user_unit_system);
		mMetricSystem.setOnClickListener(this);
		mMetricSystem.setEnabled(true);
		mMetricSystem.setVisibility(Switch.VISIBLE);
		mMetricSystem.setText(R.string.user_unit_system_metric);

		mStatus = (TextView) findViewById(R.id.status);
		mCalories = (TextView) findViewById(R.id.total_calories_value);
		mDistance = (TextView) findViewById(R.id.total_distance_value);
		mTotalRuns = (TextView) findViewById(R.id.total_runs_value);

		mUsername = (TextView) findViewById(R.id.user_name);
		mUserInfo = (TextView) findViewById(R.id.user_info);
		mUserMaxHR = (TextView) findViewById(R.id.max_heart_rate_value);

		mRestingHeartRate = (TextView) findViewById(R.id.resting_hear_rate_value);
		mRecoveryHeartRate = (TextView) findViewById(R.id.recovery_heart_rate_value);
		mHeartRateReserve = (TextView) findViewById(R.id.reserve_hear_rate_value);
		mVO2max = (TextView) findViewById(R.id.vo2max_value);
		mBodyMassIndex = (TextView) findViewById(R.id.body_mass_index_value);
		mBodyAdiposityIndex = (TextView) findViewById(R.id.body_adiposity_index_value);
		mUserCurrentWeight = (TextView) findViewById(R.id.user_weight_value);
		mUserTargetWeight = (TextView) findViewById(R.id.goal_weight_value);
		mUserCurrentFat = (TextView) findViewById(R.id.current_fat_value);
		mUserTargetFat = (TextView) findViewById(R.id.goal_fat_value);

		TextView mLink = (TextView) findViewById(R.id.runtracer_web_page);
		String linkText = "Visit the <a href='http://runtracer.com'>RunTracer</a> web page.";
		mLink.setText(Html.fromHtml(linkText));
		mLink.setMovementMethod(LinkMovementMethod.getInstance());
	}

	private String printValue(Date value) {
		String stringtoprint = "";
		//SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		SimpleDateFormat date_format = new SimpleDateFormat("MMMM/yyyy", Locale.getDefault());
		if (value.after(new Date(1000000))) {
			stringtoprint = date_format.format(value);
		}
		return stringtoprint;
	}

	private String printValue(double value) {
		NumberFormat nf = NumberFormat.getInstance(Locale.getDefault());
		String stringtoprint = "--";
		if (value > 0) {
			stringtoprint = nf.format(value);
		}
		return stringtoprint;
	}

	private String printValue(String value) {
		String stringtoprint = "";
		if (value != null && !value.isEmpty()) {
			stringtoprint = value;
		}
		return stringtoprint;
	}

	public boolean isEmailValid(String email) {
		String regExpn = "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
			+ "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
			+ "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
			+ "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
			+ "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
			+ "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$";

		Pattern pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE);
		Matcher matcher;
		matcher = pattern.matcher(email);

		return matcher.matches();
	}

	private void updateUI() {
		user_bio.getValues();
		user_bio.setBMetricSystem(mMetricSystem.isChecked());
		if (user_bio.isBMetricSystem()) {
			mMetricSystem.setText(R.string.user_unit_system_metric);
		} else {
			mMetricSystem.setText(R.string.user_unit_system_imperial);
		}
		if (user_bio.isBMetricSystem()) {
			mUserTargetWeight.setText(printValue(user_bio.getTarget_weight_v()));
			mUserCurrentWeight.setText(printValue(user_bio.getCurrent_weight_v()));
		} else {
			mUserTargetWeight.setText(printValue(user_bio.getTarget_weight_v_imperial()));
			mUserCurrentWeight.setText(printValue(user_bio.getTarget_weight_v_imperial()));
		}
		if (user_bio.isBMetricSystem()) {
			mDistance.setText(printValue(user_bio.getTotal_distance_km()));
		} else {
			mDistance.setText(printValue(user_bio.getTotal_distance_miles()));
		}
		mCalories.setText(printValue(user_bio.getTotal_calories()));
		mTotalRuns.setText(printValue(user_bio.getTotal_runs()));

		mUsername.setText(printValue(user_bio.getFull_name()));
		mUserCurrentFat.setText(printValue(user_bio.getCurrent_fat_v()));
		mUserTargetFat.setText(printValue(user_bio.getTarget_fat_v()));

		mBodyMassIndex.setText(printValue(user_bio.getBmi()));
		mBodyAdiposityIndex.setText(printValue(user_bio.getBai()));
		if (user_bio.getAge() > minimum_age) {
			user_bio.getValues();
			mUserMaxHR.setText(printValue(user_bio.getMaximum_hr()));
			mVO2max.setText(printValue(user_bio.getVo2max()));
			mHeartRateReserve.setText(printValue(user_bio.getHr_reserve()));
		}
		mRecoveryHeartRate.setText(printValue(user_bio.getRecovery_hr()));
		mRestingHeartRate.setText(printValue(user_bio.getResting_hr()));

		mIsSignedIn = mGoogleApiClient.isConnected();
		mIsEmailSignedIn = (!mIsSignedIn && isEmailValid(user_bio.getEmail()));

		if (mIsSignedIn || mIsEmailSignedIn) {
			mUserInfo.setText(String.format("Member since %s", printValue(user_bio.getCreated_v())));
			try {
				if (mIsEmailSignedIn) {
					mStatus.setText(getString(R.string.signed_in_fmt, user_bio.getFull_name()));
				}
				mGoogleSignIn.setEnabled(false);
				mGoogleSignIn.setVisibility(View.INVISIBLE);

				//ViewGroup layout = (ViewGroup) mGoogleSignIn.getParent();
				//if (null != layout) //for safety only  as you are doing onClick
				//layout.removeView(mGoogleSignIn);

				mSignOutButton.setText(R.string.sign_out);
				mSignOutButton.setBackgroundColor(Color.DKGRAY);
				mSignOutButton.setTextColor(Color.LTGRAY);
				mSignOutButton.setEnabled(true);
				mSignOutButton.setVisibility(View.VISIBLE);

				findViewById(R.id.new_user_button).setEnabled(false);
				findViewById(R.id.new_user_button).setVisibility(View.VISIBLE);
				findViewById(R.id.email_login).setEnabled(false);
				findViewById(R.id.email_login).setVisibility(View.VISIBLE);
				if (mIsAuthenticated) {
					getRunData(!isUpdated);
				}
			} catch (JSONException | MalformedURLException | InterruptedException e) {
				e.printStackTrace();
			}
		} else {
			// Show signed-out message
			mStatus.setText(R.string.signed_out);
			// Set button visibility
			//mGoogleSignIn.setEnabled(true);
			//mGoogleSignIn.setVisibility(View.VISIBLE);

			mSignOutButton.setEnabled(false);
			mSignOutButton.setVisibility(View.INVISIBLE);
		}
	}

	private boolean sendServerDataServiceRequest(String hash) throws InterruptedException {
		boolean result = false;
		if (isServerReady()) {
			available.acquire();
			dbExchange.setPending(true);
			available.release();
			Intent mServiceIntent = new Intent(this, ServerDataService.class);
			mServiceIntent.setAction(ServerDataService.ACTION_QUERY_SERVER);
			mServiceIntent.putExtra("hash", hash);
			lastHash = hash;
			this.startService(mServiceIntent);
			result = true;
		}
		return result;
	}

	private boolean isServerReady() throws InterruptedException {
		boolean result = false;
		for (int attempts = 0; attempts < MAX_ATTEMPTS && !result; attempts++) {
			available.acquire();
			result = !dbExchange.isPending();
			available.release();
		}
		return result;
	}

	private boolean getOauth2Token(JSONObject jsonUserData) {
		try {
			if (isServerReady()) {
				available.acquire();
				dbExchange.clear();
				dbExchange.setUrl(new URL("http://192.168.1.102:8080/auth/oauth/token"));
				dbExchange.setCommand("get_token");
				dbExchange.setAccountEmail((String) jsonUserData.get("email"));
				dbExchange.setFull_name((String) jsonUserData.get("full_name"));
				dbExchange.setGrant_type("client_credentials");
				dbExchange.setMethod("POST");
				dbExchange.setClient_id("admin");
				dbExchange.setClient_secret("password");
				if (dbExchange.getFull_name().compareTo("") == 0) {
					dbExchange.setFull_name(null);
				}
				dbExchange.getJson_data_in().put("command", dbExchange.getCommand());
				dbExchange.getJson_data_in().put("email", jsonUserData.isNull("email")?"-":jsonUserData.get("email"));
				dbExchange.getJson_data_in().put("passwd", jsonUserData.isNull("passwd")?"-":jsonUserData.get("passwd"));
				dbExchange.getJson_data_in().put("logged", jsonUserData.isNull("logged")?"-":jsonUserData.get("logged"));
				dbExchange.setJson_data_in(new JSONObject());
				String hash = dbExchange.getHash();
				available.release();
				sendServerDataServiceRequest(hash);
			}
		} catch (JSONException | MalformedURLException | InterruptedException e) {
			e.printStackTrace();
		}
		return true;
	}

	private boolean authUser(JSONObject jsonUserData) throws MalformedURLException, InterruptedException, JSONException {
		String email, password;
		boolean authok = false;
		email = jsonUserData.getString("email");
		password = jsonUserData.getString("passwd");
		if (isServerReady()) {
			available.acquire();
			dbExchange.clear();

			//dbExchange.url = new URL("http://www.runtracer.com/runtracer.php");
			dbExchange.setUrl(new URL("http://192.168.1.100:8082/health"));
			dbExchange.setCommand("auth_user");
			dbExchange.setAccountEmail((String) jsonUserData.get("email"));
			dbExchange.setFull_name((String) jsonUserData.get("full_name"));
			if (dbExchange.getFull_name().compareTo("") == 0) {
				dbExchange.setFull_name(null);
			}
			dbExchange.getJson_data_in().put("command", dbExchange.getCommand());
			dbExchange.getJson_data_in().put("email", jsonUserData.get("email"));
			dbExchange.getJson_data_in().put("passwd", jsonUserData.get("passwd"));
			dbExchange.getJson_data_in().put("logged", jsonUserData.get("logged"));
			String hash = dbExchange.getHash();
			available.release();
			sendServerDataServiceRequest(hash);
			authok = true;
		}

		mAuth.signInWithEmailAndPassword(email, password)
			.addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
				@Override
				public void onComplete(@NonNull Task<AuthResult> task) {
					Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

					// If sign in fails, display a message to the user. If sign in succeeds
					// the auth state listener will be notified and logic to handle the
					// signed in user can be handled in the listener.
					if (!task.isSuccessful()) {
						Log.w(TAG, "signInWithEmail:failed", task.getException());
						Toast.makeText(MainActivity.this, R.string.auth_failed, Toast.LENGTH_SHORT).show();
					}
				}
			});

		FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
		if (user != null) {
			authok = true;
			String name = user.getDisplayName();
			email = user.getEmail();
			Uri photoUrl = user.getPhotoUrl();
			// The user's ID, unique to the Firebase project. Do NOT use this value to
			// authenticate with your backend server, if you have one. Use
			// FirebaseUser.getToken() instead.
			String uid = user.getUid();
			writeLog(String.format(Locale.US, "MainActivity: authUser: FirebaseUser: name: %s", name));
			writeLog(String.format(Locale.US, "MainActivity: authUser: FirebaseUser: email: %s", email));
			writeLog(String.format(Locale.US, "MainActivity: authUser: FirebaseUser: uid: %s", uid));
			user_bio.setFull_name(name);
			user_bio.setEmail(email);
			user.getToken(true);
			getOauth2Token(jsonUserData);
			if (photoUrl != null) {
				writeLog(String.format(Locale.US, "MainActivity: authUser: FirebaseUser: photo URL: %s", photoUrl));
			}
		}
		return authok;
	}

	private int sendUserData(JSONObject jsonUserData) throws MalformedURLException, InterruptedException, JSONException {
		String email, password;
		email = jsonUserData.getString("email");
		password = jsonUserData.getString("passwd");
		boolean dataok = false;
		if (isServerReady()) {
			available.acquire();
			dbExchange.clear();
			//dbExchange.url = new URL("http://www.runtracer.com/runtracer.php");
			dbExchange.setUrl(new URL("http://192.168.1.102:8082/user/create"));
			dbExchange.setCommand("send_user_data");
			dbExchange.getJson_data_in().put("command", dbExchange.getCommand());
			dbExchange.getJson_data_in().put("full_name", jsonUserData.get("full_name"));
			dbExchange.getJson_data_in().put("email", jsonUserData.get("email"));
			dbExchange.getJson_data_in().put("dob", jsonUserData.get("dob"));
			dbExchange.getJson_data_in().put("gender", jsonUserData.get("gender"));
			dbExchange.getJson_data_in().put("height", jsonUserData.get("height"));
			dbExchange.getJson_data_in().put("hip_circumference", jsonUserData.get("hip_circumference"));
			dbExchange.getJson_data_in().put("weight", jsonUserData.get("weight"));
			dbExchange.getJson_data_in().put("target_weight", jsonUserData.get("target_weight"));
			dbExchange.getJson_data_in().put("fat", jsonUserData.get("fat"));
			dbExchange.getJson_data_in().put("target_fat", jsonUserData.get("target_fat"));
			dbExchange.getJson_data_in().put("metric", user_bio.isBMetricSystem() ? 1 : 0);
			String hash = dbExchange.getHash();
			available.release();
			for (int attempts = 0; attempts < 10 && !dataok; attempts++) {
				dataok = sendServerDataServiceRequest(hash);
			}
		}

		writeLog(String.format(Locale.US, "MainActivity: sendUserData: FirebaseUser: email: %s", email));
		writeLog(String.format(Locale.US, "MainActivity: sendUserData: FirebaseUser: password: %s", password));

		mAuth.createUserWithEmailAndPassword(email, password)
			.addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
				@Override
				public void onComplete(@NonNull Task<AuthResult> task) {
					writeLog("createUserWithEmail:onComplete:" + task.isSuccessful());

					// If sign in fails, display a message to the user. If sign in succeeds
					// the auth state listener will be notified and logic to handle the
					// signed in user can be handled in the listener.
					if (!task.isSuccessful()) {
						writeLog("createUserWithEmail:onComplete: FAILED: task.isSuccessful(): " + task.isSuccessful());
						Toast.makeText(MainActivity.this, R.string.auth_failed, Toast.LENGTH_SHORT).show();
					}
				}
			});
		return 0;
	}

	//Database method
	private int changeUserData(JSONObject jsonUserData) {
		boolean dataok = false;
		int retval = 0;
		try {
			writeLog(String.format(Locale.US, "changeUserData: jsonUserData: %s", jsonUserData));
			if (isServerReady() && (mIsSignedIn || mIsEmailSignedIn)) {
				available.acquire();
				dbExchange.clear();
				//dbExchange.url = new URL("http://www.runtracer.com/runtracer.php");
				dbExchange.setUrl(new URL("http://192.168.1.102:8082/health"));
				dbExchange.setCommand("change_user_data");
				dbExchange.getJson_data_in().put("command", dbExchange.getCommand());
				dbExchange.getJson_data_in().put("full_name", jsonUserData.get("full_name"));
				dbExchange.getJson_data_in().put("email", jsonUserData.get("email"));
				dbExchange.getJson_data_in().put("logged", "true");
				dbExchange.getJson_data_in().put("dob", jsonUserData.get("dob"));
				dbExchange.getJson_data_in().put("gender", jsonUserData.get("gender"));
				dbExchange.getJson_data_in().put("height", jsonUserData.get("height"));
				dbExchange.getJson_data_in().put("hip_circumference", jsonUserData.get("hip_circumference"));
				dbExchange.getJson_data_in().put("weight", jsonUserData.get("weight"));
				dbExchange.getJson_data_in().put("target_weight", jsonUserData.get("target_weight"));
				dbExchange.getJson_data_in().put("fat", jsonUserData.get("fat"));
				dbExchange.getJson_data_in().put("target_fat", jsonUserData.get("target_fat"));
				dbExchange.getJson_data_in().put("metric", user_bio.isBMetricSystem() ? 1 : 0);
				dbExchange.getJson_data_in().put("recovery_heart_rate", user_bio.getResting_hr());
				dbExchange.getJson_data_in().put("resting_heart_rate", user_bio.getResting_hr());
				String hash = dbExchange.getHash();
				available.release();
				writeLog(String.format(Locale.US, "changeUserData: json_data_in: %s", dbExchange.getJson_data_in()));
				for (int attempts = 0; attempts < 10 && !dataok; attempts++) {
					dataok = sendServerDataServiceRequest(hash);
				}
			} else {
				retval = -1;
			}
		} catch (JSONException | MalformedURLException | InterruptedException e) {
			e.printStackTrace();
		}
		return retval;
	}

	private int getRunInfo(int run_id) throws MalformedURLException, JSONException, InterruptedException {
		boolean dataok = false;
		if (isServerReady() && (mIsSignedIn || mIsEmailSignedIn)) {
			available.acquire();
			dbExchange.clear();
			//dbExchange.url = new URL("http://www.runtracer.com/runtracer.php");
			dbExchange.setUrl(new URL("http://192.168.1.100:8082/health"));
			dbExchange.setCommand("get_run_info");
			Date dnow = new Date();
			dbExchange.getJson_data_in().put("command", dbExchange.getCommand());
			dbExchange.getJson_data_in().put("uid", user_bio.getUid());
			dbExchange.getJson_data_in().put("session_id", user_bio.getSession_id());
			dbExchange.getJson_data_in().put("runid", run_id);
			String hash = dbExchange.getHash();
			available.release();
			if (activityListMap.containsKey(run_id)) {
				Long nowtime = dnow.getTime();
				Long timeatimeout = (Long) activityListMap.get(run_id);
				timeatimeout += NETWORK_TIMEOUT;
				if (nowtime > timeatimeout) {
					activityListMap.put(run_id, dnow.getTime());
					for (int attempts = 0; attempts < 10 && !dataok; attempts++) {
						writeLog(String.format(Locale.US, "getRunInfo: attempt: %d", attempts));
						dataok = sendServerDataServiceRequest(hash);
					}
				}
			} else {
				activityListMap.put(run_id, dnow.getTime());
				for (int attempts = 0; attempts < 10 && !dataok; attempts++) {
					dataok = sendServerDataServiceRequest(hash);
				}
			}
		}
		return 0;
	}

	private int getAllRunInfo() throws MalformedURLException, JSONException, InterruptedException {
		if (mIsSignedIn || mIsEmailSignedIn) {
			if (isServerReady()) {
				available.acquire();
				dbExchange.clear();
				//dbExchange.url = new URL("http://www.runtracer.com/runtracer.php");
				dbExchange.setUrl(new URL("http://192.168.1.100:8082/health"));
				dbExchange.setCommand("get_all_run_info");
				dbExchange.getJson_data_in().put("command", dbExchange.getCommand());
				dbExchange.getJson_data_in().put("uid", user_bio.getUid());
				dbExchange.getJson_data_in().put("session_id", user_bio.getSession_id());
				String hash = dbExchange.getHash();
				available.release();
				sendServerDataServiceRequest(hash);
				writeLog("getAllRunInfo...");
			}
		}
		return 0;
	}

	@Override
	protected void onStart() {
		super.onStart();
		mAuth.addAuthStateListener(mAuthListener);
	}

	//Bluetooth support
	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
		return intentFilter;
	}
	//end of bluetooth support

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
		isBluetoothLeRegistered = true;
		if (mBluetoothLeService != null) {
			mBluetoothLeService.connect(mDeviceAddress);
		}
	}

	// [START on_save_instance_state]
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(KEY_IS_RESOLVING, mIsResolving);
		outState.putBoolean(KEY_SHOULD_RESOLVE, mIsResolving);
	}
	// [END on_save_instance_state]


	private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
		writeLog("firebaseAuthWithGoogle:" + acct.getId());
		AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
		mAuth.signInWithCredential(credential)
			.addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
				@Override
				public void onComplete(@NonNull Task<AuthResult> task) {
					if (task.isSuccessful()) {
						// Sign in success, update UI with the signed-in user's information
						Log.d(TAG, "signInWithCredential:success");
						FirebaseUser user = mAuth.getCurrentUser();
						if (user != null) {
							user_bio.setFull_name(user.getDisplayName());
							user_bio.setEmail(user.getEmail());
							user_bio.setFull_name(user.getDisplayName());
						}
						getOauth2Token(user_bio.toJSON());
						updateUI();
					} else {
						// If sign in fails, display a message to the user.
						Log.w(TAG, "signInWithCredential:failure", task.getException());
						Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
						updateUI();
					}
				}
			});

	}

	private void handleSignInResult(GoogleSignInResult result) {
		Log.d(TAG, "handleSignInResult:" + result.isSuccess());
		if (result.isSuccess()) {
			// Signed in successfully, show authenticated UI.
			GoogleSignInAccount acct = result.getSignInAccount();
			firebaseAuthWithGoogle(acct);
			if (acct != null) {
				mStatus.setText(getString(R.string.signed_in_fmt, acct.getDisplayName()));
				updateUI();
			}
		}
	}

	// [START on_activity_result]
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == RC_SIGN_IN) {
			writeLog(String.format(Locale.US, "onActivityResult: requestCode= %d, RC_SIGN_IN= %d", requestCode, RC_SIGN_IN));
			writeLog(String.format(Locale.US, "onActivityResult: Intent data: %s", data != null ? data.toString() : "yep, it's actually null"));
			mIsResolving = false;
			GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
			handleSignInResult(result);
		}
		if (requestCode == NEW_USER_DATA && resultCode == RESULT_OK) {
			Bundle userData;
			JSONObject jsonUserData;
			try {
				if (data.getExtras().getBundle("data") != null) {
					userData = data.getExtras().getBundle("data");
					assert userData != null;
					if (userData.getString("user_data") != null) {
						jsonUserData = new JSONObject(userData.getString("user_data"));
						sendUserData(jsonUserData);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (requestCode == USER_PROFILE && resultCode == RESULT_OK) {
			Bundle userData;
			JSONObject jsonUserData;
			try {
				if (data.getExtras().getBundle("data") != null) {
					userData = data.getExtras().getBundle("data");
					assert userData != null;
					if (userData.getString("user_data") != null) {
						jsonUserData = new JSONObject(userData.getString("user_data"));
						user_bio.writeJSON(jsonUserData);
						writeLog(String.format(Locale.US, "onActivityResult: requestCode==USER_PROFILE: CALLING changeUserData: json: %s", jsonUserData));
						changeUserData(jsonUserData);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (requestCode == LOGIN_USER_DATA && resultCode == RESULT_OK) {
			Bundle userData;
			JSONObject jsonUserData;
			try {
				if (data.getExtras().getBundle("data") != null) {
					userData = data.getExtras().getBundle("data");
					assert userData != null;
					if (userData.getString("user_data") != null) {
						jsonUserData = new JSONObject(userData.getString("user_data"));
						jsonUserData.put("logged", "false");
						authUser(jsonUserData);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (requestCode == RUN_USER_DATA && resultCode == RESULT_OK && mIsAuthenticated) {
			updateUI();
		}
		if (requestCode == ACTIVITIES_DATA) {
			switch (resultCode) {
				case RESULT_CANCELED:
					break;
				case RESULT_FIRST_USER:
					break;
				case RESULT_OK:
					break;
			}
			updateUI();
		}
		if (requestCode == BLUETOOTH_LE && resultCode == RESULT_OK) {
			String bluetoothDeviceName = "";
			bluetoothDeviceName = data.getStringExtra("BluetoothDeviceName");
			try {
				final BluetoothManager bluetoothManager =
					(BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
				BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();
				if (mBluetoothAdapter == null) {
					Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
					finish();
					return;
				}
				BluetoothDevice btdevice = mBluetoothAdapter.getRemoteDevice(bluetoothDeviceName);
				mDeviceAddress = btdevice.getAddress();
				Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
				bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private int getRunData(boolean bNeedsUpdate) throws MalformedURLException, JSONException, InterruptedException {
		boolean updateOk = !bNeedsUpdate;
		int returnvalue = 0;
		if ((mIsEmailSignedIn || mIsSignedIn) && mIsAuthenticated) {
			if (updateOk) {
				long ltime;
				Date cnow = new Date();
				Collection runactivities = activityListMap.values();
				Iterator itv = runactivities.iterator();
				if (itv.hasNext() && activityListMap.size() > 1) {
					updateOk = false;
				}
				for (; itv.hasNext(); ) {
					ltime = (Long) itv.next();
					updateOk = !(ltime > cnow.getTime());
				}
			}
			if (!updateOk) {
				getAllRunInfo();
			}
		} else {
			returnvalue = -1;
		}
		return 0;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.sign_in_button:
				this.signIn();
				mStatus.setText(R.string.signing_in);
				break;

			case R.id.new_user_button:
				JSONObject userinfo = null;
				try {
					userinfo = new JSONObject("{\"key\":\"data\"}");
					this.newUser(userinfo);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;

			case R.id.user_profile_button:
				userinfo = user_bio.createJSON();
				this.userProfile(userinfo);
				break;

			case R.id.email_login:
				try {
					userinfo = new JSONObject("{\"key\":\"data\"}");
					this.loginUser(userinfo);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;

			case R.id.btn_scan:
				try {
					this.searchHRM(v);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;

			case R.id.new_run:
				this.newRun();
				break;

			case R.id.user_activity_button:
				if (isUpdated) {
					this.userActivity();
				} else {
					Snackbar.make(findViewById(android.R.id.content), "Please wait until all activities are loaded from server.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
				}

				break;

			case R.id.user_resting_hr_button:
				if (user_bio.getRhr_state() == MEASURING) {
					user_bio.setRhr_state(READY);
				} else {
					user_bio.setRhr_state(MEASURING);
				}
				break;

			case R.id.user_about_button:
				if (isUpdated) {
					this.aboutYou();
				} else {
					Snackbar.make(findViewById(android.R.id.content), "Profile not ready.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
				}
				break;

			case R.id.user_unit_system:
				user_bio.setBMetricSystem(mMetricSystem.isChecked());
				updateUI();
				break;

			case R.id.sign_out_button:
				FirebaseAuth.getInstance().signOut();
				user_bio.clean();
				activityListMap.clear();
				activityInfoMap.clear();
				updateUI();
				this.finish();
				break;

		}
	}

	private void signIn() {
		Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
		startActivityForResult(signInIntent, RC_SIGN_IN);
	}

	public int aboutYou() {
		Intent intent = new Intent(this, About.class);
		intent.putExtra("UserData", user_bio);
		startActivityForResult(intent, ABOUT_YOU);
		return (0);
	}

	public void writeLog(String msg) {
		Date cdate;
		String date = (DateFormat.format("dd-MM-yyyy hh:mm:ss a", cdate = new Date()).toString());
		String msg2 = String.format(Locale.US, "<%d>", cdate.getTime());
		Log.e(TAG, date + msg2 + ": " + msg);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	private final ServiceConnection mServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName componentName, IBinder service) {
			mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
			if (!mBluetoothLeService.initialize()) {
				finish();
			}
			// Automatically connects to the device upon successful start-up initialization.
			mBluetoothLeService.connect(mDeviceAddress);
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			mBluetoothLeService = null;
		}
	};

	// Demonstrates how to iterate through the supported GATT Services/Characteristics.
	// In this sample, we populate the data structure that is bound to the ExpandableListView
	// on the UI.
	private void displayGattServices(List<BluetoothGattService> gattServices) {
		if (gattServices == null) return;
		String uuid;
		String unknownServiceString = getResources().getString(R.string.unknown_service);
		String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
		ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
		ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<ArrayList<HashMap<String, String>>>();
		ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

		// Loops through available GATT Services.
		for (BluetoothGattService gattService : gattServices) {
			HashMap<String, String> currentServiceData = new HashMap<String, String>();
			uuid = gattService.getUuid().toString();
			writeLog("new BluetoothGattService: BLE: " + uuid);
			String LIST_NAME = "NAME";
			currentServiceData.put(LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
			String LIST_UUID = "UUID";
			currentServiceData.put(LIST_UUID, uuid);
			gattServiceData.add(currentServiceData);
			writeLog("new gattServiceData: BLE: " + currentServiceData.toString());
			ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<HashMap<String, String>>();
			List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
			ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<BluetoothGattCharacteristic>();
			// Loops through available Characteristics.
			for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
				charas.add(gattCharacteristic);
				HashMap<String, String> currentCharaData = new HashMap<String, String>();
				uuid = gattCharacteristic.getUuid().toString();
				currentCharaData.put(LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
				currentCharaData.put(LIST_UUID, uuid);
				gattCharacteristicGroupData.add(currentCharaData);
				writeLog("new item for currentCharaData: BLE " + currentCharaData.toString());
				if (gattCharacteristic.getUuid().toString().matches("00002a37-0000-1000-8000-00805f9b34fb")) {
					mBluetoothLeService.setCharacteristicNotification(gattCharacteristic, true);
					writeLog("BLE found item for gattCharacteristic matching: " + "00002a37-0000-1000-8000-00805f9b34fb: " + gattCharacteristic.getUuid().toString());
				}
			}
			mGattCharacteristics.add(charas);
			gattCharacteristicData.add(gattCharacteristicGroupData);
		}
	}

	void local_registerReceiver() {
		IntentFilter mServerDBfilter = new IntentFilter(ResponseReceiver.ACTION_RESP);
		mServerDBfilter.addCategory(Intent.CATEGORY_DEFAULT);
		ResponseReceiver receiver = new ResponseReceiver();
		registerReceiver(receiver, mServerDBfilter);
	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

	}

	@Override
	public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
		FirebaseUser user = firebaseAuth.getCurrentUser();
		if (user != null) {
			writeLog("onAuthStateChanged:signed_in:" + user.getUid());
			mIsEmailSignedIn = true;
			mIsAuthenticated = true;
			mStatus.setText("signed_in:" + user.getEmail());
			user_bio.setEmail(user.getEmail());
			user_bio.setFull_name(user.getDisplayName());
			user_bio.setStatus(user.getUid());
		} else {
			writeLog("onAuthStateChanged:signed_out");
		}
	}

	public class ResponseReceiver extends BroadcastReceiver {
		public static final String ACTION_RESP = "com.runtracer.intent.action.MESSAGE_PROCESSED";

		/**
		 * This method is called when the BroadcastReceiver is receiving an Intent broadcast.
		 * During this time you can use the other methods on BroadcastReceiver to view/modify the current result values.
		 * This method is always called within the main thread of its process, unless you explicitly asked for it to be scheduled on a different thread using
		 * {@link Context#registerReceiver(BroadcastReceiver,
		 * IntentFilter, String, Handler)}. When it runs on the main thread you should never perform long-running operations in it (there is a timeout of
		 * 10 seconds that the system allows before considering the receiver to be blocked and a candidate to be killed). You cannot launch a popup dialog
		 * in your implementation of onReceive().
		 * If this BroadcastReceiver was launched through a &lt;receiver&gt; tag, then the object is no longer alive after returning from this function.
		 * This means you should not perform any operations that return a result to you asynchronously -- in particular, for interacting with services, you should use
		 * {@link Context#startService(Intent)} instead of {@link Context#bindService(Intent, ServiceConnection, int)}.
		 * If you wish to interact with a service that is already running, you can use {@link #peekService}.
		 * The Intent filters used in {@link Context#registerReceiver} and in application manifests are not guaranteed to be exclusive. They are hints to the operating system
		 * about how to find suitable recipients. It is possible for senders to force delivery to specific recipients, bypassing filter resolution.
		 * For this reason, {@link #onReceive(Context, Intent) onReceive()} implementations should respond only to known actions, ignoring any unexpected Intents that they may receive.
		 *
		 * @param context The Context in which the receiver is running.
		 * @param intent  The Intent being received.
		 */
		@Override
		public void onReceive(Context context, Intent intent) {
			String response;
			response = intent.getStringExtra("param_out_msg");
			writeLog("MainActivity: ResponseReceiver: onReceive: json_data_in: " + dbExchange.getJson_data_in());
			writeLog("MainActivity: ResponseReceiver: onReceive getJson_data_out(): " + dbExchange.getJson_data_out());

			if (response.compareTo(lastHash) == 0) {
				dbExchange.setPending(false);
			} else {
				writeLog(String.format(Locale.US, "MainActivity: ResponseReceiver: onReceive: response.compareTo(lastHash) == 0: %b ", (response.compareTo(lastHash) == 0)));
			}
			try {
				processResponse(dbExchange);
			} catch (InterruptedException | JSONException | IOException | ParseException | NoSuchAlgorithmException | CloneNotSupportedException e) {
				e.printStackTrace();
			}
		}

		private boolean updateRunInfo(int run_id, JSONObject json_run_info) throws IOException, JSONException, ParseException, NoSuchAlgorithmException {
			boolean update_result = false;
			RunData run_info = new RunData();
			Date dnow = new Date();
			writeLog(String.format(Locale.US, "updateRunInfo: run_id: %d", run_id));

			if (activityListMap.containsKey(run_id)) {
				if (dnow.getTime() >= (long) activityListMap.get(run_id)) {
					if (!activityInfoMap.containsKey(run_id)) {
						if (!json_run_info.isNull("calories_distance")) {
							writeLog(String.format("updateRunInfo: adding calories_distance: %s ", json_run_info.get("calories_distance")));
							run_info.writeJSON(json_run_info);
							activityInfoMap.put(run_id, run_info);
							activityListMap.put(run_id, dnow.getTime() * 2); //unix time now x 2
							user_bio.setTotal_runs(activityInfoMap.size());
							user_bio.getValues();
							update_result = true;
						}
					} else {
						run_info = (RunData) activityInfoMap.get(run_id);
						user_bio.setTotal_runs(activityInfoMap.size());
					}
				}
			}
			user_bio.setTotal_distance_km(user_bio.getTotal_distance_km() + run_info.getDistance_km_v());
			user_bio.setTotal_calories(user_bio.getTotal_calories() + run_info.getCalories_v_distance());
			return update_result;
		}

		private int updateAllRunInfo(JSONObject json_all_run_info) throws IOException, JSONException, ParseException, NoSuchAlgorithmException {
			int run_id;
			int colidx, rowidx;
			JSONObject json_run_info;
			boolean eof = false;
			RunData crun = new RunData();
			user_bio.setTotal_distance_km(0);
			user_bio.setTotal_distance_miles(0);
			user_bio.setTotal_calories(0);
			writeLog("updateAllRunInfo(JSONObject json_all_run_info): isUpdated being set to true.");
			isUpdated = true;
			for (rowidx = 0; !eof; rowidx++) {
				json_run_info = new JSONObject("{\"key\":\"data\"}");
				for (colidx = 0; colidx < (RunData.colsz + 1); colidx++) {
					String key = String.format(Locale.US, "(%d:%d)", colidx, rowidx);
					if (!json_all_run_info.isNull(key)) {
						String newkey = crun.getKeyName(colidx);
						json_run_info.put(newkey, json_all_run_info.get(key));
					} else {
						if (colidx == 0) {
							eof = true;
						}
					}
				}
				if (!json_run_info.isNull("run_id")) {
					run_id = Integer.parseInt((String) json_run_info.get("run_id"));
					Date dnow = new Date();
					activityListMap.put(run_id, dnow.getTime());
					isUpdated = !updateRunInfo(run_id, json_run_info);
					writeLog(String.format(Locale.US, "updateAllRunInfo: runid: %d received and updated.", run_id));
				}
			}
			return 0;
		}

		public void processResponse(DataBaseExchange dbEx) throws InterruptedException, JSONException, IOException, ParseException, NoSuchAlgorithmException, CloneNotSupportedException {
			Boolean bUserAlreadyCreated;
			Boolean bUserStatusReady;
			Boolean bUserValidated = false;
			int sender = 0;
			writeLog(String.format(Locale.CANADA, "processResponse: dbEx.getAttemptNo(): %d", dbEx.getAttemptNo()));
			if (dbEx.getError_no() > 0 && dbEx.getAttemptNo() < dbEx.getMaxAttempts()) {
				final DataBaseExchange retry = (DataBaseExchange) dbEx.clone();
				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						available.release();
						try {
							sendServerDataServiceRequest(retry.getHash());
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}, 20000);
				Snackbar.make(findViewById(android.R.id.content), "Connectivity problem, could not update the server, retrying in 20 secs.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
			} else {
				dbEx = DataBaseExchange.createDataBaseExchange();
			}

			if (dbEx.getJson_data_out()==null || dbEx.getJson_data_out().isNull("status") || dbEx.getJson_data_out().isNull("created") || dbEx.getJson_data_out().isNull("sender")) {
				user_bio.setStatus("0");
				user_bio.setCreated("0");
				return;
			} else {
				try {
					if (!dbEx.getJson_data_out().isNull("status")) {
						user_bio.setStatus(dbEx.getJson_data_out().get("status").toString());
					}
					if (!dbEx.getJson_data_out().isNull("created")) {
						user_bio.setCreated(dbEx.getJson_data_out().get("created").toString());
					}
					if (!dbEx.getJson_data_out().isNull("sender") && dbEx.getJson_data_out().get("sender") instanceof Integer) {
						sender = dbEx.getJson_data_out().getInt("sender");
					}
				} catch (JSONException e) {
					writeLog(String.format(Locale.US, "processResponse: Exception 01: %s", e.toString()));
					e.printStackTrace();
					return;
				}
			}
			try {
				if (!dbEx.getJson_data_out().isNull("status") && !dbEx.getJson_data_out().isNull("sender")) {
					user_bio.setStatus(dbEx.getJson_data_out().get("status").toString());
				}
				if (!dbEx.getJson_data_out().isNull("created") && !dbEx.getJson_data_out().isNull("sender")) {
					user_bio.setCreated(dbEx.getJson_data_out().get("created").toString());
				}
				if (!dbEx.getJson_data_out().isNull("validated") && dbEx.getJson_data_out().get("validated") instanceof Integer && !dbEx.getJson_data_out().isNull("sender")) {
					bUserValidated = 1 == dbEx.getJson_data_out().getInt("validated");
				}
			} catch (JSONException e) {
				writeLog(String.format(Locale.US, "processResponse: Exception 02: %s", e.toString()));
				e.printStackTrace();
			}
			user_bio.getValues();
			bUserAlreadyCreated = (user_bio.getCreated_v().after(new Date(0)) || user_bio.getCreated().compareTo("1") == 0);
			bUserStatusReady = !(user_bio.getStatus().compareTo("0") == 0);

			mIsEmailSignedIn = bUserAlreadyCreated && bUserStatusReady && !mIsSignedIn;
			if (!bUserAlreadyCreated) {
				Snackbar.make(findViewById(android.R.id.content), "User not created, select New User and create your profile.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
			}
			if (bUserAlreadyCreated && !bUserStatusReady && !bUserValidated) {
				Snackbar.make(findViewById(android.R.id.content), "User email not validated, check your email and create a password.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
			}

			writeLog(String.format(Locale.US, "MainActivity: processResponse: before switch: sender: %d", sender));

			switch (sender) {
				case get_user_data:
					break;

				case send_user_data:
					break;

				case change_user_data:
					writeLog(String.format(Locale.US, "processResponse: change_user_data: mIsAuthenticated; %s", mIsAuthenticated));
					writeLog(String.format(Locale.US, "processResponse: change_user_data: mIsEmailSignedIn: %s", mIsEmailSignedIn));

				case auth_user:
					if (bUserAlreadyCreated && bUserStatusReady) {
						mIsEmailSignedIn = !mIsSignedIn;
						mIsAuthenticated = true;
						user_bio.writeJSON(dbEx.getJson_data_out());
						mMetricSystem.setChecked(user_bio.isBMetricSystem());
						writeLog(String.format(Locale.US, "processResponse: getRunData(!isUpdated: %b)", !isUpdated));
						getRunData(!isUpdated);
						updateUI();
					} else {
						if (!bUserAlreadyCreated) {
							JSONObject jsonData;
							try {
								jsonData = new JSONObject("{\"key\":\"data\"}");
								jsonData.put("full_name", dbEx.getFull_name());
								jsonData.put("email", dbEx.getAccountEmail());
								jsonData.put("passwd", "");
								jsonData.put("logged", "true");
								jsonData.put("is_signed_in", mIsSignedIn);
								jsonData.put("gender", user_bio.getGender());
								jsonData.put("birthday", user_bio.getBirthday());
								jsonData.put("height", user_bio.getHeight());
								jsonData.put("hip_circumference", user_bio.getHip_circumference());
								jsonData.put("weight", user_bio.getCurrent_weight());
								jsonData.put("target_weight", user_bio.getTarget_weight());
								jsonData.put("target_fat", user_bio.getTarget_fat());
								jsonData.put("fat_percentage", user_bio.getCurrent_fat());
								jsonData.put("metric", user_bio.isBMetricSystem() ? 1 : 0);
								writeLog(String.format(Locale.US, "jsonData: %s", jsonData.toString()));
								newUser(jsonData);
							} catch (JSONException e) {
								writeLog(String.format(Locale.US, "processResponse: Exception 03: %s", e.toString()));
								e.printStackTrace();
							}
						}
					}
					break;

				case send_run_data:
					if (mIsAuthenticated) {
						getRunData(true);
						JSONObject json = user_bio.createJSON();
						writeLog(String.format(Locale.US, "processResponse: send_run_data: CALLING changeUserData: json: %s", json));
						changeUserData(json);
					}
					break;

				case get_run_ids:
					int max_run_history_sz = 2000;
					for (int i = 0; i < max_run_history_sz; i++) {
						String key = (String.format(Locale.US, "value_%d", i));
						if (!dbEx.getJson_data_out().isNull(key)) {
							try {
								String value = (String) dbEx.getJson_data_out().get(String.format(Locale.US, "value_%d", i));
								getRunInfo(Integer.parseInt(value));
							} catch (JSONException | MalformedURLException | InterruptedException e) {
								writeLog(String.format(Locale.US, "processResponse: Exception 03: %s", e.toString()));
								e.printStackTrace();
							}
						} else {
							i = max_run_history_sz;
						}
					}
					break;

				case get_run_info:
					try {
						int runid_v;
						String runid = (String) dbEx.getJson_data_out().get("runid");
						runid_v = Integer.parseInt(runid);
						updateRunInfo(runid_v, dbEx.getJson_data_out());
					} catch (JSONException | ParseException | IOException | NoSuchAlgorithmException e) {
						writeLog(String.format(Locale.US, "processResponse: Exception 04: %s", e.toString()));
						e.printStackTrace();
					}
					break;

				case get_all_run_info:
					try {
						updateAllRunInfo(dbEx.getJson_data_out());
						updateUI();
					} catch (JSONException | ParseException | IOException | NoSuchAlgorithmException e) {
						writeLog(String.format(Locale.US, "processResponse: Exception 05: %s", e.toString()));
						e.printStackTrace();
					}
					break;

				default:
			}
		}
	}

	// Handles various events fired by the Service.
	// ACTION_GATT_CONNECTED: connected to a GATT server.
	// ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
	// ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
	// ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
	//                        or notification operations.
	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
				writeLog("MainActivity: BroadcastReceiver: action is" + action);
			} else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
				mMeasureRHR.setText(getString(R.string.user_resting_hr_button));
				mMeasureRHR.setEnabled(false);
			} else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
				// Show all the supported services and characteristics on the user interface.
				displayGattServices(mBluetoothLeService.getSupportedGattServices());
				writeLog("BLE" + (mBluetoothLeService.getSupportedGattServices()).toString());
				user_bio.setHr_reading(0);
			} else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
				String data = (intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
				TextView t = (TextView) findViewById(R.id.heart_rate_value);
				t.setText(data);
				user_bio.setCurrent_hr(Double.parseDouble(data));
				user_bio.setHr_reading(user_bio.getHr_reading() + 1);
				if (user_bio.getHr_reading() > user_bio.getRESTING_NO_READINGS() && user_bio.getCurrent_hr() < user_bio.getRESTING_HR_MAX() && user_bio.getCurrent_hr() > user_bio.getRESTING_HR_MIN()) {
					mMeasureRHR.setEnabled(true);
					mMeasureRHR.setVisibility(Button.VISIBLE);
				} else {
					mMeasureRHR.setText(getString(R.string.user_resting_hr_button));
				}
				if ((user_bio.getLast_hr() < (user_bio.getCurrent_hr() - user_bio.getRESTING_HR_MARGIN())) || (user_bio.getLast_hr() > (user_bio.getCurrent_hr() + user_bio.getRESTING_HR_MARGIN()))) {
					user_bio.setLast_hr(-1);
					user_bio.setHr_reading(0);
				} else {
					if (user_bio.getRhr_state() == READY) {
						user_bio.setResting_hr(user_bio.getCurrent_hr());
						user_bio.setRhr_state(ACQUIRED);
						user_bio.getValues();
						updateUI();
					}
				}
				if (user_bio.getRhr_state() == MEASURING) {
					mMeasureRHR.setText(String.format(Locale.getDefault(), "OK? (%.0f)", user_bio.getCurrent_hr()));
				} else {
					mMeasureRHR.setText(getString(R.string.user_resting_hr_button));
				}
				user_bio.setLast_hr(user_bio.getCurrent_hr());
			}
		}
	};

	private class RetrieveTokenTask extends AsyncTask<String, Void, String> {
		private static final int REQ_SIGN_IN_REQUIRED = 11910;

		protected String doInBackground(String... params) {
		        /*
		        String accountName = params[0];
            String scopes = "oauth2:profile email";
            //String scopes = "oauth2:email " + Scopes.PLUS_LOGIN;
            String token = null;
            writeLog("doInBackground running...");
            try {
                writeLog("doInBackground getToken( ..., " + accountName + " ,..);" );
                token = GoogleAuthUtil.getToken(getApplicationContext(), accountName, scopes);
            } catch (IOException e) {
                writeLog(e.toString());
            } catch (UserRecoverableAuthException e) {
                startActivityForResult(e.getIntent(), REQ_SIGN_IN_REQUIRED);
            } catch (GoogleAuthException e) {
                writeLog(e.toString());
            }
            return token;
            */
			return null;
		}
	}
}