package com.rikin.jain.uberclone;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

import static android.content.Context.LOCATION_SERVICE;

public class PassengerActivity extends AppCompatActivity implements OnMapReadyCallback  {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private boolean check = false;
    private Button btnRequestCab;
    private boolean isUberCancelled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        findViewById(R.id.btnLogoutFromPassengerActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseUser.getCurrentUser().logOutInBackground(new LogOutCallback() {
                    @Override
                    public void done(ParseException e) {
                        if(e==null){
                            Intent intent = new Intent(PassengerActivity.this,MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
            }
        });
        btnRequestCab = findViewById(R.id.btnRequestCab);
        ParseQuery<ParseObject> carRequestQuery = ParseQuery.getQuery("RequestCar");
        carRequestQuery.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());
        carRequestQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(objects.size()>0 && e == null){
                    isUberCancelled = false;
                    btnRequestCab.setText("cancel your uber order");
                }
            }
        });
        btnRequestCab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isUberCancelled){
                if(ContextCompat.checkSelfPermission(PassengerActivity.this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
                    Location currentPassengerLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if(currentPassengerLocation !=null){
                        ParseObject requestCar = new ParseObject("RequestCar");
                        requestCar.put("username", ParseUser.getCurrentUser().getUsername());
                        ParseGeoPoint parseGeoPoint = new ParseGeoPoint(currentPassengerLocation.getLatitude(),currentPassengerLocation.getLongitude());
                        requestCar.put("passengerLocation",parseGeoPoint);
                        requestCar.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if(e==null){

                                        Toast.makeText(PassengerActivity.this,"A car request is sent",Toast.LENGTH_SHORT).show();
                                    btnRequestCab.setText("Cancel your uber order");
                                    isUberCancelled =false;



                                }
                            }
                        });
                    }
                }else {
                      Toast.makeText(PassengerActivity.this,"Unknown error ", Toast.LENGTH_SHORT).show();
                    }
            }else{
                    ParseQuery<ParseObject> carRequest = ParseQuery.getQuery("RequestCar");
                    carRequest.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());
                    carRequest.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> requestList, ParseException e) {
                            if(requestList.size()>0 && e==null){
                                isUberCancelled = true;
                                btnRequestCab.setText("REQUEST a cab");
                                for (ParseObject object: requestList){
                                    object.deleteInBackground(new DeleteCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            if (e == null) {

                                                Toast.makeText(PassengerActivity.this,"Uber ride is now cancelled",Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    });
                }
            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
//Location listener is an interface. So, we can't create its object.
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Toast.makeText(PassengerActivity.this, "chzanged", Toast.LENGTH_SHORT).show();

                updateCameraPassengerLocation(location);

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
//Asking for permissions for API >= 23
        if (Build.VERSION.SDK_INT < 23) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        } else if (Build.VERSION.SDK_INT >= 23) {
            // If permission not granted then ask for permissions(he can either give or deny the permission), else get the location.
            if (ContextCompat.checkSelfPermission(PassengerActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(PassengerActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location currentPassengerLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                updateCameraPassengerLocation(currentPassengerLocation);

            }
        }
    }

    // after users accept or deny the permission, we implement this method.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(PassengerActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location currentPassengerLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                updateCameraPassengerLocation(currentPassengerLocation);
            }
        }

    }

    //Method to get user's location
    private void updateCameraPassengerLocation(Location pLocation) {

        try {
            LatLng passengerLocation = new LatLng(pLocation.getLatitude(), pLocation.getLongitude());
            mMap.clear(); //To remove other markers of location
            if (!check) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(passengerLocation,10)); //To move the map camera to user location
            }
            mMap.addMarker(new MarkerOptions().position(passengerLocation).title("You are here!!")); //to add marker at the user's position
            check = true;
            Toast.makeText(this, "tt", Toast.LENGTH_SHORT).show();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
}

