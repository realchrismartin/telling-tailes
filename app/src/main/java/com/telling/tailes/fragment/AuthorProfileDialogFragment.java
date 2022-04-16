package com.telling.tailes.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.telling.tailes.R;

//Popup dialog fragment for displaying author profile data
public class AuthorProfileDialogFragment extends DialogFragment {

    private String authorId;
    private int storyCount;
    private int loveCount;
    private boolean following;
    private String[] menuOptions;
    private String followOptionText;
    private String unfollowOptionText;

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        //Init dialog members
        init();

        //Set up views
        final ArrayAdapter<String> arrayAdapterItems = new ArrayAdapter<String>(
                getContext(), R.layout.author_profile_list_item, menuOptions);

        View content = getLayoutInflater().inflate(R.layout.author_profile_dialog, null);

        TextView storyCountView = content.findViewById(R.id.author_profile_story_count_view);
        storyCountView.setText(Integer.toString(storyCount));

        TextView loveCountView = content.findViewById(R.id.author_profile_love_count_view);
        loveCountView.setText(Integer.toString(loveCount));

        final ListView items = content.findViewById(R.id.author_profile_options_view);
        items.setAdapter(arrayAdapterItems);
        items.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        //Set up item onclick listener for dialog options
        items.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                CheckedTextView checkView = (CheckedTextView) view.findViewById(R.id.author_profile_check_view);
                String option = adapterView.getItemAtPosition(i).toString();

                if (option.equals(followOptionText) || option.equals(unfollowOptionText)) {
                    if(checkView.isChecked()) {
                        checkView.setText(unfollowOptionText);
                    } else {
                        checkView.setText(followOptionText);
                    }
                }
            }
        });

        //Create dialog via builder
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(authorId);
        builder.setView(content);
        builder.setIcon(R.drawable.ic_baseline_favorite_border_24);

        return builder.create();
    }

    /*
        Set up the dialog with data from the calling activity
     */
    private void init() {

        //Set up option text
        followOptionText = getResources().getString(R.string.author_profile_follow_option);
        unfollowOptionText = getResources().getString(R.string.author_profile_unfollow_option);

        //Set menu options
        menuOptions = updateValues(getResources().getStringArray(R.array.author_profile_options));

        //Set member properties from args
        Bundle args = getArguments();

        if(args == null) {
            authorId = "None";
            storyCount = 0;
            loveCount = 0;
            following = false;
            return;
        }

        if(args.containsKey("authorId")) {
            authorId = args.getString("authorId");
        }

        if(args.containsKey("storyCount")) {
            storyCount = args.getInt("storyCount");
        }

        if(args.containsKey("loveCount")) {
            loveCount = args.getInt("loveCount");
        }

        if(args.containsKey("following")) {
            following = args.getBoolean("following");
        }

    }
    /*
        Update the option list to reflect whether the author is followed or not
     */
    private String[] updateValues(String[] initialValues) {
        String[] result = new String[initialValues.length];

        for(int i=0;i<initialValues.length;i++) {

            if(initialValues[i].equals(followOptionText)) {
                if(following) {
                    result[i] = unfollowOptionText;
                    continue;
                }
            }

            result[i]  = initialValues[i];
        }

        return result;
    }
}
