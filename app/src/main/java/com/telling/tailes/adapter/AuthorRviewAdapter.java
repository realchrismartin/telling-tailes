package com.telling.tailes.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.telling.tailes.R;
import com.telling.tailes.activity.OnUnfollowClickCallbackListener;
import com.telling.tailes.card.AuthorRviewCard;
import com.telling.tailes.card.AuthorRviewCardClickListener;
import com.telling.tailes.card.StoryRviewCard;

import java.util.ArrayList;

public class AuthorRviewAdapter extends RecyclerView.Adapter{
    private ArrayList<AuthorRviewCard> authorCardList;
    private AuthorRviewCardClickListener listener;
    private OnUnfollowClickCallbackListener unfollowClickCallbackListener;
    Context context;

    public AuthorRviewAdapter(ArrayList<AuthorRviewCard> authorCardList, Context context,
                              OnUnfollowClickCallbackListener unfollowClickCallbackListener) {
        this.authorCardList = authorCardList;
        this.context = context;
        this.unfollowClickCallbackListener = unfollowClickCallbackListener;
    }

    public void setOnAuthorClickListener(AuthorRviewCardClickListener listener) {
        this.listener = listener;
    }


    @Override
    public int getItemViewType(int position) {
        switch (authorCardList.get(position).getCardType()) {
            case 0:
                return AuthorRviewCard.CARD_TYPE_AUTHOR;
            case 1:
                return AuthorRviewCard.CARD_TYPE_LOADING;
            case 2:
                return AuthorRviewCard.CARD_TYPE_NO_AUTHORS;
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
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.author_card, parent, false);
                return new AuthorRviewHolder(view, listener);
            case 1:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.loading_card, parent, false);
                return new RviewLoadingHolder(view);
            case 2:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.no_authors_card, parent, false);
                return new NoAuthorsRViewHolder(view);
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        AuthorRviewCard currentItem = authorCardList.get(position);
        switch (currentItem.getCardType()) {
            case 0:
                AuthorRviewHolder aHolder = (AuthorRviewHolder) holder;
                aHolder.authorName.setText(currentItem.getAuthor());
                aHolder.unfollowButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        unfollowClickCallbackListener.handleUnfollowClick(currentItem.getAuthor());
                    }
                });
            case 1:
            case 2:
            default:
                break;
        }

    }

    @Override
    public int getItemCount() { return authorCardList != null ? authorCardList.size() : 0; }


}
