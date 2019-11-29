package com.dewidar.makanny.map;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.dewidar.makanny.Gui_Manager;
import com.dewidar.makanny.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.snackbar.Snackbar;

import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

public class DrawRouts {
    private MapView amsMap;
    private Context context;
    private Handler handler;
    private Road road;
    private Polyline polyline = null;
    private String totalDistance_duration = "";
    private String TripCost = "";
    private Snackbar snackbar = null;
    private List<LatLng> latsLng = new ArrayList();
    private static List<LatLng> currRoadLatsLng;
    private List<Integer> directions = new ArrayList<>();
    public static boolean drawPointsFinished = false;


    public DrawRouts(MapView amsMap, Context context, Handler handler, List<LatLng> currRoadLatsLng) {
        this.currRoadLatsLng = currRoadLatsLng;
        this.amsMap = amsMap;
        this.context = context;
        this.handler = handler;
    }

    public void drawPath(GeoPoint from, GeoPoint to, final int color, final int width, final ProgressBar progressBar) {
        final RoadManager roadManager = new OSRMRoadManager(context);
        final ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();

        waypoints.add(from);
        waypoints.add(to);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.VISIBLE);
                        }
                    });
                    do {
                        road = roadManager.getRoad(waypoints);
                        Log.i("location", "trying to get road");
                    }
                    while (road.mStatus != Road.STATUS_OK);

                    Drawable nodeIcon = context.getResources().getDrawable(R.drawable.marker_default);
                    try {
                        for (int i = 0; i < road.mNodes.size(); i++) {
                            RoadNode node = road.mNodes.get(i);
                            Marker nodeMarker = new Marker(amsMap);
                            nodeMarker.setPosition(node.mLocation);
                            nodeMarker.setIcon(nodeIcon);
                            nodeMarker.setSnippet(node.mInstructions);
                            nodeMarker.setSubDescription(Road.getLengthDurationText(context, node.mLength, node.mDuration));
                            nodeMarker.setTitle("Step " + i);
                            Drawable icon = ContextCompat.getDrawable(context, R.drawable.ic_arrow_upward_black_24dp);
                            switch (node.mManeuverType) {
                                case 1:
                                    icon = ContextCompat.getDrawable(context, R.drawable.ic_arrow_upward_black_24dp);
                                    break;
                                case 3:
                                    icon = ContextCompat.getDrawable(context, R.drawable.ic_arrow_back_black_24dp);
                                    break;
                                case 4:
                                    icon = ContextCompat.getDrawable(context, R.drawable.ic_arrow_back_black_24dp);
                                    break;
                                case 5:
                                    icon = ContextCompat.getDrawable(context, R.drawable.ic_chevron_left_black_24dp);
                                    break;
                                case 6:
                                    icon = ContextCompat.getDrawable(context, R.drawable.ic_arrow_forward_black_24dp);
                                    break;
                                case 7:
                                    icon = ContextCompat.getDrawable(context, R.drawable.ic_arrow_forward_black_24dp);
                                    break;
                                case 8:
                                    icon = ContextCompat.getDrawable(context, R.drawable.ic_chevron_right_black_24dp);
                                    break;
                                case 12:
                                    icon = ContextCompat.getDrawable(context, R.drawable.ic_arrow_downward_black_24dp);
                                    break;
                                default:
                                    break;

                            }
                            nodeMarker.setImage(icon);
                            amsMap.getOverlays().add(nodeMarker);
                            latsLng.add(new LatLng(node.mLocation.getLatitude(), node.mLocation.getLongitude()));
                            directions.add(node.mManeuverType);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                    Log.i("locationinfo=", Road.getLengthDurationText(context, road.mLength, road.mDuration));
                    polyline = RoadManager.buildRoadOverlay(road);
                    polyline.setColor(color);
                    polyline.setWidth(width);

                    for (int i = 0; i < polyline.getPoints().size(); i++) {
                        GeoPoint geoPoint = polyline.getPoints().get(i);
                        currRoadLatsLng.add(new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude()));
                    }
                    if (color == Color.GREEN) {
                        drawPointsFinished = true;
                    }
                    handler.post(new Runnable() {

                        @Override
                        public void run() {
                            progressBar.setVisibility(View.INVISIBLE);
                            amsMap.getOverlays().add(polyline);
                            amsMap.invalidate();
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();

    }

    public Polyline getPolyline() {
        return polyline;
    }

    public Road getRoad() {
        return road;
    }

    public List<LatLng> getLatsLng() {
        return latsLng;
    }

    public List<LatLng> getCurrRoadLatsLng() {
        return currRoadLatsLng;
    }

    public List<Integer> getDirections() {
        return directions;
    }

    public void getCost(final GeoPoint from, final GeoPoint patient, final GeoPoint hospital) {
        final RoadManager roadManager = new OSRMRoadManager(context);
        final ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
        waypoints.add(from);
        waypoints.add(patient);
        if (hospital != null) {
            waypoints.add(hospital);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    do {
                        road = roadManager.getRoad(waypoints);
                        Log.i("location", "Trying to get cost");
                    }
                    while (road.mStatus != Road.STATUS_OK);

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                View view = null;
                                List<Fragment> fragentList = Gui_Manager.getInstance().getFragmentManager().getFragments();
                                for (Fragment f : fragentList){
                                    if (f instanceof MapFragment){
                                        view = f.getView();
                                    }
                                }

                                totalDistance_duration = Road.getLengthDurationText(context, road.mLength, road.mDuration);
                                String[] tripData =totalDistance_duration.split(",");
                                if(view!= null) {
                                    snackbar = Snackbar
                                            .make(view, context.getString(R.string.trip_distance) + ": " + tripData[0] , Snackbar.LENGTH_INDEFINITE)
                                            .setActionTextColor(context.getResources().getColor(R.color.yellow))
                                    .setAction(context.getString(R.string.trip_duration)+": "+tripData[1], new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                        }
                                    });

                                    snackbar.show();
                                }
                                Log.i("tripInfo",totalDistance_duration);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    });
                } catch (Exception e) {
                    totalDistance_duration = "";
                    getCost(from, patient, hospital);
                    e.printStackTrace();


                }
            }
        }).start();

    }


/*
    private void calculateCost(final GeoPoint currentLocation, final GeoPoint patientLocation, final Road road) throws IOException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                GeocoderNominatim geocoderNominatim = new GeocoderNominatim(BuildConfig.APPLICATION_ID);
                try {
                    double cost;
                    List<Address> currentAddresses = geocoderNominatim.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 1);
                    List<Address> patientAddresses = geocoderNominatim.getFromLocation(patientLocation.getLatitude(), patientLocation.getLongitude(), 1);
                    String currentState = currentAddresses.get(0).getAdminArea();
                    String patientState = patientAddresses.get(0).getAdminArea();
                    if (currentState.equals(patientState)) {
                        cost =   50;
                    } else {
                        cost = (road.mLength * 5);
                    }
                    TripCost += cost + "LE";
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (snackbar != null) {
                                snackbar.setAction(context.getString(R.string.trip_cost) + ": " + TripCost, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                    }
                                });

                            }
                        }
                    });


                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();

    }
*/


/*
    private int checkWaitingTime(int totalTime) {
        int pounace = 0;
        if (totalTime != 0) {
            pounace = totalTime / 60;
            if (totalTime >= 60) {
                pounace = pounace / 60;
                pounace = pounace * 50;
            }
        }

        return pounace;
    }
*/

    public String getTotalDistance_duration() {
        return totalDistance_duration;
    }

    public String getTripCost() {
        return TripCost;
    }

    public Snackbar getSnackbar() {
        return snackbar;
    }
}
