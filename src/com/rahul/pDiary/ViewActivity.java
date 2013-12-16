package com.rahul.pDiary;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Gallery;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ads.AdRequest;
import com.google.ads.AdView;

public class ViewActivity extends Activity {
	static final String TAG = "ViewActivity";
	long id;
	boolean showing, photo;
	TextView time, subject, note, photosAttached;
	Gallery gallery;

	String picPaths;

	private AdView adView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if(DiaryApp.isThemeDark()) setTheme(android.R.style.Theme_DeviceDefault);
		else setTheme(android.R.style.Theme_DeviceDefault_Light);
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_activity);

		gallery = (Gallery) findViewById(R.id.gallery);
		gallery.setVisibility(View.GONE);

		Bundle extras = getIntent().getExtras();
		id = extras.getLong("id");

		adView = (AdView) findViewById(R.id.ad_banner);
		photosAttached = (TextView) findViewById(R.id.textView_photos);

		showing = false;
	}

	@Override
	protected void onResume() {
		super.onResume();
		String subjectString = null;
		String noteString = null;

		Cursor cursor = getContentResolver().query(DbHandler.CONTENT_URI, null,
				DbHandler.C_ID + "=" + id, null, null);
		cursor.moveToFirst();

		subjectString = cursor.getString(cursor
				.getColumnIndex(DbHandler.C_SUBJECT));
		noteString = cursor.getString(cursor.getColumnIndex(DbHandler.C_NOTE));
		picPaths = cursor.getString(cursor.getColumnIndex(DbHandler.C_PICTURE));

		cursor.close();

		if (picPaths.equals("null")) {
			photo = false;
			photosAttached.setText("No photos");

		} else {
			photo = true;
			gallery.setAdapter(new ImageAdapter(this, picPaths));
			final String[] paths = picPaths.split(";");
			if (paths.length == 1)
				photosAttached.setText((paths.length) + " photo");
			else
				photosAttached.setText((paths.length) + " photos");

			gallery.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View v,
						int position, long id) {
					// Just create an intent to view the photo
					Intent intent = new Intent();
					intent.setAction(android.content.Intent.ACTION_VIEW);

					File file = new File(paths[(int) id]);
					intent.setDataAndType(Uri.fromFile(file), "image/jpg");
					startActivity(intent);

				}
			});
		}

		// Format date
		SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");
		Date resultdate = new Date(id);
		String timeString = sdf.format(resultdate).toString();

		time = (TextView) findViewById(R.id.textView_time);
		subject = (TextView) findViewById(R.id.textView_subject);
		note = (TextView) findViewById(R.id.textView_note);

		time.setText(timeString);
		subject.setText(subjectString);
		note.setText(noteString);

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

	// Menu stuff
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_view_activity, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

		case R.id.item_delete_note:
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
			alertDialog.setTitle("Delete note");
			alertDialog.setMessage("Do you really want to delete?");
			alertDialog.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							getContentResolver().delete(DbHandler.CONTENT_URI,
									DbHandler.C_ID + "=" + id, null);
							finish();
						}
					});
			alertDialog.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							// Let's see what happens here
						}
					});
			alertDialog.show();
			return true;

		case R.id.item_edit_note:
			startActivity(new Intent(this, EditActivity.class).putExtra("id",
					id));
			return true;

		case R.id.photo_toggle:
			if (photo == true) {
				if (showing == false) {
					showing = true;
					gallery.setVisibility(View.VISIBLE);
				} else {
					showing = false;
					gallery.setVisibility(View.GONE);
				}
			} else {
				Toast.makeText(ViewActivity.this, "No photos attached",
						Toast.LENGTH_SHORT).show();
			}
			return true;

		default:
			return false;
		}
	}
}
