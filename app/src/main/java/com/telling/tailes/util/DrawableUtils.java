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
                case (StringUtils.drawableBell): return R.drawable.bell_outline_color;
                case (StringUtils.drawableBookmarkOutline): return R.drawable.bookmark_outline_color;
                case (StringUtils.drawableBookmarkSolid): return R.drawable.bookmark_solid_color;
                case (StringUtils.drawableBookWithMark): return R.drawable.bookwithmark_color;
                case (StringUtils.drawableFavoriteOutline): return R.drawable.favorite_outline_color;
                case (StringUtils.drawableFavoriteSolid): return R.drawable.favorite_solid_color;
                case (StringUtils.drawableKey): return R.drawable.key_color;
                case (StringUtils.drawableLaurels): return R.drawable.laurels_color;
                case (StringUtils.drawableLogout): return R.drawable.logout_color;
                case (StringUtils.drawablePeople): return R.drawable.people_color;
                case (StringUtils.drawableText): return R.drawable.text_color;
                default: return R.mipmap.ic_launcher_bard_inverted_round;
            }
        }
        switch (icon) {
            case (StringUtils.drawableBell): return R.drawable.bell_outline_pitch;
            case (StringUtils.drawableBookmarkOutline): return R.drawable.bookmark_outline_pitch;
            case (StringUtils.drawableBookmarkSolid): return R.drawable.bookmark_solid_pitch;
            case (StringUtils.drawableBookWithMark): return R.drawable.bookwithmark_pitch;
            case (StringUtils.drawableFavoriteOutline) : return R.drawable.favorite_outline_pitch;
            case (StringUtils.drawableFavoriteSolid): return R.drawable.favorite_solid_pitch;
            case (StringUtils.drawableKey): return R.drawable.key_pitch;
            case (StringUtils.drawableLaurels): return R.drawable.laurels_pitch;
            case (StringUtils.drawableLogout): return R.drawable.logout_pitch;
            case (StringUtils.drawablePeople): return R.drawable.people_pitch;
            case (StringUtils.drawableText): return R.drawable.text_pitch;
            default: return R.mipmap.ic_launcher_bard_inverted_round;
        }
    }
}
