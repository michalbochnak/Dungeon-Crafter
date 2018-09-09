package edu.uic.cs440.group1.dungeon_crafter.Utilities;

import edu.uic.cs440.group1.dungeon_crafter.Characters.*;
import edu.uic.cs440.group1.dungeon_crafter.Characters.Character;
import edu.uic.cs440.group1.dungeon_crafter.Models.*;

/**
 * Created by Michal on 3/23/2018.
 */

public class CharacterFactory {

    private static final int HUMAN_RACE = 101, DWARF_RACE = 102, ELF_RACE = 103,
            GNOME_RACE = 104, OGRE_RACE = 105, TROLL_RACE = 106;

    public static Character getCharacterInstance(int race) {
        Character character = null;

        switch (race) {
            case HUMAN_RACE:
                character = new Human(21, 100, new Position(0, 0), 0, false);
                break;
            case DWARF_RACE:
                character = new Dwarf(21, 100, new Position(0, 0), 0, false);
                break;
            case ELF_RACE:
                character = new Elf(15, 100, new Position(0, 0), 0, false);
                break;
            case GNOME_RACE:
                character = new Gnome(15, 100, new Position(0, 0), 0, false);
                break;
            case OGRE_RACE:
                character = new Ogre(10, 100, new Position(0, 0), 0, false, 1);
                break;
            case TROLL_RACE:
                character = new Troll(10, 100, new Position(0, 0), 0, false, 1);
                break;
        }

        return character;
    }

}
