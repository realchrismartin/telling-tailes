package com.telling.tailes.adapter;

import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.telling.tailes.R;
import com.telling.tailes.card.StoryRviewCardClickListener;

public class StoryRviewStoryHolder extends RecyclerView.ViewHolder {
    public TextView titleText;
    public TextView authorText;
    public Button loveButton;
    public ImageButton bookmarkButton;
    public Button profileButton;

    public StoryRviewStoryHolder(@NonNull View itemView, final StoryRviewCardClickListener listener) {
        super(itemView);
        titleText = itemView.findViewById(R.id.storyCardTitle);
        loveButton = itemView.findViewById(R.id.storyCardLoveButton);

        bookmarkButton = itemView.findViewById(R.id.storyCardBookmarkButton);

        profileButton = itemView.findViewById(R.id.storyCardAuthorProfileButton);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    int position = getLayoutPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onStoryClick(position);
                    }
                }
            }
        });
    }
}
