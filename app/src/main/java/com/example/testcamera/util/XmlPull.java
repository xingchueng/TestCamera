package com.example.testcamera.util;

/**
 * Created by zhangxing on 9-28.
 */

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class XmlPull
{
    private List<PictureSize> sizes;
    private PictureSize size;
    public List<PictureSize> parse(InputStream input)
            throws XmlPullParserException, IOException
    {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();
        xpp.setInput(input,"UTF-8" );

        int eventType = xpp.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if(eventType == XmlPullParser.START_DOCUMENT) {
                sizes = new ArrayList<PictureSize>();
            } else if(eventType == XmlPullParser.START_TAG) {
                if(xpp.getName().equals("size")){
                    size = new PictureSize();
                }else if(xpp.getName().equals("platform")){
                    eventType = xpp.next();
                    size.setPlatform(xpp.getText());
                }else if(xpp.getName().equals("front_height")){
                    eventType = xpp.next();
                    size.setFrontHeight(Integer.parseInt(xpp.getText()));
                }else if(xpp.getName().equals("front_width")){
                    eventType = xpp.next();
                    size.setFrontWidth(Integer.parseInt(xpp.getText()));
                }else if(xpp.getName().equals("back_height")){
                    eventType = xpp.next();
                    size.setBackHeight(Integer.parseInt(xpp.getText()));
                }else if(xpp.getName().equals("back_width")){
                    eventType = xpp.next();
                    size.setBackWidth(Integer.parseInt(xpp.getText()));
                }

            } else if(eventType == XmlPullParser.END_TAG) {
                if(xpp.getName().equals("size")){
                    sizes.add(size);
                }
            }

            eventType = xpp.next();
        }
        return sizes;
    }
}