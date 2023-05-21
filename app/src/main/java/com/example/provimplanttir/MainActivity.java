package com.example.provimplanttir;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.annotation.NonNull;

import android.Manifest;

import android.os.Bundle;
import android.os.Handler;
import android.os.Build;

import android.content.pm.PackageManager;
import android.content.Context;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;

import android.location.LocationManager;
import android.location.LocationListener;
import android.location.Location;
import android.location.LocationProvider;
import android.provider.Settings;

import android.app.AlertDialog;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Toast;

import android.util.Log; // Pour utiliser Log.d(“test”, “resultat test”);

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private EditText mNomVoleeEditText;
    private NumberPicker mNumeroRangeeNumberPicker;
    private NumberPicker mNumeroTrouDansRangeeNumberPicker;
    private Button mEnregistrerTrouButton;


    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1;
    private static final String[] RUNTIME_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            //Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };

    private LocationManager mLocationManager;
    //private LocationListener mLocationListener;
    protected Context context;

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute
    public String FILENAME = "storage.json";
    protected boolean gps_enabled;

    public Volees volees;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNomVoleeEditText = findViewById(R.id.main_edittext_nom_volee);
        mNumeroRangeeNumberPicker = findViewById(R.id.main_edittext_numero_rangee);
        mNumeroTrouDansRangeeNumberPicker = findViewById(R.id.main_edittext_numero_trou_dans_rangee);

        mEnregistrerTrouButton = findViewById(R.id.main_button_enregistrer_trou);

        //mEnregistrerTrouButton.setEnabled(false);
        mEnregistrerTrouButton.setOnClickListener(mEnrgPosiButtonListener);

        mLocationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);


        //////////
        this.volees = new Volees(this, FILENAME);
    }

    @Override
    public void onStart() {
        Log.d("myApp", "[#] " + this + " - onStart()");
        super.onStart();

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void onResume() {
        Log.w("myApp", "[#] " + this + " - onResume()");
        super.onResume();

        // replace by checkPermissions()
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                3000, 0, gpsLocationListener);

        // Workaround for Nokia Devices, Android 9
        // https://github.com/BasicAirData/GPSLogger/issues/77
        //if (EventBus.getDefault().isRegistered(this)) {
        //    //Log.w("myApp", "[#] GPSActivity.java - EventBus: GPSActivity already registered");
        //    EventBus.getDefault().unregister(this);
        //}

        //EventBus.getDefault().register(this);

        //EventBus.getDefault().post(EventBusMSG.APP_RESUME);

        // Check for Location runtime Permissions (for Android 23+)
        //if (!gpsApp.isLocationPermissionChecked()) {
        //    checkLocationPermission();
        //    gpsApp.setLocationPermissionChecked(true);
        //}
    }

    @Override
    public void onStop() {
        Log.d("myApp", "[#] " + this + " - onStop()");
        super.onStop();
        EventBus.getDefault().unregister(this);


    }

    @Override
    protected void onPause() {
        super.onPause();
        mLocationManager.removeUpdates(gpsLocationListener);
    }


// https://github.com/BasicAirData/GPSLogger/tree/master/app/src/main/java/eu/basicairdata/graziano/gpslogger
//https://stackoverflow.com/questions/40191554/android-greenrobot-eventbus-in-android

