package edu.uic.cs440.group1.dungeon_crafter.Characters;

import edu.uic.cs440.group1.dungeon_crafter.Models.Position;

/**
 * Created by Artur on 3/16/2018.
 */

// FIXME
public class Ranger extends Enemy {
    public Ranger(int strength, int intelligence, Position position,
                  int playerId, boolean turn) {

        super(strength, intelligence, position, playerId, turn, 1);
    }

    @Override
    public void attack() {
        super.attack();
    }

}
