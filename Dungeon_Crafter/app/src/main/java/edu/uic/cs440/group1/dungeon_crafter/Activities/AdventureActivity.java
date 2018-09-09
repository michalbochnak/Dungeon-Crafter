package edu.uic.cs440.group1.dungeon_crafter.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import edu.uic.cs440.group1.dungeon_crafter.Utilities.*;
import edu.uic.cs440.group1.dungeon_crafter.Characters.*;
import edu.uic.cs440.group1.dungeon_crafter.Characters.Character;
import edu.uic.cs440.group1.dungeon_crafter.Models.*;
import edu.uic.cs440.group1.dungeon_crafter.R;
import edu.uic.cs440.group1.dungeon_crafter.Threads.*;

public class AdventureActivity extends AppCompatActivity {

    private static final String TAG = "AdventureActivity";
    private GridLayout gameGrid;

    // "ME" - creature that belongs to "this" device
    // and is controlled by user
    private Player player;

    private Vector<Player> groupMembers;
    private Vector<Enemy> enemies;
    private TextView rollTxtView;
    private int remainingMoves = 0;
    private final int NUM_COLS = 17;    //old value: 22
    private final int NUM_ROWS = 12;    //old value: 17

    private TextView playerOneName_TextView, playerTwoName_TextView,
            playerThreeName_TextView, playerFourName_TextView;
    private TextView playerOneHealth_TextView, playerTwoHealth_TextView,
            playerThreeHealth_TextView, playerFourHealth_TextView;
    private ProgressBar playerOneHealth_ProgressBar, playerTwoHealth_ProgressBar,
            playerThreeHealth_ProgressBar, playerFourHealth_ProgressBar;
    private TextView enemyOneName_TextView, enemyTwoName_TextView;
    private TextView enemyOneHealth_TextView, enemyTwoHealth_TextView;
    private ProgressBar enemyOneHealth_ProgressBar, enemyTwoHealth_ProgressBar;
    private ImageView playerOneTurn_ImageView, playerTwoTurn_ImageView,
            playerThreeTurn_ImageView, playerFourTurn_ImageView;
    private TextView playerOneType_TextView, playerTwoType_TextView,
            playerThreeType_TextView, playerFourType_TextView;
    private ImageButton attackButton;

    private boolean moveComplete;
    private boolean hasRolled;
    private boolean flagsHaveBeenReset;
    private boolean attackButtonShouldAttack;

    private UpdateGameplay updateGameplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("AdventureActiv: ", "OnCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adventure);
        groupMembers = new Vector<>();
        moveComplete = false;
        hasRolled = false;
        flagsHaveBeenReset = false;
        attackButtonShouldAttack = true;
        enemies = new Vector<Enemy>();

        findCharacterLabels();
        setUpGrid();
        updateGrid();
        setupButtons();
        setControls(false);
        updateHealthLabels();
        updateTurnLabels();
        setPlayerNameLabels();

        updateGameplay = new UpdateGameplay();
        new Thread(updateGameplay).start();
    }

    private View.OnClickListener upButtonListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            Position currentPosition = player.getPosition();
            Position newPosition = new Position(currentPosition.getRow() - 1,
                    currentPosition.getColumn());

