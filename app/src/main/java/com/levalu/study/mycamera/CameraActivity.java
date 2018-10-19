package com.levalu.study.mycamera;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.google.android.gms.vision.text.TextRecognizer;
import com.levalu.study.mycamera.camera.CameraPreview;

@SuppressWarnings("deprecation")
public class CameraActivity extends AppCompatActivity {
    private static final String[] CAMERA_PERMISSIONS = new String[]{
            Manifest.permission.CAMERA,
    };
    private static final int REQUEST_CAMERA_PERMISSION = 0;
    private static final int REQUEST_DOCUMENT_TREE = 42;

    private ConstraintLayout mContainer;
    private CameraPreview mPreview;
    private Button mButton;
    private FrameLayout preview;
    private RectArea mRectArea;
    private TextRecognizer mTextRecognizer;
    private CameraActivity mCameraActivity;
    private int mVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LOW_PROFILE
            | View.SYSTEM_UI_FLAG_IMMERSIVE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        mCameraActivity = this;

        // Make top-level view full screen (hide status bar)
        final View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(mVisibility);


        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    // TODO: The system bars are visible. Make any desired
                    // adjustments to your UI, such as showing the action bar or
                    // other navigational controls.
                    decorView.setSystemUiVisibility(mVisibility);
                    try{
                        Thread.sleep(3000);
                    } catch (Exception e){};

                    //getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE);
                } else {
                    // TODO: The system bars are NOT visible. Make any desired
                    // adjustments to your UI, such as hiding the action bar or
                    // other navigational controls.
                }
            }
        });

        preview = findViewById(R.id.camera_preview);
        mButton = findViewById(R.id.button_capture);
        mContainer = findViewById(R.id.container);

        // Create new instance CameraPreview and the rectangle to capture numbers
        mPreview = new CameraPreview(this);
        mRectArea = new RectArea(this);

        // If app has permission, we create preview or not, we ask user for permission
        if (hasCameraPermission()) {
            createPreview();

        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, CAMERA_PERMISSIONS[0])) {
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setMessage(R.string.camera_permission)
                    .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(CameraActivity.this, CAMERA_PERMISSIONS, REQUEST_CAMERA_PERMISSION);
                        }
                    })
                    .create();
            dialog.show();
        } else {
            ActivityCompat.requestPermissions(this, CAMERA_PERMISSIONS, REQUEST_CAMERA_PERMISSION);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPreview.releaseCamera();
        mTextRecognizer.release();
    }

    // Initialize all components when this activity create: onCreate()
    private void createPreview() {

        mTextRecognizer = new TextRecognizer.Builder(this).build();
        mTextRecognizer.setProcessor(new DetectorProcessor(this, mContainer));

        if(!mTextRecognizer.isOperational())
        {
            Snackbar.make(mContainer, R.string.detector_not_available, Snackbar.LENGTH_SHORT).show();
            finish();
        }
        preview.addView(mPreview);
        preview.addView(mRectArea);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPreview.captureImage(mRectArea.getMyWidth(), mRectArea.getMyHeight(), mTextRecognizer);
            }
        });
    }

    private boolean hasCameraPermission() {
        int result = ContextCompat.checkSelfPermission(this, CAMERA_PERMISSIONS[0]);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION:
                if (hasCameraPermission()) {
                    createPreview();
                }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        }
    }
}
