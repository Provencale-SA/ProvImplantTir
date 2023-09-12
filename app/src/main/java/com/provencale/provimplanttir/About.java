package com.provencale.provimplanttir;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.provencale.provimplanttir.R;

public class About extends AppCompatActivity {
    private Button buttonBack;
    private TextView tvAboutPathValue;

    Volees volees;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        buttonBack = findViewById(R.id.about_button_retour);
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();// return to previous
            }
        });

        this.volees = new Volees(this);

        tvAboutPathValue = (TextView)findViewById(R.id.about_path_value);
        tvAboutPathValue.setText(this.volees.getFile(this).getAbsolutePath());



    }



}