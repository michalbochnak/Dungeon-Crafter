/*
    CS 440 Software Engineering
    University of Illinois Chicago Spring 2018

    *********************************************
    *                                           *
    *     Group 1                               *
    *                                           *
    *     Michal Bochnak   netID:  mbochn2      *
    *     Sean Martinelli  netID:  smarti58     *
    *     Alex Viznytsya   netID:  avizny2      *
    *     Artur Wojcik     netID:  awojci5      *
    *                                           *
    *********************************************


    Dungeon Crafter will allow users to join groups of other players to
    participate in adventures. These adventures will consist of a series
    of levels that will each provide a unique setting with different enemies
    that the group must defeat. This scenario will define how the user will
    interact with the system in order to begin playing an adventure. This
    includes the ability to create an account, enter the main lobby, and j
    oin a group.

    Based on Project report:

    Group 12: Jay Dave, Dominick Rauba, Vincent Orea, Garret Felker
    University of Illinois Chicago Fall 2014

 */


/*
 CharacterCreationActivity.java

 Allows user to choose specifications of his character
*/


package edu.uic.cs440.group1.dungeon_crafter.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

import edu.uic.cs440.group1.dungeon_crafter.R;

public class CharacterCreationActivity extends AppCompatActivity {

    private final int NO_SELECTION = -1,
            HUMAN_RACE = 101, DWARF_RACE = 102, ELF_RACE = 103, GNOME_RACE = 104,
            FIGHTER_CLASS = 201, RANGER_CLASS = 202;
    private String characterName = "";
    private int raceID = NO_SELECTION, classID = NO_SELECTION;
    private final int SESSION_ID_WRONG = 0, SESSION_ID_OK = 1;


    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_character_creation);

        addDoneButtonListener();
        addRacesAndClassesButtonsListeners();
    }

    private String readInCharacterName() {
        return ((EditText)findViewById(R.id.characterNameEditText)).getText().toString();
    }

    private void addDoneButtonListener() {
        findViewById(R.id.doneButton).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            collectData();
            processData();
            displayData();
            }
        });
    }

    private void addRacesAndClassesButtonsListeners() {
        findViewById(R.id.humanButton).setOnClickListener(new RacesAndClassesButtonsActionListener());
        findViewById(R.id.lobbyStatsButton).setOnClickListener(new RacesAndClassesButtonsActionListener());
        findViewById(R.id.lobbyLogOutButton).setOnClickListener(new RacesAndClassesButtonsActionListener());
        findViewById(R.id.gnomeButton).setOnClickListener(new RacesAndClassesButtonsActionListener());
        findViewById(R.id.fighterButton).setOnClickListener(new RacesAndClassesButtonsActionListener());
        findViewById(R.id.rangerButton).setOnClickListener(new RacesAndClassesButtonsActionListener());
    }

    private void collectData() {
        characterName = readInCharacterName();
    }

    private void processData() {
        if (dataCorrect()) {
            Log.i("CharacterCreationA",
                    "About to start the thread....");
            new Thread(new UpdateCharacterThread()).start();
        }
    }

    private boolean dataCorrect() {

        if (!characterNameCorrect()) {
            Toast.makeText(getApplicationContext(), "Character name incorrect.\n" +
                            "Use only a-z letters,\n" +
                            "must be between 2-20 letters",
                    Toast.LENGTH_LONG).show();
            return false;
        }
        else if (!raceSelected()) {
            Toast.makeText(getApplicationContext(), "Select race.",
                    Toast.LENGTH_LONG).show();
            return false;
        }
        else if (!classSelected()) {
            Toast.makeText(getApplicationContext(), "Select class.",
                    Toast.LENGTH_LONG).show();
            return false;
        }
        else
            return true;
    }

    private boolean raceSelected() {
        return raceID != NO_SELECTION;
    }

    private boolean classSelected() {
        return classID != NO_SELECTION;
    }

    private boolean characterNameCorrect() {
        // check if only letters were user ( no : , ? * etc)
        characterName.trim();
        if (characterName.matches("[a-zA-Z]+")  && characterName.length() < 21
                && characterName.length() > 1)
            return true;
        else
            return false;
    }



    // FIXME: Debug
    private void displayData() {
        Log.i("CharacterCreationActi: ", "CharacterName: " + characterName );
        Log.i("CharacterCreationActi: ", "raceID: " + raceID);
        Log.i("CharacterCreationActi: ", "classID: " + classID);
        Log.i("CharacterCreationActi: ", "character Name Correct: " + characterNameCorrect());

    }

    private void setAllButtonsTextColor(int color) {
        setRaceButtonsTextColor(color);
        setClassButtonsTextColor(color);
    }

    private void setRaceButtonsTextColor(int color) {
        ((Button)findViewById(R.id.humanButton)).setTextColor(color);
        ((Button)findViewById(R.id.lobbyStatsButton)).setTextColor(color);
        ((Button)findViewById(R.id.lobbyLogOutButton)).setTextColor(color);
        ((Button)findViewById(R.id.gnomeButton)).setTextColor(color);
    }

    private void setClassButtonsTextColor(int color) {
        ((Button)findViewById(R.id.fighterButton)).setTextColor(color);
        ((Button)findViewById(R.id.rangerButton)).setTextColor(color);
    }

    private void updateCharacterLabel() {
        if (raceID == HUMAN_RACE)
            ((ImageView)findViewById(R.id.characterImageView)).setImageResource(R.drawable.human_race);
        else if (raceID == DWARF_RACE)
            ((ImageView)findViewById(R.id.characterImageView)).setImageResource(R.drawable.dwarf_race);
        else if (raceID == ELF_RACE)
            ((ImageView)findViewById(R.id.characterImageView)).setImageResource(R.drawable.elf_race);
        else if (raceID == GNOME_RACE)
            ((ImageView)findViewById(R.id.characterImageView)).setImageResource(R.drawable.gnome_race);
        else
            ((ImageView)findViewById(R.id.characterImageView)).setImageResource(R.drawable.empty_race);
    }

    private void launchLobbyActivity() {
        startActivity(new Intent(this, LobbyActivity.class));
    }


    // ------------------------------------------------------------------------
    // Listener classes
    // ------------------------------------------------------------------------
    class RacesAndClassesButtonsActionListener implements View.OnClickListener {
        public void onClick(View v) {

            if (v.getId() == R.id.humanButton) {
                setRaceButtonsTextColor(Color.BLACK);
                raceID = HUMAN_RACE;
            }
            else if (v.getId() == R.id.lobbyStatsButton) {
                setRaceButtonsTextColor(Color.BLACK);
                raceID = DWARF_RACE;
            }
            else if (v.getId() == R.id.lobbyLogOutButton) {
                setRaceButtonsTextColor(Color.BLACK);
                raceID = ELF_RACE;
            }
            else if (v.getId() == R.id.gnomeButton) {
                setRaceButtonsTextColor(Color.BLACK);
                raceID = GNOME_RACE;
            }
            else if (v.getId() == R.id.fighterButton) {
                setClassButtonsTextColor(Color.BLACK);
                 classID = FIGHTER_CLASS;
            }
            else if (v.getId() == R.id.rangerButton) {
                setClassButtonsTextColor(Color.BLACK);
                classID = RANGER_CLASS;
            }

            updateCharacterLabel();

            ((Button)v).setTextColor(Color.RED);
        }
    }



    // ------------------------------------------------------------------------
    // Thread classes
    // ------------------------------------------------------------------------
    class UpdateCharacterThread implements Runnable {

        @Override
        public void run() {

            String url = "http://alexviznytsya.me/dungeoncrafter/updatecharacter.php";
            JSONObject jsonObject = new JSONObject();
            try {
                SharedPreferences sp = getSharedPreferences(
                        getString(R.string.user_info_file), MODE_PRIVATE);

                Log.i("CharacterCreationA",
                        "id from sp: " + sp.getString(getString(R.string.session_id_key), null));

                jsonObject.put(getString(R.string.session_id_key),  sp.getString(
                        getString(R.string.session_id_key), null));
                jsonObject.put("name",  characterName);
                jsonObject.put("race", raceID);
                jsonObject.put("class", classID);
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
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));

                String line = null;
                StringBuilder sb = new StringBuilder();

                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                }

                bufferedReader.close();
                Log.i("Returned JSON: ", sb.toString());

                try {
                    launchLobbyActivity();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private int extractStatus(JSONObject response) {
            int status = -1;
            try {
                status =  Integer.parseInt(response.getString("status"));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return status;
        }

        private String extractSessionId(JSONObject response) {
            String status = "";
            try {
                status =  response.getString("status");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return status;
        }

    }   // RegisterOrLoginUserInDbThread


}   // end of CharacterCreationActivity class
