package com.example.dell.googleservicerapi;

import android.*;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;

import java.util.Locale;

import static android.preference.PreferenceManager.getDefaultSharedPreferencesName;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener,TextToSpeech.OnInitListener {

    TextView txtSpeed;
    Chronometer chrTimer;
    TextView txtAccelerate;
    TextView txtHA;
    TextView txtHB;
    TextView txtDistance;
    TextView txtPerform;
    Button btnSettings;
    Button btnmap;
    private TextToSpeech tts;
    private FusedLocationProviderApi locationProvider = LocationServices.FusedLocationApi;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private double lastLatitude;
    private double lastLongitude;
    Location oldLocation=new Location("old");
    private static final int MY_PERMISSION_REQUEST_FINE_LOCATION=101;
    private static final int MY_PERMISSION_REQUEST_COARSE_LOCATION=102;
    private boolean permissionToGranted=false;
    private float oldTime;
    private float newTime;
    private float oldSpeed;
    private boolean firstTime;
    private static Data data;
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        tts = new TextToSpeech(this,this);
        txtSpeed=(TextView) findViewById(R.id.txtSpeed);
        chrTimer =(Chronometer) findViewById(R.id.chrTimer);
        txtAccelerate=(TextView) findViewById(R.id.txtAccelerate);
        txtHA =(TextView) findViewById(R.id.txtHA);
        txtHB=(TextView) findViewById(R.id.txtHB);
        txtDistance=(TextView) findViewById(R.id.txtDistance);
        txtPerform = (TextView) findViewById(R.id.txtperform);
        btnmap=(Button) findViewById(R.id.btnmap);
        firstTime=true;
        oldTime=SystemClock.elapsedRealtime();
        newTime=oldTime;
        oldSpeed=0.0f;
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(2000); //Location update requests to be raised every two seconds
        locationRequest.setFastestInterval(1000); //Fastest rates for recieving location update
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); //Priority is mazimizing accuracy over battery power

        data = new Data(); //Reference to the data object



        btnmap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,MapsActivity.class);
                intent.putExtra("sentData",convertDataToGSON(data));
                startActivity(intent);
            }
        });
        InializeTimer();//Setup the timer in preparation for starting using the start button
    }

    private String convertDataToGSON(Data data) {
        Gson myGson = new Gson();
        String dataString = myGson.toJson(data);
        return dataString;
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        requestLocationUpdates();//Once established, begin receiving location update
    }

    private void requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more deta
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION},MY_PERMISSION_REQUEST_FINE_LOCATION);
            }else{
                permissionToGranted=true;
            }
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
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

        if (location.hasSpeed()) {
            newSpeed = location.getSpeed();
            newSpeed = newSpeed * 3.6f;
            data.setMaxSpeed(newSpeed);
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

        if (delAcceleration >= 10 || delAcceleration<=-10) {
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
            txtAccelerate.setTextColor(getResources().getColor(R.color.red_text));
        } else {
            txtAccelerate.setTextColor(getResources().getColor(R.color.green_text));
        }

        txtPerform.setText(String.format("%.0f",data.getPerform()));

        if (data.getPerform() >= 100) txtPerform.setBackgroundColor(getResources().getColor(R.color.blue_text));
        if (data.getPerform() >= 95 && data.getPerform()<100) txtPerform.setBackgroundColor(getResources().getColor(R.color.green_text));
        if (data.getPerform() >= 85 && data.getPerform()<95) txtPerform.setBackgroundColor(getResources().getColor(R.color.amber_text));
        if (data.getPerform() <85) txtPerform.setBackgroundColor(getResources().getColor(R.color.red_text));


            txtSpeed.setText(String.format("%.0f", newSpeed));

        if (newSpeed >= 90) {
            //if speed is in excess of 90 km/hr, alert about excessive speed.
            alert(4);
            txtSpeed.setTextColor(getResources().getColor(R.color.red_text));
        } else {
            txtSpeed.setTextColor(getResources().getColor(R.color.green_text));
        }

        txtAccelerate.setText(String.format("%.1f", delAcceleration) + " km/h/s");
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

        if (data.getDistance() >= 1000){
            //Toast.makeText(this, String.valueOf(convDistance), Toast.LENGTH_SHORT).show();
            txtDistance.setText(String.format("%.1f", data.getDistance()/1000.0f) + " km");
        }
        if (data.getDistance() < 1000.0){
              txtDistance.setText(String.format("%.0f",data.getDistance()) + " m");
           }
        oldTime=newTime;
        oldSpeed=newSpeed;

        //Save the parameters in shared preferences
        saveParameters(data.getDistance(),data.getCountHA(),data.getCountHB(),data.getPerform(),data.getMaxSpeed());
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(permissionToGranted) {
            if (googleApiClient.isConnected()) {
                requestLocationUpdates();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(permissionToGranted) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(permissionToGranted)
        googleApiClient.disconnect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tts !=null){
            tts.stop();
            tts.shutdown();
        }
    }

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

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS){
            int result =tts.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                Log.e("TTS", "This Language is not supported");
            }else{
                alert(1);//Welcome to Driver mate voice message
            }
        }else{
            Log.e("TTS","Initialization failed");
        }
    }//Text to speech intializer
    private void alert(int event) {
        String msg="";
        switch (event){
            case 1: {
                msg="Starting your, Driver Mate.";
                break;
            }
            case 2:{
                msg = "Harsh Acceleration Event.";
                break;
            }
            case 3:{
                msg = "Hard Braking Event.";
                break;
            }
            case 4: {
                msg="Excessive Speed. Please Slow Down";
                break;
            }

        }
        tts.speak(msg,TextToSpeech.QUEUE_FLUSH,null);
    }//Main Text to Speech routine

    private void saveParameters(float distance, int countHA, int countHB, float perform, float maxSpeed) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putFloat("distance",distance);
        editor.putInt("countHA",countHA);
        editor.putInt("countHB",countHB);
        editor.putFloat("perform",perform);
        editor.putFloat("maxSpeed",maxSpeed);
        editor.commit();
    }

    private void InializeTimer() {

        chrTimer.setText("00:00:00");
        chrTimer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            boolean isPair = true;
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                long time;
                if (data.isRunning()){
                    time=SystemClock.elapsedRealtime() - chronometer.getBase();
                    data.setTime(time);
                }else{
                    time=data.getTime();
                }
                int h   = (int)(time /3600000);
                int m = (int)(time  - h*3600000)/60000;
                int s= (int)(time  - h*3600000 - m*60000)/1000 ;
                String hh = h < 10 ? "0"+h: h+"";
                String mm = m < 10 ? "0"+m: m+"";
                String ss = s < 10 ? "0"+s: s+"";
                chrTimer.setText(hh+":"+mm+":"+ss);

                if (data.isRunning()){
                    chrTimer.setText(hh+":"+mm+":"+ss);
                } else {
                    if (isPair) {
                        isPair = false;
                        chrTimer.setText(hh+":"+mm+":"+ss);
                    }else{
                        isPair = true;
                        chrTimer.setText("");
                    }
                }

            }
        });
        chrTimer.setBase(SystemClock.elapsedRealtime() - data.getTime());//remember to move this to start button
        chrTimer.start();//remember to move this to start button
    }


}
