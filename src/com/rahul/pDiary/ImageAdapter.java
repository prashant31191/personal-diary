package com.rahul.pDiary;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

//Image Adapter
public class ImageAdapter extends BaseAdapter {
	static final String TAG = "ImageAdapter";
    int mGalleryItemBackground;
    private Context mContext;
    
    String absolutePath;
    String[] paths;

    public ImageAdapter(Context c, String absolutePath) {
        mContext = c;
        this.absolutePath = absolutePath;
        TypedArray attr = mContext.obtainStyledAttributes(R.styleable.style_gallery);
        mGalleryItemBackground = attr.getResourceId(
                R.styleable.style_gallery_android_galleryItemBackground, 0);
        attr.recycle();
        
        //Parsing the photo URIs
        paths = absolutePath.split(";");
    }

    public int getCount() {
        return paths.length;
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView = new ImageView(mContext);

		/* There isn't enough memory to open up more than a couple camera photos */
		/* So pre-scale the target bitmap into which the file is decoded */

		/* Get the size of the ImageView */
		int targetW = 200; 
		int targetH = 150;
		imageView.setMinimumWidth(targetW);
		imageView.setMinimumHeight(targetH);
		
		Log.d(TAG, "Width & Height : " + targetW + " & " + targetH);

		/* Get the size of the image */
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(paths[position], bmOptions);
		int photoW = bmOptions.outWidth;
		int photoH = bmOptions.outHeight;
		
		/* Figure out which way needs to be reduced less */
		int scaleFactor = 1;
		if ((targetW > 0) || (targetH > 0)) {
			scaleFactor = Math.min(photoW/targetW, photoH/targetH);	
		}

		/* Set bitmap options to scale the image decode target */
		bmOptions.inJustDecodeBounds = false;
		bmOptions.inSampleSize = scaleFactor;
		bmOptions.inPurgeable = true;

		/* Decode the JPEG file into a Bitmap */
		Bitmap bitmap = BitmapFactory.decodeFile(paths[position], bmOptions);
		
		/* Associate the Bitmap to the ImageView */
		imageView.setImageBitmap(bitmap);

        imageView.setLayoutParams(new Gallery.LayoutParams(200, 150));
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        imageView.setBackgroundResource(mGalleryItemBackground);

        return imageView;
    }
}

