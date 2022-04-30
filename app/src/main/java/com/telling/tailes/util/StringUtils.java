package com.telling.tailes.util;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class StringUtils {

    //Given a string, get the number of "words" in the string
    public static int getWordCount(String input) {
        return input.split(" ").length;
    }


    //Given a string, get an "obfuscated" string
    public static String getAsterisks(String input) {

        int num = input.length();

        StringBuilder result = new StringBuilder();

        for(int i=0;i<num;i++) {
            result.append("*");
        }

        return result.toString();
    }

    //Given an arbitrary string, convert the string to an integer that reasonably corresponds with the input
    //Used for notification ids based on user / story ids
    //Note that this is not really unique, which doesn't matter in this use case
    public static Integer toIntegerId(String input) {
        return Arrays.hashCode(input.getBytes(StandardCharsets.UTF_8));
    }
}
