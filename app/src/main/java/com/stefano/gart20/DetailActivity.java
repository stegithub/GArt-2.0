package com.stefano.gart20;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;


public class DetailActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

    /*    Intent intent = getIntent();
        Uri uri = Uri.parse(intent.getStringExtra("image"));*/
        ImageView imageView = (ImageView) findViewById(R.id.imageView);

   //     imageView.setLayoutParams( new ViewGroup.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT));

        //   Bitmap bitmap = BitmapFactory.decodeFile(pathFile);


        imageView.setScaleType(ImageView.ScaleType.FIT_XY);

   //     imageView.setImageURI(uri);

     //   String title = getIntent().getStringExtra("title");
        Intent intent = getIntent();

        String pathFile = intent.getStringExtra("image");
        //Bitmap bitmap = getIntent().getParcelableExtra("image");
        Bitmap bitmap = BitmapFactory.decodeFile(pathFile);


    //    TextView titleTextView = (TextView) findViewById(R.id.title);
    //    titleTextView.setText(title);

    //    ImageView imageView = (ImageView) findViewById(R.id.image);
        imageView.setImageBitmap(bitmap);

    }


 /*   @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
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
