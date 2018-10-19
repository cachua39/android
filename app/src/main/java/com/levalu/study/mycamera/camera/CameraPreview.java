package com.levalu.study.mycamera.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;


import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextRecognizer;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@SuppressWarnings("deprecation")
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback{

    private static final String TAG = "CameraPreview";
    private static final int FRAME_ID = 88;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    private SurfaceHolder mHolder;
    private Camera mCamera;
    float mDist = 0;        //Distance between 2 fingers


    private TextRecognizer mTextRecognizer;
    private int cameraOrientation = 90;
    private int mWidth, mHeight;

    // Constructor to create surface holder and add callback to this holder
    public CameraPreview(Context context) {
        super(context);
        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        mCamera = getCameraInstance();
        setFocus(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        setOrientation(90);
        try{
            // Tell our camera which holder it display on
            mCamera.setPreviewDisplay(holder);
            // start display camera on the holder above
            mCamera.startPreview();

        } catch(IOException ioe) {
            // Error can not start preview camera
            Log.e(TAG, "Error setting camera preview: " + ioe.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // Empty. Take care of releasing the camera preview in your activity
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h)
    {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if(mHolder.getSurface() == null)
        {
            // Preview surface does not exist
            return;
        }


        // Stop preview before making changes
        try{
           mCamera.stopPreview();
        } catch (Exception e) {
            // Ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here
        mCamera.setDisplayOrientation(cameraOrientation);
        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException ioe)
        {
            Log.e(TAG, "Error starting camera preview: " + ioe.getMessage());
        }

    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        Paint p = new Paint(Color.RED);
        canvas.drawText("REVIEW", canvas.getWidth() / 2, canvas.getHeight() /2, p);
    }

    public static Camera getCameraInstance() {
        Camera c = null;
        try{
            c = Camera.open();
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c;
    }

    public void releaseCamera() {
        if(mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Camera.Parameters params = mCamera.getParameters();

        //Get the pointer ID
        int action = event.getAction();

        // Handle multi-touch event
        if(event.getPointerCount() > 1) {
            if(action == MotionEvent.ACTION_POINTER_DOWN) {
                mDist = getFingerSpacing(event);
            } else if (action == MotionEvent.ACTION_MOVE && params.isZoomSupported()) {
                mCamera.cancelAutoFocus();
                handleZoom(event, params);
            }
        } else {
            // Handle single touch
            if(action == MotionEvent.ACTION_UP) {

            }
        }

        return true;
    }

    private void handleZoom(MotionEvent event, Camera.Parameters params) {
        // Get maximum zoom value
        int maxZoom = params.getMaxZoom();
        // Get current zoom value
        int zoom = params.getZoom();

        float newDist = getFingerSpacing(event);

        // Zoom in
        if(newDist > mDist) {
            if(zoom < maxZoom) {
                zoom++;
            }
        } else if (newDist < mDist) {
            // Zoom out
            if(zoom > 0) {
                zoom--;
            }
        }

        mDist = newDist;
        params.setZoom(zoom);
        mCamera.setParameters(params);

    }

    public void handleFocus(MotionEvent event, Camera.Parameters params) {
        int pointerId = event.getPointerId(0);
        int pointerIndex = event.findPointerIndex(pointerId);

        // Get the pointer's current position
        float x = event.getX(pointerIndex);
        float y = event.getY(pointerIndex);


        List<String> supportedFocusMode = params.getSupportedFocusModes();
        if(supportedFocusMode != null && supportedFocusMode.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    //currently set to auto-focus on single touch
                    camera.cancelAutoFocus();
                }
            });

        }
    }

    private float getFingerSpacing(MotionEvent event) {
        // Distance between 2 axis-x
        float x = event.getX(0) - event.getY(1);
        // Distance between 2 axis-y
        float y = event.getY(0) - event.getY(1);

        // Return distance between 2 point of fingers
        return (float) Math.sqrt(x * x + y * y);
    }

    public void setFocus(String mParameter) {
            Camera.Parameters params = mCamera.getParameters();
            List<String> focusMode = params.getSupportedFocusModes();
            if(focusMode.contains(mParameter)) {
                Log.e(TAG, "Focus Supported");
                params.setFocusMode(mParameter);
                mCamera.setParameters(params);
        }

    }

    public void setOrientation(int rotation) {
        Camera.Parameters params = mCamera.getParameters();
        params.setRotation(rotation);
        mCamera.setParameters(params);
    }

    private PictureCallback mPicture = new PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null) {
                Log.d("Camera_Storage", "Error creating media file, check storage permission");
            }

            try{
                // Create original bitmap when user press "shoot"
                Bitmap imageOriginal = BitmapFactory.decodeByteArray(data, 0, data.length);

                Matrix rotationMatrix  = new Matrix();
                rotationMatrix.postRotate(90);

                Bitmap rotateImage = Bitmap.createBitmap(imageOriginal, 0, 0, imageOriginal.getWidth(), imageOriginal.getHeight(), rotationMatrix, false);

                int left =  (rotateImage.getWidth() / 2) - (mWidth / 2);
                int top =  (rotateImage.getHeight() / 2) - (mHeight / 2);

                Bitmap cropImage = Bitmap.createBitmap(rotateImage, left, top, mWidth, mHeight);


                Frame frame = new Frame.Builder().setBitmap(cropImage).setId(FRAME_ID).build();
                mTextRecognizer.receiveFrame(frame);

                // Save bitmap on storage
                // FileOutputStream fos = new FileOutputStream(pictureFile);
                // cropImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
                // fos.close();

                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();
            } catch (FileNotFoundException e) {
                Log.d("Camera_Storage", "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d("Camera_Storage", "Error accessing file: " + e.getMessage());
            }

        }
    };

    /** Create a File for saving an image or video **/
    private  static File getOutputMediaFile(int type) {
        // To be safe, you should check that the SD card is mounted.
        // using Enviroment.getExternalStorageState() before doing this.
        String state = Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(state) == false) {
            Log.d("Camera_Storage", "External storage is not mounted");
            return null;
        }


         File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyCameraApp");
         // This location works best if you want the created images to be shared
         // between applications and persist after your app has been uninstalled.

         // Create the storage directory if it does not exist
        if(! mediaStorageDir.exists()) {
            if(! mediaStorageDir.mkdir()) {
                Log.d("Camera_Storage", "failed to create directory");
                return null;
            }
        }

        //Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if(type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
        }
        /**
         * else if(type == MEDIA_TYPE_VIDEO) {
         * mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID_" + timeStamp + ".mp4");
         * }
         **/
         else {
            return null;
        }

        return mediaFile;
    }

    public void captureImage(int width, int height, TextRecognizer textRecognizer) {
        mWidth = width;
        mHeight = height;
        mCamera.takePicture(null, null, mPicture);
        mTextRecognizer = textRecognizer;
    }
}
