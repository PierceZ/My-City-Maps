package com.piercezaifman.mycitymaps.kml;

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
public interface KmlGeometry<T> {

    /**
     * Gets the type of geometry
     *
     * @return type of geometry
     */
    public String getGeometryType();

    /**
     * Gets the stored KML Geometry object
     *
     * @return geometry object
     */
    public T getGeometryObject();

}