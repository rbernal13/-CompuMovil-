package com.javeriana.ricardo.tallerfirebase;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {
    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin, buttonSignup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextEmail = (EditText) findViewById(R.id.eTxtEmail);
        editTextPassword = (EditText) findViewById(R.id.eTxtPassword);
        buttonLogin = (Button) findViewById(R.id.btnLogin);
        buttonSignup = (Button) findViewById(R.id.btnSignup);


        if(validateForm()) {
            Intent intent = new Intent(this, HomeActivity.class);
        }else{
            Toast.makeText(this, "Fallo!", Toast.LENGTH_SHORT).show();
        }
    }

    private	boolean validateForm()	{
        boolean valid	=	true;
        String	email	=	editTextEmail.getText().toString();
        if	(TextUtils.isEmpty(email))	{
            editTextEmail.setError("Required.");
            valid	=	false;
        }	else	{
            editTextEmail.setError(null);
        }
        String	password	=	editTextPassword.getText().toString();
        if	(TextUtils.isEmpty(password))	 {
            editTextPassword.setError("Required.");
            valid	=	false;
        }	else	{
            editTextPassword.setError(null);
        }
        return	valid;
    }


    /*
    protected void signInUser(){
        if(validateForm()){
            String email = editTextEmail.getText().toString();
            String password = editTextPassword.getText().toString();
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());
                            if (!task.isSuccessful()) {
                                Log.w(TAG, "signInWithEmail:failed", task.getException());
                                Toast.makeText(LoginActivity.this, R.string.auth_failed,
                                        Toast.LENGTH_SHORT).show();
                                editTextEmail.setText("");
                                editTextPassword.setText("");
                            }
                        }
                    });
        }
    }
    */


}


