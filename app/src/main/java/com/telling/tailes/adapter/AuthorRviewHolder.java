package com.telling.tailes.adapter;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.telling.tailes.R;
import com.telling.tailes.card.AuthorRviewCardClickListener;

public class AuthorRviewHolder extends RecyclerView.ViewHolder {
    public TextView authorName;
    public Button unfollowButton;

    public AuthorRviewHolder(@NonNull View itemView, final AuthorRviewCardClickListener listener) {
        super(itemView);

        authorName = itemView.findViewById(R.id.authorCardName);
        unfollowButton = itemView.findViewById(R.id.authorCardFollowButton);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    int position = getLayoutPosition();
                    Log.d("AuthorCardClick", "author card was clicked");
                }
            }
        });


    }
}
