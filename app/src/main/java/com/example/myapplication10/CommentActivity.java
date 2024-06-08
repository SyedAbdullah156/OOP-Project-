package com.example.myapplication10;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myapplication10.Adapter.CommentAdapter;
import com.example.myapplication10.Model.CommentModel;
import com.example.myapplication10.Model.PostModel;
import com.example.myapplication10.Model.UserInfoModel;
import com.example.myapplication10.databinding.ActivityCommentBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

public class CommentActivity extends AppCompatActivity {

    ActivityCommentBinding binding;
    Intent intent;
    String postId;
    String postedByUser;
    FirebaseDatabase database;
    FirebaseAuth auth;
    ArrayList<CommentModel> commentsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize binding
        binding = ActivityCommentBinding.inflate(getLayoutInflater());
        // Set the content view using binding
        setContentView(binding.getRoot());

        // Setting up the toolbar
        setSupportActionBar(binding.toolbar);
        CommentActivity.this.setTitle("Comments");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize Firebase instances
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        commentsList = new ArrayList<>();

        // Get intent extras
        intent = getIntent();
        postId = intent.getStringExtra("postId");
        postedByUser = intent.getStringExtra("postedByUser");

        // Fetching and displaying the postLikeCount, postCommentCount, postImage and postDescription from the database to the commentActivity
        database.getReference()
                .child("posts")
                .child(postId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        PostModel post = snapshot.getValue(PostModel.class);
                        Picasso.get()
                                .load(post.getPostImage())
                                .placeholder(R.drawable.post_placeholder)
                                .into(binding.postImage);
                        binding.postDescription.setText(post.getPostDescription());
                        binding.postLikeCount.setText(String.valueOf(post.getPostLikes()));
                        binding.postCommentCount.setText(String.valueOf(post.getPostComments()));
                        Log.d(TAG, "post comment count in comment activity: " + post.getPostComments());

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle possible errors here
                    }
                });

        // Fetching user data including username, profileImage from the database relative to the post
        database.getReference()
                .child("Users")
                .child(postedByUser).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserInfoModel user = snapshot.getValue(UserInfoModel.class);
                        Picasso.get()
                                .load(user.getProfileImage())
                                .placeholder(R.drawable.default_profile_image)
                                .into(binding.profileImage);
                        binding.username.setText(user.getName());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        // Click listener for commentPostButton
        binding.postCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Setting data for the comment into the CommentModel object
                CommentModel comment = new CommentModel();
                comment.setCommentBody(binding.userComment.getText().toString());
                comment.setCommentAt(new Date().getTime());
                comment.setCommentedByUser(FirebaseAuth.getInstance().getUid());

                // Storing data in database using the CommentModel object
                database.getReference()
                        .child("posts")
                        .child(postId)
                        .child("comments")
                        .push() // make separate node for each comment
                        .setValue(comment).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                // Increment the comments count in the database
                                database.getReference()
                                        .child("posts")
                                        .child(postId)
                                        .child("postComments")
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                int commentCount = snapshot.exists() ? snapshot.getValue(Integer.class) : 0;
                                                commentCount++;

                                                // Update the comment count in the database
                                                database.getReference()
                                                        .child("posts")
                                                        .child(postId)
                                                        .child("postComments")
                                                        .setValue(commentCount)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {
                                                                Toast.makeText(CommentActivity.this, "Comment Added", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                // Handle onCancelled
                                            }
                                        });
                            }
                        });
            }
        });

        CommentAdapter adapter = new CommentAdapter(this, commentsList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.commentsRV.setLayoutManager(layoutManager);
        binding.commentsRV.setAdapter(adapter);

        database.getReference()
                .child("posts")
                .child(postId)
                .child("comments").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        commentsList.clear();
                        for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                            CommentModel comment = dataSnapshot.getValue(CommentModel.class);
                            commentsList.add(comment);
                        }
                        adapter.notifyDataSetChanged();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    // Clicking back on the toolbar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }
}
