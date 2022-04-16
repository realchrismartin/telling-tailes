package com.telling.tailes.adapter;

import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.telling.tailes.R;
import com.telling.tailes.card.StoryRviewCardClickListener;

public class StoryRviewHolder extends RecyclerView.ViewHolder {
    public TextView titleText;
    public TextView authorText;
    public Button loveButton;
    public Button recycleButton;
    public ImageButton bookmarkButton;

    public StoryRviewHolder(@NonNull View itemView, final StoryRviewCardClickListener listener) {
        super(itemView);
        titleText = itemView.findViewById(R.id.storyCardTitle);
        authorText = itemView.findViewById(R.id.storyCardAuthor);
        loveButton = itemView.findViewById(R.id.storyCardLoveButton);
        recycleButton = itemView.findViewById(R.id.storyCardRecycleButton);
        bookmarkButton = itemView.findViewById(R.id.storyCardBookmarkButton);
        //Note: recycle button is hidden on cards by default
        recycleButton.setVisibility(View.INVISIBLE);

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
