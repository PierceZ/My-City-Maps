package com.piercezaifman.mycitymaps;

import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Convenience class just to hold onto references of different map options.
 * Created by piercezaifman on 2017-01-30.
 */

public class MapOptionsHolder {

    private List<PolylineOptions> mPolylineOptions = new ArrayList<>();
    private List<PolygonOptions> mPolygonOptions = new ArrayList<>();
    private List<MarkerOptions> mMarkerOptions = new ArrayList<>();

    public List<PolylineOptions> getPolylineOptions() {
        return mPolylineOptions;
    }

    public List<PolygonOptions> getPolygonOptions() {
        return mPolygonOptions;
    }

    public List<MarkerOptions> getMarkerOptions() {
        return mMarkerOptions;
    }

    public void addPolylineOptions(PolylineOptions o) {
        mPolylineOptions.add(o);
    }

    public void addPolygonOptions(PolygonOptions o) {
        mPolygonOptions.add(o);
    }

    public void addMarkerOptions(MarkerOptions o) {
        mMarkerOptions.add(o);
    }

    public void clear() {
        mPolygonOptions.clear();
        mPolylineOptions.clear();
        mMarkerOptions.clear();
    }
}
