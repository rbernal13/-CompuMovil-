package com.example.ricardo.taller2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    ImageButton imgbtnContactos;
    ImageButton imgbtnCamara;
    ImageButton imgbtnLocalicacion;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imgbtnCamara = (ImageButton) findViewById(R.id.ibtnCamara);
        imgbtnContactos = (ImageButton) findViewById(R.id.ibtnContactos);
        imgbtnLocalicacion = (ImageButton)findViewById(R.id.ibtnLocalizacion);

        imgbtnContactos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =  new Intent(v.getContext(), Contactos.class);
                startActivity(intent);
            }
        });
        imgbtnCamara.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =  new Intent(v.getContext(), Camara.class);
                startActivity(intent);

            }
        });

        imgbtnLocalicacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), LocationActivity.class);
                startActivity(intent);
            }
        });

    }
}