// voir https://github.com/JaimePerezS/GPSLocation/blob/master/app/src/main/java/com/example/jaime/gpslocation/MainActivity.java
// GPS logger

    public static class MessageEvent { /* Additional fields if needed */
    }

    @Subscribe
    public void onEvent(MessageEvent event) {
        //switch (msg.eventBusMSG) {
        //    case EventBusMSG.TRACKLIST_SELECT:
        //    case EventBusMSG.TRACKLIST_DESELECT:
        //        activateActionModeIfNeeded();
        //}
    }


    // Create an anonymous implementation of OnClickListener
    private View.OnClickListener mEnrgPosiButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
            v.setEnabled(false);
            Log.d("BUTTONS", "User tapped Enregistrer trou");

            int DELAY_DISABLE_BUTTON_MS = 3000; // in ms
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // This method will be executed once the timer is over
                    v.setEnabled(true); // reanable the button
                    Log.d("BUTTONS", "Reactivation boutton \"BUTTONS\"");
                }
            }, DELAY_DISABLE_BUTTON_MS);// Reactivate after 3000 ms

            Boolean flag = checkPermissions();
            if (flag) {

                Log.v("BUTTONS", "checkPermissions=True");

                //editLocation.setText("Please!! move your device to"+
                //        " see the changes in coordinates."+"\nWait..");

                //pb.setVisibility(View.VISIBLE);
                //locationListener = new MyLocationListener();

                //locationMangaer.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                //       5000, 10,locationListener);

                //String longitude = "Longitude: " +loc.getLongitude();
                //Log.v("Position", longitude);
                //String latitude = "Latitude: " +loc.getLatitude();
                //Log.v("Position", latitude);





                Location location = mLocationManager.getLastKnownLocation("gpsLocationListener");
                if (location != null) {
                    Log.v("BUTTONS", "New GPS location: "
                            + String.format("%9.6f", location.getLatitude()) + ", "
                            + String.format("%9.6f", location.getLongitude())+ ", "
                            + String.format("%9.6f", location.getAltitude()) );

                    Log.v("BUTTONS", "New GPS location: "
                            + location.toString() );

                } else{
                    Log.v("BUTTONS", "location==null");
                }
            } else {
                Log.v("BUTTONS", "checkPermissions=False");
                alertbox("Gps Status!!", "Your GPS is: OFF");
            }

        }
    };

    // Function to check GPS is enabled
    private Boolean checkPermissions() {

        if (hasPermissions(this, RUNTIME_PERMISSIONS)) {
            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            try {
                gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
                return true;
            } catch (Exception ex) {
            }


        } else {
            ActivityCompat.requestPermissions(this,
                    RUNTIME_PERMISSIONS, REQUEST_CODE_ASK_PERMISSIONS);
        }

        return false;


    }

    /*----------Method to create an AlertBox ------------- */
    protected void alertbox(String title, String mymessage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Le GPS du téléphone est désactivé")
                .setCancelable(false)
                .setTitle("** Status Gps **")
                .setPositiveButton("Activer GPS",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // finish the current activity
                                // AlertBoxAdvance.this.finish();
                                Intent myIntent = new Intent(
                                        Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(myIntent);
                                dialog.cancel();
                            }
                        })
                .setNegativeButton("Annuler",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // cancel the dialog box
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Only when the app's target SDK is 23 or higher, it requests each dangerous permissions it
     * needs when the app is running.
     */
    private static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS: {
                for (int index = 0; index < permissions.length; index++) {
                    if (grantResults[index] != PackageManager.PERMISSION_GRANTED &&
                            !(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                                    permissions[index].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                    ) {

                        /*
                         * If the user turned down the permission request in the past and chose the
                         * Don't ask again option in the permission request system dialog.
                         */
                        if (!ActivityCompat
                                .shouldShowRequestPermissionRationale(this, permissions[index])) {
                            Toast.makeText(this, "Required permission " + permissions[index]
                                            + " not granted. "
                                            + "Please go to settings and turn on for sample app",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, "Required permission " + permissions[index]
                                    + " not granted", Toast.LENGTH_LONG).show();
                        }
                    }
                }

                //initializeMapsAndPositioning();
                break;
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /*----------Listener class to get coordinates ------------- */
    private final LocationListener gpsLocationListener = new LocationListener() {

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

            switch (status) {
                case LocationProvider.AVAILABLE:
                    Log.v("LocationListener", "GPS available again");
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    Log.v("LocationListener", "GPS out of service");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.v("LocationListener", "GPS temporarily unavailable");
                    break;
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.v("LocationListener", "GPS Provider Enabled");
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.v("LocationListener", "GPS Provider Disabled");
        }

        @Override
        public void onLocationChanged(Location location) {
            //locationManager.removeUpdates(gpsLocationListener);
            Log.v("LocationListener", "New GPS location: "
                    + String.format("%9.6f", location.getLatitude()) + ", "
                    + String.format("%9.6f", location.getLongitude()));
        }
    };



}


