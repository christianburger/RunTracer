package com.runtracer.model;

import android.util.Log;

import com.runtracer.utilities.TypeCheck;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class RunInstant implements Serializable {
	private static final String TAG = "run_instant";
	private final long serialVersionUID = 100L;
	private final SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CANADA);

	private String uid;
	private long runID;

	private long ctime;
	private double current_motion_speed_km_h_v;
	private double current_motion_distance_km_v;
	private double current_gps_speed_km_h;
	private double current_gps_distance_km;
	private double calories_v_distance;
	private double calories_v_heart_beat;
	private int current_heart_rate;
	private double longitude;
	private double latitude;
	private double altitude;

	public RunInstant fromJSON(JSONObject json) {
		writeLog("RunInstant: fromJSON() received: " + json);
		try {
			if (!json.isNull("uid") && json.get("uid") instanceof String) {
				this.uid = json.getString("uid");
			}
			if (!json.isNull("runid") && json.get("runid") instanceof Long) {
				this.runID= json.getLong("runid");
			} else {
				if (!json.isNull("runid") && json.get("runid") instanceof Integer) {
					this.runID = (long) json.getInt("runid");
				} else {
					if (!json.isNull("runid") && json.get("runid") instanceof String) {
						this.runID = Long.parseLong(json.getString("runid"));
					}
				}
			}
			if (!json.isNull("ctime") && json.get("ctime") instanceof Long) {
				this.ctime = json.getLong("ctime");
			} else {
				if (!json.isNull("ctime") && json.get("ctime") instanceof Integer) {
					this.ctime = (long) (json.getInt("ctime"));
				} else {
					if (!json.isNull("ctime") && json.get("ctime") instanceof String) {
						if (TypeCheck.isNumber(json.getString("ctime"))) {
							this.ctime = Long.parseLong(json.getString("ctime"));
						} else {
							writeLog(String.format(Locale.CANADA, "RunInstant: fromJSON: ERROR: json.getString(\"ctime\"): %s NOT NUMBER", json.getString("ctime")));
							throw new NumberFormatException(String.format(Locale.CANADA, "RunInstant: fromJSON: ERROR: json.getString(\"ctime\"): %s NOT NUMBER", json.getString("ctime")));
						}
					}
				}
			}
			writeLog(String.format(Locale.CANADA, "RunInstant: fromJSON: json.isNull(\"motion_speed\"): %b", json.isNull("motion_speed")));
			writeLog(String.format(Locale.CANADA, "RunInstant: fromJSON: json.get(\"motion_speed\").toString(): %s", (json.get("motion_speed")).toString()));
			writeLog(String.format(Locale.CANADA, "RunInstant: fromJSON: json.get(\"motion_speed\").getClass(): %s", (json.get("motion_speed")).getClass()));
			if (!json.isNull("motion_speed") && json.get("motion_speed") instanceof Double) {
				this.current_motion_speed_km_h_v = json.getDouble("motion_speed");
			} else {
				if (!json.isNull("motion_speed") && json.get("motion_speed") instanceof Float) {
					this.current_motion_speed_km_h_v = json.getDouble("motion_speed");
				} else {
					if (!json.isNull("motion_speed") && json.get("motion_speed") instanceof String) {
						this.current_motion_speed_km_h_v = Double.parseDouble(json.getString("motion_speed"));
					}
				}
			}
			if (!json.isNull("motion_distance") && json.get("motion_distance") instanceof Double) {
				this.current_motion_distance_km_v = json.getDouble("motion_distance");
			} else {
				if (!json.isNull("motion_distance") && json.get("motion_distance") instanceof Float) {
					this.current_motion_distance_km_v = (json.getDouble("motion_distance"));
				} else {
					if (!json.isNull("motion_distance") && json.get("motion_distance") instanceof String) {
						this.current_motion_distance_km_v = Double.parseDouble(json.getString("motion_distance"));
					}
				}
			}
			if (!json.isNull("gps_speed") && json.get("gps_speed") instanceof Double) {
				this.current_gps_speed_km_h = json.getDouble("gps_speed");
			} else {
				if (!json.isNull("gps_speed") && json.get("gps_speed") instanceof Float) {
					this.current_gps_speed_km_h = json.getDouble("gps_speed");
				} else {
					if (!json.isNull("gps_speed") && json.get("gps_speed") instanceof String) {
						this.current_gps_speed_km_h = Double.parseDouble(json.getString("gps_speed"));
					}
				}
			}
			if (!json.isNull("gps_distance") && json.get("gps_distance") instanceof Double) {
				this.current_gps_distance_km = json.getDouble("gps_distance");
			} else {
				if (!json.isNull("gps_distance") && json.get("gps_distance") instanceof Float) {
					this.current_gps_distance_km = json.getDouble("gps_distance");
				} else {
					if (!json.isNull("gps_distance") && json.get("gps_distance") instanceof String) {
						this.current_gps_distance_km = Double.parseDouble(json.getString("gps_distance"));
					}
				}
			}
			if (!json.isNull("calories_distance") && json.get("calories_distance") instanceof Double) {
				this.calories_v_distance = json.getDouble("calories_distance");
			} else {
				if (!json.isNull("calories_distance") && json.get("calories_distance") instanceof Float) {
					this.calories_v_distance = json.getDouble("calories_distance");
				} else {
					if (!json.isNull("calories_distance") && json.get("calories_distance") instanceof String) {
						this.calories_v_distance = Double.parseDouble(json.getString("calories_distance"));
					}
				}
			}
			if (!json.isNull("calories_heart_beat") && json.get("calories_heart_beat") instanceof Double) {
				this.calories_v_heart_beat = json.getDouble("calories_heart_beat");
			} else {
				if (!json.isNull("calories_heart_beat") && json.get("calories_heart_beat") instanceof Float) {
					this.calories_v_heart_beat = json.getDouble("calories_heart_beat");
				} else {
					if (!json.isNull("calories_heart_beat") && json.get("calories_heart_beat") instanceof String) {
						this.calories_v_heart_beat = Double.parseDouble(json.getString("calories_heart_beat"));
					}
				}
			}
			if (!json.isNull("heart_rate") && json.get("heart_rate") instanceof Integer) {
				this.current_heart_rate = (json.getInt("heart_rate"));
			} else {
				if (!json.isNull("heart_rate") && json.get("heart_rate") instanceof String) {
					this.current_heart_rate = Integer.parseInt(json.getString("heart_rate"));
				}
			}

			if (!json.isNull("longitude") && json.get("longitude") instanceof Double) {
				this.longitude = json.getDouble("longitude");
			} else {
				if (!json.isNull("longitude") && json.get("longitude") instanceof Float) {
					this.longitude = json.getDouble("longitude");
				} else {
					if (!json.isNull("longitude") && json.get("longitude") instanceof String) {
						this.longitude = Double.parseDouble(json.getString("longitude"));
					}
				}
			}
			if (!json.isNull("latitude") && json.get("latitude") instanceof Double) {
				this.latitude = json.getDouble("latitude");
			} else {
				if (!json.isNull("latitude") && json.get("latitude") instanceof Float) {
					this.latitude = json.getDouble("latitude");
				} else {
					if (!json.isNull("latitude") && json.get("latitude") instanceof String) {
						this.latitude = Double.parseDouble(json.getString("latitude"));
					}
				}
			}
			if (!json.isNull("altitude") && json.get("altitude") instanceof Double) {
				this.altitude = json.getDouble("altitude");
			} else {
				if (!json.isNull("altitude") && json.get("altitude") instanceof Float) {
					this.altitude = json.getDouble("altitude");
				} else {
					if (!json.isNull("altitude") && json.get("altitude") instanceof String) {
						this.altitude = Double.parseDouble(json.getString("altitude"));
					}
				}
			}
		} catch (JSONException e) {
			writeLog("RunInstant: fromJSON() EXCEPTION: " + e.toString());
			e.printStackTrace();
		}
		writeLog("RunInstant: fromJSON() returning: " + this);
		writeLog("RunInstant: fromJSON() this.getCtime(): " + this.getCtime());
		return this;
	}

	public JSONObject toJSON() {
		JSONObject jsonRunData = null;
		try {
			jsonRunData = new JSONObject("{\"key\":\"data\"}");
			jsonRunData.put("uid", this.uid);
			jsonRunData.put("runid", this.runID);
			jsonRunData.put("ctime", this.ctime);
			jsonRunData.put("motion_speed", this.current_motion_speed_km_h_v);
			jsonRunData.put("motion_distance", this.current_motion_distance_km_v);
			jsonRunData.put("gps_speed", this.current_gps_speed_km_h);
			jsonRunData.put("gps_distance", this.current_gps_distance_km);
			jsonRunData.put("calories_distance", this.calories_v_distance);
			jsonRunData.put("calories_heart_beat", this.calories_v_heart_beat);
			jsonRunData.put("heart_rate", this.current_heart_rate);
			jsonRunData.put("longitude", this.longitude);
			jsonRunData.put("latitude", this.latitude);
			jsonRunData.put("altitude", this.altitude);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		writeLog("RunInstant: toJSON() returning: "+jsonRunData);
		return jsonRunData;
	}

	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		out.writeObject(this.uid);
		out.writeObject(this.runID);
		out.writeObject(this.ctime);
		out.writeObject(this.current_motion_speed_km_h_v);
		out.writeObject(this.current_motion_distance_km_v);
		out.writeObject(this.current_gps_speed_km_h);
		out.writeObject(this.current_gps_distance_km);
		out.writeObject(this.calories_v_distance);
		out.writeObject(this.calories_v_heart_beat);
		out.writeObject(this.current_heart_rate);
		out.writeObject(this.longitude);
		out.writeObject(this.latitude);
		out.writeObject(this.altitude);
	}

	private void readObject(java.io.ObjectInputStream in)
		throws IOException, ClassNotFoundException {
		this.uid= (String) in.readObject();
		this.runID= (long) in.readObject();
		this.ctime = (long) in.readObject();
		this.current_motion_speed_km_h_v = (double) in.readObject();
		this.current_motion_distance_km_v = (double) in.readObject();
		this.current_gps_speed_km_h = (double) in.readObject();
		this.current_gps_distance_km = (double) in.readObject();
		this.calories_v_distance = (double) in.readObject();
		this.calories_v_heart_beat = (double) in.readObject();
		this.current_heart_rate = (int) in.readObject();
		this.longitude = (double) in.readObject();
		this.latitude = (double) in.readObject();
		this.altitude = (double) in.readObject();
	}

		public void writeLog(String msg) {
		Date datenow = new Date();
		String date = date_format.format(datenow);
		Log.e(TAG, date + ": " + msg);
	}

}
