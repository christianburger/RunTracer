package com.runtracer.utilities;

import android.text.format.DateFormat;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.Locale;

public class DataPersistence {

	private final String mFilepath;
	private File file;

	public DataPersistence(String filepath) {
		mFilepath= filepath;
	}

	public boolean fileExists(String filename) {
		boolean result;
		file = new File(mFilepath, filename);
		result = file.exists();
		return result;
	}

	public boolean writeFile(Serializable object, String filename) {
		file = new File(mFilepath, filename);
		try {
			FileOutputStream f_out = new FileOutputStream(file.getAbsolutePath(), false);
			ObjectOutputStream obj_out = new ObjectOutputStream(f_out);
			obj_out.writeObject(object);
			writeLog(String.format(Locale.US, "saved file: %s", file.getAbsolutePath()));
			f_out.close();
		} catch (IOException e) {
			writeLog(String.format(Locale.US, "writeFile(): Exception: %s", e.toString()));
			e.printStackTrace();
		}
		return true;
	}

	public Serializable readFile(String filename) {
		file = new File(mFilepath, filename);
		Serializable object = null;
		try {
			if (file.exists()) {
				// Read from disk using FileInputStream
				FileInputStream f_in = new FileInputStream(file.getAbsolutePath());
				// Read object using ObjectInputStream
				ObjectInputStream obj_in = new ObjectInputStream(f_in);
				// Read an object
				writeLog("File found, reading data now: ");
				object = (Serializable) obj_in.readObject();
				f_in.close();
			} else {
				writeLog("File not found: ");
			}
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}

		return object;
	}

	private void writeLog(String msg) {
		Date cdate;
		String date = (DateFormat.format("dd-MM-yyyy hh:mm:ss a", cdate = new Date()).toString());
		String msg2 = String.format(Locale.US, "<%d>", cdate.getTime());
		String TAG = "mercdb";
		Log.e(TAG, date + msg2 + ": " + msg);
	}
}
