package com.telling.tailes.fragment;

import android.content.SharedPreferences;

public interface Setter {
    void apply(SharedPreferences sharedPreferences, String s);
}
