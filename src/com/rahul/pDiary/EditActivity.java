package com.rahul.pDiary;

import java.io.File;

import com.google.ads.AdRequest;
import com.google.ads.AdView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class EditActivity extends Activity implements OnClickListener {
	/** Called when the activity is first created. */

	static final String TAG = "EditActivity";
	private static final int ACTION_TAKE_PHOTO_B = 1;
	private static final int ACTIVITY_SELECT_IMAGE = 2;
	private static final int GALLERY_DIALOG = 1;

	Button button_save, button_attach_photo;
	EditText edit_note;
	EditText edit_subject;

	long id;

	AlertDialog picDetail;
	
	private AdView adView;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_activity);

		Bundle extras = getIntent().getExtras();
		id = extras.getLong("id");
		Log.d(TAG, "id: " + id);

		button_save = (Button) findViewById(R.id.button_save);
		button_attach_photo = (Button) findViewById(R.id.button_attach_photo);

		edit_subject = (EditText) findViewById(R.id.editText_subject);
		edit_note = (EditText) findViewById(R.id.editText_note);

		button_save.setOnClickListener(this);
		button_attach_photo.setOnClickListener(this);

		Cursor cursor = getContentResolver().query(DbHandler.CONTENT_URI, null,
				DbHandler.C_ID + "=" + id, null, null);
		cursor.moveToFirst();

		String subjectString = cursor.getString(cursor
				.getColumnIndex(DbHandler.C_SUBJECT));
		String noteString = cursor.getString(cursor
				.getColumnIndex(DbHandler.C_NOTE));
		DiaryApp.absolutePicPath = cursor.getString(cursor
				.getColumnIndex(DbHandler.C_PICTURE));
		
		cursor.close();

		edit_subject.setText(subjectString);
		edit_note.setText(noteString);
		
		adView = (AdView) findViewById(R.id.ad_banner);
	}
	
	@Override
	public void onResume() {
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
		if (v.getId() == R.id.button_save) {
			final String noteText = edit_note.getText().toString();
			final String noteSubject = edit_subject.getText().toString();
			Log.d(TAG, "button pressed with text : " + noteSubject + ": "
					+ noteText + ": " + DiaryApp.absolutePicPath);
			if (noteText.equals("") || noteSubject.equals("")) {
				Toast.makeText(EditActivity.this, "Empty Field. Did not save.",
						Toast.LENGTH_SHORT).show();
			} else {
				// update database
				getContentResolver().update(
						DbHandler.CONTENT_URI,
						DbHandler.stringsToValues(id, noteSubject, noteText,
								DiaryApp.absolutePicPath),
						DbHandler.C_ID + "=" + id, null);
				DiaryApp.absolutePicPath = "null";
				finish();
			}
		}

		if (v.getId() == R.id.button_attach_photo) {
			createDialogMenu();
		}
	}

	void createDialogMenu() {
		final CharSequence[] items = { "Take a new photo", "From Gallery",
				"View attached photos" };
		// anything other than final cannot be passed into an inner class
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Select an option");
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				if (item == 0) {
					dispatchTakePictureIntent(ACTION_TAKE_PHOTO_B);
				}
				if (item == 1) {
					Intent galleryIntent = new Intent(
							Intent.ACTION_PICK,
							android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
					startActivityForResult(galleryIntent, ACTIVITY_SELECT_IMAGE);
				}
				if (item == 2) {
					if (DiaryApp.absolutePicPath.equals("null")) {
						Toast.makeText(EditActivity.this, "No photos attached",
								Toast.LENGTH_SHORT).show();
					} else
						showDialog(GALLERY_DIALOG);
				}

			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	// Gallery Dialog
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case GALLERY_DIALOG:
			LayoutInflater lo = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
			View picDetailView = lo.inflate(R.layout.gallery_dialog,
					(Gallery) findViewById(R.id.gallery));

			AlertDialog.Builder picDetailBuilder = new AlertDialog.Builder(this);

			picDetailBuilder.setTitle("Added photos");
			picDetailBuilder.setView(picDetailView);
			picDetail = picDetailBuilder.create();

			return picDetail;
		default:
			break;
		}
		return null;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
		switch (id) {
		case GALLERY_DIALOG:
			Gallery g = (Gallery) dialog.findViewById(R.id.gallery);
			g.setAdapter(new ImageAdapter(dialog.getContext(),
					DiaryApp.absolutePicPath));

			g.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View v,
						int position, long id) {
					createGalleryDialogMenu(id);
				}
			});
		}
		super.onPrepareDialog(id, dialog, args);
	}

	// On photo click in Gallery dialog
	void createGalleryDialogMenu(long id) {
		final CharSequence[] items = { "View photo", "Remove photo" };
		String[] paths = DiaryApp.absolutePicPath.split(";");
		// anything other than final cannot be passed into an inner class
		final String picPath = paths[(int) id];
		final long ID = id;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Select an option");

		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {

				if (items[item].equals("View photo")) {
					// Just create an intent to view the photo
					Intent intent = new Intent();
					intent.setAction(android.content.Intent.ACTION_VIEW);

					File file = new File(picPath);
					intent.setDataAndType(Uri.fromFile(file), "image/jpg");
					startActivity(intent);
				}
				if (items[item].equals("Remove photo")) {
					picDetail.dismiss();
					updatePath(ID);

				}
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	void updatePath(long id) {
		String paths[] = DiaryApp.absolutePicPath.split(";");
		paths[(int) id] = "";
		DiaryApp.absolutePicPath = "";
		for (int i = 0; i < paths.length; i++) {
			DiaryApp.absolutePicPath += paths[i];
		}
		try {
			if (DiaryApp.absolutePicPath.equals("")) {
				DiaryApp.absolutePicPath = "null";
			}
		} catch (NullPointerException e) {
			DiaryApp.absolutePicPath = "null";
		}
		Log.d(TAG, "updated path:" + DiaryApp.absolutePicPath);
	}

	// --------------------Camera Stuff----------------------------

	private String getLastImagePath() {
		final String[] imageColumns = { MediaStore.Images.Media._ID,
				MediaStore.Images.Media.DATA };
		final String imageOrderBy = MediaStore.Images.Media._ID + " DESC";

		Cursor imageCursor = managedQuery(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageColumns,
				null, null, imageOrderBy);

		if (imageCursor.moveToFirst()) {
			String fullPath = imageCursor.getString(imageCursor
					.getColumnIndex(MediaStore.Images.Media.DATA));
			imageCursor.close();
			return fullPath;
		} else {
			return "null";
		}
	}

	private void handleBigCameraPhoto() {
		if (DiaryApp.absolutePicPath.equals("null"))
			DiaryApp.absolutePicPath = getLastImagePath() + ";";
		else
			DiaryApp.absolutePicPath += getLastImagePath() + ";";
		Log.d(TAG, "handleBigCameraPhoto: " + DiaryApp.absolutePicPath);
		// button_attach_photo.setClickable(false);
		//button_attach_photo.setText("Change Picture");
	}

	private void getPathFromUri(Uri uri) {
		String[] filePathColumn = { MediaStore.Images.Media.DATA };

		Cursor cursor = getContentResolver().query(uri, filePathColumn, null,
				null, null);
		cursor.moveToFirst();

		int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
		String filePath = cursor.getString(columnIndex);
		if (DiaryApp.absolutePicPath.equals("null"))
			DiaryApp.absolutePicPath = filePath + ";";
		else
			DiaryApp.absolutePicPath += filePath + ";";
		cursor.close();
		//button_attach_photo.setText("Change Picture");
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case ACTION_TAKE_PHOTO_B:
			if (resultCode == RESULT_OK) {
				handleBigCameraPhoto();
			}
			break;

		case ACTIVITY_SELECT_IMAGE:
			if (resultCode == RESULT_OK) {
				Uri uri = data.getData();
				getPathFromUri(uri);
			}
			break;
		} // switch
	}

	protected void dispatchTakePictureIntent(int actionCode) {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		startActivityForResult(takePictureIntent, actionCode);
	}
}