package com.piercezaifman.mycitymaps.kml;

import java.util.HashMap;

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
public class KmlPlacemark {

    private final KmlGeometry mGeometry;

    private final String mStyle;

    private final KmlStyle mInlineStyle;

    private HashMap<String, String> mProperties;

    /**
     * Creates a new KmlPlacemark object
     *
     * @param geometry   geometry object to store
     * @param style      style id to store
     * @param properties properties hashmap to store
     */
    public KmlPlacemark(KmlGeometry geometry, String style, KmlStyle inlineStyle,
            HashMap<String, String> properties) {
        mProperties = new HashMap<String, String>();
        mGeometry = geometry;
        mStyle = style;
        mInlineStyle = inlineStyle;
        mProperties = properties;
    }

    /**
     * Gets the style id associated with the basic_placemark
     *
     * @return style id
     */
    public String getStyleId() {
        return mStyle;
    }

    /**
     * Gets the inline style that was found
     *
     * @return InlineStyle or null if not found
     */
    public KmlStyle getInlineStyle() {
        return mInlineStyle;
    }

    /**
     * Gets the property entry set
     *
     * @return property entry set
     */
    public Iterable getProperties() {
        return mProperties.entrySet();
    }

    /**
     * Gets the property based on property name
     *
     * @param keyValue Property name to retrieve value
     * @return property value, null if not found
     */
    public String getProperty(String keyValue) {
        return mProperties.get(keyValue);
    }

    /**
     * Gets the geometry object
     *
     * @return geometry object
     */
    public KmlGeometry getGeometry() {
        return mGeometry;
    }

    /**
     * Gets whether the basic has a given property
     *
     * @param keyValue key value to check
     * @return true if the key is stored in the properties, false otherwise
     */
    public boolean hasProperty(String keyValue) {
        return mProperties.containsKey(keyValue);
    }

    /**
     * Gets whether the placemark has a properties
     *
     * @return true if there are properties in the properties hashmap, false otherwise
     */
    public boolean hasProperties() {
        return mProperties.size() > 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Placemark").append("{");
        sb.append("\n style id=").append(mStyle);
        sb.append(",\n inline style=").append(mInlineStyle);
        sb.append(",\n properties=").append(mProperties);
        sb.append(",\n geometry=").append(mGeometry);
        sb.append("\n}\n");
        return sb.toString();
    }
}
