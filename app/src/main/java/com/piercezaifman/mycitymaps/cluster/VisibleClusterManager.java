package com.piercezaifman.mycitymaps.cluster;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.clustering.ClusterManager;
import com.piercezaifman.mycitymaps.data.MapLocation;
import com.piercezaifman.mycitymaps.util.Util;

import java.lang.reflect.Field;

/**
 * Hacky class to speed up marker rendering, based on https://github.com/googlemaps/android-maps-utils/issues/29#issuecomment-176674872
 * <p>
 * Created by piercezaifman on 2017-03-03.
 */

public class VisibleClusterManager extends ClusterManager<MapLocation> {

    private static final float PERCENTAGE_PADDING = 0.30f;

    private GoogleMap mMap2;
    private ColorClusterRenderer mRenderer2;
    private VisibleNonHierarchicalDistanceBasedAlgorithm<MapLocation> mVisibleAlgorithm;
    private boolean mDynamicMarkersEnabled = false;
    public static LatLngBounds sBounds;

    public VisibleClusterManager(Context context, GoogleMap map, VisibleNonHierarchicalDistanceBasedAlgorithm<MapLocation> algorithm) {
        super(context, map);
        mMap2 = map;
        mVisibleAlgorithm = algorithm;
    }

    public void setDynamicMarkersEnabled() {
        //Should only call this once
        if (!mDynamicMarkersEnabled) {
            //super hacky reflection
            try {
                Field field = getClass().getSuperclass().getDeclaredField("mAlgorithm");
                field.setAccessible(true);
                field.set(this, mVisibleAlgorithm);
            } catch (Exception e) {
                Util.log("Reflection", "Reflection failed for some reason", e);
            }
            mDynamicMarkersEnabled = true;
        }
    }

    public void setRenderer(ColorClusterRenderer renderer) {
        super.setRenderer(renderer);
        mRenderer2 = renderer;
    }

    @Override
    public void onCameraIdle() {
        if (mDynamicMarkersEnabled) {
            LatLngBounds b = mMap2.getProjection().getVisibleRegion().latLngBounds;
            LatLng bNE = b.northeast;
            LatLng bSW = b.southwest;
            double bAdjustLat = (bNE.latitude - bSW.latitude) * PERCENTAGE_PADDING; // % increase
            double bAdjustLon = (bNE.longitude - bSW.longitude) * PERCENTAGE_PADDING; // % increase
            bNE = new LatLng(bNE.latitude + bAdjustLat, bNE.longitude + bAdjustLon);
            bSW = new LatLng(bSW.latitude - bAdjustLat, bSW.longitude - bAdjustLon);
            sBounds = new LatLngBounds(bSW, bNE);

            if (mRenderer2 instanceof GoogleMap.OnCameraIdleListener) {
                ((GoogleMap.OnCameraIdleListener) mRenderer2).onCameraIdle();
            }

            cluster();
        } else {
            super.onCameraIdle();
        }
    }

    public static LatLngBounds getBounds() {
        return sBounds;
    }
}
