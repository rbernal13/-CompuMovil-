package com.javeriana.ricardo.tallerfirebasemaps;

import android.app.ProgressDialog;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class SettingActivity extends AppCompatActivity {
    private StorageReference mStorageRef;
    private FirebaseUser user = null;
    private TextView textCorreo,textNombre;
    private ImageView picture;
    private StorageReference riversRef;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        textCorreo = (TextView) findViewById(R.id.txtCorreoConfig);
        textNombre = (TextView) findViewById(R.id.txtNombreConfig);
        picture = (ImageView) findViewById(R.id.imageViewProfile);

        mProgressDialog = new ProgressDialog(this);

        user = FirebaseAuth.getInstance().getCurrentUser();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        if(user!=null){
            textCorreo.setText(user.getEmail());
            textNombre.setText(user.getDisplayName());
            cargarNormal(user);

        }
    }

    private void cargarNormal (FirebaseUser us)  {
        riversRef = mStorageRef.child("images/profile/"+us.getPhotoUrl().getLastPathSegment());

        mProgressDialog.setTitle("Cargando...");
        mProgressDialog.setMessage("Cargando foto del servidor");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
        riversRef.getDownloadUrl().addOnSuccessListener(this, new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) { // guardando esta uri no es necesario volver a buscarla en servidor
                Glide.with(SettingActivity.this)
                        .load(uri)
                        .fitCenter()
                        .centerCrop()
                        .into(picture);
                mProgressDialog.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                mProgressDialog.dismiss();
                Toast.makeText(SettingActivity.this, "Error cargando imagen", Toast.LENGTH_SHORT).show();
            }
        });

    }
}
