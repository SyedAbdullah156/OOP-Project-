package com.example.myapplication10.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication10.Model.FollowModel;
import com.example.myapplication10.Model.UserInfoModel;
import com.example.myapplication10.R;
import com.example.myapplication10.databinding.FollowerRvSampleBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/*
    This is used to display the profileImage of the followers of the current user in the profile fragment
 */
public class FollowersAdapter extends RecyclerView.Adapter<FollowersAdapter.viewHolder>
{
    ArrayList<FollowModel> list;
    Context context;

    public FollowersAdapter(ArrayList<FollowModel> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.follower_rv_sample, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        // This give one of the follower of the current user
        FollowModel followModel = list.get(position);

        // This is used to
        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(followModel.getFollowedBy()).addListenerForSingleValueEvent(new ValueEventListener() { // followModel represents the object of the follower user within the currentUser and followedBy returns the id of the User that is following the currentUser means the currentUser is followedBy another user whose id is stored as CurrentUser --> followers --> UserID --> followedBy --> UserID
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserInfoModel user = snapshot.getValue(UserInfoModel.class);
                        // Setting up image and name for the follower
                        Picasso.get()
                                .load(user.getProfileImage())
                                .placeholder(R.drawable.default_profile_image)
                                .into(holder.binding.profileImage);
                        holder.binding.username.setText(user.getName());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder
    {
        FollowerRvSampleBinding binding;
        public viewHolder(@NonNull View itemView)
        {
            super(itemView);
            binding = FollowerRvSampleBinding.bind(itemView);
        }
    }
}
