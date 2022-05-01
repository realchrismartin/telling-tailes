package com.telling.tailes.util;

import android.content.Context;
import android.util.Log;

import com.telling.tailes.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/*
    Contains static utility methods to interface with the GPT API
 */
public class GPTUtils {

    /*
        Given a prompt and length, generate a story
        If the story is not appropriate, keep generating stories until we find an appropriate one
     */
    public static String getStory(Context context, String prompt, int length) {

        boolean appropriate = false;
        int maxIterations = 5;
        String story = StringUtils.emptyString;
        String possibleStory = StringUtils.emptyString;

        for (int i = 0; i < maxIterations; i++) {

            //Complete the prompt and create a story
            possibleStory = getPromptCompletion(context, prompt, length);

            if (possibleStory.length() <= 0) {
                break;
            }

            //Filter out inappropriate stories
            appropriate = isStoryAppropriate(context, possibleStory);

            if (appropriate) {
                story = possibleStory;
                break;
            }
        }
        return story;
    }

    /*
        Return true if the story is appropriate for the user's eyes
        Otherwise, return false
     */
    private static boolean isStoryAppropriate(Context context, String story) {

        try {
            String serverToken = StringUtils.gptToken;

            //Wrap prompt
            story = StringUtils.gptCompletionStart + story + StringUtils.gptCompletionEnd;

            URL url = new URL(StringUtils.gptFilterURI);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(StringUtils.httpPostMethod);
            conn.setRequestProperty(StringUtils.httpContentType, StringUtils.httpContentTypeJSON);
            conn.setRequestProperty(StringUtils.httpAuthorization, StringUtils.httpAuthorizationBearer + StringUtils.space + serverToken);
            conn.setDoOutput(true);

            JSONObject body = new JSONObject();

            try {
                body.put(StringUtils.gptPropertyPrompt, story);
                body.put(StringUtils.gptPropertyMaxTokens, 1);
                body.put(StringUtils.gptPropertyTopP, 0);
                body.put(StringUtils.gptPropertyLogProbs, 10);
                body.put(StringUtils.gptPropertyTemperature, 0.0);
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }

            try {
                OutputStream outputStream = conn.getOutputStream();
                outputStream.write(body.toString().getBytes());
                outputStream.close();
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }

            String result = StringUtils.readString(conn.getInputStream());
            JSONObject responseObject;

            try {
                responseObject = new JSONObject(result);
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }

            JSONArray choices = new JSONArray();

            try {
                choices = responseObject.getJSONArray(StringUtils.gptPropertyChoices);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (choices.length() <= 0) {
                Log.e(StringUtils.gptUtilsTag, StringUtils.gptUtilsErrorNoChoices);
                return false;
            }

            try {
                double toxicityThreshold = -0.355;

                JSONObject filterObject = choices.getJSONObject(0);
                int offensivenessLevel = Integer.parseInt((String) (filterObject.get(StringUtils.gptPropertyText)));

                JSONObject logProbs = filterObject.getJSONObject(StringUtils.gptPropertyLogProbs);
                JSONArray topLogProbs = logProbs.getJSONArray(StringUtils.gptPropertyTopLogProbs);
                JSONObject allLogProbs = topLogProbs.getJSONObject(0);

                Double prob1 = (Double)allLogProbs.get(StringUtils.gptProbabilityLevel1);
                Double prob2 = (Double)allLogProbs.get(StringUtils.gptProbabilityLevel2);

                switch (offensivenessLevel) {
                    case 0:
                        return true;
                    case 1:
                        return prob1 < toxicityThreshold;
                    case 2:
                        return prob2 < toxicityThreshold;
                    default:
                        return false;
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    private static String getPromptCompletion(Context context, String prompt, int length) {
        try {

            if(length < 5) {
               length = 5;
            }

            if(length > 2048) {
                length = 2048;
            }

            String serverToken = StringUtils.gptToken;
            URL url = new URL(StringUtils.gptCompletionURI);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(StringUtils.httpPostMethod);
            conn.setRequestProperty(StringUtils.httpContentType, StringUtils.httpContentTypeJSON);
            conn.setRequestProperty(StringUtils.httpAuthorization, StringUtils.httpAuthorizationBearer + StringUtils.space + serverToken);
            conn.setDoOutput(true);

            JSONObject body = new JSONObject();

            try {
                body.put(StringUtils.gptPropertyPrompt, prompt);
                body.put(StringUtils.gptPropertyMaxTokens, length);
            } catch (JSONException e) {
                e.printStackTrace();
                return StringUtils.emptyString;
            }

            try {
                OutputStream outputStream = conn.getOutputStream();
                outputStream.write(body.toString().getBytes());
                outputStream.close();
            } catch (Exception ex) {
                ex.printStackTrace();
                return StringUtils.emptyString;
            }

            String result = StringUtils.readString(conn.getInputStream());
            JSONObject responseObject;

            try {
                responseObject = new JSONObject(result);
            } catch (JSONException e) {
                e.printStackTrace();
                return StringUtils.emptyString;
            }

            JSONArray choices = new JSONArray();

            try {
                choices = responseObject.getJSONArray(StringUtils.gptPropertyChoices);
            } catch (JSONException e) {
                return e.toString();
            }

            if (choices.length() <= 0) {
                Log.e(StringUtils.gptUtilsTag, StringUtils.gptUtilsErrorNoChoices);
                return StringUtils.emptyString;
            }

            try {

                JSONObject choice = choices.getJSONObject(0);
                String story = choice.getString(StringUtils.gptPropertyText);

                return story;
            } catch (JSONException e) {
                e.printStackTrace();
                return StringUtils.emptyString;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return StringUtils.emptyString;
        }
    }
}
