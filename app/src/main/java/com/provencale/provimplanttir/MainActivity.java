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
import android.os.SystemClock;
import android.provider.Settings;

import android.app.AlertDialog;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import android.util.Log; // Pour utiliser Log.d(“test”, “resultat test”);

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.Manifest.permission.ACCESS_FINE_LOCATION ;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {
    private Button buttonSwitchManageTrouActivity;
    private EditText mNomVoleeEditText;
    private HorizontalNumberPicker mNumeroRangeeNumberPicker;
    private HorizontalNumberPicker mNumeroTrouDansRangeeNumberPicker;
    private Switch mIncrementTrouSwitch;
    private Button mEnregistrerTrouButton;


    private final static int REQUEST_CODE_PERM_GPS_RW = 1000;
    private final static int REQUEST_CODE_PERM_GPS = 1002;

    private LocationManager mLocationManager;
    private MLocationListener mLocationListener;
    private static final int LOCATION_INTERVAL_IN_MS = 500;
    private static final float LOCATION_DISTANCE = 0f;
    public int gps_loop_for_accuracy;
    private static int MAX_GPS_LOOP = 10;
    public boolean bypass_gps_accuracy; // used to check or bypass a security on gps accuracy
    public final static float GPS_PRECISION_HORIZONTAL = 10.0F;// meters ; cannot save position unless it is within range
    public final static float GPS_PRECISION_HORIZ_BYPASS_VERTI = 6.0F;// meters ; below this horizontal precision we do not check vertical accuracy (some android have terrible vertical accuracy)
    public final static float GPS_PRECISION_VERTICAL = 10.0F;// meters ; if vertical accuracy available (some phones does not implement it) cannot save position unless it is within range.

    protected boolean gps_enabled;

    private boolean enable_mEnregistrerTrouButton_next_fix;

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

        /*Add in Oncreate() funtion after setContentView()*/

        mIncrementTrouSwitch = (Switch) findViewById(R.id.IncrTrouSwitch); // initiate Switch

        ////////////////////////////////////////////////////////////////////////////////////////////
        checkRequestPermStartLocationLis();
        ////////////////////////////////////////////////////////////////////////////////////////////


        // mEnregistrerTrouButton should be disabled if Nom de la Volee is empty
        mNomVoleeEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mEnregistrerTrouButton_checkAndActivate();
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

        gps_loop_for_accuracy = 0;
        bypass_gps_accuracy = false;
        enable_mEnregistrerTrouButton_next_fix = true;
    }

    private boolean mEnregistrerTrouButton_checkAndActivate(){
        //return if button is activated or deactivated
        CharSequence charInNomVolee = mNomVoleeEditText.getText();
        //Log.v("mEnregistrerTrouButton_checkAndActivate","charInNomVolee:"+charInNomVolee.toString()+String.valueOf(mLocationListener.mLastLocation.hasAccuracy() && (mLocationListener.mLastLocation.getAccuracy() < GPS_PRECISION_HORIZONTAL))+":"+String.valueOf((mLocationListener.mLastLocation.hasVerticalAccuracy() && (mLocationListener.mLastLocation.getVerticalAccuracyMeters() < GPS_PRECISION_VERTICAL))));
        // checks if the button need to be activated or notaa
        if((charInNomVolee.toString().trim().length() != 0) //charInNomVolee need to be  filled
            && ((mLocationListener != null)
                && (mLocationListener.mLastLocation.hasAccuracy() && (mLocationListener.mLastLocation.getAccuracy() < GPS_PRECISION_HORIZONTAL)
                && ((Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
                | !(mLocationListener.mLastLocation.hasVerticalAccuracy()) // Only checks GPS_PRECISION_VERTICAL if this precision is available: Some phones only return False to hasVerticalAccuracy
                | (mLocationListener.mLastLocation.getVerticalAccuracyMeters() < GPS_PRECISION_VERTICAL)
                | (mLocationListener.mLastLocation.hasAccuracy() && (mLocationListener.mLastLocation.getAccuracy() < GPS_PRECISION_HORIZ_BYPASS_VERTI) ))))){

            mEnregistrerTrouButton.setEnabled(true);
            return true;
        } else {
            mEnregistrerTrouButton.setEnabled(false);
            return false;
        }

    }
    private void switchToManageTrousActivity() {
        Log.d("MainActivity", "switchToManageTrousActivity");
        Intent switchActivityIntent = new Intent(this, ManageTrous.class);
        startActivity(switchActivityIntent);
    }

    private void checkRequestPermStartLocationLis() {
        if (mLocationListener == null) {
            mLocationListener = new MLocationListener(LocationManager.GPS_PROVIDER);
        }

        Boolean permFlag = checkPermissions();
        if (!permFlag) {
            Log.v("checkRequestPermStartLocationLis", "checkPermissions=False");
            requestPermission();
            // calls mEnregistrerTrouButton click after the permission is given
        }

        Boolean gpsFlag = checkGPSWorking();
        if (!gpsFlag) {
            Log.v("checkRequestPermStartLocationLis", "checkGPSWorking=False");
            alertboxGPSDisabled();
        }
        if (permFlag) {
            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            try {
                mLocationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, LOCATION_INTERVAL_IN_MS, LOCATION_DISTANCE,
                        mLocationListener);
            } catch (java.lang.SecurityException ex) {
                Log.i("checkRequestPermStartLocationLis", "fail to request location update, ignore", ex);
            } catch (IllegalArgumentException ex) {
                Log.d("checkRequestPermStartLocationLis", "gps provider does not exist " + ex.getMessage());
            }
        }
        // else do the same thing after permission granted
    }

    @Override
    public void onStart() {
        Log.d("MainActivity", "[#] " + this + " - onStart()");
        super.onStart();
    }

    @Override
    public void onResume() {
        Log.w("MainActivity", "[#] " + this + " - onResume()");
        super.onResume();

        this.volees.read(this); // lets reread the volees file (in case of a change)

        checkRequestPermStartLocationLis();
        // following is needed because android studio does not see the the permissin checking..
        if (checkPermissions() ){
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
        Log.d("MainActivity", "[#] " + this + " - onStop()");
        super.onStop();
    }

    @Override
    protected void onPause() {
        Log.d("MainActivity", "[#] " + this + " - onPause()");
        super.onPause();
        if (mLocationManager != null && mLocationListener != null){
            mLocationManager.removeUpdates(mLocationListener);
            mLocationListener = null;
        }
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

            Boolean flag = checkPermissions();
            if (!flag){
                Log.v("BUTTONS", "checkPermissions=False");
                requestPermission();
                // calls mEnregistrerTrouButton click after the permission is given
                return;
            }

            Boolean gpsFlag = checkGPSWorking();
            if (!gpsFlag){
                Log.v("BUTTONS", "checkGPSWorking=False");
                alertboxGPSDisabled();
                mEnregistrerTrouButton.setEnabled(true); // to be able to have the button withou change Volee input
                return;
            }


            Log.v("BUTTONS", "checkPermissions=True");

            // let s call mLocationListener.registerLastPositionAsTrou(); after a small delay (to ensure a mLocationListener.mLastLocation is correct)
            int DELAY_BEFORE_REGISTER_LOC = (int) (1.1* (float) LOCATION_INTERVAL_IN_MS); // in ms LOCATION_INTERVAL_IN_MS :500 ms -> just to ensure a position
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // This method will be executed once the timer is over
                    mLocationListener.registerLastPositionAsTrou();
                }
            }, DELAY_BEFORE_REGISTER_LOC);// Reactivate after DELAY_BEFORE_RETRYING_REGISTER_LOC ms


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
            return gps_enabled;
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
    protected void alertboxGPSDisabled() {
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
    protected void alertboxPrecision() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("il semble qu'il y ait un problème avec la précision. Le point n'a pas été enregistré. Souhaitez vous continuer en désactivant cette sécurité ?")
                .setCancelable(false)
                .setTitle("Precision du GPS est insuffisante.")
                .setPositiveButton("Désactiver sécurité",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // finish the current activity
                                // AlertBoxAdvance.this.finish();
                                bypass_gps_accuracy = true; // until restart of app

                                dialog.cancel();
                            }
                        })
                .setNegativeButton("Continuer sans rien changer",
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
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
                        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                        try {
                            mLocationManager.requestLocationUpdates(
                                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL_IN_MS, LOCATION_DISTANCE,
                                    mLocationListener);
                        } catch (java.lang.SecurityException ex) {
                            Log.i("onCreate", "fail to request location update, ignore", ex);
                        } catch (IllegalArgumentException ex) {
                            Log.d("onCreate", "gps provider does not exist " + ex.getMessage());
                        }

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
                        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                        try {
                            mLocationManager.requestLocationUpdates(
                                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL_IN_MS, LOCATION_DISTANCE,
                                    mLocationListener);
                        } catch (java.lang.SecurityException ex) {
                            Log.i("onCreate", "fail to request location update, ignore", ex);
                        } catch (IllegalArgumentException ex) {
                            Log.d("onCreate", "gps provider does not exist " + ex.getMessage());
                        }
                    }
                }
                break;
        }
    }

    /*----------Listener class to get coordinates ------------- */
    private class MLocationListener implements LocationListener {
        Location mLastLocation;

        public MLocationListener(String provider)
        {
            Log.d("LocationListener", "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }
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
            Log.v("LocationListener", "new location registered");
            mLastLocation.set(location);

            TextView mHAccuLabel = (TextView) findViewById(R.id.main_textview_accu_h_val);
            if (mLastLocation.hasAccuracy()) {
                mHAccuLabel.setText(String.format("%.1f m",mLastLocation.getAccuracy()));
            }
            else {
                mHAccuLabel.setText("NA");
            }

            TextView mVAccuLabel = (TextView) findViewById(R.id.main_textview_accu_v_val);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (mLastLocation.hasVerticalAccuracy()) {
                    mVAccuLabel.setText(String.format("%.1f m",mLastLocation.getVerticalAccuracyMeters()));
                }
                else {
                    mVAccuLabel.setText("NA");
                }
            }

            // if needed reenable mEnregistrerTrouButton
            if (enable_mEnregistrerTrouButton_next_fix) {
                boolean buttonActivated = mEnregistrerTrouButton_checkAndActivate();
                if (buttonActivated) {
                    enable_mEnregistrerTrouButton_next_fix = false;
                }
            }
            //                Log.v("onLocationChanged", "New GPS location: "
//                        + String.format("%9.6f", location.getLatitude()) + ", "
//                        + String.format("%9.6f", location.getLongitude())+ ", "
//                        + String.format("%9.6f", location.getAltitude()) );

        }

        public void registerLastPositionAsTrou() {
            // This call should be triggered by requestSingleUpdate on button press
            boolean is_accurate_enough = true;
            boolean is_error_skippeable = false; // is the error eligible to be skipped by bypass_gps_accuracy

            if (mLastLocation == null) {
                Toast.makeText(getApplicationContext(), "Position vide. Nouvel essai en cours." , Toast.LENGTH_LONG).show();
                is_accurate_enough = false;
                is_error_skippeable = false;
                Log.v("registerLastPositionAsTrou", "location==null");
            }
            else {
                long timestamp_lasLoc = mLastLocation.getElapsedRealtimeNanos(); // since last reboot /!\ on Android studio emulator (01/09/2023) it seems that this value is incorrect -> much higher. This is non blocking when used as below
                if (timestamp_lasLoc == 0L) {// Seems to happen with position on gps start (security with button can be bypassed when pausing the app for example)
                    Toast.makeText(getApplicationContext(), "Position bizarre. Nouvel essai en cours.", Toast.LENGTH_LONG).show();
                    is_accurate_enough = false;
                    is_error_skippeable = false;
                } else {
                    long current_timestamp = SystemClock.elapsedRealtimeNanos(); // since last reboot
                    long LOC_EXPIRATION_IN_NANOSEC = 3000000L * (long) LOCATION_INTERVAL_IN_MS; // 3 * LOCATION_INTERVAL_IN_MS in nano seconds
                    Log.v("registerLastPositionAsTrou", "GPS location: "
                            + mLastLocation.toString());

                    if (LOC_EXPIRATION_IN_NANOSEC < (current_timestamp - timestamp_lasLoc)) {
                        is_accurate_enough = false;
                        is_error_skippeable = false;
                        double delay = ((double) (current_timestamp - timestamp_lasLoc)) / 1000000000.0; // conversion nanoseconds in seconds
                        Toast.makeText(getApplicationContext(), "Temps trop long depuis dernière position: " + String.format("%.1f s", delay) + ". Nouvel essai en cours", Toast.LENGTH_LONG).show();
                        mEnregistrerTrouButton.setEnabled(true);
                    } else {

                        if (!mLastLocation.hasAccuracy()) {
                            is_accurate_enough = false;
                            is_error_skippeable = true;
                            Toast.makeText(getApplicationContext(), "Manque précision horizontale. Nouvel essai en cours", Toast.LENGTH_LONG).show();
                        } else {
                            if (GPS_PRECISION_HORIZONTAL < mLastLocation.getAccuracy()) {
                                is_accurate_enough = false;
                                is_error_skippeable = true;
                                Toast.makeText(getApplicationContext(), "Précision horizontale (" + String.format("%.1f", mLastLocation.getAccuracy()) + "m) insuffisante. Nouvel essai en cours", Toast.LENGTH_LONG).show();
                            } else {
                                if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                                    &&(mLastLocation.hasVerticalAccuracy()  // Some phone does not have vertical accuracy... So only check that if given
                                        && (GPS_PRECISION_VERTICAL < mLastLocation.getVerticalAccuracyMeters())
                                        && GPS_PRECISION_HORIZ_BYPASS_VERTI < mLastLocation.getAccuracy() )) {
                                        is_accurate_enough = false;
                                        is_error_skippeable = true;
                                        Toast.makeText(getApplicationContext(), "Précision verticale (" + String.format("%.1f", mLastLocation.getVerticalAccuracyMeters()) + "m) insuffisante. Nouvel essai en cours", Toast.LENGTH_LONG).show();

                                    }
                                else {
                                    is_accurate_enough = true;
                                    is_error_skippeable = true;
                                }
                            }
                        }
                    }
                }
            }

            if (is_accurate_enough | (bypass_gps_accuracy && is_error_skippeable) ) { //  ok if location is precise enough (or if we bypass this security)
                String nomVolee = mNomVoleeEditText.getText().toString().trim(); // trim remove spaces

                int numeroRangee = mNumeroRangeeNumberPicker.getValue();
                int numeroTrou = mNumeroTrouDansRangeeNumberPicker.getValue();
                Log.v("registerLastPositionAsTrou", "Avant : " + volees.toString());

                Date mDateTime = new java.util.Date(mLastLocation.getTime());
                //Log.v("registerLastPositionAsTrou", "mDateTime:"+mDateTime.toString());

                if (volees.contains(nomVolee, numeroRangee, numeroTrou)){
                    // volees aleady contains the same nomVolee, numeroRangee, numeroTrou

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage("Ce nom de volée avec numéro de rangée et trou existe déjà. Etes-vous sûr de vouloir l'écraser ?");
                    builder.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            boolean removeSucces = volees.removeTrou(nomVolee, numeroRangee, numeroTrou);
                            if (!removeSucces){
                                Log.e("registerLastPositionAsTrou", "OnClick : Suppression ancien trou impossible" );
                                Toast.makeText(getApplicationContext(), "Problème lors de la suppression ancien trou" , Toast.LENGTH_LONG).show();
                            }
                            else {
                                createTrouAndSave(nomVolee, numeroRangee, numeroTrou,
                                        mLastLocation.getLatitude(), mLastLocation.getLongitude(), mLastLocation.getAltitude(),
                                        mDateTime);
                            }
                        }
                    });
                    builder.setNegativeButton("Non", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // now reallow the button
                            mEnregistrerTrouButton.setEnabled(true);
                            gps_loop_for_accuracy = 0;
                            dialog.dismiss();
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();

                }
                else{
                    createTrouAndSave(nomVolee, numeroRangee, numeroTrou,
                            mLastLocation.getLatitude(), mLastLocation.getLongitude(), mLastLocation.getAltitude(),
                            mDateTime);
                }


            } else {
                Log.v("registerLastPositionAsTrou", "Position non fiable");
                // let s try again -> This can create an infinite loop draining power
                if (gps_loop_for_accuracy < MAX_GPS_LOOP) {

                    // let s try again in 1 sec
                    int DELAY_BEFORE_RETRYING_REGISTER_LOC = 5000; // in ms
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // This method will be executed once the timer is over
                            registerLastPositionAsTrou();
                        }
                    }, DELAY_BEFORE_RETRYING_REGISTER_LOC);// Reactivate after DELAY_BEFORE_RETRYING_REGISTER_LOC ms

                    gps_loop_for_accuracy += 1;
                } else {
                    mEnregistrerTrouButton.setEnabled(true);
                    alertboxPrecision();
                }
            }

        }
        public void createTrouAndSave(String nomVolee, int numeroRangee, int numeroTrou,
                                      double latitude, double longitude, double altitude, Date timeUtc){


            volees.addtrou(nomVolee, numeroRangee, numeroTrou,
                    latitude, longitude, altitude, timeUtc);
            Log.v("registerLastPositionAsTrou", "Apres : " + volees.toString());
            volees.write(getApplicationContext());

            if (mIncrementTrouSwitch.isChecked()) {
                mNumeroTrouDansRangeeNumberPicker.add_one();
            } else {
                mNumeroTrouDansRangeeNumberPicker.minus_one();
            }

            // now reallow the button
            mEnregistrerTrouButton.setEnabled(true);
            gps_loop_for_accuracy = 0;
        }
    };




}


