package com.runtracer;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Data
@ToString
@Getter
@Setter
public class UserData implements Serializable {
	private static final long serialVersionUID = 100L;
	private static final SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CANADA);
	private static final SimpleDateFormat local_format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
	private static final String TAG = "user_data";

	private final int RESTING_NO_READINGS = 20;
	private final int RESTING_HR_MARGIN = 5;
	private final int RESTING_HR_MIN = 40;
	private final int RESTING_HR_MAX = 100;

	private String full_name;
	private String birthday;
	private String gender;
	private String height;
	private String hip_circumference;
	private String current_weight;
	private String current_fat;
	private String target_weight;
	private String target_fat;
	private String email;
	private String password;
	private String metric;
	private String idToken;
	private String status;
	private String created;
	private String created_at;

	private double height_v = 0;
	private double hip_circumference_v = 0;
	private double current_weight_v = 0;
	private double current_fat_v = 0;
	private double target_weight_v = 0;
	private double target_fat_v = 0;

	private double height_v_imperial = -1;
	private double hip_circumference_v_imperial = -1;
	private double current_weight_v_imperial = -1;
	private double target_weight_v_imperial = -1;

	private final double conv_km_miles = 0.621371192237;
	private final double conv_kg_lbs = 2.204622618;
	private final double conv_cm_inches = 0.393700787402;

	private Date birthday_date;
	private int age = 0;

	private double vo2max = 0;
	private double cff = 0;
	private double bmr = 0;
	private double rmr = 0;
	private double bmi = 0;
	private double bai = 0;

	//heart rate data
	private int rhr_state = 0;
	private int hr_reading = 0;
	private double current_hr = 0;        // current reading of heart rate
	private double last_hr = 0;           // last reading of heart rate
	private double resting_hr = 0;        // resting heart rate
	private double hr_reserve = 0;        // heart rate reserve
	private double maximum_hr = 0;        // maximum heart rate.
	private double recovery_hr = 0;       // recovery heart rate.
	private double target_hr_light = 0;
	private double target_hr_moderate = 0;
	private double target_hr_heavy = 0;
	private double target_hr_very_heavy = 0;
	private double total_distance_km = 0;
	private double total_distance_miles = 0;
	private double total_calories = 0;
	private int no_runs = 0;
	private String uid;
	private int uid_v = 0;
	private String session_id;
	private int total_runs = 0;
	private final int minimum_age = 18;
	//private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
	private Date created_v;

	public UserData() {
	}

	int getValues() {
		if (this.uid_v <= 0) {
			return -1;
		}
		this.total_distance_miles = this.total_distance_km * this.conv_km_miles;
		if (this.current_weight == null || this.current_weight.compareTo("") == 0 ||
			this.height == null || this.height.compareTo("") == 0 ||
			this.hip_circumference == null || this.hip_circumference.compareTo("") == 0 ||
			this.current_fat == null || this.current_fat.compareTo("") == 0 ||
			this.target_weight == null || this.target_weight.compareTo("") == 0 ||
			this.target_fat == null || this.target_fat.compareTo("") == 0 ||
			this.gender == null || this.gender.compareTo("") == 0 ||
			this.full_name == null || this.full_name.compareTo("") == 0 ||
			this.email == null || this.email.compareTo("") == 0) {
			return (-1);
		}
		writeLog(String.format("created: %s, created_at: %s, created_v: %s", created, created_at, created_v));
		if ((this.created != null) && !(this.created.compareTo("0") == 0) && !(this.created.compareTo("1") == 0)) {
			try {
				writeLog(String.format("inside try: created: %s, created_at: %s, created_v: %s", created, created_at, created_v));
				this.created_v = new Date();
				writeLog(String.format("inside if 03: created: %s, created_at: %s, created_v: %s", created, created_at, created_v));
				this.created_v = date_format.parse(this.created);
				writeLog(String.format("inside if 04: created: %s, created_at: %s, created_v: %s", created, created_at, created_v));
				writeLog(String.format("UserData: created_v: %s", this.created_v.toString()));
			} catch (ParseException e) {
				writeLog(String.format("UserData: parseException: %s", e.toString()));
				e.printStackTrace();
			}
		}
		try {
			writeLog(String.format("created: %s, created_at: %s, created_v: %s", created, created_at, created_v));
			if (this.current_weight != null && this.current_weight.length() > 2) {
				this.current_weight_v = Double.parseDouble(this.current_weight);
			}
			if (this.height != null && this.height.length() > 2) {
				this.height_v = Double.parseDouble(this.height);
			}
			if (this.hip_circumference != null && this.hip_circumference.length() > 2) {
				this.hip_circumference_v = Double.parseDouble(this.hip_circumference);
			}
			if (this.current_fat != null && this.current_fat.length() > 2) {
				this.current_fat_v = Double.parseDouble(this.current_fat);
			}
			if (this.target_weight != null && this.target_weight.length() > 2) {
				this.target_weight_v = Double.parseDouble(this.target_weight);
			}
			if (this.target_fat != null && this.target_fat.length() > 2) {
				this.target_fat_v = Double.parseDouble(this.target_fat);
			}
			if (this.uid != null && this.uid.length() > 1) {
				this.uid_v = Integer.parseInt(this.uid);
			}
		} catch (NumberFormatException nfe) {
			writeLog("YES, GOT AN EXCEPTION: " + nfe.getMessage());
		}

		this.current_weight_v_imperial = current_weight_v * conv_kg_lbs;
		this.height_v_imperial = this.height_v * conv_cm_inches;
		this.hip_circumference_v_imperial = this.hip_circumference_v * conv_cm_inches;
		this.target_weight_v_imperial = this.target_weight_v * conv_kg_lbs;

		/*For males: BMR = (13.75 x WKG) + (5 x HC) - (6.76 x age) + 66
			For females: BMR = (9.56 x WKG) + (1.85 x HC) - (4.68 x age) + 655 */
		if (this.gender.compareToIgnoreCase("male") == 0) {
			this.bmr = 13.75 * this.current_weight_v + 5 * this.height_v - (6.76 * this.age) + 66;
		} else {
			this.bmr = 9.56 * this.current_weight_v + 1.85 * this.height_v - (4.68 * this.age) + 655;
		}
		this.rmr = this.bmr * 1.1;
		// BAI = Body Adiposity Index
		// Metric: (HC / (HM)1.5) - 18
		this.bai = this.hip_circumference_v / Math.pow(this.height_v / 100, 1.5) - 18;
		// BMI = Body Mass Index
		// Metric: BMI = WKG / (HM x HM)
		this.bmi = this.current_weight_v / Math.pow(this.height_v / 100, 2);

		if (this.resting_hr > 0 && this.maximum_hr > this.resting_hr) {
			this.hr_reserve = this.maximum_hr - this.resting_hr;
		}
		//VO2max = 15.3 x (MHR/RHR)
		if (this.resting_hr > 0) {
			this.vo2max = 15.3 * (this.maximum_hr / this.resting_hr);
		}

		/*
		For VO2max ≥ 56 mL•kg-1•min-1:
		CFF = 1.00
		For 56 mL•kg-1•min-1 > VO2max ≥ 54 mL•kg-1•min-1:
		CFF = 1.01
		For 54 mL•kg-1•min-1 > VO2max ≥ 52 mL•kg-1•min-1:
		CFF = 1.02
		For 52 mL•kg-1•min-1 > VO2max ≥ 50 mL•kg-1•min-1:
		CFF = 1.03
		For 50 mL•kg-1•min-1 > VO2max ≥ 48 mL•kg-1•min-1:
		CFF = 1.04
		For 48 mL•kg-1•min-1 > VO2max ≥ 46 mL•kg-1•min-1:
		CFF = 1.05
		For 46 mL•kg-1•min-1 > VO2max ≥ 44 mL•kg-1•min-1:
		CFF = 1.06
		For VO2max < 44 mL•kg-1•min-1:
		CFF = 1.07
		 */
		if (this.vo2max >= 56.00) {
			this.cff = 1.00;
		}
		if (this.vo2max >= 54.00 && this.vo2max < 56.00) {
			this.cff = 1.01;
		}
		if (this.vo2max >= 52.00 && this.vo2max < 54.00) {
			this.cff = 1.02;
		}
		if (this.vo2max >= 50.00 && this.vo2max < 52.00) {
			this.cff = 1.03;
		}
		if (this.vo2max >= 48.00 && this.vo2max < 50.00) {
			this.cff = 1.04;
		}
		if (this.vo2max >= 46.00 && this.vo2max < 48.00) {
			this.cff = 1.05;
		}
		if (this.vo2max >= 44.00 && this.vo2max < 46.00) {
			this.cff = 1.06;
		}
		if (this.vo2max < 44.00) {
			this.cff = 1.07;
		}
		if (this.age > this.minimum_age) {
			this.maximum_hr = (int) (208 - (0.7 * this.age));
			this.target_hr_light = 0.35 * this.maximum_hr;
			this.target_hr_moderate = 0.55 * this.maximum_hr;
			this.target_hr_heavy = 0.7 * this.maximum_hr;
			this.target_hr_very_heavy = 0.9 * this.maximum_hr;
		}
		return 0;
	}

	JSONObject createJSON() {
		JSONObject jsonuserdata = null;
		try {
			jsonuserdata = new JSONObject("{\"key\":\"data\"}");
			jsonuserdata.put("uid_v", this.getUid_v());
			jsonuserdata.put("full_name", this.getFull_name());
			jsonuserdata.put("email", this.getEmail());
			jsonuserdata.put("password", this.getPassword());
			jsonuserdata.put("id_token", this.getIdToken());
			jsonuserdata.put("gender", this.getGender());
			jsonuserdata.put("metric", this.getMetric());
			if (this.birthday_date != null) {
				jsonuserdata.put("birthday", date_format.format(this.getBirthday_date()));
			}
			jsonuserdata.put("height_v", this.getHeight_v());
			jsonuserdata.put("hip_circumference_v", this.getHip_circumference_v());
			jsonuserdata.put("current_weight_v", this.getCurrent_weight_v());
			jsonuserdata.put("current_fat_v", this.getCurrent_fat_v());
			jsonuserdata.put("target_weight_v", this.getTarget_weight_v());
			jsonuserdata.put("target_fat_v", this.getTarget_fat_v());
			jsonuserdata.put("resting_heart_rate", this.getResting_hr());
			jsonuserdata.put("recovery_heart_rate", this.getRecovery_hr());
			writeLog(String.format("UserData: createJSON last line returning: %s", jsonuserdata.toString()));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return (jsonuserdata);
	}

	private int writeJSON(JSONObject jsonuserdata) {
		int returnval = 0;
		try {
			returnval = 0;
			if (!jsonuserdata.isNull("uid_v")) {
				this.uid_v = jsonuserdata.getInt("uid_v");
				writeLog(String.format(Locale.US, "UserData: writeJSON: received: this.uid_v: %d", this.uid_v));
			} else {
				returnval = -1;
			}
			if (!jsonuserdata.isNull("full_name") && jsonuserdata.get("full_name") instanceof String) {
				this.full_name = jsonuserdata.getString("full_name");
				writeLog(String.format("UserData: writeJSON: received: this.full_name: %s", this.full_name));
			} else {
				returnval = -2;
			}
			if (!jsonuserdata.isNull("email") && jsonuserdata.get("email") instanceof String) {
				this.email = jsonuserdata.getString("email");
				writeLog(String.format("UserData: writeJSON: received: this.email: %s", this.email));
			} else {
				returnval = -3;
			}
			if (!jsonuserdata.isNull("password") && jsonuserdata.get("password") instanceof String) {
				this.password = jsonuserdata.getString("password");
				writeLog(String.format("UserData: writeJSON: received: this.password: %s", this.password));
			} else {
				returnval = -4;
			}
			if (!jsonuserdata.isNull("id_token") && jsonuserdata.get("id_token") instanceof String) {
				this.idToken = jsonuserdata.getString("id_token");
				writeLog(String.format("UserData: writeJSON: received: this.idToken: %s", this.idToken));
			} else {
				returnval = -5;
			}
			if (!jsonuserdata.isNull("birthday") && jsonuserdata.get("birthday") instanceof String) {
				this.birthday = jsonuserdata.getString("birthday");
				this.birthday_date = date_format.parse(this.birthday);
			} else {
				returnval = -6;
			}
			if (!jsonuserdata.isNull("gender")) {
				this.gender = jsonuserdata.get("gender").toString();
				writeLog(String.format("UserData: writeJSON: %s", this.gender));
			} else {
				returnval = -7;
			}
			if (!jsonuserdata.isNull("metric")) {
				this.metric= jsonuserdata.get("metric").toString();
				writeLog(String.format("UserData: writeJSON: %s", this.metric));
			} else {
				returnval = -8;
			}
			if (!jsonuserdata.isNull("height_v")) {
				if (jsonuserdata.get("height_v") instanceof Double) {
					this.height_v = jsonuserdata.getDouble("height_v");
					writeLog(String.format("UserData: writeJSON: height_v instance of Double: %s", this.getHeight_v()));
				} else {
					if (jsonuserdata.get("height_v") instanceof Long) {
						this.height_v = (double) jsonuserdata.getLong("height_v");
						writeLog(String.format("UserData: writeJSON: height_v instance of Long: %s", this.getHeight_v()));
					} else {
						if (jsonuserdata.get("height_v") instanceof Integer) {
							this.height_v = (double) jsonuserdata.getInt("height_v");
							writeLog(String.format("UserData: writeJSON: height_v instance of Integer: %s", this.getHeight_v()));
						}
					}
				}
			} else {
				returnval = -9;
			}
			if (!jsonuserdata.isNull("hip_circumference_v")) {
				if (jsonuserdata.get("hip_circumference_v") instanceof Double) {
					this.hip_circumference_v = jsonuserdata.getDouble("hip_circumference_v");
					writeLog(String.format("UserData: writeJSON: hip_circumference_v instance of Double: %s", this.getHip_circumference_v()));
				} else {
					if (jsonuserdata.get("hip_circumference_v") instanceof Long) {
						this.hip_circumference_v = (double) jsonuserdata.getLong("hip_circumference_v");
						writeLog(String.format("UserData: writeJSON: hip_circumference_v instance of Long: %s", this.getHip_circumference_v()));
					} else {
						if (jsonuserdata.get("hip_circumference_v") instanceof Integer) {
							this.hip_circumference_v = (double) jsonuserdata.getInt("hip_circumference_v");
							writeLog(String.format("UserData: writeJSON: hip_circumference_v instance of Integer: %s", this.getHip_circumference_v()));
						}
					}
				}
			} else {
				returnval = -10;
			}
			if (!jsonuserdata.isNull("current_weight_v")) {
				if (jsonuserdata.get("current_weight_v") instanceof Double) {
					this.current_weight_v = jsonuserdata.getDouble("current_weight_v");
					writeLog(String.format("UserData: writeJSON: current_weight_v instance of Double: %s", this.getCurrent_weight_v()));
				} else {
					if (jsonuserdata.get("current_weight_v") instanceof Long) {
						this.current_weight_v = (double) jsonuserdata.getLong("current_weight_v");
						writeLog(String.format("UserData: writeJSON: current_weight_v instance of Long: %s", this.getCurrent_weight_v()));
					} else {
						if (jsonuserdata.get("current_weight_v") instanceof Integer) {
							this.current_weight_v = (double) jsonuserdata.getInt("current_weight_v");
							writeLog(String.format("UserData: writeJSON: current_weight_v instance of Integer: %s", this.getCurrent_weight_v()));
						}
					}
				}
			} else {
				returnval = -11;
			}
			if (!jsonuserdata.isNull("current_fat_v")) {
				if (jsonuserdata.get("current_fat_v") instanceof Double) {
					this.current_fat_v = jsonuserdata.getDouble("current_fat_v");
					writeLog(String.format("UserData: writeJSON: current_fat_v instance of Double: %s", this.getCurrent_fat_v()));
				} else {
					if (jsonuserdata.get("current_fat_v") instanceof Long) {
						this.current_fat_v = (double) jsonuserdata.getLong("current_fat_v");
						writeLog(String.format("UserData: writeJSON: current_fat_v instance of Long: %s", this.getCurrent_fat_v()));
					} else {
						if (jsonuserdata.get("current_fat_v") instanceof Integer) {
							this.current_fat_v = (double) jsonuserdata.getInt("current_fat_v");
							writeLog(String.format("UserData: writeJSON: current_fat_v instance of Integer: %s", this.getCurrent_fat_v()));
						}
					}
				}
			} else {
				returnval = -12;
			}
			if (!jsonuserdata.isNull("target_weight_v")) {
				if (jsonuserdata.get("target_weight_v") instanceof Double) {
					this.target_weight_v = jsonuserdata.getDouble("target_weight_v");
					writeLog(String.format("UserData: writeJSON: target_weight_v instance of Double: %s", this.getTarget_weight_v()));
				} else {
					if (jsonuserdata.get("target_weight_v") instanceof Long) {
						this.target_weight_v = (double) jsonuserdata.getLong("target_weight_v");
						writeLog(String.format("UserData: writeJSON: target_weight_v instance of Long: %s", this.getTarget_weight_v()));
					} else {
						if (jsonuserdata.get("target_weight_v") instanceof Integer) {
							this.target_weight_v = (double) jsonuserdata.getInt("target_weight_v");
							writeLog(String.format("UserData: writeJSON: target_weight_v instance of Integer: %s", this.getTarget_weight_v()));
						}
					}
				}
			} else {
				returnval = -13;
			}
			if (!jsonuserdata.isNull("target_fat_v")) {
				if (jsonuserdata.get("target_fat_v") instanceof Double) {
					this.target_fat_v = jsonuserdata.getDouble("target_fat_v");
					writeLog(String.format("UserData: writeJSON: target_fat_v instance of Double: %s", this.getTarget_weight_v()));
				} else {
					if (jsonuserdata.get("target_fat_v") instanceof Long) {
						this.target_fat_v = (double) jsonuserdata.getLong("target_fat_v");
						writeLog(String.format("UserData: writeJSON: target_fat_v instance of Long: %s", this.getTarget_weight_v()));
					} else {
						if (jsonuserdata.get("target_fat_v") instanceof Integer) {
							this.target_fat_v = (double) jsonuserdata.getInt("target_fat_v");
							writeLog(String.format("UserData: writeJSON: target_fat_v instance of Integer: %s", this.getTarget_weight_v()));
						}
					}
				}
			} else {
				returnval = -14;
			}
			if (!jsonuserdata.isNull("resting_heart_rate")) {
				if (jsonuserdata.get("resting_heart_rate") instanceof String) {
					this.resting_hr = Double.parseDouble((String) jsonuserdata.get("resting_heart_rate"));
				} else {
					if (jsonuserdata.get("resting_heart_rate") instanceof Double) {
						this.resting_hr = jsonuserdata.getDouble("resting_heart_rate");
					}
				}
				writeLog(String.format(Locale.US, "UserData: writeJSON: resting_heart_rate: %.2f", this.resting_hr));
			} else {
				returnval = -16;
			}
			if (!jsonuserdata.isNull("recovery_heart_rate")) {
				if (jsonuserdata.get("recovery_heart_rate") instanceof String) {
					this.recovery_hr = Double.parseDouble((String) jsonuserdata.get("recovery_heart_rate"));
				} else {
					if (jsonuserdata.get("recovery_heart_rate") instanceof Double) {
						this.recovery_hr = jsonuserdata.getDouble("recovery_heart_rate");
					}
				}
				writeLog(String.format(Locale.US, "UserData: writeJSON: recovery_hr: %.2f", this.recovery_hr));
			} else {
				returnval = -17;
			}
			if (!jsonuserdata.isNull("created")) {
				this.created_at = jsonuserdata.get("created").toString();
			}
			Calendar calendar_birth = Calendar.getInstance();
			calendar_birth.setTime(this.getBirthday_date());
			Calendar calendar_now = Calendar.getInstance();
			this.age = calendar_now.get(Calendar.YEAR) - calendar_birth.get(Calendar.YEAR);
			this.getValues();
		} catch (JSONException | ParseException e) {
			e.printStackTrace();
		}
		return (returnval);
	}

	public UserData fromJSON(JSONObject jsonObject) {
		this.writeJSON(jsonObject);
		return this;
	}

	public JSONObject toJSON() {
		return (this.createJSON());
	}

	public void writeLog(String msg) {
		Date datenow = new Date();
		String date = date_format.format(datenow);
		Log.e(TAG, date + ": " + msg);
	}

	private void writeObject(java.io.ObjectOutputStream out)
		throws IOException {
		out.writeObject(this.full_name);
		out.writeObject(this.birthday);
		out.writeObject(this.gender);
		out.writeObject(this.height);
		out.writeObject(this.hip_circumference);
		out.writeObject(this.current_weight);
		out.writeObject(this.current_fat);
		out.writeObject(this.target_weight);
		out.writeObject(this.target_fat);
		out.writeObject(this.email);
		out.writeObject(this.status);
		out.writeObject(this.created);
		out.writeObject(this.created_at);

		out.writeObject(this.height_v);
		out.writeObject(this.hip_circumference_v);
		out.writeObject(this.current_weight_v);
		out.writeObject(this.current_fat_v);
		out.writeObject(this.target_weight_v);
		out.writeObject(this.target_fat_v);

		out.writeObject(height_v_imperial);
		out.writeObject(hip_circumference_v_imperial);
		out.writeObject(current_weight_v_imperial);
		out.writeObject(target_weight_v_imperial);

		out.writeObject(this.birthday_date);
		out.writeObject(this.age);
		out.writeObject(this.vo2max);
		out.writeObject(this.cff);
		out.writeObject(this.bmr);
		out.writeObject(this.rmr);
		out.writeObject(this.bmi);
		out.writeObject(this.bai);
		out.writeObject(this.rhr_state);
		out.writeObject(this.hr_reading);
		out.writeObject(this.current_hr);
		out.writeObject(this.last_hr);
		out.writeObject(this.resting_hr);
		out.writeObject(this.hr_reserve);
		out.writeObject(this.maximum_hr);
		out.writeObject(this.recovery_hr);
		out.writeObject(this.target_hr_light);
		out.writeObject(this.target_hr_moderate);
		out.writeObject(this.target_hr_heavy);
		out.writeObject(this.target_hr_very_heavy);

		out.writeObject(this.total_distance_km);
		out.writeObject(this.total_distance_miles);
		out.writeObject(this.total_calories);
		out.writeObject(this.no_runs);

		out.writeObject(this.uid);
		out.writeObject(this.uid_v);
		out.writeObject(this.session_id);
		out.writeObject(this.total_runs);
		out.writeObject(this.created_v);
	}

	private void readObject(java.io.ObjectInputStream in)
		throws IOException, ClassNotFoundException {
		// populate the fields of 'this' from the data in 'in'...
		this.full_name = (String) in.readObject();
		this.birthday = (String) in.readObject();
		this.gender = (String) in.readObject();
		this.height = (String) in.readObject();
		this.hip_circumference = (String) in.readObject();
		this.current_weight = (String) in.readObject();
		this.current_fat = (String) in.readObject();
		this.target_weight = (String) in.readObject();
		this.target_fat = (String) in.readObject();
		this.email = (String) in.readObject();
		this.status = (String) in.readObject();
		this.created = (String) in.readObject();
		this.created_at = (String) in.readObject();

		this.height_v = (double) in.readObject();
		this.hip_circumference_v = (double) in.readObject();
		this.current_weight_v = (double) in.readObject();
		this.current_fat_v = (double) in.readObject();
		this.target_weight_v = (double) in.readObject();
		this.target_fat_v = (double) in.readObject();

		this.height_v_imperial = (double) in.readObject();
		this.hip_circumference_v_imperial = (double) in.readObject();
		this.current_weight_v_imperial = (double) in.readObject();
		this.target_weight_v_imperial = (double) in.readObject();

		this.birthday_date = (Date) in.readObject();
		this.age = (int) in.readObject();
		this.vo2max = (double) in.readObject();
		this.cff = (double) in.readObject();
		this.bmr = (double) in.readObject();
		this.rmr = (double) in.readObject();
		this.bmi = (double) in.readObject();
		this.bai = (double) in.readObject();
		this.rhr_state = (int) in.readObject();
		this.hr_reading = (int) in.readObject();
		this.current_hr = (double) in.readObject();
		this.last_hr = (double) in.readObject();
		this.resting_hr = (double) in.readObject();
		this.hr_reserve = (double) in.readObject();
		this.maximum_hr = (double) in.readObject();
		this.recovery_hr = (double) in.readObject();
		this.target_hr_light = (double) in.readObject();
		this.target_hr_moderate = (double) in.readObject();
		this.target_hr_heavy = (double) in.readObject();
		this.target_hr_very_heavy = (double) in.readObject();

		this.total_distance_km = (double) in.readObject();
		this.total_distance_miles = (double) in.readObject();
		this.total_calories = (double) in.readObject();
		this.no_runs = (int) in.readObject();

		this.uid = (String) in.readObject();
		this.uid_v = (int) in.readObject();
		this.session_id = (String) in.readObject();
		this.total_runs = (int) in.readObject();
		this.created_v = (Date) in.readObject();
	}

	public boolean clean() {
		uid = null;
		full_name = null;
		birthday = null;
		gender = null;
		height = null;
		hip_circumference = null;
		current_weight = null;
		current_fat = null;
		target_weight = null;
		target_fat = null;
		email = null;
		session_id = null;
		status = null;
		created = null;
		created = null;
		height_v = -1;
		hip_circumference_v = -1;
		current_weight_v = -1;
		current_fat_v = -1;
		target_weight_v = -1;
		target_fat_v = -1;
		height_v_imperial = -1;
		hip_circumference_v_imperial = -1;
		current_weight_v_imperial = -1;
		target_weight_v_imperial = -1;
		birthday_date = new Date();
		age = -1;
		vo2max = -1;
		cff = -1;
		bmr = -1;
		rmr = -1;
		bmi = -1;
		bai = -1;
		rhr_state = -1;
		hr_reading = -1;
		current_hr = -1;        // current reading of heart rate
		last_hr = -1;           // last reading of heart rate
		resting_hr = -1;        // resting heart rate
		hr_reserve = -1;        // heart rate reserve
		maximum_hr = -1;        // maximum heart rate.
		recovery_hr = -1;       // recovery heart rate.
		total_distance_km = 0;
		total_distance_miles = 0;
		total_calories = 0;
		no_runs = 0;
		target_hr_light = -1;
		target_hr_moderate = -1;
		target_hr_heavy = -1;
		target_hr_very_heavy = -1;
		uid_v = -1;
		total_runs = 0;
		created_v = new Date(0);
		return true;
	}
}

