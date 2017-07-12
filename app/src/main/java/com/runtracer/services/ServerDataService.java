package com.runtracer.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.text.format.DateFormat;
import android.util.Base64;
import android.util.Log;

import com.runtracer.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import static android.os.Process.myTid;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in a service on a separate handler thread.
 * TODO: Customize class - update intent actions, extra parameters and static helper methods.
 */
public class ServerDataService extends IntentService {

	protected static final String ACTION_GET_OAUTH2_TOKEN = "com.runtracer.oauth2_token";
	public static final String ACTION_QUERY_SERVER = "com.runtracer.query_server";
	protected static final String ACTION_REPORT_STATUS = "com.runtracer.report_status";
	protected static final String PARAM_OUT_MSG = "param_out_msg";

	private static final String EXTRA_PARAM1 = "com.runtracer.extra.PARAM1";
	private static final String EXTRA_PARAM2 = "com.runtracer.extra.PARAM2";

	private static final String TAG = "runtracer";
	private static final int NETWORK_TIMEOUT = 8000;

	private static DataBaseExchange localDbExchange;

	private SimpleOAuth2Token simpleOAuth2Token;
	private int attempts;

	public ServerDataService() {
		super("ServerDataService");
		simpleOAuth2Token = new SimpleOAuth2Token("", new Date());
		localDbExchange = DataBaseExchange.createDataBaseExchange();
		simpleOAuth2Token.setToken("Fgasdjv|@#68961834bdjkagksfjhas");
		writeLog("\n\n\n\n\n\n++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\nServerDataService: starting ServerDataService...");
	}

	public void writeLog(String msg) {
		Date cdate;
		String date = (DateFormat.format("dd-MM-yyyy hh:mm:ss a", cdate = new Date()).toString());
		int threadID;
		threadID = Process.getThreadPriority(myTid());
		String msg2 = String.format(Locale.US, "<%d> thread id: %d \t>> ", cdate.getTime(), threadID);
		Log.e(TAG, date + ": " + msg2 + ": \t" + msg);
	}

	/**
	 * Starts this service to perform action QueryServer with the given parameters.
	 * If the service is already performing a task this action will be queued.
	 *
	 * @see IntentService
	 */
	public static void startActionQueryServer(Context context, String param1, String param2) {
		Intent intent = new Intent(context, ServerDataService.class);
		intent.setAction(ACTION_QUERY_SERVER);
		intent.putExtra(EXTRA_PARAM1, param1);
		intent.putExtra(EXTRA_PARAM2, param2);
		context.startService(intent);
	}

