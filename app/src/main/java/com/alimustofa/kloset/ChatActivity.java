package com.alimustofa.kloset;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class ChatActivity extends AppCompatActivity {

    //view from xml
    Toolbar toolbar;
    RecyclerView recyclerView;
    ImageView profileIv;
    TextView nameTv, userStatusTv;
    EditText massageEt;
    ImageButton sendBtn;

    FirebaseAuth firebaseAuth;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //init views
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");


        recyclerView=findViewById(R.id.chat_reyclerView);
        profileIv = findViewById(R.id.profileIv);
        nameTv = findViewById(R.id.nameTv);
        userStatusTv = findViewById(R.id.userStatusTv);
        massageEt = findViewById(R.id.massageEt);
        sendBtn = findViewById(R.id.sendBtn);

        //firebases auth instance
        firebaseAuth = FirebaseAuth.getInstance();

    }
    private void checkUserStatus(){
        //get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user !=null){
            //user sign and stay here

            //set email of loggged in user

        }else{
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_main, menu);

        //hiden search view as we dont need it here
        menu.findItem(R.id.action_serach).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id==R.id.action_logout){
            firebaseAuth.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);

    }
}
