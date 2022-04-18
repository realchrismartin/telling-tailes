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
    NONE;

    private String authorUsernameFilter = ""; //Only set if filter is for specific author
    private ArrayList<String> bookmarksFilter = new ArrayList<>(); // Only set if filter is for bookmarks

    //Get a FilterType given a string
    public static FilterType get(String str, String authorId, ArrayList<String> bookmarks) {
        FilterType ft;
        switch (str) {
            case ("My T(ai)les"): {
                return MY;
            }
            case ("Bookmarks"): {
                ft = BOOKMARKS;
                ft.setBookmarksFilter(bookmarks);
                return ft;
            }
            case ("Drafts"): {
                return DRAFTS;
            }
            case ("By Author") : {
                ft = AUTHOR;
                ft.setAuthorFilter(authorId);
                return ft;
            }
            default:
                return NONE;
        }
    }

    //Get a Query for this FilterType from the provided ref that is appropriate for this filter
    //TODO: This is unused right now, but will be helpful to order by love count etc when implemented
    public Query getQuery(DatabaseReference ref) {
        switch (this) {
            case MY: {
                return ref.orderByChild("id").limitToFirst(10); //TODO: duplicates: make these appropriate for each filter
            }
            case DRAFTS: {
                return ref.orderByChild("id").limitToFirst(10);
            }
            case BOOKMARKS: {
                return ref.orderByChild("id").limitToFirst(10);
            }
            case AUTHOR: {
                return ref.orderByChild("id").limitToFirst(10);
            }
            default: {
                return ref.orderByChild("id").limitToFirst(10);
            }
        }
    }

    //Return true if this filter includes the provided story, false otherwise
    //This is not efficient, sorry.
    //TODO: make more efficient
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
                // return false; //story.getAuthorID().equals(AuthUtils.getLoggedInUserID(context)); //TODO: This does nothing currently
            }
            case AUTHOR: {
               return authorUsernameFilter.equals("") || story.getAuthorID().equals(authorUsernameFilter);
            }
            default: {
                return true;
            }
        }
    }

    private void setAuthorFilter(String username) {
        this.authorUsernameFilter = username;
    }

    private void setBookmarksFilter(ArrayList<String> bookmarks) {
        this.bookmarksFilter = bookmarks;
    }
}