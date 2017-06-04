package com.miyue.http;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;

import java.io.StringReader;



/**
 * Created by zhangzhendong on 17/5/31.
 */

public class XmlParser {

    public static  String getQQLRC(String xml) {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(new StringReader(xml));
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:// 开始结点
                        if ("lyric".equals(parser.getName())) {
                            return parser.nextText();
                        }
                        break;
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
