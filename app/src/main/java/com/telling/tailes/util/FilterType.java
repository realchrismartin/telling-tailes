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
            case ("By Followed Authors"): {
                return FOLLOWING;
            }
            default:
                return NONE;
        }
    }

    //Return the property this filter sorts FB data results by
    private static String getSortProperty(FilterType type) {
        switch(type) {
            case POPULAR: {
                return "loveCount";
            }
            default: {
                return "timestamp";
            }
        }
    }

    //Given a Story, return the property this filter would sort that story by
    public Object getSortPropertyValue(Story story) {
       switch(getSortProperty(this)) {
           case "id": {
               return story.getId();
           }
           case "loveCount": {
              return story.getLoveCount(); //TODO
           }
           case "timestamp": {
               return story.getTimestamp(); //TODO
           }
           case "title": {
               return story.getTitle();
           }
           default : {
               return "";
           }
       }
    }

    //Get a Query for this FilterType from the provided ref that is appropriate for this filter
    public Query getQuery(DatabaseReference ref, Object lastLoadedStorySortValue) {
        String key = getSortProperty(this);

        if (lastLoadedStorySortValue == null) {
            return ref.orderByChild(key).limitToFirst(10);
        }
        if (key.equals("timestamp") || key.equals("loveCount")) {
            return ref.orderByChild(key).limitToFirst(10).startAfter((double) lastLoadedStorySortValue);
        }
        return ref.orderByChild(key).limitToFirst(10).startAfter((String)lastLoadedStorySortValue);
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
                return bookmarksFilter.contains(story.getId()) && !story.getIsDraft();
            }
            case AUTHOR: {
               return (authorUsernameFilter.equals("") || story.getAuthorID().equals(authorUsernameFilter)) && !story.getIsDraft();
            }
            case FOLLOWING: {
                return followsFilter.contains(story.getAuthorID()) && !story.getIsDraft();
            }
            case POPULAR: {
                return story.getLovers().size() > 1 && !story.getIsDraft();
            }
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

    public void addFollowFilter(String username) {this.followsFilter.add(username); }

    public void removeFollowFilter(String username) { this.followsFilter.remove(username); }
}