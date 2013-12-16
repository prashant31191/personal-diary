package com.rahul.pDiary;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.preference.PreferenceActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ads.AdRequest;
import com.google.ads.AdView;

public class PrefsActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {
	static final String TAG = "PrefsActivity";
	ListView listView;
	private AdView adView;
	SharedPreferences prefs;
	TextView ballTrap, pDiaryPro;
	Context thisContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (DiaryApp.isThemeDark())
			setTheme(android.R.style.Theme_DeviceDefault);
		else
			setTheme(android.R.style.Theme_DeviceDefault_Light);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.pref_activity);
		addPreferencesFromResource(R.xml.prefs);

		prefs = ((DiaryApp) getApplication()).prefs;
		prefs.registerOnSharedPreferenceChangeListener(this);

		listView = (ListView) findViewById(android.R.id.list);

		ballTrap = (TextView) findViewById(R.id.ball_trap);
		ballTrap.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri
						.parse("market://search?q=pname:com.rahul.bTrap")));

			}
		});

		pDiaryPro = (TextView) findViewById(R.id.p_diary_pro);
		pDiaryPro.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri
						.parse("market://search?q=pname:com.rahul.pDiary.pro")));

			}
		});

		thisContext = this;
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
	protected void onDestroy() {
		if (adView != null) {
			adView.destroy();
		}
		super.onDestroy();
	}

	public void autoClick() {
		listView.performItemClick(listView, 2, listView.getItemIdAtPosition(2));
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		Log.d(TAG, "onSharedPreferenceChanged");
		if (key.equals("security_question")) {
			Log.d(TAG, "inside IF");
			autoClick();
		} else if (key.equals("theme")) {
			try {
				popAlert();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	void popAlert() {
		AlertDialog.Builder builder = new AlertDialog.Builder(thisContext);
		builder.setTitle(R.string.theme_alert_title);
		builder.setMessage(R.string.theme_alert_summary);
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
			}
		});
		builder.show();
	}
}

class PasswordPreference extends DialogPreference {

	EditText password1, password2;

	public PasswordPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	protected View onCreateDialogView() {
		// Inflate layout
		LayoutInflater inflater = (LayoutInflater) getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.confirm_password_pref, null);

		password1 = (EditText) view.findViewById(R.id.password);
		password2 = (EditText) view.findViewById(R.id.confirm_password);

		return view;
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if (!positiveResult) {
			return;
		}

		if (shouldPersist()) {
			if (!password1.getText().toString()
					.equals(password2.getText().toString())) {
				Toast.makeText(getContext(), "Passwords did not match",
						Toast.LENGTH_LONG).show();
				return;
			} else {
				Toast.makeText(getContext(), "Password changed",
						Toast.LENGTH_LONG).show();
				persistString(password1.getText().toString());
			}
		}
		super.onDialogClosed(positiveResult);
		notifyChanged();
	}
}
