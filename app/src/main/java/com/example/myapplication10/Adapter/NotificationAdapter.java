package com.example.myapplication10.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication10.Model.NotificationModel;
import com.example.myapplication10.Model.UserInfoModel;
import com.example.myapplication10.R;
import com.example.myapplication10.databinding.NotificationRvSampleBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.viewHolder>{
    ArrayList<NotificationModel> notificationList;
    Context notificationContext;

    public NotificationAdapter(ArrayList<NotificationModel> list, Context context) {
        this.notificationList = list;
        this.notificationContext = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(notificationContext).inflate(R.layout.notification_rv_sample, parent, false);

        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        NotificationModel notification = notificationList.get(position);

        String type = notification.getType();

        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(notification.getNotificationBy())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserInfoModel user = snapshot.getValue(UserInfoModel.class);
                        Picasso.get()
                                .load(user.getProfileImage())
                                .placeholder(R.drawable.default_profile_image)
                                .into(holder.binding.profileImage);
                        if(type.equals("like")){
                            holder.binding.notificationMessage.setText(user.getName() + " Liked on your post");
                        }else{
                            holder.binding.notificationMessage.setText(user.getName() + " Started Following you");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder{
        NotificationRvSampleBinding binding;
        public viewHolder(@NonNull View itemView){
            super(itemView);
            binding = NotificationRvSampleBinding.bind(itemView);
        }
    }
}

