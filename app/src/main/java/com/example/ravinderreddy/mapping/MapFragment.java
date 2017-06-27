package com.example.ravinderreddy.mapping;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.multidex.MultiDex;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class MapFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, OnMapReadyCallback {

    View mapView;
    MapView mMapView;
    public GoogleMap mMap;
    private static final String TAG = "GoogleMaps";
    Location bestLocation = null;
    String newAddress = "";
    double current_latitude = 17.4237;
    double current_longitude = 78.4584;
    double distance;
    String address = "";
    private Marker customMarker;
    LocationManager locationManager;
    LatLng currentLocation;
    private LatLng mCenterLatLong;
    private GoogleApiClient mGoogleApiClient;
    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    TextView txt_location_address;
    RelativeLayout layout_locationNames, mapdetails;
    String result_address = "";
    double place_latitude, place_longitude;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    boolean flag_distance = false;
    Location mLocation;
    TextView txt_select_vehilce_type;
    boolean flag_vehicle_type;
    Location mLastLocation;
    boolean flag_gps = false;
    Button button;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        MultiDex.install(getActivity());
        try {

            mapView = inflater.inflate(R.layout.fragment_map, container, false);
//
//            if (getArguments() != null) {
//                place_latitude = getArguments().getDouble("latitude");
//                place_longitude = getArguments().getDouble("longitude");
//            }
//

            initElements(mapView);
            initObjects();
            initListeners();
            getLastKnownLocation();
            checkPlayServices();
            turnGPSOn();

            mMapView = (MapView) mapView.findViewById(R.id.mapView);
            mMapView.onCreate(savedInstanceState);
            mMapView.onResume();

            try {
                MapsInitializer.initialize(getActivity().getApplicationContext());
            } catch (Exception e) {
                e.printStackTrace();
            }

            buildGoogleApiClient();
            mMapView.getMapAsync(this);


        } catch (Exception e) {
            e.printStackTrace();
        }
        return mapView;

    }


    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
    }

    private void initListeners() {

        layout_locationNames.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(getActivity(), GooglePlacesActivity.class));
                try {
                    Intent intent =
                            new PlaceAutocomplete
                                    .IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                    .build(getActivity());
                    startActivityForResult(intent, 1);
                } catch (GooglePlayServicesRepairableException e) {
                    // TODO: Handle the error.
                    Toast.makeText(getActivity(), "GooglePlayServices Repairable Exception", Toast.LENGTH_SHORT).show();
                } catch (GooglePlayServicesNotAvailableException e) {
                    // TODO: Handle the error.
                    Toast.makeText(getActivity(), "GooglePlayServices Not Available Exception", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }


    private void initObjects() {

    }

    private void initElements(View mapView) {

        txt_location_address = (TextView) mapView.findViewById(R.id.lm_location_name);
        layout_locationNames = (RelativeLayout) mapView.findViewById(R.id.lm_relative_locationName);
//        mapdetails= (RelativeLayout) mapView.findViewById(R.id.mapdetails);
//        button= (Button) mapView.findViewById(R.id.mapbtndetails);
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Animation slideUp = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_up);
//                Animation slideDown = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_down);
//                if(mapdetails.getVisibility()==View.INVISIBLE){
//                    button.startAnimation(slideUp);
//                    mapdetails.setVisibility(View.VISIBLE);
//                }else{
//                    mapdetails.setVisibility(View.INVISIBLE);
//                    button.startAnimation(slideDown);
//                }
//
//
//
//
//
//            }
//        });
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;


        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                Log.d("Camera postion change" + "", cameraPosition + "");

                mCenterLatLong = cameraPosition.target;

                mMap.clear();

                try {

                    mLocation = new Location("");
                    mLocation.setLatitude(mCenterLatLong.latitude);
                    mLocation.setLongitude(mCenterLatLong.longitude);

                    String address = "";
                    address = getCompleteAddressString(current_latitude, current_longitude);

                    current_latitude = mLocation.getLatitude();
                    current_longitude = mLocation.getLongitude();

                    layout_locationNames.setVisibility(View.VISIBLE);
                    txt_location_address.setText(address);

                    double hitech_latitude = 17.4498180;
                    double hitech_longitude = 78.3657330;
                    LatLng currentlocation = new LatLng(current_latitude, current_longitude);
                    LatLng hitechCity = new LatLng(hitech_latitude, hitech_longitude);
                    if (current_latitude != 0.0 && current_longitude != 0.0) {
                        PolylineOptions polylineOptions = new PolylineOptions().add(currentlocation).
                                add(hitechCity).width(5).color(Color.RED).geodesic(true);
                        mMap.addPolyline(polylineOptions);
                        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                        mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker))
                                .position(hitechCity)).setDraggable(true);

                        distance = distance(hitech_latitude, hitech_longitude, mLocation.getLatitude(), mLocation.getLongitude());
                        Log.v("distance ", "distance :" + distance);
                        if (distance <= 5.0) {
                            flag_vehicle_type = true;
                        } else {
                            flag_vehicle_type = false;
                            txt_select_vehilce_type.setText("Choose Another Location");
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);

        if (mapView != null && mapView.findViewById(Integer.parseInt("1")) != null) {
            // Get the button view
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            // and next place it, on bottom right (as Google Maps app)
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                    locationButton.getLayoutParams();
            // position on right bottom
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 30, 30);

        }

        if (mapView != null && mapView.findViewById(Integer.parseInt("1")) != null) {
            // Get the view
            View locationCompass = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("5"));
            // and next place it, on bottom right (as Google Maps app)
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                    locationCompass.getLayoutParams();
            // position on right bottom
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            layoutParams.setMargins(30, 30, 0, 0); // 160 la truc y , 30 la  truc x
        }

        changeMap(mLocation);


    }


    public Location getLastKnownLocation() {

        try {
            locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

            List<String> providers = locationManager.getProviders(true);

            for (String provider : providers) {
                if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return null;
                }
                Location l = locationManager.getLastKnownLocation(provider);
                if (l == null) {
                    continue;
                }
                if (bestLocation == null
                        || l.getAccuracy() < bestLocation.getAccuracy()) {
                    bestLocation = l;
                }
            }
            if (bestLocation == null) {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return bestLocation;
        }
    }


    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double dist = 0.0;
        try {
            double theta = lon1 - lon2;
            dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
            dist = Math.acos(dist);
            dist = rad2deg(dist);
            dist = dist * 60 * 1.1515;
            dist = dist * 1.609344;
            /*if (unit == "K") {
                dist = dist * 1.609344;
            } else if (unit == "N") {
                dist = dist * 0.8684;
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");
                for (int i = 0; i < returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
                //strReturnedAddress.substring(0,strAdd.length()-1);  //remove last character from string
                Log.w(TAG, "location address :" + strReturnedAddress.toString());
            } else {
                Log.w(TAG, "No Address returned");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w(TAG, "Cannont get Address");
        }
        return strAdd;
    }

    private Bitmap createDrawableFromView(Context context, View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((MainActivity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    @Override
    public void onResume() {
        super.onResume();

        // mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }


        if (checkGpsAvailable()) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            if (mLastLocation != null) {

                if (mLocation == null) {
                    changeMap(mLastLocation);
                    Log.d(TAG, "ON connected");
                } else {
                    changeMap(mLocation);
                }

                changeMap(mLastLocation);
                Log.d(TAG, "ON connected");

            } else
                try {
                    LocationServices.FusedLocationApi.removeLocationUpdates(
                            mGoogleApiClient, this);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            try {
                LocationRequest mLocationRequest = new LocationRequest();
                mLocationRequest.setInterval(10000);
                mLocationRequest.setFastestInterval(5000);
                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                LocationServices.FusedLocationApi.requestLocationUpdates(
                        mGoogleApiClient, mLocationRequest, this);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                    alertDialogBuilder.setMessage("Turn On Location Services to Allow water on wheels to Determine Your Location");
                    alertDialogBuilder.setCancelable(false);
                    alertDialogBuilder.setPositiveButton("Yes",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {

                                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    startActivity(intent);

                                    checkgps_sperate();

                                }
                            });

                    alertDialogBuilder.setNegativeButton("No",
                            new DialogInterface.OnClickListener() {
                                @Override

                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    getActivity().finish();
                                }
                            });

                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();


                }
            }, 1000);


        }


    }

    private void checkgps_sperate() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if (checkGpsAvailable()) {
                    changeMap(mLastLocation);
                }

                /*else{
                    checkgps_sperate();
                }*/

            }
        }, 3000);
    }

    private boolean checkGpsAvailable() {
        final LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            flag_gps = false;
        } else {
            flag_gps = true;
        }
        return flag_gps;
    }


    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        try {
            if (location != null)
                mLastLocation = location;
            changeMap(location);
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        try {
            mGoogleApiClient.connect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        try {

        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }


    private void changeMap(Location location) {
        Log.d(TAG, "Reaching map" + mMap);

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        // check if map is created successfully or not
        if (mMap != null) {
            mMap.getUiSettings().setZoomControlsEnabled(true);

            LatLng latLong;
            latLong = new LatLng(current_latitude, current_longitude);


            /*View marker = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker_layout, null);
            TextView numTxt = (TextView) marker.findViewById(R.id.num_txt);
            numTxt.setText("Select Vehicle Type");


            customMarker = mMap.addMarker(new MarkerOptions().position(latLong).icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(getActivity(), marker))));*/

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLong).zoom(14f).tilt(70).build();

            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


        } else {
            Toast.makeText(getActivity(),
                    "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                    .show();
        }

    }


    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, getActivity(),
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                //finish();
            }
            return false;
        }
        return true;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            // retrive the data by using getPlace() method.
            Place place = PlaceAutocomplete.getPlace(getActivity(), data);
            Log.e("Tag", "Place: " + place.getAddress() + place.getPhoneNumber() + ", " + place.getLatLng());


            /*LatLng latLng = place.getLatLng();
            double latitude = latLng.latitude;
            double longitude = latLng.longitude;

            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.putExtra("lat", latitude);
            intent.putExtra("long", longitude);
            startActivity(intent);*/

            LatLng latLong;
            latLong = place.getLatLng();
            place_latitude = latLong.latitude;
            place_longitude = latLong.longitude;

            CameraPosition cameraPosition = new CameraPosition.Builder().target(latLong).zoom(14f).tilt(70).build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


        } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
            Status status = PlaceAutocomplete.getStatus(getActivity(), data);
            // TODO: Handle the error.
            Log.e("Tag", status.getStatusMessage());

        } else if (resultCode == RESULT_CANCELED) {
            // The user canceled the operation.
        }
    }


    private void turnGPSOn() {
        String provider = Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        if (!provider.contains("gps")) { //if gps is disabled
            final Intent poke = new Intent();
            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse("3"));
            getActivity().sendBroadcast(poke);
        }
    }


}
