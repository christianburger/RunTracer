package com.runtracer.utilities;

import android.content.Context;
import android.view.Display;
import android.view.WindowManager;

public class SystemScale {

	static public int getScaleY(Context context, int pic_height) {
		Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		int height = display.getHeight();
		Double val;
		val = (double) height / (double) pic_height;
		val = val * 100d;
		return val.intValue();
	}

	static public int getScaleX(Context context, int pic_width) {
		Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		int width = display.getWidth();
		Double val;
		val = (double) width / (double) pic_width;
		val = val * 100d;
		return val.intValue();
	}

}
