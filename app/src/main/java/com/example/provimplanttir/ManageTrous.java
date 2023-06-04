package com.example.provimplanttir;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

//https://stackoverflow.com/questions/23586910/selecting-item-from-listview-and-delete-it-onclick-android-to-do-list-applicat
public class ManageTrous extends AppCompatActivity {

    private Button buttonBackToMainActivity;
    ListView listTrous;

    Volees volees;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        // Create the adapter to convert the array to views
        TrousAdapter adapter = new TrousAdapter(this, this.volees);

        // Attach the adapter to a ListView
        listTrous = (ListView)findViewById(R.id.listTrous);
        listTrous.setAdapter(adapter);

    }

    public class TrousAdapter extends ArrayAdapter<Trou> {
        public TrousAdapter(Context context, Volees volees) {
            super(context, 0, volees.toArrayList());
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
            // Populate the data into the template view using the data object
            tvNomVolee.setText(trou.nomVolee);
            tvNumeroRangee.setText(String.valueOf(trou.numeroRangee));
            tvNumeroTrou.setText(String.valueOf(trou.numeroTrou));
            // Return the completed view to render on screen
            return convertView;
        }

    }
}