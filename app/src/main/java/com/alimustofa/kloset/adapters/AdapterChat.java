package com.alimustofa.kloset.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alimustofa.kloset.R;
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

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterChat extends RecyclerView.Adapter <AdapterChat.MyHolder> {

    private  static  final int MSG_TYPE_lEFT = 0;
    private static final int MSG_TYPE_RIGHT= 1;
    Context   context;
    List<ModelChat> chatList;
    String imageUrl;

    FirebaseUser firebaseUser;

    public AdapterChat(Context context, List<ModelChat> chatList, String imageUrl) {
        this.context = context;
        this.chatList = chatList;
        this.imageUrl = imageUrl;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layput
        if (viewType== MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_right, parent, false);
            return new MyHolder(view);
        }else {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_left, parent, false);
            return new MyHolder(view);

        }

    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, final int position) {
        //get data
        String message = chatList.get(position).getMessage();
        String timeStamp = chatList.get(position).getTimestamp();

        //convert time stamp to dd/mm/yyyy hh:mm am.pm
        Calendar calender = Calendar.getInstance(Locale.ENGLISH);
        calender.setTimeInMillis(Long.parseLong(timeStamp));
        String dateTime = DateFormat.format("dd/MM/yyyy hh:mm",calender).toString();

        //set daya
        holder.messegeTv.setText(message);
        holder.timeTv.setText(dateTime);
        try{
            Picasso.get().load(imageUrl).into(holder.profileIv);

        }catch (Exception e){

        }

        holder.messageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show konfrim delete message
                AlertDialog.Builder builder= new AlertDialog.Builder(context);
                builder.setTitle("Delete");
                builder.setMessage("Are you sure to delete this message");
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteMessage(position);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                //create new dialog
                builder.show();
            }
        });

        //set send or delivered status
        if (position==chatList.size()-1){
            if (chatList.get(position).isSend()){
                holder.isSendTv.setText("send");
            }else{
                holder.isSendTv.setText("delivered");
            }
        }else{
            holder.isSendTv.setVisibility(View.GONE);
        }

    }

    private void deleteMessage(int position) {
        final String getUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        String messegeTS = chatList.get(position).getTimestamp();
        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("Chats");
        Query query = databaseReference.orderByChild("timestamp").equalTo(messegeTS);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){

                        if (ds.child("sender").getValue().equals(getUid)){
                            ds.getRef().removeValue();
                            //HashMap<String, Object> hashMap = new HashMap<>();
                            //hashMap.put("message", "This message was deletd");
                            //ds.getRef().updateChildren(hashMap);
                            Toast.makeText(context, "message deleted...", Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(context, "You can delete only your message...", Toast.LENGTH_SHORT).show();
                        }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        //get cuttenty
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (chatList.get(position).getSender().equals(firebaseUser.getUid())){
            return MSG_TYPE_RIGHT;
        }else  {
            return  MSG_TYPE_lEFT;
        }

    }

    //view holde class

    class  MyHolder extends RecyclerView.ViewHolder {


        //views
        ImageView profileIv;
        TextView messegeTv, timeTv, isSendTv;
        LinearLayout messageLayout; // for click listener to show delete message

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //init view
            profileIv = itemView.findViewById(R.id.profileIv);
            messegeTv = itemView.findViewById(R.id.messageTv);
            timeTv = itemView.findViewById(R.id.timeTv);
            isSendTv = itemView.findViewById(R.id.issendTv);
            messageLayout=itemView.findViewById(R.id.messangeLayout);


        }
    }
}
