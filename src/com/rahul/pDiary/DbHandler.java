package com.rahul.pDiary;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

public class DbHandler extends ContentProvider {
	public static final String AUTHORITY = "content://com.rahul.pDiary.provider";
	public static final Uri CONTENT_URI = Uri.parse(AUTHORITY);
	public static final String DB_NAME = "notes.db";
	public static final int DB_VERSION = 1;
	public static final String TABLE = "notes_table";
	public static final String C_ID = "_id";
	public static final String C_SUBJECT = "subject";
	public static final String C_NOTE = "status_text";
	public static final String C_PICTURE = "picture";

	DbHelper dbHelper;
	static SQLiteDatabase db;

	@Override
	public boolean onCreate() {
		dbHelper = new DbHelper(getContext());
		return true;
	}

	@Override
	public String getType(Uri uri) {
		if (uri.getLastPathSegment() == null) {
			return "vnd.android.cursor.dir/vnd.rahul.pDiary.notes";
		} else {
			return "vnd.android.cursor.item/vnd.rahul.pDiary.notes";
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		db = dbHelper.getWritableDatabase();
		long id = db.insertWithOnConflict(TABLE, null, values,
				SQLiteDatabase.CONFLICT_IGNORE);

		if (id != -1)
			uri = Uri.withAppendedPath(uri, Long.toString(id));

		return uri;
	}

	@Override
	public int update(Uri uri, ContentValues values, String whereClause,
			String[] whereArgs) {
		db = dbHelper.getWritableDatabase();
		db.updateWithOnConflict(TABLE, values, whereClause, whereArgs,
				SQLiteDatabase.CONFLICT_IGNORE);
		return 0;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		db = dbHelper.getWritableDatabase();
		int i = db.delete(TABLE, selection, null);
		return i;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		db = dbHelper.getReadableDatabase();
		Cursor cursor = db.query(TABLE, projection, selection, selectionArgs,
				null, null, sortOrder);
		return cursor;
	}
	
	// Helper class for Database stuff
	class DbHelper extends SQLiteOpenHelper {
		static final String TAG = "DbHelper";

		public DbHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			String sql;
			sql = String
					.format("create table %s (%s int primary key, %s text, %s text, %s text)",
							TABLE, C_ID, C_SUBJECT, C_NOTE, C_PICTURE);
			Log.d(TAG, "onCreate with SQL : " + sql);
			db.execSQL(sql);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.d(TAG, "onUpgrade");
			// Usually ALTER TABLE goes here
			// db.execSQL("drop table if exsists " + TABLE);
			// onCreate(db);
		}
	}

	public static ContentValues stringsToValues(long id, String subject, String notes, String picPath) {
		ContentValues values = new ContentValues();
		values.put(C_ID, id);
		values.put(C_SUBJECT, subject);
		values.put(C_NOTE, notes);
		values.put(C_PICTURE, picPath);
		return values;
	}

	//close all database connections
	public static void closeDatabase() {
		try {
			db.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
