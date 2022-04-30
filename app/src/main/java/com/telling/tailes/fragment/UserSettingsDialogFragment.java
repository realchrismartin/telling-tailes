package com.telling.tailes.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.InputType;
import android.util.Pair;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.telling.tailes.R;
import com.telling.tailes.activity.LoginActivity;
import com.telling.tailes.model.User;
import com.telling.tailes.util.AuthUtils;
import com.telling.tailes.util.FBUtils;
import com.telling.tailes.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class UserSettingsDialogFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private Executor backgroundTaskExecutor;
    private Handler backgroundTaskResultHandler;

    //Interface for Setter methods
    private interface Setter {
        void apply(SharedPreferences sharedPreferences, String s);
    }

    //Interface for Validator methods
    private interface Validator {
        Boolean validate(Object value);
    }

    private static final int[] SETTINGS = new int[] {
            R.string.setting_hide_title,
            R.string.setting_password_title,
            R.string.setting_text_size_title
    };

    //Methods used to set setting values
    private final Setter[] SETTERS = new Setter [] {
            this::handleGeneric,
            this::handlePasswordChange,
            this::handleGeneric
    };

    //Methods used to validate setting values
    private final Validator[] VALIDATORS = new Validator[] {
            this::validateGeneric,
            this::validatePassword,
            this::validateTextSize
    };

    private Map<String, Preference> preferences = new HashMap<>();
    private Map<String, Setter> setters = new HashMap<>();
    private Map<String, Validator> validators = new HashMap<>();

    private Toast toast;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        backgroundTaskExecutor = Executors.newFixedThreadPool(2);

        for (int key : SETTINGS){
            String preferenceKey = getString(key);
            Preference preference = findPreference(preferenceKey);
            preferences.put(preferenceKey,preference);
        }

        for (int i = 0; i < SETTINGS.length; i++){
            String setting = getString(SETTINGS[i]);
            setters.put(setting, SETTERS[i]);
            validators.put(setting,VALIDATORS[i]);
        }

        activateListeners();

        toast = Toast.makeText(getContext(),getString(R.string.empty_string),Toast.LENGTH_SHORT);

        //Handle background request post-logout
        backgroundTaskResultHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {

                if (msg.getData() == null) {
                    return;
                }

                Bundle bundle = msg.getData();

                String type = bundle.getString(getString(R.string.background_task_result_type));
                Boolean result = bundle.getBoolean(getString(R.string.background_task_result_result));
                String error = bundle.getString(getString(R.string.background_task_result_error));

                if(error.length() > 0 && !result) {
                    toast.setText(error);
                    toast.show();
                    return;
                }

                switch(type) {
                    case (StringUtils.backgroundResultPropertyLogout): {
                        Intent intent = new Intent(getContext(), LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        break;
                    }
                    case (StringUtils.backgroundResultPropertyPasswordChange): {
                       toast.setText(R.string.password_change_notification);
                       toast.show();
                       break;
                    }
                }
            }
        };

        //Set up logout handler
        Preference logoutPreference = findPreference(getString(R.string.setting_logout_title));

        if(logoutPreference != null) {
            logoutPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    handleLogout();
                    return true;
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        deactivateListeners();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        //Set up all preferences
        setPreferencesFromResource(R.xml.preferences, rootKey);

        //Set up password text replacement
        final EditTextPreference passwordPreference = findPreference(getString(R.string.preference_change_password));

        if (passwordPreference == null) {
            return;
        }

        passwordPreference.setSummaryProvider(preference -> {

            String password = PreferenceManager.getDefaultSharedPreferences(getContext()).getString(getString(R.string.preference_change_password), getString(R.string.empty_string));
            return StringUtils.getAsterisks(password);
        });

        passwordPreference.setOnBindEditTextListener(
            new EditTextPreference.OnBindEditTextListener() {
                @Override
                public void onBindEditText(@NonNull EditText editText) {
                    editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    passwordPreference.setSummaryProvider(preference -> StringUtils.getAsterisks(editText.getText().toString()));
                }
            });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void activateListeners() {
        attachListenerToObject(this);
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    private void deactivateListeners() {
        attachListenerToObject(null);
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    private void attachListenerToObject(Preference.OnPreferenceChangeListener object) {
        for (Preference preference : preferences.values()) {
            preference.setOnPreferenceChangeListener(object);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (getActivity() != null && isAdded() && setters.get(s) != null) {
            setters.get(s).apply(sharedPreferences, s);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if(validators.get(preference.getKey()) != null) {
            return validators.get(preference.getKey()).validate(newValue);
        }

        return true;
    }

    //Validate that the generic object is valid (always true). Use this when there's no validation needed on a setting
    private Boolean validateGeneric(Object object) {
        return true;
    }

    private Boolean validateTextSize(Object object) {
        Integer size = (Integer)object;

        if(size == null) {
            return false;
        }

        return true;
    }

    //Validate that the new password being set is valid
    private Boolean validatePassword(Object object) {
        String value = (String)object;

        if(value.contains(getString(R.string.space)) || value.length() < 5) {
            toast.setText(R.string.password_complexity_error_notification);
            toast.show();
            return false;
        }

        return true;
    }

    //Handle generic preference change, most likely by doing nothing
    private void handleGeneric(SharedPreferences sharedPreferences, String s) { }

    //Handle user submitting a password change via setting
    private void handlePasswordChange(SharedPreferences sharedPreferences, String s) {

        Preference preference = preferences.get(s);

        if(preference == null) {
            return;
        }

        String newPassword = sharedPreferences.getString(s,getString(R.string.shared_preference_password));
        String value = StringUtils.getAsterisks(s);
        preference.setDefaultValue(value);
        preference.setSummary(value);

        backgroundTaskExecutor.execute(new Runnable() {
            @Override
            public void run() {

                Message resultMessage = new Message();
                Bundle bundle = new Bundle();
                bundle.putString(getString(R.string.background_task_result_type), StringUtils.backgroundResultPropertyPasswordChange);

                Context context = getContext();

                if(context == null || newPassword == null || newPassword.equals(getString(R.string.background_task_result_password))) {
                    bundle.putBoolean(getString(R.string.background_task_result_result),false);
                    bundle.putString(getString(R.string.background_task_result_error),getString(R.string.error_new_password_null));
                    resultMessage.setData(bundle);
                    backgroundTaskResultHandler.sendMessage(resultMessage);
                    return;
                }

                FBUtils.getUser(getContext(), AuthUtils.getLoggedInUserID(context), new Consumer<User>() {
                    @Override
                    public void accept(User user) {
                       if(user == null)  {
                           bundle.putBoolean(getString(R.string.background_task_result_result),false);
                           bundle.putString(getString(R.string.background_task_result_error),getString(R.string.error_user_null));
                           resultMessage.setData(bundle);
                           backgroundTaskResultHandler.sendMessage(resultMessage);
                           return;
                       }

                       //Hash and set new password
                       Pair<String,String> hashedPieces = AuthUtils.hashPassword(newPassword);
                       user.setHashedPassword(hashedPieces.first);
                       user.setSalt(hashedPieces.second);

                      FBUtils.updateUser(getContext(), user, new Consumer<Boolean>() {
                          @Override
                          public void accept(Boolean result) {
                              bundle.putBoolean(getString(R.string.background_task_result_result),result);
                              bundle.putString(getString(R.string.background_task_result_error),getString(R.string.empty_string));
                              resultMessage.setData(bundle);
                              backgroundTaskResultHandler.sendMessage(resultMessage);
                          }
                      });
                    }
                });
            }
        });
    }

    //Handle user clicking logout setting
    private void handleLogout() {
        backgroundTaskExecutor.execute(new Runnable() {
            @Override
            public void run() {

                AuthUtils.logOutUser(getContext(), new Consumer<String>() {
                    @Override
                    public void accept(String s) {
                        Message resultMessage = new Message();
                        Bundle bundle = new Bundle();
                        bundle.putString(getString(R.string.background_task_result_type),StringUtils.backgroundResultPropertyLogout);
                        bundle.putBoolean(getString(R.string.background_task_result_result),s.length() <= 0);
                        bundle.putString(getString(R.string.background_task_result_error),s);
                        resultMessage.setData(bundle);
                        backgroundTaskResultHandler.sendMessage(resultMessage);
                    }
                });
            }
        });
    }
}
