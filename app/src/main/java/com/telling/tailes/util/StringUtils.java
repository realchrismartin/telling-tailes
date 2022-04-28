package com.telling.tailes.util;

public class StringUtils {

    //Given a string, get the number of "words" in the string
    public static int getWordCount(String input) {
        return 0; //TODO
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
}
