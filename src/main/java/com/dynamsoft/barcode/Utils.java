package com.dynamsoft.barcode;

public class Utils {
	private static String OS = System.getProperty("os.name").toLowerCase();
	
	public static boolean isWindows() {

		return (OS.indexOf("win") >= 0);

	}

	public static boolean isMac() {

		return (OS.indexOf("mac") >= 0);

	}

	public static boolean isLinux() {

		return (OS.indexOf("nux") >= 0);
		
	}
}
