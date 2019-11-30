package com.dewidar.makanny.map;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import com.dewidar.makanny.Gui_Manager;
import com.dewidar.makanny.R;
import com.dewidar.makanny.WaitingFragment;
import com.google.android.gms.maps.model.LatLng;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;

import static com.dewidar.makanny.map.DrawRouts.drawPointsFinished;


public class OSMdroid implements SearchListener {
    private MapView amsMap;
    private IMapController controller;
    private GeoPoint currentLocation, hospitalLocation, patientPoint;
    private DrawRouts currRoad;
    private Handler handler;
    private ProgressBar progressBar;
    private ImageView searchOnMap;
    private ImageView userCurrentLocation;
    private int progressStatus = 0;
    private DrawRouts drawcost;
    private GeoDistanceAlgorithm geoDistanceAlgorithm = new GeoDistanceAlgorithm(true);
    private MyLocationNewOverlay mLocationOverlay;
    private static boolean startBtnClicked = false;
    private static List<LatLng> currRoadLatsLng = new ArrayList();
    private String carNumber = "";
    private String paramedicName = "";


    private CountDownTimer timer;
    private int waitedTime = 0;
    private String status;
    private PopupWindow popupWindow;
    private Context context = Gui_Manager.getInstance().getContext();
    private String[] PERMISSIONS_LOCATION = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
    };

    private TextView onPathToPatient, arrivedToPatient, waitingForPatient,
            onTheRoadToDestination, arriveToDestination, waitingForHandOver, patientHandedOver, patientRejected,
            patientRelocation, onRouteToStationBase, end;
    private View view;

    public OSMdroid(MapView amsmap, Handler handler, ProgressBar progressBar, ImageView searchOnMap, ImageView userCurrentLocation, View view) {
        this.amsMap = amsmap;
        this.handler = handler;
        this.progressBar = progressBar;
        this.searchOnMap = searchOnMap;
        this.userCurrentLocation = userCurrentLocation;
        this.view = view;
    }

    public void setup() {
        checkPermission();
        setupOverlay();
        geoDistanceAlgorithm.setOsMdroid(this);
        geoDistanceAlgorithm.setHandler(handler);
        this.controller = amsMap.getController();
        drawcost = new DrawRouts(amsMap, context.getApplicationContext(), handler, currRoadLatsLng);
//        locationListener();
        currentLocationOnChangListener();

        clickableMapViewsHandler();
        onUserClickOnMap();
        progressBarRout();
        // get drawn rout points to check if user on track or not
        currRoad = new DrawRouts(amsMap, context, handler, currRoadLatsLng);
        initCurrentLocation();
    }

    public void initCurrentLocation() {
        GpsMyLocationProvider gpsMyLocationProvider = new GpsMyLocationProvider(context.getApplicationContext());
        mLocationOverlay = new MyLocationNewOverlay(gpsMyLocationProvider, amsMap);
        mLocationOverlay.enableMyLocation();
        mLocationOverlay.runOnFirstFix(new Runnable() {
            @Override
            public void run() {
                do {
                    currentLocation = mLocationOverlay.getMyLocation();
                } while (currentLocation != null);
            }
        });
        amsMap.postInvalidate();
    }


    private void checkPermission() {

        int permission = ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            makeRequestPermissions();
        }
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));

        amsMap.setTileSource(TileSourceFactory.MAPNIK);
        amsMap.setBuiltInZoomControls(true);
        amsMap.setMultiTouchControls(true);

        GpsMyLocationProvider gpsMyLocationProvider = new GpsMyLocationProvider(context.getApplicationContext());
        final MyLocationNewOverlay mLocationOverlay = new MyLocationNewOverlay(gpsMyLocationProvider, amsMap);
        mLocationOverlay.enableMyLocation();
        mLocationOverlay.runOnFirstFix(new Runnable() {
            @Override
            public void run() {
                final GeoPoint curr = mLocationOverlay.getMyLocation();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        amsMap.getController().setZoom(11.0);
                        amsMap.getController().animateTo(curr);
                    }
                });

            }
        });
        amsMap.postInvalidate();

        // enable current location overlay
        drawCurrentLocationOverlay();
        // check wifi
        if (!haveNetworkConnection()) {
            openData();
        }

        // check gps statue
        new GpsUtils(context).turnGPSOn(new GpsUtils.onGpsListener() {
            @Override
            public void gpsStatus(boolean isGPSEnable) {
                Log.i("gps", String.valueOf(isGPSEnable));
            }
        });

    }

    private void clickableMapViewsHandler() {

        searchOnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (currentLocation != null) {
                    SearchScreen searchScreen = new SearchScreen();
                    searchScreen.setCurrentLocation(currentLocation);
                    searchScreen.setSearchListener(OSMdroid.this);
                    Gui_Manager.getInstance().setCurrentFragment(searchScreen);
                } else {
                    Gui_Manager.getInstance().setCurrentFragment(new WaitingFragment());
                    GpsMyLocationProvider gpsMyLocationProvider = new GpsMyLocationProvider(context.getApplicationContext());
                    mLocationOverlay = new MyLocationNewOverlay(gpsMyLocationProvider, amsMap);
                    mLocationOverlay.enableMyLocation();
                    mLocationOverlay.runOnFirstFix(new Runnable() {
                        @Override
                        public void run() {
                            final GeoPoint curr = mLocationOverlay.getMyLocation();
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    SearchScreen searchScreen = new SearchScreen();
                                    searchScreen.setCurrentLocation(curr);
                                    searchScreen.setSearchListener(OSMdroid.this);
                                    Gui_Manager.getInstance().setCurrentFragment(searchScreen);
                                    Gui_Manager.getInstance().getFragmentManager().popBackStack();
                                }
                            });

                        }
                    });
                    amsMap.postInvalidate();

                }

            }

        });

        userCurrentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userCurrentLocation.setAlpha(0.5f);
                startBtnClicked = true;
                GpsMyLocationProvider gpsMyLocationProvider = new GpsMyLocationProvider(context.getApplicationContext());
                mLocationOverlay = new MyLocationNewOverlay(gpsMyLocationProvider, amsMap);
                mLocationOverlay.enableMyLocation();
                mLocationOverlay.runOnFirstFix(new Runnable() {
                    @Override
                    public void run() {
                        final GeoPoint curr = mLocationOverlay.getMyLocation();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                amsMap.getController().animateTo(curr);
                                amsMap.getController().setZoom(19.0);
                            }
                        });

                    }
                });
                amsMap.postInvalidate();
            }
        });
    }


    public void setupOverlay() {

        CompassOverlay mCompassOverlay = new CompassOverlay(context, new InternalCompassOrientationProvider(context), amsMap);
        mCompassOverlay.enableCompass();
        amsMap.getOverlays().add(mCompassOverlay);


        RotationGestureOverlay mRotationGestureOverlay = new RotationGestureOverlay(context, amsMap);
        mRotationGestureOverlay.setEnabled(true);
        amsMap.setMultiTouchControls(true);
        amsMap.getOverlays().add(mRotationGestureOverlay);

        final DisplayMetrics dm = context.getResources().getDisplayMetrics();
        ScaleBarOverlay mScaleBarOverlay = new ScaleBarOverlay(amsMap);
        mScaleBarOverlay.setCentred(true);
        //play around with these values to get the location on screen in the right place for your application
        mScaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, 10);
        amsMap.getOverlays().add(mScaleBarOverlay);

        drawCurrentLocationOverlay();

    }

    private void makeRequestPermissions() {

        int REQUEST_EXTERNAL_STORAGE = 1;
        ActivityCompat.requestPermissions(
                (Activity) context,
                PERMISSIONS_LOCATION,
                REQUEST_EXTERNAL_STORAGE
        );
    }

    private void drawCurrentLocationOverlay() {

        mLocationOverlay = new MyLocationNewOverlay(amsMap);
        mLocationOverlay.enableMyLocation();
        mLocationOverlay.runOnFirstFix(new Runnable() {
            @Override
            public void run() {
                mLocationOverlay.getMyLocation();
                Log.i("locationprovidar", "=" + mLocationOverlay.getMyLocation());
            }
        });
        amsMap.postInvalidate();
        amsMap.getOverlays().add(mLocationOverlay);
    }

    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;

    }

    private void openData() {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle("You Are Offline");
        alertDialog.setMessage("Please Open Your Network");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    private void onUserClickOnMap() {
        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(context, new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                Drawable nodeIcon = context.getResources().getDrawable(R.drawable.marker_default);
                Marker nodeMarker = new Marker(amsMap);
                nodeMarker.setIcon(nodeIcon);
                nodeMarker.setPosition(p);
                nodeMarker.setTitle("your destination");
                amsMap.getOverlays().add(nodeMarker);
                amsMap.invalidate();
//                Toast.makeText(context, "longTab" + p, Toast.LENGTH_SHORT).show();
                nodeMarker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(final Marker marker, MapView mapView) {
                        if (!marker.isInfoWindowShown()) {

                            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                            dialog.setTitle(marker.getTitle());
                            dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            dialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    patientPoint = marker.getPosition();
                                    GpsMyLocationProvider gpsMyLocationProvider = new GpsMyLocationProvider(context.getApplicationContext());
                                    final MyLocationNewOverlay mLocationOverlay = new MyLocationNewOverlay(gpsMyLocationProvider, amsMap);
                                    mLocationOverlay.enableMyLocation();
                                    mLocationOverlay.runOnFirstFix(new Runnable() {
                                        @Override
                                        public void run() {
                                            final GeoPoint curr = mLocationOverlay.getMyLocation();
                                            handler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    clearLastDestination();
                                                    currRoad = new DrawRouts(amsMap, context, handler, currRoadLatsLng);
                                                    currRoad.drawPath(curr, patientPoint, Color.GREEN, 10, progressBar);
                                                }
                                            });

                                        }
                                    });
                                    amsMap.postInvalidate();
                                }
                            });
                            dialog.show();
                            marker.showInfoWindow();

                        }

                        return true;
                    }
                });

                return true;
            }

        });

        amsMap.getOverlays().add(0, mapEventsOverlay);
        InfoWindow.closeAllInfoWindowsOn(amsMap);


        amsMap.addMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) {
                GeoPoint centerPoint = new GeoPoint(amsMap.getBoundingBox().getCenterLatitude(), amsMap.getBoundingBox().getCenterLongitude());
                List<LatLng> latLngs = new ArrayList<>();
                latLngs.add(new LatLng(centerPoint.getLatitude(), centerPoint.getLongitude()));
                latLngs.add(new LatLng(centerPoint.getLatitude(), centerPoint.getLongitude()));
                latLngs.add(new LatLng(centerPoint.getLatitude(), centerPoint.getLongitude()));
                GeoDistanceAlgorithm geoDistanceAlgorithm = new GeoDistanceAlgorithm(false);
                if (currentLocation != null) {
                    boolean isCentered = geoDistanceAlgorithm.isCurrentLocationFocused(latLngs, new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 25);
                    if (!isCentered) {
                        startBtnClicked = false;
                        userCurrentLocation.setAlpha(1f);
                    } else {
                        userCurrentLocation.setAlpha(0.5f);
                    }
                }
                return false;
            }

            @Override
            public boolean onZoom(ZoomEvent event) {
                if (amsMap.getZoomLevelDouble() != 19.0) {
                    startBtnClicked = false;
                } else if (amsMap.getZoomLevelDouble() == 19.0) {
                    startBtnClicked = true;
                }
                return false;
            }
        });

