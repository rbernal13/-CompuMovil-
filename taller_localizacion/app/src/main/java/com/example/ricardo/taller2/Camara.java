package com.example.ricardo.taller2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class Camara extends AppCompatActivity {

    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 1;
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 4;
    public static final int MY_PERMISSIONS_REQUEST_IMAGE_PICKER = 2;

    Button buttonGaleria, buttonCamara;
    ImageView imgGaleria;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camara);

        imgGaleria = (ImageView)findViewById(R.id.imageCamara);
        buttonGaleria = (Button)findViewById(R.id.btnUploadImage);
        buttonCamara = (Button)findViewById(R.id.btnCapture);

        buttonGaleria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pedirPermisoGaleria();

                int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE);

                if(permissionCheck == 0){
                   seleccionarGaleria();
                }


            }
        });

        buttonCamara.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pedirPermisoCamara();
                int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA);

                if(permissionCheck == 0){
                   tomarFoto();
                }


            }
        });




    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults){
        switch (requestCode){
            case MY_PERMISSIONS_REQUEST_IMAGE_PICKER:
                if(grantResults.length > 0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    seleccionarGaleria();
                }

            case MY_PERMISSIONS_REQUEST_CAMERA:
                if (grantResults.length > 0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                  tomarFoto();
                }

        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        switch (requestCode){
            case MY_PERMISSIONS_REQUEST_IMAGE_PICKER:
                if(resultCode == RESULT_OK){
                    try{
                        final Uri imageUri = data.getData();
                        final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                        final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                        imgGaleria.setImageBitmap(selectedImage);
                    }catch (FileNotFoundException e){
                        e.printStackTrace();
                    }
                }

            case MY_PERMISSIONS_REQUEST_CAMERA:
                if (resultCode == RESULT_OK){
                    Bundle eBundle = data.getExtras();
                    Bitmap imageBitmap = (Bitmap)eBundle.get("data");
                    imgGaleria.setImageBitmap(imageBitmap);
                }
        }
    }

    private void tomarFoto(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePictureIntent.resolveActivity(getPackageManager())!= null){
            startActivityForResult(takePictureIntent, MY_PERMISSIONS_REQUEST_CAMERA);
        }
    }

    private void seleccionarGaleria(){
        Intent pickImage = new Intent(Intent.ACTION_PICK);
        pickImage.setType("image/*");
        startActivityForResult(pickImage,MY_PERMISSIONS_REQUEST_IMAGE_PICKER);

    }



    private void pedirPermisoCamara() {

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                // Show an expanation to the user *asynchronously*

                Toast.makeText(this, "Se necesita el permiso para capturar fotos!", Toast.LENGTH_LONG).show();

            }
            // No explanation needed, we can request the permission.

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant.
            //The callback method gets the result of the request.

        }
    }

    private void pedirPermisoGaleria(){

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // Show an expanation to the user *asynchronously*

                Toast.makeText(this, "Se necesita el permiso para seleccionar las fotos", Toast.LENGTH_LONG).show();

            }
            // No explanation needed, we can request the permission.

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant.
            //The callback method gets the result of the request.

        }



    }




}



