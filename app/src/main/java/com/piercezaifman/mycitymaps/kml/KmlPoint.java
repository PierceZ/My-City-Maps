package com.piercezaifman.mycitymaps.kml;

import com.google.android.gms.maps.model.LatLng;

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
public class KmlPoint implements KmlGeometry<LatLng> {

    public static final String GEOMETRY_TYPE = "Point";

    private final LatLng mCoordinate;

    /**
     * Creates a new KmlPoint
     *
     * @param coordinate coordinate of the KmlPoint
     */
    public KmlPoint(LatLng coordinate) {
        if (coordinate == null) {
            throw new IllegalArgumentException("Coordinates cannot be null");
        }
        mCoordinate = coordinate;
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
     * @return LatLng with the coordinate of the KmlPoint
     */
    public LatLng getGeometryObject() {
        return mCoordinate;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(GEOMETRY_TYPE).append("{");
        sb.append("\n coordinates=").append(mCoordinate);
        sb.append("\n}\n");
        return sb.toString();
    }
}
