package com.telling.tailes.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.telling.tailes.R;
import com.telling.tailes.activity.OnAuthorClickCallbackListener;
import com.telling.tailes.card.StoryRviewCard;
import com.telling.tailes.card.StoryRviewCardClickListener;
import com.telling.tailes.model.Story;
import com.telling.tailes.util.AuthUtils;
import com.telling.tailes.util.FBUtils;

import java.util.ArrayList;
import java.util.function.Consumer;

public class StoryRviewAdapter extends RecyclerView.Adapter {
    private ArrayList<StoryRviewCard> storyCardList;
    private StoryRviewCardClickListener listener;
    private Context context;
    private String currentUser;
    private OnAuthorClickCallbackListener authorClickCallbackListener;


    public StoryRviewAdapter(ArrayList<StoryRviewCard> storyCardList, Context context, OnAuthorClickCallbackListener authorClickCallbackListener) {
        this.storyCardList = storyCardList;
        this.context = context;
        this.currentUser = AuthUtils.getLoggedInUserID(context);
        this.authorClickCallbackListener = authorClickCallbackListener;

    }

    public void setOnStoryClickListener(StoryRviewCardClickListener listener) {
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        switch (storyCardList.get(position).getType()) {
            case 0:
                return StoryRviewCard.CARD_TYPE_STORY;
            case 1:
                return StoryRviewCard.CARD_TYPE_LOADING;
            default:
                return -1;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case 0:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.story_card, parent, false);
                return new StoryRviewStoryHolder(view, listener);
            case 1:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.loading_card, parent, false);
                return new RviewLoadingHolder(view);
            default:
                return null;
        }

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        StoryRviewCard currentItem = storyCardList.get(position);

        switch (currentItem.getType()) {
            case 0: {
                StoryRviewStoryHolder stHolder = (StoryRviewStoryHolder) holder;
                stHolder.titleText.setText(currentItem.getTitle());
                updateLoveIconState(currentItem, currentUser, stHolder);
                stHolder.loveButton.setText(
                        currentItem.getStory().getLovers().size() > 0 ?
                                Integer.toString(currentItem.getStory().getLovers().size())
                                :
                                ""
                );
                stHolder.loveButton.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View view) {

                         if(currentUser.equals(currentItem.getStory().getAuthorID())) {
                             return; //TODO: indicate to user that love failed?
                         }
                         FBUtils.updateLove(context.getApplicationContext(), currentItem.getStory(), new Consumer<Story>() {
                             @Override
                             public void accept(Story result) {

                                 if (result == null) {
                                     return; //TODO: indicate to user that love failed?
                                 }

                                 currentItem.setStory(result);

                                 stHolder.loveButton.setText(
                                         result.getLovers().size() > 0 ?
                                                 Integer.toString(result.getLovers().size())
                                                 :
                                                 ""
                                 );
                                 updateLoveIconState(currentItem, currentUser, stHolder);
                             }
                         });
                    }
                });

                updateBookmarkIconState(currentItem, currentUser, stHolder);

                stHolder.bookmarkButton.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        FBUtils.updateBookmark(context.getApplicationContext(), currentItem.getStory(), new Consumer<Story>() {
                            @Override
                            public void accept(Story result) {
                                if (result == null) {
                                    return;
                                }
                                updateBookmarkIconState(currentItem, currentUser, stHolder);
                            }
                        });
                    }
                });


                stHolder.profileButton.setText(currentItem.getAuthorId());
                stHolder.profileButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        authorClickCallbackListener.handleAuthorClick(currentItem.getAuthorId());
                    }
                });

            }
            case 1:
            default:
                break;
        }
    }

    @Override
    public int getItemCount() { return storyCardList.size(); }

    private void updateBookmarkIconState(StoryRviewCard currentItem, String currentUser, StoryRviewStoryHolder holder) {
        if (currentItem.getBookmarkers().contains(currentUser)) {
            holder.bookmarkButton.setImageResource(R.drawable.bookmark_solid_pitch);
        } else {
            holder.bookmarkButton.setImageResource(R.drawable.bookmark_outline_pitch);
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateLoveIconState(StoryRviewCard currentItem, String currentUser, StoryRviewStoryHolder holder) {
        if (currentItem.getStory().getLovers().contains(AuthUtils.getLoggedInUserID(context))) {
            holder.loveButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.favorite_solid_pitch, 0);
        } else {
            holder.loveButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.favorite_outline_pitch, 0);
        }
    }
}
