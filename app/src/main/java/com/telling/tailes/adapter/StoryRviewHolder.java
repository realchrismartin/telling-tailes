package com.telling.tailes.adapter;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.telling.tailes.R;
import com.telling.tailes.card.StoryRviewCardClickListener;

public class StoryRviewHolder extends RecyclerView.ViewHolder {
    public TextView titleText;
    public TextView authorText;

    public StoryRviewHolder(@NonNull View itemView, final StoryRviewCardClickListener listener) {
        super(itemView);
        titleText = itemView.findViewById(R.id.storyCardTitle);
        authorText = itemView.findViewById(R.id.storyCardAuthor);

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
