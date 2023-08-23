package com.provencale.provimplanttir;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.annotation.NonNull;

import android.Manifest;

import android.annotation.SuppressLint;
import android.location.Criteria;
import android.os.Bundle;
import android.os.Handler;
import android.os.Build;

import android.content.pm.PackageManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.location.LocationManager;
import android.location.LocationListener;
import android.location.Location;
import android.location.LocationProvider;
import android.os.Looper;
import android.provider.Settings;

import android.app.AlertDialog;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import android.util.Log; // Pour utiliser Log.d(“test”, “resultat test”);

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.Manifest.permission.ACCESS_FINE_LOCATION ;

public class MainActivity extends AppCompatActivity {
    private Button buttonSwitchManageTrouActivity;
    private EditText mNomVoleeEditText;
    private HorizontalNumberPicker mNumeroRangeeNumberPicker;
    private HorizontalNumberPicker mNumeroTrouDansRangeeNumberPicker;
    private Button mEnregistrerTrouButton;


    private final static int REQUEST_CODE_PERM_GPS_RW = 1000;
    private final static int REQUEST_CODE_PERM_GPS = 1002;

    private LocationManager mLocationManager;
    //private LocationListener mLocationListener;
    final Looper looper = null;

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

    protected boolean gps_enabled;

    public Volees volees;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonSwitchManageTrouActivity = findViewById(R.id.main_button_suppr_trous);
        buttonSwitchManageTrouActivity.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              switchToManageTrousActivity();
          }
        });

        mNomVoleeEditText = findViewById(R.id.main_edittext_nom_volee);
        mNumeroRangeeNumberPicker = findViewById(R.id.main_edittext_numero_rangee);
        mNumeroTrouDansRangeeNumberPicker = findViewById(R.id.main_edittext_numero_trou_dans_rangee);

        mEnregistrerTrouButton = findViewById(R.id.main_button_enregistrer_trou);


        mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        // mEnregistrerTrouButton should be disabled if Nom de la Volee is empty
        mNomVoleeEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().trim().length()==0){
                    mEnregistrerTrouButton.setEnabled(false);
                } else {
                    mEnregistrerTrouButton.setEnabled(true);
                }
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub
            }
            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
            }
        });


        mEnregistrerTrouButton.setEnabled(false); // start with the button disabled
        mEnregistrerTrouButton.setOnClickListener(mEnrgPosiButtonListener);

        //////////
        this.volees = new Volees(this);
        Log.d("MainActivity","onCreate:volees"+this.volees.toString());
    }

    private void switchToManageTrousActivity() {
        Log.d("MainActivity", "switchToManageTrousActivity");
        Intent switchActivityIntent = new Intent(this, ManageTrous.class);
        startActivity(switchActivityIntent);
    }
    @Override
    public void onStart() {
        Log.d("myApp", "[#] " + this + " - onStart()");
        super.onStart();
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
    }

    @Override
    public void onStop() {
        Log.d("myApp", "[#] " + this + " - onStop()");
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


// https://github.com/BasicAirData/GPSLogger/tree/master/app/src/main/java/eu/basicairdata/graziano/gpslogger

// voir https://github.com/JaimePerezS/GPSLocation/blob/master/app/src/main/java/com/example/jaime/gpslocation/MainActivity.java
// GPS logger


    // Create an anonymous implementation of OnClickListener
    private View.OnClickListener mEnrgPosiButtonListener = new View.OnClickListener() {
        @SuppressLint("MissingPermission")
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
            if (!flag){
                Log.v("BUTTONS", "checkPermissions=False");
                requestPermission();
                return;
            }

            Boolean gpsFlag = checkGPSWorking();
            if (!gpsFlag){
                Log.v("BUTTONS", "checkGPSWorking=False");
                alertbox("Gps Status!!", "Your GPS is: OFF");
                return;
            }


            Log.v("BUTTONS", "checkPermissions=True");

            ///// Inspired from https://stackoverflow.com/questions/10524381/gps-android-get-positioning-only-once/38794291#38794291
            // Now first make a criteria with your requirements
            // this is done to save the battery life of the device
            // there are various other other criteria you can search for..
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            criteria.setPowerRequirement(Criteria.POWER_LOW);
            criteria.setAltitudeRequired(false);
            criteria.setBearingRequired(false);
            criteria.setSpeedRequired(false);
            criteria.setCostAllowed(true);
            criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
            criteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);

            mLocationManager.requestSingleUpdate(criteria, gpsLocationListener, looper);

        }
    };

    // Function to check GPS is enabled
    private Boolean checkPermissions() {
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M) { // Only when the app's target SDK is 23 or higher
            return true;
        }

        if (Build.VERSION_CODES.R <= android.os.Build.VERSION.SDK_INT) { // Only when the app's target SDK is 30 (11.0) or lower : no need for READ_EXTERNAL_STORAGE and WRITE_EXTERNAL_STORAGE
            int gpsPermission = ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION);
            return gpsPermission == PackageManager.PERMISSION_GRANTED;
        }
        else {

            int gpsPermission = ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION);
            int readStoragePermission = ActivityCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE);
            int writeStoragePermission = ActivityCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE);

            return gpsPermission == PackageManager.PERMISSION_GRANTED &&
                    readStoragePermission == PackageManager.PERMISSION_GRANTED &&
                    writeStoragePermission == PackageManager.PERMISSION_GRANTED;
        }
    }

    private Boolean checkGPSWorking() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
    private void requestPermission() {
        if (Build.VERSION_CODES.R <= android.os.Build.VERSION.SDK_INT) { // Only when the app's target SDK is 30 (11.0) or lower : no need for READ_EXTERNAL_STORAGE and WRITE_EXTERNAL_STORAGE
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{ACCESS_FINE_LOCATION }, this.REQUEST_CODE_PERM_GPS);
        }
        else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{ACCESS_FINE_LOCATION, READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE}, this.REQUEST_CODE_PERM_GPS_RW);
        }

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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.v("onRequestPermissionsResult", "called");
        switch (requestCode) {

            case REQUEST_CODE_PERM_GPS_RW:
                if (grantResults.length > 0) {
                    Log.v("onRequestPermissionsResult", "grantResults.length>0");
                    boolean gps = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean readStorage = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorage = grantResults[2] == PackageManager.PERMISSION_GRANTED;

                    if (!gps){
                        Toast.makeText(this, "Manque autorisation GPS", Toast.LENGTH_LONG).show();
                    }
                    if (!readStorage){
                        Toast.makeText(this, "Manque autorisation lecture", Toast.LENGTH_LONG).show();
                    }
                    if (!writeStorage){
                        Toast.makeText(this, "Manque autorisation ecriture", Toast.LENGTH_LONG).show();
                    }

                    if (gps && readStorage && writeStorage){
                        mEnregistrerTrouButton.callOnClick(); // onRequestPermissionsResult is called after a mEnregistrerTrouButton click
                    }

                }
                break;
            case REQUEST_CODE_PERM_GPS:
                if (grantResults.length > 0) {
                    Log.v("onRequestPermissionsResult", "grantResults.length>0");
                    boolean gps = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if (!gps){
                        Toast.makeText(this, "Manque autorisation GPS", Toast.LENGTH_LONG).show();
                    }
                    else{
                        mEnregistrerTrouButton.callOnClick(); // onRequestPermissionsResult is called after a mEnregistrerTrouButton click
                    }
                }
                break;
        }
    }

    /*----------Listener class to get coordinates ------------- */
    private final LocationListener gpsLocationListener = new LocationListener() {

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("LocationListener", "onStatusChanged:"+String.valueOf(status));
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
            // This call should be triggered by requestSingleUpdate on button press

            if (location != null) {
//                Log.v("onLocationChanged", "New GPS location: "
//                        + String.format("%9.6f", location.getLatitude()) + ", "
//                        + String.format("%9.6f", location.getLongitude())+ ", "
//                        + String.format("%9.6f", location.getAltitude()) );

                Log.v("onLocationChanged", "New GPS location: "
                        + location.toString() );

                String nomVolee = mNomVoleeEditText.getText().toString().trim(); // trim remove spaces

                int numeroRangee = mNumeroRangeeNumberPicker.getValue();
                int numeroTrou = mNumeroTrouDansRangeeNumberPicker.getValue();
                Log.v("onLocationChanged","Avant : "+volees.toString());
                volees.addtrou(nomVolee, numeroRangee, numeroTrou,
                        location.getLatitude(),location.getLatitude(),location.getAltitude());
                Log.v("onLocationChanged","Apres : "+volees.toString());
                volees.write(getApplicationContext());

                mNumeroTrouDansRangeeNumberPicker.add_one();

            } else{
                Log.v("onLocationChanged", "location==null");
            }
        }
    };



}

