package com.example.testcamera;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.example.testcamera.util.PictureSize;
import com.example.testcamera.util.XmlPull;

public class TestCameraActivity extends Activity implements FocusManager.Listener
{
    private static final String TAG = "testcamera.activity";
    private static boolean DEBUG = true;

    private TextureView mTextureView = null;
    private Size mPreviewSize;
    private CaptureRequest.Builder mPreviewBuilder;
    private CameraDevice mCameraDevice;
    private CameraCaptureSession mPreviewSession;
    private String action = null;
    private boolean fromIntent = false;
    private boolean auto = false;
    private int cameraId = 0;
    private Handler mBackgroundHandler = null;
    private HandlerThread mBackgroundThread = null;

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_camera);
        getDataFromLineTest();
        initActions();
    }

    protected void onPause() {
        // TODO Auto-generated method stub
        Log.e(TAG, "onPause");
        super.onPause();
        if (null != mCameraDevice) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
        stopBackgroundThread();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();
        if(mTextureView.isAvailable()){
            openCamera(cameraId);
        }
    }

    public void getDataFromLineTest(){
        Intent it = getIntent();
        auto = it.getBooleanExtra("CameraAuto", false);
        cameraId = it.getIntExtra("CameraId",0);
        action = it.getAction();
        if(!action.equals("android.intent.action.MAIN")){
            fromIntent = true;
        }
    }

    public void initActions(){
        mTextureView = (TextureView)findViewById(R.id.texture);
        final Button mCaptureButton = (Button)findViewById(R.id.capture);
        final Button mSwitchButton  = (Button)findViewById(R.id.switch_camera);
        final Button mBackButton    = (Button)findViewById(R.id.finish);
        final Button mFlashButton   = (Button)findViewById(R.id.flashlight);
        startBackgroundThread();
        new Handler().postDelayed(new Runnable(){
            public void run() {
                mCaptureButton.setVisibility(View.VISIBLE);
                mSwitchButton.setVisibility(View.VISIBLE);
                mBackButton.setVisibility(View.VISIBLE);
                mFlashButton.setVisibility(View.VISIBLE);
            }
        }, 2*1000);

        mCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
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

        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
                log("on surfaceTexture available.");
                openCamera(cameraId);
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

            }
        });

    }

    @Override
    public void autoFocus() {

    }

    @Override
    public void setFocusParameters() {

    }

    private void openCamera(int id) {

        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        Log.e(TAG, "openCamera E");
        try {
            String cameraId = manager.getCameraIdList()[id];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            mPreviewSize = map.getOutputSizes(SurfaceTexture.class)[0];

            manager.openCamera(cameraId, mStateCallback, null);
        } catch (CameraAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Log.e(TAG, "openCamera X");
    }

    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(CameraDevice camera) {
            // TODO Auto-generated method stub
            Log.e(TAG, "onOpened");
            mCameraDevice = camera;
            startPreview();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            // TODO Auto-generated method stub
            Log.e(TAG, "onDisconnected");
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            // TODO Auto-generated method stub
            Log.e(TAG, "onError");
        }

    };

    public Size getBestPictureSize(CameraDevice device, CameraCharacteristics cameraCharacteristics){
        Size[] Sizes = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                                            .getOutputSizes(ImageFormat.JPEG);
        Size best = null;
        if(Sizes != null){
            best = Sizes[Sizes.length - 1];
        }

        //wait for completing.

        return best;
    }

    protected void takePicture() {
        log("takePicture");
        if(null == mCameraDevice) {
            Log.e(TAG, "mCameraDevice is null, return");
            return;
        }

        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(mCameraDevice.getId());

            Size best = getBestPictureSize(mCameraDevice, characteristics);
            int width = best.getWidth();
            int height = best.getHeight();   //need add a default value;

            ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            List<Surface> outputSurfaces = new ArrayList<Surface>(2);
            outputSurfaces.add(reader.getSurface());
            outputSurfaces.add(new Surface(mTextureView.getSurfaceTexture()));

            final CaptureRequest.Builder captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            // Orientation
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));
            final StringBuilder path = new StringBuilder();
            path.append("/sdcard/factory_test_");
            //path.append("/factory_test_");
            path.append(cameraId);
            path.append(".jpg");
            final File file = new File(path.toString());

            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {

                @Override
                public void onImageAvailable(ImageReader reader) {

                    mBackgroundHandler.post(new ImageSaver(reader.acquireLatestImage(), file));
                    /*Image image = null;
                    try {
                        image = reader.acquireLatestImage();
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        save(bytes);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (image != null) {
                            image.close();
                        }
                    }*/
                }

/*                private void save(byte[] bytes) throws IOException {
                    OutputStream output = null;
                    try {
                        output = new FileOutputStream(file);
                        output.write(bytes);
                    } finally {
                        if (null != output) {
                            output.close();
                        }
                    }
                }*/

            };

