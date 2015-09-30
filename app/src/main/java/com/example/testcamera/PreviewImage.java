package com.example.testcamera;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.media.ExifInterface;

public class PreviewImage extends Activity implements OnTouchListener {

    private ImageView imgv;

    private PointF point0 = new PointF();
    private PointF pointM = new PointF();

    private final float ZOOM_MIN_SPACE = 10f;

    // 设定事件模式
    private final int NONE = 0;
    private final int DRAG = 1;
    private final int ZOOM = 2;
    private int mode = NONE;

    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();

    private int displayHeight ;
    private int displayWidth ;

    private float minScale = 1f;
    private float maxScale = 10f;
    private float currentScale = 1f;
    private float oldDist;

    private Bitmap bm;
    private String path;
    private int imgWidth;
    private int imgHeight;
    private int cameraId;

    private static final String TAG = "TESTCAMERA";
    public static final String ACT_BACK_FRONTCAMERATEST = "com.android.gallery3d.app.back.frontcameratest";
    public static final String ACT_BACK_BACKCAMRATEST = "com.android.gallery3d.app.back.backcameratest";
    public static boolean fromIntent = true;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        //hideNavigationBar();
        setContentView(R.layout.preview_image);
        Button backButton   = (Button)findViewById(R.id.back);
        Button deleteButton = (Button)findViewById(R.id.delete);
        Button retakeButton = (Button)findViewById(R.id.retake);

        backButton.setOnClickListener(backButtonListner);
        deleteButton.setOnClickListener(delButtonListner);
        retakeButton.setOnClickListener(retakeButtonListner);

        Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        displayHeight = point.y;
        displayWidth  = point.x;

/*
        displayWidth  = getWindowManager().getDefaultDisplay().getWidth();
        displayHeight = getWindowManager().getDefaultDisplay().getHeight();
*/

        path = getIntent().getStringExtra("ImagePath");
        cameraId = getIntent().getIntExtra("CameraId",2);
        fromIntent = getIntent().getBooleanExtra("FromIntent",false);

        init(path);
    }
    protected void hideNavigationBar(){

        View view = this.getWindow().getDecorView();
        int options = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        view.setSystemUiVisibility(options);
    }

    protected void onResume(){
        //hideNavigationBar();
        super.onResume();
    }

    protected void onStop(){
        super.onStop();
        finish();
    }
    private OnClickListener backButtonListner = new OnClickListener(){

        @Override
        public void onClick(View arg0) {
            Intent it = new Intent();
            Log.d(TAG,"fromIntent = " + fromIntent);
            if(fromIntent){
                switch(cameraId){
                    case 0:
                        it.setAction(ACT_BACK_BACKCAMRATEST);
                        break;
                    case 1:
                        it.setAction(ACT_BACK_FRONTCAMERATEST);
                        break;
                    default:
                        break;
                }
            }else{
                it.setAction(Intent.ACTION_MAIN);
                it.addCategory(Intent.CATEGORY_HOME);
            }



            it.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(it);
        }

    };

    private OnClickListener delButtonListner = new OnClickListener(){

        @Override
        public void onClick(View arg0) {
            try{
                File f = new File(path);
                if(f.isFile())
                    f.delete();
                if(!f.exists()){
                    Toast.makeText(PreviewImage.this, "File save in \"" + path + "\" has been deleted.", Toast.LENGTH_SHORT).show();
                }
            }catch(NullPointerException e){
            }

        }

    };

    private OnClickListener retakeButtonListner = new OnClickListener(){

        @Override
        public void onClick(View arg0) {
            finish();

        }

    };

    private void init(String path) {
        imgv = (ImageView) findViewById(R.id.imageview);
        imgv.setOnTouchListener(this);

        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int result = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int rotate = 0;
            switch(result) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                default:
                    break;
            }
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);
            if (options.outWidth < 0 || options.outHeight < 0) {
                return ;
            }
            options.inJustDecodeBounds = false;
            bm=  BitmapFactory.decodeFile(path, options);
            if(rotate > 0) {
                Matrix matrix = new Matrix();
                matrix.setRotate(rotate);
                Bitmap rotateBitmap = Bitmap.createBitmap(
                        bm, 0, 0, options.outWidth, options.outHeight, matrix, true);
                if(rotateBitmap != null) {
                    bm.recycle();
                    bm = rotateBitmap;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        imgWidth = bm.getWidth();
        imgHeight = bm.getHeight();
        imgv.setImageBitmap(bm);
        minScale = getMinScale();
        matrix.setScale(minScale, minScale);
        center();
        imgv.setImageMatrix(matrix);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        ImageView imgv = (ImageView) v;
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                savedMatrix.set(matrix);
                point0.set(event.getX(), event.getY());
                mode = DRAG;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                if (oldDist > ZOOM_MIN_SPACE) {
                    savedMatrix.set(matrix);
                    setMidPoint(event);
                    mode = ZOOM;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                break;
            case MotionEvent.ACTION_MOVE:
                whenMove(event);
                break;

        }
        imgv.setImageMatrix(matrix);
        checkView();
        return true;
    }

    private void whenMove(MotionEvent event) {
        switch (mode) {
            case DRAG:
                matrix.set(savedMatrix);
                matrix.postTranslate(event.getX() - point0.x, event.getY()
                        - point0.y);
                break;
            case ZOOM:
                float newDist = spacing(event);
                if (newDist > ZOOM_MIN_SPACE) {
                    matrix.set(savedMatrix);
                    float sxy = newDist / oldDist;
                    System.out.println(sxy + "<==放大缩小倍数");
                    matrix.postScale(sxy, sxy, pointM.x, pointM.y);
                }
                break;
        }
    }

    // 两个触点的距离
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private void setMidPoint(MotionEvent event) {
        float x = event.getX(0) + event.getY(1);
        float y = event.getY(0) + event.getY(1);
        pointM.set(x / 2, y / 2);
    }

    // 图片居中
    private void center() {
        RectF rect = new RectF(0, 0, imgWidth, imgHeight);
        matrix.mapRect(rect);
        float width = rect.width();
        float height = rect.height();
        float dx = 0;
        float dy = 0;

        if (width < displayWidth)
            dx = displayWidth / 2 - width / 2 - rect.left;
        else if (rect.left > 0)
            dx = -rect.left;
        else if (rect.right < displayWidth)
            dx = displayWidth - rect.right;

        if (height < displayHeight)
            dy = displayHeight / 2 - height / 2 - rect.top;
        else if (rect.top > 0)
            dy = -rect.top;
        else if (rect.bottom < displayHeight)
            dy = displayHeight - rect.bottom;

        matrix.postTranslate(dx, dy);
    }

    // 获取最小缩放比例
    private float getMinScale() {
        float sx = (float) displayWidth / imgWidth;
        float sy = (float) displayHeight / imgHeight;
        float scale = sx < sy ? sx : sy;
        if (scale > 1) {
            scale = 1f;
        }
        return scale;
    }

    // 检查约束条件，是否居中，空间显示是否合理
    private void checkView() {
        currentScale = getCurrentScale();
        if (mode == ZOOM) {
            if (currentScale < minScale) {
                matrix.setScale(minScale, minScale);
            }
            if (currentScale > maxScale) {
                matrix.set(savedMatrix);
            }
        }
        center();
    }

    // 图片当前的缩放比例
    private float getCurrentScale() {
        float[] values = new float[9];
        matrix.getValues(values);
        return values[Matrix.MSCALE_X];
    }
}
