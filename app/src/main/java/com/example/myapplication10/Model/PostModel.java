package com.example.myapplication10.Model;

public class PostModel
{
    private String postId;
    private String postedByUser;
    private String postImage;
    private String postDescription;
    private long postedATime;
    private int postLikes;
    private int postComments;
    public PostModel(){

    }

    public int getPostComments() {
        return postComments;
    }

    public void setPostComments(int postComments) {
        this.postComments = postComments;
    }

    public int getPostLikes() {
        return postLikes;
    }

    public void setPostLikes(int postLikes) {
        this.postLikes = postLikes;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getPostedByUser() {
        return postedByUser;
    }

    public void setPostedByUser(String postedByUser) {
        this.postedByUser = postedByUser;
    }

    public String getPostImage() {
        return postImage;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    public String getPostDescription() {
        return postDescription;
    }

    public void setPostDescription(String postDescription) {
        this.postDescription = postDescription;
    }

    public long getPostedATime() {
        return postedATime;
    }

    public void setPostedATime(long postedATime) {
        this.postedATime = postedATime;
    }
}
