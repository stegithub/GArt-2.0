package com.stefano.gart20;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.stefano.gart20.WiFi.DeviceDetailFragment;
import com.stefano.gart20.WiFi.FileTransferService;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import static org.opencv.imgcodecs.Imgcodecs.imread;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static int STATE; //0 Sd secondaria non presente, 1 se presente

    String targetPath = null;


    private Uri mMakePhotoUri;

    // A File object containing the path to the transferred files
    private String mParentPath;
    // Incoming Intent
    private Intent mIntent;


    /*
     * Called from onNewIntent() for a SINGLE_TOP Activity
     * or onCreate() for a new Activity. For onNewIntent(),
     * remember to call setIntent() to store the most
     * current Intent
     *
     */
    private void handleViewIntent() {

        // Get the Intent action
        mIntent = getIntent();
        String action = mIntent.getAction();
        /*
         * For ACTION_VIEW, the Activity is being asked to display data.
         * Get the URI.
         */
        if (TextUtils.equals(action, Intent.ACTION_VIEW)) {
            // Get the URI from the Intent
            Uri beamUri = mIntent.getData();
            /*
             * Test for the type of URI, by getting its scheme value
             */
            if (TextUtils.equals(beamUri.getScheme(), "file")) {
                mParentPath = handleFileUri(beamUri);
            } else if (TextUtils.equals(
                    beamUri.getScheme(), "content")) {
                mParentPath = handleContentUri(beamUri);
            }
        }
    }

    public String handleContentUri(Uri beamUri) {
        // Position of the filename in the query Cursor
        int filenameIndex;
        // File object for the filename
        File copiedFile;
        // The filename stored in MediaStore
        String fileName;
        // Test the authority of the URI
        if (!TextUtils.equals(beamUri.getAuthority(), MediaStore.AUTHORITY)) {
            /*
             * Handle content URIs for other content providers
             */
            // For a MediaStore content URI
        } else {
            // Get the column that contains the file name
            String[] projection = {MediaStore.MediaColumns.DATA};
            Cursor pathCursor =
                    getContentResolver().query(beamUri, projection,
                            null, null, null);
            // Check for a valid cursor
            if (pathCursor != null &&
                    pathCursor.moveToFirst()) {
                // Get the column index in the Cursor
                filenameIndex = pathCursor.getColumnIndex(
                        MediaStore.MediaColumns.DATA);
                // Get the full file name including path
                fileName = pathCursor.getString(filenameIndex);
                // Create a File object for the filename
                copiedFile = new File(fileName);
                // Return the parent directory of the file
                return new String(copiedFile.getParent());
            } else {
                // The query didn't work; return null
                return null;
            }
        }
        return null;
    }

    public String handleFileUri(Uri beamUri) {
        // Get the path part of the URI
        String fileName = beamUri.getPath();
        // Create a File object for this filename
        File copiedFile = new File(fileName);
        // Get a string containing the file's parent directory
        return copiedFile.getParent();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                  //      .setAction("Action", null).show();
                CheckBox checkBoxFolder = new CheckBox(getApplicationContext());
                SharedPreferences settingsFolder = getSharedPreferences("preferenceFolder", 0);
                Boolean isCheckedFolder = settingsFolder.getBoolean("cbx_ischeckedFolder", false);
                checkBoxFolder.setChecked(isCheckedFolder);

                if(checkBoxFolder.isChecked()) {
                    String dirName = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).
                            getString("MYLABEL", "defaultStringIfNothingFound");
                    String filenameGLOBAL;
                    filenameGLOBAL = (targetPath + "/" + dirName + "/" + System.currentTimeMillis() + ".jpg");
                    File f = new File(filenameGLOBAL);
                    Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    //mMakePhotoUri = Uri.fromFile(f);

                    mMakePhotoUri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".fileprovider", f);

                    i.putExtra(MediaStore.EXTRA_OUTPUT, mMakePhotoUri);
                    startActivityForResult(i, 1888);
                }

                else {
                    //     String filenameGLOBAL;
                    //   filenameGLOBAL = (targetPath + "/" + System.currentTimeMillis() + ".jpg");
                    //     File f = new File(filenameGLOBAL);
                    Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    File f =getOutputMediaFile(1);

                    //mMakePhotoUri = Uri.fromFile(f);
                    mMakePhotoUri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".fileprovider", f);

                    i.putExtra(MediaStore.EXTRA_OUTPUT, mMakePhotoUri);
                    startActivityForResult(i, 1888);
                    //   startActivityForResult(cameraIntent, 1888);
                }
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        //ViewGroup linearLayout = (ViewGroup) findViewById(R.id.drawer_layout);

    /*    Button btnGridView = new Button(this);
        btnGridView.setText("Gallery");
        btnGridView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        btnGridView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GridView2Activity/*PicassoGalleryActivity*///.class);
    /*            startActivity(intent);
            }
        });
        linearLayout.addView(btnGridView);*/


        /*Button btnExtSd = new Button(this);
        btnExtSd.setText("ExtSdCard Photo");
        btnExtSd.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        Button btnSd = new Button(this);
        btnSd.setText("SdCard Photo");
        btnSd.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));*/

        /*btnExtSd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenExtSdCardPhoto(v);
            }
        });*/

        /*btnSd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenSdCardPhoto(v);
            }
        });*/

        String sec_storage = System.getenv("SECONDARY_STORAGE");

        if(sec_storage == null){
            //non è presente sd card esterna
            targetPath = System.getenv("EXTERNAL_STORAGE") + "/DCIM/Camera";
            STATE = 0;
        }
        else  {
            targetPath = System.getenv("SECONDARY_STORAGE") + "/DCIM/Camera";
            STATE = 1;
            //Controllo se nella sd interna ci sono file
            /*String ext_storage = System.getenv("EXTERNAL_STORAGE");
            String targetPath = ext_storage + "/DCIM/Camera";
            File targetDirector = new File(targetPath);
            final File[] files = targetDirector.listFiles();
            if(files.length > 0){
                linearLayout.addView(btnSd);
            }
            linearLayout.addView(btnExtSd);*/

        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.searchPeers) {
            Intent intent = new Intent(this, SearchPeersActivity.class);
            startActivity(intent);
            return true;
        }

        if(id == R.id.enableWiFi){
            WifiManager wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            boolean wifiEnabled = wifiManager.isWifiEnabled();
            if(wifiEnabled){
                return true;
            }
            else {
                wifiManager.setWifiEnabled(true);
            }
        }

        if (id == R.id.settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    OpenSdCardPhoto(item);
                    return true;
                }
            });
            return true;
        } else if (id == R.id.nav_gallery_ext) {
            String sec_storage = System.getenv("SECONDARY_STORAGE");
            if(sec_storage == null){
                Toast.makeText(getApplicationContext(), "Ext SD card not present", Toast.LENGTH_SHORT).show();
            } else  {
                item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        OpenExtSdCardPhoto(item);
                        return true;
                    }
                });
            }

            return true;
        } else if (id == R.id.nav_manage) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        LoadPreferences();
    }


    private void LoadPreferences() {
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        boolean checkBox = sharedPreferences.getBoolean("checkBox", false);
    }

    /** Called when the user clicks the imageButtonExt */
    public void OpenExtSdCardPhoto(MenuItem item) {
        Intent intent = new Intent(this, ExtSdCardActivity.class);
        startActivity(intent);
    }

    public void OpenSdCardPhoto(MenuItem item){
        /*String sec_storage = System.getenv("SECONDARY_STORAGE");
        Class sdCardClass;
        if(sec_storage == null){
            sdCardClass = SdCardActivity.class;
        } else  {
            targetPath = System.getenv("SECONDARY_STORAGE") + "/DCIM/Camera";
            STATE = 1;
            sdCardClass = ExtSdCardActivity.class;
        }*/

        Intent intent = new Intent(this, SdCardActivity.class);
        startActivity(intent);
    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height/ (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;      }       final float totalPixels = width * height;       final float totalReqPixelsCap = reqWidth * reqHeight * 2;       while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }

    public String getFilename() {
        File file = new File(Environment.getExternalStorageDirectory().getPath(), "MyFolder/Images");
        if (!file.exists()) {
            file.mkdirs();
        }
        String uriSting = (file.getAbsolutePath() + "/" + System.currentTimeMillis() + ".jpg");
        return uriSting;
    }

    private String getRealPathFromURI(Uri contentURI) {
        //Uri contentUri = Uri.parse(contentURI);
        /*Cursor cursor = getContentResolver().query(contentUri, null, null, null, null);
        if (cursor == null) {
            return contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(index);
        }*/

        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    public String compressImage(String filePath) {

        //String filePath = getRealPathFromURI(imageUri);
        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

//      max Height and width values of the compressed image is taken as 816x612

        float maxHeight = 816.0f;
        float maxWidth = 612.0f;
        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;

//      width and height values are set maintaining the aspect ratio of the image

        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {               imgRatio = maxHeight / actualHeight;                actualWidth = (int) (imgRatio * actualWidth);               actualHeight = (int) maxHeight;             } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;

            }
        }

