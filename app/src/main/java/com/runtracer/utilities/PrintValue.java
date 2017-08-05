package com.runtracer.utilities;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PrintValue {
	public static String printValue(Date value) {
		String stringtoprint = "";
		//SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		SimpleDateFormat date_format = new SimpleDateFormat("MMMM/yyyy", Locale.getDefault());
		if (value != null && value.after(new Date(1000000))) {
			stringtoprint = date_format.format(value);
		}
		return stringtoprint;
	}

	public static String printValue(double value) {
		NumberFormat nf = NumberFormat.getInstance(Locale.getDefault());
		String stringtoprint = "--";
		if (value >= 0) {
			stringtoprint = nf.format(value);
		}
		return stringtoprint;
	}

	public static String printValue(String value) {
		String stringtoprint = "";
		if (value != null && !value.isEmpty()) {
			stringtoprint = value;
		}
		return stringtoprint;
	}
}