	/**
	 * Starts this service to perform action ReportStatus with the given parameters.
	 * If the service is already performing a task this action will be queued.
	 *
	 * @see IntentService
	 */
	public static void startActionReportStatus(Context context, String param1, String param2) {
		Intent intent = new Intent(context, ServerDataService.class);
		intent.setAction(ACTION_REPORT_STATUS);
		intent.putExtra(EXTRA_PARAM1, param1);
		intent.putExtra(EXTRA_PARAM2, param2);
		context.startService(intent);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		String hash;
		if (intent != null) {
			try {
				final String action = intent.getAction();
				hash = intent.getStringExtra("hash");
				writeLog(String.format("ServerDataService: localDbExchange received action: %s", action));
				if (ACTION_QUERY_SERVER.equals(action)) {
					writeLog(String.format("ServerDataService: localDbExchange received hash: %s", hash));
					writeLog(String.format("ServerDataService: localDbExchange getHash(): %s", localDbExchange.getHash()));
					writeLog(String.format("ServerDataService: localDbExchange getHash(): %s", localDbExchange.getHash()));
					writeLog(String.format("ServerDataService: MainActivity.dbExchange.getHash(): %s", MainActivity.dbExchange.getHash()));
					writeLog(String.format("ServerDataService: MainActivity.dbExchange.getHash(): %s", MainActivity.dbExchange.getHash()));
					if (MainActivity.dbExchange != null && (MainActivity.dbExchange.getHash().compareTo(hash) == 0)) {
						writeLog(String.format("ServerDataService: localDbExchange received hash: %s MATCHED", hash));
						MainActivity.available.acquire();
						localDbExchange.clear();
						localDbExchange = (DataBaseExchange) MainActivity.dbExchange.clone();
						MainActivity.available.release();
						localDbExchange.setJson_data_out(new JSONObject("{\"key\":\"data\"}"));
						writeLog(String.format("ServerDataService: localDbExchange received full_name: %s", localDbExchange.getFull_name()));
						writeLog(String.format("ServerDataService: localDbExchange received accountEmail: %s", localDbExchange.getAccountEmail()));
						writeLog(String.format("ServerDataService: localDbExchange received command: %s", localDbExchange.getCommand()));
						writeLog(String.format("ServerDataService: localDbExchange received url: %s", localDbExchange.getUrl()));
						writeLog(String.format("ServerDataService: localDbExchange received: error: %s", localDbExchange.getError_no()));
						writeLog(String.format("ServerDataService: localDbExchange received: : json_in: %s", localDbExchange.getJson_data_in()));
						writeLog(String.format("ServerDataService: localDbExchange received: : json_out: %s", localDbExchange.getJson_data_out()));
						doExchange(localDbExchange);
						handleActionQueryServer(hash);
					}
				} else {
					if (ACTION_REPORT_STATUS.equals(action)) {
						final String param1 = intent.getStringExtra(EXTRA_PARAM1);
						final String param2 = intent.getStringExtra(EXTRA_PARAM2);
						handleActionReportStatus(param1, param2);
					}
				}
			} catch (InterruptedException | JSONException | CloneNotSupportedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Handle action QueryServer in the provided background thread with the provided parameters.
	 */
	private void handleActionQueryServer(String response) throws InterruptedException, CloneNotSupportedException, JSONException {
		MainActivity.available.acquire();
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(MainActivity.ResponseReceiver.ACTION_RESP);
		broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
		MainActivity.dbExchange.clear();
		MainActivity.dbExchange = (DataBaseExchange) localDbExchange.clone();
		MainActivity.available.release();
		broadcastIntent.putExtra(PARAM_OUT_MSG, response);
		sendBroadcast(broadcastIntent);
	}

	/**
	 * Handle action ReportStatus in the provided background thread with the provided parameters.
	 */
	private void handleActionReportStatus(String param1, String param2) {
		String response = "<msg>message FROM ServerDataService: data from handleActionReportStatus.</msg>";
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(MainActivity.ResponseReceiver.ACTION_RESP);
		broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
		broadcastIntent.putExtra(PARAM_OUT_MSG, response);
		sendBroadcast(broadcastIntent);
	}

	protected synchronized DataBaseExchange doExchange(DataBaseExchange dbEx) throws JSONException {
		int json_sz = dbEx.getJson_data_in().toString().length();
		int dbgidx = 0;
		dbEx.getJson_data_in().put("json_size", json_sz);
		HttpURLConnection httpConnection;
		try {
			assert dbEx.getUrl() != null;
			httpConnection = (HttpURLConnection) dbEx.getUrl().openConnection();
			writeLog(String.format(Locale.US, "ServerDataService:doExchange: httpConnection = (HttpURLConnection) dbEx.getUrl(): %s .openConnection();", dbEx.getUrl()));
		} catch (IOException e) {
			writeLog(String.format(Locale.US, "ServerDataService:doExchange: %d :IOException: %s", dbgidx, e.toString()));
			dbgidx++;
			dbEx.setError_no(dbgidx);
			return dbEx;
		}

		try {
			assert httpConnection != null;
			writeLog(String.format(Locale.US, "ServerDataService:doExchange: BEFORE: httpConnection: %s", httpConnection.toString()));
			httpConnection.setConnectTimeout(NETWORK_TIMEOUT);
			httpConnection.setReadTimeout(NETWORK_TIMEOUT);
			httpConnection.setDoInput(true);
			httpConnection.setDoOutput(true);
			httpConnection.setUseCaches(false);
			httpConnection.setRequestMethod(dbEx.getMethod());
			writeLog(String.format(Locale.US, "ServerDataService:doExchange: AFTER: httpConnection: %s", httpConnection.toString()));
			if (dbEx.getClient_id() != null && dbEx.getClient_secret() != null) {
				writeLog(String.format(Locale.CANADA, "httpConnection.addRequestProperty(\"client_id\", dbEx.getClient_id(): %s)", dbEx.getClient_id()));
				writeLog(String.format(Locale.CANADA, "httpConnection.addRequestProperty(\"client_secret\", dbEx.getClient_secret(): %s)", dbEx.getClient_secret()));
				//httpConnection.addRequestProperty("client_id", dbEx.getClient_id());
				//httpConnection.addRequestProperty("client_secret", dbEx.getClient_secret());
				String userCredentials = dbEx.getClient_id() + ":" + dbEx.getClient_secret();
				String basicAuth = "Basic " + Base64.encodeToString(userCredentials.getBytes(), Base64.DEFAULT);
				httpConnection.setRequestProperty("Authorization", basicAuth);
				httpConnection.setRequestMethod("POST");
				httpConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				//httpConnection.setRequestProperty("Content-Length", "14");
				httpConnection.setRequestProperty("Content-Language", "en-US");
				httpConnection.setUseCaches(false);
				httpConnection.setDoInput(true);
				httpConnection.setDoOutput(true);
			}
			if (dbEx.getGrant_type() != null) {
				writeLog(String.format(Locale.CANADA, "httpConnection.addRequestProperty(\"grant_type\", dbEx.getGrant_type(): %s)", dbEx.getGrant_type()));
				httpConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				dbEx.setJson_data_in(new JSONObject().put("grant_type", dbEx.getGrant_type()));
				//httpConnection.addRequestProperty("grant_type", dbEx.getGrant_type());
				//httpConnection.setRequestProperty("grant_type", dbEx.getGrant_type());
			}
			//httpConnection.setRequestMethod("GET");
			//httpConnection.setRequestMethod("POST");
			writeLog(String.format(Locale.US, "ServerDataService: %s", dbEx.toString()));
		} catch (ProtocolException e) {
			writeLog(String.format(Locale.US, "ServerDataService:doExchange:%d :IOException: %s", dbgidx, e.toString()));
			dbgidx++;
			dbEx.setError_no(dbgidx);
			return dbEx;
		}
		int delay;
		try {
			delay = 0;
			OutputStream os;
			os = httpConnection.getOutputStream();
			BufferedWriter bufferedWriter;
			assert os != null;
			bufferedWriter = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
			if (dbEx.getGrant_type() != null) {
				String postData = URLEncoder.encode("grant_type", "UTF-8") + '=' + URLEncoder.encode(dbEx.getGrant_type(), "UTF-8");
				bufferedWriter.write(postData);
				writeLog(String.format(Locale.US, "ServerDataService: BufferedWriter br.write(%s)", postData));
			} else {
				bufferedWriter.write(dbEx.getJson_data_in().toString());
				writeLog(String.format(Locale.US, "ServerDataService: BufferedWriter br.write(%s)", dbEx.getJson_data_in().toString()));
			}
			bufferedWriter.flush();
			bufferedWriter.close();
			os.flush();
			os.close();
		} catch (IOException e) {
			writeLog(String.format(Locale.US, "ServerDataService:doExchange:%d :IOException: %s", dbgidx, e.toString()));
			dbgidx++;
			dbEx.setError_no(dbgidx);
			httpConnection.disconnect();
			e.printStackTrace();
			return dbEx;
		}
		try {
			httpConnection.connect();
		} catch (IOException e) {
			writeLog(String.format(Locale.US, "ServerDataService:doExchange:%d :IOException: %s", dbgidx, e.toString()));
			dbgidx++;
			dbEx.setError_no(dbgidx);
			httpConnection.disconnect();
			e.printStackTrace();
			return dbEx;
		}
		try {
			InputStream is = httpConnection.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader in = new BufferedReader(isr);
			String inputLine;
			JSONObject json_data;
			StringBuilder sb = new StringBuilder();
			while ((inputLine = in.readLine()) != null) {
				sb.append(inputLine);
				json_data = new JSONObject(sb.toString());
				Iterator<String> itr = json_data.keys();
				while (itr.hasNext()) {
					String key = itr.next();
					dbEx.getJson_data_out().put(key, json_data.get(key));
				}
			}
			httpConnection.disconnect();
			is.close();
			isr.close();
			in.close();
		} catch (IOException | JSONException e) {
			writeLog(String.format(Locale.US, "ServerDataService:doExchange:%d :IOException: %s", dbgidx, e.toString()));
			dbgidx++;
			dbEx.setError_no(dbgidx);
			httpConnection.disconnect();
			e.printStackTrace();
			return dbEx;
		}
		try {
			if (dbEx.getError_no() > 0) {
				this.attempts++;
				writeLog(String.format(Locale.US, "ServerDataService:doExchange: attempt: %d", this.attempts));
				if (this.attempts > dbEx.getMaxAttempts()) {
					writeLog(String.format(Locale.US, "ServerDataService:doExchange: EXCEEDED attempt: %d", this.attempts));
				}
				delay = 8000;
				dbEx.setError_no(0);
			}
			if (delay > 0) {
				Thread.sleep(delay);
			}
		} catch (InterruptedException e) {
			writeLog(String.format(Locale.US, "ServerDataService:doExchange:%d :IOException: %s", dbgidx, e.toString()));
			dbgidx++;
			dbEx.setError_no(dbgidx);
			httpConnection.disconnect();
			e.printStackTrace();
		}
		return dbEx;
	}
}
