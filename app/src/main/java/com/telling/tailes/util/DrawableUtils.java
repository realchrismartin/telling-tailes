package com.telling.tailes.util;

import com.telling.tailes.R;

public class DrawableUtils {

    public static int profileIconCount() {
        return 5;
    }

    public static int getProfileIconResourceId(int profileIcon) {
        switch(profileIcon) {
            case 0: {
                return R.drawable.profile_0;
            }
            case 1: {
                return R.drawable.profile_1;
            }
            case 2: {
               return R.drawable.profile_2;
            }
            default:
                return R.drawable.ic_baseline_profile_24;
        }
    }
}
