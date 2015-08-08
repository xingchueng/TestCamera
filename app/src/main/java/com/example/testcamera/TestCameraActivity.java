package com.example.testcamera;

import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import java.util.Arrays;

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
    private String mCameraId = null;
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

        cameraManager = getCameraManager(this);
        try {
            IDs = cameraManager.getCameraIdList();
        } catch (CameraAccessException e) {
            log(e.getMessage());
        }
    }



    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mSurfaceHolder = holder;
        HandlerThread handlerThread = new HandlerThread("testcamera");
        handlerThread.start();
        final Handler handler = new Handler(handlerThread.getLooper());
        try {
            final ImageReader imageReader = ImageReader.newInstance(mSurfaceView.getWidth(), mSurfaceView.getHeight(),
                    ImageFormat.JPEG, 7);
            imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {

                }
            }, handler);

            cameraManager.openCamera(IDs[0], new CameraDevice.StateCallback() {
                @Override
                public void onOpened(CameraDevice camera) {
                    log("camera is opened.id = " + IDs[0]);
                    try {
                        final CaptureRequest.Builder previewBuild = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                        previewBuild.addTarget(mSurfaceHolder.getSurface());
                        mDeviceState = STATE_PREVIEW;
                        log("start create capture session");
                        camera.createCaptureSession(Arrays.asList(mSurfaceHolder.getSurface(), imageReader.getSurface()),
                                new CameraCaptureSession.StateCallback() {
                                    @Override
                                    public void onConfigured(CameraCaptureSession session) {
                                        log("preview session onConfigured");
                                        previewBuild.set(CaptureRequest.CONTROL_AF_MODE,
                                                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                                        previewBuild.set(CaptureRequest.CONTROL_AE_MODE,
                                                CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                                        try {
                                            session.setRepeatingRequest(previewBuild.build(), cameraCaptureCallback, handler);
                                        } catch (CameraAccessException e) {
                                            log(e.getMessage());
                                        }
                                    }

                                    @Override
                                    public void onConfigureFailed(CameraCaptureSession session) {

                                    }
                                }, handler);
                    } catch (CameraAccessException e) {
                        log(e.getMessage());
                    }
                }

                @Override
                public void onDisconnected(CameraDevice camera) {

                }

                @Override
                public void onError(CameraDevice camera, int error) {

                }
            }, handler);


        } catch (CameraAccessException e) {
            log(e.getMessage());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    private CameraCaptureSession.CaptureCallback cameraCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            checkState(result);
        }

        @Override
        public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request, CaptureResult partialResult) {
            super.onCaptureProgressed(session, request, partialResult);
            checkState(partialResult);
        }

        private void checkState(CaptureResult result) {
            switch (mDeviceState) {
                case STATE_PREVIEW:
                    break;
                case STATE_WAITING_CAPTURE:
                    int afState = result.get(CaptureResult.CONTROL_AF_STATE);

                    if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState ||
                            CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState
                            || CaptureResult.CONTROL_AF_STATE_PASSIVE_FOCUSED == afState
                            || CaptureResult.CONTROL_AF_STATE_PASSIVE_UNFOCUSED == afState) {
                        //do something like save picture
                    }
                    break;
            }
        }
    };


    public CameraManager getCameraManager(Context context){
        return (CameraManager)context.getSystemService(Context.CAMERA_SERVICE);
    }

    public void log(String message){
        if(DEBUG){
            Log.d(TAG, message);
        }
    }


}
