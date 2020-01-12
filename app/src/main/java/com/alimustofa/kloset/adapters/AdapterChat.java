package com.alimustofa.kloset.adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alimustofa.kloset.R;
import com.alimustofa.kloset.models.ModelChat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.squareup.picasso.Picasso;

import java.util.Calendar;
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
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
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

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //init view
            profileIv = itemView.findViewById(R.id.profileIv);
            messegeTv = itemView.findViewById(R.id.messageTv);
            timeTv = itemView.findViewById(R.id.timeTv);
            isSendTv = itemView.findViewById(R.id.issendTv);



        }
    }
}
