package com.runtracer.model;

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

@Data
@Getter
@Setter
public class RunData implements Serializable {
	private final long serialVersionUID = 100L;
	private final int ERROR = -1001;
	private final String TAG = "rundata";
	private final SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CANADA);

	private String run_date_start;
	private String run_date_end;
	private String uid;

	private long run_id_v;

	final double conv_m_s_km_h = 3.6;
	final double conv_km_miles = 0.621371192237;

	private Date run_date_start_v;
	private Date run_date_end_v;

	private double average_speed_km_h_v;
	private double average_speed_miles_h_v;
	private double current_speed_m_s_v;
	private double current_speed_km_h_v;
	private double current_speed_miles_h_v;
	private double calories_v_distance;
	private double calories_v_heart_beat;
	private double current_weight_v;
	private double current_fat_v;
	private int inclination;
	private double threadmill_factor;
	private int current_heart_rate;
	private int recovery_hr;
	private int resting_hr;
	private long granularity_time = 2000;
	private double distance_m_v = 0.0;
	private double distance_km_v = 0.0;
	private double distance_miles_v = 0.0;
	private double gps_distance_km = 0.0;
	private double gps_distance_miles = 0.0;

	public void writeLog(String msg) {
		SimpleDateFormat datef1 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.CANADA);
		Date datenow = new Date();
		String date = datef1.format(datenow);
		Log.e(TAG, date + ": " + msg);
	}

	public RunData() {
		run_date_start = "";
		run_date_end = "";
		run_date_start_v = new Date(0);
		run_date_end_v = new Date(0);
		distance_km_v = -1;
		distance_miles_v = -1;
		current_speed_km_h_v = -1;
		current_speed_miles_h_v = -1;
		average_speed_km_h_v = -1;
		average_speed_miles_h_v = -1;
		calories_v_distance = -1;
		calories_v_heart_beat = -1;
		current_heart_rate = -1;
		current_weight_v = -1;
		current_fat_v = -1;
		inclination = -1;
		threadmill_factor = -1;
		gps_distance_km = 0.0;
		gps_distance_miles = 0.0;
	}

	public JSONObject toJSON() {
		return (this.createJSON());
	}

	private JSONObject createJSON() {
		JSONObject jsonRunData = null;
		writeLog("RunData: createJSON()");
		try {
			jsonRunData = new JSONObject("{\"key\":\"data\"}");
			jsonRunData.put("runid", this.run_id_v);
			jsonRunData.put("uid", this.uid);
			jsonRunData.put("distance", this.distance_km_v);
			jsonRunData.put("distance_gps", this.gps_distance_km);
			jsonRunData.put("average_speed", this.average_speed_km_h_v);
			jsonRunData.put("calories_distance", this.calories_v_distance);
			jsonRunData.put("calories_heart_beat", this.calories_v_heart_beat);
			jsonRunData.put("current_weight", this.current_weight_v);
			jsonRunData.put("current_fat", this.current_fat_v);
			jsonRunData.put("date_start", date_format.format(this.run_date_start_v));
			jsonRunData.put("date_end", date_format.format(this.run_date_end_v));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonRunData;
	}

	public RunData fromJSON(JSONObject json) {
		this.writeJSON(json);
		return (this);
	}

	private int writeJSON(JSONObject json_run_data) {
		try {
			if (!json_run_data.isNull("runid") && json_run_data.get("runid") instanceof Long) {
				this.run_id_v = json_run_data.getLong("runid");
			} else {
				if (!json_run_data.isNull("runid") && json_run_data.get("runid") instanceof Integer) {
					this.run_id_v = (long) json_run_data.getInt("runid");
				} else {
					if (!json_run_data.isNull("runid") && json_run_data.get("runid") instanceof String) {
						this.run_id_v = Long.parseLong(json_run_data.getString("runid"));
					}
				}
			}
			if (!json_run_data.isNull("uid") && json_run_data.get("uid") instanceof String) {
				this.uid = json_run_data.getString("uid");
			}

			if (!json_run_data.isNull("distance")) {
				this.distance_km_v = json_run_data.getDouble("distance");
			}
			if (!json_run_data.isNull("distance_gps")) {
				this.gps_distance_km = json_run_data.getDouble("distance_gps");
			}
			if (!json_run_data.isNull("average_speed")) {
				this.average_speed_km_h_v = json_run_data.getDouble("average_speed");
			}
			if (!json_run_data.isNull("calories_distance")) {
				this.calories_v_distance = json_run_data.getDouble("calories_distance");
			}
			if (!json_run_data.isNull("calories_heart_beat")) {
				this.calories_v_heart_beat = json_run_data.getDouble("calories_heart_beat");
			}
			if (!json_run_data.isNull("current_weight")) {
				this.current_weight_v = json_run_data.getDouble("current_weight");
			}
			if (!json_run_data.isNull("current_fat")) {
				this.current_fat_v = json_run_data.getDouble("current_fat");
			}
			if (!json_run_data.isNull("date_start")) {
				this.run_date_start = (String) json_run_data.get("date_start");
			}
			if (!json_run_data.isNull("date_end")) {
				this.run_date_end = (String) json_run_data.get("date_end");
			}
			this.getValues();
			this.run_date_start_v = new Date();
			this.run_date_start_v = date_format.parse(this.run_date_start);
			this.run_date_end_v = new Date();
			this.run_date_end_v = date_format.parse(this.run_date_end);
		} catch (ParseException | JSONException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public boolean setStartTime() {
		this.run_date_start_v = new Date();
		Calendar ccdate = Calendar.getInstance();
		ccdate.setTime(this.run_date_start_v);
		int am_or_pm = ccdate.get(Calendar.AM_PM);
		this.run_date_start = String.format(Locale.US, "%04d-%02d-%02d %02d:%02d:%02d %s",
			ccdate.get(Calendar.YEAR),
			ccdate.get(Calendar.MONTH) + 1,
			ccdate.get(Calendar.DAY_OF_MONTH),
			ccdate.get(Calendar.HOUR),
			ccdate.get(Calendar.MINUTE),
			ccdate.get(Calendar.SECOND),
			am_or_pm == 0 ? "am" : "pm");

		writeLog(String.format("setting start time to: %s, should be %s", this.run_date_start, this.run_date_start_v.toString()));

		return (true);
	}

	public boolean setEndTime() {
		this.run_date_end_v = new Date();
		Calendar ccdate = Calendar.getInstance();
		ccdate.setTime(this.run_date_end_v);
		int am_or_pm = ccdate.get(Calendar.AM_PM);
		this.run_date_end = String.format(Locale.US, "%04d-%02d-%02d %02d:%02d:%02d %s",
			ccdate.get(Calendar.YEAR),
			ccdate.get(Calendar.MONTH) + 1,
			ccdate.get(Calendar.DAY_OF_MONTH),
			ccdate.get(Calendar.HOUR),
			ccdate.get(Calendar.MINUTE),
			ccdate.get(Calendar.SECOND),
			am_or_pm == 0 ? "am" : "pm");

		writeLog(String.format("setting end time to: %s, should be %s", this.run_date_end, this.run_date_end_v.toString()));
		return (true);
	}

	public int getValues() {
		this.distance_miles_v = this.distance_km_v * this.conv_km_miles;
		this.average_speed_miles_h_v = this.average_speed_km_h_v * this.conv_km_miles;
		this.current_speed_miles_h_v = this.current_speed_km_h_v * this.conv_km_miles;
		this.gps_distance_miles = this.gps_distance_km * this.conv_km_miles;
		return (0);
	}

	public int getDateValues() throws ParseException {
		this.getValues();
		this.run_date_start_v = new Date();
		this.run_date_start_v = date_format.parse(this.run_date_start);
		this.run_date_end_v = new Date();
		this.run_date_end_v = date_format.parse(this.run_date_end);
		int no_xpoints = (int) ((this.run_date_end_v.getTime() - this.run_date_start_v.getTime()));
		return no_xpoints / 1000;
	}

	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		out.writeObject(this.run_date_start);
		out.writeObject(this.run_date_end);
		out.writeObject(this.uid);
		out.writeObject(this.run_id_v);
		out.writeObject(this.run_date_start_v);
		out.writeObject(this.run_date_end_v);
		out.writeObject(this.average_speed_km_h_v);
		out.writeObject(this.average_speed_miles_h_v);
		out.writeObject(this.current_speed_m_s_v);
		out.writeObject(this.current_speed_km_h_v);
		out.writeObject(this.current_speed_miles_h_v);
		out.writeObject(this.calories_v_distance);
		out.writeObject(this.calories_v_heart_beat);
		out.writeObject(this.current_weight_v);
		out.writeObject(this.current_fat_v);
		out.writeObject(this.inclination);
		out.writeObject(this.threadmill_factor);
		out.writeObject(this.current_heart_rate);
		out.writeObject(this.recovery_hr);
		out.writeObject(this.resting_hr);
		out.writeObject(this.granularity_time);
		out.writeObject(this.distance_m_v);
		out.writeObject(this.distance_km_v);
		out.writeObject(this.distance_miles_v);
		out.writeObject(this.gps_distance_km);
		out.writeObject(this.gps_distance_miles);
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		this.run_date_start = (String) in.readObject();
		this.run_date_end = (String) in.readObject();
		this.uid = (String) in.readObject();
		this.run_id_v = (Long) in.readObject();
		this.run_date_start_v = (Date) in.readObject();
		this.run_date_end_v = (Date) in.readObject();
		this.average_speed_km_h_v = (double) in.readObject();
		this.average_speed_miles_h_v = (double) in.readObject();
		this.current_speed_m_s_v = (double) in.readObject();
		this.current_speed_km_h_v = (double) in.readObject();
		this.current_speed_miles_h_v = (double) in.readObject();
		this.calories_v_distance = (double) in.readObject();
		this.calories_v_heart_beat = (double) in.readObject();
		this.current_weight_v = (double) in.readObject();
		this.current_fat_v = (double) in.readObject();
		this.inclination = (int) in.readObject();
		this.threadmill_factor = (double) in.readObject();
		this.current_heart_rate = (int) in.readObject();
		this.recovery_hr = (int) in.readObject();
		this.resting_hr = (int) in.readObject();
		this.granularity_time = (long) in.readObject();
		this.distance_m_v = (double) in.readObject();
		this.distance_km_v = (double) in.readObject();
		this.distance_miles_v = (double) in.readObject();
		this.gps_distance_km = (double) in.readObject();
		this.gps_distance_miles = (double) in.readObject();
	}
}
