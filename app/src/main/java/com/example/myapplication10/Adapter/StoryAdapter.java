package com.example.myapplication10.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication10.Model.StoryModel;
import com.example.myapplication10.Model.UserInfoModel;
import com.example.myapplication10.Model.UserStoriesModel;
import com.example.myapplication10.R;
import com.example.myapplication10.databinding.StoryRvDesignBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import omari.hamza.storyview.StoryView;
import omari.hamza.storyview.callback.StoryClickListeners;
import omari.hamza.storyview.model.MyStory;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.ViewHolder> {

    private ArrayList<StoryModel> storyList;
    private Context storyContext;

    public StoryAdapter(ArrayList<StoryModel> list, Context context) {
        this.storyList = list;
        this.storyContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(storyContext).inflate(R.layout.story_rv_design, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StoryModel story = storyList.get(position);

        if (story.getStories() != null && !story.getStories().isEmpty()) {
            UserStoriesModel lastStory = story.getStories().get(story.getStories().size() - 1);
            if (lastStory != null && lastStory.getImage() != null) {
                Picasso.get()
                        .load(lastStory.getImage())
                        .into(holder.binding.statusStory);
            }
            holder.binding.statusCircle.setPortionsCount(story.getStories().size());
        }

        // Fetching user data for story
        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(story.getStoryBy()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserInfoModel user = snapshot.getValue(UserInfoModel.class);
                        if (user != null) {
                            if (user.getProfileImage() != null) {
                                Picasso.get()
                                        .load(user.getProfileImage())
                                        .placeholder(R.drawable.default_profile_image)
                                        .into(holder.binding.profileImage);
                            }
                            holder.binding.username.setText(user.getName());
                        }

                        holder.binding.statusStory.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ArrayList<MyStory> myStories = new ArrayList<>();
                                if (story.getStories() != null) {
                                    for (UserStoriesModel stories : story.getStories()) {
                                        if (stories.getImage() != null) {
                                            myStories.add(new MyStory(stories.getImage()));
                                        }
                                    }
                                }

                                new StoryView.Builder(((AppCompatActivity) storyContext).getSupportFragmentManager())
                                        .setStoriesList(myStories) // Required
                                        .setStoryDuration(5000) // Default is 2000 Millis (2 Seconds)
                                        .setTitleText(user != null ? user.getName() : "") // Default is Hidden
                                        .setSubtitleText("") // Default is Hidden
                                        .setTitleLogoUrl(user != null ? user.getProfileImage() : null) // Default is Hidden
                                        .setStoryClickListeners(new StoryClickListeners() {
                                            @Override
                                            public void onDescriptionClickListener(int position) {
                                                // your action
                                            }

                                            @Override
                                            public void onTitleIconClickListener(int position) {
                                                // your action
                                            }
                                        }) // Optional Listeners
                                        .build() // Must be called before calling show method
                                        .show();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle error
                    }
                });
    }

    @Override
    public int getItemCount() {
        return storyList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        StoryRvDesignBinding binding;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = StoryRvDesignBinding.bind(itemView);
        }
    }
}