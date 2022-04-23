package com.telling.tailes.util;

import android.content.Context;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.telling.tailes.model.Story;

import java.util.ArrayList;

public enum FilterType {

    MY,
    BOOKMARKS,
    DRAFTS,
    AUTHOR,
    FOLLOWING,
    POPULAR,
    NONE;

    private String authorUsernameFilter = ""; //Only set if filter is for specific author
    private ArrayList<String> bookmarksFilter = new ArrayList<>(); // Only set if filter is for bookmarks
    private ArrayList<String> followsFilter = new ArrayList<>(); // Only set if filter is for bookmarks

    //Get a FilterType given a string
    public static FilterType get(String str) {
        FilterType ft;
        switch (str) {
            case ("My T(ai)les"): {
                return MY;
            }
            case ("Bookmarks"): {
                return BOOKMARKS;
            }
            case ("Drafts"): {
                return DRAFTS;
            }
            case ("By Author") : {
                return AUTHOR;
            }
            case ("Popular") : {
                return POPULAR;
            }
            case ("Followed Authors"): {
                return FOLLOWING;
            }
            default:
                return NONE;
        }
    }

    //Get a Query for this FilterType from the provided ref that is appropriate for this filter
    public Query getQuery(Context context, DatabaseReference ref) {
        switch(this) {
            case POPULAR: {
                return ref.orderByChild("loveCount").limitToFirst(10);
            }
            default: {
                return ref.orderByChild("timestamp").limitToFirst(10);
            }
        }
    }

    //Return true if this filter includes the provided story, false otherwise
    public boolean includes(Context context, Story story) {
        switch (this) {
            case MY: {
                return story.getAuthorID().equals(AuthUtils.getLoggedInUserID(context)) && !story.getIsDraft();
            }
            case DRAFTS: {
                return story.getAuthorID().equals(AuthUtils.getLoggedInUserID(context)) && story.getIsDraft();
            }
            case BOOKMARKS: {
                return bookmarksFilter.contains(story.getId());
            }
            case AUTHOR: {
               return authorUsernameFilter.equals("") || story.getAuthorID().equals(authorUsernameFilter);
            }
            case FOLLOWING: {
                return followsFilter.contains(story.getAuthorID());
            }
            case POPULAR: {}
            default: {
                return !story.getIsDraft();
            }
        }
    }

    public void setAuthorFilter(String username) {
        this.authorUsernameFilter = username;
    }

    public void setBookmarksFilter(ArrayList<String> bookmarks) {
        this.bookmarksFilter = bookmarks;
    }

    public void setFollowsFilter(ArrayList<String> follows) {
        this.followsFilter = follows;
    }
}