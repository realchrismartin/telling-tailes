package com.telling.tailes.card;

import com.telling.tailes.R;
import com.telling.tailes.util.StringUtils;

public class AuthorRviewCard {
    public static final Integer CARD_TYPE_AUTHOR = 0;
    public static final Integer CARD_TYPE_LOADING = 1;
    public static final Integer CARD_TYPE_NO_AUTHORS = 2;
    private Integer cardType;
    private String authorId;

    public AuthorRviewCard(String author) {
        this.authorId = author;
        this.cardType = CARD_TYPE_AUTHOR;
    }

    public AuthorRviewCard(int type) {
        if (type == 1) {
            this.cardType = CARD_TYPE_LOADING;
        }
        this.cardType = CARD_TYPE_NO_AUTHORS;
    }

    public String getAuthor() {
        switch (cardType) {
            case(0):
                return authorId;
            case(1):
                return StringUtils.loadingString;
            case(2):
                return StringUtils.noAuthorsString;
            default:
                return StringUtils.errorString;
        }
    }

    public Integer getCardType() {
        return cardType;
    }

}
