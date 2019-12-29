package com.alimustofa.kloset;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    //views
    Button mBtnRegister, mBrnLogin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //init views
        mBtnRegister = findViewById(R.id.btn_register);
        mBrnLogin = findViewById(R.id.btn_login);

        //handle register button click
        mBtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start registerAcivity
                startActivity(new Intent(MainActivity.this, RegisterActivity.class));
            }
        });

        //handle login button
        mBrnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start Login Activity
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });
    }
}
