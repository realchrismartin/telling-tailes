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
            R.string.setting_password_title
    };

    //Methods used to set setting values
    private final Setter[] SETTERS = new Setter [] {
            this::handleGeneric,
            this::handlePasswordChange
    };

    //Methods used to validate setting values
    private final Validator[] VALIDATORS = new Validator[] {
            this::validateGeneric,
            this::validatePassword
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

        toast = Toast.makeText(getContext(),"",Toast.LENGTH_SHORT);

        //Handle background request post-logout
        backgroundTaskResultHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {

                if (msg.getData() == null) {
                    return;
                }

                Bundle bundle = msg.getData();

                String type = bundle.getString("type");
                Boolean result = bundle.getBoolean("result");
                String error = bundle.getString("error");

                if(error.length() > 0 && !result) {
                    toast.setText(error);
                    toast.show();
                    return;
                }

                switch(type) {
                    case "logout": {
                        Intent intent = new Intent(getContext(), LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        break;
                    }
                    case "password_change": {
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
        final EditTextPreference passwordPreference = findPreference("change_password");

        if (passwordPreference == null) {
            return;
        }

        passwordPreference.setSummaryProvider(new Preference.SummaryProvider() {
            @Override
            public CharSequence provideSummary(Preference preference) {

                String password = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("change_password", "");
                return asterisks(password.length());
            }
        });

        passwordPreference.setOnBindEditTextListener(
            new EditTextPreference.OnBindEditTextListener() {
                @Override
                public void onBindEditText(@NonNull EditText editText) {
                    editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    passwordPreference.setSummaryProvider(new Preference.SummaryProvider() {
                        @Override
                        public CharSequence provideSummary(Preference preference) {
                            return asterisks(editText.getText().toString().length());
                        }
                    });
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

    //Validate that the new password being set is valid
    private Boolean validatePassword(Object object) {
        String value = (String)object;

        if(value.contains(" ") || value.length() < 5) {
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

        String newPassword = sharedPreferences.getString(s,"password");
        String value = asterisks(s.length());
        preference.setDefaultValue(value);
        preference.setSummary(value);

        backgroundTaskExecutor.execute(new Runnable() {
            @Override
            public void run() {

                Message resultMessage = new Message();
                Bundle bundle = new Bundle();
                bundle.putString("type", "password_change");

                Context context = getContext();

                if(context == null || newPassword == null || newPassword.equals("password")) {
                    bundle.putBoolean("result",false);
                    bundle.putString("error","Context was null, or new password is null");
                    resultMessage.setData(bundle);
                    backgroundTaskResultHandler.sendMessage(resultMessage);
                    return;
                }

                FBUtils.getUser(getContext(), AuthUtils.getLoggedInUserID(context), new Consumer<User>() {
                    @Override
                    public void accept(User user) {
                       if(user == null)  {
                           bundle.putBoolean("result",false);
                           bundle.putString("error","User was null");
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
                              bundle.putBoolean("result",result);
                              bundle.putString("error","");
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
                        bundle.putString("type","logout");
                        bundle.putBoolean("result",s.length() <= 0);
                        bundle.putString("error",s);
                        resultMessage.setData(bundle);
                        backgroundTaskResultHandler.sendMessage(resultMessage);
                    }
                });
            }
        });
    }

    //Utility function to replace passwords with asterisks
    private String asterisks(int num) {
        StringBuilder result = new StringBuilder();

        for(int i=0;i<num;i++) {
            result.append("*");
        }

        return result.toString();
    }
}
