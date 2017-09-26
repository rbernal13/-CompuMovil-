package com.example.ricardo.taller2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class Contactos extends AppCompatActivity {

    ListView list;
    String[] mProjection;
    ContactsCursor mCursorAdapter;
    Cursor mContactsCursor;

    public static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 5;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contactos);
        list = (ListView)findViewById(R.id.listContactos);
        mProjection = new String[]{
                ContactsContract.Profile._ID,
                ContactsContract.Profile.DISPLAY_NAME_PRIMARY,
        };
        mCursorAdapter = new ContactsCursor(this, null, 0);
        list.setAdapter(mCursorAdapter);

        pedirPermisoContactos();

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);

        if(permissionCheck == 0){
            mContactsCursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,mProjection, null, null, null);
            mCursorAdapter.changeCursor(mContactsCursor);
        }
    }


    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults){
        switch (requestCode){
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Cursor mContactsCursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,mProjection, null, null, null);
                    mCursorAdapter.changeCursor(mContactsCursor);
                }else{
                    Toast.makeText(this, "Permiso denegado!", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }





    private void pedirPermisoContactos(){
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)!= PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                // Show an expanation to the user *asynchronously*

                Toast.makeText(this, "Se necesita el permiso para poder mostrar los contactos!", Toast.LENGTH_LONG).show();
            }
            // No explanation needed, we can request the permission.

            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_CONTACTS},MY_PERMISSIONS_REQUEST_READ_CONTACTS);

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant.
            //The callback method gets the result of the request.

        }

    }

}
