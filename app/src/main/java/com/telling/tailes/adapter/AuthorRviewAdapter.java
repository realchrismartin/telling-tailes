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

import java.util.ArrayList;

public class AuthorRviewAdapter extends RecyclerView.Adapter<AuthorRviewHolder> {
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




    @NonNull
    @Override
    public AuthorRviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.author_card, parent, false);
        return new AuthorRviewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(AuthorRviewHolder holder, int position) {
        AuthorRviewCard currentItem = authorCardList.get(position);
        holder.authorName.setText(currentItem.getAuthor());
        holder.unfollowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                unfollowClickCallbackListener.handleUnfollowClick(currentItem.getAuthor());
            }
        });
    }

    @Override
    public int getItemCount() { return authorCardList != null ? authorCardList.size() : 0; }
}
