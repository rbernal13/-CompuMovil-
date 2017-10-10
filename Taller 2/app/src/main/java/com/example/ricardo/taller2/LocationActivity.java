package com.example.ricardo.taller2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class LocationActivity extends AppCompatActivity {

    private FusedLocationProviderClient mFusedLocationClient;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 3;

    TextView txtLatitud,txtLongitud,txtAltitud;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        mFusedLocationClient =	LocationServices.getFusedLocationProviderClient(this);

        txtLatitud = (TextView)findViewById(R.id.textAltitud);
        txtLongitud = (TextView)findViewById(R.id.textAltitud);
        txtAltitud = (TextView)findViewById(R.id.textAltitud);

        pedirPermisoLocacion();

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if(permissionCheck == 0){
            localizacion();
        }
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults){
        switch (requestCode){
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    localizacion();
                }else{
                        Toast.makeText(this, "Permiso denegado!", Toast.LENGTH_LONG).show();
                    }
                return;
            }
        }
    }

    private void localizacion() {

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

            mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        txtLatitud.setText(String.valueOf("Latitud" +location.getLatitude()));
                        txtLongitud.setText(String.valueOf("Longitud" +location.getLongitude()));
                        txtAltitud.setText(String.valueOf("Altitud" +location.getAltitude()));
                    }
                }
            });
        }
    }




    private void pedirPermisoLocacion() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an expanation to the user *asynchronously*

                Toast.makeText(this, "Se necesita el permiso para poder conocer la ubicacion", Toast.LENGTH_LONG).show();
            }
            // No explanation needed, we can request the permission.

            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},MY_PERMISSIONS_REQUEST_LOCATION);

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant.
            //The callback method gets the result of the request.

        }
    }
}

