package com.telling.tailes.util;

import com.telling.tailes.R;

public class DrawableUtils {

    public static int profileIconCount() {
        return 7;
    }

    public static int getProfileIconResourceId(int profileIcon, boolean large) {
        if (!large) {
            switch (profileIcon) {
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
                    return R.drawable.profile_0;
            }
        }
        switch (profileIcon) {
            case 1: {
                return R.drawable.profile_1_large;
            }
            case 2: {
                return R.drawable.profile_2_large;
            }
            case 3: {
                return R.drawable.profile_3_large;
            }
            case 4: {
                return R.drawable.profile_4_large;
            }
            case 5: {
                return R.drawable.profile_5_large;
            }
            case 6: {
                return R.drawable.profile_6_large;
            }
            case 7: {
                return R.drawable.profile_7_large;
            }
            case 0:
            default:
                return R.drawable.profile_0_large;
        }
    }
}
