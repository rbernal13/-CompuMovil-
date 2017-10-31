package com.javeriana.ricardo.tallerfirebasemaps;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.android.PolyUtil;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;
import org.joda.time.DateTime;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class HomeActivity extends FragmentActivity implements OnMapReadyCallback {

    private final int MY_PERMISSIONS_REQUEST_ACCESS_FINE =8;
    private final int MY_REQUEST_CHECK_SETTINGS = 9;

    public final static double	RADIUS_OF_EARTH_KM = 6371;

    public static final double lowerLeftLatitude = 1.396967;
    public static final double lowerLeftLongitude= -78.903968;
    public static final double upperRightLatitude= 11.983639;
    public static final double upperRigthLongitude= -71.869905;

    private static final String GOOGLE_KEY_SERVER = "AIzaSyBACjrLdnt8SGCup1gXAOOAnEW8NZm4ZEU";

    private GoogleMap mMap;
    private TextView textCorreo,textNombre, textDistancia, textTiempo, rutaInfo;
    private EditText etextDireccion;
    private Button buttonIniciarRecorrido, buttonAvanzado, buttonRuta, buttonVolver, buttonVolverLst,buttonCancelar;
    private ImageView move_mark;
    private View popup = null;
    private ListView listRutas = null;
    private List<String> listRutasString = new ArrayList<String>();
    private FirebaseAuth mAuth = null;
    private FirebaseUser user = null;
    private Location location = null;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private FusedLocationProviderClient mFusedLocationClient;
    private LatLng pOrigen, pDestino;
    private String nOrigen = "Universidad Javeriana", nDestino = null;
    private Marker bikeActual,destinoAzul,local1,local2,local3,local4,local5;
    private PlaceAutocompleteFragment autocompleteFragment = null;
    private DirectionsResult results = null;
    private boolean first = true, advanceLooking = false, recorriendo = false, tiempo = false;
    private Polyline poly;
    private int routeSelected =0;

    public double longitudBicityke =  -74.065709, latitudBicityke = 4.627275;
    public double longituDonLuca =  -74.065641, latitudDonLuca = 4.629657;
    public double longituOxxo =  -74.065199, latitudOxxo = 4.628697;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        textCorreo = (TextView) findViewById(R.id.txtEmail);
        textNombre = (TextView) findViewById(R.id.txtNombre);

        mAuth =	FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Name, email address, and profile photo Url
            String name = user.getDisplayName();
            String email = user.getEmail();
            //String nickname = user.getDisplayName();
            //Uri photoUrl = user.getPhotoUrl();

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getToken() instead.
            //String uid = user.getUid();
            textCorreo.setText(email);
            textNombre.setText(name);
        }

        mLocationRequest =	createLocationRequest();
        mFusedLocationClient =	LocationServices.getFusedLocationProviderClient(this);



        textDistancia = (TextView) findViewById(R.id.txtDistancia);
        textTiempo = (TextView) findViewById(R.id.txtTiempo);

        move_mark = (ImageView) findViewById(R.id.imgMLocation);
        move_mark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(),
                        android.Manifest.permission.ACCESS_FINE_LOCATION);
                if(permissionCheck==0){
                    if(location!=null && mMap!=null) {
                        pOrigen = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(pOrigen));
                        mMap.moveCamera(CameraUpdateFactory.zoomTo(17));
                    }
                }
                else
                    solicitudPermiso ();
            }
        });

        mLocationCallback =	new	LocationCallback()	 {
            @Override
            public	void	onLocationResult(LocationResult locationResult)	 {
                location	=	locationResult.getLastLocation();
                localizarActual();
                calculoDistancia();
                if(recorriendo)
                    enMovimiento();
            }
        };

        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION);
        if(permissionCheck == 0){
            localizacion();
        }else
            solicitudPermiso ();

        etextDireccion = (EditText) findViewById(R.id.etxtDireccion);
        etextDireccion.setImeActionLabel("Custom text", KeyEvent.KEYCODE_ENTER);
        etextDireccion.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    buscarDireccion();
                    InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(etextDireccion.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });

        busquedaAvanzada();

        buttonAvanzado = (Button) findViewById(R.id.btnAyuda);
        buttonVolver = (Button) findViewById(R.id.btnAtras);
        buttonRuta = (Button) findViewById(R.id.btnRutas);
        buttonIniciarRecorrido = (Button) findViewById(R.id.btnIniciar);
        buttonVolverLst = (Button) findViewById(R.id.btnLstVolver);
        buttonCancelar = (Button) findViewById(R.id.btnCancelar);
        rutaInfo = (TextView) findViewById(R.id.rutaInfo);
        rutaInfo.setVisibility(View.GONE);
        buttonVolver.setVisibility(View.GONE);
        buttonVolverLst.setVisibility(View.GONE);
        buttonIniciarRecorrido.setVisibility(View.GONE);
        buttonCancelar.setVisibility(View.GONE);
        buttonAvanzado.setVisibility(View.GONE);


        buttonAvanzado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etextDireccion.setVisibility(View.GONE);
                buttonAvanzado.setVisibility(View.GONE);
                autocompleteFragment.getView().setVisibility(View.VISIBLE);
                buttonVolver.setVisibility(View.VISIBLE);
            }
        });

        buttonVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etextDireccion.setVisibility(View.VISIBLE);
                buttonAvanzado.setVisibility(View.VISIBLE);
                autocompleteFragment.getView().setVisibility(View.GONE);
                buttonVolver.setVisibility(View.GONE);
                etextDireccion.setText("");
                destinoAzul.setVisible(false);
                pDestino = null;
                nDestino = null;
                calculoDistancia();
            }
        });

        listRutas = (ListView) findViewById(R.id.listRutas);
        listRutas.setVisibility(View.GONE);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, listRutasString);
        listRutas.setAdapter(adapter);
        listRutas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                routeSelected = i;
                if(removePolyline())
                    addPolyline(results, i);
                buttonIniciarRecorrido.setVisibility(View.VISIBLE);
            }
        });

        buttonRuta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean complete = true;
                if(pDestino == null ){
                    Toast.makeText(HomeActivity.this, "Especifique un destino", Toast.LENGTH_SHORT).show();
                    complete = false;
                }
                if(pDestino == null){
                    Toast.makeText(HomeActivity.this, "Especifique un origen", Toast.LENGTH_SHORT).show();
                    complete = false;
                }
                if(complete){
                    DateTime now = new DateTime();
                    try {
                        results = DirectionsApi.newRequest(getGeoContext())
                                .mode(TravelMode.DRIVING).origin(new com.google.maps.model.LatLng(pDestino.latitude,pDestino.longitude))
                                .destination(new com.google.maps.model.LatLng(pDestino.latitude,pDestino.longitude))
                                .alternatives(true)
                                .departureTime(now)
                                .await();
                        listRutasString.clear();
                        if(results.routes.length>0) {
                            for(int i = 0; i < results.routes.length;i++){


                                String valor = (i+1)+". Distancia a recorrer: "+results.routes[i].legs[0].distance+
                                        " Duración: "+results.routes[i].legs[0].duration;
                                listRutasString.add(valor);
                            }
                            listRutas.setVisibility(View.VISIBLE);
                            rutaInfo.setVisibility(View.VISIBLE);
                            buttonVolverLst.setVisibility(View.VISIBLE);
                            buttonRuta.setVisibility(View.GONE);
                            etextDireccion.setVisibility(View.GONE);
                            buttonAvanzado.setVisibility(View.GONE);
                            autocompleteFragment.getView().setVisibility(View.GONE);
                            buttonVolver.setVisibility(View.GONE);
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(pDestino));
                            mMap.moveCamera(CameraUpdateFactory.zoomTo(14));
                        }
                        else
                            Toast.makeText(HomeActivity.this, "No se encuentran rutas", Toast.LENGTH_SHORT).show();
                    } catch (ApiException e) {
                        e.printStackTrace();
                        Toast.makeText(HomeActivity.this, "Error con el servidor", Toast.LENGTH_SHORT).show();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Toast.makeText(HomeActivity.this, "Se perdió conexión", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(HomeActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        buttonVolverLst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listRutas.setVisibility(View.GONE);
                rutaInfo.setVisibility(View.GONE);
                buttonRuta.setVisibility(View.VISIBLE);
                buttonVolverLst.setVisibility(View.GONE);
                buttonIniciarRecorrido.setVisibility(View.GONE);
                removePolyline();
                routeSelected = 0;
                if(!advanceLooking){
                    etextDireccion.setVisibility(View.VISIBLE);
                    buttonAvanzado.setVisibility(View.VISIBLE);
                }else{
                    autocompleteFragment.getView().setVisibility(View.VISIBLE);
                    buttonVolver.setVisibility(View.VISIBLE);
                }
            }
        });

        buttonIniciarRecorrido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonVolverLst.setVisibility(View.GONE);
                listRutas.setVisibility(View.GONE);
                rutaInfo.setVisibility(View.GONE);
                buttonCancelar.setVisibility(View.VISIBLE);
                buttonIniciarRecorrido.setVisibility(View.GONE);
                recorriendo = true;
                textDistancia.setText("Distancia ruta: "+results.routes[routeSelected].legs[0].distance);
                textTiempo.setText("Duración: "+results.routes[routeSelected].legs[0].duration);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(pOrigen));
                mMap.moveCamera(CameraUpdateFactory.zoomTo(17));
            }
        });

        buttonCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etextDireccion.setVisibility(View.VISIBLE);
                etextDireccion.setText("");
                removePolyline();
                pDestino=null;
                buttonAvanzado.setVisibility(View.VISIBLE);
                buttonRuta.setVisibility(View.VISIBLE);
                buttonCancelar.setVisibility(View.GONE);
                destinoAzul.setVisible(false);
                textDistancia.setText("");
                textTiempo.setText("");
                routeSelected = 0;
                recorriendo = false;
            }
        });
    }//Fin onCreate

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Date date = new Date();
        // Add a marker in Sydney and move the camera
        mMap.moveCamera(CameraUpdateFactory.zoomTo(15));
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        pOrigen = new LatLng(4.628479,-74.064908);
        if(date.getHours()>=6 && date.getHours()<18  ){
            if(move_mark != null)
                move_mark.setImageResource(R.drawable.move_location);
            mMap.setMapStyle(MapStyleOptions
                    .loadRawResourceStyle(this, R.raw.style_dia_json));
            bikeActual = mMap.addMarker(new MarkerOptions()
                    .position(pOrigen)
                    .title("Posición origen")
                    .icon(BitmapDescriptorFactory
                            .fromResource(R.drawable.bike)));
            bikeActual.setVisible(false);

        }else {
            if(move_mark != null)
                move_mark.setImageResource(R.drawable.move_location_noche);
            mMap.setMapStyle(MapStyleOptions
                    .loadRawResourceStyle(this, R.raw.style_noche_json));
            bikeActual = mMap.addMarker(new MarkerOptions()
                    .position(pOrigen)
                    .title("Posición origen")
                    .icon(BitmapDescriptorFactory
                            .fromResource(R.drawable.bicinight)));
            bikeActual.setVisible(false);
        }

        destinoAzul = mMap.addMarker(new MarkerOptions()
                .position(pOrigen)
                .icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        destinoAzul.setVisible(false);


        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                if(popup == null){
                    popup = getLayoutInflater().inflate(R.layout.popupmaps,null);
                }

                TextView tv = (TextView) popup.findViewById(R.id.title);
                tv.setText(marker.getTitle());
                tv = (TextView) popup.findViewById(R.id.snippet);
                tv.setText(marker.getSnippet());
                return popup;
            }
        });
        LatLng t1 = new LatLng(latitudBicityke, longitudBicityke);
        local1 = mMap.addMarker(new MarkerOptions()
                .position(t1)
                .title("Bicityke")
                .snippet("Lunes - Viernes: 8:00-18:00")
                .icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.bicityke)));

        LatLng t2 = new LatLng(latitudDonLuca, longituDonLuca);
        local2 = mMap.addMarker(new MarkerOptions()
                .position(t2)
                .title("Taller de Ciclas Don Luca")
                .snippet("Lunes - Sábado: 12:00-22:00 \n" +
                        "Domingo: 6:00-11:00")
                .icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.don_luca)));

        LatLng t3 = new LatLng(latitudOxxo, longituOxxo);
        local3 = mMap.addMarker(new MarkerOptions()
                .position(t3)
                .title("OXXO")
                .snippet("Lunes - Domingo: 24/7")
                .icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.oxxo)));

    }

    @Override
    public	boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu,	menu);
        return	true;
    }

    @Override
    public	boolean onOptionsItemSelected(MenuItem item){
        int itemClicked =	item.getItemId();
        if(itemClicked ==	R.id.menuLogOut){
            mAuth.signOut();
            Intent intent	=	new	Intent(HomeActivity.this,	LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }else	if	(itemClicked ==	R.id.menuSettings){
            Intent intent	=	new	Intent(HomeActivity.this,	SettingActivity.class);
            startActivity(intent);
        }else if(itemClicked == R.id.menuTime){
            if(!tiempo){
                if(move_mark != null)
                    move_mark.setImageResource(R.drawable.move_location);
                mMap.setMapStyle(MapStyleOptions
                        .loadRawResourceStyle(this, R.raw.style_dia_json));
                bikeActual.setIcon(BitmapDescriptorFactory
                        .fromResource(R.drawable.bike));
                tiempo = true;
            }else {
                if(move_mark != null)
                    move_mark.setImageResource(R.drawable.move_location_noche);
                mMap.setMapStyle(MapStyleOptions
                        .loadRawResourceStyle(this, R.raw.style_noche_json));
                bikeActual.setIcon(BitmapDescriptorFactory
                        .fromResource(R.drawable.bicinight));
                tiempo = false;
            }
        }
        return	super.onOptionsItemSelected(item);
    }

    private void localizarActual(){
        if(mMap != null && location!=null){
            pOrigen = new LatLng(location.getLatitude(), location.getLongitude());
            bikeActual.setPosition(pOrigen);
            bikeActual.setVisible(true);
            if(first){
                mMap.moveCamera(CameraUpdateFactory.newLatLng(pOrigen));
                first = false;
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    localizacion();
                } else {
                    Toast.makeText(getApplicationContext(),"Permiso denegado localización", Toast.LENGTH_SHORT).show();
                }
                return;
            }

        }
    }

    private void solicitudPermiso (){
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(this, "Se necesita el permiso para poder mostrar los contactos!", Toast.LENGTH_LONG).show();
            }
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE);


        }
    }

    private void localizacion(){
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION);
        if(permissionCheck == 0){

            LocationSettingsRequest.Builder builder	=	new
                    LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
            SettingsClient client	 =	LocationServices.getSettingsClient(HomeActivity.this);
            Task<LocationSettingsResponse> task	=	client.checkLocationSettings(builder.build());
            task.addOnSuccessListener(HomeActivity.this,	 new	OnSuccessListener<LocationSettingsResponse>()
            {
                @Override
                public	void	onSuccess(LocationSettingsResponse locationSettingsResponse)	 {
                    startLocationUpdates();	 //Todas las condiciones para	recibir localizaciones
                }
            });

            /*
            task.addOnFailureListener(HomeActivity.this,	 new	OnFailureListener()	 {
                @Override
                public	void	onFailure( Exception	 e)	{
                    int statusCode =	((ApiException)	e.getStatusCode();
                    switch	(statusCode)	{
                        case	CommonStatusCodes.RESOLUTION_REQUIRED:
                            //	Location	settings	are	not	satisfied,	but	this	can	be	fixed	by	showing	the	user	a	dialog.
                            try	{//	Show	the	dialog	by	calling	startResolutionForResult(),	and	check	the	result	in	onActivityResult().
                                ResolvableApiException resolvable	 =	(ResolvableApiException)	 e;
                                resolvable.startResolutionForResult(HomeActivity.this,
                                        MY_REQUEST_CHECK_SETTINGS);// lanza dialogo para encender localización
                            }	catch	(IntentSender.SendIntentException sendEx)	{
                                //	Ignore	the	error.
                            }	break;
                        case	LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            //	Location	settings	are	not	satisfied.	No	way	to	fix	the	settings	so	we	won't	show	the	dialog.
                            break;
                    }
                }
            });*/
        }
    }


    protected	LocationRequest createLocationRequest()	 {
        LocationRequest mLocationRequest =	new	LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return	mLocationRequest;
    }


    private	void	startLocationUpdates()	 {
        if	(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,mLocationCallback,null);
        }
    }

    @Override
    protected	void	onActivityResult(int requestCode,	 int resultCode,	 Intent data)	 {
        switch	(requestCode)	 {
            case	MY_REQUEST_CHECK_SETTINGS:	 {
                if	(resultCode ==	RESULT_OK)	 {
                    startLocationUpdates();
                }	else	{
                    Toast.makeText(this,
                            "Sin	acceso a	localización,	hardware	deshabilitado!",
                            Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

   public	double	distance(double	 lat1,	double	long1,	double	lat2,	double	long2)	{
        double	latDistance =Math.toRadians(lat1-lat2);
        double	lngDistance =Math.toRadians(long1 - long2);
        double	a	=	Math.sin(latDistance/2)*Math.sin(latDistance/2)
                +	Math.cos(Math.toRadians(lat1))*Math.cos(Math.toRadians(lat2))
                *	Math.sin(lngDistance/2)*Math.sin(lngDistance/2);
        double	c	=	2*Math.atan2(Math.sqrt(a),Math.sqrt(1-a));
        double	result	=RADIUS_OF_EARTH_KM*c;
        return	Math.round(result*100.0)/100.0;
    }


    private void buscarDireccion(){
        Geocoder mGeocoder = new Geocoder(getBaseContext());
        String addressString = textDistancia.getText().toString();
        if (!addressString.isEmpty()) {
            try {
                List<Address> addresses = mGeocoder.getFromLocationName(
                        addressString, 4,
                        lowerLeftLatitude,
                        lowerLeftLongitude,
                        upperRightLatitude,
                        upperRigthLongitude);
                if (addresses != null && !addresses.isEmpty()) {
                    Address addressResult = addresses.get(0);
                    pDestino = new LatLng(addressResult.getLatitude(), addressResult.getLongitude());
                    if (mMap != null) {
                        nDestino = addressResult.getFeatureName();
                        destinoAzul.setPosition(pDestino);
                        destinoAzul.setVisible(true);
                        destinoAzul.setTitle(addressResult.getFeatureName());
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(pDestino));
                        mMap.moveCamera(CameraUpdateFactory.zoomTo(15));
                        calculoDistancia();
                        advanceLooking = false;
                    }
                } else {
                    Toast.makeText(HomeActivity.this, "Dirección no encontrada", Toast.LENGTH_SHORT).show();
                    destinoAzul.setVisible(false);
                    pDestino = null;
                    nDestino = null;
                    calculoDistancia();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(HomeActivity.this, "Null, campo vacio", Toast.LENGTH_SHORT).show();
            destinoAzul.setVisible(false);
            pDestino = null;
            nDestino = null;
            calculoDistancia();
        }
    }

    private void calculoDistancia(){
        if	(pOrigen !=	null && pDestino!=null && !recorriendo){
            textDistancia.setText("Distancia es: "+
                    String.valueOf(distance(location.getLatitude(),location.getLongitude(),
                            pDestino.latitude,pDestino.longitude))+" km");
        }
        else
            textDistancia.setText("");
    }

    private void busquedaAvanzada(){
        autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        autocompleteFragment.getView().setVisibility(View.GONE);
        autocompleteFragment.setBoundsBias(new LatLngBounds(
                new LatLng(lowerLeftLongitude, lowerLeftLatitude),
                new LatLng(upperRigthLongitude, upperRightLatitude)));
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                pDestino = new LatLng(place.getLatLng().latitude, place.getLatLng().longitude);
                if (mMap != null) {
                    nDestino =  place.getName().toString();
                    destinoAzul.setPosition(pDestino);
                    destinoAzul.setVisible(true);
                    destinoAzul.setTitle(place.getName().toString());
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(pDestino));
                    mMap.moveCamera(CameraUpdateFactory.zoomTo(15));
                    calculoDistancia();
                    advanceLooking = true;
                }
            }
            @Override
            public void onError(Status status) {
                Toast.makeText(HomeActivity.this, "Error cargando destino", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private GeoApiContext getGeoContext(){
        GeoApiContext geoApiContext = new GeoApiContext();
        return geoApiContext.setQueryRateLimit(3).setApiKey(GOOGLE_KEY_SERVER).setConnectTimeout(1,
                TimeUnit.SECONDS).setReadTimeout(1, TimeUnit.SECONDS).setWriteTimeout(1, TimeUnit.SECONDS);
    }

    private void addPolyline(DirectionsResult results, int r) {
        List<LatLng> decodedPath = PolyUtil.decode(results.routes[r].overviewPolyline.getEncodedPath());
        poly = mMap.addPolyline(new PolylineOptions().addAll(decodedPath)
                .width(2)
                .color(Color.RED));

    }

    private boolean removePolyline(){
        try{
            if(poly!=null){
                poly.remove();
            }
            return true;
        }catch (Exception e){
            return false;
        }
    }

    private void enMovimiento(){
        boolean complete = true;
        if(pDestino == null || pOrigen == null){
            complete = false;
        }
        if(complete) {
            DateTime now = new DateTime();
            try {
                results = DirectionsApi.newRequest(getGeoContext()).mode(TravelMode.DRIVING)

                        .origin(new com.google.maps.model.LatLng(pOrigen.latitude,pOrigen.longitude))
                        .destination(new com.google.maps.model.LatLng(pDestino.latitude, pDestino.longitude))
                        .alternatives(true)
                        .departureTime(now)
                        .await();
                if (results.routes.length > 0) {
                    if (results.routes.length >= routeSelected) {
                        textDistancia.setText("Distancia ruta: " + results.routes[routeSelected].legs[0].distance);
                        textTiempo.setText("Duración: " + results.routes[routeSelected].legs[0].duration);
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(pOrigen));
                        mMap.moveCamera(CameraUpdateFactory.zoomTo(17));
                        if (removePolyline())
                            addPolyline(results, routeSelected);
                    } else {
                        textDistancia.setText("Distancia ruta: " + results.routes[routeSelected].legs[0].distance);
                        textTiempo.setText("Duración: " + results.routes[routeSelected].legs[0].duration);
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(pOrigen));
                        mMap.moveCamera(CameraUpdateFactory.zoomTo(17));
                        if (removePolyline())
                            addPolyline(results, routeSelected);
                    }
                }
                else {
                    if (removePolyline()) {
                        textDistancia.setText("Espere un momento");
                        textTiempo.setText("");
                    }
                }
            } catch (ApiException e) {
                recorriendo = false;
                textTiempo.setText("");
                textDistancia.setText("Vuelva a iniciar recorrido");
                e.printStackTrace();
                Toast.makeText(HomeActivity.this, "Error con el servidor", Toast.LENGTH_SHORT).show();
            } catch (InterruptedException e) {
                recorriendo = false;
                textTiempo.setText("");
                textDistancia.setText("Vuelva a iniciar recorrido");
                e.printStackTrace();
                Toast.makeText(HomeActivity.this, "Se perdió conexión", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                recorriendo = false;
                textTiempo.setText("");
                textDistancia.setText("Vuelva a iniciar recorrido");
                e.printStackTrace();
                Toast.makeText(HomeActivity.this, "Error en la ruta", Toast.LENGTH_SHORT).show();
            }

        }
    }
}
