package com.example.myapplication10.Model;

public class UserInfoModel
{
    private String profileImage;
    private String name;
    private String profession;
    private String email;
    private String password;
    private String userId;
    private int followerCount;
    private int followingCount;

    public UserInfoModel(){

    }
    public UserInfoModel(String name, String profession, String email, String password ) {
        this.name = name;
        this.profession = profession;
        this.email = email;
        this.password = password;
    }

    public int getFollowerCount() {
        return followerCount;
    }

    public void setFollowerCount(int followerCount) {
        this.followerCount = followerCount;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    public int getFollowingCount() { // Add this method
        return followingCount;
    }

    public void setFollowingCount(int followingCount) { // Add this method
        this.followingCount = followingCount;
    }
}
