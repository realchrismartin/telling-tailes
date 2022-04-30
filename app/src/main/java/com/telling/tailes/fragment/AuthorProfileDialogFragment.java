package com.telling.tailes.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.telling.tailes.R;
import com.telling.tailes.activity.StoryFeedActivity;
import com.telling.tailes.model.User;
import com.telling.tailes.util.AuthUtils;
import com.telling.tailes.util.DrawableUtils;
import com.telling.tailes.util.FBUtils;
import com.telling.tailes.util.StringUtils;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

//Popup dialog fragment for displaying author profile data
public class AuthorProfileDialogFragment extends DialogFragment {

    private String authorId;
    private int profileIcon;
    private int storyCount;
    private int loveCount;
    private int followCount;
    private boolean following;
    private Toast profileToast;
    private TextView profileUserNameView;
    private ImageView profileButtonView;
    private Button authorProfileFollowButton;
    private Button authorProfileStoriesButton;

    private Executor backgroundTaskExecutor;
    private Handler backgroundTaskResultHandler;

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        //Init dialog members
        init();

        //Set up toast
        profileToast = Toast.makeText(getContext(),getString(R.string.empty_string),Toast.LENGTH_SHORT);

        //Set up executor
        backgroundTaskExecutor = Executors.newFixedThreadPool(2);

        //Set up views
/*        final ArrayAdapter<String> arrayAdapterItems = new ArrayAdapter<String>(
                getContext(), R.layout.author_profile_list_item, menuOptions);*/

        View content = getLayoutInflater().inflate(R.layout.author_profile_dialog, null);

        TextView storyCountView = content.findViewById(R.id.author_profile_story_count_view);
        storyCountView.setText(Integer.toString(storyCount));

        TextView loveCountView = content.findViewById(R.id.author_profile_love_count_view);
        loveCountView.setText(Integer.toString(loveCount));

        TextView followCountView = content.findViewById(R.id.author_profile_follower_count_view);
        followCountView.setText(Integer.toString(followCount));

        TextView ratingView = content.findViewById(R.id.author_profile_rating_view);
        ratingView.setText(calculateRating());

        profileUserNameView = content.findViewById(R.id.author_profile_user_name_view);
        profileUserNameView.setText(authorId);

        profileButtonView = content.findViewById(R.id.author_profile_user_profile_image);
        profileButtonView.setImageDrawable(ContextCompat.getDrawable(getContext(), DrawableUtils.getProfileIconResourceId(profileIcon, false)));

        authorProfileStoriesButton = content.findViewById(R.id.authorProfileStoriesButton);
        authorProfileStoriesButton.setText(authorId + getString(R.string.author_profile_read_option) );

        String followOptionText = getString(R.string.author_profile_follow_option);
        String unfollowOptionText = getString(R.string.author_profile_unfollow_option);

        authorProfileFollowButton = content.findViewById(R.id.authorProfileFollowButton);
        if(following) {
            authorProfileFollowButton.setText(unfollowOptionText);
        } else {
            authorProfileFollowButton.setText(followOptionText);
        }

        //Handle background task interactions with the follow/unfollow toggle
        backgroundTaskResultHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {

                if (msg.getData() == null) {
                    return;
                }

                int result = msg.getData().getInt(getString(R.string.background_task_result_result));

                if(result != 0) {
                    profileToast.setText(R.string.generic_error_notification);
                    return;
                }

                boolean following = msg.getData().getBoolean(getString(R.string.background_task_result_data_following));

                Bundle bundle = new Bundle();
                bundle.putString(getString(R.string.background_task_result_username),authorId);

                if(following) {
                    authorProfileFollowButton.setText(unfollowOptionText);
                    profileToast.setText(getText(R.string.author_profile_follow_notification) + " " + authorId);
                    followCount += 1;
                    bundle.putBoolean(getString(R.string.background_task_result_followed),true);
                } else {
                    authorProfileFollowButton.setText(followOptionText);
                    profileToast.setText(getText(R.string.author_profile_unfollow_notification) + " " + authorId);
                    followCount -= 1;
                    bundle.putBoolean(getString(R.string.background_task_result_followed),false);
                }


                //Inform the parent activity of the follow/unfollow activity
                getParentFragmentManager().setFragmentResult(getString(R.string.author_profile_dialog_fragment_follow),bundle);
                followCountView.setText(Integer.toString(followCount));
                profileToast.show();

            }
        };

        authorProfileFollowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String currentUsername = AuthUtils.getLoggedInUserID(getContext());

                if(currentUsername.equals(getString(R.string.empty_string))){
                    return;
                }

                if(currentUsername.equals(authorId)) {
                    profileToast.setText(R.string.author_profile_follow_self_error);
                    profileToast.show();
                    return;
                }

                //Handle following the author if not following self
                handleAuthorFollow(authorId);
            }
        });


        authorProfileStoriesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), StoryFeedActivity.class);
                intent.putExtra(getString(R.string.intent_extra_feed_filter), StringUtils.filterTypeByFollowedAuthors);
                intent.putExtra(getString(R.string.intent_extra_author_id),authorId);
                startActivity(intent);
            }
        });


        //Create dialog via builder
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(content);

        return builder.create();
    }

    /*
        Set up the dialog with data from the calling activity
     */
    private void init() {
        //Set member properties from args
        Bundle args = getArguments();

        if(args == null) {
            authorId = getString(R.string.none_string);
            storyCount = 0;
            loveCount = 0;
            followCount = 0;
            profileIcon = 0;
            following = false;
            return;
        }

        if(args.containsKey(getString(R.string.background_task_result_data_author_id))) {
            authorId = args.getString(getString(R.string.background_task_result_data_author_id));
        }

        if(args.containsKey(getString(R.string.background_task_result_data_story_count))) {
            storyCount = args.getInt(getString(R.string.background_task_result_data_story_count));
        }

        if(args.containsKey(getString(R.string.background_task_result_data_love_count))) {
            loveCount = args.getInt(getString(R.string.background_task_result_data_love_count));
        }

        if(args.containsKey(getString(R.string.background_task_result_data_follow_count))) {
            followCount = args.getInt(getString(R.string.background_task_result_data_story_count));
        }

        if(args.containsKey(getString(R.string.background_task_result_data_profile_icon))) {
            profileIcon = args.getInt(getString(R.string.background_task_result_data_profile_icon));
        }
        if(args.containsKey(getString(R.string.background_task_result_data_following))) {
            following = args.getBoolean(getString(R.string.background_task_result_data_following));
        }

    }

    /*
        Fragment onClick handler for following or unfollowing an author
     */
    public void handleAuthorFollow(String username) {

        backgroundTaskExecutor.execute(new Runnable() {
            @Override
            public void run() {
                FBUtils.updateFollow(getContext(), username, new Consumer<User>() {
                    @Override
                    public void accept(User user) {
                        Message message = new Message();
                        Bundle data = new Bundle();
                        data.putInt(getString(R.string.background_task_result_result), user == null ? 1 : 0);
                        data.putBoolean(getString(R.string.background_task_result_data_following), user != null && user.getFollows() != null && user.getFollows().contains(authorId));
                        message.setData(data);
                        backgroundTaskResultHandler.sendMessage(message);
                    }
                });
            }
        });
    }

    @SuppressLint("DefaultLocale")
    private String calculateRating() {
        // I HATE JAVA'S INTEGER DIVISION -- wrc
        double number = (loveCount + followCount)/((double)storyCount);
        return String.format("%.1f", number);
    }
}