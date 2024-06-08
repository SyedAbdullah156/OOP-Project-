package com.example.myapplication10.Fragment;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication10.Adapter.PostAdapter;
import com.example.myapplication10.Adapter.StoryAdapter;
import com.example.myapplication10.Model.PostModel;
import com.example.myapplication10.Model.StoryModel;
import com.example.myapplication10.Model.UserStoriesModel;
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
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.Date;

public class HomeFragment extends Fragment {
    RecyclerView storyRv, postRV;
    ArrayList<StoryModel> storyModelList;
    ArrayList<PostModel> postModelList;
    FirebaseDatabase database;
    FirebaseAuth auth;
    FirebaseStorage storage;
    ImageView addStoryImage;
    ActivityResultLauncher<String> galleryLauncher;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initializing instances for database and auth
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        // Initialize Story RecyclerView
        storyRv = view.findViewById(R.id.storyRV);
        storyModelList = new ArrayList<>();

        StoryAdapter storyAdapter = new StoryAdapter(storyModelList, getContext());
        LinearLayoutManager storyLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        storyRv.setLayoutManager(storyLayoutManager);
        storyRv.setAdapter(storyAdapter);

        // Initialize Post RecyclerView
        postRV = view.findViewById(R.id.postRV);
        postModelList = new ArrayList<>();
        PostAdapter postAdapter = new PostAdapter(postModelList, getContext());
        LinearLayoutManager dashboardLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        postRV.setLayoutManager(dashboardLayoutManager);
        postRV.setAdapter(postAdapter);

        // Getting data from database for the posts
        database.getReference().child("posts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postModelList.clear();
               for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    PostModel post = dataSnapshot.getValue(PostModel.class);
                    post.setPostId(dataSnapshot.getKey());
                    postModelList.add(post);
               }
               postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // Getting data from database for the stories
        database.getReference()
                .child("stories").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            storyModelList.clear();
                            for(DataSnapshot storySnapshot : snapshot.getChildren()){
                                StoryModel story  = new StoryModel();
                                story.setStoryBy(storySnapshot.getKey());
                                story.setStoryAt(storySnapshot.child("postedBy").getValue(Long.class));

                                ArrayList<UserStoriesModel> stories = new ArrayList<>();
                                for(DataSnapshot snapshot1 : storySnapshot.child("userStories").getChildren()){
                                    UserStoriesModel userStories = snapshot1.getValue(UserStoriesModel.class);
                                    stories.add(userStories);
                                }
                                story.setStories(stories);
                                storyModelList.add(story);
                            }
                            storyAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        addStoryImage = view.findViewById(R.id.status_story);
        addStoryImage.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                galleryLauncher.launch("image/*");
            }
        });

        galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri uri) {
                if (uri != null) {
                    final StorageReference reference = storage.getReference()
                            .child("stories")
                            .child(FirebaseAuth.getInstance().getUid())
                            .child(new Date().getTime() + "");
                    reference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {

                                @Override
                                public void onSuccess(Uri uri) {
                                    StoryModel story = new StoryModel();
                                    story.setStoryAt(new Date().getTime());

                                    database.getReference()
                                            .child("stories")
                                            .child(FirebaseAuth.getInstance().getUid())
                                            .child("postedBy")
                                            .setValue(story.getStoryAt()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    UserStoriesModel stories = new UserStoriesModel(uri.toString(), story.getStoryAt());

                                                    database.getReference()
                                                            .child("stories")
                                                            .child(FirebaseAuth.getInstance().getUid())
                                                            .child("userStories")
                                                            .push()
                                                            .setValue(stories);
                                                }
                                            });
                                }
                            });
                        }
                    });
                } else {
                    // Handle the case where no image was selected
                    // You could show a toast or log this incident
                }
            }
        });

        return view;
    }
}
