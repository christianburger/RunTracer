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

import com.runtracer.RunData;

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
	private static final String TAG = SqliteHandler.class.getSimpleName();
	private static final SimpleDateFormat date_format_mysql = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CANADA);
	private static final int DATABASE_VERSION = 2;
	private static final String DATABASE_NAME = "run_summary";

	private static final int MAX_AVAILABLE = 1;
	private static final Semaphore dbLock = new Semaphore(MAX_AVAILABLE, true);
/*
describe run_summary;
+---------------------+-------------+------+-----+---------+----------------+
| Field               | Type        | Null | Key | Default | Extra          |
+---------------------+-------------+------+-----+---------+----------------+
| uid                 | int(10)     | YES  |     | NULL    |                |
| runid               | int(10)     | NO   | PRI | NULL    | auto_increment |
| distance            | double      | YES  |     | NULL    |                |
| gps_distance        | double      | YES  |     | NULL    |                |
| average_speed       | double      | YES  |     | NULL    |                |
| calories_distance   | double      | YES  |     | NULL    |                |
| calories_heart_beat | double      | YES  |     | NULL    |                |
| current_weight      | double      | YES  |     | NULL    |                |
| current_fat         | double      | YES  |     | NULL    |                |
| runtrace            | blob        | YES  |     | NULL    |                |
| runtrace_md5sum     | varchar(80) | YES  |     | NULL    |                |
| date_start          | datetime    | YES  |     | NULL    |                |
| date_end            | datetime    | YES  |     | NULL    |                |
+---------------------+-------------+------+-----+---------+----------------+
 */

	private static final int NO_TABLES = 1;
	private static final int TABLE_RUN_SUMMARY = 0;

	private static HashMap<Integer, String> dbtables;
	public static HashMap<Integer, String>[] dbtablefields;
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
	public static final int field_runtrace = 9;
	public static final int field_runtrace_md5sum = 10;
	public static final int field_date_start = 11;
	public static final int field_date_end = 12;

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
		dbtabletypes[TABLE_RUN_SUMMARY].put(field_uid, "INTEGER PRIMARY KEY");

		dbtablefields[TABLE_RUN_SUMMARY].put(field_runid, "runid");
		dbtabletypes[TABLE_RUN_SUMMARY].put(field_runid, "INTEGER PRIMARY KEY");

		dbtablefields[TABLE_RUN_SUMMARY].put(field_distance, "distance");
		dbtabletypes[TABLE_RUN_SUMMARY].put(field_distance, "NUMERIC");

		dbtablefields[TABLE_RUN_SUMMARY].put(field_gps_distance, "gps_distance");
		dbtabletypes[TABLE_RUN_SUMMARY].put(field_gps_distance, "NUMERIC");

		dbtablefields[TABLE_RUN_SUMMARY].put(field_average_speed, "average_speed");
		dbtabletypes[TABLE_RUN_SUMMARY].put(field_average_speed, "NUMERIC");

		dbtablefields[TABLE_RUN_SUMMARY].put(field_calories_distance, "calories_distance");
		dbtabletypes[TABLE_RUN_SUMMARY].put(field_calories_distance, "NUMERIC");

		dbtablefields[TABLE_RUN_SUMMARY].put(field_calories_heart_beat, "calories_heart_beat");
		dbtabletypes[TABLE_RUN_SUMMARY].put(field_calories_heart_beat, "NUMERIC");

		dbtablefields[TABLE_RUN_SUMMARY].put(field_current_weight, "current_weight");
		dbtabletypes[TABLE_RUN_SUMMARY].put(field_current_weight, "NUMERIC");

		dbtablefields[TABLE_RUN_SUMMARY].put(field_current_fat, "current_fat");
		dbtabletypes[TABLE_RUN_SUMMARY].put(field_current_fat, "NUMERIC");

		dbtablefields[TABLE_RUN_SUMMARY].put(field_runtrace, "runtrace");
		dbtabletypes[TABLE_RUN_SUMMARY].put(field_runtrace, "BLOB");

		dbtablefields[TABLE_RUN_SUMMARY].put(field_runtrace_md5sum, "runtrace_md5sum");
		dbtabletypes[TABLE_RUN_SUMMARY].put(field_runtrace_md5sum, "TEXT");

		dbtablefields[TABLE_RUN_SUMMARY].put(field_date_start, "date_start");
		dbtabletypes[TABLE_RUN_SUMMARY].put(field_date_start, "NUMERIC");

		dbtablefields[TABLE_RUN_SUMMARY].put(field_date_end, "date_end");
		dbtabletypes[TABLE_RUN_SUMMARY].put(field_date_end, "NUMERIC");

		this.setCreated(true);
		dbLock.release();
		return true;
	}

	public SqliteHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.setCreated(false);
		writeLog(String.format(Locale.US, "SqliteHandle: Constructor: TAG: %s", TAG));
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

	/**
	 * Check if the database exist and can be read.
	 *
	 * @return true if it exists and can be read, false if it doesn't
	 */
	private synchronized boolean checkDataBase() {
		boolean result = false;
		try {
			dbLock.acquire();
			SQLiteDatabase checkDB;
			checkDB = SQLiteDatabase.openDatabase(TAG, null, SQLiteDatabase.OPEN_READONLY);
			checkDB.close();
			result = true;
		} catch (SQLiteException | InterruptedException e) {
			e.printStackTrace();
		}
		dbLock.release();
		return result;
	}

	public synchronized void addRunSummary(JSONObject runSummaryJSON) {
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

							case "INTEGER PRIMARY KEY":
								writeLog(String.format(Locale.US, "SqliteHandler: addRunSummary: value for key: %s is: %s", key, "INTEGER PRIMARY KEY"));
							case "INTEGER":
								writeLog(String.format(Locale.US, "SqliteHandler: addRunSummary: value for key: %s is: %s", key, "INTEGER"));
								if (runSummaryJSON.get(key) instanceof Long) {
									writeLog(String.format(Locale.US, "SqliteHandler: addRunSummary: Long: contentValues.put(%s, %s) ", key, runSummaryJSON.getString(key)));
									contentValues.put(key, runSummaryJSON.getLong(key));
								} else {
									if (runSummaryJSON.get(key) instanceof Integer) {
										writeLog(String.format(Locale.US, "SqliteHandler: addRunSummary: Integer: contentValues.put(%s, %s) ", key, runSummaryJSON.getString(key)));
										contentValues.put(key, runSummaryJSON.getInt(key));
									}
								}
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
								writeLog(String.format(Locale.US, "SqliteHandler: addRunSummary: key TYPE NOT found: %s", key));
						}
					} else {
						writeLog(String.format(Locale.US, "SqliteHandler: addRunSummary: key NOT found: %s", key));
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
	}

	public synchronized ArrayList<String> getAllRunSummary(Integer field) {
		try {
			dbLock.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		ArrayList<String> pricelist = new ArrayList<>();
		writeLog("SqliteHandler: getAllPrices....");
		if (this.isCreated() && dbtables != null && dbtablefields != null && dbtabletypes != null) {
			SQLiteDatabase db = this.getReadableDatabase();
			Cursor cursor = db.query(dbtables.get(TABLE_RUN_SUMMARY), null, null, null, null, null, null);
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				do {
					String name = cursor.getString(cursor.getColumnIndex(dbtablefields[TABLE_RUN_SUMMARY].get(field)));
					pricelist.add(name);
				} while (cursor.moveToNext());
			}
			assert cursor != null;
			cursor.close();
			db.close();
			writeLog(String.format(Locale.US, "SqliteHandler: getAllPrices: pricelist:%s", pricelist));
		}
		dbLock.release();
		return pricelist;
	}

	public synchronized ArrayList<RunData> getRunData(long runid) {
		try {
			dbLock.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		JSONObject priceJSON;
		ArrayList<RunData> runDataArrayList = new ArrayList<>();
		if (this.isCreated() && dbtables != null && dbtablefields != null && dbtabletypes != null) {
			try {
				SQLiteDatabase db = this.getReadableDatabase();
				String[] selections = {String.valueOf(runid)};
				String columns[] = {dbtablefields[TABLE_RUN_SUMMARY].get(field_runid), dbtablefields[TABLE_RUN_SUMMARY].get(field_date_start), dbtablefields[TABLE_RUN_SUMMARY].get(field_date_end), dbtablefields[TABLE_RUN_SUMMARY].get(field_calories_distance), dbtablefields[TABLE_RUN_SUMMARY].get(field_calories_heart_beat), dbtablefields[TABLE_RUN_SUMMARY].get(field_distance), dbtablefields[TABLE_RUN_SUMMARY].get(field_runtrace), dbtablefields[TABLE_RUN_SUMMARY].get(field_runtrace_md5sum), dbtablefields[TABLE_RUN_SUMMARY].get(field_gps_distance)};
				Cursor cursor;
				cursor = db.query(dbtables.get(TABLE_RUN_SUMMARY), columns, dbtablefields[TABLE_RUN_SUMMARY].get(field_runid) + "=" + selections[0], null, null, null, dbtablefields[TABLE_RUN_SUMMARY].get(field_runid));
				String sqlQry = SQLiteQueryBuilder.buildQueryString(false, dbtables.get(TABLE_RUN_SUMMARY), columns, dbtablefields[TABLE_RUN_SUMMARY].get(field_runid) + "=" + selections[0], null, null, dbtablefields[TABLE_RUN_SUMMARY].get(field_runid), null);
				writeLog(String.format(Locale.US, "SqliteHandler: getPriceInfo: buildQueryString: %s, cursor.getCount(): %d, cursor.getColumnCount: %d ", sqlQry, cursor.getCount(), cursor.getColumnCount()));

				priceJSON = new JSONObject("{\"key\":\"data\"}");
				for (int i = 0; i < cursor.getCount(); i++) {
					for (int j = 0; j < dbtablefields[TABLE_RUN_SUMMARY].size(); j++) {
						String column;
						Object value;
						column = dbtablefields[TABLE_RUN_SUMMARY].get(j);
						value = getCursorObject(cursor, j, i);
					}
					if (!priceJSON.isNull("runid")) {
						RunData check = new RunData();
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		dbLock.release();
		return runDataArrayList;
	}

	@Nullable
	private synchronized Object getCursorObject(Cursor cursor, int col, int row) {
		int coltype, currentRow;
		String coltypest = null, valuest = null;
		Object value = null;
		if (cursor.getCount() < row) {
			writeLog(String.format(Locale.US, "SqliteHandle: getCursorObject: No Data Available %d", cursor.getCount()));
			return null;
		} else {
			try {
				cursor.moveToFirst();
				for (currentRow = 0; currentRow < row && cursor.moveToNext(); currentRow++) {
					writeLog(String.format(Locale.US, "SqliteHandle: getCursorObject: currentRow: %d", currentRow));
				}
				if (!cursor.isAfterLast()) {
					coltype = cursor.getType(col);
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
		writeLog(String.format(Locale.US, "SqliteHandle: getCursorObject: getCount: %d \tcurrentRow: %d \tcolidx: %d,\t coltype: %s,\t colvalue: %s ", cursor.getCount(), currentRow, col, coltypest, valuest));
		return value;
	}

	private synchronized void printCursorInfo(Cursor cursor) {
		if (cursor.getCount() < 1) {
			writeLog(String.format(Locale.US, "SqliteHandle: printCursorInfo: No Data Available %d", cursor.getCount()));
		} else {
			try {
				cursor.moveToFirst();
				writeLog(String.format(Locale.US, "SqliteHandle: printCursorInfo: Data Available: cursor.getCount(): %d", cursor.getCount()));
				if (!cursor.isAfterLast()) {
					writeLog(String.format(Locale.US, "SqliteHandle: printCursorInfo: cursor.getColumnCount: %d cursor.getCount: %d", cursor.getColumnCount(), cursor.getCount()));
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
									valuest = String.valueOf(cursor.getInt(index));
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
							writeLog(String.format(Locale.US, "SqliteHandle: printCursorInfo: colname: %s ", colname));
							writeLog(String.format(Locale.US, "SqliteHandle: printCursorInfo: colidx: %d, %s ", index, colname));
							writeLog(String.format(Locale.US, "SqliteHandle: printCursorInfo: coltypest: %s ", coltypest));
							writeLog(String.format(Locale.US, "SqliteHandle: printCursorInfo: colvaluest: %s ", valuest));
						}
					}
				}
			} finally {
				cursor.moveToFirst();
			}
		}
	}

	@Override
	public synchronized SQLiteDatabase getReadableDatabase() {
		writeLog("SqliteHandle: getReadableDatabase");
		SQLiteDatabase db = super.getReadableDatabase();
		writeLog("SqliteHandle: getReadableDatabase: version: " + db.getVersion());
		return db;
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
