package com.provencale.provimplanttir;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
}