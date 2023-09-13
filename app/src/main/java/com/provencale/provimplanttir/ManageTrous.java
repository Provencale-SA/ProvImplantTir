package com.provencale.provimplanttir;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

//https://stackoverflow.com/questions/23586910/selecting-item-from-listview-and-delete-it-onclick-android-to-do-list-applicat
public class ManageTrous extends AppCompatActivity {

    private Button buttonBackToMainActivity;
    private Button buttonOpenFile;
    ListView listTrous;
    TrousAdapter adapter;

    Volees volees;
    private final static int REQUEST_CODE_PERM_RW = 1001;
    private static final String FILEPROVIDERAUTHORITY =
            BuildConfig.APPLICATION_ID + ".fileprovider";

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
        Log.d("ManageTrous","onCreate:this.volees:"+this.volees.toString());

        // Create the adapter to convert the array to views
        adapter = new TrousAdapter(this, this.volees);

        // Attach the adapter to a ListView
        listTrous = (ListView)findViewById(R.id.listTrous);
        listTrous.setAdapter(adapter);
        Log.d("ManageTrous","onCreate:end:this.volees:"+this.volees.toString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        if (menu instanceof MenuBuilder) {
            ((MenuBuilder) menu).setOptionalIconsVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_open:
                File file = volees.getFile(getApplicationContext());
                Uri uri = FileProvider.getUriForFile(this, FILEPROVIDERAUTHORITY, file);
                String mime = "application/json";//"text/plain"
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, mime);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getApplicationContext(),
                            "Merci d'installer QField ou une application lisant les (geo)json",
                            Toast.LENGTH_LONG).show();
                }

                return true;
            case R.id.menu_share:
                Uri uri_share;
                File file_share = volees.getFile(getApplicationContext());
                if(file_share.exists()) {

                    uri_share = FileProvider.getUriForFile(this, FILEPROVIDERAUTHORITY, file_share);// Min SDK 24


                    Intent inter_share = new Intent();
                    inter_share.setAction(Intent.ACTION_SEND);
                    inter_share.setType("application/json");
                    inter_share.putExtra(Intent.EXTRA_STREAM, uri_share);
                    //inter_share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    // Following to avoid a (non blocking?) exception : java.lang.SecurityException: Permission Denial
                    Intent chooser = Intent.createChooser(inter_share, "Partager fichier");
                    List<ResolveInfo> resInfoList = this.getPackageManager().queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY);

                    for (ResolveInfo resolveInfo : resInfoList) {
                        String packageName = resolveInfo.activityInfo.packageName;
                        this.grantUriPermission(packageName, uri_share,  Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }
                    startActivity(chooser);
                }
                return true;
            case R.id.menu_delete_all:

                AlertDialog.Builder builder = new AlertDialog.Builder(ManageTrous.this);
                builder.setMessage("Etes-vous sûr de vouloir supprimer TOUS les trous ?");
                builder.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        volees.removeALlTrou();
                        volees.write(getApplicationContext()); // write to file
                        adapter.clear();
                        adapter.notifyDataSetChanged(); // force a view update (needed after this.remove(trou))
                    }
                });
                builder.setNegativeButton("Non", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();

                return true;


            case R.id.menu_about_file:
                Intent switchActivityIntent = new Intent(this, About.class);
                startActivity(switchActivityIntent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public class TrousAdapter extends ArrayAdapter<Trou> {
        public TrousAdapter(Context context, Volees volees) {
            super(context, 0, volees.toArrayList());
        }

        private void deleteItem(int position) {
            if (position >=0) {
                try {
                    Trou trou = getItem(position);
                    boolean result = volees.removeTrou(trou);// remove from volees (ie the common data with MainActivity)
                    if (result) {
                        Toast.makeText(getApplicationContext(),
                                "Suppresion de " + String.valueOf(trou.nomVolee) + ";" + String.valueOf(trou.numeroRangee) + ";" + String.valueOf(trou.numeroTrou),
                                Toast.LENGTH_SHORT).show();

                        volees.write(getApplicationContext()); // write to file
                        this.remove(trou); // Now lets remove it on the current screen. The data is a duplicate of volees
                    } else {
                        Toast.makeText(getApplicationContext(),
                                "Echec suppression",
                                Toast.LENGTH_SHORT).show();
                    }

                } catch (IndexOutOfBoundsException e) {
                    Toast.makeText(getApplicationContext(),"Problème lors de la suppression",Toast.LENGTH_SHORT).show();
                    Log.e("ManageTrous","TrousAdapter:deleteItem:Cannot getItem, Item probably already deleted");
                }
                this.notifyDataSetChanged(); // force a view update (needed after this.remove(trou))
            }
        }

        public void setDateTime (TextView view, Date timeUtc){
            TimeZone tz = TimeZone.getDefault();
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy-HH:mm:ss", Locale.getDefault());

            view.setText(formatter.format(timeUtc));
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
            TextView tvTime = (TextView) convertView.findViewById(R.id.tvTime);
            TextView tvNumeroRangee = (TextView) convertView.findViewById(R.id.tvNumeroRangee);
            TextView tvNumeroTrou = (TextView) convertView.findViewById(R.id.tvNumeroTrou);
            ImageButton btDelete = (ImageButton)convertView.findViewById(R.id.btDelete);

            // Populate the data into the template view using the data object
            tvNomVolee.setText(trou.nomVolee);
            setDateTime(tvTime,trou.timeUtc);
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