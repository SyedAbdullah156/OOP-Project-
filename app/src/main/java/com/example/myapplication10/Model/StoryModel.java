package com.example.myapplication10.Model;

import java.util.ArrayList;

public class StoryModel
{
    private String storyBy;
    private long storyAt;
    ArrayList<UserStoriesModel> stories;

    public StoryModel(){

    }

    public String getStoryBy() {
        return storyBy;
    }

    public void setStoryBy(String storyBy) {
        this.storyBy = storyBy;
    }

    public long getStoryAt() {
        return storyAt;
    }

    public void setStoryAt(long storyAt) {
        this.storyAt = storyAt;
    }

    public ArrayList<UserStoriesModel> getStories() {
        return stories;
    }

    public void setStories(ArrayList<UserStoriesModel> stories) {
        this.stories = stories;
    }
}
