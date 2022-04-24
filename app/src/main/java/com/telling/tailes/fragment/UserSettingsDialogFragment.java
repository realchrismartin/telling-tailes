package com.telling.tailes.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.telling.tailes.R;

import java.util.HashMap;
import java.util.Map;

public class UserSettingsDialogFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private static final int[] SETTINGS = new int[] {
    };

    private Map<String, Preference> preferences = new HashMap<>();

    private final Setter[] SETTERS = new Setter [] {
    };

    private Map<String, Setter> setters = new HashMap<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        for (int key : SETTINGS){
            preferences.put(getString(key), findPreference(getString(key)));
        }

        for (int i = 0; i < SETTINGS.length; i++){
            setters.put(getString(SETTINGS[i]), SETTERS[i]);
        }

        activateListeners();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        deactivateListeners();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
       //TODO
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        //TODO
        return false;
    }
}
