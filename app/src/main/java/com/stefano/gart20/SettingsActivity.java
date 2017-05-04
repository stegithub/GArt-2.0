package com.stefano.gart20;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;


public class SettingsActivity extends AppCompatActivity {

    public static String folderName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ViewGroup linearLayout = (ViewGroup) findViewById(R.id.settings);

        final CheckBox checkBoxShare = new CheckBox(this);

        SharedPreferences settingsShare = getSharedPreferences("preferenceShare", 0);
        Boolean isCheckedShare = settingsShare.getBoolean("cbx_ischeckedShare", false);
        checkBoxShare.setChecked(isCheckedShare );

        checkBoxShare.setText("Instant peers share");
        checkBoxShare.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        linearLayout.addView(checkBoxShare);

        checkBoxShare.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
                // TODO Auto-generated method stub
                SharedPreferences.Editor editor = getSharedPreferences("preferenceShare", 0).edit();
                editor.putBoolean("cbx_ischeckedShare", isChecked);
                editor.apply();
            }
        });



        final CheckBox checkBoxFolder = new CheckBox(this);

        SharedPreferences settingsFolder = getSharedPreferences("preferenceFolder", 0);
        Boolean isCheckedFolder = settingsFolder.getBoolean("cbx_ischeckedFolder", false);
        checkBoxFolder.setChecked(isCheckedFolder);

        checkBoxFolder.setText("Save in Folder");
        checkBoxFolder.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        linearLayout.addView(checkBoxFolder);

        checkBoxFolder.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
                // TODO Auto-generated method stub
                SharedPreferences.Editor editor = getSharedPreferences("preferenceFolder", 0).edit();
                editor.putBoolean("cbx_ischeckedFolder", isChecked);
                editor.apply();

                if (checkBoxFolder.isChecked()) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(SettingsActivity.this);
                    alert.setTitle("Folder name");

                    // Set an EditText view to get user input
                    final EditText input = new EditText(SettingsActivity.this);
                    alert.setView(input);

                    alert.setPositiveButton("Create", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                             folderName = input.getText().toString();

                            PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this).edit()
                                    .putString("MYLABEL", folderName).apply();

                             createDirIfNotExists(folderName);
                        }
                    });

                    alert.setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    checkBoxFolder.setChecked(false);
                                }
                            });

                    alert.show();
                }
            }
        });
    }


    public boolean createDirIfNotExists(String path) {
        boolean ret = true;

        String sec_storage = System.getenv("SECONDARY_STORAGE");
        String ext_storage = System.getenv("EXTERNAL_STORAGE");

        if (MainActivity.STATE == 1) {
            File file = new File(sec_storage + "/DCIM/Camera/", path);
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    Log.e("TravellerLog :: ", "Problem creating Image folder");
                    Toast.makeText(getApplicationContext(), "Cartella non creata " , Toast.LENGTH_SHORT).show();
                    ret = false;
                }
            }
            return ret;
        }
        else
        {
            File file = new File(ext_storage + "/DCIM/Camera/", path);
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    Log.e("TravellerLog :: ", "Problem creating Image folder");
                    Toast.makeText(getApplicationContext(), "Cartella non creata " , Toast.LENGTH_SHORT).show();
                    ret = false;
                }
            }
            return ret;
        }
    }
}
