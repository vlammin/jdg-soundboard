/*
 * RssEntry
 * JDGSoundboard
 *
 * Copyright (c) 2017 Vincent Lammin
 */

package com.atmx.android.jdgspl.objects;

import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RssEntry {
    private static final String TAG = RssEntry.class.getSimpleName();

    private String title;
    private String publishDate;
    private String link;

    private RssEntry(String title, String publishDate, String link) {
        this.title = title;
        this.publishDate = publishDate;
        this.link = link;
    }

    public String getTitle() {
        return this.title;
    }

    public String getLink() {
        return this.link;
    }

    public String getFormattedPublishDate() {
        SimpleDateFormat inFormat = new SimpleDateFormat(
            "EEE, dd MMM yyyy HH:mm:ss Z",
            Locale.ENGLISH
        );
        try {
            Date date = inFormat.parse(this.publishDate);
            return new SimpleDateFormat("dd MMMM yyyy", Locale.FRENCH).format(date);
        } catch (ParseException ex) {
            return this.publishDate;
        }
    }

    public static RssEntry getLastRssNews(String feedUrl) {
        RssEntry entry = null;

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            URL url = new URL(feedUrl);
            xpp.setInput(url.openConnection().getInputStream(), "UTF-8");

            String title = "";
            String publishDate = "";
            String link = "";

            boolean insideItem = false;

            int event = xpp.getEventType();
            while (event != XmlPullParser.END_DOCUMENT && entry == null) {
                if (event == XmlPullParser.START_TAG) {
                    String name = xpp.getName();
                    if (name.equalsIgnoreCase("item"))
                        insideItem = true;
                    else if (insideItem && name.equalsIgnoreCase("title"))
                        title = xpp.nextText();
                    else if (insideItem && name.equalsIgnoreCase("pubDate"))
                        publishDate = xpp.nextText();
                    else if (insideItem && name.equalsIgnoreCase("link"))
                        link = xpp.nextText();

                } else if (event == XmlPullParser.END_TAG) {
                    if (xpp.getName().equalsIgnoreCase("item")) {
                        insideItem = false;
                        entry = new RssEntry(title, publishDate, link);
                    }
                }
                event = xpp.next();
            }
        } catch (Throwable t) {
            Log.e(TAG, "Could not get last RSS news");
        }

        return entry;
    }
}
