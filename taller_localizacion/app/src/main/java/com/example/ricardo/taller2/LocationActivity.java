package com.example.ricardo.taller2;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LocationActivity extends AppCompatActivity {

    private FusedLocationProviderClient mFusedLocationClient;
    public static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 3;
    public static final int MY_PERMISSIONS_REQUEST_CHECK_SETTINGS = 4;

    private TextView txtLatitud,txtLongitud,txtAltitud, txtDistancia;
    private Button buttonGuardar;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;

    private List<String> lst_array = new ArrayList<String>();
    private ArrayAdapter<String> adapter;
    private ListView lst_View;

    public double longitudPlaza = -74.076033,latitudPlaza = 4.598110;
    public final static double RADIUS_OF_EARTH = 6371;

    private JSONArray localizaciones = new JSONArray();
    private Date date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //Evita rotacion automatica del celular

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,lst_array);

        lst_View = (ListView)findViewById(R.id.listaHistorial);

        txtLatitud = (TextView)findViewById(R.id.textLatitud);
        txtLongitud = (TextView)findViewById(R.id.textLongitud);
        txtAltitud = (TextView)findViewById(R.id.textAltitud);
        txtDistancia = (TextView)findViewById(R.id.textDistancia);
        buttonGuardar = (Button)findViewById(R.id.btnGuardar);

        mFusedLocationClient =	LocationServices.getFusedLocationProviderClient(this);
        mLocationRequest = createLocationRequest();

        mLocationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult){
                Location	location	=	locationResult.getLastLocation();
                if(location != null){
                    txtLatitud.setText("Latitud: "+String.valueOf(location.getLatitude()));
                    txtLongitud.setText("Longitud: "+String.valueOf(location.getLongitude()));
                    txtAltitud.setText("Altitud: "+String.valueOf(location.getAltitude()));
                    txtDistancia.setText("Distancia a la Plaza de Bolivar es "+ String.valueOf(distance(location.getLatitude(), location.getLongitude(), latitudPlaza, longitudPlaza))+" km");

                    date = new Date();
                    String fecha = "Fecha: YYYY/MM/DD hh:mm:ss "+(date.getYear()+1900)+"/"+(date.getMonth()+1+"/"+(date.getDay()+10)+" "+date.getHours()+" "+date.getMinutes()+" "+date.getSeconds());
                    String valor = "Lat: "+String.valueOf(location.getLatitude())+" Long: "+String.valueOf(location.getLongitude())+" "+fecha;
                    lst_array.add(valor);

                    lst_View.setAdapter(adapter);

                    localizaciones.put(toJSON(String.valueOf(location.getLatitude()),String.valueOf(location.getLongitude()),fecha));
                }
            }
        };

        pedirPermisoLocacion();
        localizacion();

        buttonGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeJSONObject();
            }
        });

    }//Fin OnCreate();

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults){
        switch (requestCode){
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    localizacion();
                }else{
                        Toast.makeText(this, "Permiso de localizacion denegado!", Toast.LENGTH_LONG).show();
                    }
                return;
            }
        }
    }


    private void localizacion() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if(permissionCheck == 0){
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
            SettingsClient client = LocationServices.getSettingsClient(LocationActivity.this);
            Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
            task.addOnSuccessListener(LocationActivity.this, new OnSuccessListener<LocationSettingsResponse>() {
                @Override
                public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                    startLocationUpdates();
                }
            });

            task.addOnFailureListener(LocationActivity.this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    int statusCode = ((ApiException)e).getStatusCode();
                    switch (statusCode){
                        case CommonStatusCodes.RESOLUTION_REQUIRED:
                            ResolvableApiException resolvable = (ResolvableApiException) e;
                            try {
                                resolvable.startResolutionForResult(LocationActivity.this, MY_PERMISSIONS_REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException e1) {
                                e1.printStackTrace();
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            break;
                    }
                }
            });
        }
    }

    /*
        Localizacion dinamica cada 10 segundos con GPS
     */
    protected LocationRequest createLocationRequest(){
        LocationRequest mLocationRequest =  new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    /*
        Verifica el permiso y obtiene la localizacion
     */
    private void startLocationUpdates(){
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        switch (requestCode){
            case MY_PERMISSIONS_REQUEST_CHECK_SETTINGS:
                if (resultCode == RESULT_OK){
                    startLocationUpdates();
                } else {
                    Toast.makeText(this, "Sin acceso a localizacion, hardware deshabilitado.", Toast.LENGTH_LONG).show();
                }
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

            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant.
            //The callback method gets the result of the request.

        }
    }

    /*
        Escritura de objetos JSON
     */
    public JSONObject toJSON (String lat, String lon, String fecha)	{
        JSONObject obj =	new	JSONObject();
        try	{
            obj.put("latitud",	lat);
            obj.put("longitud",	lon);
            obj.put("date",	fecha);
        }	catch	(JSONException e)	{
            e.printStackTrace();
        }
        return	obj;
    }
    private	void	writeJSONObject(){
        Writer output	=	null;
        String	filename=	"locations.json";
        try	{
            File file	=	new File(getBaseContext().getExternalFilesDir(null), filename);
            Log.i("LOCATION",	"Ubicacion de	archivo:	"+file);
            output	=	new BufferedWriter(new FileWriter(file));
            output.write(localizaciones.toString());
            output.close();
            Toast.makeText(getApplicationContext(),	 "Location	saved",
                    Toast.LENGTH_LONG).show();
        }	catch	(Exception	e)	 {
            Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    /*
        Calculo de distancias entre 2 puntos
     */
    public	double	distance(double	 lat1,	double	long1,	double	lat2,	double	long2)	{
        double	latDistance =	Math.toRadians(lat1	 - lat2);
        double	lngDistance =	Math.toRadians(long1	 - long2);
        double	a	=	Math.sin(latDistance /	2)	*	Math.sin(latDistance /	2)
                +	Math.cos(Math.toRadians(lat1))	 *	Math.cos(Math.toRadians(lat2))
                *	Math.sin(lngDistance /	2)	*	Math.sin(lngDistance /	2);
        double	c	=	2	*	Math.atan2(Math.sqrt(a),	 Math.sqrt(1 - a));
        double	result	=	RADIUS_OF_EARTH	 *	c;
        return	Math.round(result*100.0)/100.0;
    }
}

