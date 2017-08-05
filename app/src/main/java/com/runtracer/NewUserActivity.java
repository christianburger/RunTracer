package com.runtracer;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
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
import com.runtracer.utilities.TypeCheck;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.view.View.OnClickListener;
import static android.view.View.OnTouchListener;

public class NewUserActivity extends AppCompatActivity implements OnDateSetListener, OnClickListener, OnTouchListener, TextView.OnEditorActionListener {

	private final NumberFormat nf = NumberFormat.getNumberInstance(Locale.getDefault());
	private static final SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CANADA);

	public static String datePicked = "";
	private static final String TAG = "runtracer";

	// GUI elements
	Button mNewUser;
	Button mUserDateofBirth;

	Switch mUserGender;
	Switch mUnitSystem;

	EditText mFullName;
	EditText mUserEmail;
	EditText mUserPassword;

	EditText mUserHeight;
	EditText mUserHipCircumference;
	EditText mUserWeight;
	EditText mUserTargetWeight;
	EditText mUserBodyMassIndex;
	EditText mUserBodyAdiposityIndex;
	EditText mUserFat;
	EditText mUserTargetFat;

	TextView mUserHeightUnits;
	TextView mUserHipCircumferenceUnits;
	TextView mUserWeightUnits;
	TextView mUserWeightTargetUnits;

	String retrievedFullName;
	String retrievedEmail;
	String retrievedPassword;
	String retrievedGender;
	String retrievedMetricSystem;
	String retrievedDOB;
	String retrievedHeight;
	String retrievedHipCircumference;
	String retrievedWeight;
	String retrievedTargetWeight;
	String retrievedFat;
	String retrievedTargetFat;

	double retrievedHeight_v;
	double retrievedHipCircumference_v;
	double retrievedWeight_v;
	double retrievedTargetWeight_v;

	Date retrievedDateOfBirth;

	private double bmi;
	private double bai;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);

		retrievedFullName = "";
		retrievedEmail = "";
		retrievedPassword = "";
		retrievedGender = "";
		retrievedDOB = "";
		retrievedHeight = "0";
		retrievedHipCircumference = "0";
		retrievedWeight = "0";
		retrievedTargetWeight = "0";
		retrievedFat = "0";
		retrievedTargetFat = "0";

		retrievedHeight_v = -1;
		retrievedHipCircumference_v = -1;
		retrievedWeight_v = -1;
		retrievedTargetWeight_v = -1;

		UserData userData = MainActivity.newUser;
		retrievedFullName = userData.getFull_name();
		retrievedEmail = userData.getEmail();
		retrievedMetricSystem = userData.getMetric();

		setContentView(R.layout.activity_new_user);
		this.setupGui();
	}

	private void setupGui() {
		// Set up button click listeners
		mNewUser = (Button) findViewById(R.id.sign_in_button);
		findViewById(R.id.sign_up_button).setOnClickListener(this);
		findViewById(R.id.sign_up_button).setEnabled(true);
		findViewById(R.id.sign_up_button).setVisibility(Button.VISIBLE);

		// Set up view instances
		mFullName = (EditText) findViewById(R.id.full_name);
		mUserEmail = (EditText) findViewById(R.id.email);
		mUserPassword = (EditText) findViewById(R.id.password);
		mUserDateofBirth = (Button) findViewById(R.id.date_picker_button);
		mUserDateofBirth.setText(retrievedDOB);

		mUserGender = (Switch) findViewById(R.id.user_gender_value);
		mUserGender.setOnClickListener(this);
		mUserHeight = (EditText) findViewById(R.id.user_height);

		mUnitSystem= (Switch) findViewById(R.id.user_unit_system);
		mUnitSystem.setChecked(retrievedMetricSystem.compareToIgnoreCase("metric")==0);
		mUnitSystem.setOnClickListener(this);

		mUserHeightUnits = (TextView) findViewById(R.id.height_units);
		mUserWeightUnits = (TextView) findViewById(R.id.weight_units);
		mUserWeightTargetUnits = (TextView) findViewById(R.id.weight_unit_target);
		mUserHipCircumferenceUnits = (TextView) findViewById(R.id.length_units);

		mUserHipCircumference = (EditText) findViewById(R.id.hip_circunference_value);
		mUserWeight = (EditText) findViewById(R.id.user_weight);
		mUserTargetWeight = (EditText) findViewById(R.id.user_weight_target);

		mUserBodyMassIndex = (EditText) findViewById(R.id.user_bmi_value);
		mUserBodyAdiposityIndex = (EditText) findViewById(R.id.user_bai_value);

		mUserBodyMassIndex.setEnabled(false);
		mUserBodyAdiposityIndex.setEnabled(false);

		mUserFat = (EditText) findViewById(R.id.user_fat_percentage);
		mUserTargetFat = (EditText) findViewById(R.id.user_fat_percentage_target);
		mUserDateofBirth.setOnClickListener(this);
		mUserDateofBirth.setOnTouchListener(this);
		mUserDateofBirth.setOnEditorActionListener(this);
		mUserDateofBirth.setText(datePicked);
		mUserHeight.setOnClickListener(this);
		mUserHipCircumference.setOnClickListener(this);
		mUserHipCircumference.setOnTouchListener(this);
		mUserHeight.setOnTouchListener(this);
		mUserWeight.setOnClickListener(this);
		mUserWeight.setOnTouchListener(this);
		mUserTargetWeight.setOnClickListener(this);
		mUserTargetWeight.setOnTouchListener(this);
		mUserFat.setOnClickListener(this);
		mUserFat.setOnTouchListener(this);
		mUserTargetFat.setOnClickListener(this);
		mUserTargetFat.setOnTouchListener(this);
		mFullName.setOnClickListener(this);
		mFullName.setOnTouchListener(this);
		mFullName.setText(retrievedFullName);
		mUserEmail.setOnClickListener(this);
		mUserEmail.setOnTouchListener(this);
		mUserPassword.setOnClickListener(this);
		mUserPassword.setOnTouchListener(this);
		mUserEmail.setText(retrievedEmail);
		mUserGender.setText(retrievedGender);
		mUnitSystem.setText(retrievedMetricSystem);

		if (retrievedMetricSystem.compareToIgnoreCase("metric")==0) {
			mUserHeightUnits.setText(R.string.unit_cm);
			mUserWeightUnits.setText(R.string.unit_kg);
			mUserWeightTargetUnits.setText(R.string.unit_kg);
			mUserHipCircumferenceUnits.setText(R.string.unit_cm);
			mUnitSystem.setChecked(true);
		} else {
			mUserHeightUnits.setText(R.string.unit_ft);
			mUserWeightUnits.setText(R.string.unit_lb);
			mUserWeightTargetUnits.setText(R.string.unit_lb);
			mUserHipCircumferenceUnits.setText(R.string.unit_inches);
			mUnitSystem.setChecked(false);
		}
	}

	boolean calculateBMI() throws ParseException {
		boolean result;
		getValues(false);

		writeLog(String.format(Locale.US, "calculateBMI():retrievedHeight: %s", retrievedHeight));
		writeLog(String.format(Locale.US, "calculateBMI():retrievedHeight_v: %f", retrievedHeight_v));

		writeLog(String.format(Locale.US, "calculateBMI():retrievedHipCircumference: %s", retrievedHipCircumference));
		writeLog(String.format(Locale.US, "calculateBMI():retrievedHipCircumference_v: %f", retrievedHipCircumference_v));

		writeLog(String.format(Locale.US, "calculateBMI():retrievedWeight: %s", retrievedWeight));
		writeLog(String.format(Locale.US, "calculateBMI():retrievedWeight_v: %f", retrievedWeight_v));

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
	public boolean onTouch(View v, MotionEvent event) {
		mUserDateofBirth.setText(datePicked);
		try {
			if (calculateBMI()) {
				mUserBodyMassIndex.setText(String.format(Locale.getDefault(), "%.2f", this.bmi));
				mUserBodyAdiposityIndex.setText(String.format(Locale.getDefault(), "%.2f", this.bai));
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return false;
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		mUserDateofBirth.setText(datePicked);
		return false;
	}

	@Override
	public void onDateSet(Date date) {
		SimpleDateFormat l_format = new SimpleDateFormat("yyyy-MM-dd ", Locale.getDefault());
		retrievedDateOfBirth = date;
		writeLog(String.format(Locale.US, "NewUserActivity: LISTENER RECEIVED: date: %s >> retrievedDateOfBirth: %s ", date.toString(), retrievedDateOfBirth));
		mUserDateofBirth.setText(l_format.format(date));
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

		@NonNull
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current date as the default date in the picker
			final Calendar c = Calendar.getInstance();
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH);

			// Create a new instance of DatePickerDialog and return it
			return new DatePickerDialog(getActivity(), this, year - 20, month, day);
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
			String date = (DateFormat.format("dd-MM-yyyy hh:mm:ss", new java.util.Date()).toString());
//			Log.e(TAG, date + ": " + msg);
		}
	}

	public void showDatePickerDialog(View v) throws ParseException {
		//DialogFragment newFragment = new DatePickerFragment();
		DatePickerFragment newFragment = new DatePickerFragment();
		newFragment.setOnDateSetListener(this);
		newFragment.show(getSupportFragmentManager(), "datePicker");
		retrievedDOB = datePicked;
		writeLog(String.format(Locale.US, "showDatePickerDialog: retrievedDOB  %s ", datePicked));
		SimpleDateFormat l_format = new SimpleDateFormat("yyyy-MM-dd ", Locale.getDefault());
		retrievedDateOfBirth = l_format.parse(datePicked);
		writeLog(String.format(Locale.US, "showDatePickerDialog: retrievedDateOfBirth: %s ", retrievedDateOfBirth));
		mUserDateofBirth.setText(datePicked);
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

	private boolean isAgeValid() {
		Date dateOfBirth = retrievedDateOfBirth;
		Date dateOfToday = new Date();
		int age_v;
		Calendar dob = Calendar.getInstance();
		Calendar today = Calendar.getInstance();

		if (dateOfBirth != null && dob != null) {
			dob.setTime(dateOfBirth);
			today.setTime(dateOfToday);
			if (today.get(Calendar.MONTH) - dob.get(Calendar.MONTH) > 0) {
				age_v = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
			} else {
				age_v = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR) - 1;
			}

			writeLog(String.format(Locale.US, "isAgeValid: dob: %s", dob.toString()));
			writeLog(String.format(Locale.US, "isAgeValid: today: %s", today.toString()));

			writeLog(String.format(Locale.US, "isAgeValid: retrievedDateOfBirth: %s", retrievedDateOfBirth.toString()));
			writeLog(String.format(Locale.US, "isAgeValid: dateOfToday: %s", dateOfToday.toString()));
			writeLog(String.format(Locale.US, "isAgeValid: dateOfBirth: %s", dateOfBirth.toString()));
			writeLog(String.format(Locale.US, "isAgeValid: age: %d", age_v));
		} else {
			writeLog(String.format(Locale.US, "retrievedDateOfBirth!=null: %b", retrievedDateOfBirth != null));
			age_v = 0;
		}
		return age_v > 13;
	}

	private boolean isNameValid(String name) {
		return name.length() > 4;
	}

	private boolean isWeightValid(String weight) {
		boolean result = false;
		try {
			double weight_v = 0.0;
			if (TypeCheck.isNumber(weight)) {
				weight_v = nf.parse(weight).doubleValue();
			}
			if (retrievedMetricSystem.compareToIgnoreCase("metric")==0) {
				result = (weight_v > 20);
			} else {
				result = (weight_v > 40);
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return result;
	}

	private boolean isFatValid(String fat) {
		double fat_v = 0.0;
		try {
			if (TypeCheck.isNumber(fat)) {
				fat_v = nf.parse(fat).doubleValue();
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return (fat_v > 2 && fat_v < 90);
	}

	private boolean isHeightValid(String height) throws ParseException {
		writeLog(String.format(Locale.CANADA, "isHeightValid(%s), retrievedMetricSystem: %b", height, retrievedMetricSystem));
		double height_v = 0.0;
		if (TypeCheck.isNumber(height)) {
			height_v = nf.parse(height).doubleValue();
			writeLog(String.format(Locale.CANADA, "isHeightValid(...), height_v: %f", height_v));
		}
		if (retrievedMetricSystem.compareToIgnoreCase("metric")==0) {
			return (height_v > 40 && height_v < 240);
		} else {
			return (height_v > 3 && height_v < 8);
		}
	}

	private boolean isHipCircumferenceValid(String hip_circumference) throws ParseException {
		double hip_circumference_v = 0.0;
		if (TypeCheck.isNumber(hip_circumference)) {
			hip_circumference_v = nf.parse(hip_circumference).doubleValue();
		}
		if (retrievedMetricSystem.compareToIgnoreCase("metric")==0) {
			return (hip_circumference_v > 20 && hip_circumference_v < 240);
		} else {
			return (hip_circumference_v > 7 && hip_circumference_v < 80);
		}
	}

	public boolean getValues(boolean bFillJSON) {
		try {
			double lretrievedHeight_v;
			double lretrievedHipCircumference_v;
			double lretrievedWeight_v;
			double lretrievedTargetWeight_v;
			double lretrievedFat_v;
			double lretrievedTargetFat_v;

			retrievedFullName = String.valueOf(mFullName.getText());
			retrievedEmail = String.valueOf(mUserEmail.getText());
			retrievedPassword = String.valueOf(mUserPassword.getText());
			retrievedGender = String.valueOf(mUserGender.isChecked() ? "female" : "male");
			retrievedMetricSystem= String.valueOf(mUnitSystem.isChecked() ? "metric" : "imperial");
			if (retrievedDateOfBirth != null) {
				retrievedDOB = (date_format.format(retrievedDateOfBirth));
			}
			retrievedHeight = String.valueOf(mUserHeight.getText());
			retrievedHipCircumference = String.valueOf(mUserHipCircumference.getText());
			retrievedWeight = String.valueOf(mUserWeight.getText());
			retrievedTargetWeight = String.valueOf(mUserTargetWeight.getText());
			retrievedFat = String.valueOf(mUserFat.getText());
			retrievedTargetFat = String.valueOf(mUserTargetFat.getText());

			writeLog(String.format(Locale.US, "retrievedFat: %s", retrievedFat));
			writeLog(String.format(Locale.US, "retrievedTargetFat: %s", retrievedTargetFat));
			lretrievedFat_v = 0.0;
			lretrievedTargetFat_v = 0.0;
			if (retrievedFat != null && retrievedFat.length() > 0) {
				lretrievedFat_v = nf.parse(retrievedFat).doubleValue();
				writeLog(String.format(Locale.US, "lretrievedFat_v: %f", lretrievedFat_v));
			}
			if (retrievedTargetFat != null && retrievedTargetFat.length() > 0) {
				lretrievedTargetFat_v = nf.parse(retrievedTargetFat).doubleValue();
				writeLog(String.format(Locale.US, "lretrievedTargetFat_v: %f", lretrievedTargetFat_v));
			}
			writeLog(String.format(Locale.US, "isAgeValid: %b", isAgeValid()));
			writeLog(String.format(Locale.US, "isWeightValid(retrievedWeight): %b", isWeightValid(retrievedWeight)));
			writeLog(String.format(Locale.US, "isHeightValid(retrievedHeight): %b", isHeightValid(retrievedHeight)));
			writeLog(String.format(Locale.US, "isHipCircumferenceValid(retrievedHipCircumference): %b", isHipCircumferenceValid(retrievedHipCircumference)));
			writeLog(String.format(Locale.US, "isWeightValid(retrievedTargetWeight): %b", isWeightValid(retrievedTargetWeight)));
			if (isAgeValid() && isWeightValid(retrievedWeight) && isHeightValid(retrievedHeight) && isHipCircumferenceValid(retrievedHipCircumference) && isWeightValid(retrievedTargetWeight)) {
				lretrievedHeight_v = nf.parse(retrievedHeight).doubleValue();
				lretrievedHipCircumference_v = nf.parse(retrievedHipCircumference).doubleValue();
				lretrievedWeight_v = nf.parse(retrievedWeight).doubleValue();
				lretrievedTargetWeight_v = nf.parse(retrievedTargetWeight).doubleValue();
			} else {
				return (false);
			}

			if (retrievedMetricSystem.compareToIgnoreCase("metric")==0) {
				retrievedHeight_v = lretrievedHeight_v;
				retrievedHipCircumference_v = lretrievedHipCircumference_v;
				retrievedWeight_v = lretrievedWeight_v;
				retrievedTargetWeight_v = lretrievedTargetWeight_v;
			} else {
				double conv_ft_cm = 30.48;
				retrievedHeight_v = lretrievedHeight_v * conv_ft_cm;
				double conv_in_cm = 2.54;
				retrievedHipCircumference_v = lretrievedHipCircumference_v * conv_in_cm;
				double conv_lb_kg = 0.45359237;
				retrievedWeight_v = lretrievedWeight_v * conv_lb_kg;
				retrievedTargetWeight_v = lretrievedTargetWeight_v * conv_lb_kg;
			}

			if (bFillJSON) {
				if (!isWeightValid(retrievedWeight)) {
					String errorString = "Weight is invalid: " + retrievedWeight;
					Toast.makeText(this, errorString, Toast.LENGTH_SHORT).show();
					return false;
				}
				if (!isHeightValid(retrievedHeight)) {
					String errorString = "Height is invalid: " + retrievedHeight;
					Toast.makeText(this, errorString, Toast.LENGTH_SHORT).show();
					return false;
				}
				if (!isHipCircumferenceValid(retrievedHipCircumference)) {
					String errorString = "Hip Circumference is invalid: " + retrievedHipCircumference;
					Toast.makeText(this, errorString, Toast.LENGTH_SHORT).show();
					return false;
				}
				if (!TypeCheck.isEmailValid(retrievedEmail)) {
					String errorString = "email is invalid: " + retrievedEmail;
					Toast.makeText(this, errorString, Toast.LENGTH_SHORT).show();
					return false;
				}
				if (!isNameValid(retrievedFullName)) {
					String errorString = "Name is invalid: " + retrievedFullName;
					Toast.makeText(this, errorString, Toast.LENGTH_SHORT).show();
					return false;
				}
				if (!isWeightValid(retrievedTargetWeight)) {
					String errorString = "Target Weight is invalid: " + retrievedTargetWeight;
					Toast.makeText(this, errorString, Toast.LENGTH_SHORT).show();
					return false;
				}
				if (!isFatValid(retrievedFat)) {
					String errorString = "Body Fat % is invalid: " + retrievedFat;
					Toast.makeText(this, errorString, Toast.LENGTH_SHORT).show();
					return false;
				}
				if (!isFatValid(retrievedTargetFat)) {
					String errorString = "Target Body Fat % is invalid: " + retrievedTargetFat;
					Toast.makeText(this, errorString, Toast.LENGTH_SHORT).show();
					return false;
				}
				UserData userData = MainActivity.newUser;
				userData.setFull_name(retrievedFullName);
				userData.setEmail(retrievedEmail);
				userData.setPassword(retrievedPassword);
				userData.setGender(mUserGender.isChecked() ? "female" : "male");
				userData.setMetric(mUnitSystem.isChecked() ? "metric" : "imperial");
				userData.setBirthday_date(retrievedDateOfBirth);
				userData.setCurrent_fat_v(lretrievedFat_v);
				userData.setTarget_fat_v(lretrievedTargetFat_v);
				userData.setHeight_v(retrievedHeight_v);
				userData.setHip_circumference_v(retrievedHipCircumference_v);
				userData.setCurrent_weight_v(retrievedWeight_v);
				userData.setTarget_weight_v(retrievedTargetWeight_v);
				writeLog(String.format("getValues: userData: %s", userData.toString()));
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return (true);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.sign_up_button:
				Snackbar.make(v, "Please check your email and setup your password from the link provided.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
				if (getValues(true)) {
					writeLog(String.format(Locale.CANADA, "NewUserActivity: onClick() setResult: %d before this.finish()", RESULT_OK));
					setResult(RESULT_OK);
					this.finish();
					writeLog(String.format(Locale.CANADA, "NewUserActivity: onClick() setResult: %d after this.finish()", RESULT_OK));
				}
				break;

			case R.id.user_gender_value:
				if (!mUserGender.isChecked()) {
					mUserGender.setText(R.string.st_male);
					retrievedGender = "male";
				} else {
					mUserGender.setText(R.string.st_female);
					retrievedGender = "female";
				}
				break;


			case R.id.user_unit_system:
				if (!mUnitSystem.isChecked()) {
					mUnitSystem.setText(R.string.user_unit_system_metric);
					retrievedMetricSystem= "metric";
				} else {
					mUnitSystem.setText(R.string.user_unit_system_imperial);
					retrievedMetricSystem= "imperial";
				}
				break;

			case R.id.date_picker_button:
				try {
					showDatePickerDialog(v);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				break;

			case R.id.full_name:
				break;

			case R.id.email:
				break;

			default:
				break;
		}
	}

	public void writeLog(String msg) {
		String date = (DateFormat.format("dd-MM-yyyy hh:mm:ss", new java.util.Date()).toString());
		Log.e(TAG, date + ": " + msg);
	}
}