//        amsMap.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                
//            }
//        });
//        
//        amsMap.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(context, "mabclicked", Toast.LENGTH_SHORT).show();
//                startBtnClicked = false;
//            }
//        });
    }

    private void progressBarRout() {
        new Thread(new Runnable() {
            public void run() {
                while (progressStatus < 100) {
                    progressStatus += 1;
                    handler.post(new Runnable() {
                        public void run() {
                            progressBar.setProgress(progressStatus);
                            progressBar.getIndeterminateDrawable().setColorFilter(0xFF0000FF, android.graphics.PorterDuff.Mode.MULTIPLY);
                        }
                    });
                }
            }
        }).start();
    }


    /* @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
     public void drawRoutesFromFireBase(com.yelloco.ambulanceapp.models.Location patientLocation, com.yelloco.ambulanceapp.models.Location hospitallocation) {

         this.patientPoint = new GeoPoint(Double.parseDouble(patientLocation.getLatitude()), Double.parseDouble(patientLocation.getLongitude()));
         this.hospitalLocation = new GeoPoint(Double.parseDouble(hospitallocation.getLatitude()), Double.parseDouble(hospitallocation.getLongitude()));

         // draw rout from current location to destination
         GpsMyLocationProvider gpsMyLocationProvider = new GpsMyLocationProvider(context.getApplicationContext());
         mLocationOverlay = new MyLocationNewOverlay(gpsMyLocationProvider, amsMap);
         mLocationOverlay.enableMyLocation();
         mLocationOverlay.runOnFirstFix(new Runnable() {
             @Override
             public void run() {
                 GeoPoint curr = mLocationOverlay.getMyLocation();
                 handler.post(new Runnable() {
                     @Override
                     public void run() {
 //                        Toast.makeText(context, "patientLoc="+patientPoint + "hospital="+hospitalLocation + "currentloc="+ curr, Toast.LENGTH_LONG).show();
                         if (patientPoint != null && curr != null) {
                             currRoad = new DrawRouts(amsMap, context, handler, currRoadLatsLng);
                             currRoad.drawPath(curr, patientPoint, Color.GREEN, 10, progressBar);
                         } else if (curr == null && patientPoint != null && currentLocation != null) {
                             currRoad = new DrawRouts(amsMap, context, handler, currRoadLatsLng);
                             currRoad.drawPath(currentLocation, patientPoint, Color.GREEN, 10, progressBar);
                         }
                         if (hospitalLocation != null && patientPoint != null && !hospitalLocation.equals(patientPoint)) {
                             currRoad = new DrawRouts(amsMap, context, handler, currRoadLatsLng);
                             currRoad.drawPath(patientPoint, hospitalLocation, Color.RED, 6, progressBar);
                         }
                     }
                 });

             }
         });
         amsMap.postInvalidate();
         resetViewsOnMap();
         startTripAndCalcCost();

     }
 */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void resetViewsOnMap() {
        startBtnClicked = false;
        clickableMapViewsHandler();
        onUserClickOnMap();
        currentLocationOnChangListener();
    }

    private boolean clearLastDestination() {
        if (amsMap != null) {
            amsMap.getOverlays().clear();
            amsMap.invalidate();
            setupOverlay();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                resetViewsOnMap();
            }
            Log.i("location", "cleared");
            return true;
        }
        return false;

    }

    private void currentLocationOnChangListener() {
        final GeoDistanceAlgorithm directions = new GeoDistanceAlgorithm(false);
        GpsMyLocationProvider mGpsMyLocationProvider = new GpsMyLocationProvider(context);
        mGpsMyLocationProvider.setLocationUpdateMinTime(0);
        mGpsMyLocationProvider.setLocationUpdateMinDistance(0);
        mGpsMyLocationProvider.startLocationProvider(new IMyLocationConsumer() {

            @Override
            public void onLocationChanged(Location location, IMyLocationProvider source) {

                currentLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
                if (startBtnClicked) {
                    controller.setZoom(19.0);
                    controller.animateTo(new GeoPoint(location));
                    controller.setCenter(new GeoPoint(location));
                }


                if (currRoad != null && currRoad.getCurrRoadLatsLng().size() != 0 && startBtnClicked && drawPointsFinished) {
                    try {
                        /*
                         * direction enhancment with camera roll for future
                         * */
                        geoDistanceAlgorithm.geoDistanceCheckWithRadius(currRoad.getCurrRoadLatsLng(), new LatLng(location.getLatitude(), location.getLongitude()), 35);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (mLocationOverlay == null) {
                    final ArrayList<OverlayItem> items = new ArrayList<>();
                    items.add(new OverlayItem("Me", "My Location",
                            new GeoPoint(location)));
                    amsMap.getOverlays().add(mLocationOverlay);
                    amsMap.getController().setZoom(19.0);
                }
            }
        });

    }

    public void reDrawCorrectRoad() {
//        if (patientPoint != null) {
        amsMap.getOverlays().clear();
        amsMap.invalidate();
        setupOverlay();
        onUserClickOnMap();
        currRoadLatsLng.clear();
        currRoad = new DrawRouts(amsMap, context, handler, currRoadLatsLng);
        try {
            if (patientPoint != null)
                currRoad.drawPath(currentLocation, patientPoint, Color.GREEN, 10, progressBar);
            if (hospitalLocation != null) {
                currRoad = new DrawRouts(amsMap, context, handler, currRoadLatsLng);
                currRoad.drawPath(patientPoint, hospitalLocation, Color.RED, 6, progressBar);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//        }
    }

    private void setDirectionOrintation(GeoDistanceAlgorithm directions) {
        directions.geoDistanceCheckWithRadius(currRoad.getLatsLng(), new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 10);
        if (directions.onDirection == 1) {
            if (directions.markPos != -1) {
                amsMap.getMapOrientation();
                // TODO to be continued
                currRoad.getDirections().get(directions.markPos);
//                Toast.makeText(context, "Orientation" + directions.markPos, Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void callback(GeoPoint geoPoint, String desc) {
        startBtnClicked = false;
        Drawable nodeIcon = context.getResources().getDrawable(R.drawable.marker_default);
        Marker nodeMarker = new Marker(amsMap);
        nodeMarker.setIcon(nodeIcon);
        nodeMarker.setPosition(geoPoint);
        nodeMarker.setTitle(desc);
        amsMap.getOverlays().add(nodeMarker);
        amsMap.getController().animateTo(geoPoint);
        amsMap.getController().setZoom(18.0);
        amsMap.getController().setCenter(geoPoint);
        amsMap.invalidate();

        nodeMarker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker, MapView mapView) {
                if (!marker.isInfoWindowShown()) {

                    AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                    dialog.setTitle(marker.getTitle());
                    dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    dialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            clearLastDestination();
                            patientPoint = marker.getPosition();
                            GpsMyLocationProvider gpsMyLocationProvider = new GpsMyLocationProvider(context.getApplicationContext());
                            final MyLocationNewOverlay mLocationOverlay = new MyLocationNewOverlay(gpsMyLocationProvider, amsMap);
                            mLocationOverlay.enableMyLocation();
                            mLocationOverlay.runOnFirstFix(new Runnable() {
                                @Override
                                public void run() {
                                    final GeoPoint curr = mLocationOverlay.getMyLocation();
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            clearLastDestination();
                                            currRoadLatsLng.clear();
                                            currRoad = new DrawRouts(amsMap, context, handler, currRoadLatsLng);
                                            currRoad.drawPath(curr, patientPoint, Color.GREEN, 10, progressBar);
                                            currRoad.getCost(curr, patientPoint, null);
                                        }
                                    });

                                }
                            });
                            amsMap.postInvalidate();
                        }
                    });
                    dialog.show();
                    marker.showInfoWindow();

                }

                return true;
            }
        });
    }


}
