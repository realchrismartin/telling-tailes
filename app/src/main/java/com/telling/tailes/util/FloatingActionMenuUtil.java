package com.telling.tailes.util;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class FloatingActionMenuUtil {

    public static boolean toggleFAM(boolean isOpen, ArrayList<FloatingActionButton> fabList) {
        if (isOpen) {
            for (FloatingActionButton fab : fabList) {
                fab.animate().translationY(0);
            }

            return false;
        }
        int dist = -175;
        for (FloatingActionButton fab : fabList) {
            fab.animate().translationY(dist);
            dist -= 175;
        }
        return true;
    }
}
