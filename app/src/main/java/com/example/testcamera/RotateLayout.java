package com.example.testcamera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.ViewParent;

/**
 * Created by zhangxing on 15-8-8.
 */
public class RotateLayout extends ViewGroup implements Rotatable {
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    public RotateLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public ViewParent invalidateChildInParent(int[] location, Rect dirty) {
        return super.invalidateChildInParent(location, dirty);
    }

    @Override
    public void setOrientation(int orientation, boolean animation) {

    }
}
