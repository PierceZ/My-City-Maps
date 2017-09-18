package com.piercezaifman.mycitymaps.kml;

import com.google.android.gms.maps.model.LatLng;

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
public class KmlLineString implements KmlGeometry<List<LatLng>> {

    public static final String GEOMETRY_TYPE = "LineString";

    final ArrayList<LatLng> mCoordinates;

    /**
     * Creates a new KmlLineString object
     *
     * @param coordinates array of coordinates
     */
    public KmlLineString(ArrayList<LatLng> coordinates) {
        if (coordinates == null) {
            throw new IllegalArgumentException("Coordinates cannot be null");
        }
        mCoordinates = coordinates;
    }

    /**
     * Gets the type of geometry
     *
     * @return type of geometry
     */
    @Override
    public String getGeometryType() {
        return GEOMETRY_TYPE;
    }

    /**
     * Gets the coordinates
     *
     * @return ArrayList of LatLng
     */
    public ArrayList<LatLng> getGeometryObject() {
        return mCoordinates;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(GEOMETRY_TYPE).append("{");
        sb.append("\n coordinates=").append(mCoordinates);
        sb.append("\n}\n");
        return sb.toString();
    }
}
