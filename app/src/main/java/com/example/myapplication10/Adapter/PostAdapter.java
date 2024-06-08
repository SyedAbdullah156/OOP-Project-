package com.example.myapplication10.Adapter;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;

import com.example.myapplication10.CommentActivity;
import com.example.myapplication10.Model.NotificationModel;
import com.example.myapplication10.Model.PostModel;
import com.example.myapplication10.Model.UserInfoModel;
import com.example.myapplication10.R;
import com.example.myapplication10.databinding.PostRvSampleBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.viewHolder>
{

    ArrayList<PostModel> list;
    Context context;

    public PostAdapter(ArrayList<PostModel> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.post_rv_sample, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position)
    {
        PostModel post = list.get(position);

        /*-------------------------------------------------------------------------------
        Setting postImage, description, postLikes, postComments of the post for currentUser
        --------------------------------------------------------------------------------*/
        Picasso.get()
                .load(post.getPostImage())
                .placeholder(R.drawable.post_placeholder)
                .into(holder.binding.postImage);
        holder.binding.postLike.setText(post.getPostLikes() + "");
        holder.binding.postComment.setText(post.getPostComments() + "");
        Log.d(TAG, "post comment count in post adapter: " + post.getPostComments());
        // If description is available then displaying otherwise not
        if(post.getPostDescription() != null){
            holder.binding.postDescription.setText(post.getPostDescription());
            holder.binding.postDescription.setVisibility(View.VISIBLE);
        } else{
            holder.binding.postDescription.setVisibility(View.GONE);
        }


        /*-----------------------------------------------------------------------
        Setting username, profession and profileImage for currentUser on the post
        ------------------------------------------------------------------------*/
        FirebaseDatabase.getInstance().getReference().child("Users")
                .child(post.getPostedByUser()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserInfoModel user = snapshot.getValue(UserInfoModel.class);
                        Picasso.get()
                                .load(user.getProfileImage())
                                .placeholder((R.drawable.default_profile_image))
                                .into(holder.binding.profileImage);
                        holder.binding.username.setText(user.getName());
                        holder.binding.profession.setText(user.getProfession());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        /*-------------------------------------------------------------------------------------------------------------------------------------------------------
        Incrementing likes for the post of the currentUser has not liked already and if the currentUser has likes already then not incrementing the likes and deleting the like and changing the button to red color and non-red color
        --------------------------------------------------------------------------------------------------------------------------------------------------------*/
        FirebaseDatabase.getInstance().getReference()
                .child("posts")
                .child(post.getPostId())
                .child("likes")
                .child(FirebaseAuth.getInstance().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        /*-------------------------IF SNAPSHOT EXISTS THEN CURRENTUSER HAS ALREADY LIKED THE POST-------------------------------*/
                        if(snapshot.exists()){
                            holder.binding.postLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like, 0, 0, 0);

                            holder.binding.postLike.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // When we click on the like button again, remove the user from likes list and decrement the likes count
                                    FirebaseDatabase.getInstance().getReference()
                                            .child("posts")
                                            .child(post.getPostId())
                                            .child("likes")
                                            .child(FirebaseAuth.getInstance().getUid())
                                            .removeValue()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    FirebaseDatabase.getInstance().getReference()
                                                            .child("posts")
                                                            .child(post.getPostId())
                                                            .child("postLikes")
                                                            .setValue(post.getPostLikes() - 1)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void unused) {
                                                                    holder.binding.postLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_not_like, 0, 0, 0);

                                                                    NotificationModel notification = new NotificationModel();
                                                                    notification.setNotificationBy(FirebaseAuth.getInstance().getUid());
                                                                    notification.setNotificationAt(new Date().getTime());
                                                                    notification.setPostID(post.getPostId());
                                                                    notification.setType("like");

                                                                    // Post like notification added to database
                                                                    FirebaseDatabase.getInstance().getReference()
                                                                            .child("Notification")
                                                                            .child(post.getPostedByUser())
                                                                            .push()
                                                                            .setValue(notification);
                                                                }
                                                            });
                                                }
                                            });
                                }
                            });
                        }

                        /*------------------- NO SNAPSHOT MEANS CURRENTUSER HAS NOT LIKED ALREADY AND NOT ADDING CLICK LISTENER TO IT---------------------------*/
                        else
                        {
                            holder.binding.postLike.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // When we click on the like button the posts-->postId-->Likes-->UserId is stored with value of true
                                    FirebaseDatabase.getInstance().getReference()
                                            .child("posts")
                                            .child(post.getPostId())
                                            .child("likes")
                                            .child(FirebaseAuth.getInstance().getUid())
                                            // After the CurrentUserId is stored in the likes section in the post the postLikes count should be incremented so that code is for that. This code goes like posts-->postId-->postLikes(new child created if the post has not being like by anyone before and previous one used if the postLike is made already)--> and the like is incremented
                                            .setValue(true).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    FirebaseDatabase.getInstance().getReference()
                                                            .child("posts")
                                                            .child(post.getPostId())
                                                            .child("postLikes")
                                                            .setValue(post.getPostLikes() + 1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                // This sets the postLike button to red after liked by currentUser
                                                                @Override
                                                                public void onSuccess(Void unused) {
                                                                    holder.binding.postLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like, 0, 0, 0);
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

        /*-----------------------------------------------------
        Setting Activity Comment when comment button is clicked
        -----------------------------------------------------*/
        holder.binding.postComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, CommentActivity.class);
                intent.putExtra("postId", post.getPostId());
                intent.putExtra("postedByUser", post.getPostedByUser());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder
    {
        PostRvSampleBinding binding;
        public viewHolder(@NonNull View itemView)
        {
            super(itemView);
            binding = PostRvSampleBinding.bind(itemView);

        }
    }

}
