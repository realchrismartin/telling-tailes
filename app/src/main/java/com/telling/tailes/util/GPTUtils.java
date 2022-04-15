package com.telling.tailes.util;

import android.content.Context;
import android.util.Log;

import com.telling.tailes.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
        String story = "";
        String possibleStory = "";

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
            String serverToken = context.getString(R.string.gpt_api_token);

            //Wrap prompt
            story = "<|endoftext|>" + story + "\n--\nLabel:";

            URL url = new URL(context.getString(R.string.gpt_api_filter_uri));

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + serverToken);
            conn.setDoOutput(true);

            JSONObject body = new JSONObject();

            try {
                body.put("prompt", story);
                body.put("max_tokens", 1);
                body.put("top_p", 0);
                body.put("logprobs", 10);
                body.put("temperature", 0.0);
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

            String result = readString(conn.getInputStream());
            JSONObject responseObject;

            try {
                responseObject = new JSONObject(result);
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }

            JSONArray choices = new JSONArray();

            try {
                choices = responseObject.getJSONArray("choices");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (choices.length() <= 0) {
                Log.e("GPTUtils", "Response had no choices");
                return false;
            }

            try {
                JSONObject filterObject = choices.getJSONObject(0);
                int offensivenessLevel = Integer.parseInt((String) (filterObject.get("text")));



                double toxicityThreshold = -0.355;
                JSONObject logProbs = filterObject.getJSONObject("logprobs");
                JSONArray topLogProbs = logProbs.getJSONArray("top_logprobs");
                JSONObject allLogProbs = topLogProbs.getJSONObject(0);


                Double prob0 = (Double)allLogProbs.get("0");
                Double prob1 = (Double)allLogProbs.get("1");
                Double prob2 = (Double)allLogProbs.get("2");

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

                //Return true if the result is completely inoffensive
/*                if (offensivenessLevel < 2) {
                    return true;
                }

                if(prob2 < toxicityThreshold) {
                    return true;
                    try {


                        if (prob0 >= prob1) {

                        }
                    } catch (JSONException ex) {
                       ex.printStackTrace();
                       return false;
                    }
                }*/

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
            String serverToken = context.getString(R.string.gpt_api_token);

            //TODO: update length to be something more realistic
            //Probably do some math on the length value to translate it to number of tokens
            //Example default length is 5

            URL url = new URL(context.getString(R.string.gpt_api_completion_uri));

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + serverToken);
            conn.setDoOutput(true);

            JSONObject body = new JSONObject();

            try {
                body.put("prompt", prompt);
                body.put("max_tokens", length);
            } catch (JSONException e) {
                e.printStackTrace();
                return "";
            }

            try {
                OutputStream outputStream = conn.getOutputStream();
                outputStream.write(body.toString().getBytes());
                outputStream.close();
            } catch (Exception ex) {
                ex.printStackTrace();
                return "";
            }

            String result = readString(conn.getInputStream());
            JSONObject responseObject;

            try {
                responseObject = new JSONObject(result);
            } catch (JSONException e) {
                e.printStackTrace();
                return "";
            }

            JSONArray choices = new JSONArray();

            try {
                choices = responseObject.getJSONArray("choices");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (choices.length() <= 0) {
                Log.e("GPTUtils", "Response had no choices");
                return "";
            }

            try {

                //TODO: perhaps don't return the first choice only?
                JSONObject choice = choices.getJSONObject(0);
                String story = choice.getString("text");

                return story;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    /*
        Helper method to read a string from an InputStream
     */
    private static String readString(InputStream inputStream) {
        StringBuilder stringBuilder = new StringBuilder();
        String res = "";
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String len;
            while ((len = bufferedReader.readLine()) != null) {
                stringBuilder.append(len);
            }
            bufferedReader.close();
            //TODO: check this formatting to make sure it's right
            res = stringBuilder.toString().replace(",", ",\n");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return res;
    }
}
