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

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.graphics.Color.BLACK;
import static java.lang.Math.abs;


/**
 * Result activity.
 *
 * In this activity the results are displayed. It also have tools to improve the results, like
 * zooming the photo or selecting parts of the photo to analyze.
 *
 * @author AperCloud
 * @version 2018.0226
 */
public class ResultActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 2;
    static final int REQUEST_IMAGE_PICK = 1;
    String mCurrentPhotoPath;

    /**
     * Constructor
     *
     * Initialize result activity, check if camera or gallery where choosen and launch intent,
     * set zoom buttons functionality, load photo from intent to ImageView and analize it
     *
     * @param savedInstanceState Bundle
     * @throws IOException if can't create file from camera photo
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

        if(message.equals("Gallery")) {
            Intent intentGallery = new Intent();
            // Show only images, no videos or anything else
            intentGallery.setType("image/*");
            intentGallery.setAction(Intent.ACTION_GET_CONTENT);
            // Always show the chooser (if there are multiple options available)
            startActivityForResult(Intent.createChooser(intentGallery, "Select Picture"),
                    REQUEST_IMAGE_PICK);
        } else if(message.equals("Camera")) {
            Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // Ensure that there's a camera activity to handle the intent
            if (intentCamera.resolveActivity(getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    // Error occurred while creating the File
                    ex.printStackTrace();
                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    Uri photoURI;
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT){
                        photoURI = Uri.fromFile(photoFile);
                    } else {
                        photoURI = FileProvider.getUriForFile(this,
                                getPackageName() + ".fileprovider",
                                photoFile);
                    }
                    intentCamera.putExtra("return-data", true);
                    intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(intentCamera, REQUEST_IMAGE_CAPTURE);
                }
            }
        }

        //Zoom in button
        final ImageButton zoomIn = findViewById(R.id.btnZoomPlus);
        zoomIn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ImageView photoView = findViewById(R.id.photoView);

                float x = photoView.getScaleX();
                float y = photoView.getScaleY();

                //Scale photo
                photoView.setScaleX((float) (x+0.1));
                photoView.setScaleY((float) (y+0.1));

                DragRectView dragRect = findViewById(R.id.dragRect);

                float xRect = dragRect.getScaleX();
                float yRect = dragRect.getScaleY();

                //Scale selection rectangle area
                dragRect.setScaleX((float) (xRect+0.1));
                dragRect.setScaleY((float) (yRect+0.1));
            }
        });
        //Zoom out button
        final ImageButton zoomOut = findViewById(R.id.btnZoomMinus);
        zoomOut.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ImageView photoView = findViewById(R.id.photoView);

                float x = photoView.getScaleX();
                float y = photoView.getScaleY();

                //Scale photo
                photoView.setScaleX((float) (x-0.1));
                photoView.setScaleY((float) (y-0.1));

                DragRectView dragRect = findViewById(R.id.dragRect);

                float xRect = dragRect.getScaleX();
                float yRect = dragRect.getScaleY();

                //Scale selection rectangle area
                dragRect.setScaleX((float) (xRect-0.1));
                dragRect.setScaleY((float) (yRect-0.1));
            }
        });
        //Zoom original. Set original dimensions
        final ImageButton zoomOrig = findViewById(R.id.btnZoomOriginal);
        zoomOrig.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ImageView photoView = findViewById(R.id.photoView);

                //Scale photo
                photoView.setScaleX(1.0f);
                photoView.setScaleY(1.0f);

                DragRectView dragRect = findViewById(R.id.dragRect);

                //Scale selection rectangle area
                dragRect.setScaleX(1.0f);
                dragRect.setScaleY(1.0f);
            }
        });

        final DragRectView view = findViewById(R.id.dragRect);

        //If selection rectangle is set or changed, do all the steps
        if (null != view) {
            view.setOnUpCallback(new DragRectView.OnUpCallback() {
                @Override
                public void onRectFinished(final Rect rect) {
                    ImageView photoView = findViewById(R.id.photoView);
                    photoView.setDrawingCacheEnabled(true);
                    Bitmap bitmap = photoView.getDrawingCache();
                    analyzeImg(bitmap,rect.left,rect.top,rect.right,rect.bottom);
                }
            });
        }
    }

    /**
     * Called when an activity you launched exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     * The resultCode will be RESULT_CANCELED if the activity explicitly returned that,
     * didn't return any result, or crashed during its operation
     *
     * @param requestCode int: The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode int: The integer result code returned by the child
     *                   activity through its setResult().
     * @param data Intent: An Intent, which can return result data to the caller
     *             (various data can be attached to Intent "extras").
     * @throws IOException if get photo from external storage fails
     * @throws Exception if get photo from camera fails
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // this creates a MUTABLE bitmap
        Bitmap bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        super.onActivityResult(requestCode, resultCode, data);

        //If no photo selected or taken return to main activity
        if(resultCode == RESULT_CANCELED) {
            finish();
        }

        ImageView photoView = findViewById(R.id.photoView);
        photoView.destroyDrawingCache();
        //If photo from gallery
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null &&
                data.getData() != null) {
            Uri uri = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                photoView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        //If photo from camera
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            galleryAddPic();
            File f = new File(mCurrentPhotoPath);
            Uri uri = Uri.fromFile(f);
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                photoView.setImageBitmap(bitmap);
            } catch (Exception e) {
                Log.e("Camera", e.toString());
            }
        }

        int height = bitmap.getHeight();
        int width = bitmap.getWidth();

        //Analyze photo
        analyzeImg(bitmap,0,0,width,height);
    }

    /**
     * Method to analyze photo and get color and hue
     *
     * @param bitmap Bitmap: photo
     * @param left int: rect coordinates from left. Default is 0
     * @param top int: rect coordinates from top. Default is 0
     * @param right int: rect coordinates from right. Default is bitmap width
     * @param bottom int: rect coordinates from bottom. Default is bitmap height
     */
    private void analyzeImg(Bitmap bitmap, int left, int top, int right, int bottom) {
        int totalPixels = 1;
        int pixelSpacing = 3;
        int RED = 0; int GREEN = 0; int BLUE = 0;
        int height; int width;

        if(bottom-top < 1) {
            height = bitmap.getHeight();
        } else {
            height = bottom-top;
        }
        if(right-left < 1) {
            width = bitmap.getWidth();
        } else {
            width = right-left;
        }
        //Create bitmap with selection rectangle coordinates. Default original photo dimensions
        Bitmap bitmap2 = Bitmap.createBitmap(bitmap, abs(left), abs(top), abs(width), abs(height));
        int[] pixels = new int[bitmap2.getWidth() * bitmap2.getHeight()];
        bitmap2.getPixels(pixels, 0, bitmap2.getWidth(), 0, 0, bitmap2.getWidth(),
                bitmap2.getHeight());
        //Go over every pixel getting its color
        for (int i = 0; i < pixels.length; i += pixelSpacing) {
            int color = pixels[i];

            int r = Color.red(color);
            int g = Color.green(color);
            int b = Color.blue(color);
            if(r < 150 && g < 150 && b < 150) {
                RED += Color.red(color);
                GREEN += Color.green(color);
                BLUE += Color.blue(color);
                totalPixels++;
            }
        }

        //Convert RGB to HSV
        float[] hsv = new float[3];
        Color.RGBToHSV(RED / totalPixels, GREEN / totalPixels,
                BLUE / totalPixels, hsv);

        TextView valueView = findViewById(R.id.valueView);
        //If hue equals to 0 then we don't show any value and the color is 0 in scale
        if(hsv[0] == 0.0f) {
            valueView.setText("N/A");
            hsv[0] = 60;
        } else {
            valueView.setText(String.valueOf(HueToScale((int) hsv[0])));
        }

        //Convert Hue to RGB to use in ImageView and Gradient
        hsv[1] = 255;
        hsv[2] = 255;
        final int color = Color.HSVToColor(hsv);

        ImageView gradientView = findViewById(R.id.gradientView);

        //If gradient not initialized, wait until it has width to draw gradient and
        // ImageView with color
        if(gradientView.getWidth() == 0) {
            final ViewTreeObserver observer = gradientView.getViewTreeObserver();
            observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    drawGradientAndColor(color);
                }
            });
        //Else draw gradient and ImageView with color without waiting
        } else {
            drawGradientAndColor(color);
        }

    }

    /**
     * Method to change colorView's color and point in gradientView
     *
     * @param color int: result color after hueToScale conversion
     */
    private void drawGradientAndColor(int color) {
        //Change colorView's color
        ImageView colorView = findViewById(R.id.colorView);
        Bitmap bmp=Bitmap.createBitmap(colorView.getWidth(),colorView.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas1 = new Canvas(bmp);
        canvas1.drawColor(color);
        colorView.setImageBitmap(bmp);

        //Create gradient
        ImageView gradientView = findViewById(R.id.gradientView);
        GradientDrawable rainbow = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[] {Color.YELLOW, Color.RED, Color.MAGENTA, Color.BLUE});
        Bitmap bitmapGradient = Bitmap.createBitmap(gradientView.getWidth(),
                gradientView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmapGradient);
        rainbow.setBounds(0, 0, gradientView.getWidth(), gradientView.getHeight());
        rainbow.draw(canvas);

        gradientView.setImageBitmap(bitmapGradient);

        //Get color x position in gradient, with a distance between color and result of 5
        int posX = 0;
        for (int i = 0; i < bitmapGradient.getWidth(); i += 1) {
            int color2 = bitmapGradient.getPixel(i,1);
            if(colorDistance(color2,color) < 5) {
                posX = i;
                break;
            }
        }

        //Draw pointer in gradient
        for(int y = 120; y > 60; y--) {
            for(int x = 60; x < y; x++) {
                if(posX + x - 60 < bitmapGradient.getWidth()) {
                    bitmapGradient.setPixel(posX + x - 60, y, BLACK);
                }
                if(posX - x + 60 > 0) {
                    bitmapGradient.setPixel(posX - x + 60, y, BLACK);
                }
            }
        }
        gradientView.setImageBitmap(bitmapGradient);
    }

    /**
     * Method to calculate distance between 2 colors
     *
     * @param a int: first color
     * @param b int: second color
     * @return distance int
     */
    public int colorDistance(int a, int b) {
        return Math.abs(Color.red(a) - Color.red(b)) + Math.abs(Color.green(a) -
                Color.green(b)) + Math.abs(Color.blue(a) - Color.blue(b));
    }

    /**
     * Method to create file with personalized name format (Ozone_YearMonthDay_HourMinuteSecond.jpg)
     *
     * @return image File: file object of the new created file
     * @throws IOException if an IO exception occurred, for example with no write permissions
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "Ozone_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        storageDir.mkdirs();
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    /**
     * Method to save camera taken photo to public gallery and refresh it
     */
    private void galleryAddPic() {
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    /**
     * Method to make HUE <=> OZONE SCALE conversion.
     *
     * Hue values in a HSV representation can vary from 0 to 359. In our Ozone Scale
     * the hue of the samples can move from the yellow (no ozone at all) to blue
     * (max amount of ozone measurable). Hence our scale overlaps the HSV hue
     * values, so we should map HSV hue values to our Ozone Scale value. This is
     * values from 60 to 0 followed by values from 359 to 240 will be mapped to
     * range values [0, 180].
     *
     * Hue: 0-359
     * Scale: [60..0] & [359..240] ==> [0..180]
     *
     * @param hue int: hue value to convert
     */
    private int HueToScale(int hue) {
        if(hue <= 60) {
            return abs(hue - 60);
        }
        return abs(hue - 359) + 61;
    }
}
