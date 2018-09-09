package edu.uic.cs440.group1.dungeon_crafter.Characters;

import android.util.Log;

import edu.uic.cs440.group1.dungeon_crafter.Models.Position;

/**
 * Created by sean on 3/16/2018.
 */

public abstract class Player extends Character
{

    //private Position position;    //remove for abstract

    //added
    private int totalDamageDealt;
    private int numberOfMovesMade;
    private int numberOfAttacks;

    public Player(int strength, int intelligence, Position position, int playerId, boolean turn)
    {
        super(strength, intelligence, position, playerId, turn);
        totalDamageDealt = 0;
        numberOfMovesMade = 0;
        numberOfAttacks = 0;
    }

    public void receiveDamage(int damage)
    {
        setHealth(getHealth() - damage);
    }

    public void dealDamage(int damage)
    {
        totalDamageDealt += damage;
        ++numberOfAttacks;
    }

    public int getNumberOfAttacks()
    {
        return numberOfAttacks;
    }

    public int getTotalDamageDealt() {
        return totalDamageDealt;
    }

    public int getNumberOfMovesMade() {
        return numberOfMovesMade;
    }

    public void setNumberOfAttacks(int numberOfAttacks) {
        this.numberOfAttacks = numberOfAttacks;
    }

    public void setTotalDamageDealt(int totalDamageDealt) {
        this.totalDamageDealt = totalDamageDealt;
    }

    public void setNumberOfMovesMade(int numberOfMovesMade)
    {
        this.numberOfMovesMade = numberOfMovesMade;
    }

    public void incrementNumMovesMade()
    {
        ++numberOfMovesMade;
    }

}
