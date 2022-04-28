package com.telling.tailes.util;

import com.telling.tailes.R;

public class DrawableUtils {

    public static int profileIconCount() {
        return 7;
    }

    public static int getProfileIconResourceId(int profileIcon) {
        switch(profileIcon) {
            case 1: {
                return R.drawable.profile_1;
            }
            case 2: {
                return R.drawable.profile_2;
            }
            case 3: {
               return R.drawable.profile_3;
            }
            case 4: {
                return R.drawable.profile_4;
            }
            case 5: {
                return R.drawable.profile_5;
            }
            case 6: {
                return R.drawable.profile_6;
            }
            case 7: {
                return R.drawable.profile_7;
            }
            case 0:
            default:
                return R.drawable.profile_icon_pitch;
        }
    }
}
