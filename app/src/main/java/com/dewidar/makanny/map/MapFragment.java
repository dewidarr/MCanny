package com.dewidar.makanny.map;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dewidar.makanny.Gui_Manager;
import com.dewidar.makanny.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.osmdroid.views.MapView;

public class MapFragment extends Fragment {
    private MapView amsMap;
    private ProgressBar progressBar;
    private ImageView userCurrentLocation;
    private ImageView searchOnMap;
    private Handler handler = new Handler();
    private OSMdroid osMdroid;
    private Context context = Gui_Manager.getInstance().getContext();
    private AdView adView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.osm_map, container, false);
        this.amsMap = view.findViewById(R.id.map);

        this.progressBar = view.findViewById(R.id.finding_rout);
        this.userCurrentLocation = view.findViewById(R.id.get_current_location);
        this.searchOnMap = view.findViewById(R.id.search_on_osm);
        this.adView = view.findViewById(R.id.adView);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        osMdroid = new OSMdroid(amsMap, handler, progressBar, searchOnMap, userCurrentLocation, view);
        osMdroid.setup();
    }

    @Override
    public void onPause() {
        Log.d("Dewidar", "onPause From Maps");
        super.onPause();
        try {
            amsMap.onPause(); //needed for compass, my location overlays, v6.0.0 and up
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onResume() {
        Log.d("Dewidar", "onResume From Maps");
        super.onResume();
        try {
            amsMap.onResume(); //needed for compass, my location overlays, v6.0.0 and up
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
