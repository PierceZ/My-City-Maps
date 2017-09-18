package com.piercezaifman.mycitymaps.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.maps.android.clustering.ClusterItem;
import com.piercezaifman.mycitymaps.MapOptionsHolder;
import com.piercezaifman.mycitymaps.R;
import com.piercezaifman.mycitymaps.base.BaseActivity;
import com.piercezaifman.mycitymaps.cluster.ColorClusterRenderer;
import com.piercezaifman.mycitymaps.cluster.VisibleClusterManager;
import com.piercezaifman.mycitymaps.cluster.VisibleNonHierarchicalDistanceBasedAlgorithm;
import com.piercezaifman.mycitymaps.data.City;
import com.piercezaifman.mycitymaps.data.MapLocation;
import com.piercezaifman.mycitymaps.fragment.NearbyLocationsFragment;
import com.piercezaifman.mycitymaps.kml.KmlGeometry;
import com.piercezaifman.mycitymaps.kml.KmlLineString;
import com.piercezaifman.mycitymaps.kml.KmlMultiGeometry;
import com.piercezaifman.mycitymaps.kml.KmlParser;
import com.piercezaifman.mycitymaps.kml.KmlPlacemark;
import com.piercezaifman.mycitymaps.kml.KmlPoint;
import com.piercezaifman.mycitymaps.kml.KmlPolygon;
import com.piercezaifman.mycitymaps.tileprovider.PolygonTileProvider;
import com.piercezaifman.mycitymaps.tileprovider.PolylineTileProvider;
import com.piercezaifman.mycitymaps.util.RxBus;
import com.piercezaifman.mycitymaps.util.Util;
import com.tbruyelle.rxpermissions.RxPermissions;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MapActivity extends BaseActivity implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<LocationSettingsResult>,
        LocationListener {

    public static final String EXTRA_CITY = "EXTRA_CITY";
    public static final String EXTRA_FILENAME = "EXTRA_FILENAME";

    private static final String KML_KEY_LON = "lon";
    private static final String KML_KEY_LAT = "lat";
    private static final String KML_KEY_NAME = "name";
    private static final String KML_KEY_DESCRIPTION = "description";
    private static final String KEY_TIMESTAMP_PREFIX = "TIMESTAMP_";

    private static final int REQUEST_CODE_SETTINGS = 112;

    private static final int CAMERA_PADDING = 25;
    private static final int MAX_ANIMATED_MARKERS = 1000;

    @BindView(R.id.toolbar) Toolbar mToolbar;

    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private GoogleMap mMap;
    private List<LatLng> mLocations = new ArrayList<>();
    private List<MapLocation> mMapLocations = new ArrayList<>();
    private VisibleClusterManager mClusterManager;
    private MapOptionsHolder mMapOptionsHolder = new MapOptionsHolder();
    private BitmapDescriptor mMarkerDescriptor;
    private boolean mShowNearestPlacemark;
    private boolean mMapIsLoaded;
    private boolean mFileDownloaded;
    private File mMapFile;
    private String mMapFileName;
    private City mCity;
    private Marker mLocationMarker;

    private final GoogleMap.InfoWindowAdapter mInfoWindowAdapter = new GoogleMap.InfoWindowAdapter() {

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
            LinearLayout info = new LinearLayout(MapActivity.this);
            info.setOrientation(LinearLayout.VERTICAL);

            TextView title = new TextView(MapActivity.this);
            title.setTextColor(Color.BLACK);
            title.setTypeface(null, Typeface.BOLD);
            title.setText(marker.getTitle());

            TextView snippet = new TextView(MapActivity.this);
            snippet.setTextColor(Color.GRAY);
            snippet.setText(marker.getSnippet());

            info.addView(title);
            info.addView(snippet);

            return info;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        ButterKnife.bind(this);


        if (savedInstanceState != null) {
            mMapFileName = savedInstanceState.getString(EXTRA_FILENAME);
            mCity = savedInstanceState.getParcelable(EXTRA_CITY);
        } else {
            Bundle extras = getIntent().getExtras();
            mMapFileName = extras.getString(EXTRA_FILENAME);
            mCity = extras.getParcelable(EXTRA_CITY);
        }

        setSupportActionBar(mToolbar);
        setTitle(Util.formatMapFileName(mMapFileName));

        ActionBar actionBar = getSupportActionBar();
        //This should never be null, but just to be safe
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        FragmentManager fragmentManager = getSupportFragmentManager();

        NearbyLocationsFragment nearbyLocationsFragment = NearbyLocationsFragment.newInstance();
        fragmentManager.beginTransaction()
                .replace(R.id.nearby_locations, nearbyLocationsFragment)
                .commit();

        SupportMapFragment supportMapFragment = SupportMapFragment.newInstance();
        fragmentManager.beginTransaction()
                .replace(R.id.map, supportMapFragment)
                .commit();

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        supportMapFragment.getMapAsync(this);

        loadMapFile();

        RxBus.subscribe(RxBus.SUBJECT_MAP_LOCATION_CLICKED, this, (id) -> showMarker((int) id));
    }

    private void loadMapFile() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference mapsReference = storage.getReferenceFromUrl(getString(R.string.firebase_url_maps));
        StorageReference mapReference = mapsReference.child(mCity.getCountry());
        String tempMapFileName = mCity.getCountry() + "/";
        if (mCity.getState() != null && !mCity.getState().trim().isEmpty()) {
            tempMapFileName += mCity.getState() + "/";
            mapReference = mapReference.child(mCity.getState());
        }
        tempMapFileName += mCity.getName() + "/" + mMapFileName;
        mapReference = mapReference.child(mCity.getName()).child(mMapFileName);

        final String mapFileName = tempMapFileName;
        mMapFile = new File(getFilesDir(), mapFileName);
        mMapFile.getParentFile().mkdirs();
        mFileDownloaded = mMapFile.exists();

        updateMap(mMapFile);

        mapReference.getMetadata().addOnSuccessListener(MapActivity.this, (metadata) -> {

            long timestamp = Util.getSharedPrefs().getLong(KEY_TIMESTAMP_PREFIX + mapFileName, 0);
            if ((metadata.getUpdatedTimeMillis() > timestamp || !mMapFile.exists()) && metadata.getReference() != null) {
                mFileDownloaded = false;
                metadata.getReference().getFile(mMapFile).addOnSuccessListener(taskSnapshot -> {
                    mFileDownloaded = true;
                    updateMap(mMapFile);
                    Util.getSharedPrefs().edit().putLong(KEY_TIMESTAMP_PREFIX + mapFileName, metadata.getUpdatedTimeMillis()).apply();
                }).addOnFailureListener(exception -> {
                    Util.log("Update Map", "Failed to download map " + mMapFileName, exception);
                });
            }
        }).addOnFailureListener(MapActivity.this, (exception) -> {
            Util.log("Metadata", "Failed to load metadata " + mMapFileName, exception);

            //If we already had a file we don't need to show an error
            if (!mMapFile.exists()) {
                Snackbar.make(findViewById(android.R.id.content), R.string.map_error_loading, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(EXTRA_FILENAME, mMapFileName);
        outState.putParcelable(EXTRA_CITY, mCity);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        refreshLastLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    private void refreshLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (location != null) {
                mLastLocation = location;
                publishMapUpdate();
            } else if (Util.isLocationEnabled()) {
                // if location is null and location services IS enabled, then try to do a location request
                LocationRequest locationRequest = new LocationRequest();
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.activity_map_nearby:
                Util.logEvent(R.string.ga_category_map, R.string.ga_action_show_nearest, mCity.toString() + "/" + mMapFileName);
                showNearestPlacemark();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showNearestPlacemark() {
        if (mMap == null) {
            Snackbar.make(findViewById(android.R.id.content), R.string.map_error_not_finished_loading, Snackbar.LENGTH_LONG).show();
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
                refreshLastLocation();

                mShowNearestPlacemark = true;

                if (mMapLocations.size() > 0 && mLastLocation != null) {
                    mShowNearestPlacemark = false;

                    // Just pick a random marker to start with and check if any markers are closer
                    MapLocation nearestMarker = mMapLocations.get(0);
                    Location markerLocation = new Location("");

                    markerLocation.setLatitude(nearestMarker.getPosition().latitude);
                    markerLocation.setLongitude(nearestMarker.getPosition().longitude);
                    float closestDistance = markerLocation.distanceTo(mLastLocation);

                    for (MapLocation marker : mMapLocations) {
                        markerLocation.setLatitude(marker.getPosition().latitude);
                        markerLocation.setLongitude(marker.getPosition().longitude);
                        float distance = mLastLocation.distanceTo(markerLocation);
                        if (distance < closestDistance) {
                            closestDistance = distance;
                            nearestMarker = marker;
                        }
                    }

                    showMarker(nearestMarker);
                } else if (mLocations.size() > 0 && mLastLocation != null) {
                    LatLng nearestLocation = mLocations.get(0);
                    Location markerLocation = new Location("");

                    markerLocation.setLatitude(nearestLocation.latitude);
                    markerLocation.setLongitude(nearestLocation.longitude);
                    float closestDistance = markerLocation.distanceTo(mLastLocation);

                    for (LatLng latLng : mLocations) {
                        markerLocation.setLatitude(latLng.latitude);
                        markerLocation.setLongitude(latLng.longitude);
                        float distance = mLastLocation.distanceTo(markerLocation);
                        if (distance < closestDistance) {
                            closestDistance = distance;
                            nearestLocation = latLng;
                        }
                    }

                    showMarker(nearestLocation, getString(R.string.map_nearest_marker_title), getString(R.string.map_nearest_marker_snippet));
                } else if (mLastLocation == null) {
                    checkLocationServicesEnabled();
                }
            } else {
                new RxPermissions(this).request(Manifest.permission.ACCESS_FINE_LOCATION).subscribe((granted) -> {
                    if (granted) {
                        showNearestPlacemark();
                        Util.logEvent(R.string.ga_category_map, R.string.ga_action_request_location_permission, getString(R.string.ga_label_yes));
                    } else {
                        Util.logEvent(R.string.ga_category_map, R.string.ga_action_request_location_permission, getString(R.string.ga_label_no));
                    }
                });
            }
        }
    }

    /**
     * Display the details of the marker and focus the camera on it.
     */
    private void showMarker(Marker marker) {
        if (marker != null) {
            marker.showInfoWindow();
            mMap.moveCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
        }
    }

    /**
     * Search for the marker with the specified id and show it.
     */
    private void showMarker(int index) {
        showMarker(mMapLocations.get(index));
    }

    /**
     * Create a marker from the MapLocation and then show it.
     */
    private void showMarker(MapLocation marker) {
        if (marker != null) {
            showMarker(marker.getPosition(), marker.getTitle(), marker.getSubTitle());
        }
    }

    /**
     * Make a marker and show it, also remove the last marker we showed this way.
     */
    private void showMarker(LatLng location, String title, String snippet) {
        if (mLocationMarker != null) {
            mLocationMarker.remove();
            mLocationMarker = null;
        }

        MarkerOptions marker = new MarkerOptions();
        marker.position(location)
                .title(title)
                .snippet(snippet)
                .icon(mMarkerDescriptor);

        mLocationMarker = mMap.addMarker(marker);

        showMarker(mLocationMarker);
    }

    private void checkLocationServicesEnabled() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(this);
    }

    @Override
    public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
        final Status status = locationSettingsResult.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                try {
                    status.startResolutionForResult(this, REQUEST_CODE_SETTINGS);
                } catch (IntentSender.SendIntentException e) {
                    Util.log("Location dialog", "PendingIntent unable to execute request.", e);
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        showNearestPlacemark();
                        break;
                }
                break;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            mLastLocation = location;

            // We just want to get their location once, so we'll cancel updates after that
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            if (mShowNearestPlacemark) {
                showNearestPlacemark();
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLoadedCallback(this);
    }

    @Override
    public void onMapLoaded() {
        // Just make the descriptor once, since we're going to reuse it a bunch
        float[] hsv = new float[3];
        Color.colorToHSV(ContextCompat.getColor(this, R.color.colorPrimaryDark), hsv);
        mMarkerDescriptor = BitmapDescriptorFactory.defaultMarker(hsv[0]);

        mMap.setInfoWindowAdapter(mInfoWindowAdapter);

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        mClusterManager = new VisibleClusterManager(this, mMap, new VisibleNonHierarchicalDistanceBasedAlgorithm<>());
        ColorClusterRenderer renderer = new ColorClusterRenderer(this, mMap, mClusterManager, mMarkerDescriptor);
        mClusterManager.setRenderer(renderer);
        mClusterManager.setAnimation(true);

        mMapIsLoaded = true;
        updateMap(mMapFile);

        mClusterManager.setOnClusterClickListener(cluster -> {
            LatLngBounds.Builder builder = LatLngBounds.builder();
            for (ClusterItem item : cluster.getItems()) {
                builder.include(item.getPosition());
            }
            final LatLngBounds bounds = builder.build();
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, CAMERA_PADDING));
            return true;
        });

        mMap.setOnInfoWindowClickListener(marker -> {
            double lat = marker.getPosition().latitude;
            double lon = marker.getPosition().longitude;
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + lat + "," + lon);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        });

        mMap.setOnCameraIdleListener(() -> {
            renderer.setZoomLevel(mMap.getCameraPosition().zoom);
            mClusterManager.onCameraIdle();
        });
        mMap.setOnMarkerClickListener(mClusterManager);
    }

    private void updateMap(File mapFile) {
        if (mapFile != null && mapFile.exists() && mMapIsLoaded && mFileDownloaded) {
            Observable.fromCallable(() -> convertFileToKml(mapFile))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::updateMap);
        }
    }

    private void updateMap(KmlParser kmlParser) {
        if (kmlParser != null && mMap != null) {
            mMap.clear();
            Observable.fromCallable(() -> prepMap(kmlParser))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::showMap);
        } else {
            Snackbar.make(findViewById(android.R.id.content), R.string.map_error_loading, Snackbar.LENGTH_LONG).show();
        }
    }

    private List<TileOverlayOptions> prepMap(@NonNull KmlParser kmlParser) {
        mLocations.clear();
        mMapLocations.clear();
        convertKml(kmlParser);
        setupMapLocations();

        List<TileOverlayOptions> tileOverlayList = new ArrayList<>();
        if (mMapOptionsHolder.getPolylineOptions().size() > 0) {
            tileOverlayList.add(new TileOverlayOptions().tileProvider(new PolylineTileProvider(mMapOptionsHolder.getPolylineOptions())));
        }
        if (mMapOptionsHolder.getPolygonOptions().size() > 0) {
            tileOverlayList.add(new TileOverlayOptions().tileProvider(new PolygonTileProvider(mMapOptionsHolder.getPolygonOptions())));
        }

        return tileOverlayList;
    }

    private void showMap(@NonNull List<TileOverlayOptions> tileOverlayOptions) {
        long startTime = System.nanoTime();

        for (TileOverlayOptions tileOverlayOption : tileOverlayOptions) {
            mMap.addTileOverlay(tileOverlayOption);
        }

        setupCluster();
        moveCamera();
        showHideNearbyLocations();
        publishMapUpdate();

        mMapOptionsHolder.clear();

        Util.log("Time to show map", "Time to show is: " + (System.nanoTime() - startTime) / 1000000);
    }

    /**
     * If there are no markers, hide the bottom fragment. Otherwise show it.
     */
    private void showHideNearbyLocations() {
        View v = findViewById(R.id.nearby_locations_container);
        if (mMapLocations.size() > 0) {
            v.setVisibility(View.VISIBLE);
        } else {
            v.setVisibility(View.GONE);
        }
    }

    private void setupMapLocations() {
        List<MapLocation> locations = new ArrayList<>();
        Location markerLocation = new Location("");

        for (MarkerOptions marker : mMapOptionsHolder.getMarkerOptions()) {
            markerLocation.setLatitude(marker.getPosition().latitude);
            markerLocation.setLongitude(marker.getPosition().longitude);
            float distance = -1;
            if (mLastLocation != null) {
                distance = markerLocation.distanceTo(mLastLocation);
            }
            MapLocation location = new MapLocation(distance, marker.getTitle(), marker.getSnippet(), -1, marker.getPosition());
            locations.add(location);
        }

        // Sorting just so the clusters are consistent, otherwise they change every time you load the map
        Collections.sort(locations, (m1, m2) -> m1.getTitle().compareTo(m2.getTitle()));

        // Update the ID for each location so we can map them back to the right value when passing data around
        for (int i = 0; i < locations.size(); i++) {
            MapLocation location = locations.get(i);
            location.setId(i);
        }

        mMapLocations.clear();
        mMapLocations.addAll(locations);
    }

    private void setupCluster() {
        if (mClusterManager != null) {
            if (mMapLocations.size() > MAX_ANIMATED_MARKERS) {
                mClusterManager.setDynamicMarkersEnabled();
            }
            mClusterManager.setAnimation(mMapLocations.size() < MAX_ANIMATED_MARKERS);
            mClusterManager.clearItems();
            mClusterManager.addItems(mMapLocations);
            mClusterManager.cluster();
        }
    }

    /**
     * Send out map update so the nearby locations fragment can update itself with the data.
     */
    private void publishMapUpdate() {
        if (mMapLocations.size() > 0) {
            RxBus.publish(RxBus.SUBJECT_MAP_UPDATED, mMapLocations);
        }
    }

    private KmlParser convertFileToKml(File file) {
        KmlParser parser = null;
        try {
            parser = new KmlParser();
            parser.parseKml(file);
        } catch (XmlPullParserException | IOException e) {
            Util.log("Kml import", "It broke " + mMapFileName, e);
        }
        return parser;
    }

    /**
     * Convert the KML parser to markers and polygons in the map. That way we can actually
     * modify them and add listeners.
     */
    private void convertKml(KmlParser kmlParser) {
        long startTime = System.nanoTime();

        // There should always be placemarks, something went wrong if there are none
        if (kmlParser.getPlacemarks() != null && kmlParser.getPlacemarks().size() > 0) {

            for (KmlPlacemark kmlPlacemark : kmlParser.getPlacemarks()) {
                parseKmlPlacemark(kmlPlacemark);
            }
        }

        Util.log("Time to convert kml", "Time to convert is: " + (System.nanoTime() - startTime) / 1000000);
    }

    private void parseKmlPlacemark(KmlPlacemark kmlPlacemark) {
        KmlGeometry geometry = kmlPlacemark.getGeometry();

        if (geometry instanceof KmlLineString) {
            KmlLineString kmlLine = (KmlLineString) geometry;
            PolylineOptions polyline = new PolylineOptions();
            polyline.color(ContextCompat.getColor(this, R.color.colorPrimary));
            polyline.addAll(kmlLine.getGeometryObject());

            ArrayList<LatLng> list = kmlLine.getGeometryObject();
            if (list.size() > 1) {
                mLocations.add(list.get(0));
                mLocations.add(list.get(list.size() - 1));
            }

            mMapOptionsHolder.addPolylineOptions(polyline);
        } else if (geometry instanceof KmlMultiGeometry) {
            boolean addedMarker = addMarkerFromNonPoint(kmlPlacemark, null);

            KmlMultiGeometry multiGeometry = (KmlMultiGeometry) geometry;
            for (KmlGeometry kmlGeometry : multiGeometry.getGeometryObject()) {
                HashMap<String, String> propertyMap = new HashMap<>();

                // If we didn't add a marker, try adding the properties to the children geometries
                if (!addedMarker) {
                    for (Object object : kmlPlacemark.getProperties()) {
                        if (object instanceof Map.Entry) {
                            Map.Entry entry = (Map.Entry) object;
                            if (entry.getKey() instanceof String && entry.getValue() instanceof String) {
                                String key = (String) entry.getKey();
                                String value = (String) entry.getValue();
                                propertyMap.put(key, value);
                            }
                        }
                    }
                }
                KmlPlacemark multiPlaceMark = new KmlPlacemark(kmlGeometry, kmlPlacemark.getStyleId(), kmlPlacemark.getInlineStyle(), propertyMap);
                parseKmlPlacemark(multiPlaceMark);
            }
        } else if (geometry instanceof KmlPolygon) {
            KmlPolygon kmlPolygon = (KmlPolygon) geometry;
            PolygonOptions polygon = new PolygonOptions();
            polygon.fillColor(ContextCompat.getColor(this, R.color.colorPrimary));
            polygon.strokeColor(ContextCompat.getColor(this, android.R.color.black));
            polygon.addAll(kmlPolygon.getOuterBoundaryCoordinates());

            if (kmlPolygon.getOuterBoundaryCoordinates().size() > 0) {
                addMarkerFromNonPoint(kmlPlacemark, kmlPolygon.getOuterBoundaryCoordinates().get(0));
            }

            for (ArrayList<LatLng> holePoints : kmlPolygon.getInnerBoundaryCoordinates()) {
                polygon.addHole(holePoints);
            }

            mMapOptionsHolder.addPolygonOptions(polygon);
        } else if (geometry instanceof KmlPoint) {
            KmlPoint point = (KmlPoint) geometry;

            String title = kmlPlacemark.getProperty(KML_KEY_NAME);
            title = title == null ? "" : title.trim();
            String snippet = kmlPlacemark.getProperty(KML_KEY_DESCRIPTION);

            //Put the rest of the properties at the end of the snippet
            for (Object object : kmlPlacemark.getProperties()) {
                if (object instanceof Map.Entry) {
                    Map.Entry entry = (Map.Entry) object;
                    if (entry.getKey() instanceof String && entry.getValue() instanceof String) {
                        String key = (String) entry.getKey();
                        String value = (String) entry.getValue();
                        if (!KML_KEY_NAME.equals(key) && !KML_KEY_DESCRIPTION.equals(key)) {
                            if (snippet == null) {
                                snippet = value;
                            } else {
                                snippet += "\n" + value;
                            }
                        }
                    }
                }
            }

            addMarker(point.getGeometryObject(), title, snippet);
        }
    }

    /**
     * Make a marker for something that isn't a point, if possible.
     *
     * @return True if it added a marker, false otherwise.
     */
    private boolean addMarkerFromNonPoint(@NonNull KmlPlacemark kmlPlacemark, @Nullable LatLng latLng) {
        boolean success = false;
        if (kmlPlacemark.hasProperty(KML_KEY_NAME) && kmlPlacemark.hasProperty(KML_KEY_DESCRIPTION)
                && kmlPlacemark.hasProperty(KML_KEY_LAT) && kmlPlacemark.hasProperty(KML_KEY_LON)) {
            String title = kmlPlacemark.getProperty(KML_KEY_NAME);
            String snippet = kmlPlacemark.getProperty(KML_KEY_DESCRIPTION);
            double lat = Double.parseDouble(kmlPlacemark.getProperty(KML_KEY_LAT));
            double lon = Double.parseDouble(kmlPlacemark.getProperty(KML_KEY_LON));

            addMarker(new LatLng(lat, lon), title, snippet);
            success = true;
        } else if (kmlPlacemark.hasProperty(KML_KEY_NAME) && latLng != null) {
            String title = kmlPlacemark.getProperty(KML_KEY_NAME);
            String snippet = null;
            if (kmlPlacemark.hasProperty(KML_KEY_DESCRIPTION)) {
                snippet = kmlPlacemark.getProperty(KML_KEY_DESCRIPTION);
            }
            addMarker(latLng, title, snippet);
            success = true;
        }

        return success;
    }

    private void addMarker(LatLng latLng, String title, String snippet) {
        MarkerOptions marker = new MarkerOptions();
        marker.position(latLng)
                .title(title)
                .snippet(snippet)
                .icon(mMarkerDescriptor);

        mMapOptionsHolder.addMarkerOptions(marker);
    }

    private void moveCamera() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        boolean hasData = false;

        for (PolylineOptions line : mMapOptionsHolder.getPolylineOptions()) {
            for (LatLng latLng : line.getPoints()) {
                builder.include(latLng);
                hasData = true;
            }
        }

        for (PolygonOptions polygon : mMapOptionsHolder.getPolygonOptions()) {
            for (LatLng latLng : polygon.getPoints()) {
                builder.include(latLng);
                hasData = true;
            }
        }

        for (MapLocation marker : mMapLocations) {
            builder.include(marker.getPosition());
            hasData = true;
        }

        // this shouldn't happen, but if it does it will crash
        if (hasData) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), CAMERA_PADDING));
        } else {
            // If there's something wrong with the file, we'll just delete it and hope to try again later
            mMapFile.delete();
            Util.log("Map", "Missing data " + mMapFileName, new Exception("No data"));
            Toast.makeText(this, R.string.map_error_loading, Toast.LENGTH_LONG).show();
        }
    }
}
