package edu.uic.cs440.group1.dungeon_crafter.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import edu.uic.cs440.group1.dungeon_crafter.R;

public class PostGameStatisticsActivity extends AppCompatActivity {

    private TextView damageDealtTextView;
    private TextView numMovesTextView;
    private TextView finalHealthTextView;
    private TextView numAttacksLabel;
    private TextView strengthLabel;
    private TextView totalXpLabel;
    private TextView maxHealthLabel;
    private TextView increaseStrengthLabel;
    private TextView increaseTotalXpLabel;
    private TextView increaseMaxHealthLabel;

    private int currentStrength;
    private int currentMaxHealth;
    private int currentTotalXP;
    private int damageDealt;
    private int numMoves;
    private int finalHealth;
    private int numAttacks;
    private boolean gameWon;

    public final static String DAMAGE_DEALT_EXTRA = "Damage Dealt Extra";
    public final static String FINAL_HEALTH_EXTRA = "Final Health Extra";
    public final static String NUMBER_OF_ATTACKS_EXTRA = "Number Of Attacks Extra";
    public final static String NUMBER_OF_MOVES_MADE_EXTRA = "Damage Received Extra";
    public final static String GAME_WON_EXTRA = "Game Won";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_game_statistics);

        Button returnToLobby = findViewById(R.id.statisticsReturnToLobbyButton);
        returnToLobby.setOnClickListener(returnToLobbyHandler);

        Button logoutButton = findViewById(R.id.stattisticsLogOutbutton);
        logoutButton.setOnClickListener(logoutButtonHandler);

        //Find TextViews
        damageDealtTextView = (TextView) findViewById(R.id.damageDealtLabel);
        numMovesTextView = (TextView) findViewById(R.id.numMovesLabel);
        finalHealthTextView = (TextView) findViewById(R.id.finalHealthLabel);
        numAttacksLabel = (TextView) findViewById(R.id.numAttacksLabel);
        strengthLabel = (TextView) findViewById(R.id.strengthLabel);
        totalXpLabel = (TextView) findViewById(R.id.totalXpLabel);
        maxHealthLabel = (TextView) findViewById(R.id.maxHealthLabel);
        increaseMaxHealthLabel = (TextView) findViewById(R.id.increaseMaxHealthLabel);
        increaseTotalXpLabel = (TextView) findViewById(R.id.increaseTotalXpLabel);
        increaseStrengthLabel = (TextView) findViewById(R.id.increaseStrengthLabel);

        gameWon = false;

        unpackBundle();
        setPlayersCurrentStats();
        setStatisticsLabels();

        if (gameWon)
            increasePlayersStats();
        else
            hideIncreaseStatsLabels();
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
            startActivity(new Intent(PostGameStatisticsActivity.this, LoginActivity.class));
            finish();
        }
    };

    private void unpackBundle() {
        Intent intent = getIntent();
        Bundle data;

        if (intent != null && (data = intent.getExtras()) != null) {
            damageDealt = data.getInt(DAMAGE_DEALT_EXTRA, 0);
            numMoves = data.getInt(NUMBER_OF_MOVES_MADE_EXTRA, 0);
            finalHealth = data.getInt(FINAL_HEALTH_EXTRA, 100);
            numAttacks = data.getInt(NUMBER_OF_ATTACKS_EXTRA, 0);
            gameWon = data.getBoolean(GAME_WON_EXTRA, false);
        }
    }

    private void setStatisticsLabels() {
        //Set "Adventure Stats" labels
        damageDealtTextView.setText(Integer.toString(damageDealt));
        numMovesTextView.setText(Integer.toString(numMoves));
        finalHealthTextView.setText(Integer.toString(finalHealth));
        numAttacksLabel.setText(Integer.toString(numAttacks));


        //Set "Experience Gained" labels
        strengthLabel.setText(Integer.toString(currentStrength));
        maxHealthLabel.setText(Integer.toString(currentMaxHealth));
        totalXpLabel.setText(Integer.toString(currentTotalXP));
    }

    private void setPlayersCurrentStats() {
        currentMaxHealth = getSharedPreferences(getString(R.string.user_info_file), MODE_PRIVATE)
                .getInt(getString(R.string.playerHealth), 100);
        currentStrength = getSharedPreferences(getString(R.string.user_info_file), MODE_PRIVATE)
                .getInt(getString(R.string.playerStrength), 15);
        currentTotalXP = getSharedPreferences(getString(R.string.user_info_file), MODE_PRIVATE)
                .getInt(getString(R.string.playerXP), 100);
    }

    private void increasePlayersStats() {
        //Update stats
        int updatedStrength = currentStrength + 1;
        int updatedMaxHealth = currentMaxHealth + 3;
        int updatedTotalXP = currentTotalXP + 15;

        // Save updated player stats to preferences
        SharedPreferences.Editor editor = getSharedPreferences(
                getString(R.string.user_info_file), MODE_PRIVATE).edit();
        editor.putInt(getString(R.string.playerStrength), updatedStrength);
        editor.putInt(getString(R.string.playerHealth), updatedMaxHealth);
        editor.putInt(getString(R.string.playerXP), updatedTotalXP);
        editor.apply();
    }

    private void hideIncreaseStatsLabels()
    {
        increaseStrengthLabel.setVisibility(View.INVISIBLE);
        increaseTotalXpLabel.setVisibility(View.INVISIBLE);
        increaseMaxHealthLabel.setVisibility(View.INVISIBLE);
    }
}
