/**
 * Author    : Rahul A R
 * Institute : IIIT-Bangalore
 * Date      : 12:57:50 PM
 */
package com.rahul.pDiary;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.fima.cardsui.views.CardUI;
import com.google.ads.AdRequest;
import com.google.ads.AdView;

public class NoteCardActivity extends Activity {
	private CardUI mCardView;
	private static final String TAG = "NoteCardActivity";
	private ArrayList<MyCard> myCards = new ArrayList<MyCard>();

	private static String sortOrder = " DESC";
	private Context thisContext;
	private Intent thisIntent;

	private AdView adView;

	Cursor cursor;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (DiaryApp.isThemeDark())
			setTheme(android.R.style.Theme_DeviceDefault);
		else
			setTheme(android.R.style.Theme_DeviceDefault_Light);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.note_card_activity);
		adView = (AdView) findViewById(R.id.ad_banner);

		// init CardView
		mCardView = (CardUI) findViewById(R.id.cardsview);
		mCardView.setSwipeable(false);

		thisContext = this;
		thisIntent = getIntent();
	}

	void searchAndSet(String query) {
		Log.d(TAG, query);
		Cursor searchCursor = getContentResolver().query(DbHandler.CONTENT_URI,
				null, DbHandler.C_SUBJECT + " LIKE " + "'%" + query + "%'",
				null, DbHandler.C_ID + " DESC");
		displayCards(searchCursor);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (Intent.ACTION_SEARCH.equals(thisIntent.getAction())) {
			String query = thisIntent.getStringExtra(SearchManager.QUERY);
			searchAndSet(query);
		} else {
			fetchAndDisplay();
		}
		mCardView.refresh();

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

	void fetchAndDisplay() {
		cursor = getContentResolver().query(DbHandler.CONTENT_URI, null, null,
				null, DbHandler.C_ID + sortOrder);
		displayCards(cursor);
	}

	void displayCards(Cursor cursor) {
		Log.d(TAG, "entered displayCards");
		mCardView.clearCards();
		while (cursor.moveToNext()) {
			String subject = cursor.getString(cursor
					.getColumnIndex(DbHandler.C_SUBJECT));
			String note = cursor.getString(cursor
					.getColumnIndex(DbHandler.C_NOTE));
			String titleColor = cursor.getString(cursor
					.getColumnIndex(DbHandler.C_COLOR_TITLE));
			String stripeColor = cursor.getString(cursor
					.getColumnIndex(DbHandler.C_COLOR_STRIPE));

			final long id = cursor.getLong(cursor
					.getColumnIndex(DbHandler.C_ID));
			CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(id);

			if (subject.length() >= 20) {
				subject = subject.substring(0, 20) + "...";
			}
			if (note.length() >= 60) {
				note = note.substring(0, 60) + "...";
			}
			note = relativeTime.toString() + "\n" + note;
			Log.d(TAG, "just before creation" + subject + ":" + note + ", "
					+ titleColor + " " + stripeColor);
			if (titleColor == null || titleColor.isEmpty() || titleColor.equals("null"))
				titleColor = "#808080";
			if (stripeColor == null || stripeColor.isEmpty() || stripeColor.equals("null"))
				stripeColor = "#808080";
			MyCard newCard = new MyCard(subject, note, stripeColor, titleColor,
					false, true);

			newCard.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					startActivity(new Intent(thisContext, ViewActivity.class)
							.putExtra("id", id));
				}
			});
			myCards.add(newCard);
			mCardView.addCard(newCard);
		}
	}

	// Menu stuff
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_note_list_activity, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

		case R.id.search:
			onSearchRequested();
			return true;

		case R.id.sort:
			createSortMenu();
			return true;

		case R.id.backup:
			Toast.makeText(this, "Backing up..", Toast.LENGTH_SHORT).show();
			DbHandler.closeDatabase();
			backUp();
			return true;

		case R.id.restore:
			DbHandler.closeDatabase();
			try {
				restore();
			} catch (XmlPullParserException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			onResume();
			return true;

		default:
			return false;
		}
	}

	void createSortMenu() {
		final CharSequence[] items = { "Earliest first", "Latest first" };

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Select an option");
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				if (items[item].equals("Earliest first")) {
					sortOrder = "";
				}
				if (items[item].equals("Latest first")) {
					sortOrder = " DESC";
				}
				fetchAndDisplay();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	// xml backup procedure
	void backUp() {
		Cursor cursor = getContentResolver().query(DbHandler.CONTENT_URI, null,
				null, null, null);
		int id = cursor.getColumnIndex(DbHandler.C_ID);
		int subject = cursor.getColumnIndex(DbHandler.C_SUBJECT);
		int note = cursor.getColumnIndex(DbHandler.C_NOTE);
		int picture = cursor.getColumnIndex(DbHandler.C_PICTURE);
		int title = cursor.getColumnIndex(DbHandler.C_COLOR_TITLE);
		int stripe = cursor.getColumnIndex(DbHandler.C_COLOR_STRIPE);

		try {
			// create directory for backup
			File dir = new File(Environment.getExternalStorageDirectory()
					.getAbsolutePath() + "/pDiary");
			if (!dir.exists())
				dir.mkdirs();

			// SharedPreferences prefs = getSharedPreferences("PREFS",
			// MODE_WORLD_WRITEABLE);
			// SharedPreferences.Editor editor = prefs.edit();
			// int number = prefs.getInt("backUp_number", 0);
			// editor.putInt("backUp_number", ++number);
			// editor.commit();

			// Path to the external backup
			OutputStream output = new FileOutputStream(Environment
					.getExternalStorageDirectory().getAbsolutePath()
					+ "/pDiary/backup.xml");

			while (cursor.moveToNext()) {
				String backingUp = "";
				backingUp += "<row>";
				backingUp += "<C_ID>" + cursor.getLong(id) + "</C_ID>";
				backingUp += "<C_SUB>" + cursor.getString(subject) + "</C_SUB>";
				backingUp += "<C_NOTE>" + cursor.getString(note) + "</C_NOTE>";
				backingUp += "<C_PIC>" + cursor.getString(picture) + "</C_PIC>";
				backingUp += "<C_COLOR_TITLE>" + cursor.getString(title)
						+ "</C_COLOR_TITLE>";
				backingUp += "<C_COLOR_STRIPE>" + cursor.getString(stripe)
						+ "</C_COLOR_STRIPE>";
				backingUp += "</row>";
				Log.d(TAG, backingUp);
				// transfer bytes from the Input File to the Output File
				byte[] buffer = backingUp.getBytes();
				output.write(buffer, 0, buffer.length);
			}
			output.flush();
			output.close();
			Toast.makeText(this, "Back up successful.", Toast.LENGTH_SHORT)
					.show();
		} catch (FileNotFoundException e) {
			Toast.makeText(this, "Back up failed.", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		} catch (IOException e) {
			Toast.makeText(this, "Back up failed.", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}

	}

	// xml restore procedure
	void restore() throws XmlPullParserException, IOException {
		Long id = (long) 0;
		String subject = "", note = "", picPath = "", titleColor = "", stripeColor = "";

		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		factory.setNamespaceAware(true);
		XmlPullParser xpp = factory.newPullParser();

		FileReader reader = null;
		try {
			reader = new FileReader(Environment.getExternalStorageDirectory()
					.getAbsolutePath() + "/pDiary/backup.xml");
		} catch (Exception e) {
			Toast.makeText(getBaseContext(), "Backup file not found",
					Toast.LENGTH_LONG).show();
		}

		xpp.setInput(reader);
		int eventType = xpp.getEventType();

		while (eventType != XmlPullParser.END_DOCUMENT) {
			// if (eventType == XmlPullParser.START_DOCUMENT) {
			// Log.d(TAG, "Start document");
			// } else if (eventType == XmlPullParser.TEXT) {
			// Log.d(TAG, "Start tag " + xpp.getName());
			// } else if (eventType == XmlPullParser.END_TAG) {
			// Log.d(TAG, "End tag " + xpp.getName());
			// }
			if (eventType == XmlPullParser.START_TAG) {
				if (xpp.getName().equals("C_ID")) {
					xpp.next();
					id = Long.parseLong(xpp.getText());
					// Log.d(TAG, "" + id);
				} else if (xpp.getName().equals("C_SUB")) {
					xpp.next();
					subject = xpp.getText();
					// Log.d(TAG, subject);
				} else if (xpp.getName().equals("C_NOTE")) {
					xpp.next();
					note = xpp.getText();
					// Log.d(TAG, note);
				} else if (xpp.getName().equals("C_PIC")) {
					xpp.next();
					picPath = xpp.getText();
					// Log.d(TAG, picPath);
				} else if (xpp.getName().equals("C_COLOR_TITLE")) {
					xpp.next();
					titleColor = xpp.getText();
					// Log.d(TAG, picPath);
				} else if (xpp.getName().equals("C_COLOR_STRIPE")) {
					xpp.next();
					stripeColor = xpp.getText();
					// Log.d(TAG, picPath);
				}
			} else if (eventType == XmlPullParser.END_TAG) {
				if (xpp.getName().equals("row")) {
					// insert into notes.db
					getContentResolver().insert(
							DbHandler.CONTENT_URI,
							DbHandler.stringsToValues(id, subject, note,
									picPath, titleColor, stripeColor));
				}
			}
			eventType = xpp.next();
		}
		Toast.makeText(getBaseContext(), "Restore successful",
				Toast.LENGTH_LONG).show();
	}
}
