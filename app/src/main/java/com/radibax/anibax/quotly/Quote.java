package com.radibax.anibax.quotly;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;

/**
 * Created by anibax on 11-10-2016.
 */

public class Quote extends Activity {

    URL url;
    String quoteURL="http://api.forismatic.com/api/1.0/?method=getQuote&format=json&key=&lang=en";
/**  Windows shell command with wget64.exe to save to json file with 3 second sleep
 for /l %x in (1, 1, 10) do (
   ping -n 2 127.0.0.1 >nul
   wget64.exe "http://api.forismatic.com/api/1.0/?method=getQuote&format=json&key=&lang=en" -O - >> hi.json
 )
 */
    JSONArray bothColors;
    String quoteText, quoteAuthor,colorName;
    String c1 = "#FFFFFF",c2 = "#000000";
    Context c;

    public Quote(Context context) {
        c = context;
     }
    public void changeColors() {

        InputStream is = c.getResources().openRawResource(R.raw.colors);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        }

        catch (IOException e){
            e.printStackTrace();
        }

        finally {
            try {
                is.close();
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
        try {
            JSONObject jsonString = new JSONObject(writer.toString());
            JSONArray color = jsonString.getJSONArray("color");
            //Randomize this colorObject
            int randomColor = (int)(Math.random() * ((183 - 1) + 1) + 1);
            JSONObject colorObject = color.getJSONObject(randomColor);
            bothColors = colorObject.getJSONArray("colors");
            colorName = colorObject.getString("name");
            c1 = bothColors.getString(0);
            c2 = bothColors.getString(1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.v("colors", c1 + " " + c2 + " " + colorName);
    }

    public String getTextColor(){
        return c1;
    }

    public String getBackgroundColor(){
        return c2;
    }

    public void changeQuote() {

        InputStream is = c.getResources().openRawResource(R.raw.quotes1);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        }

        catch (IOException e){
            e.printStackTrace();
        }

        finally {
            try {
                is.close();
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
        try {
            JSONObject jsonString = new JSONObject(writer.toString());
            JSONArray quote = jsonString.getJSONArray("quote");
            //Randomize this quoteObject
            int maxQuote=quote.length()-1;
            int randomQuote = (int)(Math.random() * ((maxQuote - 1) + 1) + 1);
            JSONObject quoteObject = quote.getJSONObject(randomQuote);
            quoteText = quoteObject.getString("quoteText");
            quoteAuthor = quoteObject.getString("quoteAuthor");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getQuoteText(){
        return quoteText;
    }

    public String getQuoteAuthor(){
        return quoteAuthor;
    }
}
