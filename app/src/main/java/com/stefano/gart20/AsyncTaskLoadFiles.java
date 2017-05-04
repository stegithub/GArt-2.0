package com.stefano.gart20;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by stefano on 06/05/2015.
 */
public class AsyncTaskLoadFiles extends AsyncTask<Void, String, Void> {

    private Context mContext;

    File targetDirector;
    ImageAdapter myTaskAdapter;

    String myPath;
    public static File[] files = null;

    public AsyncTaskLoadFiles(ImageAdapter adapter, String path, Context context) {
        myTaskAdapter = adapter;
        myPath = path;
        mContext = context;
    }

    @Override
    protected void onPreExecute() {

        targetDirector = new File(myPath);

        files = targetDirector.listFiles();

        if(files == null) {
            Toast.makeText(mContext, "Permission denied", Toast.LENGTH_SHORT).show();
            return;
        }

        if(files.length == 0){
            this.cancel(true);
            Toast.makeText(mContext, "Cartella vuota", Toast.LENGTH_SHORT).show();
        }
        /*files = targetDirector.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return (name.endsWith(".jpg") || name.endsWith(".JPG"));
            }
        });*/
        Arrays.sort(files, new Comparator<File>() {
            public int compare(File f1, File f2) {
                return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
            }
        });

        myTaskAdapter.clear();

        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... params) {

        //open jpg only
     /*   File[] files = targetDirector.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return (name.endsWith(".jpg") || name.endsWith(".JPG"));
            }
        });*/
        //File[] files = targetDirector.listFiles();

        //Arrays.sort(files);
        for (File file : files) {
            publishProgress(file.getAbsolutePath());
            if (isCancelled()) break;
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        myTaskAdapter.add(values[0]);
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(Void result) {
        myTaskAdapter.notifyDataSetChanged();
        super.onPostExecute(result);
    }
}
