package com.runtracer.sqlitedb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.support.annotation.Nullable;
import android.text.format.DateFormat;
import android.util.Log;

import com.runtracer.model.RunData;
import com.runtracer.model.RunInstant;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.Semaphore;

public class SqliteHandler extends SQLiteOpenHelper {
	private static String dbPath;
	private static final SimpleDateFormat date_format_mysql = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CANADA);
	private static final int DATABASE_VERSION = 2;
	private static final String DATABASE_NAME = "sqlite_database";
	private static final int MAX_AVAILABLE = 1;
	private static final Semaphore dbLock = new Semaphore(MAX_AVAILABLE, true);

	private static final int NO_TABLES = 2;
	private static final int TABLE_RUN_SUMMARY = 0;
	private static final int TABLE_RUN_INSTANT = 1;

	private static HashMap<Integer, String> dbtables;
	private static HashMap<Integer, String>[] dbtablefields;
	private static HashMap<Integer, String>[] dbtabletypes;

	public static final int field_uid = 0;
	public static final int field_runid = 1;
	public static final int field_distance = 2;
	public static final int field_gps_distance = 3;
	public static final int field_average_speed = 4;
	public static final int field_calories_distance = 5;
	public static final int field_calories_heart_beat = 6;
	public static final int field_current_weight = 7;
	public static final int field_current_fat = 8;
	public static final int field_date_start = 9;
	public static final int field_date_end = 10;

	public static final int field_ctime = 2;
	public static final int field_current_motion_speed_km_h_v = 3;
	public static final int field_current_motion_distance_km_v = 4;
	public static final int field_current_gps_speed_km_h = 5;
	public static final int field_current_gps_distance_km = 6;
	public static final int field_calories_v_distance = 7;
	public static final int field_calories_v_heart_beat = 8;
	public static final int field_current_heart_rate = 9;
	public static final int field_longitude = 10;
	public static final int field_latitude = 11;
	public static final int field_altitude = 12;

	private boolean isCreated;

	private synchronized boolean createDBStructure() {
		writeLog("SqliteHandler: createDBStructure");

		try {
			dbLock.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		this.setCreated(false);
		dbtables = new HashMap<>();
		dbtablefields = new HashMap[NO_TABLES];
		dbtabletypes = new HashMap[NO_TABLES];

		for (int i = 0; i < NO_TABLES; i++) {
			dbtablefields[i] = new HashMap();
			dbtabletypes[i] = new HashMap();
		}

		dbtables.put(TABLE_RUN_SUMMARY, "run_summary");

		dbtablefields[TABLE_RUN_SUMMARY].put(field_uid, "uid");
		dbtabletypes[TABLE_RUN_SUMMARY].put(field_uid, "TEXT");

		dbtablefields[TABLE_RUN_SUMMARY].put(field_runid, "runid");
		dbtabletypes[TABLE_RUN_SUMMARY].put(field_runid, "INTEGER PRIMARY KEY NOT NULL");

		dbtablefields[TABLE_RUN_SUMMARY].put(field_distance, "distance");
		dbtabletypes[TABLE_RUN_SUMMARY].put(field_distance, "REAL");

		dbtablefields[TABLE_RUN_SUMMARY].put(field_gps_distance, "distance_gps");
		dbtabletypes[TABLE_RUN_SUMMARY].put(field_gps_distance, "REAL");

		dbtablefields[TABLE_RUN_SUMMARY].put(field_average_speed, "average_speed");
		dbtabletypes[TABLE_RUN_SUMMARY].put(field_average_speed, "REAL");

		dbtablefields[TABLE_RUN_SUMMARY].put(field_calories_distance, "calories_distance");
		dbtabletypes[TABLE_RUN_SUMMARY].put(field_calories_distance, "REAL");

		dbtablefields[TABLE_RUN_SUMMARY].put(field_calories_heart_beat, "calories_heart_beat");
		dbtabletypes[TABLE_RUN_SUMMARY].put(field_calories_heart_beat, "REAL");

		dbtablefields[TABLE_RUN_SUMMARY].put(field_current_weight, "current_weight");
		dbtabletypes[TABLE_RUN_SUMMARY].put(field_current_weight, "REAL");

		dbtablefields[TABLE_RUN_SUMMARY].put(field_current_fat, "current_fat");
		dbtabletypes[TABLE_RUN_SUMMARY].put(field_current_fat, "REAL");

		dbtablefields[TABLE_RUN_SUMMARY].put(field_date_start, "date_start");
		dbtabletypes[TABLE_RUN_SUMMARY].put(field_date_start, "NUMERIC");

		dbtablefields[TABLE_RUN_SUMMARY].put(field_date_end, "date_end");
		dbtabletypes[TABLE_RUN_SUMMARY].put(field_date_end, "NUMERIC");

		dbtables.put(TABLE_RUN_INSTANT, "run_instant");

		dbtablefields[TABLE_RUN_INSTANT].put(field_uid, "uid");
		dbtabletypes[TABLE_RUN_INSTANT].put(field_uid, "TEXT");

		dbtablefields[TABLE_RUN_INSTANT].put(field_runid, "runid");
		dbtabletypes[TABLE_RUN_INSTANT].put(field_runid, "INTEGER");

		dbtablefields[TABLE_RUN_INSTANT].put(field_ctime, "ctime");
		dbtabletypes[TABLE_RUN_INSTANT].put(field_ctime, "INTEGER");

		dbtablefields[TABLE_RUN_INSTANT].put(field_current_motion_speed_km_h_v, "motion_speed");
		dbtabletypes[TABLE_RUN_INSTANT].put(field_current_motion_speed_km_h_v, "REAL");

		dbtablefields[TABLE_RUN_INSTANT].put(field_current_motion_distance_km_v, "motion_distance");
		dbtabletypes[TABLE_RUN_INSTANT].put(field_current_motion_distance_km_v, "REAL");

		dbtablefields[TABLE_RUN_INSTANT].put(field_current_gps_speed_km_h, "gps_speed");
		dbtabletypes[TABLE_RUN_INSTANT].put(field_current_gps_speed_km_h, "REAL");

		dbtablefields[TABLE_RUN_INSTANT].put(field_current_gps_distance_km, "gps_distance");
		dbtabletypes[TABLE_RUN_INSTANT].put(field_current_gps_distance_km, "REAL");

		dbtablefields[TABLE_RUN_INSTANT].put(field_calories_v_distance, "calories_distance");
		dbtabletypes[TABLE_RUN_INSTANT].put(field_calories_v_distance, "REAL");

		dbtablefields[TABLE_RUN_INSTANT].put(field_calories_v_heart_beat, "calories_heart_beat");
		dbtabletypes[TABLE_RUN_INSTANT].put(field_calories_v_heart_beat, "REAL");

		dbtablefields[TABLE_RUN_INSTANT].put(field_current_heart_rate, "heart_rate");
		dbtabletypes[TABLE_RUN_INSTANT].put(field_current_heart_rate, "INTEGER");

		dbtablefields[TABLE_RUN_INSTANT].put(field_longitude, "longitude");
		dbtabletypes[TABLE_RUN_INSTANT].put(field_longitude, "REAL");

		dbtablefields[TABLE_RUN_INSTANT].put(field_latitude, "latitude");
		dbtabletypes[TABLE_RUN_INSTANT].put(field_latitude, "REAL");

		dbtablefields[TABLE_RUN_INSTANT].put(field_altitude, "altitude");
		dbtabletypes[TABLE_RUN_INSTANT].put(field_altitude, "REAL");

		this.setCreated(true);
		dbLock.release();
		return true;
	}

	public SqliteHandler(Context context, String path) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		dbPath = path;
		this.setCreated(false);
		writeLog(String.format(Locale.US, "SqliteHandle: Constructor: dbPath: %s", dbPath));
		this.createDBStructure();
	}

	@Override
	public synchronized void onCreate(SQLiteDatabase db) {
		writeLog("SqliteHandle: onCreate: dbLock OK: recreating database tables and all that.");
		for (int i = 0; this.isCreated() && i < dbtables.size(); i++) {
			String table_sql = String.format(Locale.US, "CREATE TABLE %s (", dbtables.get(i));
			for (int j = 0; j < dbtablefields[i].size(); j++) {
				table_sql += String.format(Locale.US, "%s %s ", dbtablefields[i].get(j), dbtabletypes[i].get(j));
				if (j < (dbtablefields[i].size() - 1)) {
					table_sql += ", ";
				}
			}
			table_sql += ")";
			writeLog("SqliteHandle: onCreate: table_sql: " + table_sql);
			db.execSQL(table_sql);
		}
	}

	@Override
	public synchronized void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		writeLog("SqliteHandle: onUpgrade");
		try {
			dbLock.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		for (int i = 0; i < dbtables.size(); i++) {
			db.execSQL("DROP TABLE IF EXISTS " + dbtables.get(i));
		}
		dbLock.release();
		// Create tables again
		onCreate(db);
	}


	@Override
	public synchronized SQLiteDatabase getReadableDatabase() {
		writeLog("SqliteHandle: getReadableDatabase");
		SQLiteDatabase db = super.getReadableDatabase();
		writeLog("SqliteHandle: getReadableDatabase: version: " + db.getVersion());
		return db;
	}

	/**
	 * Check if the database exist and can be read.
	 * @return true if it exists and can be read, false if it doesn't
	 */
	public synchronized boolean checkDataBase() {
		boolean result = false;
		writeLog(String.format(Locale.CANADA, "SqliteHandle: checkDataBase: dbPath: %s", dbPath));
		try {
			dbLock.acquire();
			SQLiteDatabase checkDB;
			checkDB = this.getReadableDatabase();
			writeLog(String.format(Locale.CANADA, "SqliteHandle: checkDataBase: path found: %s", checkDB.getPath()));
			writeLog(String.format(Locale.CANADA, "SqliteHandle: checkDataBase: version found: %s", checkDB.getVersion()));
			writeLog(String.format(Locale.CANADA, "SqliteHandle: checkDataBase: maximum size: %s", checkDB.getMaximumSize()));
			checkDB.close();
			result = true;
		} catch (SQLiteException | InterruptedException e) {
			writeLog(String.format(Locale.CANADA, "SqliteHandle: checkDataBase: EXCEPTION: %s", e.toString()));
			e.printStackTrace();
		} finally {
			dbLock.release();
		}
		writeLog(String.format(Locale.CANADA, "SqliteHandle: checkDataBase: result: %s", result));
		return result;
	}

	public synchronized void addRunSummary(JSONObject runSummaryJSON) {
		writeLog(String.format(Locale.CANADA, "addRunSummary: %s", runSummaryJSON));
		try {
			dbLock.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (this.isCreated() && dbtables != null && dbtablefields != null && dbtabletypes != null) {
			SQLiteDatabase db = this.getWritableDatabase();
			ContentValues contentValues = new ContentValues();
			try {
				for (int i = 0; i < dbtablefields[TABLE_RUN_SUMMARY].size(); i++) {
					String key = dbtablefields[TABLE_RUN_SUMMARY].get(i);
					writeLog(String.format(Locale.US, "SqliteHandler: addRunSummary: searching for key: %s: %b", key, !(runSummaryJSON.isNull(key))));
					if (!(runSummaryJSON.isNull(key))) {
						switch (dbtabletypes[TABLE_RUN_SUMMARY].get(i)) {
							case "TEXT":
								writeLog(String.format(Locale.US, "SqliteHandler: addRunSummary: TEXT: runSummaryJSON.get(key): %s instanceof String: %s", runSummaryJSON.get(key), runSummaryJSON.get(key) instanceof String));
								if (runSummaryJSON.get(key) instanceof String) {
									writeLog(String.format(Locale.US, "SqliteHandler: addRunSummary: TEXT: contentValues.put(%s, %s) ", key, runSummaryJSON.getString(key)));
									contentValues.put(key, runSummaryJSON.getString(key));
								} else {
									writeLog(String.format(Locale.US, "SqliteHandler: addRunSummary: TEXT: FORCING TO: contentValues.put(%s, %s) ", key, runSummaryJSON.get(key).toString()));
									contentValues.put(key, String.valueOf(runSummaryJSON.get(key)));
								}
								break;

							case "NUMERIC":
								writeLog(String.format(Locale.US, "SqliteHandler: addRunSummary: value for key: %s is: %s", key, "NUMERIC"));
								if (runSummaryJSON.get(key) instanceof Date) {
									contentValues.put(key, date_format_mysql.format((Date) runSummaryJSON.get(key)));
								} else {
									if (runSummaryJSON.get(key) instanceof String) {
										Date date = date_format_mysql.parse(runSummaryJSON.getString(key));
										contentValues.put(key, date_format_mysql.format(date));
									}
								}
								break;

							case "INTEGER PRIMARY KEY NOT NULL":
							case "INTEGER PRIMARY KEY":
							case "INTEGER PRIMARY":
							case "INTEGER KEY":
							case "INTEGER":
								writeLog(String.format(Locale.US, "SqliteHandler: addRunSummary: value for key: %s is: %s", key, "INTEGER"));
								if (runSummaryJSON.get(key) instanceof Long) {
									writeLog(String.format(Locale.US, "SqliteHandler: addRunSummary: Long: contentValues.put(%s, %s) ", key, runSummaryJSON.getString(key)));
									contentValues.put(key, runSummaryJSON.getLong(key));
								} else {
									if (runSummaryJSON.get(key) instanceof Integer) {
										writeLog(String.format(Locale.US, "SqliteHandler: addRunSummary: Integer: contentValues.put(%s, %s) ", key, runSummaryJSON.getString(key)));
										contentValues.put(key, runSummaryJSON.getInt(key));
									} else {
										if (runSummaryJSON.get(key) instanceof String) {
											writeLog(String.format(Locale.US, "SqliteHandler: addRunSummary: String: contentValues.put(%s, Long.parseLong(%s)==%d) ", key, runSummaryJSON.getString(key), Long.parseLong(runSummaryJSON.getString(key))));
											contentValues.put(key, Long.parseLong(runSummaryJSON.getString(key)));
										}
									}
								}
								break;

							case "REAL":
								writeLog(String.format(Locale.US, "SqliteHandler: addRunSummary: value for key: %s is: %s", key, "REAL"));
								if (runSummaryJSON.get(key) instanceof Double) {
									writeLog(String.format(Locale.US, "SqliteHandler: addRunSummary: Double: contentValues.put(%s, %s) ", key, runSummaryJSON.getString(key)));
									contentValues.put(key, runSummaryJSON.getDouble(key));
								} else {
									if (runSummaryJSON.get(key) instanceof Float) {
										writeLog(String.format(Locale.US, "SqliteHandler: addRunSummary: Float: contentValues.put(%s, %s) ", key, runSummaryJSON.getString(key)));
										contentValues.put(key, (Float) runSummaryJSON.get(key));
									} else {
										if (runSummaryJSON.get(key) instanceof String) {
											writeLog(String.format(Locale.US, "SqliteHandler: addRunSummary: String: contentValues.put(%s, %s) ", key, runSummaryJSON.getString(key)));
											contentValues.put(key, Double.parseDouble(runSummaryJSON.getString(key)));
										}
									}
								}
								break;

							case "BLOB":
								if (runSummaryJSON.get(key) instanceof Double) {
									writeLog(String.format(Locale.US, "SqliteHandler: addRunSummary: BLOB: contentValues.put(%s, %s) ", key, runSummaryJSON.getString(key)));
									contentValues.put(key, runSummaryJSON.getString(key));
								}
								break;

							default:
								writeLog(String.format(Locale.US, "SqliteHandler: addRunSummary: key TYPE: %s << not found for key: %s", dbtabletypes[TABLE_RUN_SUMMARY].get(i), key));
						}
					} else {
						writeLog(String.format(Locale.US, "SqliteHandler: addRunSummary: key: %s << not found in: %s", key, runSummaryJSON));
					}
				}
			} catch (JSONException | ParseException e) {
				e.printStackTrace();
				db.close();
			}
			long id = db.insert(dbtables.get(TABLE_RUN_SUMMARY), null, contentValues);
			db.close(); // Closing database connection
		}
		dbLock.release();
		writeLog(String.format(Locale.US, "SqliteHandler: addRunSummary: release db:dbLock.hasQueuedThreads(): %s", dbLock.hasQueuedThreads()));
	}

	public synchronized void addRunInstant(JSONObject runInstantJSON) {
		writeLog(String.format(Locale.CANADA, "addInstant: %s", runInstantJSON));
		try {
			dbLock.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (this.isCreated() && dbtables != null && dbtablefields != null && dbtabletypes != null) {
			SQLiteDatabase db = this.getWritableDatabase();
			ContentValues contentValues = new ContentValues();
			try {
				for (int i = 0; i < dbtablefields[TABLE_RUN_INSTANT].size(); i++) {
					String key = dbtablefields[TABLE_RUN_INSTANT].get(i);
					writeLog(String.format(Locale.US, "SqliteHandler: addRunInstant: searching for key: %s: %b", key, !(runInstantJSON.isNull(key))));
					if (!(runInstantJSON.isNull(key))) {
						switch (dbtabletypes[TABLE_RUN_INSTANT].get(i)) {
							case "TEXT":
								writeLog(String.format(Locale.US, "SqliteHandler: addRunInstant: TEXT: runInstantJSON.get(key): %s instanceof String: %s", runInstantJSON.get(key), runInstantJSON.get(key) instanceof String));
								if (runInstantJSON.get(key) instanceof String) {
									writeLog(String.format(Locale.US, "SqliteHandler: addRunInstant: TEXT: contentValues.put(%s, %s) ", key, runInstantJSON.getString(key)));
									contentValues.put(key, runInstantJSON.getString(key));
								} else {
									writeLog(String.format(Locale.US, "SqliteHandler: addRunInstant: TEXT: FORCING TO: contentValues.put(%s, %s) ", key, runInstantJSON.get(key).toString()));
									contentValues.put(key, String.valueOf(runInstantJSON.get(key)));
								}
								break;

							case "NUMERIC":
								writeLog(String.format(Locale.US, "SqliteHandler: addRunInstant: value for key: %s is: %s", key, "NUMERIC"));
								if (runInstantJSON.get(key) instanceof Date) {
									contentValues.put(key, date_format_mysql.format((Date) runInstantJSON.get(key)));
								} else {
									if (runInstantJSON.get(key) instanceof String) {
										Date date = date_format_mysql.parse(runInstantJSON.getString(key));
										contentValues.put(key, date_format_mysql.format(date));
									}
								}
								break;

							case "INTEGER PRIMARY KEY NOT NULL":
							case "INTEGER PRIMARY KEY":
							case "INTEGER PRIMARY":
							case "INTEGER KEY":
							case "INTEGER":
								writeLog(String.format(Locale.US, "SqliteHandler: addRunInstant: case INTEGER: >> value for key: %s is: %s", key, dbtabletypes[TABLE_RUN_INSTANT].get(i)));
								if (runInstantJSON.get(key) instanceof Long) {
									writeLog(String.format(Locale.US, "SqliteHandler: addRunInstant: Long: contentValues.put(%s, %s) ", key, runInstantJSON.getString(key)));
									contentValues.put(key, runInstantJSON.getLong(key));
								} else {
									if (runInstantJSON.get(key) instanceof Integer) {
										writeLog(String.format(Locale.US, "SqliteHandler: addRunInstant: Integer: contentValues.put(%s, %s) ", key, runInstantJSON.getString(key)));
										contentValues.put(key, runInstantJSON.getInt(key));
									} else {
										if (runInstantJSON.get(key) instanceof String) {
											writeLog(String.format(Locale.US, "SqliteHandler: addRunInstant: String: contentValues.put(%s, Long.parseLong(%s)==%d) ", key, runInstantJSON.getString(key), Long.parseLong(runInstantJSON.getString(key))));
											contentValues.put(key, Long.parseLong(runInstantJSON.getString(key)));
										}
									}
								}
								break;

							case "REAL":
								writeLog(String.format(Locale.US, "SqliteHandler: addRunInstant: value for key: %s is: %s", key, "REAL"));
								if (runInstantJSON.get(key) instanceof Double) {
									writeLog(String.format(Locale.US, "SqliteHandler: addRunInstant: Double: contentValues.put(%s, %s) ", key, runInstantJSON.getString(key)));
									contentValues.put(key, runInstantJSON.getDouble(key));
								} else {
									if (runInstantJSON.get(key) instanceof Float) {
										writeLog(String.format(Locale.US, "SqliteHandler: addRunInstant: Float: contentValues.put(%s, %s) ", key, runInstantJSON.getString(key)));
										contentValues.put(key, (Float) runInstantJSON.get(key));
									} else {
										if (runInstantJSON.get(key) instanceof String) {
											writeLog(String.format(Locale.US, "SqliteHandler: addRunInstant: String: contentValues.put(%s, %s) ", key, runInstantJSON.getString(key)));
											contentValues.put(key, Double.parseDouble(runInstantJSON.getString(key)));
										}
									}
								}
								break;

							case "BLOB":
								if (runInstantJSON.get(key) instanceof Double) {
									writeLog(String.format(Locale.US, "SqliteHandler: addRunInstant: BLOB: contentValues.put(%s, %s) ", key, runInstantJSON.getString(key)));
									contentValues.put(key, runInstantJSON.getString(key));
								}
								break;

							default:
								writeLog(String.format(Locale.US, "SqliteHandler: addRunInstant: key TYPE NOT found: %s", key));
						}
					} else {
						writeLog(String.format(Locale.US, "SqliteHandler: addRunInstant: key NOT found: %s", key));
					}
				}
			} catch (JSONException | ParseException e) {
				e.printStackTrace();
				db.close();
			}
			long id = db.insert(dbtables.get(TABLE_RUN_INSTANT), null, contentValues);
			db.close(); // Closing database connection
		}
		dbLock.release();
		writeLog(String.format(Locale.US, "SqliteHandler: addRunInstant: release db:dbLock.hasQueuedThreads(): %s", dbLock.hasQueuedThreads()));
	}

	public synchronized ArrayList<String> getAllRunSummaries(Integer field) {
		try {
			dbLock.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		ArrayList<String> run_summary_list = new ArrayList<>();
		writeLog("SqliteHandler: getAllRunSummaries for field no: " + field + " name:" + dbtablefields[TABLE_RUN_SUMMARY].get(field));
		if (this.isCreated() && dbtables != null && dbtablefields != null && dbtabletypes != null) {
			SQLiteDatabase db = this.getReadableDatabase();
			Cursor cursor = db.query(dbtables.get(TABLE_RUN_SUMMARY), null, null, null, null, null, null);
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				do {
					String name = cursor.getString(cursor.getColumnIndex(dbtablefields[TABLE_RUN_SUMMARY].get(field)));
					run_summary_list.add(name);
				} while (cursor.moveToNext());
			}
			assert cursor != null;
			cursor.close();
			db.close();
			writeLog(String.format(Locale.US, "SqliteHandler: getAllRunSummaries: %s", run_summary_list));
		}
		dbLock.release();
		return run_summary_list;
	}

	public synchronized ArrayList<String> getAllRunInstants(Integer field) {
		try {
			dbLock.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		ArrayList<String> run_instant_list = new ArrayList<>();
		writeLog("SqliteHandler: getAllRunInstants for field no: " + field + " name:" + dbtablefields[TABLE_RUN_INSTANT].get(field));
		if (this.isCreated() && dbtables != null && dbtablefields != null && dbtabletypes != null) {
			SQLiteDatabase db = this.getReadableDatabase();
			Cursor cursor = db.query(dbtables.get(TABLE_RUN_INSTANT), null, null, null, null, null, null);
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				do {
					String name = cursor.getString(cursor.getColumnIndex(dbtablefields[TABLE_RUN_INSTANT].get(field)));
					run_instant_list.add(name);
				} while (cursor.moveToNext());
			}
			assert cursor != null;
			cursor.close();
			db.close();
			writeLog(String.format(Locale.US, "SqliteHandler: getAllRunInstants: %s", run_instant_list));
		}
		dbLock.release();
		return run_instant_list;
	}

	public synchronized int getNoRunSummaries() {
		int no_runs = 0;
		try {
			dbLock.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		ArrayList<String> run_summary_list = new ArrayList<>();
		writeLog("SqliteHandler: getNoRunSummaries....");
		if (this.isCreated() && dbtables != null && dbtablefields != null && dbtabletypes != null) {
			SQLiteDatabase db = this.getReadableDatabase();
			Cursor cursor = db.query(dbtables.get(TABLE_RUN_SUMMARY), null, null, null, null, null, null);
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				do {
					String name = cursor.getString(cursor.getColumnIndex(dbtablefields[TABLE_RUN_SUMMARY].get(field_date_start)));
					run_summary_list.add(name);
					no_runs++;
				} while (cursor.moveToNext());
			}
			assert cursor != null;
			cursor.close();
			db.close();
			writeLog(String.format(Locale.US, "SqliteHandler: getNoRunSummaries: %d >> run_summary_list: { %s }", no_runs, run_summary_list));
		}
		dbLock.release();
		return no_runs;
	}

	public synchronized RunData getRunData(long runid) {
		RunData check = new RunData();
		try {
			dbLock.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		JSONObject rundataJSON;
		if (this.isCreated() && dbtables != null && dbtablefields != null && dbtabletypes != null) {
			try {
				SQLiteDatabase db = this.getReadableDatabase();
				String[] selections = {String.valueOf(runid)};
				String columns[] = {
					dbtablefields[TABLE_RUN_SUMMARY].get(field_uid),
					dbtablefields[TABLE_RUN_SUMMARY].get(field_runid),
					dbtablefields[TABLE_RUN_SUMMARY].get(field_distance),
					dbtablefields[TABLE_RUN_SUMMARY].get(field_gps_distance),
					dbtablefields[TABLE_RUN_SUMMARY].get(field_average_speed),
					dbtablefields[TABLE_RUN_SUMMARY].get(field_calories_distance),
					dbtablefields[TABLE_RUN_SUMMARY].get(field_calories_heart_beat),
					dbtablefields[TABLE_RUN_SUMMARY].get(field_current_weight),
					dbtablefields[TABLE_RUN_SUMMARY].get(field_current_fat),
					dbtablefields[TABLE_RUN_SUMMARY].get(field_date_start),
					dbtablefields[TABLE_RUN_SUMMARY].get(field_date_end)

				};
				Cursor cursor;
				cursor = db.query(dbtables.get(TABLE_RUN_SUMMARY), columns, dbtablefields[TABLE_RUN_SUMMARY].get(field_runid) + "=" + selections[0], null, null, null, dbtablefields[TABLE_RUN_SUMMARY].get(field_runid));
				String sqlQry = SQLiteQueryBuilder.buildQueryString(false, dbtables.get(TABLE_RUN_SUMMARY), columns, dbtablefields[TABLE_RUN_SUMMARY].get(field_runid) + "=" + selections[0], null, null, dbtablefields[TABLE_RUN_SUMMARY].get(field_runid), null);
				writeLog(String.format(Locale.US, "SqliteHandler: getRunData: buildQueryString: %s, cursor.getCount(): %d, cursor.getColumnCount: %d ", sqlQry, cursor.getCount(), cursor.getColumnCount()));
				rundataJSON = new JSONObject("{\"key\":\"data\"}");
				for (int i = 0; i < cursor.getCount(); i++) {
					for (int j = 0; j < dbtablefields[TABLE_RUN_SUMMARY].size(); j++) {
						String column;
						Object value;
						column = dbtablefields[TABLE_RUN_SUMMARY].get(j);
						value = getCursorObject(cursor, j, i);
						rundataJSON.put(column, value);
					}
					if (!rundataJSON.isNull("runid")) {
						check = check.fromJSON(rundataJSON);
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		dbLock.release();
		return check;
	}

	public synchronized ArrayList<RunInstant> getRunInstants(long runid) {
		writeLog("SqliteHandler: getRunInstants for runid: " + runid);
		ArrayList<RunInstant> runinstants = new ArrayList<>();
		try {
			dbLock.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		JSONObject runInstantJSON;
		if (this.isCreated() && dbtables != null && dbtablefields != null && dbtabletypes != null) {
			try {
				SQLiteDatabase db = this.getReadableDatabase();
				String[] selections = {String.valueOf(runid)};
				String columns[] = {
					dbtablefields[TABLE_RUN_INSTANT].get(field_uid),
					dbtablefields[TABLE_RUN_INSTANT].get(field_runid),
					dbtablefields[TABLE_RUN_INSTANT].get(field_ctime),
					dbtablefields[TABLE_RUN_INSTANT].get(field_current_motion_speed_km_h_v),
					dbtablefields[TABLE_RUN_INSTANT].get(field_current_motion_distance_km_v),
					dbtablefields[TABLE_RUN_INSTANT].get(field_current_gps_speed_km_h),
					dbtablefields[TABLE_RUN_INSTANT].get(field_current_gps_distance_km),
					dbtablefields[TABLE_RUN_INSTANT].get(field_calories_v_distance),
					dbtablefields[TABLE_RUN_INSTANT].get(field_calories_v_heart_beat),
					dbtablefields[TABLE_RUN_INSTANT].get(field_current_heart_rate),
					dbtablefields[TABLE_RUN_INSTANT].get(field_longitude),
					dbtablefields[TABLE_RUN_INSTANT].get(field_latitude),
					dbtablefields[TABLE_RUN_INSTANT].get(field_altitude)
				};
				Cursor cursor;
				cursor = db.query(dbtables.get(TABLE_RUN_INSTANT), columns, dbtablefields[TABLE_RUN_INSTANT].get(field_runid) + "=" + selections[0], null, null, null, dbtablefields[TABLE_RUN_INSTANT].get(field_ctime));
				String sqlQry = SQLiteQueryBuilder.buildQueryString(false, dbtables.get(TABLE_RUN_INSTANT), columns, dbtablefields[TABLE_RUN_INSTANT].get(field_runid) + "=" + selections[0], null, null, dbtablefields[TABLE_RUN_INSTANT].get(field_ctime), null);
				writeLog(String.format(Locale.US, "SqliteHandler: getRunInstants: buildQueryString: %s, cursor.getCount(): %d, cursor.getColumnCount: %d ", sqlQry, cursor.getCount(), cursor.getColumnCount()));
				runInstantJSON = new JSONObject("{\"key\":\"data\"}");
				writeLog(String.format(Locale.US, "SqliteHandler: getRunInstants: DEBUGGING cursor.getColumnName(%d): %s", field_ctime, cursor.getColumnName(field_ctime)));
				for (String colname:columns) {
					writeLog(String.format(Locale.US, "SqliteHandler: getRunInstants: DEBUGGING: colname: %s", colname));
				}
				for (int i = 0; i < cursor.getCount(); i++) {
					writeLog(String.format(Locale.CANADA, "SqliteHandler: getRunInstants: i: %d cursor.getCount(): %d", i, cursor.getCount()));
					for (int j = 0; j < dbtablefields[TABLE_RUN_INSTANT].size(); j++) {
						String column;
						Object value;
						column = dbtablefields[TABLE_RUN_INSTANT].get(j);
						value = getCursorObject(cursor, j, i);
						runInstantJSON.put(column, value);
						writeLog(String.format(Locale.CANADA, "SqliteHandler: getRunInstants: column: %s, value: %s, value.getClass: %s", column, value!=null?value.toString():"null", value!=null?value.getClass():"OBJECT IS NULL" ));
					}
					if (!runInstantJSON.isNull("runid")) {
						writeLog("SqliteHandler: getRunInstants: runInstantJSON: " + runInstantJSON);
						RunInstant check = new RunInstant();
						check = check.fromJSON(runInstantJSON);
						runinstants.add(check);
						writeLog("SqliteHandler: getRunInstants adding: run_id: " + check.getRunID() + " ctime: " + check.getCtime() + " runinstants.size(): " + runinstants.size());
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		dbLock.release();
		writeLog("SqliteHandler: getRunInstants runinstants: " + runinstants);
		return runinstants;
	}

	@Nullable
	private synchronized Object getCursorObject(Cursor cursor, int col, int row) {
		int coltype, currentRow;
		String coltypest = null, valuest = null;
		Object value = null;
		if (cursor.getCount() < row) {
			//writeLog(String.format(Locale.US, "SqliteHandler: getCursorObject: No Data Available %d", cursor.getCount()));
			return null;
		} else {
			try {
				cursor.moveToFirst();
				for (currentRow = 0; currentRow < row && cursor.moveToNext();) {
					currentRow++;
				}
				if (!cursor.isAfterLast()) {
					coltype = cursor.getType(col);
					//writeLog(String.format(Locale.US, "SqliteHandler: getCursorObject: col: %d, colName: %s,  coltype: %s, row: %d", col, cursor.getColumnName(col), coltype, row));
					switch (coltype) {

						case Cursor.FIELD_TYPE_BLOB:
							coltypest = "FIELD_TYPE_BLOB";
							valuest = "BLOB";
							value = cursor.getBlob(col);
							break;

						case Cursor.FIELD_TYPE_FLOAT:
							coltypest = "FIELD_TYPE_FLOAT";
							valuest = String.valueOf(cursor.getFloat(col));
							value = cursor.getFloat(col);
							break;

						case Cursor.FIELD_TYPE_INTEGER:
							coltypest = "FIELD_TYPE_INTEGER";
							valuest = String.valueOf(cursor.getLong(col));
							value = cursor.getLong(col);
							break;

						case Cursor.FIELD_TYPE_STRING:
							coltypest = "FIELD_TYPE_STRING";
							valuest = cursor.getString(col);
							value = cursor.getString(col);
							break;

						case Cursor.FIELD_TYPE_NULL:
							coltypest = "FIELD_TYPE_NULL";
							valuest = "null";
							value = null;
							break;

						default:
							valuest = "NOT_FOUND";
							coltypest = "NOT_FOUND";
							value = null;
					}
				}
			} finally {
				cursor.moveToFirst();
			}
		}
		//writeLog(String.format(Locale.US, "SqliteHandler: getCursorObject: getCount: %d \tcurrentRow: %d \tcolidx: %d,\t coltype: %s,\t colvalue: %s ", cursor.getCount(), currentRow, col, coltypest, valuest));
		return value;
	}

	private synchronized void printCursorInfo(Cursor cursor) {
		if (cursor.getCount() < 1) {
			writeLog(String.format(Locale.US, "SqliteHandler: printCursorInfo: No Data Available %d", cursor.getCount()));
		} else {
			try {
				cursor.moveToFirst();
				writeLog(String.format(Locale.US, "SqliteHandler: printCursorInfo: Data Available: cursor.getCount(): %d", cursor.getCount()));
				if (!cursor.isAfterLast()) {
					writeLog(String.format(Locale.US, "SqliteHandler: printCursorInfo: cursor.getColumnCount: %d cursor.getCount: %d", cursor.getColumnCount(), cursor.getCount()));
					for (String colname : cursor.getColumnNames()) {
						int index, coltype;
						String coltypest = "", valuest = "";
						index = cursor.getColumnIndex(colname);
						if (index >= 0 && !cursor.isNull(index)) {
							coltype = cursor.getType(index);
							switch (coltype) {

								case Cursor.FIELD_TYPE_BLOB:
									coltypest = "FIELD_TYPE_BLOB";
									valuest = "BLOB";
									break;

								case Cursor.FIELD_TYPE_FLOAT:
									coltypest = "FIELD_TYPE_FLOAT";
									valuest = String.valueOf(cursor.getFloat(index));
									break;

								case Cursor.FIELD_TYPE_INTEGER:
									coltypest = "FIELD_TYPE_INTEGER";
									valuest = String.format(Locale.CANADA, "%d", cursor.getInt(index));
									break;

								case Cursor.FIELD_TYPE_STRING:
									coltypest = "FIELD_TYPE_STRING";
									valuest = cursor.getString(index);
									break;

								case Cursor.FIELD_TYPE_NULL:
									coltypest = "FIELD_TYPE_NULL";
									valuest = "null";
									break;

								default:
									valuest = "NOT_FOUND";
									coltypest = "NOT_FOUND";
							}
							writeLog("\n");
							writeLog(String.format(Locale.US, "SqliteHandler: printCursorInfo: colidx: %d, %s", index, colname));
							writeLog(String.format(Locale.US, "SqliteHandler: printCursorInfo: coltypest: %s", coltypest));
							writeLog(String.format(Locale.US, "SqliteHandler: printCursorInfo: colvaluest: %s", valuest));
							writeLog("\n");
						}
					}
				}
			} finally {
				cursor.moveToFirst();
			}
		}
	}

	private synchronized boolean isCreated() {
		return isCreated;
	}

	private synchronized void setCreated(boolean created) {
		isCreated = created;
	}

	public void writeLog(String msg) {
		Date cdate;
		String date = (DateFormat.format("dd-MM-yyyy hh:mm:ss a", cdate = new Date()).toString());
		String msg2 = String.format(Locale.US, "<%d>", cdate.getTime());
		String TAG = "sqlite";
		Log.e(TAG, date + msg2 + ": " + msg);
	}

}
