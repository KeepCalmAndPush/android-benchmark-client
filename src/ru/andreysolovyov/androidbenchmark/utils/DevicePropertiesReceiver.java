package ru.andreysolovyov.androidbenchmark.utils;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Pattern;


public class DevicePropertiesReceiver {
	
	public static String getDeviceName(){
		return android.os.Build.MANUFACTURER + " " + android.os.Build.PRODUCT;
	}
	public static int getNumberOfCores(){
		return getNumberOfCoresFromFile();
	}
	private static int getNumberOfCoresFromFile() {
		// Private Class to display only CPU devices in the directory listing
		class CpuFilter implements FileFilter {
			@Override
			public boolean accept(File pathname) {
				// Check if filename is "cpu", followed by a single digit number
				if (Pattern.matches("cpu[0-9]+", pathname.getName())) {
					return true;
				}
				return false;
			}
		}
		try {
			// Get directory containing CPU info
			File dir = new File("/sys/devices/system/cpu/");
			// Filter to only list the devices we care about
			File[] files = dir.listFiles(new CpuFilter());
			// Return the number of cores (virtual CPU devices)
			return files.length;
		} catch (Exception e) {
			// Default to return 1 core
			return 1;
		}
	}
}
