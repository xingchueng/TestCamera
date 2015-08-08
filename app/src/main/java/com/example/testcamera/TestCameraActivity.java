package com.example.testcamera;

import android.app.Activity;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

public class TestCameraActivity extends Activity implements SurfaceHolder.Callback{
    private static final String TAG = "testcamera.activity";
    private static boolean DEBUG = true;

    private static final int STATE_PREVIEW = 1;
    private static final int STATE_WAITING_LOCK = 2;
    private static final int STATE_WAITING_PRECAPTURE = 3;
    private static final int STATE_WAITING_NON_PRECAPTURE = 4;
    private static final int STATE_PICTURE_TAKEN = 5;
    private static final int STATE_WAITING_CAPTURE = 6;

    private SurfaceView mSurfaceView = null;
    private SurfaceHolder mSurfaceHolder = null;
    private String[] IDs = null;
    private int mDeviceState = 0;
    private CameraManager cameraManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_camera);
        initActions();
    }

    public void initActions(){
        mSurfaceView = (SurfaceView)findViewById(R.id.camera_preview);
        mSurfaceView.getHolder().addCallback(TestCameraActivity.this);

        Button mCaptureButton = (Button)findViewById(R.id.capture);
        Button mSwitchButton  = (Button)findViewById(R.id.switch_camera);
        Button mBackButton    = (Button)findViewById(R.id.finish);
        Button mFlashButton   = (Button)findViewById(R.id.flashlight);

        mCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        mSwitchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        mFlashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }



    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    public void log(String message){
        if(DEBUG){
            Log.d(TAG, message);
        }
    }


}
