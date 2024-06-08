package com.example.myapplication10.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication10.Adapter.FollowersAdapter;
import com.example.myapplication10.Model.FollowModel;
import com.example.myapplication10.Model.UserInfoModel;
import com.example.myapplication10.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;

public class ProfileFragment extends Fragment {
    // Recycler View things
    RecyclerView followersRecyclerView;
    ArrayList<FollowModel> followersOfCurrentUserList;

    // Firebase things
    FirebaseAuth auth;
    FirebaseStorage storage;
    FirebaseDatabase database;

    // Making objects of all views in fragment_profile layout
    ImageView profile_image;
    TextView profession;
    TextView username;
    TextView followersCount;
    TextView followingCount; // Add this line

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize the corresponding objects to their XML code
        profile_image = view.findViewById(R.id.profile_image);
        profession = view.findViewById(R.id.profession);
        username = view.findViewById(R.id.username);
        followersCount = view.findViewById(R.id.followersCount);
        followingCount = view.findViewById(R.id.followingCount); // Add this line

        // Displaying the profileImage, username, profession, followerCount, and followingCount for currentUser for only single time
        database.getReference().child("Users").child(auth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    UserInfoModel user = snapshot.getValue(UserInfoModel.class);
                    Picasso.get()
                            .load(user.getProfileImage())
                            .placeholder(R.drawable.default_profile_image)
                            .into(profile_image);
                    username.setText(user.getName());
                    profession.setText(user.getProfession());
                    followersCount.setText(user.getFollowerCount() + "");

                    // Assuming you have a `followingCount` field in your UserInfoModel
                    followingCount.setText(user.getFollowingCount() + ""); // Add this line
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // Displaying the profileImage, username and profession, followerCount, for currentUser for only single time
        database.getReference().child("Users").child(auth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    UserInfoModel user = snapshot.getValue(UserInfoModel.class);
                    Picasso.get()
                            .load(user.getProfileImage())
                            .placeholder(R.drawable.default_profile_image)
                            .into(profile_image);
                    username.setText(user.getName());
                    profession.setText(user.getProfession());
                    followersCount.setText(user.getFollowerCount() + "");

                    followingCount.setText(user.getFollowingCount() + ""); // Add this line
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // Initializing recyclerView and List for showing followers at the end of the profile for the currentUser
        followersRecyclerView = view.findViewById(R.id.followersRV);
        followersOfCurrentUserList = new ArrayList<>();

        // Making FollowerAdapter for displaying the images of the followers of the currentUser
        FollowersAdapter adapter = new FollowersAdapter(followersOfCurrentUserList, getContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        followersRecyclerView.setLayoutManager(linearLayoutManager);
        followersRecyclerView.setAdapter(adapter);

        // Creating a list of followers that follow the current user and again notifying the adapter to see if the followersOfCurrentUserList content is changed means a follower is added then again add the profileImage of that follower to the currentUser's list at bottom
        // Current User --> followers --> Collect each FollowModel Objects that are actually followers of the current user --> Pass this list to the FollowersAdapter to render all the profileImages of the users
        database.getReference().child("Users")
                .child(auth.getUid())
                .child("followers").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        followersOfCurrentUserList.clear(); // Other wise if not used the list does not append new user but makes copy of previous ones and then add new user
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                            // The data inside the followers-->UserId is of FollowModel class
                            FollowModel followerOfCurrentUser = dataSnapshot.getValue(FollowModel.class);
                            followersOfCurrentUserList.add(followerOfCurrentUser);
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        // Opening Gallery when profileImage is clicked
        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 11);
            }
        });

        return view;
    }

    // Getting the image from the gallery
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri uri = data.getData();
                profile_image.setImageURI(uri);

                // Storing Image URI to the database
                final StorageReference reference = storage.getReference().child("profileImages").child(FirebaseAuth.getInstance().getUid());
                reference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        if (isAdded()) {  // Check if the fragment is attached
                            Toast.makeText(getContext(), "Cover Photo Saved", Toast.LENGTH_SHORT).show();
                        }

                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri downloadUri) {
                                database.getReference().child("Users").child(auth.getUid()).child("profileImage").setValue(downloadUri.toString());
                            }
                        }).addOnFailureListener(e -> {
                            Log.e("ProfileFragment", "Failed to get download URL", e);
                        });
                    }
                }).addOnFailureListener(e -> {
                    Log.e("ProfileFragment", "Failed to upload image", e);
                });
            } else {
                // Handle the case where the intent data or URI is null
                Log.e("ProfileFragment", "Intent data or URI is null");
            }
        } else {
            // Handle the case where the result is not RESULT_OK
            Log.e("ProfileFragment", "Result code is not OK: " + resultCode);
        }
    }

}
