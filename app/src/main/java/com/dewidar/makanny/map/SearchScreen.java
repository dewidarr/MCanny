package com.dewidar.makanny.map;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dewidar.makanny.Gui_Manager;
import com.dewidar.makanny.R;
import com.dewidar.makanny.WaitingFragment;

import org.osmdroid.bonuspack.location.NominatimPOIProvider;
import org.osmdroid.bonuspack.location.POI;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;

public class SearchScreen extends Fragment {
    private TextView hospitals, pharmacy, gas, atm, restaurant, hotel, groceries, parking, shopping, cinema,market,toilts,stadium;
    private GeoPoint currentLocation;
    private RecyclerView recyclerView;
    private SearchAdapter searchAdapter;
    private Handler handler = new Handler();
    private Road road;
    private SearchListener searchListener;
    private SearchView searchView;
    private LinearLayout linearLayout;

    private List<String> durationList;
    private List<POI> poisList;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(
                R.layout.search_on_map, container, false);
        this.hospitals = view.findViewById(R.id.hospitals_search);
        this.pharmacy = view.findViewById(R.id.phrmacy_search);
        this.gas = view.findViewById(R.id.gas_search);
        this.atm = view.findViewById(R.id.atm_search);
        this.restaurant = view.findViewById(R.id.restaurant_search);
        this.hotel = view.findViewById(R.id.hotel_search);
        this.groceries = view.findViewById(R.id.groceries_search);
        this.parking = view.findViewById(R.id.parking_search);
        this.shopping = view.findViewById(R.id.shopping_search);
        this.linearLayout = view.findViewById(R.id.search_categories);
        this.cinema = view.findViewById(R.id.search_cinema);
        this.market = view.findViewById(R.id.search_school);
        this.toilts = view.findViewById(R.id.search_cafe);
        this.stadium =view.findViewById(R.id.search_stadium);

        searchView = view.findViewById(R.id.searchView);
        searchView.setVisibility(View.INVISIBLE);
        this.recyclerView = view.findViewById(R.id.search_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        hospitals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLocation != null)
                    searchForPlace(currentLocation, "hospital",0.1);

            }
        });

        pharmacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLocation != null)
                    searchForPlace(currentLocation, "pharmacy",0.1);

            }
        });

        gas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLocation != null)
                    searchForPlace(currentLocation, "gas",0.1);

            }
        });

        atm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLocation != null)
                    searchForPlace(currentLocation, "atm",0.1);

            }
        });

        restaurant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentLocation != null)
                    searchForPlace(currentLocation, "restaurant",0.1);
            }
        });

        hotel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentLocation != null)
                    searchForPlace(currentLocation, "hotel",0.1);
            }
        });
        groceries.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentLocation != null)
                    searchForPlace(currentLocation, "supermarket",0.1);

            }
        });
        shopping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentLocation != null)
                    searchForPlace(currentLocation, "mall",0.1);
            }
        });

        parking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentLocation != null)
                    searchForPlace(currentLocation, "parking",0.1);
            }
        });

        cinema.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentLocation != null)
                    searchForPlace(currentLocation, "cinema",0.1);
            }
        });

        market.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentLocation != null)
                    searchForPlace(currentLocation, "school",0.1);
            }
        });

        toilts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentLocation != null)
                    searchForPlace(currentLocation, "cafe",0.1);
            }
        });

        stadium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentLocation != null)
                    searchForPlace(currentLocation, "stadium",0.1);
            }
        });

       /* searchView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(event.getRawX() >= (searchView.getRight() - searchView.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        Toast.makeText(getActivity(), "cliked", Toast.LENGTH_SHORT).show();
                        if (!searchView.getText().equals(""))
                        {
                            getSearchResults(searchView.getText().toString(),currentLocation);
                        }
                        return true;
                    }
                }
                return false;
            }
        });
*/

        searchFilter(searchView);
        return view;
    }

    public void searchForPlace(final GeoPoint regionStartPoint, final String place, final double distance) {
        durationList = new ArrayList<>();
        if (distance != 0.3) {
            Gui_Manager.getInstance().setCurrentFragment(new WaitingFragment());
        }
        final RoadManager roadManager = new OSRMRoadManager(getActivity());
        new Thread(new Runnable() {
            @Override
            public void run() {
                NominatimPOIProvider poiProvider = new NominatimPOIProvider("OSMBonusPackTutoUserAgent");
                poisList = poiProvider.getPOICloseTo(regionStartPoint, place, 20, distance);
                if (poisList != null) {
                    if (poisList.size() != 0) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                linearLayout.setVisibility(View.INVISIBLE);
                                searchView.setVisibility(View.VISIBLE);
                                recyclerView.setVisibility(View.VISIBLE);
                                searchAdapter = new SearchAdapter(poisList);
                                searchAdapter.setCurrLocation(currentLocation);
                                searchAdapter.setHandler(handler);
                                searchAdapter.setSearchListener(searchListener);
                                recyclerView.setAdapter(searchAdapter);
                                Gui_Manager.getInstance().getFragmentManager().popBackStack();
                            }
                        });
                    } else {
                        if (distance != 0.3) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    searchForPlace(regionStartPoint, place, 0.3);
                                }
                            });
                        } else {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getContext(), getActivity().getString(R.string.try_again_later), Toast.LENGTH_LONG).show();
                                    Gui_Manager.getInstance().getFragmentManager().popBackStack();
                                }
                            });
                        }
                    }

                }else {
                    Toast.makeText(getContext(), getActivity().getString(R.string.try_again_later), Toast.LENGTH_LONG).show();
                    Gui_Manager.getInstance().getFragmentManager().popBackStack();
                }

            }
        }).start();

    }

    private void getSearchResults(final String place, final GeoPoint point) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                NominatimPOIProvider poiProvider = new NominatimPOIProvider("OSMBonusPackTutoUserAgent");
                List<POI> poisList = poiProvider.getPOICloseTo(point, "name=" + place, 20, 4.0);
            }
        }).start();
    }

    public void setCurrentLocation(GeoPoint currentLocation) {
        this.currentLocation = currentLocation;
    }

    public void setSearchListener(SearchListener searchListener) {
        this.searchListener = searchListener;
    }


    private void searchFilter(SearchView searchView) {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                newText = newText.toLowerCase();
                ArrayList<POI> newList = new ArrayList<>();
                for (POI placeInfo : poisList) {
                    String desc = placeInfo.mDescription.toLowerCase();
                    if (desc.contains(newText)) {
                        newList.add(placeInfo);
                    }
                }
                searchAdapter.setFilter(newList);
                return true;
            }
        });
    }
}
