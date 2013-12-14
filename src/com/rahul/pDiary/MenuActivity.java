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
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu_activity);
		Log.d(TAG, "onCreate");
		
		write = (ImageButton) findViewById(R.id.imageButton1);
		view = (ImageButton) findViewById(R.id.imageButton2);
		settings = (ImageButton) findViewById(R.id.imageButton3);
		
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
		if(v.getId() == R.id.imageButton1) {
			startActivity(new Intent(this, TypeActivity.class));
		}
		if(v.getId() == R.id.imageButton2) {
			startActivity(new Intent(this, NoteListActivity.class));
		}
		if(v.getId() == R.id.imageButton3) {
			startActivity(new Intent(this, PrefsActivity.class));
		}
	}
}
