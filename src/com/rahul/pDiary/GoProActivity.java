package com.rahul.pDiary;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;

public class GoProActivity extends Activity {
	private static final String TAG = "GoProActivity";
	Button getIt, noThanks;
	CheckBox dontRemind;
	Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if(DiaryApp.isThemeDark()) setTheme(android.R.style.Theme_DeviceDefault);
		else setTheme(android.R.style.Theme_DeviceDefault_Light);
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.go_pro_activity);
		context = getBaseContext();

		Log.d(TAG, "came in");

		getIt = (Button) findViewById(R.id.get_it_now_button);
		noThanks = (Button) findViewById(R.id.no_thanks_button);
		dontRemind = (CheckBox) findViewById(R.id.dont_show_check_box);

		noThanks.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (dontRemind.isChecked()) {
					SharedPreferences settings = getSharedPreferences(
							"dontRemindMe", 0);
					SharedPreferences.Editor editor = settings.edit();
					editor.putInt("dontRemindMe", 1);
					editor.commit();
				}
				startActivity(new Intent(context, MenuActivity.class));
			}
		});

		getIt.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri
						.parse("market://search?q=pname:com.rahul.pDiary.pro")));
			}
		});
	}
	
	
	
	@Override
	public void onBackPressed() {
		startActivity(new Intent(context, MenuActivity.class));
	}



	@Override
	protected void onStop() {
		super.onStop();
		finish();
	}
}
