package com.javeriana.ricardo.tallerfirebasemaps;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;

public class HomeActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Tcorreo = (TextView) findViewById(R.id.textViewCorreo);
        Tnombre = (TextView) findViewById(R.id.textViewNombre);

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
            Tcorreo.setText(email);
            Tnombre.setText(name);
        }

//--------------------------------------------------------------------------------------------------------

        mLocationRequest =	createLocationRequest();
        mFusedLocationClient =	LocationServices.getFusedLocationProviderClient(this);

        /* Move boton que permite volver a ubicación actual.*
        / Revisa si tiene permisos, sino los pide.
         */
        distanci = (TextView) findViewById(R.id.textViewDistancia);
        tiempo = (TextView) findViewById(R.id.textViewTiempo);

        move = (ImageView) findViewById(R.id.imageViewMove);
        move.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(),
                        android.Manifest.permission.ACCESS_FINE_LOCATION);
                if(permissionCheck==0){
                    if(location!=null && mMap!=null) {
                        origen = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(origen));
                        mMap.moveCamera(CameraUpdateFactory.zoomTo(17));
                    }
                }
                else
                    solicitudPermiso ();
            }
        });
        // acá el callback tiene la localización actualizada
        mLocationCallback =	new	LocationCallback()	 {
            @Override
            public	void	onLocationResult(LocationResult locationResult)	 {
                location	=	locationResult.getLastLocation();
                // Log.i("LOCATION",	"Location	update	in	the	callback:	"	+	location);
                localizarActual();
                calculoDistancia();
                if(recorriendo)
                    enMovimiento();
            }
        };

        // revisa si tiene permisos, sino los pide al rutas.
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION);
        if(permissionCheck == 0){
            localizacion();
        }else
            solicitudPermiso ();


        // Escucha enter al terminar de escribir destino
        dirección = (EditText) findViewById(R.id.texto);
        dirección.setImeActionLabel("Custom text", KeyEvent.KEYCODE_ENTER);
        dirección.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    buscarDireccion();
                    InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(dirección.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });

        busquedaAvanzada();

        avanzada = (Button) findViewById(R.id.buttonHelp);
        volver = (Button) findViewById(R.id.buttonBack);
        rutas = (Button) findViewById(R.id.buttonRutas);
        iniciarRecorrido = (Button) findViewById(R.id.buttonIniciar);
        volverLista = (Button) findViewById(R.id.buttonBackList);
        cancelar = (Button) findViewById(R.id.buttonCancelar);
        rutaInfo = (TextView) findViewById(R.id.rutaInfo);
        rutaInfo.setVisibility(View.GONE);
        volver.setVisibility(View.GONE);
        volverLista.setVisibility(View.GONE);
        iniciarRecorrido.setVisibility(View.GONE);
        cancelar.setVisibility(View.GONE);

        // si el usuario desea buscar de forma avanzada
        avanzada.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dirección.setVisibility(View.GONE);
                avanzada.setVisibility(View.GONE);
                autocompleteFragment.getView().setVisibility(View.VISIBLE);
                volver.setVisibility(View.VISIBLE);
            }
        });

        // si usuario desea volver a busqeuda normal
        volver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dirección.setVisibility(View.VISIBLE);
                avanzada.setVisibility(View.VISIBLE);
                autocompleteFragment.getView().setVisibility(View.GONE);
                volver.setVisibility(View.GONE);
                dirección.setText("");
                destinoAzul.setVisible(false);
                desti = null;
                Ndestino = null;
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
                iniciarRecorrido.setVisibility(View.VISIBLE);
            }
        });
        // inicia recorrido con origen y destino dado
        rutas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean complete = true;
                if(desti == null ){
                    Toast.makeText(InicioActivity.this, "Especifique un destino", Toast.LENGTH_SHORT).show();
                    complete = false;
                }
                if(origen == null){
                    Toast.makeText(InicioActivity.this, "Especifique un origen", Toast.LENGTH_SHORT).show();
                    complete = false;
                }
                if(complete){
                    DateTime now = new DateTime();
                    try {
                        results = DirectionsApi.newRequest(getGeoContext())
                                .mode(TravelMode.DRIVING)// preguntar si hacer dos solictudes con
                                // biclycling y si vacia con driving o solo driving
                                .origin(new com.google.maps.model.LatLng(origen.latitude,origen.longitude))
                                .destination(new com.google.maps.model.LatLng(desti.latitude,desti.longitude))
                                .alternatives(true)
                                .departureTime(now)
                                .await();
                        listRutasString.clear();
                        if(results.routes.length>0) {
                            for(int i = 0; i < results.routes.length;i++){
                              /*  System.out.println("esto: "+i+" - "+results.routes[i].legs[0].startAddress);
                                System.out.println("esto: "+i+" - "+results.routes[i].legs[0].distance);
                                System.out.println("esto: "+i+" - "+results.routes[i].legs[0].endAddress);
                                System.out.println("esto: "+i+" - "+results.routes[i].legs[0].arrivalTime);
                                System.out.println("esto: "+i+" - "+results.routes[i].legs[0].duration);*/

                                String valor = (i+1)+". Distancia a recorrer: "+results.routes[i].legs[0].distance+
                                        " Duración: "+results.routes[i].legs[0].duration;
                                listRutasString.add(valor);
                            }
                            listRutas.setVisibility(View.VISIBLE);
                            rutaInfo.setVisibility(View.VISIBLE);
                            volverLista.setVisibility(View.VISIBLE);
                            rutas.setVisibility(View.GONE);
                            dirección.setVisibility(View.GONE);
                            avanzada.setVisibility(View.GONE);
                            autocompleteFragment.getView().setVisibility(View.GONE);
                            volver.setVisibility(View.GONE);
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(desti));
                            mMap.moveCamera(CameraUpdateFactory.zoomTo(14));
                        }
                        else
                            Toast.makeText(InicioActivity.this, "No se encuentran rutas", Toast.LENGTH_SHORT).show();
                    } catch (com.google.maps.errors.ApiException e) {
                        e.printStackTrace();
                        Toast.makeText(InicioActivity.this, "Error con el servidor", Toast.LENGTH_SHORT).show();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Toast.makeText(InicioActivity.this, "Se perdió conexión", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(InicioActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        volverLista.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listRutas.setVisibility(View.GONE);
                rutaInfo.setVisibility(View.GONE);
                rutas.setVisibility(View.VISIBLE);
                volverLista.setVisibility(View.GONE);
                iniciarRecorrido.setVisibility(View.GONE);
                removePolyline();
                routeSelected = 0;
                if(!advanceLooking){
                    dirección.setVisibility(View.VISIBLE);
                    avanzada.setVisibility(View.VISIBLE);
                }else{
                    autocompleteFragment.getView().setVisibility(View.VISIBLE);
                    volver.setVisibility(View.VISIBLE);
                }
            }
        });

        iniciarRecorrido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                volverLista.setVisibility(View.GONE);
                listRutas.setVisibility(View.GONE);
                rutaInfo.setVisibility(View.GONE);
                cancelar.setVisibility(View.VISIBLE);
                iniciarRecorrido.setVisibility(View.GONE);
                recorriendo = true;
                distanci.setText("Distancia ruta: "+results.routes[routeSelected].legs[0].distance);
                tiempo.setText("Duración: "+results.routes[routeSelected].legs[0].duration);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(origen));
                mMap.moveCamera(CameraUpdateFactory.zoomTo(17));
            }
        });

        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dirección.setVisibility(View.VISIBLE);
                dirección.setText("");
                removePolyline();
                desti=null;
                avanzada.setVisibility(View.VISIBLE);
                rutas.setVisibility(View.VISIBLE);
                cancelar.setVisibility(View.GONE);
                destinoAzul.setVisible(false);
                distanci.setText("");
                tiempo.setText("");
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

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
}
