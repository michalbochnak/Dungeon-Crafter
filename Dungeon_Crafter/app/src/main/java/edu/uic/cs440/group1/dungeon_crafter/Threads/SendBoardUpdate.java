package edu.uic.cs440.group1.dungeon_crafter.Threads;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Vector;

import edu.uic.cs440.group1.dungeon_crafter.Characters.*;

/**
 * Created by Michal on 4/7/2018.
 */

//
// Send the update to database about board state
//
public class SendBoardUpdate implements Runnable {

    private static final String TAG = "SendBoardUpdate";
    private JSONObject jsonToSend = new JSONObject();
    private static boolean shouldRun = true;

    public SendBoardUpdate(Vector<Player> players, Vector<Enemy> enemies, JSONObject info) {
        update(players, enemies, info);
    }

    public void update(Vector<Player> players, Vector<Enemy> enemies, JSONObject info) {
        JSONArray characters = new JSONArray();
        for (Player p : players)
            characters.put(p.getJsonInfo());
        for (Enemy e : enemies)
            characters.put(e.getJsonInfo());

        try {
            jsonToSend.put("players", characters);
            jsonToSend.put("info", info);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        String url = "http://alexviznytsya.me/dungeoncrafter/updategameplay.php";

        //while (shouldRun) {
            try {
                //Thread.sleep(2000);
                //Connect
                HttpURLConnection urlConnection = (HttpURLConnection) ((new URL(url).openConnection()));
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setRequestMethod("POST");
                urlConnection.connect();

                //Write
                OutputStream outputStream = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));

                writer.write(jsonToSend.toString());
                writer.close();
                outputStream.close();

                BufferedReader bufferedReader = new BufferedReader
                        (new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));

                String line = null;
                StringBuilder sb = new StringBuilder();

                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                }

                bufferedReader.close();

                Log.i(TAG, "SJON:" + sb.toString());

                try {
                    Log.i(TAG, "JSON Sent: " + jsonToSend.toString());
                    if (extractDoneValue(jsonToSend) == 1)
                        return;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }


            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        //}
    }

    public static void setShouldRun(boolean value)
    {
        shouldRun = value;
    }

    private int extractDoneValue(JSONObject json) {
        int done = -1;
        try {
            done = json.getInt("done");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return done;
    }

}   // SendBoardUpdate class