package com.mrkelpy.aosplayermanager.util;

import com.mrkelpy.aosplayermanager.common.LevelSetConfiguration;
import com.mrkelpy.aosplayermanager.common.PartialLocation;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EventUtils {


    /**
     * Saves the playerdata for a player into a level or level set. This method
     * is meant to be run from event listeners.
     * @param player The player to save the data for
     * @param levelName The level to save the data to
     */
    public static void eventPlayerdataSave(Player player, String levelName) {

        // Check if the world is in a set
        List<ArrayList<String>> levelSet = LevelSetConfiguration.getLevelSets().stream().filter(set -> set.contains(levelName)).collect(Collectors.toList());

        FileUtils.savePlayerData(player, levelName, new PartialLocation(player.getLocation()));

        // Applies the playerdata for every world in the set, save for the location.
        if (levelSet.isEmpty()) return;

        for (String level : levelSet.get(0)) {
            if (level.equals(levelName)) continue; // Skips the level we are saving to.
            FileUtils.savePlayerData(player, level, FileUtils.getPlayerData(player, level).getPlayerLocation());
        }


    }

    /**
     * Saves the playerdata for a player into a level or level set, but resets the HP and
     * any other not kept-on-death attributes to their default values,
     * and also nulls out their location, so minecraft handles the coordinate placement.
     * This method is meant to be run from event listeners.
     *
     * @param player The player to save the data for
     * @param levelName The level to save the data to
     */
    public static void eventPlayerdataSaveForDeath(Player player, String levelName) {

        // Check if the world is in a set
        List<ArrayList<String>> levelSet = LevelSetConfiguration.getLevelSets().stream().filter(set -> set.contains(levelName)).collect(Collectors.toList());
        PlayerInventory inventory = player.getInventory();

        if (!Boolean.getBoolean(player.getWorld().getGameRuleValue("keepInventory")))
            inventory = (PlayerInventory) Bukkit.createInventory(player, InventoryType.PLAYER);

        FileUtils.savePlayerDataForDeath(player, levelName, inventory);

        // Applies the playerdata for every world in the set, save for the location.
        if (levelSet.isEmpty()) return;

        for (String level : levelSet.get(0)) {
            if (level.equals(levelName)) continue; // Skips the level we are saving to.
            FileUtils.savePlayerDataForDeath(player, level, inventory);
        }
    }

}

