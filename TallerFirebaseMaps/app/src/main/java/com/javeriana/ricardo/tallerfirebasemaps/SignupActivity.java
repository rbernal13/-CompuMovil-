package com.javeriana.ricardo.tallerfirebasemaps;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.storage.StorageManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class SignupActivity extends AppCompatActivity {

    private final int MY_PERMISSIONS_REQUEST_CAMARA = 1;
    private final int MY_PERMISSIONS_REQUEST_EXTERNAL_STORAGE =2;

    private EditText editNombre, editApellido, editPassword, editEmail;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseRef;
    private ProgressDialog mProgressDialog;
    private StorageReference mStorageRef;
    private Uri profileUri = null;
    private Button buttonRegistrar, buttonSelecFoto,buttonTomarFoto;
    private ImageView imageProfile;
    private UserProfileChangeRequest.Builder upcrb = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        mProgressDialog = new ProgressDialog(this);
        mStorageRef = FirebaseStorage.getInstance().getReference();

        buttonTomarFoto = (Button)findViewById(R.id.btnTomarFoto);
        buttonSelecFoto = (Button)findViewById(R.id.btnSeleccionarFoto);
        buttonRegistrar = (Button)findViewById(R.id.btnRegistrar);

        editNombre = (EditText)findViewById(R.id.etxtNombre);
        editApellido = (EditText)findViewById(R.id.etxtApellido);
        editEmail = (EditText)findViewById(R.id.etxtEmail);
        editPassword = (EditText)findViewById(R.id.etxtPassword);



        buttonSelecFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
                if (permissionCheck == 0){
                    imagePicker();
                }else{
                    pedirPermisoGaleria();
                }
            }
        });

        buttonTomarFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA);
                if (permissionCheck == 0){
                    takePicture();
                }else{
                    pedirPermisoCamara();
                }
            }
        });

        buttonRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validateForm()){
                    registrar();
                }
            }
        });



    }//Fin onCreate

    private boolean validateForm() {
        boolean valid = true;
        String email = editEmail.getText().toString();
        String nombre = editNombre.getText().toString();
        String apellido = editApellido.getText().toString();
        String password = editPassword.getText().toString();

        //Correo
        if	(TextUtils.isEmpty(email))	{
            editEmail.setError("Campo requerido.");
            valid	=	false;
        }else{
            if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    valid	=	false;
                    editEmail.setError("Formato incorrecto.");
            }else{
                editEmail.setError(null);
            }
        }

        //Contraseña
        if (TextUtils.isEmpty(password)){
            editPassword.setError("Campo requerido.");
            valid = false;
        }else{
            if (password.length()<8){
                editPassword.setError("Minimo 8 caracteres.");
            }else{
                editPassword.setError(null);
            }
        }

        //Nombre
        if (TextUtils.isEmpty(nombre)){
            editNombre.setError("Campo requerido.");
            valid = false;
        }else {
            editNombre.setError(null);
        }

        //Apellido
        if (TextUtils.isEmpty(apellido)){
            editApellido.setError("Campo requerido.");
            valid = false;
        }else {
            editApellido.setError(null);
        }
        return valid;

    }//Fin ValidateForm

    private void registrar() {
        String mUser, mPass;
        mUser = editEmail.getText().toString();
        mPass = editPassword.getText().toString();

        mAuth.createUserWithEmailAndPassword(mUser,mPass).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null){
                        upcrb = new UserProfileChangeRequest.Builder();
                        upcrb.setDisplayName(editNombre.getText().toString()+" "+editApellido.getText().toString());
                        if (profileUri != null){
                            mProgressDialog.setMessage("Registrando informacion.....");
                            mProgressDialog.show();
                            final Uri file = profileUri;
                            upcrb.setPhotoUri(file);
                            StorageReference riversRef = mStorageRef.child("images/profile/"+file.getLastPathSegment());
                            riversRef.putFile(file).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    Toast.makeText(SignupActivity.this, "Se ha subido la imagen correctamente.", Toast.LENGTH_SHORT).show();
                                    mProgressDialog.dismiss();
                                    profileUri = null;
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    profileUri = null;
                                    Toast.makeText(SignupActivity.this, "Error al subir imagen.", Toast.LENGTH_SHORT).show();
                                    upcrb.setPhotoUri(null);
                                }
                            });

                        }else {
                            Toast.makeText(SignupActivity.this, "Selecciona una imagen.", Toast.LENGTH_SHORT).show();
                        }
                        user.updateProfile(upcrb.build());
                        startActivity(new Intent(SignupActivity.this, HomeActivity.class));
                    }
                }
                if (!task.isSuccessful()){
                    Toast.makeText(SignupActivity.this, "Error al registrar.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }//Fin Registrar

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults){
        switch (requestCode){
            case MY_PERMISSIONS_REQUEST_EXTERNAL_STORAGE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    imagePicker();
                }else {
                    Toast.makeText(this, "Permiso denegado a la Galeria", Toast.LENGTH_SHORT).show();
                }
                return;
            case MY_PERMISSIONS_REQUEST_CAMARA:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    takePicture();
                }else {
                    Toast.makeText(this, "Permiso denegado a la Camara", Toast.LENGTH_SHORT).show();
                }
                return;
        }

    }// Fin onRequestPermissions

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case MY_PERMISSIONS_REQUEST_EXTERNAL_STORAGE:
                if (requestCode == RESULT_OK){
                    try {
                        final Uri imageUri = data.getData();
                        profileUri = imageUri;
                        final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                        final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                        imageProfile.setImageBitmap(selectedImage);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                return;

            case MY_PERMISSIONS_REQUEST_CAMARA:
                if (requestCode == RESULT_OK){
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap)extras.get("data");
                    profileUri = data.getData();
                    imageProfile.setImageBitmap(imageBitmap);
                }
                return;

        }
    }//Fin onActivityResult

    public void imagePicker(){
        Intent pickImage = new Intent(Intent.ACTION_PICK);
        pickImage.setType("image/*");
        startActivityForResult(pickImage, MY_PERMISSIONS_REQUEST_EXTERNAL_STORAGE);
    }

    public void takePicture(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePictureIntent.resolveActivity(getPackageManager())!= null){
            startActivityForResult(takePictureIntent, MY_PERMISSIONS_REQUEST_CAMARA);
        }
    }

    public void pedirPermisoGaleria() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // Show an expanation to the user *asynchronously*
                Toast.makeText(this, "Se necesita el permiso para poder mostrar la imagen!", Toast.LENGTH_LONG).show();
            }
            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},MY_PERMISSIONS_REQUEST_EXTERNAL_STORAGE);
            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant.
            //The callback method gets the result of the request.
        }
    }

    public void pedirPermisoCamara(){
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.CAMERA)) {
                // Show an expanation to the user *asynchronously*
                Toast.makeText(this, "Se necesita el permiso para poder acceder a la camara!", Toast.LENGTH_LONG).show();
            }
            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMARA);
            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant.
            //The callback method gets the result of the request.
        }
    }
}
