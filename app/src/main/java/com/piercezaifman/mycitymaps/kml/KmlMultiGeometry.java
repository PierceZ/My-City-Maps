package com.piercezaifman.mycitymaps.kml;

import java.util.ArrayList;

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
public class KmlMultiGeometry implements KmlGeometry<ArrayList<KmlGeometry>> {

    private static final String GEOMETRY_TYPE = "MultiGeometry";

    private ArrayList<KmlGeometry> mGeometries = new ArrayList<KmlGeometry>();

    /**
     * Creates a new KmlMultiGeometry object
     *
     * @param geometries array of KmlGeometry objects contained in the MultiGeometry
     */
    public KmlMultiGeometry(ArrayList<KmlGeometry> geometries) {
        if (geometries == null) {
            throw new IllegalArgumentException("Geometries cannot be null");
        }
        mGeometries = geometries;
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
     * Gets an ArrayList of KmlGeometry objects
     *
     * @return ArrayList of KmlGeometry objects
     */

    public ArrayList<KmlGeometry> getGeometryObject() {
        return mGeometries;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(GEOMETRY_TYPE).append("{");
        sb.append("\n geometries=").append(mGeometries);
        sb.append("\n}\n");
        return sb.toString();
    }
}
