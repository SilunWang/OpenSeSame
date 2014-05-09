package com.opensesameapp;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.apache.http.util.EncodingUtils;

public class FileOperation {
	// 写数据到SD中的文件
	
	public static void writeFileSdcardFile(String fileName, String write_str) {
		try {
			//FileOutputStream fout = openFileOutput(fileName, MODE_PRIVATE);
			FileOutputStream fout = new FileOutputStream(fileName,true);
			byte[] bytes = write_str.getBytes();

			fout.write(bytes);
			fout.close();
		}

		catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 读SD中的文件
	public static String readFileSdcardFile(String fileName) {
		String res = "";
		try {
			FileInputStream fin = new FileInputStream(fileName);

			int length = fin.available();

			byte[] buffer = new byte[length];
			fin.read(buffer);

			res = EncodingUtils.getString(buffer, "UTF-8");
			fin.close();
		}

		catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}
}
