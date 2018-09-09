package edu.uic.cs440.group1.dungeon_crafter.Characters;


import org.json.JSONArray;
import org.json.JSONObject;

import edu.uic.cs440.group1.dungeon_crafter.Models.Position;

/**
 * Created by Artur on 3/16/2018.
 */

public abstract class Character {

    private int health;
    private int strength;
    private int intelligence;
    private Position position;
    private int playerId;
    private boolean turn;
    private int playerNumber;
    private String name;
    private int attackRange;

    public Character(int strength, int intelligence, Position position,
                     int playerId, boolean turn) {

        this.health = 100;
        this.strength = strength;
        this.intelligence = intelligence;
        this.position = position;
        this.playerId = playerId;
        this.turn = turn;
        this.attackRange = 1;
    }

    public int getAttackRange() {
        return attackRange;
    }

    public void setAttackRange(int attackRange) {
        this.attackRange = attackRange;
    }

    public String getName() {
        return name;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        if (health < 0)
            this.health = 0;
        else
            this.health = health;
    }

    public void receiveDamage(int damage)
    {
        setHealth(health - damage);
    }

    public int getStrength() {
        return strength;
    }

    public int getPlayerNumber() {
        return playerNumber;
    }

    public void setPlayerNumber(int playerNumber) {
        this.playerNumber = playerNumber;
    }


    public void setStrength(int strength) {
        this.strength = strength;
    }

    public int getIntelligence() {
        return intelligence;
    }

    public boolean isTurn() {
        return this.turn;
    }

    public void setIntelligence(int intelligence) {
        this.intelligence = intelligence;
    }

    public int getPlayerId() {
        return playerId;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public void setStats(int health, int strength, int intelligence, Position position,
                              int playerId, boolean turn, String name){

        this.health = health;
        this.strength = strength;
        this.intelligence = intelligence;
        this.position = position;
        this.playerId = playerId;
        this.turn = turn;
        this.name = name;
    }

    public void setStats(int health, int intelligence, Position position,
                         int playerId, boolean turn, String name){

        this.health = health;
        this.intelligence = intelligence;
        this.position = position;
        this.playerId = playerId;
        this.turn = turn;
        this.name = name;
    }

    public void setStats(int health, int strength, int intelligence, Position position) {
        this.health = health;
        this.strength = strength;
        this.intelligence = intelligence;
        this.position = position;
    }

    public void setStats(int health, Position position) {
        this.health = health;
        this.position = position;
    }

    public void setTurn(boolean turn) {
        this.turn = turn;
    }

    public void attack(){

    }

    /**
     * @return JSONArray with: x, y, health
     */
    public JSONObject getJsonInfo() {
        JSONObject json = new JSONObject();
        try {
            json.put("x", position.getColumn());
            json.put("y", position.getRow());
            json.put("health", health);
            json.put("pid", playerId);
            json.put("turn", turn?1:0);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return json;
    }


}
