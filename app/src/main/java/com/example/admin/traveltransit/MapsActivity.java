package com.example.admin.traveltransit;

//import android.location.LocationListener;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
//import com.google.android.libraries.places.api.Places;
//import com.google.android.libraries.places.api.net.PlacesClient;

import android.location.LocationListener;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener{
        //TaskLoadedCallback


    private GoogleMap mMap;
    private GoogleApiClient client;
    private LocationRequest locationRequest;
    private Location lastLocation;
    private Marker currentLocationMarker;
    private FusedLocationProviderClient fusedLocationClient;
    public static final int REQUEST_LOCATION_CODE = 99;
    double latitude, longitude;
    double end_latitude, end_longitude;
   // private Button getDirection;
    //private Polyline currentPolyline;
    //MarkerOptions place1,place2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            checkLocationPermisiion();
        }

        //getDirection = findViewById(R.id.btnGetDirection);

        /*getDirection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //new FetchURL(MapsActivity.this).execute(getUrl(place1.getPosition(), place2.getPosition(), "driving"), "driving");
            }
        });*/

        //FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode)
        {
            case REQUEST_LOCATION_CODE :
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    //permission granted
                    if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    {
                        if (client == null)
                        {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                }
                else //permission denied
                {
                    Toast.makeText(this,"Pemission denied!",Toast.LENGTH_LONG).show();
                }
                return;

        }
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            mMap.setMyLocationEnabled(true);
            buildGoogleApiClient();

        }

    }


    public void onClick(View v){
        Object dataTransfer[] = new Object[2];

        switch (v.getId()) {
            case R.id.B_search:
                EditText tf_location = (EditText) findViewById(R.id.TF_location);
                String location = tf_location.getText().toString();
                List<Address> addressList = null;

                MarkerOptions mo = new MarkerOptions();

                if (!location.equals("")) {
                    Geocoder geocoder = new Geocoder(this);
                    try {
                        addressList = geocoder.getFromLocationName(location, 5);
                        if (addressList !=null)
                        {
                            for (int i = 0; i < addressList.size(); i++) {
                                Address myAddress = addressList.get(i);

                                LatLng latlng = new LatLng(myAddress.getLatitude(), myAddress.getLongitude());
                                mo.position(latlng);
                                //place1.position(latlng);
                                mMap.animateCamera(CameraUpdateFactory.newLatLng(latlng));
                                mMap.addMarker(mo);
                                mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                   for (int i = 0; i < addressList.size(); i++) {
                        Address myAddress = addressList.get(i);

                        LatLng latlng = new LatLng(myAddress.getLatitude(), myAddress.getLongitude());
                        mo.position(latlng);
                        //place1.position(latlng);
                        mMap.animateCamera(CameraUpdateFactory.newLatLng(latlng));
                    }
                    //Testing to remove marker of previous search.
                    mo.title("Search Results!");
                    mMap.addMarker(mo);
                    // mMap.animateCamera(CameraUpdateFactory.newLatLng(latlng));



                }

                break;



        }

    }


        // Add a marker in Sydney and move the camera
        // LatLng mumbai = new LatLng(19.07, 72.87);
        //mMap.addMarker(new MarkerOptions().position(mumbai).title("Marker in Mumbai"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(mumbai));


    public synchronized void buildGoogleApiClient()
    {
        client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        client.connect();
    }


    @Override
    public void onLocationChanged(Location location){
          lastLocation = location;

          if (currentLocationMarker != null)
          {
              currentLocationMarker.remove();
          }

          //Getting latitude and longitude of new location
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        //place2.position(latLng);
        markerOptions.title("Current Location bro!");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));

        currentLocationMarker = mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomBy(10));

        /*if(client != null){
              fusedLocationClient.removeLocationUpdates(client, this);
            //LocationServices.FusedLocationProviderApi.removeLocationUpdates(client , this);

        }*/

    }


   /* private String getUrl(LatLng origin, LatLng dest, String directionMode) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + directionMode;
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.google_maps_key);
        return url;
    }*/


    @Override
    public void onConnected(Bundle bundle){

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

   /** @Override
    public void onConnected(@Nullable Bundle bundle){
        locationRequest = new LocationRequest();

        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(client, locationRequest, this);
        }
    } */

 /*   @Override
    protected void onResume() {
        super.onResume();
        boolean requestingLocationUpdates;
        if (requestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        LocationCallback locationCallback;
        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                null *//* Looper *//*);
    }
*/


    public boolean checkLocationPermisiion(){
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION))
            {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_LOCATION_CODE);
            }
            else
            {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_LOCATION_CODE);
            }
            return false;

        }
        else
        return true;
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    /*@Override
   public void onTaskDone(Object... values) {
        if (currentPolyline != null)
            currentPolyline.remove();
        currentPolyline = mMap.addPolyline((PolylineOptions) values[0]);
    }*/
}
