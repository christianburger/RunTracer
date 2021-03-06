package com.runtracer;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.runtracer.interfaces.OnDateSetListener;
import com.runtracer.model.UserData;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.view.View.OnClickListener;
import static android.view.View.OnTouchListener;

public class ProfileActivity extends AppCompatActivity implements OnDateSetListener, OnClickListener, OnTouchListener, TextView.OnEditorActionListener {
	private static final String TAG = "profile";

	protected static UserData userData;

	public static String datePicked = "";

	EditText mFullName;
	EditText mUserEmail;

	Button mUserDateofBirth;
	Switch mUserGender;

	EditText mUserHeight;
	EditText mUserHipCircumference;
	EditText mUserBodyMassIndex;
	EditText mUserBodyAdiposityIndex;
	EditText mUserWeight;
	EditText mUserTargetWeight;
	EditText mUserFat;
	EditText mUserTargetFat;

	TextView mUserHeightUnits;
	TextView mUserHipCircumferenceUnits;
	TextView mUserWeightUnits;
	TextView mUserWeightTargetUnits;

	String retrievedFullName;
	String retrievedEmail;
	String retrievedDOB;
	String retrievedGender;
	String retrievedHeight;
	String retrievedHipCircumference;
	String retrievedWeight;
	String retrievedTargetWeight;
	String retrievedFat;
	String retrievedTargetFat;

	private final double conv_in_cm = 2.54;
	private final double conv_ft_cm = 30.48;
	private final double conv_lb_kg = 0.45359237;

	private final NumberFormat nf = NumberFormat.getNumberInstance(Locale.getDefault());

	double retrievedHeight_v;
	double retrievedHipCircumference_v;
	double retrievedWeight_v;
	double retrievedTargetWeight_v;
	double retrievedFat_v;
	double retrievedTargetFat_v;

	private double bmi;
	private double bai;