            if(validateNewPosition(newPosition) && moveAvailable()) {
                makeMove(newPosition);
                updateGameplay.updateDb();
            }
        }
    };

    private View.OnClickListener downButtonListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            Position currentPosition = player.getPosition();
            Position newPosition = new Position(currentPosition.getRow() + 1,
                    currentPosition.getColumn());

            if(validateNewPosition(newPosition) && moveAvailable()) {
                makeMove(newPosition);
                updateGameplay.updateDb();
            }
        }
    };

    private View.OnClickListener leftButtonListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            Position currentPosition = player.getPosition();
            Position newPosition = new Position(currentPosition.getRow(),
                    currentPosition.getColumn() - 1);

            if(validateNewPosition(newPosition) && moveAvailable()) {
                makeMove(newPosition);
                updateGameplay.updateDb();
            }
        }
    };

    private View.OnClickListener rightButtonListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            Position currentPosition = player.getPosition();
            Position newPosition = new Position(currentPosition.getRow(),
                    currentPosition.getColumn() + 1);

            if(validateNewPosition(newPosition) && moveAvailable()) {
                makeMove(newPosition);
                updateGameplay.updateDb();
            }
        }
    };

    private View.OnClickListener rollDiceButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(!hasRolled) {
                Random rand = new Random();
                remainingMoves = rand.nextInt(6) + 1;
                rollTxtView.setText(Integer.toString(remainingMoves));
                hasRolled = true;
            }
        }
    };

    //FIXME
    private View.OnClickListener attackButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view)
        {
            if(attackButtonShouldAttack)
                attack();
            else
                endTurn();

        }
    };

    private void endTurn()
    {
        //Switch the attack button image back to the attack button
        attackButton.setImageResource(R.drawable.attack_button);
        attackButton.invalidate();

        enemyAttack();

        updateHealthLabels();
        moveAllEnemies();
        moveComplete = true;
        flagsHaveBeenReset = false;
        updateGameplay.updateDb();
        updateGrid();

        // turn off controls
        setControls(false);

        attackButtonShouldAttack = true;
    }

    private void attack()
    {
        ArrayList<Pair<String,Integer>> damageDealt = new ArrayList<Pair<String,Integer>>();

        //Find and attack all enemies within range
        for(Enemy enemy : enemies)
        {
            int range = player.getAttackRange();

            //Attack the enemy if they are within range
            if (player.getPosition().isWithinRange(enemy.getPosition(), range))
            {
                int damageToDeal = determineDamageDealt(player);

                //Attack
                player.dealDamage(damageToDeal);
                enemy.receiveDamage(damageToDeal);

                //Add the damage dealt to each enemy to an array list to be displayed
                if(enemy instanceof Troll)
                    damageDealt.add(new Pair<String, Integer>("Troll", damageToDeal));
                else if(enemy instanceof Ogre)
                    damageDealt.add(new Pair<String, Integer>("Ogre", damageToDeal));
            }

            //if the enemy is dead move them of the grid.
            if(enemy.getHealth() <= 0)
                enemy.setPosition(new Position(-10000, -10000));
        }

        //Switch the attack button image to the done button
        attackButton.setImageResource(R.drawable.done_button);
        attackButton.invalidate();

        attackButtonShouldAttack = false;

        updateHealthLabels();
        updateGrid();
        displayDamageDealt(damageDealt);

        if(isGameOver())
            endTurn();
    }

    private void displayDamageDealt(ArrayList<Pair<String,Integer>> damageDealt)
    {
        if(damageDealt.isEmpty())
            return;

        StringBuilder messageToDisplay = new StringBuilder("Damage dealt:");

        for(int i=0; i < damageDealt.size(); ++i)
        {
            if(i > 0)
                messageToDisplay.append(" ||");

            messageToDisplay.append(" ");
            messageToDisplay.append(damageDealt.get(i).first);
            messageToDisplay.append(" -> ");
            messageToDisplay.append(damageDealt.get(i).second);
        }

        Toast.makeText(this, messageToDisplay, Toast.LENGTH_LONG).show();
    }

    private int determineDamageDealt(Character character)
    {
        // Set upper and lower limit based on character's strength
        int upperLimit = character.getStrength();
        int lowerLimit = character.getStrength() - 5;

        //make sure the lower limit is not negative
        if(lowerLimit < 0)
            lowerLimit = 0;

        //Generate a random number between the upper and lower limits
        Random rand = new Random();
        return rand.nextInt(upperLimit - lowerLimit) + lowerLimit;
    }

    private void enemyAttack()
    {
        //Find and attack all players within range for each enemy
        for(Enemy enemy : enemies) {
            for (Player player : groupMembers)
            {
                int range = enemy.getAttackRange();

                //Attack the player if they are within range
                if (enemy.getPosition().isWithinRange(player.getPosition(), range))
                    player.receiveDamage(determineDamageDealt(enemy));

                //if the player is dead move them of the grid.
                if(player.getHealth() <= 0)
                    player.setPosition(new Position(10000, 10000));
            }
        }
    }

    private void moveAllEnemies()
    {
        for(Enemy enemy : enemies)
            moveEnemy(enemy);
    }

    private void moveEnemy(Enemy enemy)
    {
        if(groupMembers == null || enemy == null || groupMembers.isEmpty())
            return;

        Player closestPlayer = findClosestPlayerToEnemy(enemy);
        Position positionOfClosestPlayer = closestPlayer.getPosition();

        //Get all surrounding positions and
        ArrayList<Position> surroundingPositions = enemy.getPosition().getSurroundingPositions();

        //Check all surrounding positions to see if any are better than the current position
        Position bestNewPosition = enemy.getPosition();
        for(Position position : surroundingPositions)
        {
            if(position.calculateDistanceTo(positionOfClosestPlayer) <
                    bestNewPosition.calculateDistanceTo(positionOfClosestPlayer))
                bestNewPosition = position;
        }

        //Move the enemy if the new position is valid
        if(validateNewPosition(bestNewPosition))
            enemy.setPosition(bestNewPosition);
    }

    private Player findClosestPlayerToEnemy(Enemy enemy)
    {
        Position enemyPosition = enemy.getPosition();
        Player closestPlayer = groupMembers.get(0);

        for(Player player : groupMembers)
        {
            Position playerPosition = player.getPosition();
            Position closestPlayerPosition = closestPlayer.getPosition();

            if(enemyPosition.calculateDistanceTo(playerPosition) <
                    enemyPosition.calculateDistanceTo(closestPlayerPosition)) {
                closestPlayer = player;
            }
        }

        return closestPlayer;
    }

    private void findCharacterLabels()
    {
        //Find player name TextViews
        playerOneName_TextView = (TextView) findViewById(R.id.playerOneName);
        playerTwoName_TextView = (TextView) findViewById(R.id.playerTwoName);
        playerThreeName_TextView = (TextView) findViewById(R.id.playerThreeName);
        playerFourName_TextView = (TextView) findViewById(R.id.playerFourName);

        //Find enemy name TextViews
        enemyOneName_TextView = (TextView) findViewById(R.id.enemyOneName);
        enemyTwoName_TextView = (TextView) findViewById(R.id.enemyTwoName);

        //Find player health TextViews
        playerOneHealth_TextView = (TextView) findViewById(R.id.playerOneHealth);
        playerTwoHealth_TextView = (TextView) findViewById(R.id.playerTwoHealth);
        playerThreeHealth_TextView = (TextView) findViewById(R.id.playerThreeHealth);
        playerFourHealth_TextView = (TextView) findViewById(R.id.playerFourHealth);

        //Find player health Progress Bars
        playerOneHealth_ProgressBar = (ProgressBar) findViewById(R.id.playerOneHealthBar);
        playerTwoHealth_ProgressBar = (ProgressBar) findViewById(R.id.playerTwoHealthBar);
        playerThreeHealth_ProgressBar = (ProgressBar) findViewById(R.id.playerThreeHealthBar);
        playerFourHealth_ProgressBar = (ProgressBar) findViewById(R.id.playerFourHealthBar);

        //Find enemy health TextViews
        enemyOneHealth_TextView = (TextView) findViewById(R.id.enemyOneHealth);
        enemyTwoHealth_TextView = (TextView) findViewById(R.id.enemyTwoHealth);

        //Find enemy health Progress Bars
        enemyOneHealth_ProgressBar = (ProgressBar) findViewById(R.id.enemyOneHealthBar);
        enemyTwoHealth_ProgressBar = (ProgressBar) findViewById(R.id.enemyTwoHealthBar);

        //Find player turn Images
        playerOneTurn_ImageView = (ImageView) findViewById(R.id.playerOneTurnImage);
        playerTwoTurn_ImageView = (ImageView) findViewById(R.id.playerTwoTurnImage);
        playerThreeTurn_ImageView = (ImageView) findViewById(R.id.playerThreeTurnImage);
        playerFourTurn_ImageView = (ImageView) findViewById(R.id.playerFourTurnImage);

        //Find player type TextViews
        playerOneType_TextView = (TextView) findViewById(R.id.playerOneTypeLabel);
        playerTwoType_TextView = (TextView) findViewById(R.id.playerTwoTypeLabel);
        playerThreeType_TextView = (TextView) findViewById(R.id.playerThreeTypeLabel);
        playerFourType_TextView = (TextView) findViewById(R.id.playerFourTypeLabel);

    }

    private void updateHealthLabels()
    {
        if(player == null)
            return;

        ArrayList<Character> allCharacters = new ArrayList<Character>();
        allCharacters.add(player);
        allCharacters.addAll(groupMembers);
        allCharacters.addAll(enemies);


        //Loop through all characters to update their health views
        for(Character character : allCharacters)
        {
            TextView healthTextView;
            ProgressBar healthBar;
            int playerNumber = character.getPlayerNumber();

            switch (playerNumber)
            {
                case (1):
                    healthTextView = playerOneHealth_TextView;
                    healthBar = playerOneHealth_ProgressBar;
                    break;
                case (2):
                    healthTextView = playerTwoHealth_TextView;
                    healthBar = playerTwoHealth_ProgressBar;
                    break;
                case (3):
                    healthTextView = playerThreeHealth_TextView;
                    healthBar = playerThreeHealth_ProgressBar;
                    break;
                case (4):
                    healthTextView = playerFourHealth_TextView;
                    healthBar = playerFourHealth_ProgressBar;
                    break;
                case (-1):
                    healthTextView = enemyOneHealth_TextView;
                    healthBar = enemyOneHealth_ProgressBar;
                    break;
                case (-2):
                    healthTextView = enemyTwoHealth_TextView;
                    healthBar = enemyTwoHealth_ProgressBar;
                    break;
                default:
                    continue; //This player has an invalid ID so skip them.
            }

            //Update health TextView
            String healthTextViewString = character.getHealth() + "/100";
            healthTextView.setText(healthTextViewString);

            //Update health bar
            healthBar.setProgress(character.getHealth());
        }
    }

    private void updateTurnLabels()
    {
        ArrayList<Character> allPlayers = new ArrayList<>();
        allPlayers.add(player);
        allPlayers.addAll(groupMembers);

        Character currentTurnPlayer = null;

        //Hide all labels
        playerOneTurn_ImageView.setVisibility(View.INVISIBLE);
        playerTwoTurn_ImageView.setVisibility(View.INVISIBLE);
        playerThreeTurn_ImageView.setVisibility(View.INVISIBLE);
        playerFourTurn_ImageView.setVisibility(View.INVISIBLE);

        if(player == null)
            return;

        //Find out which player's turn it is
        for(Character character : allPlayers)
            if(character.isTurn())
                currentTurnPlayer = character;

        //If the player is dead don't update their label
        if(currentTurnPlayer == null || currentTurnPlayer.getHealth() <= 0)
            return;

        //Display only the label of the player whose turn it is
        switch (currentTurnPlayer.getPlayerNumber())
        {
            case (1):
                playerOneTurn_ImageView.setVisibility(View.VISIBLE);
                break;
            case (2):
                playerTwoTurn_ImageView.setVisibility(View.VISIBLE);
                break;
            case (3):
                playerThreeTurn_ImageView.setVisibility(View.VISIBLE);
                break;
            case(4):
                playerFourTurn_ImageView.setVisibility(View.VISIBLE);
                break;
        }

    }

    private synchronized void setPlayerNameLabels()
    {
        //Set player name labels
        for(Player player : groupMembers)
        {
            if(player == null)
                continue;

            //Determine which label to update based on player number
            switch (player.getPlayerNumber())
            {
                case (1):
                    playerOneName_TextView.setText(player.getName());
                    playerOneType_TextView.setText(determineFighterOrRanger(player));
                    break;
                case (2):
                    playerTwoName_TextView.setText(player.getName());
                    playerTwoType_TextView.setText(determineFighterOrRanger(player));
                    break;
                case (3):
                    playerThreeName_TextView.setText(player.getName());
                    playerThreeType_TextView.setText(determineFighterOrRanger(player));
                    break;
                case (4):
                    playerFourName_TextView.setText(player.getName());
                    playerFourType_TextView.setText(determineFighterOrRanger(player));
                    break;
            }
        }

        //Set enemy name labels
        for(Enemy enemy : enemies)
        {
            if(enemy.getPlayerNumber() == -1)
            {
                if(enemy instanceof Troll)
                    enemyOneName_TextView.setText("Troll");
                else if(enemy instanceof Ogre)
                    enemyOneName_TextView.setText("Ogre");
            }
            else if(enemy.getPlayerNumber() == -2)
            {
                if(enemy instanceof Troll)
                    enemyTwoName_TextView.setText("Troll");
                else if(enemy instanceof Ogre)
                    enemyTwoName_TextView.setText("Ogre");
            }
        }
    }

    private String determineFighterOrRanger(Player player)
    {
        if(player instanceof Human || player instanceof Dwarf)
            return "(Fighter)";
        else
            return "(Ranger)";
    }

    private boolean moveAvailable() {
        return remainingMoves > 0;
    }

    private boolean validateNewPosition(Position newPosition)
    {
        int newRow = newPosition.getRow();
        int newCol = newPosition.getColumn();

        //Make sure column is valid
        if(newCol < 1 || newCol > NUM_COLS)
            return false;

        //Make sure row is valid
        if(newRow < 1 || newRow > NUM_ROWS)
            return false;

        ArrayList<Character> allCharacters = new ArrayList<>();
        allCharacters.addAll(groupMembers);
        allCharacters.addAll(enemies);

        //Make sure another character is not already at this position
        for(Character character : allCharacters)
        {
            Position characterPosition = character.getPosition();
            if(characterPosition.equals(newPosition))
                return false;
        }

        return true;
    }

    private void makeMove(Position newPosition)
    {
        player.setPosition(newPosition);
        player.incrementNumMovesMade();
        updateGrid();
        rollTxtView.setText(Integer.toString(--remainingMoves));
    }

    private boolean isGameOver()
    {
        return allPlayersDead() || allEnemiesDead();
    }

    private boolean allPlayersDead() {
        if (groupMembers.size() == 0)
            return false;

        for (Character c : groupMembers)
            if (c.getHealth() > 0)
                return false;

        return true;
    }

    private boolean allEnemiesDead() {
        if (enemies.size() == 0)
            return false;

        for (Character c : enemies)
            if (c.getHealth() > 0)
                return false;

        return true;
    }

    synchronized private void updateGrid()
    {
        int numChildren = gameGrid.getChildCount();

        // put players and enemies to one array
        ArrayList<Character> allCharacters = new ArrayList<Character>();
        allCharacters.addAll(groupMembers);
        allCharacters.addAll(enemies);

        for(int i = 0 ; i < numChildren ; i++)
        {
            ImageView child = (ImageView) gameGrid.getChildAt(i);

            child.setImageResource(R.drawable.empty_cell_background);

            Position currentIndexPosition = convertGridIndexToPosition(i);

            for(Character character : allCharacters)
                if (character != null && character.getPosition().equals(currentIndexPosition)) {
                    // enemy image
                    if (isEnemySubclass(character)) {
                        Log.i(TAG, "Setting resource for: (if) " + character);
                        child.setImageResource(getEnemyImageResource((Enemy) character));
                    } else {
                        Log.i(TAG, "Setting resource for: (else) " + character);
                        child.setImageResource(getPlayerImageResource((Player) character));
                    }
                }
            child.invalidate();
        }

        updateHealthLabels();
        updateTurnLabels();
        gameGrid.invalidate();
    }

    private boolean isEnemySubclass(Character character) {
        Log.i(TAG, "------------" + character.getClass());
        if (Enemy.class.isAssignableFrom(character.getClass()))
            return true;
        else {
            return false;
        }
    }

    private int getPlayerImageResource(Player player)
    {
        switch (player.getPlayerNumber())
        {
            case (1):
                return R.drawable.grid_number_one;
            case (2):
                return R.drawable.grid_number_two;
            case (3):
                return R.drawable.grid_number_three;
            case (4):
                return R.drawable.grid_number_four;
            default:
                return R.drawable.empty_cell_background;
        }
    }

    private int getEnemyImageResource(Enemy enemy)
    {
        if(enemy instanceof Troll)
            return R.drawable.grid_troll;
        else if(enemy instanceof Ogre)
            return R.drawable.grid_ogre;
        else
            return R.drawable.grid_enemy_default;
    }

    private Position convertGridIndexToPosition(int gridIndex)
    {
        int row = (gridIndex / NUM_COLS) + 1;
        int col = (gridIndex % NUM_COLS) + 1;

        return new Position(row, col);
    }

    private void setUpGrid()
    {
        gameGrid = (GridLayout) findViewById(R.id.gameGrid);

        for(int i=0; i<(NUM_COLS * NUM_ROWS); ++i)
        {
            ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(new LinearLayout.LayoutParams(100,100));
            imageView.setImageResource(R.drawable.empty_cell_background);

            gameGrid.addView(imageView);
        }
    }

    private void setupButtons()
    {
        ImageButton upButton = findViewById(R.id.upButton);
        ImageButton downButton = findViewById(R.id.downButton);
        ImageButton leftButton = findViewById(R.id.leftButton);
        ImageButton rightButton = findViewById(R.id.rightButton);
        ImageButton rollDiceButton = findViewById(R.id.rollDiceBtn);
        attackButton = findViewById(R.id.attackBtn);
        rollTxtView = findViewById(R.id.rollDiceTxtView);

        upButton.setOnClickListener(upButtonListener);
        downButton.setOnClickListener(downButtonListener);
        leftButton.setOnClickListener(leftButtonListener);
        rightButton.setOnClickListener(rightButtonListener);
        attackButton.setOnClickListener(attackButtonListener);
        rollDiceButton.setOnClickListener(rollDiceButtonListener);
    }

    private void setControls(boolean status) {
        ImageButton upButton = findViewById(R.id.upButton);
        ImageButton downButton = findViewById(R.id.downButton);
        ImageButton leftButton = findViewById(R.id.leftButton);
        ImageButton rightButton = findViewById(R.id.rightButton);
        ImageButton rollDiceButton = findViewById(R.id.rollDiceBtn);
        ImageButton attackButton = findViewById(R.id.attackBtn);

        downButton.setEnabled(status);
        upButton.setEnabled(status);
        leftButton.setEnabled(status);
        rightButton.setEnabled(status);
        rollDiceButton.setEnabled(status);
        attackButton.setEnabled(status);
    }


    //--------------------------------------------------------------------------------
    // FIXME check with real game
    private boolean playerDied(Player p){
        return p.getHealth()<=0;
    }

    private boolean enemyDied(Enemy e){
        return e.getHealth() <= 0;
    }

    // FIXME: form correct if clause
    private void displayGameOver(){

        Intent i = new Intent(this, GameOverActivity.class);

        if(allPlayersDead()){
            i.putExtra("msg", "Game Over\nYou lose..");
        }
        else {
            i.putExtra("msg", "Game Over\nCongratulation\nNext Level");
        }
        startActivityForResult(i, 1);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

            Intent intent = new Intent(this, PostGameStatisticsActivity.class);

            int damageDealt = 0;
            int numMoves = 0;
            int finalHealth = 0;
            int numAttacks = 0;

            if(player !=null) {
                damageDealt = player.getTotalDamageDealt();
                numMoves = player.getNumberOfMovesMade();
                finalHealth = player.getHealth();
                numAttacks = player.getNumberOfAttacks();
            }

            intent.putExtra(PostGameStatisticsActivity.DAMAGE_DEALT_EXTRA, damageDealt);
            intent.putExtra(PostGameStatisticsActivity.NUMBER_OF_MOVES_MADE_EXTRA, numMoves);
            intent.putExtra(PostGameStatisticsActivity.FINAL_HEALTH_EXTRA, finalHealth);
            intent.putExtra(PostGameStatisticsActivity.NUMBER_OF_ATTACKS_EXTRA, numAttacks);

            if(allPlayersDead())
                intent.putExtra(PostGameStatisticsActivity.GAME_WON_EXTRA, false);
            else
                intent.putExtra(PostGameStatisticsActivity.GAME_WON_EXTRA, true);

            startActivity(intent);
            finish();
    }

    //--------------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // Thread classes
    // ------------------------------------------------------------------------

    class UpdateGameplay implements Runnable {

        private boolean ignoreUpdate = false;
        private int done = 0;

        @Override
        public void run() {

            Log.i("Adventure Activity: ", "Update Game play thread started...: " );

            String url = "http://alexviznytsya.me/dungeoncrafter/gameplay.php";
            JSONObject jsonObject = new JSONObject();

            try {
                Log.i("AdventureAct: ", "check 001");
                SharedPreferences sp = getSharedPreferences(
                        getString(R.string.user_info_file), MODE_PRIVATE);
                Log.i("AdventureAct: ", "check 002");

                jsonObject.put(getString(R.string.session_id_key),
                        sp.getString(getString(R.string.session_id_key), null));

                jsonObject.put(getString(R.string.groupId),
                        sp.getInt(getString(R.string.groupId), -1));
                jsonObject.put(getString(R.string.playerId),
                        sp.getInt(getString(R.string.playerId), -1));
            } catch (Exception e) {
                e.printStackTrace();
            }

            Log.i("AdventureAct: ", "Sent JSON: " + jsonObject.toString());

            while (true) {
                try {
                    Thread.sleep(1000);
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

                    Log.i("AdventureAct: ", "Before while...");

                    while ((line = bufferedReader.readLine()) != null) {
                        sb.append(line);
                    }

                    bufferedReader.close();
                    Log.i("AdventureAct: ", "--Got JSON:" + sb);

                    try {
                        if(isGameOver()) {
                            SendBoardUpdate.setShouldRun(false);
                            displayGameOver();
                            break;
                        }

                        // FIXME
                        Log.i(TAG, ignoreUpdate + "");
                        if (ignoreUpdate == false) {
                            processResponse(new JSONObject(sb.toString()));
                        }

                    if (player.isTurn()) {
                        // set the flag to true, so updates from Db about
                        // the board are ignored

                        if(player.getHealth() <= 0) {
                            moveComplete = true;
                            updateDb();
                        } else {

                            if (!flagsHaveBeenReset) {
                                ignoreUpdate = true;
                                moveComplete = false;
                                hasRolled = false;

                                // turn controls on
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        setControls(true);
                                    }
                                });

                                flagsHaveBeenReset = true;
                            }

                        }

                            // FIXME
                            // answer the server
                            //updateDb();

                            // turn off player turn
                            // player.setTurn(false);
                            // enemyStriked = false;

                    }

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        //
        // Function is called when there is player move
        // Function exits when player move is 100% complete
        //
        private void updateDb() {
            JSONObject info = new JSONObject();
            try {
                info.put("player_id", player.getPlayerId());
                info.put("session_id", getSharedPreferences
                        (getString(R.string.user_info_file), MODE_PRIVATE).getString
                        (getString(R.string.session_id_key), null));
                info.put("group_id", getSharedPreferences
                        (getString(R.string.user_info_file), MODE_PRIVATE).getInt
                        (getString(R.string.groupId), -1));
                // info.put(new JSONObject("{\"player_id\":" + player.getPlayerId() + "}"));
                // get session_id from shared prefs
//                info.put(new JSONObject("{\"session_id\":" + getSharedPreferences
//                        (getString(R.string.user_info_file), MODE_PRIVATE).getString
//                        (getString(R.string.session_id_key), null) + "}"));
                // get group id from user prefs
//                info.put(new JSONObject("{\"group_id\":" + getSharedPreferences
//                        (getString(R.string.user_info_file), MODE_PRIVATE).getInt
//                        (getString(R.string.groupId), -1) + "}"));

                SendBoardUpdate sendBoardUpdate = new SendBoardUpdate
                        (groupMembers, enemies, info);

                new Thread(sendBoardUpdate).start();

                if (moveComplete == true) {
                    done = 1;
                    ignoreUpdate = false;
                }
                else {
                    done = 0;
                }

                // FIXME: might add new entry every time
                info.put("done", done);

                sendBoardUpdate.update(groupMembers, enemies, info);
            }
                catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void processResponse(JSONObject response) {

            JSONArray arr1 = null;
            try {
                arr1 = response.getJSONArray("players");
            } catch (Exception e) {};

            Log.i("AdventureAct: ", "X Received JSON: " + arr1);

            // extract players
            JSONArray players = extractPlayers(response);
            setUpPlayers(players);

            // extract enemies
            JSONArray enemies = extractEnemies(response);
            setUpEnemies(enemies);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setPlayerNameLabels();
                }
            });
        }

        private void setUpPlayers(JSONArray players) {

            JSONObject currPlayer = null;
            groupMembers.clear();
            for (int i = 0; i < players.length(); ++i) {
                try {
                    currPlayer = (JSONObject) players.get(i);
                    // FIXME: update Db to send race also
                    int race = extractRace(currPlayer);

                    // get desired player instance from the factory
                    Player playerToUpdate = (Player) CharacterFactory.getCharacterInstance(race);
                    // update stats based on Db data
                    updateStats(currPlayer, playerToUpdate);
                    playerToUpdate.setPlayerNumber(i+1);
                    groupMembers.add(playerToUpdate);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateGrid();
                }
            });

            findMyPlayer();
            Log.i("AdventureAct", "groupMembers size; " + groupMembers.size());
        }

        private void findMyPlayer() {
            // get my playerId from prefs
            SharedPreferences sp = getSharedPreferences(
                    getString(R.string.user_info_file), MODE_PRIVATE);

            int myPlayerId = sp.getInt(getString(R.string.playerId), 0);

            for (int i = 0; i < groupMembers.size(); ++i) {
                if (groupMembers.get(i).getPlayerId() == myPlayerId)
                {
                    int damageDealt = 0, numMoves = 0, numAttacks = 0;

                    // Save current player instance stats.
                    // If this is the first time the player is created, this will be null.
                    if(player != null) {
                        damageDealt = player.getTotalDamageDealt();
                        numMoves = player.getNumberOfMovesMade();
                        numAttacks = player.getNumberOfAttacks();
                    }

                    player = groupMembers.get(i);

                    // Assign the old stats to new instance. These will be zero if this
                    // is the first time the player has been created.
                    player.setTotalDamageDealt(damageDealt);
                    player.setNumberOfMovesMade(numMoves);
                    player.setNumberOfAttacks(numAttacks);
                }
            }
        }

        private void setUpEnemies(JSONArray enemiesJSONArray) {
            JSONObject currEnemy = null;
            enemies.clear();
            for (int i = 0; i < enemiesJSONArray.length(); ++i) {
                try {
                    currEnemy = (JSONObject) enemiesJSONArray.get(i);
                }
                catch (Exception e){
                    e.printStackTrace();
                }

                // FIXME: update Db to send race also
                int race = extractRace(currEnemy);

                // get desired player instance from the factory
                Enemy enemy = (Enemy)CharacterFactory.getCharacterInstance(race);
                // update stats based on Db data
                updateStats(currEnemy, enemy);
                enemy.setPlayerNumber(-1-i);
                enemies.add(enemy);
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateGrid();
                }
            });
        }

        private void updateStats(JSONObject jsonPlayer, Character player) {
            int playerId = (int)extractValue(jsonPlayer, getString(R.string.playerId));
            int x = (int)extractValue(jsonPlayer, "x");
            int y = (int)extractValue(jsonPlayer, "y");
            int health = (int)extractValue(jsonPlayer, "health");
            int intTurn = (int)extractValue(jsonPlayer, "turn");
            String name = extractName(jsonPlayer);

            // convert get boolean for turn
            boolean turn;
            if (intTurn == 1) {
                turn = true;
            }
            else {
                turn = false;
            }

            // FIXME: Opposite as Db (x,y), (y,x)
            // Range not consistant Db -> App
            // row is 'y', column is 'x'
            Position position = new Position(y, x);

            if (player != null)
                player.setStats(health, 100, position, playerId, turn, name);
        }

        private JSONArray extractPlayers(JSONObject data) {
            JSONArray players = new JSONArray();
            try {
                players = data.getJSONArray(getString(R.string.players));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return players;
        }

        private JSONArray extractEnemies(JSONObject data) {
            JSONArray enemies = new JSONArray();
            try {
                enemies = data.getJSONArray(getString(R.string.enemies));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return enemies;
        }

        private String extractName(JSONObject obj) {
            String name = "none";
            try {
                name = obj.getString("playerName");
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return name;
        }

        public int extractPlayerId(JSONObject obj) {
            int id = -1;
            try {
                id = obj.getInt(getString(R.string.playerId));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return id;
        }

        public int extractY(JSONObject obj) {
            int y = -1;
            try {
                y = obj.getInt("y");
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return y;
        }

        public Object extractValue(JSONObject obj, String key) {
            Object value = -1;
            try {
                value = obj.getInt(key);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return value;
        }

        public int extractRace(JSONObject obj) {
            int race = -1;
            try {
                race = obj.getInt(getString(R.string.race));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return race;
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

    }   //  UpdateGameplay

}
