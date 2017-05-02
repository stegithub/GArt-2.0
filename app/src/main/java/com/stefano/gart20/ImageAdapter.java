package com.stefano.gart20;

/**
 * Created by stefano on 05/05/2015.
 */

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ImageAdapter extends BaseAdapter {

    private Context context;
    ArrayList<String> imageList = new ArrayList<String>();

    public ImageAdapter(Context c) {
        context = c;
    }

    void add(String path){
        imageList.add(path);
    }

    void clear() {
        imageList.clear();
    }

    void remove(int index){
        imageList.remove(index);
    }

    @Override
    public int getCount() {
        return imageList.size();
    }

    @Override
    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }


    // Convert DP to PX
    // Source: http://stackoverflow.com/a/8490361
    public int dpToPx(int dps) {
        final float scale = context.getResources().getDisplayMetrics().density;
        int pixels = (int) (dps * scale + 0.5f);

        return pixels;
    }

    //getView load bitmap in AsyncTask
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;


        int wPixel = dpToPx(120);
        int hPixel = dpToPx(120);


        ImageView imageView;
        if (convertView == null) { // if it's not recycled, initialize some
            // attributes
            imageView = new ImageView(context);

            // Set height and width constraints for the image view
            imageView.setLayoutParams(new LinearLayout.LayoutParams(wPixel, hPixel));
           // imageView.setLayoutParams(new GridView.LayoutParams(220, 220));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);

            convertView = imageView;

            holder = new ViewHolder();
            holder.image = imageView;
            holder.position = position;
            convertView.setTag(holder);
        } else {
            //imageView = (ImageView) convertView;
            holder = (ViewHolder) convertView.getTag();
            ((ImageView)convertView).setImageBitmap(null);
        }

        //Bitmap bm = decodeSampledBitmapFromUri(itemList.get(position), 220, 220);
        // Using an AsyncTask to load the slow images in a background thread
                new AsyncTask<ViewHolder, Void, Bitmap>() {
                    private ViewHolder v;

                    @Override
                    protected Bitmap doInBackground(ViewHolder... params) {

                        Bitmap bm = null;

                        boolean haveThumbNail = false;

                        try {
                            ExifInterface exifInterface =
                                    new ExifInterface(imageList.get(position));
                            if(exifInterface.hasThumbnail()){
                                byte[] thumbnail = exifInterface.getThumbnail();
                                bm = BitmapFactory.decodeByteArray(thumbnail, 0, thumbnail.length);
                            }
                            else{
                                  bm = decodeSampledBitmapFromUri(
                                    imageList.get(position), 220, 220);
                                    }


                            if(AsyncTaskLoadFiles.files[position].isDirectory()){
                                bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.directory_icon);
                                // notBuilder.setLargeIcon(largeIcon);

                                String dirName = AsyncTaskLoadFiles.files[position].getName();
                                    Bitmap wrotebm = drawTextToBitmap(context, bm, dirName);

                                    v = params[0];
                                    return wrotebm;
                            }


                            haveThumbNail = true;
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                        if(!haveThumbNail){
                            bm = decodeSampledBitmapFromUri(
                                    imageList.get(position), 220, 220);
                        }


                File file = new File(imageList.get(position));
                int orientation = ExtSdCardActivity.getCameraPhotoOrientation(context, Uri.fromFile(file), file.getAbsolutePath());
                if(orientation != 0 && bm != null){
                    Matrix matrix = new Matrix();
                    matrix.postRotate(orientation);
                  //  Bitmap scaledBitmap = Bitmap.createScaledBitmap(bm,bm.getWidth(),bm.getHeight(),true);
                    Bitmap rotatedBitmap = Bitmap.createBitmap(bm , 0, 0, bm .getWidth(),
                                bm .getHeight(), matrix, true);

                    v = params[0];
                    return rotatedBitmap;
                    }

                else
                {v = params[0];
                   return  bm;}
            }

            @Override
            protected void onPostExecute(Bitmap result) {
                super.onPostExecute(result);
                //Not work for me!

			        /*if (v.position == position) {
			            // If this item hasn't been recycled already,
			        	// show the image
			            v.image.setImageBitmap(result);
			        }*/

                v.image.setImageBitmap(result);
            }
                }.execute(holder);

        //imageView.setImageBitmap(bm);
        //return imageView;
        return convertView;
    }


    public Bitmap drawTextToBitmap(Context gContext, Bitmap bitmap, String gText) {
        Resources resources = gContext.getResources();
        float scale = resources.getDisplayMetrics().density;

        android.graphics.Bitmap.Config bitmapConfig =
                bitmap.getConfig();
        // set default bitmap config if none
        if(bitmapConfig == null) {
            bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
        }
        // resource bitmaps are imutable,
        // so we need to convert it to mutable one
        bitmap = bitmap.copy(bitmapConfig, true);

        Canvas canvas = new Canvas(bitmap);
        // new antialised Paint
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // text color - #3D3D3D
        paint.setColor(Color.rgb(61, 61, 61));
        // text size in pixels
        paint.setTextSize((int) (25 * scale));

      //  Paint.setTextAlign(Paint.Align.CENTER)
        // text shadow
        paint.setShadowLayer(1f, 0f, 1f, Color.WHITE);

        // draw text to the Canvas center
        Rect bounds = new Rect();
      //  paint.setTextAlign(Paint.Align.CENTER);

        paint.getTextBounds(gText, 0, gText.length(), bounds);
        int x = (bitmap.getWidth() - bounds.width())/2;
        int y = (bitmap.getHeight() + bounds.height())/2;

        //canvas.drawText(gText, x * scale, y * scale, paint);
        canvas.drawText(gText, x, y, paint);

        return bitmap;
    }


    public Bitmap decodeSampledBitmapFromUri(String path, int reqWidth, int reqHeight) {

        Bitmap bm = null;
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        bm = BitmapFactory.decodeFile(path, options);

        return bm;
    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) height
                        / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }
        }
        return inSampleSize;
    }
    class ViewHolder {
        ImageView image;
        int position;
    }
}
