package com.telling.tailes.card;

import com.telling.tailes.model.AuthorProfile;

public class AuthorRviewCard {
    private String authorId;

    public AuthorRviewCard(String author) {
        this.authorId = author;
    }

    public String getAuthor() {
        return authorId;
    }

}
