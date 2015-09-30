package com.example.testcamera;

/**
 * Created by zhangxing on 9-28.
 */
import java.util.ArrayList;
import java.util.List;

import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.RelativeLayout;
import android.hardware.camera2.params.MeteringRectangle;
import com.example.testcamera.util.Utils;

public class FocusManager {
    private static final String TAG = "TEST_CAM_FocusManager";

    private Matrix mMatrix;
    private FocusIndicatorRotateLayout mFocusIndicatorRotateLayout;
    private int mPreviewWidth;
    private int mPreviewHeight;
    private boolean mInitialized;
    private boolean mMirror;
    private int mDisplayOrientation;
    private Listener mListener;
    private List<MeteringRectangle> mFocusArea;
    private int mState;
    private Handler mHandler;

    private static final int RESET_FOCUS = 0;
    private static final int RESET_FOCUS_DELAY = 300;

    private static final int STATE_SUCCESS = 0;
    private static final int STATE_IDLE = 1;
    private static final int STATE_FAIL = 2;
    private static final int STATE_FOCUSING = 3;

    public interface Listener {
        public void autoFocus();
        public void setFocusParameters();
    }

    public FocusManager(View l, Listener listener, boolean isMirror,
                        Looper looper) {
        mMatrix = new Matrix();
        mFocusIndicatorRotateLayout = (FocusIndicatorRotateLayout) l;
        mListener = listener;
        mMirror = isMirror;
        mHandler = new MainHandler(looper);
    }

    public void setPreviewSize(int previewWidth, int previewHeight) {
        mPreviewWidth = previewWidth;
        mPreviewHeight = previewHeight;
        setMatrix();
    }

    public void onSingleTapUp(int x, int y) {
        if (STATE_FOCUSING == mState) return;
        mState = STATE_IDLE;
        int focusWidth = mFocusIndicatorRotateLayout.getWidth();
        int focusHeight = mFocusIndicatorRotateLayout.getHeight();
        int previewWidth = mPreviewWidth;
        int previewHeight = mPreviewHeight;

        initializeFocusAreas(focusWidth, focusHeight, x, y, previewWidth, previewHeight);

        RelativeLayout.LayoutParams p =
                (RelativeLayout.LayoutParams) mFocusIndicatorRotateLayout.getLayoutParams();
        int left = Utils.clamp(x - focusWidth / 2, 0, previewWidth - focusWidth);
        int top = Utils.clamp(y - focusHeight / 2, 0, previewHeight - focusHeight);
        p.setMargins(left, top, 0, 0);
        int[] rules = p.getRules();
        rules[RelativeLayout.CENTER_IN_PARENT] = 0;
        mFocusIndicatorRotateLayout.requestLayout();
        mListener.setFocusParameters();
        mListener.autoFocus();
        updateFocusUI();
    }

    public void onAutoFocus(boolean focused) {
        if (focused) {
            mState = STATE_SUCCESS;
        } else {
            mState = STATE_FAIL;
        }
        updateFocusUI();
        mHandler.sendEmptyMessageDelayed(RESET_FOCUS, RESET_FOCUS_DELAY);
    }

    public void onPreviewStarted() {
        mState = STATE_IDLE;
    }

    public void onPreviewStopped() {
        mState = STATE_IDLE;
        resetTouchFocus();
        updateFocusUI();
    }

    public List<MeteringRectangle> getFocusArea() {
        return mFocusArea;
    }

    private class MainHandler extends Handler {
        public MainHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RESET_FOCUS: {
                    resetTouchFocus();
                    break;
                }
            }
        }
    }

    private void resetTouchFocus() {
        if (!mInitialized) return;
        mState = STATE_IDLE;
        // Put focus indicator to the center.
        RelativeLayout.LayoutParams p =
                (RelativeLayout.LayoutParams) mFocusIndicatorRotateLayout.getLayoutParams();
        int[] rules = p.getRules();
        rules[RelativeLayout.CENTER_IN_PARENT] = RelativeLayout.TRUE;
        p.setMargins(0, 0, 0, 0);
        mFocusIndicatorRotateLayout.clear();

        mFocusArea = null;
    }

    private void updateFocusUI() {
        if (STATE_IDLE == mState) {
            mFocusIndicatorRotateLayout.showStart();
        } else if (STATE_FAIL == mState) {
            mFocusIndicatorRotateLayout.showFail(false);
            mState = STATE_IDLE;
        } else if (STATE_SUCCESS == mState) {
            mFocusIndicatorRotateLayout.showSuccess(false);
            mState = STATE_IDLE;
        }
    }

    private void setMatrix() {
        if (mPreviewWidth != 0 && mPreviewHeight != 0) {
            Matrix matrix = new Matrix();
            Utils.prepareMatrix(matrix, mMirror, mDisplayOrientation,
                    mPreviewWidth, mPreviewHeight);
            matrix.invert(mMatrix);
            mInitialized = true;
        }
    }

    private void initializeFocusAreas(int focusWidth, int focusHeight,
                                      int x, int y, int previewWidth, int previewHeight) {
        if (mFocusArea == null) {
            mFocusArea = new ArrayList<MeteringRectangle>();
            mFocusArea.add(new MeteringRectangle(new Rect(), 1));
        }
        calculateTapArea(focusWidth, focusHeight, 1.5f, x, y, previewWidth, previewHeight, ((MeteringRectangle) mFocusArea.get(0)).getRect());
    }

    private void calculateTapArea(int focusWidth, int focusHeight, float areaMultiple,
                                  int x, int y, int previewWidth, int previewHeight, Rect rect) {
        int areaWidth = (int) (focusWidth * areaMultiple);
        int areaHeight = (int) (focusHeight * areaMultiple);
        int left = Utils.clamp(x - areaWidth / 2, 0, previewWidth - areaWidth);
        int top = Utils.clamp(y - areaHeight / 2, 0, previewHeight - areaHeight);

        RectF rectF = new RectF(left, top, left + areaWidth, top + areaHeight);
        mMatrix.mapRect(rectF);
        Utils.rectFToRect(rectF, rect);
    }
}
