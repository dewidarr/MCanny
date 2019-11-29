package com.dewidar.makanny.map;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;


import com.dewidar.makanny.Gui_Manager;
import com.dewidar.makanny.R;

import org.osmdroid.bonuspack.location.POI;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.MyViewHolder> {

    private List<POI> placesList;
    private Handler handler;
    private GeoPoint currLocation;
    private Road road;
    private SearchListener searchListener;
    final RoadManager roadManager = new OSRMRoadManager(Gui_Manager.getInstance().getContext());


    public SearchAdapter(List<POI> placesList) {
        this.placesList = placesList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final POI places = placesList.get(position);
        String title = places.mDescription.substring(0,places.mDescription.indexOf(","));
        String desc = places.mDescription.substring(places.mDescription.indexOf(","),places.mDescription.length());
        holder.title.setText(title);
        holder.places.setText( desc.replace(","," "));

        holder.constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (searchListener != null)
                searchListener.callback(places.mLocation,places.mDescription);
                Gui_Manager.getInstance().getFragmentManager().popBackStack();
            }
        });

        getDuration(places,holder);
    }

    @Override
    public int getItemCount() {
        return placesList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView places, placeDistance,placeTime,title;
        public ConstraintLayout constraintLayout;

        public MyViewHolder(View view) {
            super(view);
            places = view.findViewById(R.id.place_info);
            placeDistance = view.findViewById(R.id.place_duration);
            placeTime = view.findViewById(R.id.place_time);
            title = view.findViewById(R.id.search_item_title);
            constraintLayout = view.findViewById(R.id.search_item_clicked);
        }
    }

    public void setCurrLocation(GeoPoint currLocation) {
        this.currLocation = currLocation;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }


    private void getDuration(POI places, final MyViewHolder holder){
        final ArrayList<GeoPoint> list =new ArrayList<>();
        list.add(currLocation);
        list.add(places.mLocation);
        new Thread(new Runnable() {
            @Override
            public void run() {
                do {
                    road = roadManager.getRoad(list);
                }
                while (road.mStatus != Road.STATUS_OK);
                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        String time = Road.getLengthDurationText(Gui_Manager.getInstance().getContext(),road.mLength,road.mDuration);
                        List<String> elephantList = Arrays.asList(time.split(","));
                        holder.placeDistance.setText(elephantList.get(0));
                        holder.placeTime.setText(elephantList.get(1));
                      }
                });
            }
        }).start();
    }

    public void setSearchListener(SearchListener searchListener) {
        this.searchListener = searchListener;
    }

    public void setFilter(List<POI> newList){
        placesList=new ArrayList<>();
        placesList.addAll(newList);
        notifyDataSetChanged();
    }
}




