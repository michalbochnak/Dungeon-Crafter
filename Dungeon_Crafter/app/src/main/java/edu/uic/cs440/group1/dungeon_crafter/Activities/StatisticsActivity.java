package edu.uic.cs440.group1.dungeon_crafter.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import edu.uic.cs440.group1.dungeon_crafter.R;

public class StatisticsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        createStatsitics();
        Button returnToLobby = findViewById(R.id.statisticsReturnToLobbyButton);
        returnToLobby.setOnClickListener(returnToLobbyHandler);
        Button logoutButton = findViewById(R.id.stattisticsLogOutbutton);
        logoutButton.setOnClickListener(logoutButtonHandler);

    }

    private View.OnClickListener returnToLobbyHandler = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            finish();
        }
    };

    private View.OnClickListener logoutButtonHandler = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            startActivity(new Intent(StatisticsActivity.this, LoginActivity.class));
            finish();
        }
    };


    private void createStatsitics(){
        int pHealth = getSharedPreferences(getString(R.string.user_info_file), MODE_PRIVATE).getInt(getString(R.string.playerHealth), -1);
        int pStrength = getSharedPreferences(getString(R.string.user_info_file), MODE_PRIVATE).getInt(getString(R.string.playerStrength), -1);
        int pExperience = getSharedPreferences(getString(R.string.user_info_file), MODE_PRIVATE).getInt(getString(R.string.playerXP), -1);



        ((TextView) findViewById(R.id.expierienceTxtBox)).setText(Integer.toString(pExperience));
        ((TextView) findViewById(R.id.strengthTxtBox)).setText(Integer.toString(pStrength));
        ((TextView) findViewById(R.id.healthTxtBox)).setText(Integer.toString(pHealth));
    }


}
