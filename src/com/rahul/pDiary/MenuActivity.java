package com.rahul.pDiary;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.ads.AdRequest;
import com.google.ads.AdView;

public class MenuActivity extends Activity implements OnClickListener{
	
	private static final String TAG = "MenuActivity";
	ImageButton write,view,settings;
	
	private AdView adView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if(DiaryApp.isThemeDark()) setTheme(android.R.style.Theme_DeviceDefault);
		else setTheme(android.R.style.Theme_DeviceDefault_Light);
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu_activity);
		Log.d(TAG, "onCreate");
		
		write = (ImageButton) findViewById(R.id.ib_write_new_note);
		view = (ImageButton) findViewById(R.id.ib_view_saved_notes);
		settings = (ImageButton) findViewById(R.id.ib_settings);
		
		write.setOnClickListener(this);
		view.setOnClickListener(this);
		settings.setOnClickListener(this);
		
		adView = (AdView) findViewById(R.id.ad_banner);
		
		String t = ((DiaryApp) getApplication()).checkForPasswords();
		
		Log.d(TAG, t);
		if(!t.equals("Please go to Settings and set your ")) {
			Toast.makeText(MenuActivity.this, t, Toast.LENGTH_LONG).show();
		}
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

	public void onClick(View v) {
		if(v.getId() == R.id.ib_write_new_note) {
			Log.d(TAG, "Write new note");
			startActivity(new Intent(this, TypeActivity.class));
		}
		else if(v.getId() == R.id.ib_view_saved_notes) {
			Log.d(TAG, "View saved notes");
			// startActivity(new Intent(this, NoteListActivity.class));
			startActivity(new Intent(this, NoteCardActivity.class));
		}
		else if(v.getId() == R.id.ib_settings) {
			Log.d(TAG, "Settings");
			startActivity(new Intent(this, PrefsActivity.class));
		}
	}
}
