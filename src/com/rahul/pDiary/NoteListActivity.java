package com.rahul.pDiary;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.SearchManager;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ads.AdRequest;
import com.google.ads.AdView;

public class NoteListActivity extends ListActivity implements
		OnItemLongClickListener {
	static final String TAG = "NoteListActivity";
	static final String[] FROM = { DbHandler.C_ID, DbHandler.C_SUBJECT };
	static final int[] TO = { R.id.text_time, R.id.text_subject };

	static String sortOrder = " DESC";

	Cursor cursor;
	SimpleCursorAdapter adapter;

	Intent editActivityIntent;
	Intent thisIntent;

	private AdView adView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.note_list_activity);

		// experimental long click listener
		ListView listView = (ListView) findViewById(android.R.id.list);
		listView.setOnItemLongClickListener(this);

		editActivityIntent = new Intent(this, EditActivity.class);
		thisIntent = getIntent();

		adView = (AdView) findViewById(R.id.ad_banner);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Search logic initialization
		if (Intent.ACTION_SEARCH.equals(thisIntent.getAction())) {
			String query = thisIntent.getStringExtra(SearchManager.QUERY);
			searchAndSet(query);
		} else {
			fetchAndDisplay();
		}
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

	void fetchAndDisplay() {
		cursor = getContentResolver().query(DbHandler.CONTENT_URI, null, null,
				null, DbHandler.C_ID + sortOrder);
		displayList(cursor);
	}

	@Override
	protected void onStop() {
		super.onStop();
		// cursor.close();
	}

	@Override
	public void onDestroy() {
		if (adView != null) {
			adView.destroy();
		}
		super.onDestroy();
	}

	void searchAndSet(String query) {
		Log.d(TAG, query);
		Cursor searchCursor = getContentResolver().query(DbHandler.CONTENT_URI,
				null, DbHandler.C_SUBJECT + " LIKE " + "'%" + query + "%'",
				null, DbHandler.C_ID + " DESC");
		displayList(searchCursor);
	}

	void displayList(Cursor tempCursor) {
		adapter = new SimpleCursorAdapter(this, R.layout.row, tempCursor, FROM,
				TO);
		adapter.setViewBinder(viewBinder);
		setListAdapter(adapter);
	}

	// on list item long click
	public boolean onItemLongClick(AdapterView<?> parent, View view, int pos,
			long id) {
		Log.d(TAG, "onItemLongClick");
		createDialogMenu(id);
		return true;
	}

	// on list item short click
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Log.d(TAG, "Item clicked with ID: " + id);
		startActivity(new Intent(this, ViewActivity.class).putExtra("id", id));
	}

	void createDialogMenu(long id) {
		final CharSequence[] items = { "Edit", "Delete" };
		// anything other than final cannot be passed into an inner class
		final long ID = id;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Select an option");
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				if (items[item].equals("Edit")) {
					startActivity(editActivityIntent.putExtra("id", ID));
				}
				if (items[item].equals("Delete")) {
					deleteDialog(ID);
				}
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	void deleteDialog(long id) {
		final long ID = id;
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
		alertDialog.setTitle("Delete note");
		alertDialog.setMessage("Do you really want to delete?");
		alertDialog.setPositiveButton("OK",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						getContentResolver().delete(DbHandler.CONTENT_URI,
								DbHandler.C_ID + "=" + ID, null);
						finish();
						startActivity(thisIntent);
					}
				});
		alertDialog.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// Let's see what happens here
					}
				});
		alertDialog.show();
	}

	static final ViewBinder viewBinder = new ViewBinder() {

		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			if (view.getId() == R.id.text_time) {
				long time = cursor.getLong(cursor
						.getColumnIndex(DbHandler.C_ID));
				CharSequence relativeTime = DateUtils
						.getRelativeTimeSpanString(time);
				((TextView) view).setText(relativeTime);
				return true;
			}

			if (view.getId() == R.id.text_subject) {
				String subject = cursor.getString(cursor
						.getColumnIndex(DbHandler.C_SUBJECT));
				if (subject.length() > 18) {
					((TextView) view).setText(subject.substring(0, 18) + "...");
				} else {
					((TextView) view).setText(subject);
				}
				return true;
			}
			return false;
		}
	};

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
				backingUp += "</row>";
				Log.d(TAG, backingUp);
				// transfer bytes from the Input File to the Output File
				byte[] buffer = backingUp.getBytes();
				output.write(buffer, 0, buffer.length);
			}
			output.flush();
			output.close();
			Toast.makeText(this, "Back up successful.", Toast.LENGTH_SHORT).show();
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
		String subject = "", note = "", picPath = "";

		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		factory.setNamespaceAware(true);
		XmlPullParser xpp = factory.newPullParser();

		FileReader reader = null;
		try {
			reader = new FileReader(Environment
					.getExternalStorageDirectory().getAbsolutePath()
					+ "/pDiary/backup.xml");
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

					// insert into notes.db
					getContentResolver().insert(
							DbHandler.CONTENT_URI,
							DbHandler.stringsToValues(id, subject, note,
									picPath, "#000000", "ffffff"));
				}
			}
			eventType = xpp.next();
		}
		Toast.makeText(getBaseContext(), "Restore successful",
				Toast.LENGTH_LONG).show();
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

}
