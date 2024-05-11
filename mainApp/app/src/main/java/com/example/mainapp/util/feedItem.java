package com.example.mainapp.util;
import com.example.mainapp.MainActivity;
public class feedItem {
    private long postID;
    private String profileImage;
    private String profileUser;
    private String ImageUrl;
    private String favoriteCounterText;
    private String explainText;
    private String bestComment;

    private int likes;


    public feedItem() {
        this.postID=0;
        this.profileUser = "test";
        this.favoriteCounterText = "test";
        this.explainText = "test";
        this.bestComment = "test";
        this.likes=0;
        this.profileImage="https://search.pstatic.net/sunny/?src=https%3A%2F%2Fpng.pngtree.com%2Fpng-clipart%2F20221102%2Fourlarge%2Fpngtree-wedding-ornaments-png-image_6406490.png&type=sc960_832";
        this.ImageUrl="https://search.pstatic.net/sunny/?src=https%3A%2F%2Fpng.pngtree.com%2Fpng-clipart%2F20221102%2Fourlarge%2Fpngtree-wedding-ornaments-png-image_6406490.png&type=sc960_832";
    }//나중에 등록할거url 바꿀거임

    public void setPostID(long id){
        postID=id;
    }

    public long getPostID() {
        return postID;
    }

    public String getProfileUser() {
        return profileUser;
    }

    public void setProfileUser(String profileText) {
        this.profileUser = profileText;
    }

    public String  getImage() {
        return ImageUrl;
    }

    public void setImage(String image) {
        this.ImageUrl = image;
    }


    public String getProfileImageUrl() {
        return this.profileImage;
    }

    public void setProfileImageUrl(String profileImageurl) {
        this.profileImage = profileImageurl;
    }

    public String getFavoriteCounterText() {
        return favoriteCounterText;
    }

    public void setFavoriteCounterText(String favoriteCounterText) {
        this.favoriteCounterText = favoriteCounterText;
    }

    public String getExplainText() {
        return explainText;
    }

    public void setExplainText(String explainText) {
        this.explainText = explainText;
    }

    public String getBestComment() {
        return bestComment;
    }

    public void setBestComment(String bestComment) {
        this.bestComment = bestComment;
    }

    public void setLikes(int likes){
        this.likes=likes;
    }
    public int getLikes(){
        return this.likes;
    }
}
