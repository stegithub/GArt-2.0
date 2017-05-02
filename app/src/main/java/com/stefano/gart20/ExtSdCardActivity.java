package com.stefano.gart20;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.stefano.gart20.WiFi.DeviceDetailFragment;
import com.stefano.gart20.WiFi.FileTransferService;

import java.io.File;

public class ExtSdCardActivity extends AppCompatActivity {

    public static int screenWidth;
    public static int screenHeight;

    AsyncTaskLoadFiles myAsyncTaskLoadFiles;

    String sec_storage = System.getenv("SECONDARY_STORAGE");
    String targetPath = sec_storage + "/DCIM/Camera";

    File targetDirector = new File(targetPath);
    //final File[] files = targetDirector.listFiles();

    ImageAdapter imageAdapter;

    /*static final int ID_JPGDIALOG = 0;
    ImageView jpgdialigImage;
    TextView jpgdialigText;
    File jpgdialigFile;
    //have to match width and height of
    //"@+id/image" in jpgdialog.xml
    final int DIALOG_IMAGE_WIDTH = 500;
    final int DIALOG_IMAGE_HEIGHT = 350;*/

    GridView gridView = null;


    private NfcAdapter nfcAdapter;

    public void sendFile(String fileName) {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        // Check whether NFC is enabled on device
        if(!nfcAdapter.isEnabled()){
            // NFC is disabled, show the settings UI
            // to enable NFC
            Toast.makeText(this, "Please enable NFC.",
                    Toast.LENGTH_SHORT).show();
            startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
        }
        // Check whether Android Beam feature is enabled on device
        else if(!nfcAdapter.isNdefPushEnabled()) {
            // Android Beam is disabled, show the settings UI
            // to enable Android Beam
            Toast.makeText(this, "Please enable Android Beam.",
                    Toast.LENGTH_SHORT).show();
            startActivity(new Intent(Settings.ACTION_NFCSHARING_SETTINGS));
        }
        else {
            // NFC and Android Beam both are enabled

            // Retrieve the path to the user's public pictures directory

            // Create a new file using the specified directory and name
            File fileToTransfer = new File(fileName);
            fileToTransfer.setReadable(true, false);

            nfcAdapter.setBeamPushUris(
                    new Uri[]{Uri.fromFile(fileToTransfer)}, this);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ext_sd_card);


        PackageManager pm = this.getPackageManager();
        // Check whether NFC is available on device
        if (!pm.hasSystemFeature(PackageManager.FEATURE_NFC)) {
            // NFC is not available on the device.
            Toast.makeText(this, "The device does not has NFC hardware.",
                    Toast.LENGTH_SHORT).show();
        }
        // Check whether device is running Android 4.1 or higher
        else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            // Android Beam feature is not supported.
            Toast.makeText(this, "Android Beam is not supported.",
                    Toast.LENGTH_SHORT).show();
        }
        else {
            // NFC and Android Beam file transfer is supported.
            Toast.makeText(this, "Android Beam is supported on your device.",
                    Toast.LENGTH_SHORT).show();
        }


        gridView = (GridView)findViewById(R.id.gridView);
        imageAdapter = new ImageAdapter(this);
        gridView.setAdapter(imageAdapter);

        myAsyncTaskLoadFiles = new AsyncTaskLoadFiles(imageAdapter, targetPath, ExtSdCardActivity.this);
        myAsyncTaskLoadFiles.execute();

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

                String pathFile = imageAdapter.imageList.get(position); //files[position].getAbsolutePath();

                //   Open dialog to show jpg
                //      jpgdialigFile = new File(pathFile);
                //   showDialog(ID_JPGDIALOG);

