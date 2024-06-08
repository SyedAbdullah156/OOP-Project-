package com.example.myapplication10.Model;

public class FollowModel
{
    private String followedBy;
    private long followedAtTime;

    public FollowModel(){

    }

    public String getFollowedBy() {
        return followedBy;
    }

    public void setFollowedBy(String followedBy) {
        this.followedBy = followedBy;
    }

    public long getFollowedAtTime() {
        return followedAtTime;
    }

    public void setFollowedAtTime(long followedAtTime) {
        this.followedAtTime = followedAtTime;
    }
}
