package com.example.testcamera.util;

/**
 * Created by zhangxing on 9-28.
 */
public class PictureSize {
    private String mPlatform;
    private int mFrontHeight;
    private int mFrontWidth;
    private int mBackHeight;
    private int mBackWidth;


    public String getPlatform() {
        return this.mPlatform;
    }

    public int getFrontHeight(){
        return mFrontHeight;
    }

    public int getBackHeight(){
        return mBackHeight;
    }

    public int getFrontWidth(){
        return mFrontWidth;
    }

    public int getBackWidth(){
        return mBackWidth;
    }


    public void setPlatform(String p) {
        this.mPlatform = p;
    }

    public void setFrontHeight(int h){
        this.mFrontHeight = h;
    }

    public void setBackHeight(int h){
        this.mBackHeight = h;
    }

    public void setFrontWidth(int w){
        this.mFrontWidth = w;
    }

    public void setBackWidth(int w){
        this.mBackWidth = w;
    }

    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append("platform:");
        builder.append(getPlatform());
        builder.append('\n');
        builder.append("front:");
        builder.append(getFrontWidth() + "*" + getFrontHeight() + '\n');
        builder.append("back");
        builder.append(getBackWidth() + "*" + getBackHeight() + '\n') ;
        return builder.toString();
    }
}
