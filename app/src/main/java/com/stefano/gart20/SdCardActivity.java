package com.stefano.gart20;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.ContextMenu;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.stefano.gart20.WiFi.DeviceDetailFragment;
import com.stefano.gart20.WiFi.FileTransferService;

import java.io.File;

import static com.stefano.gart20.ExtSdCardActivity.getCameraPhotoOrientation;


public class SdCardActivity extends Activity {

    public static int screenWidth;
    public static int screenHeight;

    AsyncTaskLoadFiles myAsyncTaskLoadFiles;

    String ext_storage = System.getenv("EXTERNAL_STORAGE");
    String targetPath = ext_storage + "/DCIM/Camera";

    File targetDirector = new File(targetPath);
   // final File[] files = targetDirector.listFiles();

    // private GridView gridView;
    // private GridViewAdapter gridAdapter;

    ImageAdapter imageAdapter;

    static final int ID_JPGDIALOG = 0;
    ImageView jpgdialigImage;
    TextView jpgdialigText;
    File jpgdialigFile;
    //have to match width and height of
    //"@+id/image" in jpgdialog.xml
    final int DIALOG_IMAGE_WIDTH = 500;
    final int DIALOG_IMAGE_HEIGHT = 350;

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

            // File to be transferred
            // For the sake of this tutorial I've placed an image
            // named 'wallpaper.png' in the 'Pictures' directory
            //String fileName = "wallpaper.png";

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
        setContentView(R.layout.activity_sd_card);


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


        gridView = (GridView) findViewById(R.id.gridView);
        imageAdapter = new ImageAdapter(this);
        gridView.setAdapter(imageAdapter);

        myAsyncTaskLoadFiles = new AsyncTaskLoadFiles(imageAdapter, targetPath, SdCardActivity.this);
        myAsyncTaskLoadFiles.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

                String pathFile = imageAdapter.imageList.get(position);
                if(pathFile.endsWith(".jpg") || pathFile.endsWith("JPG")) {

                    // Get screen size
                    Display display = SdCardActivity.this.getWindowManager().getDefaultDisplay();
                    Point size = new Point();
                    display.getSize(size);
                    screenWidth = size.x;
                    screenHeight = size.y;

                    // Get target image size
                    Bitmap bitmap = BitmapFactory.decodeFile(pathFile);


                    BitmapDrawable resizedBitmap = null;
                    File file = new File(pathFile);
                    int orientation = getCameraPhotoOrientation(SdCardActivity.this, Uri.fromFile(file), pathFile);
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
                        resizedBitmap = new BitmapDrawable(SdCardActivity.this.getResources(),
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
                        resizedBitmap = new BitmapDrawable(SdCardActivity.this.getResources(),
                                Bitmap.createScaledBitmap(bitmap, bitmapWidth, bitmapHeight, false));

                    }
                    // Create dialog
                    Dialog dialog = new Dialog(SdCardActivity.this);
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
                    imageAdapter = new ImageAdapter(SdCardActivity.this);
                    gridView.setAdapter(imageAdapter);
                    myAsyncTaskLoadFiles = new AsyncTaskLoadFiles(imageAdapter, newPath, SdCardActivity.this);
                    myAsyncTaskLoadFiles.execute();
                }
            }
        });

        registerForContextMenu(gridView);

    }


    /*@Override
    @Deprecated
    protected Dialog onCreateDialog(int id) {
        final Dialog jpgDialog = new Dialog(this,android.R.style.Theme_Black_NoTitleBar_Fullscreen);//Dialog(this);
        switch(id){
            case ID_JPGDIALOG:

                jpgDialog.setContentView(R.layout.activity_detail);
                jpgdialigImage = (ImageView)jpgDialog.findViewById(R.id.imageView);
                //   jpgdialigText = (TextView)jpgDialog.findViewById(R.id.textpath);

           *//*     Button okDialogButton = (Button)jpgDialog.findViewById(R.id.okdialogbutton);
                okDialogButton.setOnClickListener(new View.OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        jpgDialog.dismiss();
                    }});*//*
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
                Bitmap bm = imageAdapter.decodeSampledBitmapFromUri(jpgdialigFile.getPath(),
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
                    imageAdapter = new ImageAdapter(SdCardActivity.this);
                    gridView.setAdapter(imageAdapter);
                    myAsyncTaskLoadFiles = new AsyncTaskLoadFiles(imageAdapter, targetPath, SdCardActivity.this);
                    myAsyncTaskLoadFiles.execute();
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

  /*  @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sd_card, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/
}
