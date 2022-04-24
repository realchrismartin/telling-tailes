package com.telling.tailes.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
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
import com.telling.tailes.util.AuthUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class UserSettingsDialogFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener, SharedPreferences.OnSharedPreferenceChangeListener {

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
            this::handleHide,
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        deactivateListeners();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

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

    private Boolean validatePassword(Object object) {
        return true; //TODO
    }

    //Handle user clicking hide setting
    private void handleHide(SharedPreferences sharedPreferences, String s) {
        int i = 0;
        //TODO
    }

    //Handle user submitting a password change via setting
    private void handlePasswordChange(SharedPreferences sharedPreferences, String s) {
        int i = 0;
        //TODO
    }

    //Handle user clicking logout setting
    private void handleLogout() {

        Context context = getContext();

        AuthUtils.logOutUser(context, new Consumer<String>() {
            @Override
            public void accept(String s) {
                Intent intent = new Intent(context, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }

    private String asterisks(int num) {
        StringBuilder result = new StringBuilder();

        for(int i=0;i<num;i++) {
            result.append("*");
        }

        return result.toString();
    }
}
