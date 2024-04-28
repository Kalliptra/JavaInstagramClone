package com.sametsoydan.javainstagramclone.models;

public class Post {
    String email;
    String comment;
    String downloadURL;

    public Post(String email, String comment, String downloadURL) {
        this.email = email;
        this.comment = comment;
        this.downloadURL = downloadURL;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDownloadURL() {
        return downloadURL;
    }

    public void setDownloadURL(String downloadURL) {
        this.downloadURL = downloadURL;
    }
}
