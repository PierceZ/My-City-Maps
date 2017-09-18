package com.piercezaifman.mycitymaps.kml;

import com.piercezaifman.mycitymaps.util.Util;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.

 ----
 MODIFICATIONS TO THE ORIGINAL CODE HAVE BEEN MADE
 */
public class KmlParser {

    private final static String PLACEMARK = "Placemark";

    private final List<KmlPlacemark> mPlacemarks;

    /**
     * Creates a new KmlParser object
     */
    public KmlParser() {
        mPlacemarks = new ArrayList<>();
    }

    /**
     * Parses the KML file and stores the created KmlStyle and KmlPlacemark
     */
    public void parseKml(File file) throws XmlPullParserException, IOException {
        long startTime = System.nanoTime();

        FileInputStream stream = new FileInputStream(file);
        List<String> placemarks = new ArrayList<>();


        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xmlParser = factory.newPullParser();
        xmlParser.setInput(stream, null);


        int eventType = xmlParser.getEventType();
        StringBuilder placemarkBuilder = new StringBuilder();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {

                String name = xmlParser.getName();

                if (name.equals(PLACEMARK)) {
                    placemarkBuilder.append('<');
                    placemarkBuilder.append(name);
                    placemarkBuilder.append('>');
                } else if (placemarkBuilder.length() > 0) {
                    placemarkBuilder.append('<');
                    placemarkBuilder.append(name);

                    for (int i = 0; i < xmlParser.getAttributeCount(); i++) {
                        placemarkBuilder.append(' ');
                        placemarkBuilder.append(xmlParser.getAttributeName(i));
                        placemarkBuilder.append('=');
                        placemarkBuilder.append('\'');
                        placemarkBuilder.append(xmlParser.getAttributeValue(i));
                        placemarkBuilder.append('\'');
                    }
                    placemarkBuilder.append('>');
                }
            } else if (eventType == XmlPullParser.END_TAG) {
                String name = xmlParser.getName();
                if (placemarkBuilder.length() > 0) {
                    placemarkBuilder.append('<');
                    placemarkBuilder.append('/');
                    placemarkBuilder.append(name);
                    placemarkBuilder.append('>');
                    if (name.equals(PLACEMARK)) {
                        final String text = placemarkBuilder.toString();
                        placemarks.add(text);
                        placemarkBuilder.setLength(0);
                    }
                }
            } else if (placemarkBuilder.length() > 0 && xmlParser.getText() != null) {
                placemarkBuilder.append(xmlParser.getText());
            }

            eventType = xmlParser.next();
        }

        stream.close();
        placemarkBuilder.setLength(0);
        placemarkBuilder = null;
        stream = null;
        factory = null;
        xmlParser = null;

        Util.log("The time to run", "Time to setup placemarks is: " + (System.nanoTime() - startTime) / 1000000);

        Iterable<List<KmlPlacemark>> result = Util.parseDataInParallel(placemarks,
                (sublist) -> {
                    List<KmlPlacemark> parsedPlacemarks = new ArrayList<>();
                    try {
                        for (String text : sublist) {
                            parsedPlacemarks.add(KmlFeatureParser.createPlacemark(text));
                        }
                    } catch (IOException | XmlPullParserException e) {
                        Util.log("Parsing", "Something is wrong", e);
                    }
                    return parsedPlacemarks;
                });

        for (List<KmlPlacemark> kmlPlacemarks : result) {
            mPlacemarks.addAll(kmlPlacemarks);
        }

        Util.log("The time to run", "Time to run is: " + (System.nanoTime() - startTime) / 1000000);
    }

    /**
     * @return A list of Kml Placemark objects
     */
    public List<KmlPlacemark> getPlacemarks() {
        return mPlacemarks;
    }

}
