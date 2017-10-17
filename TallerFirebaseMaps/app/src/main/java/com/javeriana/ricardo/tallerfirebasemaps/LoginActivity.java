package com.javeriana.ricardo.tallerfirebasemaps;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private Button buttonLogin, buttonSignup;
    private TextView textUser, textPass;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private boolean autenticado = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                   // Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    Toast.makeText(LoginActivity.this, "onAuthStateChanged:signed_in:", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                } else {
                    // User is signed out
                    //Log.d(TAG, "onAuthStateChanged:signed_out");
                }

            }
        };//Fin mAuthListener

        buttonLogin = (Button)findViewById(R.id.btnLogin);
        buttonSignup = (Button)findViewById(R.id.btnSignup);
        textUser = (TextView)findViewById(R.id.etxtUser);
        textPass = (TextView)findViewById(R.id.etxtPass);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateForm()){
                    if (signin()){
                        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                        startActivity(intent);
                    }
                }else {
                    Toast.makeText(LoginActivity.this, "Error", Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onStart(){
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop(){
        super.onStop();
        if (mAuthListener != null){
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    /**
     * Validacion campos
     * @return
     */
    private boolean validateForm() {
        boolean valid = true;
        String email = textUser.getText().toString();
        if (TextUtils.isEmpty(email)) {
            textUser.setError("Campo requerido.");
            valid = false;
        } else {
            if(!email.contains("@") || !email.contains(".") || email.length() < 5){
                valid = false;
                textUser.setError("Formato incorrecto.");
            }else{
                textUser.setError(null);
            }
        }
        String password = textPass.getText().toString();
        if (TextUtils.isEmpty(password)) {
            textPass.setError("Campo requerido.");
            valid = false;
        } else {
            if (password.length()<8){
                textPass.setError("Minimo 8 caracteres.");
                valid = false;
            }else{
                textPass.setError(null);
            }
        }
        return valid;
    } //Fin validateForm

    private boolean signin(){
        final String email = textUser.getText().toString();
        String password = textPass.getText().toString();
        autenticado = false;
        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                // Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());
                if (!task.isSuccessful()){
                    //Log.w(TAG, "signInWithEmail:failed", task.getException());
                    Toast.makeText(LoginActivity.this, "Error en la autenticación "+task.getException(), Toast.LENGTH_SHORT).show();
                    textUser.setText("");
                    textPass.setText("");

                }else {
                    autenticado = true;
                }
            }
        });
        return autenticado;
    }
}
