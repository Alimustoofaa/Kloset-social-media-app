package com.alimustofa.kloset;


import androidx.appcompat.app.ActionBar;
import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {
    //view
    EditText mEmailEt, mPasswordEt;
    Button mRegisterBtn;
    TextView mhave_accountTV;


    ProgressDialog progressDialog;

    //Declare an instance of FirebaseAuth
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Actionbar and its title
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Create Account");

        //enable button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        //init
        mEmailEt = findViewById(R.id.emailEt);
        mPasswordEt = findViewById(R.id.passwordEt);
        mRegisterBtn = findViewById(R.id.registerBtn);
        mhave_accountTV = findViewById(R.id.have_accountTV);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering user ...");

        //handle Register btn click
        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //input email, password
                String email = mEmailEt.getText().toString().trim();
                String password = mPasswordEt.getText().toString().trim();

                //validate
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    //set error and focus to email edittext
                    mEmailEt.setError("Email Salah");
                    mEmailEt.setFocusable(true);
                }else if(password.length()<6){

                    //set error and focus to password edit text
                    mPasswordEt.setError("Password Harus Lebih Dari 6 Karakter");
                    mPasswordEt.setFocusable(true);
                }else {
                    registerUser(email, password);
                }
            }

            private void registerUser(String email, String password) {

                //email and password pattern is valid, show proggres dialog and
                progressDialog.show();

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    progressDialog.dismiss();
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    //Gey user email and uid from auth
                                    String email = user.getEmail();
                                    String uid = user.getUid();

                                    //when user is registered store user info in firebases realtime databases to
                                    // using HasMap
                                    HashMap<Object, String> hasMap = new HashMap<>();

                                    //put indo in hashmap
                                    hasMap.put("email",email);
                                    hasMap.put("uid",uid);
                                    hasMap.put("name",""); //will add later edit profile
                                    hasMap.put("onlineStatus","online"); //will add later edit profile
                                    hasMap.put("typingTo","noOne"); //will add later edit profile
                                    hasMap.put("phone","");//will add later edit profile
                                    hasMap.put("image","");//will add later edit profile
                                    hasMap.put("cover","");//will add later edit profile

                                    //firebases databases instance
                                    FirebaseDatabase database = FirebaseDatabase.getInstance();

                                    //path to store user named
                                    DatabaseReference reference = database.getReference("Users");

                                    //put data with in hasmap in databases
                                    reference.child(uid).setValue(hasMap);

                                    Toast.makeText(RegisterActivity.this, "Register..\n"+user.getEmail()+"\nSucces",Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                    finish();
                                } else {
                                    // If sign in fails, display a message to the user.
                                    progressDialog.dismiss();
                                    Toast.makeText(RegisterActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();

                                }

                                // ...
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //eror dismis progresa
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, ""+e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        //handle click have account
        mhave_accountTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class) );
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp(){
        onBackPressed();//go previous activity
        return super.onSupportNavigateUp();
    }
}