                if(pathFile.endsWith(".jpg") || pathFile.endsWith("JPG")) {

                    // Get screen size
                    Display display = ExtSdCardActivity.this.getWindowManager().getDefaultDisplay();
                    Point size = new Point();
                    display.getSize(size);
                    screenWidth = size.x;
                    screenHeight = size.y;

                    // Get target image size
                    Bitmap bitmap = BitmapFactory.decodeFile(pathFile);


                    BitmapDrawable resizedBitmap = null;
                    File file = new File(pathFile);
                    int orientation = getCameraPhotoOrientation(ExtSdCardActivity.this, Uri.fromFile(file), pathFile);
                    if (orientation != 0) {
                        Matrix matrix = new Matrix();
                        matrix.postRotate(orientation);
                        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), true);
                        Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(),
                                scaledBitmap.getHeight(), matrix, true);

                        int bitmapHeight = rotatedBitmap.getHeight();
                        int bitmapWidth = rotatedBitmap.getWidth();

                        // Scale the image down to fit perfectly into the screen
                        // The value (250 in this case) must be adjusted for phone/tables displays
                        while (bitmapHeight > (screenHeight - 1) || bitmapWidth > (screenWidth - 1)) {
                            bitmapHeight = bitmapHeight / 2;
                            bitmapWidth = bitmapWidth / 2;
                        }

                        // Create resized bitmap image
                        resizedBitmap = new BitmapDrawable(ExtSdCardActivity.this.getResources(),
                                Bitmap.createScaledBitmap(rotatedBitmap, bitmapWidth, bitmapHeight, false));
                    }
                /*Matrix matrix = new Matrix();
                matrix.postRotate(270);
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap,bitmap.getWidth(),bitmap.getHeight(),true);
                Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap , 0, 0, scaledBitmap .getWidth(), scaledBitmap .getHeight(), matrix, true);
*/
                    else {
                        int bitmapHeight = bitmap.getHeight();
                        int bitmapWidth = bitmap.getWidth();

                        // Scale the image down to fit perfectly into the screen
                        // The value (250 in this case) must be adjusted for phone/tables displays
                        while (bitmapHeight > (screenHeight - 1) || bitmapWidth > (screenWidth - 1)) {
                            bitmapHeight = bitmapHeight / 2;
                            bitmapWidth = bitmapWidth / 2;
                        }

                        // Create resized bitmap image
                        resizedBitmap = new BitmapDrawable(ExtSdCardActivity.this.getResources(),
                                Bitmap.createScaledBitmap(bitmap, bitmapWidth, bitmapHeight, false));

                    }
                    // Create dialog
                    Dialog dialog = new Dialog(ExtSdCardActivity.this);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.activity_detail);

                    ImageView image = (ImageView) dialog.findViewById(R.id.imageView);

                    // !!! Do here setBackground() instead of setImageDrawable() !!! //
                    image.setBackground(resizedBitmap);

                    // Without this line there is a very small border around the image (1px)
                    // In my opinion it looks much better without it, so the choice is up to you.
                    dialog.getWindow().setBackgroundDrawable(null);

                    // Show the dialog
                    dialog.show();
                }

                else {
                    String dirName = AsyncTaskLoadFiles.files[position].getName();
                    String newPath = targetPath + "/" + dirName;

                    myAsyncTaskLoadFiles.cancel(true);
                    //new another ImageAdapter, to prevent the adapter have
                    //mixed files
                    imageAdapter = new ImageAdapter(ExtSdCardActivity.this);
                    gridView.setAdapter(imageAdapter);
                    myAsyncTaskLoadFiles = new AsyncTaskLoadFiles(imageAdapter, newPath, ExtSdCardActivity.this);
                    myAsyncTaskLoadFiles.execute();
                }
            }
        });

        registerForContextMenu(gridView);
    }


    public static int getCameraPhotoOrientation(Context context, Uri imageUri, String imagePath){
        int rotate = 0;
        try {
            context.getContentResolver().notifyChange(imageUri, null);
            File imageFile = new File(imagePath);
            ExifInterface exif = new ExifInterface(
                    imageFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
            Log.v("Orientation", "Exif orientation: " + orientation);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rotate;
    }


    /*@Override
    @Deprecated
    protected Dialog onCreateDialog(int id) {
        final Dialog jpgDialog = new Dialog(this,android.R.style.Theme_Black_NoTitleBar_Fullscreen);//Dialog(this);

        switch(id){
            case ID_JPGDIALOG:

                jpgDialog.setContentView(R.layout.activity_detail);
                jpgdialigImage = (ImageView)jpgDialog.findViewById(R.id.imageView);

                break;

            default:
                break;
        }

        return jpgDialog;
    }

    @Override
    @Deprecated
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch(id){
            case ID_JPGDIALOG:
              //  jpgdialigText.setText(jpgdialigFile.getPath());
                //Bitmap bm = BitmapFactory.decodeFile(jpgdialigFile.getPath());

                Bitmap bm = null;

                bm = imageAdapter.decodeSampledBitmapFromUri(jpgdialigFile.getPath(),
                        DIALOG_IMAGE_WIDTH, DIALOG_IMAGE_HEIGHT);
                jpgdialigImage.setImageBitmap(bm);

                break;

            default:
                break;
        }
    }*/


    /** This will be invoked when an item in the listview is long pressed */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.action, menu);
    }

    /** This will be invoked when a menu item is selected */
    @Override
    public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        int position = info.position;
        File file = AsyncTaskLoadFiles.files[position];

        String pathFile = file.getAbsolutePath();

        switch(item.getItemId()){
            case R.id.cnt_mnu_delete:
                Toast.makeText(this, "Delete : " + position  , Toast.LENGTH_SHORT).show();
                boolean deleted = file.delete();
                if(deleted){
                   imageAdapter.imageList.remove(position);
                   imageAdapter.notifyDataSetChanged();
                   //Cancel the previous running task, if exist.
                   myAsyncTaskLoadFiles.cancel(true);
                   //new another ImageAdapter, to prevent the adapter have
                   //mixed files
                   imageAdapter = new ImageAdapter(ExtSdCardActivity.this);
                   gridView.setAdapter(imageAdapter);
                   myAsyncTaskLoadFiles = new AsyncTaskLoadFiles(imageAdapter, targetPath, ExtSdCardActivity.this);
                   myAsyncTaskLoadFiles.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                   }
                break;
            case R.id.cnt_mnu_share:
                sendFile(pathFile);
                Toast.makeText(this, "Share : " + position  , Toast.LENGTH_SHORT).show();
                break;
            case R.id.cnt_mnu_shareWiFi:

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

                Toast.makeText(this, "Share : " + position  , Toast.LENGTH_SHORT).show();
                break;

        }
        return true;
    }
}
