package com.piercezaifman.mycitymaps.data;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Represents a location on the map.
 *
 * Created by piercezaifman on 2017-01-02.
 */

public class MapLocation implements ClusterItem {
    private float distance;
    private String title;
    private String subTitle;
    private int id;
    private LatLng position;

    public MapLocation(float distance, String title, String subTitle, int id, LatLng position) {
        this.distance = distance;
        this.title = title;
        this.subTitle = subTitle;
        this.id = id;
        this.position = position;
    }

    @Override
    public LatLng getPosition() {
        return this.position;
    }

    @Override
    public String getSnippet() {
        return this.subTitle;
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


}
