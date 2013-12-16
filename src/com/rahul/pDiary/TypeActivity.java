package com.rahul.pDiary;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Shader.TileMode;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.larswerkman.colorpicker.ColorPicker;
import com.larswerkman.colorpicker.ColorPicker.OnColorChangedListener;

abstract class AlbumStorageDirFactory {
	public abstract File getAlbumStorageDir(String albumName);
}

public class TypeActivity extends Activity implements OnClickListener {
	/** Called when the activity is first created. */

	static final String TAG = "TypeActivity";
	private static final int ACTION_TAKE_PHOTO_B = 1;
	private static final int ACTIVITY_SELECT_IMAGE = 2;
	private static final int GALLERY_DIALOG = 1;

	Button button_save;
	Button button_attach_photo;
	EditText edit_note;
	EditText edit_subject;

	private AdView adView;

	AlertDialog picDetail;
	private String mCurrentPhotoPath;
	private static final String JPEG_FILE_PREFIX = "IMG_";
	private static final String JPEG_FILE_SUFFIX = ".jpg";
	
	private ColorPicker titleColorPicker, stripeColorPicker;
	private TextView colorTitle, colorStripe;
	private String stripeColor, titleColor;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");
		// setContentView(R.layout.type_activity);
		setContentView(R.layout.type_color_activity);

		button_save = (Button) findViewById(R.id.button_save);
		button_attach_photo = (Button) findViewById(R.id.button_attach_photo);
		edit_subject = (EditText) findViewById(R.id.editText_subject);
		edit_note = (EditText) findViewById(R.id.editText_note);

		button_save.setOnClickListener(this);
		button_attach_photo.setOnClickListener(this);

		adView = (AdView) findViewById(R.id.ad_banner);
		
		titleColorPicker = (ColorPicker) findViewById(R.id.titlePicker);
		stripeColorPicker = (ColorPicker) findViewById(R.id.stripePicker);
		colorTitle = (TextView) findViewById(R.id.colorTitle);
		colorStripe = (TextView) findViewById(R.id.colorStripe);
		handleColorPickers();
	}
	
	private void handleColorPickers() {

		titleColorPicker
				.setOnColorChangedListener(new OnColorChangedListener() {
					public void onColorChanged(int color) {
						colorTitle.setTextColor(titleColorPicker.getColor());
						titleColor = String.format("#%06X",
								(0xFFFFFF & titleColorPicker.getColor()));
					}
				});

		stripeColorPicker
				.setOnColorChangedListener(new OnColorChangedListener() {
					public void onColorChanged(int color) {
						colorStripe.setTextColor(stripeColorPicker.getColor());
						stripeColor = String.format("#%06X",
								(0xFFFFFF & stripeColorPicker.getColor()));
					}
				});
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
		if (v.getId() == R.id.button_save) {
			final String noteText = edit_note.getText().toString();
			final String noteSubject = edit_subject.getText().toString();
			Log.d(TAG, "button pressed with text : " + noteSubject + ": "
					+ noteText);
			if (noteText.equals("") || noteSubject.equals("")) {
				Toast.makeText(TypeActivity.this, "Empty Field. Did not save.",
						Toast.LENGTH_SHORT).show();
			} else {
				// insert into database
				new insertToDb().execute(noteSubject, noteText,
						DiaryApp.absolutePicPath, titleColor, stripeColor);
			}
		}

		if (v.getId() == R.id.button_attach_photo) {
			createDialogMenu();
		}
	}

	void createDialogMenu() {
		final CharSequence[] items = { "Take a new photo", "From Gallery",
				"View attached photos" };
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
						Toast.makeText(TypeActivity.this, "No photos attached",
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

	// UI Async Task
	class insertToDb extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			try {
				getContentResolver().insert(
						DbHandler.CONTENT_URI,
						DbHandler.stringsToValues(System.currentTimeMillis(),
								params[0], params[1], params[2], params[3], params[4]));
				Log.d(TAG, "Successfully posted: " + params[0] + ", "
						+ params[1] + ", " + params[2] + ", " + params[3] + ", " + params[4]);
				return "Successfully posted: " + params[0];
			} catch (Exception e) {
				e.printStackTrace();
				return "Failed to post: " + params[0];
			}
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (result.contains("Success")) {
				edit_note.setText("");
				edit_subject.setText("");
				DiaryApp.absolutePicPath = "null";
			}
			Toast.makeText(TypeActivity.this, result, Toast.LENGTH_LONG).show();
		}
	}

	// Menu stuff
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_type_activity, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

		case R.id.item_get_data:
			startActivity(new Intent(this, NoteListActivity.class));
			return true;

		case R.id.item_prefs:
			startActivity(new Intent(this, PrefsActivity.class));
			return true;

		default:
			return false;
		}
	}

	// --------------------Camera Stuff----------------------------

	private void galleryAddPic() {
		Intent mediaScanIntent = new Intent(
				"android.intent.action.MEDIA_SCANNER_SCAN_FILE");
		File f = new File(mCurrentPhotoPath);
		Uri contentUri = Uri.fromFile(f);
		mediaScanIntent.setData(contentUri);
		this.sendBroadcast(mediaScanIntent);
	}

	private void handleBigCameraPhoto() {
		if (mCurrentPhotoPath != null) {
			galleryAddPic();
		}

		if (DiaryApp.absolutePicPath.equals("null"))
			DiaryApp.absolutePicPath = mCurrentPhotoPath + ";";
		else
			DiaryApp.absolutePicPath += mCurrentPhotoPath + ";";
		Log.d(TAG, "handleBigCameraPhoto: " + DiaryApp.absolutePicPath);
		mCurrentPhotoPath = null;
		// button_attach_photo.setClickable(false);
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
		Log.d(TAG, DiaryApp.absolutePicPath);
		cursor.close();
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

	private File createImageFile() throws IOException {
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
		File albumF = getAlbumDir();
		File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX,
				albumF);
		return imageF;
	}
	
	public File getAlbumStorageDir(String albumName) {
		return new File (
				Environment.getExternalStorageDirectory()
				+ "/dcim/"
				+ albumName
		);
	}

	private File getAlbumDir() {
		File storageDir = null;

		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {

			try {
				storageDir = getAlbumStorageDir(getString(R.string.album_name));
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (storageDir != null) {
				if (!storageDir.mkdirs()) {
					if (!storageDir.exists()) {
						Log.d("CameraSample", "failed to create directory");
						return null;
					}
				}
			}

		} else {
			Log.v(getString(R.string.app_name),
					"External storage is not mounted READ/WRITE.");
		}
		
		Log.d(TAG, storageDir.getPath());

		return storageDir;
	}

	private File setUpPhotoFile() throws IOException {

		File f = createImageFile();
		mCurrentPhotoPath = f.getAbsolutePath();

		return f;
	}

	protected void dispatchTakePictureIntent(int actionCode) {
		File f = null;
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		try {
			f = setUpPhotoFile();
			mCurrentPhotoPath = f.getAbsolutePath();
			takePictureIntent
					.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
		} catch (IOException e) {
			e.printStackTrace();
			f = null;
			mCurrentPhotoPath = null;
		}
		startActivityForResult(takePictureIntent, actionCode);
	}
}