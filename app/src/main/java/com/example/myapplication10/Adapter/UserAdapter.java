package com.example.myapplication10.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication10.Model.FollowModel;
import com.example.myapplication10.Model.NotificationModel;
import com.example.myapplication10.Model.UserInfoModel;
import com.example.myapplication10.R;
import com.example.myapplication10.databinding.UserRvSampleBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

/*
    This adapter is used in the Search Fragment to display all the users and also give option to the
    current user to follow other users
 */
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.viewHolder>
{
    Context context;
    ArrayList<UserInfoModel> allUsersList;

    public UserAdapter(Context context, ArrayList<UserInfoModel> list) {
        this.context = context;
        this.allUsersList = list;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { // This method takes the user_rv_sample that is the single layout for one item in the recycler view and this single layout is the stored in the viewHolder class that then use to binding the data to the individual items in the recycler vie. Each viewHolder holds one item or one rv_sample containing data
        View view = LayoutInflater.from(context).inflate(R.layout.user_rv_sample, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        // Get each user info one by one from the list and set it on the user_rv_sample fo reach user on teh search fragment
        UserInfoModel user = allUsersList.get(position);
        Picasso.get()
                .load(user.getProfileImage())
                .placeholder(R.drawable.default_profile_image)
                .into(holder.binding.profileImage);
        holder.binding.name.setText(user.getName());
        holder.binding.profession.setText(user.getProfession());

        // This set the followButton either enabled or disabled for the already followed users by the current user
        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(user.getUserId())
                .child("followers")
                .child(FirebaseAuth.getInstance().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // This check of the user is already being followed by the currentUser and then set the follow button for that user to non editable
                        if (snapshot.exists()){
                            holder.binding.followButton.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.button_disabled));
                            holder.binding.followButton.setText("Following");
                            holder.binding.followButton.setTextColor(context.getResources().getColor(R.color.gray));
                            holder.binding.followButton.setEnabled(false);
                        }
                        else {
                            // This code sets up a click listener if that user is not being followed by the current user for a follow button inside the viewHolders containing the follower_rv_sample, creates a new FollowModel object with the current user's ID and the current timestamp, and then updates the Firebase Realtime Database to add the follower to the user's followers list(this is the user that is being clicked by the current user) and increments the follower count of the user being followed by the current user. And on the success of the user added the user's follower count is updated
                            holder.binding.followButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    FollowModel follow = new FollowModel();
                                    follow.setFollowedBy(FirebaseAuth.getInstance().getUid());
                                    follow.setFollowedAtTime(new Date().getTime());

                                    FirebaseDatabase.getInstance().getReference()
                                            .child("Users")
                                            .child(user.getUserId()) // this user is the same user that is being followed by the currentUser when the currentUser clicks the follow button
                                            .child("followers")
                                            .child(FirebaseAuth.getInstance().getUid()) // This creates a child in the followers of the user being followed by the currentUser and then add the id of the currentUser to the user being followed by the currentUser
                                            .setValue(follow).addOnSuccessListener(new OnSuccessListener<Void>() { // In that particular is it sets the values for time and id by using FollowModel class. After it is done and successfully completed the follower count is incremented
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    FirebaseDatabase.getInstance().getReference()
                                                            .child("Users")
                                                            .child(user.getUserId())
                                                            .child("followerCount")
                                                            .setValue(user.getFollowerCount() + 1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void unused) {
                                                                    // This make the follow button to become diaabled and changed it color and background when disabled
                                                                    holder.binding.followButton.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.button_disabled));
                                                                    holder.binding.followButton.setText("Following");
                                                                    holder.binding.followButton.setTextColor(context.getResources().getColor(R.color.gray));
                                                                    holder.binding.followButton.setEnabled(false);
                                                                    Toast.makeText(context, "You Followed " + user.getName(), Toast.LENGTH_SHORT).show();

                                                                    NotificationModel notification = new NotificationModel();
                                                                    notification.setNotificationBy(FirebaseAuth.getInstance().getUid());
                                                                    notification.setNotificationAt(new Date().getTime());
                                                                    notification.setType("follow");

                                                                    // Adding  notification when user is being followed
                                                                    FirebaseDatabase.getInstance().getReference()
                                                                            .child("Notification")
                                                                            .child(user.getUserId())
                                                                            .push()
                                                                            .setValue(notification);
                                                                }
                                                            });
                                                }
                                            });
                                }
                            });
                        }

                        }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return allUsersList.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder{
        UserRvSampleBinding binding;
        public viewHolder(@NonNull View itemView) {
            super(itemView);
            binding = UserRvSampleBinding.bind(itemView);
        }
    }
}
