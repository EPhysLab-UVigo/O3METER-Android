/*
 *   This file is part of O₃METER.
 *
 *   O₃METER is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   O₃METER is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with O₃METER.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.apercloud.o3meter;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;


/**
 * Main activity.
 *
 * This activity is used as landing activity and where we can select between 2
 * options to obtain photographs to process
 *
 * @author AperCloud
 * @version 2018.0226
 */
public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "";
    private static final int PERMISSIONS_ALL = 1;

    /**
     * Constructor
     *
     * Initialize main activity and if SKD version < Lollipop (API 23) then
     * disable buttons until permissions check
     *
     * @param savedInstanceState Bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ImageButton cameraBtn = findViewById(R.id.btnCamera);
        ImageButton galleryBtn = findViewById(R.id.btnGallery);
        if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            cameraBtn.setEnabled(false);
            galleryBtn.setEnabled(false);
            requestAppPermissions();
        }
    }

    /**
     * Menu constructor
     *
     * @param menu Menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Method to handle action bar item clicks
     *
     * @param item MenuItem: action bar item where user clicked
     * @return <code>true</code>
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Method to handle the click on the button to obtain photo by camera
     *
     * @param view View: The view that was clicked.
     */
    public void cameraPhoto(View view) {
        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra(EXTRA_MESSAGE, "Camera");
        startActivity(intent);
    }

    /**
     * Method to handle the click on the button to obtain photo by gallery
     *
     * @param view View: The view that was clicked.
     */
    public void galleryPhoto(View view) {
        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra(EXTRA_MESSAGE, "Gallery");
        startActivity(intent);
    }

    /**
     * Method to request necessary App permissions to operate
     *
     * @since Android 6.0 (API 23)
     */
    private void requestAppPermissions() {
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }

        //If permissions granted, buttons are enabled
        if (hasCameraPermissions() && hasReadPermissions() && hasWritePermissions()) {
            ImageButton cameraBtn = findViewById(R.id.btnCamera);
            cameraBtn.setEnabled(true);
            ImageButton galleryBtn = findViewById(R.id.btnGallery);
            galleryBtn.setEnabled(true);
            return;
        }

        ActivityCompat.requestPermissions(this,
                new String[] {
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, PERMISSIONS_ALL); // your request code
    }

    /**
     * Method that verifies if we have permission to use the camera
     *
     * @return <code>true</code> if permission granted;
     *         <code>false</code> otherwise
     */
    private boolean hasCameraPermissions() {
        return (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED);
    }

    /**
     * Method that verifies if we have permission to read external storage
     *
     * @return <code>true</code> if permission granted;
     *         <code>false</code> otherwise
     */
    private boolean hasReadPermissions() {
        return (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    /**
     * Method that verifies if we have permission to write to external storage
     *
     * @return <code>true</code> if permission granted;
     *         <code>false</code> otherwise
     */
    private boolean hasWritePermissions() {
        return (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    /**
     * Called when permissions where requested
     *
     * @param requestCode int The request code passed in requestPermissions
     * @param permissions String[] The requested permissions. Never null.
     * @param grantResults int[] The grant results for the corresponding
     *                     permissions which is either PERMISSION_GRANTED
     *                     or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_ALL: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length == 3) {
                    if(grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                            grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                        ImageButton galleryBtn = findViewById(R.id.btnGallery);
                        galleryBtn.setEnabled(true);
                        if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                            ImageButton cameraBtn = findViewById(R.id.btnCamera);
                            cameraBtn.setEnabled(true);
                        }
                    }
                }
            }
        }
    }
}
