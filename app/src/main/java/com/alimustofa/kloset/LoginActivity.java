package com.alimustofa.kloset;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {


    private static final int RC_SIGN_IN = 100;
    GoogleSignInClient mGoogleSignInClient;
    EditText mEmailEt, mPasswordET;
    TextView mdontthave_accountTV, mrecoverPassTV;
    Button mLoginBtn;
    SignInButton mgoogleLoginBtn;

    //Declare an instance of FirebaseAuth
    private FirebaseAuth mAuth;

    //progers dialog
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Actionbar and its title
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Login");

        //enable button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        mdontthave_accountTV = findViewById(R.id.dontthave_accountTV);
        mEmailEt= findViewById(R.id.emailEt);
        mPasswordET = findViewById(R.id.passwordEt);
        mLoginBtn = findViewById(R.id.loginBtn);
        mgoogleLoginBtn = findViewById(R.id.googleLoginBtn);
        mrecoverPassTV = findViewById(R.id.recoverPassTV);


        //login Btn Click
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //input data

                String email = mEmailEt.getText().toString();
                String pass = mPasswordET.getText().toString().trim();
                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    //infailed email patterns set error
                    mEmailEt.setError("Invalid Emial");
                    mEmailEt.setFocusable(true);
                }else {
                    //valid email patterns
                    loginUser(email, pass);
                }
            }
        });


        //handle donthave account
        mdontthave_accountTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                finish();
                }
        });

        //handle forget password and view dialog
        mrecoverPassTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRecoverDialog();
            }
        });

        //handel button Google sign
        mgoogleLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //begin google login process
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

    }

    private void showRecoverDialog() {
        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Riset Password");

        //set layout linear layout
        LinearLayout linearLayout = new LinearLayout(this);

        //view to set in dialog
        final EditText emailEt = new EditText(this);
        emailEt.setHint("Email");
        emailEt.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailEt.setMinEms(14);

        linearLayout.addView(emailEt);
        linearLayout.setPadding(50, 20, 20, 20);

        builder.setView(linearLayout);

        //botton recovery
        builder.setPositiveButton("Reset", new DialogInterface.OnClickListener(){


            @Override
            public void onClick(DialogInterface dialog, int which) {
                //inpu email
                String email = emailEt.getText().toString().trim();
                beginRecovery(email);
            }
        });

        //button cancel
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){


            @Override
            public void onClick(DialogInterface dialog, int which) {
                //dismis dialog
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    private void beginRecovery(String email) {
        //init proggres dialog
        pd = new ProgressDialog(this);
        pd.setMessage("Sending...");
        pd.show();
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(!task.isSuccessful()){
                    pd.dismiss();
                    Toast.makeText(LoginActivity.this, "Email Send Check Spam Folder", Toast.LENGTH_SHORT).show();
                }else{
                    pd.dismiss();
                    Toast.makeText(LoginActivity.this, "Email Failed", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                //get and show proper error messege
                Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loginUser(String email, String pass) {
        //init proggres dialog
        pd = new ProgressDialog(this);
        pd.setMessage("Login In...");
        //show progress
        pd.show();
        mAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //dismis progress dialog
                            pd.dismiss();
                            // Sign in success, update UI with the signed-in user's information

                            FirebaseUser user = mAuth.getCurrentUser();

                            //user is loggined
                            startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                            finish();
                        } else {
                            //dismis progress dialog
                            pd.dismiss();
                            // If sign in fails, display a message to the user.

                            Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //dismis progress dialog
                pd.dismiss();
                //error get and show
                Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public boolean onSupportNavigateUp(){
        onBackPressed();//go previous activity
        return super.onSupportNavigateUp();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();

                            //if user is signing in frist time then get and show user info from google account
                            if (task.getResult().getAdditionalUserInfo().isNewUser()){
                                //Get user email and uid from auth
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
                                hasMap.put("cover","");

                                //firebases databases instance
                                FirebaseDatabase database = FirebaseDatabase.getInstance();

                                //path to store user named
                                DatabaseReference reference = database.getReference("Users");

                                //put data with in hasmap in databases
                                reference.child(uid).setValue(hasMap);
                            }



                            //show user email in toast
                            Toast.makeText(LoginActivity.this, ""+user.getEmail(), Toast.LENGTH_SHORT).show();

                            //goto profile activity
                            startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                            finish();
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "Login Failed...", Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }


                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //get and show error messege
                Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
