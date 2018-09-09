package edu.uic.cs440.group1.dungeon_crafter.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

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

import edu.uic.cs440.group1.dungeon_crafter.R;


public class FindGroupActivity extends AppCompatActivity {

    private int groupId = 0;
    private final int QUIT = 0, JOIN = 1;
    private int ACTION = JOIN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_group);

        //Button logoutButton = (Button) findViewById(R.id.logoutButton);
        //Button readyStatusButton = (Button) findViewById(R.id.readyStatusButton);
        Button returnToLobbyButton = (Button) findViewById(R.id.returnToLobbyButton);

        //logoutButton.setOnClickListener(logoutButtonHandler);
        //readyStatusButton.setOnClickListener(readyStatusButtonHandler);
        returnToLobbyButton.setOnClickListener(returnToLobbyButtonHandler);
        new Thread(new RequestGroup()).start();
    }

    private View.OnClickListener logoutButtonHandler = new View.OnClickListener() {
        @Override
        public void onClick(View view)
        {

        }
    };

    private View.OnClickListener readyStatusButtonHandler = new View.OnClickListener() {
        @Override
        public void onClick(View view)
        {

        }
    };

    private View.OnClickListener returnToLobbyButtonHandler = new View.OnClickListener() {
        @Override
        public void onClick(View view)
        {
            ACTION = QUIT;
        }
    };

    private void launchAdventureActivity() {
        // FIXME: start Adventure Activity
        startActivity(new Intent(this, AdventureActivity.class));
        finish();
    }

    private void updateUsersInGroupList(ArrayList<String> users) {
        TextView tv = findViewById(R.id.usersInGroupTextView);
        // clear list
        tv.setText("");
        for (String u : users) {
            tv.setText(tv.getText() + u + "\n");
        }
    }



    // ------------------------------------------------------------------------
    // Thread classes
    // ------------------------------------------------------------------------
    class RequestGroup implements Runnable {

        @Override
        public void run() {

            Log.i("FindGroupAct: ", "Requesting group");

            String url = "http://alexviznytsya.me/dungeoncrafter/groupformation.php";


            while (true) {
                try {
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

                    Log.i("FindGroupAct", "Loop...");
                    Log.i("FindGroupAct", "Crash 0");
                    JSONObject jsonObject = new JSONObject();
                    try {
                        Thread.sleep(1000);
                        if (ACTION == JOIN)
                            jsonObject.put("request", "join");
                        else if (ACTION == QUIT)
                            jsonObject.put("request", "quit");

                        jsonObject.put("groupid", groupId);
                        jsonObject.put(getString(R.string.session_id_key),
                                getSharedPreferences(getString(R.string.user_info_file),
                                        MODE_PRIVATE).getString
                                        (getString(R.string.session_id_key), null));

                        Log.i("FindGroupAnt: ", getSharedPreferences(getString(R.string.user_info_file),
                                MODE_PRIVATE).toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Log.i("FindGroupAct", "sent: " + jsonObject.toString());

                    writer.write(jsonObject.toString());
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

                    Log.i("FindGroupAct: ", "SJON:" + sb.toString());

                    try {
                        processResponse(new JSONObject(sb.toString()));
                        if (ACTION == QUIT) {
                            Log.i("FindGroupAct", "FINISH");
                            finish();
                            return;
                        }
                        if (extractReadyStatus(new JSONObject(sb.toString())) == 1) {
                            launchAdventureActivity();
                            return;
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void processResponse(final JSONObject response) {

            groupId = extractGroupId(response);

            // save goupId in user prefs
            SharedPreferences.Editor editor = getSharedPreferences(
                    getString(R.string.user_info_file), MODE_PRIVATE).edit();
            editor.putInt(getString(R.string.groupId), groupId);
            editor.apply();

            final ArrayList<String> users = extractUsers(response);
            runOnUiThread(new Runnable() {
                public void run() {
                    updateUsersInGroupList(users);
                }
            });
        }

        private int extractGroupId(JSONObject response) {
            int id = -1;
            try {
                id = Integer.parseInt(response.getString("groupid"));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return id;
        }

        private int extractReadyStatus(JSONObject response) {
            int ready = -1;
            try {
                ready = Integer.parseInt(response.getString("ready"));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return ready;
        }

        private ArrayList<String> extractUsers(JSONObject response) {

            ArrayList<String> users = new ArrayList<>();
            try {
                JSONArray groupMembers = response.getJSONArray("groupmembers");
                for(int i = 0; i < groupMembers.length(); i++) {
                    users.add(groupMembers.getString(i));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return users;
        }

    }   // NotifyServerThatLookingForGroup
}
