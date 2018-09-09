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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

import edu.uic.cs440.group1.dungeon_crafter.Characters.Player;
import edu.uic.cs440.group1.dungeon_crafter.R;

public class LobbyActivity extends AppCompatActivity {

    private final int NO_SELECTION = -1,
            HUMAN_RACE = 101, DWARF_RACE = 102, ELF_RACE = 103, GNOME_RACE = 104,
            FIGHTER_CLASS = 201, PALADIN_CLASS = 202;
    private Player player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        Button findGroupButton = (Button) findViewById(R.id.lobbyFindGroupButton);
        Button viewStatsButton = (Button) findViewById(R.id.lobbyStatsButton);
        Button logoutButton = (Button) findViewById(R.id.lobbyLogOutButton);

        findGroupButton.setOnClickListener(findGroupButtonHandler);
        viewStatsButton.setOnClickListener(viewStatsButtonHandler);
        logoutButton.setOnClickListener(logoutButtonHandler);
        new Thread(new GetUserData()).start();

    }

    private View.OnClickListener findGroupButtonHandler = new View.OnClickListener() {
        @Override
        public void onClick(View view)
        {
            Intent startFindGroupIntent = new Intent(LobbyActivity.this, FindGroupActivity.class);
            startActivity(startFindGroupIntent);
        }
    };

    private View.OnClickListener viewStatsButtonHandler = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent openStatsIntent = new Intent(LobbyActivity.this, StatisticsActivity.class);
            startActivity(openStatsIntent);
//            finish();
        }
    };

    private View.OnClickListener logoutButtonHandler = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            startActivity(new Intent(LobbyActivity.this, LoginActivity.class));
            finish();
        }
    };

    // ------------------------------------------------------------------------
    // Thread classes
    // ------------------------------------------------------------------------
    class GetUserData implements Runnable {

        @Override
        public void run() {

            String sessionID = getSharedPreferences(getString(R.string.user_info_file), MODE_PRIVATE).getString(getString(R.string.session_id_key), null);

            Log.i("LobbyActivity", "SessionID for user: " + sessionID);

            String url = "http://alexviznytsya.me/dungeoncrafter/getuserinfo.php";

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("sessionid",  sessionID);
            } catch (Exception e) {
                e.printStackTrace();
            }

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
                writer.write(jsonObject.toString());
                writer.close();
                outputStream.close();

                //Read
                BufferedReader bufferedReader = new BufferedReader
                        (new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));

                String line = null;
                StringBuilder sb = new StringBuilder();

                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                }

                bufferedReader.close();
                Log.i("Lobby Activity",  "Returned JSON: " + sb.toString());

                try {
                    processResponse(new JSONObject(sb.toString()));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void processResponse(JSONObject response) {
            String playerName = "";
            int playerId = -1;
            int playerRace = -1;
            int playerClass = -1;
            int playerXP = -1;
            int playerLevel = -1;
            int playerIntelligence = -1;
            int playerHealth = -1;
            try {
                playerName =  response.getString("playerName");
                playerRace = response.getInt("playerRace");
                playerClass = response.getInt("playerClass");
                playerXP = response.getInt("playerXP");
                playerLevel = response.getInt("playerLevel");
                playerId = response.getInt("playerId");
                playerIntelligence = response.getInt("playerIntelligence");
                playerHealth = response.getInt("playerHealth");

                // save to preferences
                SharedPreferences.Editor editor = getSharedPreferences(
                        getString(R.string.user_info_file), MODE_PRIVATE).edit();
                editor.putString(getString(R.string.playerName), playerName);
                editor.putInt(getString(R.string.playerRace), playerRace);
                editor.putInt(getString(R.string.playerClass), playerClass);
                editor.putInt(getString(R.string.playerXP), playerXP);
                editor.putInt(getString(R.string.playerLevel), playerLevel);
                editor.putInt(getString(R.string.playerId), playerId);
                editor.putInt(getString(R.string.playerIntelligence), playerIntelligence);
                editor.putInt(getString(R.string.playerHealth), playerHealth);
                //FIXME: get strength from the database
                editor.putInt(getString(R.string.playerStrength), 15);
                editor.apply();

            } catch (Exception ex) {
                ex.printStackTrace();
            }

            ((TextView)findViewById(R.id.playerName)).setText(playerName.toString());


            if (playerRace == HUMAN_RACE)
                ((ImageView)findViewById(R.id.characterPicture)).setImageResource(R.drawable.human_race);
            else if (playerRace == DWARF_RACE)
                ((ImageView)findViewById(R.id.characterPicture)).setImageResource(R.drawable.dwarf_race);
            else if (playerRace == ELF_RACE)
                ((ImageView)findViewById(R.id.characterPicture)).setImageResource(R.drawable.elf_race);
            else if (playerRace == GNOME_RACE)
                ((ImageView)findViewById(R.id.characterPicture)).setImageResource(R.drawable.gnome_race);
            else
                ((ImageView)findViewById(R.id.characterPicture)).setImageResource(R.drawable.empty_race);

            ((TextView)findViewById(R.id.playerXPLabel)).setText("XP: " + Integer.toString(playerXP));
            ((TextView)findViewById(R.id.playerLevelLabel)).setText("Level: " + Integer.toString(playerLevel));

        }

    }   // GetUserData


}


