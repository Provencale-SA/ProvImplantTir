package com.provencale.provimplanttir;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

//https://stackoverflow.com/questions/23586910/selecting-item-from-listview-and-delete-it-onclick-android-to-do-list-applicat
public class ManageTrous extends AppCompatActivity {

    private Button buttonBackToMainActivity;
    ListView listTrous;

    Volees volees;
    private final static int REQUEST_CODE_PERM_RW = 1001;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("ManageTrous","onCreate:start");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_trous);

        buttonBackToMainActivity = findViewById(R.id.manage_button_retour_implant);
        buttonBackToMainActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();// return to Main Activity
            }
        });

        Boolean flag = checkPermissions();
        if (!flag){
            Log.v("ManageTrous:onCreate", "checkPermissions=False");
            requestPermission();
            return;
        }

        this.volees = new Volees(this);
        Log.d("ManageTrous","onCreate:volees"+this.volees.toString());

        // Create the adapter to convert the array to views
        TrousAdapter adapter = new TrousAdapter(this, this.volees);

        // Attach the adapter to a ListView
        listTrous = (ListView)findViewById(R.id.listTrous);
        listTrous.setAdapter(adapter);
        Log.d("ManageTrous","onCreate:end:volees"+this.volees.toString());
    }

    public class TrousAdapter extends ArrayAdapter<Trou> {
        public TrousAdapter(Context context, Volees volees) {
            super(context, 0, volees.toArrayList());
        }

        private void deleteItem(int position) {
            if (position >=0) {
                Trou trou = getItem(position);
                boolean result = volees.removeTrou(trou);// remove from volees (ie the common data with MainActivity)
                if (result)  {
                    Toast.makeText(getApplicationContext(),
                            "Suppresion de " + String.valueOf(trou.nomVolee) + ";" + String.valueOf(trou.numeroRangee) + ";" + String.valueOf(trou.numeroTrou),
                            Toast.LENGTH_SHORT).show();

                    volees.write(getApplicationContext()); // write to file
                    this.remove(trou); // Now lets remove it on the current screen. The data is a duplicate of volees
                }
                else {
                    Toast.makeText(getApplicationContext(),
                            "Echec suppression",
                            Toast.LENGTH_SHORT).show();
                }

                this.notifyDataSetChanged(); // force a view update (needed after this.remove(trou))
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            Trou trou = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.trou, parent, false);
            }

            // Lookup view for data population
            TextView tvNomVolee = (TextView) convertView.findViewById(R.id.tvNomVolee);
            TextView tvNumeroRangee = (TextView) convertView.findViewById(R.id.tvNumeroRangee);
            TextView tvNumeroTrou = (TextView) convertView.findViewById(R.id.tvNumeroTrou);
            ImageButton btDelete = (ImageButton)convertView.findViewById(R.id.btDelete);

            // Populate the data into the template view using the data object
            tvNomVolee.setText(trou.nomVolee);
            tvNumeroRangee.setText(String.valueOf(trou.numeroRangee));
            tvNumeroTrou.setText(String.valueOf(trou.numeroTrou));

            btDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Let s add a confirmation dialog (else call directly deleteItem(position);)
                    AlertDialog.Builder builder = new AlertDialog.Builder(ManageTrous.this);
                    builder.setMessage("Etes-vous sûr ?");
                    builder.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            deleteItem(position);    // delete
                        }
                    });
                    builder.setNegativeButton("Non", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            });
            // Return the completed view to render on screen
            return convertView;
        }

    }
    private Boolean checkPermissions() {
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M) { // Only when the app's target SDK is 23 or lower :  no permission
            return true;
        }
        if (Build.VERSION_CODES.R <= android.os.Build.VERSION.SDK_INT) { // Only when the app's target SDK is 30 (11.0) or lower : no need for READ_EXTERNAL_STORAGE and WRITE_EXTERNAL_STORAGE
            return true;
        }
        int readStoragePermission = ActivityCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE);
        int writeStoragePermission = ActivityCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE);

        return readStoragePermission == PackageManager.PERMISSION_GRANTED &&
                writeStoragePermission == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        Log.v("requestPermission", "called");
        // For android 30 (11.0) and above READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE does nothing
        ActivityCompat.requestPermissions(ManageTrous.this, new String[]{ READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE}, this.REQUEST_CODE_PERM_RW);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.v("onRequestPermissionsResult", "called");
        switch (requestCode) {

            case REQUEST_CODE_PERM_RW:
                if (grantResults.length > 0) {
                    Log.v("onRequestPermissionsResult", "grantResults.length>0");
                    boolean readStorage = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorage = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (!readStorage){
                        Toast.makeText(this, "Manque autorisation lecture", Toast.LENGTH_LONG).show();
                    }
                    if (!writeStorage){
                        Toast.makeText(this, "Manque autorisation ecriture", Toast.LENGTH_LONG).show();
                    }
                }
        }
    }
}