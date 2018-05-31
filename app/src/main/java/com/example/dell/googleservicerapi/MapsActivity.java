package com.example.dell.googleservicerapi;

import android.*;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener,TextToSpeech.OnInitListener {

    private GoogleMap mMap;
    Data data;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;
    private TextView locationTxt;
    private TextView accelerationTxt;
    private static long UPDATE_INT = 1000;
    private static long FAST_RATE = 500;
    private static int SET_ACCURACY = LocationRequest.PRIORITY_HIGH_ACCURACY;
    private long newTime;
    private float oldSpeed;
    private long oldTime;
    private boolean firstTime;
    private Location oldLocation = new Location("old");;
    private TextToSpeech tts;
    private static final int MY_PERMISSION_REQUEST_FINE_LOCATION=101;
    private static final int MY_PERMISSION_REQUEST_COARSE_LOCATION=102;
    private boolean permissionToGranted=false;
    private double lastLatitude;
    private double lastLongitude;
    private TextView txtHA;
    private TextView txtHB;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        locationTxt = (TextView) findViewById(R.id.locationTxt);
        txtHA = (TextView) findViewById(R.id.txtHA);
        txtHB = (TextView) findViewById(R.id.txtHB);
        tts = new TextToSpeech(this,this);
        firstTime=true;
        oldTime=SystemClock.elapsedRealtime();
        newTime=oldTime;
        oldSpeed=0.0f;
        data = new Data();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                requestPermissions(new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION},MY_PERMISSION_REQUEST_FINE_LOCATION);
            }else{
                permissionToGranted=true;
            }
            return;
        }
        mMap.animateCamera(CameraUpdateFactory.zoomTo(14));
        arrangeLocationUpdate();
        mGoogleApiClient.connect();
        mMap.setMyLocationEnabled(true);
    }

    private void arrangeLocationUpdate() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(UPDATE_INT);
        mLocationRequest.setFastestInterval(FAST_RATE);
        mLocationRequest.setPriority(SET_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more deta
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_FINE_LOCATION);
            } else {
                permissionToGranted = true;
            }
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);


    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        float delTime = 0f;
        float delSpeed = 0f;
        float delAcceleration = 0f;
        float tempDistance = 0f;
        float newSpeed=0f;
        LatLng latlang = new LatLng(location.getLatitude(), location.getLongitude());
        CameraPosition position = CameraPosition.builder(mMap.getCameraPosition())
                .bearing(location.getBearing())
                .target(latlang)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));
        locationTxt.setText( "Lat: " + String.format("%.3f", location.getLatitude()) + " | Long: " + String.format("%.3f",location.getLongitude()));
        if (location.hasSpeed()) {
            newSpeed = location.getSpeed();
            newSpeed = newSpeed * 3.6f;
        }
        newTime = SystemClock.elapsedRealtime();
        delSpeed = newSpeed - oldSpeed;
        delTime = (newTime - oldTime) * (1 / 1000.0f);
        delAcceleration = delSpeed / delTime;
        if (firstTime) {
            lastLatitude = location.getLatitude();
            lastLongitude = location.getLongitude();
            data.setDistance(0.0f);
            firstTime = false;
        }

        oldLocation.setLatitude(lastLatitude);
        oldLocation.setLongitude(lastLongitude);
        tempDistance = oldLocation.distanceTo(location);
        if ((location.getAccuracy() < tempDistance) && delSpeed>0) {
            data.setDistance(tempDistance);
            lastLongitude = location.getLongitude();
            lastLatitude = location.getLatitude();
        }
            if(delAcceleration >=10) {
                //call the text to speech to alert about harsh acceleration and update HA counter
                data.setCountHA();
                alert(2);
            }
            if(delAcceleration <=-10){
                //call the text to speech to alert about hard braking and update HB coun ter
                data.setCountHB();
                alert(3);
            }

        if (newSpeed >= 90) {
            //if speed is in excess of 90 km/hr, alert about excessive speed.
            alert(4);
        }
        txtHA.setText(String.valueOf(data.getCountHA()));

        if (data.getCountHA() > 0) {
            txtHA.setTextColor(getResources().getColor(R.color.red_text));
        } else {
            txtHA.setTextColor(getResources().getColor(R.color.green_text));
        }

        txtHB.setText(String.valueOf(data.getCountHB()));

        if (data.getCountHB() > 0) {
            txtHB.setTextColor(getResources().getColor(R.color.red_text));
        } else {
            txtHB.setTextColor(getResources().getColor(R.color.green_text));
        }

        oldTime=newTime;
        oldSpeed=newSpeed;
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
                alert(1);//Welcome to Driver mate voice message
            }
        } else {
            Log.e("TTS", "Initialization failed");
        }
    }


    private void alert(int event) {
        String msg = "";
        switch (event) {
            case 1: {
                msg = "Starting your, Driver Mate.";
                break;
            }
            case 2: {
                msg = "Harsh Acceleration Event.";
                break;
            }
            case 3: {
                msg = "Hard Braking Event.";
                break;
            }
            case 4: {
                msg = "Excessive Speed. Please Slow Down";
                break;
            }

        }
        tts.speak(msg, TextToSpeech.QUEUE_FLUSH, null);

    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(permissionToGranted) {
            if (!mGoogleApiClient.isConnected()) {
                arrangeLocationUpdate();
                mGoogleApiClient.connect();
            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if(permissionToGranted) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(permissionToGranted)
            mGoogleApiClient.disconnect();    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case MY_PERMISSION_REQUEST_FINE_LOCATION:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //action if the permission is granted
                    permissionToGranted=true;
                }else {
                    permissionToGranted = false;
                    Toast.makeText(getApplicationContext(),"Permissions required",Toast.LENGTH_SHORT).show();
                }
                break;
            case MY_PERMISSION_REQUEST_COARSE_LOCATION:
                break;
        }

    }

}
