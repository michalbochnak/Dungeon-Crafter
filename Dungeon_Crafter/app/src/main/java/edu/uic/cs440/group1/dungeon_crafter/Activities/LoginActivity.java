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
 Login Activity.java

 Allows user to register or login.
*/


/*
Notes:


 */

package edu.uic.cs440.group1.dungeon_crafter.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
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

public class LoginActivity extends AppCompatActivity {

    private String email = "", password = "";
    // enums to handle server response
    private final int LOGIN_FAIL = 0,
        LOGIN_SUCCESS = 1,
        REGISTER_FAIL = 2,
        REGISTER_SUCCESS = 3,
        SIGNUP = 100,
        LOGIN = 101;

    private int ACTION = SIGNUP;

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
        //open in full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
                */
        setContentView(R.layout.activity_login);

        findViewById(R.id.loginButton).setOnClickListener(new loginListener());
        findViewById(R.id.signUpButton).setOnClickListener(new signUpListener());
    }

    private void collectCredentials() {
        email = ((EditText) findViewById(R.id.emailEditText)).getText().toString();
        password = ((EditText) findViewById(R.id.passwordEditText)).getText().toString();
    }

    private void launchCharacterCreationActivity() {
        startActivity(new Intent(this, CharacterCreationActivity.class));
    }

    // FIXME:
    private boolean credentialsCorrect() {
        // check:
        //  -if fields are not empty
        //  -if email format is correct
        if (email.length() > 0 && password.length() > 0)
            return true;
        else
            return false;
    }

    //FIXME:
    private void registerOrLoginUserInDb() {
        new Thread(new RegisterOrLoginUserInDbThread()).start();
    }

    private void launchLobbyActivity() {
            startActivity(new Intent(this, LobbyActivity.class));
    }

    // ------------------------------------------------------------------------
    // Listener classes
    // ------------------------------------------------------------------------

    // FIXME:
    class loginListener implements View.OnClickListener {
        public void onClick(View view) {
            collectCredentials();
            ACTION = LOGIN;
            if (credentialsCorrect()) {
                registerOrLoginUserInDb();
            }
            else {
                showToastMessageLong("Please provide email and password.");
            }
        }
    }

    // FIXME:
    class signUpListener implements View.OnClickListener {
        public void onClick(View v) {
            collectCredentials();
            ACTION = SIGNUP;
            if (credentialsCorrect()) {
                registerOrLoginUserInDb();
            }
            else {
                showToastMessageLong("Please provide email and password.");
            }
        }
    }

    private void showToastMessageLong(final String msg) {
        Log.i("LoginActivity: ", "kurwaaaa");
        Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show();
    }



    // ------------------------------------------------------------------------
    // Thread classes
    // ------------------------------------------------------------------------
    class RegisterOrLoginUserInDbThread implements Runnable {

        @Override
        public void run() {

            Log.i("LoginActivity: ", "Signing up user: " + email + ", " + password);

            String url = "";

            if (ACTION == LOGIN)
                url = "http://alexviznytsya.me/dungeoncrafter/login.php";
            else if (ACTION == SIGNUP)
                url = "http://alexviznytsya.me/dungeoncrafter/signup.php";

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("email",  email);
                jsonObject.put("password", password);
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
                Log.i("Returned JSON: ", sb.toString());

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
            int status = extractStatus(response);

            SharedPreferences.Editor editor = getSharedPreferences(
                    getString(R.string.user_info_file), MODE_PRIVATE).edit();

            editor.putString(getString(R.string.session_id_key), extractSessionId(response));
            editor.apply();

            Log.i("LoginActivity: ", response.toString());

            if (status == LOGIN_SUCCESS) {
                launchLobbyActivity();
            }
            else if (status == LOGIN_FAIL) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        showToastMessageLong("Invalid credentials.");
                    }
                });
            }
            else if (status == REGISTER_SUCCESS) {
                launchCharacterCreationActivity();
            }
            else if (status == REGISTER_FAIL) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        showToastMessageLong("User already exist.");
                    }
                });
            }
            else {
                showToastMessageLong( "Error occurred.");
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
                status =  response.getString("sessionid");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return status;
        }

    }   // RegisterOrLoginUserInDbThread


}   // end of LoginActivity class
