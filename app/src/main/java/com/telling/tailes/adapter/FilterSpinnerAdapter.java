package com.telling.tailes.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.telling.tailes.R;
import com.telling.tailes.model.FilterSpinnerItem;

import java.util.ArrayList;

public class FilterSpinnerAdapter extends ArrayAdapter<FilterSpinnerItem> {

    public FilterSpinnerAdapter(Context context, ArrayList<FilterSpinnerItem> filterSpinnerItems) {
        super(context, 0, filterSpinnerItems);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    private View initView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.filter_spinner_item,
                    parent,
                    false
            );
        }

        TextView filterSelection = convertView.findViewById(R.id.filterSpinnerItem);
        FilterSpinnerItem currentItem = getItem(position);
        if (currentItem != null) {
            filterSelection.setText(currentItem.getFilterTitle());
        }
        return convertView;
    }
}