/*            HandlerThread thread = new HandlerThread("CameraPicture");
            thread.start();
            final Handler backgroudHandler = new Handler(thread.getLooper());*/
            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);

            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {

                @Override
                public void onCaptureCompleted(CameraCaptureSession session,
                                               CaptureRequest request, TotalCaptureResult result) {

                    super.onCaptureCompleted(session, request, result);
                    Toast.makeText(TestCameraActivity.this, "Saved:"+file, Toast.LENGTH_SHORT).show();
                    /*new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            handlePicture(path.toString());
                        }
                    },2000);*/

                    startPreview();
                }

            };

            mCameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(CameraCaptureSession session) {

                    try {
                        session.capture(captureBuilder.build(), captureListener, mBackgroundHandler);
                    } catch (CameraAccessException e) {

                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {

                }
            }, mBackgroundHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    protected void startPreview() {
        // TODO Auto-generated method stub
        if(null == mCameraDevice || !mTextureView.isAvailable() || null == mPreviewSize) {
            Log.e(TAG, "startPreview fail, return");
        }

        SurfaceTexture texture = mTextureView.getSurfaceTexture();
        if(null == texture) {
            Log.e(TAG,"texture is null, return");
            return;
        }

        texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        Surface surface = new Surface(texture);

        try {
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        } catch (CameraAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        mPreviewBuilder.addTarget(surface);

        try {
            mCameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(CameraCaptureSession session) {
                    // TODO Auto-generated method stub
                    mPreviewSession = session;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                    // TODO Auto-generated method stub
                    Toast.makeText(TestCameraActivity.this, "onConfigureFailed", Toast.LENGTH_LONG).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    protected void updatePreview() {
        // TODO Auto-generated method stub
        if(null == mCameraDevice) {
            Log.e(TAG, "updatePreview error, return");
        }

        mPreviewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        HandlerThread thread = new HandlerThread("CameraPreview");
        thread.start();
        Handler backgroundHandler = new Handler(thread.getLooper());

        try {
            mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, backgroundHandler);
        } catch (CameraAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static class ImageSaver implements Runnable{
        private  Image mImage;
        private  File mFile;

        public ImageSaver(Image mImage, File mFile) {
            this.mImage = mImage;
            this.mFile = mFile;
        }

        @Override
        public void run() {
            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            FileOutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(mFile);
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                mImage.close();
                if(outputStream != null){
                    try{
                        outputStream.close();
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void handlePicture(String path){
        Intent it = new Intent();
        it.putExtra("ImagePath",path);
        if(auto){  //to return the path of picture
            setResult(RESULT_OK,it);
            Toast.makeText(TestCameraActivity.this, "the path of picture have been return successfully.", Toast.LENGTH_SHORT).show();
            finish();
        }else{    //to preview the picture
            it.setClassName("com.example.testcamera", "com.example.testcamera.PreviewImage");
            it.putExtra("CameraId",cameraId);
            it.putExtra("FromIntent",fromIntent);
            Log.d(TAG,"mCameraId = " + cameraId);
            Uri mUri = Uri.parse("file://" + path);
            it.setDataAndType(mUri, "image/*");
            startActivity(it);
        }
    }

    private void startBackgroundThread(){
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    private void stopBackgroundThread(){
        mBackgroundThread.quitSafely();
        try{
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        }catch (InterruptedException e){
            log(e.getMessage());
        }
    }


    public void log(String message){
        if(DEBUG){
            Log.d(TAG, message);
        }
    }


}
