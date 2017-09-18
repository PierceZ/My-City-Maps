package com.piercezaifman.mycitymaps.cluster;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.piercezaifman.mycitymaps.data.MapLocation;

/**
 * Created by piercezaifman on 2017-02-22.
 */

public class ColorClusterRenderer extends DefaultClusterRenderer<MapLocation> {

    private final BitmapDescriptor mMarkerDescriptor;
    private final float mMaxZoomLevel;
    private float mZoomLevel = 0;

    public ColorClusterRenderer(Context context, GoogleMap map, ClusterManager<MapLocation> clusterManager, BitmapDescriptor bitmapDescriptor) {
        super(context, map, clusterManager);
        mMaxZoomLevel = map.getMaxZoomLevel();
        mMarkerDescriptor = bitmapDescriptor;
    }

    public void setZoomLevel(float zoom) {
        mZoomLevel = zoom;
    }



    @Override
    protected boolean shouldRenderAsCluster(Cluster<MapLocation> cluster) {
        // Don't cluster if we're at the max zoom level
        if (mMaxZoomLevel > mZoomLevel) {
            return super.shouldRenderAsCluster(cluster);
        } else {
            return false;
        }
    }

    @Override
    protected void onBeforeClusterItemRendered(MapLocation item, MarkerOptions markerOptions) {
        markerOptions.icon(mMarkerDescriptor);
    }
}
