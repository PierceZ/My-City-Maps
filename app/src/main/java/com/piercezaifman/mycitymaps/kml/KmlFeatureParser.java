package com.piercezaifman.mycitymaps.kml;

import com.google.android.gms.maps.model.LatLng;
import com.piercezaifman.mycitymaps.util.DoubleUtil;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
/* package */ class KmlFeatureParser {

    private final static String GEOMETRY_REGEX = "(Point|LineString|Polygon)";
    private final static String BOUNDARY_REGEX = "(outerBoundaryIs|innerBoundaryIs)";

    private final static String POINT_START = "<Point>";
    private final static String POINT_END = "</Point>";
    private final static String LINE_START = "<LineString>";
    private final static String LINE_END = "</LineString>";
    private final static String POLYGON_START = "<Polygon>";
    private final static String POLYGON_END = "</Polygon>";

    private final static int LONGITUDE_INDEX = 0;

    private final static int LATITUDE_INDEX = 1;

    private final static Pattern PROPERTY_DESCRIPTION_REGEX = Pattern.compile("<description>(.*?)</description>");

    private final static Pattern PROPERTY_NAME_REGEX = Pattern.compile("<name>(.*?)</name>");

    private final static Pattern PROPERTY_GEOMETRY_REGEX = Pattern.compile("<" + GEOMETRY_REGEX + ">(.*?)</\\1>");

    private final static Pattern PROPERTY_COORDINATES_REGEX = Pattern.compile("<coordinates>(.*?)</coordinates>");

    private final static Pattern PROPERTY_BOUNDARY_REGEX = Pattern.compile("<" + BOUNDARY_REGEX + ">(.*?)</\\1>");

    private final static Pattern PROPERTY_EXTENDED_DATA_REGEX =
            Pattern.compile("<ExtendedData><Data *name=.(.*?).><value>(.*?)</value></Data></ExtendedData>");

    private final static String KML_KEY_NAME = "name";
    private final static String KML_KEY_DESCRIPTION = "description";

    /**
     * Creates a new Placemark object (created if a Placemark start tag is read by the
     * XmlPullParser and if a Geometry tag is contained within the Placemark tag)
     * and assigns specific elements read from the parser to the Placemark.
     */
    /* package */
    static KmlPlacemark createPlacemark(String placemark) throws IOException, XmlPullParserException {
        HashMap<String, String> properties = new HashMap<String, String>();

        String xml = placemark.replace("\n", " ");

        KmlGeometry geometry = createGeometry(xml);

        Matcher matcher = PROPERTY_NAME_REGEX.matcher(xml);
        if (matcher.find()) {
            properties.put(KML_KEY_NAME, matcher.group(1));
            matcher = PROPERTY_DESCRIPTION_REGEX.matcher(xml);
            if (matcher.find()) {
                properties.put(KML_KEY_DESCRIPTION, matcher.group(1));
            }
            properties.putAll(setExtendedDataProperties(xml));
        }

        return new KmlPlacemark(geometry, null, null, properties);
    }

    /**
     * Creates a new KmlGeometry object (Created if "Point", "LineString", "Polygon" or
     * "MultiGeometry" tag is detected by the XmlPullParser)
     */
    private static KmlGeometry createGeometry(String xml) throws IOException, XmlPullParserException {
        ArrayList<KmlGeometry> geometries = new ArrayList<KmlGeometry>();

        int startIndex = xml.indexOf(POINT_START);
        int endIndex;

        while (startIndex >= 0) {
            startIndex += POINT_START.length();
            endIndex = xml.indexOf(POINT_END, startIndex);
            geometries.add(createPoint(xml.substring(startIndex, endIndex)));
            startIndex = xml.indexOf(POINT_START, endIndex + POINT_END.length());
        }

        startIndex = xml.indexOf(LINE_START);

        while (startIndex >= 0) {
            startIndex += LINE_START.length();
            endIndex = xml.indexOf(LINE_END, startIndex);
            geometries.add(createLineString(xml.substring(startIndex, endIndex)));
            startIndex = xml.indexOf(LINE_START, endIndex + LINE_END.length());
        }

        startIndex = xml.indexOf(POLYGON_START);

        while (startIndex >= 0) {
            startIndex += POLYGON_START.length();
            endIndex = xml.indexOf(POLYGON_END, startIndex);
            geometries.add(createPolygon(xml.substring(startIndex, endIndex)));
            startIndex = xml.indexOf(POLYGON_START, endIndex + POLYGON_END.length());
        }

        if (geometries.size() > 1) {
            return new KmlMultiGeometry(geometries);
        } else if (geometries.size() == 1) {
            return geometries.get(0);
        } else {
            return null;
        }
    }

    /**
     * Adds untyped name value pairs parsed from the ExtendedData
     */
    private static HashMap<String, String> setExtendedDataProperties(String xml) throws XmlPullParserException, IOException {
        HashMap<String, String> properties = new HashMap<String, String>();

        Matcher matcher = PROPERTY_EXTENDED_DATA_REGEX.matcher(xml);
        while (matcher.find()) {
            String name = matcher.group(1);
            String value = matcher.group(2);
            properties.put(name, value);
        }

        return properties;
    }

    /**
     * Creates a new KmlPoint object
     *
     * @return KmlPoint object
     */
    private static KmlPoint createPoint(String xml) throws XmlPullParserException, IOException {
        LatLng coordinate = null;
        Matcher matcher = PROPERTY_COORDINATES_REGEX.matcher(xml);
        if (matcher.find()) {
            coordinate = convertToLatLng(matcher.group(1));
        }
        return new KmlPoint(coordinate);
    }

    /**
     * Creates a new KmlLineString object
     *
     * @return KmlLineString object
     */
    private static KmlLineString createLineString(String xml) throws XmlPullParserException, IOException {
        ArrayList<LatLng> coordinates = new ArrayList<LatLng>();
        Matcher matcher = PROPERTY_COORDINATES_REGEX.matcher(xml);
        if (matcher.find()) {
            coordinates = convertToLatLngArray(matcher.group(1));
        }

        return new KmlLineString(coordinates);
    }

    /**
     * Creates a new KmlPolygon object. Parses only one outer boundary and no or many inner
     * boundaries containing the coordinates.
     *
     * @return KmlPolygon object
     */
    private static KmlPolygon createPolygon(String xml) throws XmlPullParserException, IOException {
        ArrayList<LatLng> outerBoundary = new ArrayList<LatLng>();
        ArrayList<ArrayList<LatLng>> innerBoundaries = new ArrayList<ArrayList<LatLng>>();


        Matcher matcher = PROPERTY_BOUNDARY_REGEX.matcher(xml);
        while (matcher.find()) {
            String boundaryType = matcher.group(1);
            String value = matcher.group(2);

            Matcher coordinateMatcher = PROPERTY_COORDINATES_REGEX.matcher(value);
            if (coordinateMatcher.find()) {
                String coordinates = coordinateMatcher.group(1);
                if (boundaryType.equals("outerBoundaryIs")) {
                    outerBoundary = convertToLatLngArray(coordinates);
                } else {
                    innerBoundaries.add(convertToLatLngArray(coordinates));
                }
            }
        }

        return new KmlPolygon(outerBoundary, innerBoundaries);
    }

    /**
     * Convert a string of coordinates into an array of LatLngs
     *
     * @param coordinatesString coordinates string to convert from
     * @return array of LatLng objects created from the given coordinate string array
     */
    private static ArrayList<LatLng> convertToLatLngArray(String coordinatesString) {
        ArrayList<LatLng> coordinatesArray = new ArrayList<LatLng>();

        for (String pair : coordinatesString.trim().split(" +")) {
            coordinatesArray.add(convertToLatLng(pair));
        }

        return coordinatesArray;
    }

    /**
     * Convert a string coordinate from a string into a LatLng object
     *
     * @param coordinateString coordinate string to convert from
     * @return LatLng object created from given coordinate string
     */
    private static LatLng convertToLatLng(String coordinateString) {
        // Lat and Lng are separated by a ,
        String[] coordinate = coordinateString.split(",");
        return new LatLng(DoubleUtil.getDouble(coordinate[LATITUDE_INDEX]), DoubleUtil.getDouble(coordinate[LONGITUDE_INDEX]));
    }
}
