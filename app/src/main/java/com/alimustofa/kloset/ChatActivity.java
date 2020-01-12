package com.alimustofa.kloset;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.EditText;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alimustofa.kloset.adapters.AdapterChat;
import com.alimustofa.kloset.models.ModelChat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class ChatActivity extends AppCompatActivity {

    //view from xml
    Toolbar toolbar;
    RecyclerView recyclerView;
    ImageView profileIv;
    TextView nameTv, userStatusTv;
    EditText massageEt;
    ImageButton sendBtn;

    FirebaseAuth firebaseAuth;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databasesUser;

    //for checking if use has send message or not
    ValueEventListener sendListerner;
    DatabaseReference userReferenceSend;

    List<ModelChat> chatList;
    AdapterChat adapterChat;


    String hisUid;
    String myUid;
    String hisImage;

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

        //layout (Linear Layout ) for RecyclerView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);

        //recyclerview properties
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        Intent intent= getIntent();
        hisUid = intent.getStringExtra("hisUID");

        //firebases auth instance
        firebaseAuth = FirebaseAuth.getInstance();

        firebaseDatabase = FirebaseDatabase.getInstance();
        databasesUser = firebaseDatabase.getReference("Users");

        //search user to get that user info
        Query userQuery = databasesUser.orderByChild("uid").equalTo(hisUid);

        //get user picture and name
        userQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //check until required info is receive
                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    //get data
                    String name = ""+ds.child("name").getValue();
                    hisImage =""+ ds.child("image").getValue();
                    String typingStatus = ""+ds.child("typingTo").getValue();

                    //check tyiping status
                    if (typingStatus.equals(myUid)){
                        userStatusTv.setText("tyiping...");
                    }else {
                        //get value of online status
                        String onlineStatus = ""+ds.child("onlineStatus").getValue();
                        if (onlineStatus.equals("online")){
                            userStatusTv.setText(onlineStatus);
                        }else{
                            //convert time stamp to dd/mm/yyyy hh:mm am.pm
                            Calendar calender = Calendar.getInstance(Locale.ENGLISH);
                            calender.setTimeInMillis(Long.parseLong(onlineStatus));
                            String dateTime = DateFormat.format("dd/MM/yyyy hh:mm",calender).toString();
                            userStatusTv.setText("Last seen at :"+dateTime);
                        }

                    }

                    //set data
                    nameTv.setText(name);

                    try{
                        Picasso.get().load(hisImage).into(profileIv);
                    }catch (Exception e){
                        Picasso.get().load(R.drawable.ic_dafault_img_white).into(profileIv);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get text from edit text
                String message = massageEt.getText().toString().trim();

                //check if text is empty or not
                if (TextUtils.isEmpty(message)){
                    //text empty
                    Toast.makeText(ChatActivity.this, "Cannot send the empty message..", Toast.LENGTH_SHORT).show();
                }else {
                    //text not empty
                    sendMessage(message);

                }
            }
        });

        //check edit text change listerner
        massageEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length()==0){
                    checkoTypingtatus("noOne");
                }else{
                    checkoTypingtatus(hisUid);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        readMessage();

        senddMessage();

    }

    private void senddMessage() {
        userReferenceSend = FirebaseDatabase.getInstance().getReference("Chats");
        sendListerner = userReferenceSend.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds :dataSnapshot.getChildren()){
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if (chat.getReceiver().equals(myUid)&& chat.getSender().equals(hisUid)){
                        HashMap<String, Object> hasSendHasMap = new HashMap<>();
                        hasSendHasMap.put("is Send", true);
                        ds.getRef().updateChildren(hasSendHasMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readMessage() {
        chatList = new ArrayList<>();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Chats");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelChat modelChat = ds.getValue(ModelChat.class);
                    if (modelChat.getReceiver().equals(myUid)&& modelChat.getSender().equals(hisUid)||
                        modelChat.getReceiver().equals(hisUid) && modelChat.getSender().equals(myUid)){
                        chatList.add(modelChat);
                    }
                    //adapter
                    adapterChat = new AdapterChat(ChatActivity.this, chatList, hisImage );
                    adapterChat.notifyDataSetChanged();

                    //set adapter to recyelerview
                    recyclerView.setAdapter(adapterChat);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage(String message) {

        DatabaseReference databaseReference =FirebaseDatabase.getInstance().getReference();

        String timestamp = String.valueOf(System.currentTimeMillis());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", myUid);
        hashMap.put("receiver", hisUid);
        hashMap.put("message", message);
        hashMap.put("timestamp", timestamp);
        hashMap.put("isSend", false);
        databaseReference.child("Chats").push().setValue(hashMap);

        //reset edit text after sending message
        massageEt.setText("");
    }

    private void checkUserStatus(){
        //get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user !=null){
            //user sign and stay here

            //set email of loggged in user
            myUid =user.getUid();//currently dignrf in user uif
        }else{
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    private void checkonlinestatus(String status){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String, Object> hashMap= new HashMap<>();
        hashMap.put("onlineStatus", status);


        databaseReference.updateChildren(hashMap);
    }

    private void checkoTypingtatus(String typing){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String, Object> hashMap= new HashMap<>();
        hashMap.put("typingTo", typing);


        databaseReference.updateChildren(hashMap);
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        //set online
        checkonlinestatus("online");
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
    //get time
        String timestamp = String.valueOf(System.currentTimeMillis());

        //set ofline with last seen time stamp
        checkonlinestatus(timestamp);
        checkoTypingtatus("noOne");
        userReferenceSend.removeEventListener(sendListerner);
    }

    @Override
    protected void onResume() {
        //set online
        checkonlinestatus("online");
        super.onResume();
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
