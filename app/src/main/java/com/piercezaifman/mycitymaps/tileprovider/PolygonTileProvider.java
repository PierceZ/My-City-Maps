package com.piercezaifman.mycitymaps.tileprovider;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;
import com.google.maps.android.geometry.Point;
import com.google.maps.android.projection.SphericalMercatorProjection;
import com.piercezaifman.mycitymaps.App;
import com.piercezaifman.mycitymaps.R;
import com.piercezaifman.mycitymaps.util.Util;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Used to draw polygons on the map.
 * <p>
 * Created by piercezaifman on 2017-02-09.
 */

public class PolygonTileProvider implements TileProvider {
    private static final int TILE_SIZE = 256;
    private static final int SCALE = 2;
    private static final int DIMENSION = SCALE * TILE_SIZE;
    private static final SphericalMercatorProjection PROJECTION = new SphericalMercatorProjection(TILE_SIZE);

    private final List<PolygonOptions> mPolygons;
    private final Map<PolygonOptions, LatLngBounds> mBounds;

    public PolygonTileProvider(@NonNull List<PolygonOptions> polygons) {
        super();
        mPolygons = new ArrayList<>(polygons);
        mBounds = new HashMap<>();
        setupPolygonBounds();
    }

    private void setupPolygonBounds() {
        for (PolygonOptions polygon : mPolygons) {
            List<LatLng> points = polygon.getPoints();
            if (points != null && points.size() > 1) {
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (LatLng latLng : polygon.getPoints()) {
                    builder.include(latLng);
                }
                mBounds.put(polygon, builder.build());
            }
        }
    }

    @Override
    public Tile getTile(int x, int y, int zoom) {
        Matrix matrix = new Matrix();
        float scale = ((float) Math.pow(2, zoom) * SCALE);
        matrix.postScale(scale, scale);
        matrix.postTranslate(-x * DIMENSION, -y * DIMENSION);
        Bitmap bitmap = Bitmap.createBitmap(DIMENSION, DIMENSION, Bitmap.Config.ARGB_8888); //save memory on old phones
        Canvas c = new Canvas(bitmap);
        c.setMatrix(matrix);

        long startTime = System.nanoTime();
        boolean tileHasData = onDraw(c, getBoundsOfTile(x, y, zoom));
        Util.log("The time to draw tile", "Time to draw tile (" + zoom + "," + x + "," + y + ")" + " is: " + (System.nanoTime() - startTime) / 1000000);

        if (tileHasData) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            return new Tile(DIMENSION, DIMENSION, baos.toByteArray());
        } else {
            return NO_TILE;
        }
    }

    /**
     * Return true if something was drawn, otherwise false.
     */
    private boolean onDraw(Canvas canvas, LatLngBounds bounds) {
        Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        int color = ContextCompat.getColor(App.getApp(), R.color.colorPrimary);
        fillPaint.setShadowLayer(0, 0, 0, 0);
        fillPaint.setColor(color);
        fillPaint.setStyle(Paint.Style.FILL);

        Iterable<List<Path>> result = Util.parseDataInParallel(mPolygons, (sublist) -> {
            List<Path> paths = new ArrayList<>();
            for (int i = 0; i < sublist.size(); i++) {
                PolygonOptions polygon = sublist.get(i);
                List<LatLng> boundary = polygon.getPoints();
                if (boundary != null && boundary.size() > 1 && boundsIntersect(bounds, mBounds.get(polygon))) {
                    Path path = new Path();
                    path.incReserve(boundary.size());
                    Point screenPt1 = PROJECTION.toPoint(boundary.get(0));
                    path.moveTo((float) screenPt1.x, (float) screenPt1.y);
                    for (int j = 1; j < boundary.size(); j++) {
                        Point screenPt2 = PROJECTION.toPoint(boundary.get(j));
                        path.lineTo((float) screenPt2.x, (float) screenPt2.y);
                    }
                    path.close();

                    List<List<LatLng>> holes = polygon.getHoles();
                    if (holes != null && holes.size() > 0) {
                        for (List<LatLng> hole : holes) {
                            if (hole.size() > 0) {
                                path.incReserve(hole.size());
                                screenPt1 = PROJECTION.toPoint(hole.get(0));
                                path.moveTo((float) screenPt1.x, (float) screenPt1.y);
                                for (int j = 1; j < hole.size(); j++) {
                                    Point screenPt2 = PROJECTION.toPoint(hole.get(j));
                                    path.lineTo((float) screenPt2.x, (float) screenPt2.y);
                                }
                                path.close();
                            }
                        }
                    }


                    path.setFillType(Path.FillType.EVEN_ODD);

                    paths.add(path);
                }
            }
            return paths;
        });

        boolean hasData = false;
        for (List<Path> paths : result) {
            for (Path path : paths) {
                if (!canvas.quickReject(path, Canvas.EdgeType.AA)) {
                    canvas.drawPath(path, fillPaint);
                }
            }
            hasData = hasData || paths.size() > 0;
        }

        return hasData;
    }

    private LatLngBounds getBoundsOfTile(int x, int y, int zoom) {
        int noTiles = (1 << zoom);
        double longitudeSpan = 360.0 / noTiles;
        double longitudeMin = -180.0 + x * longitudeSpan;

        double mercatorMax = 180 - (((double) y) / noTiles) * 360;
        double mercatorMin = 180 - (((double) y + 1) / noTiles) * 360;
        double latitudeMax = toLatitude(mercatorMax);
        double latitudeMin = toLatitude(mercatorMin);

        return new LatLngBounds(new LatLng(latitudeMin, longitudeMin),
                new LatLng(latitudeMax, longitudeMin + longitudeSpan));
    }

    private double toLatitude(double mercator) {
        double radians = Math.atan(Math.exp(Math.toRadians(mercator)));
        return Math.toDegrees(2 * radians) - 90;
    }

    private boolean boundsIntersect(LatLngBounds bounds1, LatLngBounds bounds2) {
        boolean latIntersects =
                (bounds2.northeast.latitude >= bounds1.southwest.latitude) && (bounds2.southwest.latitude <= bounds1.northeast.latitude);
        boolean lngIntersects =
                (bounds2.northeast.longitude >= bounds1.southwest.longitude) && (bounds2.southwest.longitude <= bounds1.northeast.longitude);

        return latIntersects && lngIntersects;
    }
}
