package com.runtracer.utilities;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TypeCheck {
	public static boolean isEmailValid(String email) {
		String regExpn = "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
			+ "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
			+ "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
			+ "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
			+ "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
			+ "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$";

		Pattern pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE);
		Matcher matcher;
		matcher = pattern.matcher(email);
		return matcher.matches();
	}

	public static boolean isNumber(String str) {
		int size = str.length();
		for (int i = 0; i < size; i++) {
			Character cchar = str.charAt(i);
			if (!Character.isDigit(cchar) && (cchar != '.') && (cchar != ',')) {
				return false;
			}
		}
		return size > 0;
	}
}
