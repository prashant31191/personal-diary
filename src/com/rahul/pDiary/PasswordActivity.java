package com.rahul.pDiary;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ads.AdRequest;
import com.google.ads.AdView;

public class PasswordActivity extends Activity implements OnClickListener {
	static final String TAG = "PasswordActivity";
	static final int PASS_DIALOG = 0;
	EditText editPassword;
	TextView forgot, fp_question;
	Button buttonOk, fp_ok;

	private AdView adView;
	AlertDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if(DiaryApp.isThemeDark()) setTheme(android.R.style.Theme_DeviceDefault);
		else setTheme(android.R.style.Theme_DeviceDefault_Light);
		
		super.onCreate(savedInstanceState);
		final String secret = ((DiaryApp) getApplication()).prefs.getString(
				"pref_password", "");
		if (secret.equals("")) {
			SharedPreferences settings = getSharedPreferences("dontRemindMe", 0);
			int reminder = settings.getInt("dontRemindMe", 0);
			if (reminder == 1)
				startActivity(new Intent(this, MenuActivity.class));
			else
				startActivity(new Intent(this, GoProActivity.class));
		}

		setContentView(R.layout.password_activity);

		editPassword = (EditText) findViewById(R.id.editText_password);
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

		buttonOk = (Button) findViewById(R.id.button_ok);
		buttonOk.setOnClickListener(this);

		forgot = (TextView) findViewById(R.id.textView_forgot_password);
		forgot.setOnClickListener(this);

		adView = (AdView) findViewById(R.id.ad_banner);
	}

	@Override
	protected void onResume() {
		super.onResume();
		try {
			// Create the adView
			AdRequest adRequest;

			// Add request
			adRequest = new AdRequest();
			adRequest.addTestDevice(AdRequest.TEST_EMULATOR);
			adView.loadAd(adRequest);
		} catch (Exception e) {
			Log.d(TAG, "Error is here");
			e.printStackTrace();
		}
	}

	@Override
	public void onDestroy() {
		if (adView != null) {
			adView.destroy();
		}
		super.onDestroy();
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.d(TAG, "onStop");
		finish();
	}

	void checkPassword() {
		final String pwd = editPassword.getText().toString();
		final String secret = ((DiaryApp) getApplication()).prefs.getString(
				"pref_password", "");
		if (!pwd.equals(secret))
			Toast.makeText(PasswordActivity.this, "Wrong Password!",
					Toast.LENGTH_SHORT).show();
		else {
			SharedPreferences settings = getSharedPreferences("dontRemindMe", 0);
			int reminder = settings.getInt("dontRemindMe", 0);
			if (reminder == 1)
				startActivity(new Intent(this, MenuActivity.class));
			else
				startActivity(new Intent(this, GoProActivity.class));
		}
	}

	public void onClick(View v) {
		if (v.getId() == R.id.button_ok) {
			checkPassword();
		}
		if (v.getId() == R.id.button_fp_ok) {
			EditText e = (EditText) dialog
					.findViewById(R.id.editText_security_answer);
			String userAnswer = e.getText().toString();
			forgotPassword(userAnswer);
		}
		if (v.getId() == R.id.textView_forgot_password) {
			showDialog(PASS_DIALOG);
		}
	}

	void wrongPasswordAlert() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
		alertDialog.setTitle("Alert");
		alertDialog.setMessage("Wrong Answer");
		alertDialog.setPositiveButton("OK",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// Do nothing
					}
				});
		alertDialog.show();
	}

	// Forgot password
	void forgotPassword(String userAnswer) {
		String answer = PreferenceManager.getDefaultSharedPreferences(this)
				.getString("security_answer", "null");
		if (answer.equals(userAnswer)) {
			startActivity(new Intent(this, PrefsActivity.class));
		} else {
			dialog.cancel();
			wrongPasswordAlert();
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case PASS_DIALOG:
			LayoutInflater lo = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
			View passwordView = lo.inflate(R.layout.password_dialog,
					(LinearLayout) findViewById(R.id.password_dialog_root));

			AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

			dialogBuilder.setTitle("Security question");
			dialogBuilder.setView(passwordView);
			dialog = dialogBuilder.create();

			return dialog;
		default:
			break;
		}
		return null;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
		switch (id) {
		case PASS_DIALOG:
			String q = ((DiaryApp) getApplication()).prefs.getString(
					"security_question", "null");
			Log.d(TAG, q);

			fp_question = (TextView) dialog
					.findViewById(R.id.textView_security_question);
			fp_question.setText(q);

			fp_ok = (Button) dialog.findViewById(R.id.button_fp_ok);
			fp_ok.setOnClickListener(this);
		}
		super.onPrepareDialog(id, dialog, args);
	}

}
