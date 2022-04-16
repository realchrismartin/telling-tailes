package com.telling.tailes.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.telling.tailes.R;
import com.telling.tailes.card.StoryRviewCard;
import com.telling.tailes.card.StoryRviewCardClickListener;
import com.telling.tailes.fragment.AuthorProfileDialogFragment;
import com.telling.tailes.util.AuthUtils;
import com.telling.tailes.util.FBUtils;

import java.util.ArrayList;
import java.util.function.Consumer;

public class StoryRviewAdapter extends RecyclerView.Adapter<StoryRviewHolder> {
    private ArrayList<StoryRviewCard> storyCardList;
    private StoryRviewCardClickListener listener;
    private Context context;
    private FragmentManager fragmentManager;

    public StoryRviewAdapter(ArrayList<StoryRviewCard> storyCardList, Context context, FragmentManager fragmentManager) {
        this.storyCardList = storyCardList;
        this.context = context;
        this.fragmentManager = fragmentManager;
    }

    public void setOnStoryClickListener(StoryRviewCardClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public StoryRviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.story_card, parent, false);
        return new StoryRviewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(StoryRviewHolder holder, int position) {

        StoryRviewCard currentItem = storyCardList.get(position);
        holder.titleText.setText(currentItem.getTitle());
        holder.authorText.setText(currentItem.getAuthorId());

        if (currentItem.getLovers().contains(AuthUtils.getLoggedInUserID(context))) {
            holder.loveButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_favorite_24, 0, 0, 0);
        } else {
            holder.loveButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_favorite_border_24, 0, 0, 0);
        }

        holder.loveButton.setText(currentItem.getLovers().size() > 0 ? currentItem.getLovers().size() + "" : ""); //TODO: better way to make the int a string?
        holder.loveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FBUtils.updateLove(context.getApplicationContext(), currentItem.getStory(), new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean result) {

                        if (!result) {
                            return; //TODO: indicate to user that love failed?
                        }

                        holder.loveButton.setText(currentItem.getLovers().size() > 0 ? Integer.toString(currentItem.getLovers().size()) : "");

                        if (currentItem.getLovers().contains(AuthUtils.getLoggedInUserID(context))) {
                            holder.loveButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_favorite_24, 0, 0, 0);
                        } else {
                            holder.loveButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_favorite_border_24, 0, 0, 0);
                        }
                    }

                });
            }
        });

        holder.profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AuthorProfileDialogFragment frag = new AuthorProfileDialogFragment();
                Bundle args = new Bundle();
                args.putString("authorId",currentItem.getAuthorId());
                frag.setArguments(args);
                frag.show(fragmentManager,"AuthorProfileDialogFragment");
            }
        });
    }

    @Override
    public int getItemCount() { return storyCardList.size(); }
}
