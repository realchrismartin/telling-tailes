package com.telling.tailes.card;

import com.telling.tailes.model.AuthorProfile;

public class AuthorRviewCard {
    private AuthorProfile author;
    private boolean isFollowed;

    public AuthorRviewCard(AuthorProfile authorProfile) {
        this.author = authorProfile;
    }

    public String getAuthor() {
        return author.getAuthorId();
    }

    public boolean isFollowed() {
        return author.following();
    }

}
