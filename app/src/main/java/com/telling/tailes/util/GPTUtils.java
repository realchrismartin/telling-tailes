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
     */
    public static String getStory(Context context, String prompt, int length)
    {
        try {
            String serverToken = context.getString(R.string.gpt_api_token);

            //TODO: update length to be something more realistic
            //Probably do some math on the length value to translate it to number of tokens
            //Example default length is 5

            URL url = new URL(context.getString(R.string.gpt_api_uri));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + serverToken);
            conn.setDoOutput(true);

            JSONObject body = new JSONObject();

            try {
                body.put("prompt",prompt);
                body.put("max_tokens",length);
            } catch (JSONException e)
            {
                e.printStackTrace();
                return "";
            }

            OutputStream outputStream = conn.getOutputStream();
            outputStream.write(body.toString().getBytes());
            outputStream.close();

            String result =  readString(conn.getInputStream());
            JSONObject responseObject;

           try {
               responseObject = new JSONObject(result);
           } catch (JSONException e)
           {
               e.printStackTrace();
               return "";
           }

           JSONArray choices = new JSONArray();

           try {
                choices = responseObject.getJSONArray("choices");
           } catch(JSONException e) {
                e.printStackTrace();
           }

           if(choices.length() <= 0)
           {
               Log.e("GPTUtils","Response had no choices");
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
