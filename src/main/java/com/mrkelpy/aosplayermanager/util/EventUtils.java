package com.mrkelpy.aosplayermanager.util;

import com.mrkelpy.aosplayermanager.common.PartialLocation;
import com.mrkelpy.aosplayermanager.configuration.AOSPlayerManagerConfig;
import com.mrkelpy.aosplayermanager.configuration.LevelSetConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class EventUtils {


    /**
     * Saves the playerdata for a player into a level or level set. This method
     * is meant to be run from event listeners.
     * @param player The player to save the data for
     * @param levelName The level to save the data to
     */
    public static void eventPlayerdataSave(Player player, String levelName) {

        // If the null-coordinates setting is enabled for this level, don't save the coordinates for it or the set.
        boolean nullCoordinates = AOSPlayerManagerConfig.getConfig().getList("worlds.null-coordinates").contains(levelName);

        FileUtils.savePlayerData(player, levelName, nullCoordinates ? null : new PartialLocation(player.getLocation()));

        // Applies the playerdata for every world in the set, save for the location.
        if (LevelSetConfiguration.getLevelSetFor(levelName).isEmpty()) return;

        for (String level : LevelSetConfiguration.getLevelSetFor(levelName)) {
            if (level.equals(levelName)) continue; // Skips the level we are saving to.
            FileUtils.savePlayerData(player, level, nullCoordinates ? null : FileUtils.getPlayerData(player, level).getPlayerCoordinates());
        }
    }

    /**
     * Backs up the playerdata for a player into a level or level set. This method is meant to be run from event listeners.
     * <br>
     * Before anyone that checks this code out screams at me: I know that there's code repetition here, and I'm going against
     * the DRY principle, but I don't really want to parametrise the backup in the save event, and would rather have a method
     * specifically for the backups. It's better and less messy in the long run.
     * @param player The player to save the data for
     * @param levelName The level to save the data to
     */
    public static void eventPlayerdataBackup(Player player, String levelName) {

        // If the null-coordinates setting is enabled for this level, don't save the coordinates for it or the set.
        boolean nullCoordinates = AOSPlayerManagerConfig.getConfig().getList("worlds.null-coordinates").contains(levelName);

        FileUtils.backupPlayerData(player, levelName, nullCoordinates ? null : new PartialLocation(player.getLocation()));

        // Applies the playerdata for every world in the set, save for the location.
        if (LevelSetConfiguration.getLevelSetFor(levelName).isEmpty()) return;

        for (String level : LevelSetConfiguration.getLevelSetFor(levelName)) {
            if (level.equals(levelName)) continue; // Skips the level we are backing up to.
            FileUtils.backupPlayerData(player, level, nullCoordinates ? null : FileUtils.getPlayerData(player, level).getPlayerCoordinates());
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
        Inventory inventory = player.getInventory();
        ItemStack[] armour = player.getInventory().getArmorContents();

        // If the keepInventory gamerule is set to true, but there's a forced no keepinv, the items won't been dropped, so
        // we need to drop them manually. This is an edge case that I don't think has a better fix.
        if (Boolean.parseBoolean(player.getWorld().getGameRuleValue("keepInventory")) && AOSPlayerManagerConfig.getConfig().getList("worlds.no-keepinventory").contains(levelName)) {
            Arrays.stream(inventory.getContents()).filter(Objects::nonNull).filter(i -> i.getType() != Material.AIR).forEach(i -> player.getWorld().dropItemNaturally(player.getLocation(), i));
            Arrays.stream(armour).filter(Objects::nonNull).filter(i -> i.getType() != Material.AIR).forEach(item -> player.getWorld().dropItemNaturally(player.getLocation(), item));
        }

        // Delete the inventory from the player if keepinventory is disabled or force disabled, and update the inventory and armour.
        if (!Boolean.parseBoolean(player.getWorld().getGameRuleValue("keepInventory")) || AOSPlayerManagerConfig.getConfig().getList("worlds.no-keepinventory").contains(levelName)) {
            player.getInventory().setContents(Bukkit.createInventory(null, InventoryType.PLAYER).getContents());
            player.getInventory().setArmorContents(new ItemStack[4]);

            inventory = Bukkit.createInventory(null, InventoryType.PLAYER);
            armour = new ItemStack[4];
        }

        FileUtils.savePlayerDataForDeath(player, levelName, inventory, armour);

        // Applies the playerdata for every world in the set, save for the location.
        if (levelSet.isEmpty()) return;

        for (String level : levelSet.get(0)) {
            if (level.equals(levelName)) continue; // Skips the level we are saving to.
            FileUtils.savePlayerDataForDeath(player, level, inventory, armour);
        }
    }

}