//      setting inSampleSize value allows to load a scaled down version of the original image

        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

//      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;

//      this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
//          load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();

        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight,Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

//      check the rotation of the image and display it properly
        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath);

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 3) {
                matrix.postRotate(180);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 8) {
                matrix.postRotate(270);
                Log.d("EXIF", "Exif: " + orientation);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                    true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream out = null;
        String filename = getFilename();
        try {
            out = new FileOutputStream(filename);
//          write the compressed bitmap at the destination specified by filename.
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return filename;
    }



    public String getImgPath(Uri uri) {
        String[] largeFileProjection = { MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.DATA };
        String largeFileSort = MediaStore.Images.ImageColumns._ID + " DESC";
        Cursor myCursor = this.managedQuery(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                largeFileProjection, null, null, largeFileSort);
        String largeImagePath = "";
        try {
            myCursor.moveToFirst();
            largeImagePath = myCursor
                    .getString(myCursor
                            .getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA));
        } finally {
            myCursor.close();
        }
        return largeImagePath;
    }

    private static final String TAG = "MainActivity";
    static {
        if(!OpenCVLoader.initDebug()){
            Log.d(TAG, "OpenCV not loaded");
        } else {
            Log.d(TAG, "OpenCV loaded");
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        CheckBox checkBoxShare = new CheckBox(this);
        SharedPreferences settingsShare = getSharedPreferences("preferenceShare", 0);
        Boolean isCheckedShare = settingsShare.getBoolean("cbx_ischeckedShare", false);
        checkBoxShare.setChecked(isCheckedShare);

        if (requestCode == 1888 && resultCode == RESULT_OK && checkBoxShare.isChecked()) {
            //Bitmap photo = (Bitmap) data.getExtras().get("data");
            Uri uriImage = mMakePhotoUri;

            //System.loadLibrary("opencv_java");

            /*String ext_storage = System.getenv("SECONDARY_STORAGE");
            String targetPath = ext_storage + "/DCIM/Camera";*/
            File targetDirector = new File(targetPath);
            final File[] files = targetDirector.listFiles();

            Arrays.sort(files, new Comparator<File>() {
                public int compare(File f1, File f2) {
                    return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
                }
            });

            //String last = getImgPath(uriImage);
            String last = "/sdcard" + uriImage.getPath(); //getRealPathFromURI(uriImage);//.toString());
            String preLast = null;

            if(!files[1].isDirectory()){
                preLast = files[1].getAbsolutePath();
            }
            else {
                int i = 2;
                while(files[i].isDirectory()){
                    i++;
                }
                preLast = files[i].getAbsolutePath();
            }
            double res = compare(preLast, last);
            Toast.makeText(getApplicationContext(), "Soglia: " + res, Toast.LENGTH_SHORT).show();

            if(res < 0.9) {

                //String compressFile = compressImage(last);//uriString);
                //we can check if "compress image" preference is enabled or not
                File file = new File(last);//compressFile);
                Uri uri = Uri.fromFile(file);


                Intent serviceIntent = new Intent(this, FileTransferService.class);

                for (int i = 0; i < DeviceDetailFragment.clientListIP.size(); i++) {

                    serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
                    serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, uri.toString()); //uriImage.toString());
                    serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
                            DeviceDetailFragment.clientListIP.get(i));
                    serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, 8988);
                    startService(serviceIntent);
                }
            }
            else
            {
                Toast.makeText(getApplicationContext(), "Foto simile" , Toast.LENGTH_SHORT).show();
            }
        }
    }

    public double  compare(String fileName1, String fileName2) {
        Mat image0 = imread(fileName1);
        Mat image1 = imread(fileName2);

        Mat hist0 = new Mat();
        Mat hist1 = new Mat();

        int hist_bins = 30;           //number of histogram bins
        int hist_range[]= {0,180};//histogram range
        MatOfFloat ranges = new MatOfFloat(0f, 256f);
        MatOfInt histSize = new MatOfInt(25);

        Imgproc.calcHist(Arrays.asList(image0), new MatOfInt(0), new Mat(), hist0, histSize, ranges);
        Imgproc.calcHist(Arrays.asList(image1), new MatOfInt(0), new Mat(), hist1, histSize, ranges);

        double res = Imgproc.compareHist(hist0, hist1, Imgproc.CV_COMP_CORREL);

        return res;
    }

    /** Create a File for saving an image */
    private  File getOutputMediaFile(int type){
        //File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
        // Environment.DIRECTORY_PICTURES), "MyApplication");

        /**Create the storage directory if it does not exist*/
     /*   if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }*/

        /**Create a media file name*/
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == 1){
            mediaFile = new File(targetPath + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }
}
