package com.example.testcamera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

/**
 * Created by zhangxing on 15-8-8.
 */
public class RotateLayout extends ViewGroup implements Rotatable {
    private int mOrientation;
    private Matrix mMatrix = new Matrix();
    protected View mChild;
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        final int w = getMeasuredWidth();
        final int h = getMeasuredHeight();
        switch (mOrientation) {
            case 0:
                mMatrix.setTranslate(0, 0);
                break;
            case 90:
                mMatrix.setTranslate(0, -h);
                break;
            case 180:
                mMatrix.setTranslate(-w, -h);
                break;
            case 270:
                mMatrix.setTranslate(-w, 0);
                break;
        }
        mMatrix.postRotate(mOrientation);
        ev = transformEvent(ev, mMatrix);
        return super.dispatchTouchEvent(ev);
    }

    public RotateLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int width = right - left;
        int height = bottom - top;
        switch (mOrientation) {
            case 0:
            case 180:
                mChild.layout(0, 0, width, height);
                break;
            case 90:
            case 270:
                mChild.layout(0, 0, height, width);
                break;
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.save();
        int w = getMeasuredWidth();
        int h = getMeasuredHeight();
        switch (mOrientation) {
            case 0:
                canvas.translate(0, 0);
                break;
            case 90:
                canvas.translate(0, h);
                break;
            case 180:
                canvas.translate(w, h);
                break;
            case 270:
                canvas.translate(w, 0);
                break;
        }
        canvas.rotate(-mOrientation, 0, 0);
        super.dispatchDraw(canvas);
        canvas.restore();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int w = 0, h = 0;
        switch(mOrientation) {
            case 0:
            case 180:
                measureChild(mChild, widthMeasureSpec, heightMeasureSpec);
                w = mChild.getMeasuredWidth();
                h = mChild.getMeasuredHeight();
                break;
            case 90:
            case 270:
                measureChild(mChild, heightMeasureSpec, widthMeasureSpec);
                w = mChild.getMeasuredHeight();
                h = mChild.getMeasuredWidth();
                break;
        }
        setMeasuredDimension(w, h);

        switch (mOrientation) {
            case 0:
                mChild.setTranslationX(0);
                mChild.setTranslationY(0);
                break;
            case 90:
                mChild.setTranslationX(0);
                mChild.setTranslationY(h);
                break;
            case 180:
                mChild.setTranslationX(w);
                mChild.setTranslationY(h);
                break;
            case 270:
                mChild.setTranslationX(w);
                mChild.setTranslationY(0);
                break;
        }
        mChild.setRotation(-mOrientation);
    }

    @Override
    public ViewParent invalidateChildInParent(int[] location, Rect dirty) {
        dirty.set(0, 0, getWidth(), getHeight());
        return super.invalidateChildInParent(location, dirty);
    }

    @Override
    public void setOrientation(int orientation, boolean animation) {
        if(orientation < 0){
            orientation = 0;
        }
        orientation = orientation % 360;
        if(mOrientation == orientation)
            return;

        mOrientation = orientation;
        requestLayout();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mChild = getChildAt(0);
        mChild.setPivotX(0);
        mChild.setPivotY(0);
    }

    private MotionEvent transformEvent(MotionEvent e, Matrix m) {
        MotionEvent newEvent = MotionEvent.obtain(e);
        newEvent.transform(m);
        return newEvent;
    }
}
