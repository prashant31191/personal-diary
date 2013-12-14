package com.rahul.pDiary;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class DiaryApp extends Application {
	static final String TAG = "DiaryApp";
	static String absolutePicPath = "null";
	SharedPreferences prefs;

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate");
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
	}

	String checkForPasswords() {
		String pwd = prefs.getString("pref_password", "null");
		String sAns = prefs.getString("security_answer", "null");
		String toast = "Please go to Settings and set your ";

		if (pwd.equals("") && sAns.equals(""))
			toast += "password and security question";
		else if (pwd.equals(""))
			toast += "password";
		else if (sAns.equals(""))
			toast += "security question";
		return toast;
	}
}
