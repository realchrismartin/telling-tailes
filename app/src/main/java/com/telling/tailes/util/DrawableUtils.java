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

    public static int getAppIconResourceId(String icon, boolean color) {
        if (color) {
            switch (icon) {
                case ("bell"): return R.drawable.bell_outline_color;
                case ("bookmark_outline"): return R.drawable.bookmark_outline_color;
                case ("bookmark_solid"): return R.drawable.bookmark_solid_color;
                case ("bookwithmark"): return R.drawable.bookwithmark_color;
                case ("favorite_outline"): return R.drawable.favorite_outline_color;
                case ("favorite_solid"): return R.drawable.favorite_solid_color;
                case ("key"): return R.drawable.key_color;
                case ("laurels"): return R.drawable.laurels_color;
                case ("logout"): return R.drawable.logout_color;
                case ("people"): return R.drawable.people_color;
                case ("text"): return R.drawable.text_color;
                default: return R.mipmap.ic_launcher_bard_inverted_round;
            }
        }
        switch (icon) {
            case ("bell"): return R.drawable.bell_outline_pitch;
            case ("bookmark_outline"): return R.drawable.bookmark_outline_pitch;
            case ("bookmark_solid"): return R.drawable.bookmark_solid_pitch;
            case ("bookwithmark"): return R.drawable.bookwithmark_pitch;
            case ("favorite_outline"): return R.drawable.favorite_outline_pitch;
            case ("favorite_solid"): return R.drawable.favorite_solid_pitch;
            case ("key"): return R.drawable.key_pitch;
            case ("laurels"): return R.drawable.laurels_pitch;
            case ("logout"): return R.drawable.logout_pitch;
            case ("people"): return R.drawable.people_pitch;
            case ("text"): return R.drawable.text_pitch;
            default: return R.mipmap.ic_launcher_bard_inverted_round;
        }
    }
}