	private boolean retrievedMetricSystem;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);

		userData = MainActivity.user_bio;
		if (userData != null) {
			retrievedFullName = userData.getFull_name();
			retrievedEmail = userData.getEmail();
			retrievedDOB = userData.getBirthday();
			retrievedGender = userData.getGender();
			retrievedFat = userData.getCurrent_fat();
			retrievedTargetFat = userData.getTarget_fat();
			retrievedMetricSystem = userData.getMetric().compareTo("metric") == 0;
			retrievedHeight = nf.format(userData.getHeight_v());
			retrievedHeight_v = userData.getHeight_v();
			retrievedHipCircumference = nf.format(userData.getHip_circumference_v());
			retrievedHipCircumference_v = userData.getHip_circumference_v();
			retrievedWeight = nf.format(userData.getCurrent_weight_v());
			retrievedWeight_v = userData.getCurrent_weight_v();
			retrievedTargetWeight = nf.format(userData.getTarget_weight_v());
			retrievedTargetWeight_v = userData.getTarget_weight_v();
			retrievedFat = nf.format(userData.getCurrent_fat_v());
			retrievedFat_v = userData.getCurrent_fat_v();
			retrievedTargetFat = nf.format(userData.getTarget_fat_v());
			retrievedTargetFat_v = userData.getTarget_fat_v();
			datePicked = retrievedDOB;
		}
		setContentView(R.layout.activity_profile);
		try {
			this.setupGui();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onDateSet(Date date) {
		SimpleDateFormat l_format = new SimpleDateFormat("yyyy-MM-dd ", Locale.getDefault());
		writeLog(String.format(Locale.US, "ProfileActivity: LISTENER RECEIVED: date: %s >> retrievedDateOfBirth: %s ", date.toString(), date));
		mUserDateofBirth.setText(l_format.format(date));
		retrievedDOB = l_format.format(date);
	}

	@Override
	public void onDateSet(String date) {

	}

	public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

		public OnDateSetListener onDateSetListener;

		public OnDateSetListener getOnDateSetListener() {
			return onDateSetListener;
		}

		public void setOnDateSetListener(OnDateSetListener onDateSetListener) {
			this.onDateSetListener = onDateSetListener;
		}

		private static final SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CANADA);
		private static final String TAG = "profile";
		private String retrievedDOB;

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			Bundle bundle = new Bundle(this.getArguments());
			retrievedDOB = bundle.getString("date", "");
			writeLog(String.format("Inside OnCreateDialog, received: %s", retrievedDOB));
			Calendar c = Calendar.getInstance();
			try {
				c.setTime(date_format.parse(retrievedDOB));
			} catch (ParseException e) {
				writeLog(e.toString());
			}
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH);
			return new DatePickerDialog(getActivity(), this, year, month, day);
		}

		public void onDateSet(DatePicker view, int year, int month, int day) {
			Date picked;
			datePicked = String.format(Locale.getDefault(), "%d-%d-%d ", year, month + 1, day);
			writeLog(String.format(Locale.US, "onDateSet: %d-%d-%d ", year, month + 1, day));
			SimpleDateFormat l_format = new SimpleDateFormat("yyyy-MM-dd ", Locale.getDefault());
			try {
				picked = l_format.parse(datePicked);
				onDateSetListener.onDateSet(picked);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

		public void writeLog(String msg) {
			String date = (DateFormat.format("dd-MM-yyyy hh:mm:ss a", new java.util.Date()).toString());
//			Log.e(TAG, date + ": " + msg);
		}
	}

	public void showDatePickerDialog(View v) {
		writeLog("starting showDatePickerDialog");
		//DialogFragment newFragment = new DatePickerFragment();
		DatePickerFragment newFragment = new DatePickerFragment();
		newFragment.setOnDateSetListener(this);

		Bundle bundle = new Bundle();
		bundle.putString("date", retrievedDOB);
		newFragment.setArguments(bundle);
		newFragment.show(getSupportFragmentManager(), "datePicker");
		retrievedDOB = datePicked;
		mUserDateofBirth.setText(datePicked);
		writeLog("ending showDatePickerDialog: retrievedDOB: " + retrievedDOB);
	}

	private void setupGui() throws ParseException {
		Context context = this.getApplicationContext();
		// Set up button click listeners
		findViewById(R.id.btn_change_data).setOnClickListener(this);
		findViewById(R.id.btn_change_data).setEnabled(true);
		findViewById(R.id.btn_change_data).setVisibility(Button.VISIBLE);

		// Set up view instances
		mFullName = (EditText) findViewById(R.id.full_name);
		mUserEmail = (EditText) findViewById(R.id.email);

		mUserHeight = (EditText) findViewById(R.id.user_height);
		mUserHipCircumference = (EditText) findViewById(R.id.hip_circunference_value);
		mUserWeight = (EditText) findViewById(R.id.user_weight);
		mUserTargetWeight = (EditText) findViewById(R.id.user_weight_target);

		mUserBodyMassIndex = (EditText) findViewById(R.id.user_bmi_value);
		mUserBodyAdiposityIndex = (EditText) findViewById(R.id.user_bai_value);

		mUserBodyMassIndex.setOnClickListener(this);
		mUserBodyAdiposityIndex.setOnClickListener(this);

		mUserBodyMassIndex.setEnabled(false);
		mUserBodyAdiposityIndex.setEnabled(false);

		mUserFat = (EditText) findViewById(R.id.user_fat_percentage);
		mUserTargetFat = (EditText) findViewById(R.id.user_fat_percentage_target);

		mUserHeightUnits = (TextView) findViewById(R.id.height_units);
		mUserWeightUnits = (TextView) findViewById(R.id.weight_units);
		mUserWeightTargetUnits = (TextView) findViewById(R.id.weight_unit_target);
		mUserHipCircumferenceUnits = (TextView) findViewById(R.id.length_units);

		mUserDateofBirth = (Button) findViewById(R.id.date_picker_button);
		mUserDateofBirth.setOnClickListener(this);
		mUserDateofBirth.setOnTouchListener(this);
		mUserDateofBirth.setOnEditorActionListener(this);

		mUserGender = (Switch) findViewById(R.id.user_gender_value);
		mUserGender.setOnClickListener(this);

		mUserHeight.setOnClickListener(this);
		mUserHeight.setOnTouchListener(this);
		mUserHeight.setOnEditorActionListener(this);


		mUserHipCircumference.setOnClickListener(this);
		mUserHipCircumference.setOnTouchListener(this);
		mUserHipCircumference.setOnEditorActionListener(this);

		mUserWeight.setOnClickListener(this);
		mUserWeight.setOnTouchListener(this);
		mUserWeight.setOnEditorActionListener(this);

		mUserTargetWeight.setOnClickListener(this);
		mUserTargetWeight.setOnTouchListener(this);
		mUserTargetWeight.setOnEditorActionListener(this);

		mUserFat.setOnClickListener(this);
		mUserFat.setOnTouchListener(this);
		mUserFat.setOnEditorActionListener(this);

		mUserTargetFat.setOnClickListener(this);
		mUserTargetFat.setOnTouchListener(this);
		mUserTargetFat.setOnEditorActionListener(this);

		mFullName.setOnClickListener(this);
		mFullName.setOnTouchListener(this);
		mFullName.setOnEditorActionListener(this);
		mFullName.setText(retrievedFullName);
		mUserEmail.setOnClickListener(this);
		mUserEmail.setOnTouchListener(this);
		mUserEmail.setOnEditorActionListener(this);

		mUserEmail.setText(retrievedEmail);
		mUserEmail.setEnabled(false);

		mUserDateofBirth.setText(retrievedDOB);
		mUserDateofBirth.setText(datePicked);

		mUserGender.setText(retrievedGender);

		mUserFat.setText(String.format(Locale.getDefault(), "%.2f", retrievedFat_v));
		mUserTargetFat.setText(String.format(Locale.getDefault(), "%.2f", retrievedTargetFat_v));

		getValues(false);

		if (retrievedMetricSystem) {
			mUserHeightUnits.setText(R.string.unit_cm);
			mUserWeightUnits.setText(R.string.unit_kg);
			mUserWeightTargetUnits.setText(R.string.unit_kg);
			mUserHipCircumferenceUnits.setText(R.string.unit_cm);
			mUserHeight.setText(String.format(Locale.getDefault(), "%.2f", retrievedHeight_v));
			mUserHipCircumference.setText(String.format(Locale.getDefault(), "%.2f", retrievedHipCircumference_v));
			mUserWeight.setText(String.format(Locale.getDefault(), "%.2f", retrievedWeight_v));
			mUserTargetWeight.setText(String.format(Locale.getDefault(), "%.2f", retrievedTargetWeight_v));
		} else {
			mUserHeightUnits.setText(R.string.unit_ft);
			mUserWeightUnits.setText(R.string.unit_lb);
			mUserWeightTargetUnits.setText(R.string.unit_lb);
			mUserHipCircumferenceUnits.setText(R.string.unit_inches);
			mUserHeight.setText(String.format(Locale.getDefault(), "%.2f", retrievedHeight_v / conv_ft_cm));
			mUserHipCircumference.setText(String.format(Locale.getDefault(), "%.2f", retrievedHipCircumference_v / conv_in_cm));
			mUserWeight.setText(String.format(Locale.getDefault(), "%.2f", retrievedWeight_v / conv_lb_kg));
			mUserTargetWeight.setText(String.format(Locale.getDefault(), "%.2f", retrievedTargetWeight_v / conv_lb_kg));
		}
	}

	public boolean isNumber(String str) {
		int size = str.length();
		for (int i = 0; i < size; i++) {
			Character cchar = new Character(str.charAt(i));
			if (!Character.isDigit(cchar) && (cchar != '.') && (cchar != ',')) {
				return false;
			}
		}
		return size > 0;
	}

	boolean calculateBMI() throws ParseException {
		boolean result;
		getValues(false);
		if (retrievedHeight_v > 0 && retrievedHipCircumference_v > 0 && retrievedWeight_v > 0) {
			// Metric: (HC / (HM)1.5) - 18
			// BAI = Body Adiposity Index
			this.bai = retrievedHipCircumference_v / Math.pow(retrievedHeight_v / 100, 1.5) - 18;
			// BMI = Body Mass Index
			// Metric: BMI = WKG / (HM x HM)
			this.bmi = retrievedWeight_v / Math.pow(retrievedHeight_v / 100, 2);
			result = true;
		} else {
			result = false;
		}
		return result;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_new_user, menu);
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

	public boolean isEmailValid(String email) {
		String regExpn = "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
			+ "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
			+ "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
			+ "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
			+ "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
			+ "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$";

		Pattern pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(email);

		return matcher.matches();
	}

	private boolean isNameValid(String name) {
		return name.length() > 4;
	}

	private boolean isWeightValid(String weight) throws ParseException {
		double weight_v = 0.0;
		if (isNumber(weight)) {
			weight_v = (nf.parse(weight).doubleValue());
		}
		if (retrievedMetricSystem) {
			return weight_v > 20;
		} else {
			return weight_v > 40;
		}
	}

	private boolean isFatValid(String fat) throws ParseException {
		double fat_v = 0.0;
		if (isNumber(fat)) {
			fat_v = nf.parse(fat).doubleValue();
		}
		return (fat_v > 2 && fat_v < 90);
	}

	private boolean isHeightValid(String height) throws ParseException {
		double height_v = 0.0;
		if (isNumber(height)) {
			height_v = nf.parse(height).doubleValue();
		}
		if (retrievedMetricSystem) {
			return (height_v > 40 && height_v < 240);
		} else {
			return (height_v > 3 && height_v < 8);
		}
	}

	private boolean isHipCircumferenceValid(String hip_circumference) throws ParseException {
		double hip_circumference_v = 0.0;
		if (isNumber(hip_circumference)) {
			hip_circumference_v = nf.parse(hip_circumference).doubleValue();
		}
		if (retrievedMetricSystem) {
			return (hip_circumference_v > 20 && hip_circumference_v < 240);
		} else {
			return (hip_circumference_v > 7 && hip_circumference_v < 80);
		}
	}

	public boolean getValues(boolean bFillJSON) throws ParseException {

		double lretrievedHeight_v = -1;
		double lretrievedHipCircumference_v = -1;
		double lretrievedWeight_v = -1;
		double lretrievedTargetWeight_v = -1;

		retrievedFullName = String.valueOf(mFullName.getText());
		retrievedEmail = String.valueOf(mUserEmail.getText());
		retrievedGender = String.valueOf(mUserGender.getText());
		retrievedDOB = datePicked;
		retrievedHeight = String.valueOf(mUserHeight.getText());
		retrievedHipCircumference = String.valueOf(mUserHipCircumference.getText());
		retrievedWeight = String.valueOf(mUserWeight.getText());
		retrievedTargetWeight = String.valueOf(mUserTargetWeight.getText());
		retrievedFat = String.valueOf(mUserFat.getText());
		retrievedTargetFat = String.valueOf(mUserTargetFat.getText());

		if (isWeightValid(retrievedWeight) && isHeightValid(retrievedHeight) && isHipCircumferenceValid(retrievedHipCircumference) && isWeightValid(retrievedTargetWeight)) {
			lretrievedHeight_v = nf.parse(retrievedHeight).doubleValue();
			lretrievedHipCircumference_v = nf.parse(retrievedHipCircumference).doubleValue();
			lretrievedWeight_v = nf.parse(retrievedWeight).doubleValue();
			lretrievedTargetWeight_v = nf.parse(retrievedTargetWeight).doubleValue();
		} else {
			return (false);
		}

		if (isFatValid(retrievedFat) && isFatValid(retrievedTargetFat)) {
			retrievedFat_v = nf.parse(retrievedFat).doubleValue();
			retrievedTargetFat_v = nf.parse(retrievedTargetFat).doubleValue();
		}

		if (retrievedMetricSystem) {
			retrievedHeight_v = lretrievedHeight_v;
			retrievedHipCircumference_v = lretrievedHipCircumference_v;
			retrievedWeight_v = lretrievedWeight_v;
			retrievedTargetWeight_v = lretrievedTargetWeight_v;
		} else {
			retrievedHeight_v = lretrievedHeight_v * conv_ft_cm;
			retrievedHipCircumference_v = lretrievedHipCircumference_v * conv_in_cm;
			retrievedWeight_v = lretrievedWeight_v * conv_lb_kg;
			retrievedTargetWeight_v = lretrievedTargetWeight_v * conv_lb_kg;
		}

		if (bFillJSON) {
			if (!isWeightValid(retrievedWeight)) {
				writeLog("isWeightValid == false");
				String errorString = "Weight is invalid: " + retrievedWeight;
				writeLog("isWeightValid == false");
				Toast.makeText(this, errorString, Toast.LENGTH_SHORT).show();
				return false;
			}
			if (!isHeightValid(retrievedHeight)) {
				writeLog("isHeightValid== false");
				String errorString = "Height is invalid: " + retrievedHeight;
				Toast.makeText(this, errorString, Toast.LENGTH_SHORT).show();
				return false;
			}
			if (!isHipCircumferenceValid(retrievedHipCircumference)) {
				writeLog("isHipCircumferenceValid== false");
				String errorString = "Hip Circumference is invalid: " + retrievedHipCircumference;
				Toast.makeText(this, errorString, Toast.LENGTH_SHORT).show();
				return false;
			}
			if (!isEmailValid(retrievedEmail)) {
				writeLog("isEmailValid== false");
				String errorString = "email is invalid: " + retrievedEmail;
				Toast.makeText(this, errorString, Toast.LENGTH_SHORT).show();
				return false;
			}
			if (!isNameValid(retrievedFullName)) {
				writeLog("isNameValid== false");
				String errorString = "Name is invalid: " + retrievedFullName;
				Toast.makeText(this, errorString, Toast.LENGTH_SHORT).show();
				return false;
			}
			if (!isWeightValid(retrievedTargetWeight)) {
				writeLog("isWeightValid== false");
				String errorString = "Target Weight is invalid: " + retrievedTargetWeight;
				Toast.makeText(this, errorString, Toast.LENGTH_SHORT).show();
				return false;
			}
			if (!isFatValid(retrievedFat)) {
				writeLog("isFatValid(retrievedFat)== false");
				String errorString = "Body Fat % is invalid: " + retrievedFat;
				Toast.makeText(this, errorString, Toast.LENGTH_SHORT).show();
				return false;
			}
			if (!isFatValid(retrievedTargetFat)) {
				writeLog("isFatValid(retrievedTargetFat)== false");
				String errorString = "Target Body Fat % is invalid: " + retrievedTargetFat;
				Toast.makeText(this, errorString, Toast.LENGTH_SHORT).show();
				return false;
			}
			writeLog("getValues: true");
			userData.setFull_name(retrievedFullName);
			userData.setEmail(retrievedEmail);
			userData.setGender(retrievedGender);
			userData.setBirthday(retrievedDOB);
			userData.setCurrent_fat_v(retrievedFat_v);
			userData.setTarget_fat_v(retrievedTargetFat_v);
			userData.setHeight_v(retrievedHeight_v);
			userData.setHip_circumference_v(retrievedHipCircumference_v);
			userData.setCurrent_weight_v(retrievedWeight_v);
			userData.setTarget_weight_v(retrievedTargetWeight_v);
			userData.getValues();
			writeLog(String.format("getValues: userData: %s", userData.toString()));

		} else {
			writeLog("getValues: false");
		}
		return (true);
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
			case R.id.btn_change_data:
				try {
					if (getValues(true)) {
						Intent data = new Intent();
						Bundle returnValue = new Bundle();
						returnValue.putString("user_data", userData.toString());
						writeLog(String.format(Locale.US, "ProfileActivity: onClick: userData %s", userData));
						data.putExtra("data", returnValue);
						setResult(RESULT_OK, data);
						this.finish();
					} else {
						writeLog("ProfileActivity: onClick ERROR");
					}
				} catch (ParseException e) {
					e.printStackTrace();
				}
				break;

				/*
				retrievedFullName= String.valueOf(mFullName.getText());
				retrievedEmail= String.valueOf(mUserEmail.getText());
				retrievedDOB=String.valueOf(mUserDateofBirth.getText());
				retrievedGender=String.valueOf(mUserGender.getText());
				retrievedHeight=String.valueOf(mUserHeight.getText());
				retrievedHipCircumference=String.valueOf(mUserHipCircumference.getText());
				retrievedWeight=String.valueOf(mUserWeight.getText());
				retrievedTargetWeight=String.valueOf(mUserTargetWeight.getText());
				retrievedFat=String.valueOf(mUserFat.getText());
				retrievedTargetFat=String.valueOf(mUserTargetFat.getText());
				if( isEmailValid(retrievedEmail) ) {
				} else {
					String errorString= "email is invalid: " + retrievedEmail;
					Toast.makeText(this, errorString, Toast.LENGTH_SHORT).show();
					return;
				}
				Intent data = new Intent();
				try {
					assert jsonData != null;
					jsonData.accumulate("full_name", retrievedFullName);
					jsonData.accumulate("email", retrievedEmail);
					jsonData.accumulate("dob", retrievedDOB);
					jsonData.accumulate("gender", retrievedGender);
					jsonData.accumulate("height", retrievedHeight);
					jsonData.accumulate("hip_circumference", retrievedHipCircumference);
					jsonData.accumulate("weight", retrievedWeight);
					jsonData.accumulate("target_weight", retrievedTargetWeight);
					jsonData.accumulate("fat", retrievedFat);
					jsonData.accumulate("target_fat", retrievedTargetFat);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				Bundle returnValue= new Bundle();
				returnValue.putString("user_data", jsonData.toString());
				data.putExtra("data", returnValue);
				// Activity finished ok, return the data
				setResult(RESULT_OK, data);
				this.finish();
				break;
				*/

			case R.id.date_picker_button:
				showDatePickerDialog(v);
				break;

			case R.id.full_name:
				break;
			case R.id.email:
				break;
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		mUserDateofBirth.setText(datePicked);
		try {
			if (calculateBMI()) {
				mUserBodyMassIndex.setText(String.format("%.2f", this.bmi));
				mUserBodyAdiposityIndex.setText(String.format("%.2f", this.bai));
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return false;
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		switch (v.getId()) {
			case R.id.full_name:
				break;
			case R.id.email:
				break;
		}
		return false;
	}

	public void writeLog(String msg) {
		String date = (DateFormat.format("dd-MM-yyyy hh:mm:ss", new java.util.Date()).toString());
//		Log.e(TAG, date + ": " + msg);
	}
}
