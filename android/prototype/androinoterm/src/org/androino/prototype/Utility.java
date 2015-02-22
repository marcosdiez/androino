package org.androino.prototype;

import java.io.FileWriter;
import java.io.IOException;

import android.os.Environment;
import android.util.Log;

public class Utility {
	
	private static String TAG = "Utility";
	private static String FILENAME = "androino.log";

	public static String getFilePath(){
		String path = Environment.getExternalStorageDirectory().getPath();
		String fileFullPath = path + "/" + FILENAME;
		return fileFullPath;
	}
	
	public static void writeToFile(String message){
		String fileFullPath = getFilePath();
		Log.i(TAG, "full log path:" + fileFullPath);
		try {
			//make sure directory exists or it will fail
			writeToFile(fileFullPath, message, false);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void appendToFile(String message){
		String path = Environment.getExternalStorageDirectory().getPath();
		String fileFullPath = path + "/androino.log";
		//Log.v(TAG, "full log path:" + fileFullPath);
		try {
			//make sure directory exists or it will fail
			writeToFile(fileFullPath, message, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	private static void writeToFile(String fileFullPath, String content, boolean append) throws IOException {
        FileWriter fw = new FileWriter(fileFullPath, append);
        fw.append(content);
        fw.close();
	}	

}
