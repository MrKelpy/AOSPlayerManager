package com.mrkelpy.aosplayermanager.common;

import org.bukkit.GameMode;

import java.util.HashMap;

/**
 * This class provides a clean way to obtain the default gamemodes for
 * any given world.
 */
public class DefaultGamemodes {

    private final static HashMap<String, GameMode> DEFAULT_GAMEMODES = buildDefaultGamemodes();

    /**
     * Builds a HashMap containing the default gamemodes for every world.
     * Any non-specified worlds will have their gamemodes be Survival.
     * @return Default Gamemodes Map
     */
     private static HashMap<String, GameMode> buildDefaultGamemodes() {

        HashMap<String, GameMode> gamemodes = new HashMap<>();
        gamemodes.put("Creative", GameMode.CREATIVE);

        return gamemodes;
    }

    /**
     * Returns the default gamemode for a world, and if none is specified, return Survival.
     * @param levelName The name of the world
     * @return The default gamemode
     */
    public static GameMode get(String levelName) {

         if (!DEFAULT_GAMEMODES.containsKey(levelName)) return GameMode.SURVIVAL;
         return DEFAULT_GAMEMODES.get(levelName);
    }

}

