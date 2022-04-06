package com.telling.tailes.adapter;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.telling.tailes.R;
import com.telling.tailes.card.StoryRviewCardClickListener;

public class StoryRviewHolder extends RecyclerView.ViewHolder {
    public TextView testText;

    public StoryRviewHolder(@NonNull View itemView, final StoryRviewCardClickListener listener) {
        super(itemView);
        testText = itemView.findViewById(R.id.testTextValue);

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
