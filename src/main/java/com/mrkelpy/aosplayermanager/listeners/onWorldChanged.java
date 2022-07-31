package com.mrkelpy.aosplayermanager.listeners;

import com.mrkelpy.aosplayermanager.common.LevelSetConfiguration;
import com.mrkelpy.aosplayermanager.common.PlayerDataHolder;
import com.mrkelpy.aosplayermanager.util.FileUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.stream.Collectors;


public class onWorldChanged implements Listener {

    /**
     * Handles the dynamic loading of playerdata before a player moves worlds.
     * This takes in the Set context, so if a world in a set is affected, the entire set will too.
     * @param event PlayerTeleportEvent
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @EventHandler
    public void onWorldChangedPre(PlayerTeleportEvent event) {

        if (event.getFrom().getWorld() == event.getTo().getWorld())
            return;

        Player player = event.getPlayer();

        // Check if the world is in a set
        ArrayList<ArrayList<String>> levelSets = LevelSetConfiguration.getLevelSets();
        levelSets.stream().filter(set -> !set.contains(event.getFrom().getWorld().getName()));

        // Applies the playerdata for every world in the set, save for the location.
        if (!levelSets.isEmpty()) {
            levelSets.get(0).forEach(level -> FileUtils.savePlayerData(player, level, FileUtils.getPlayerData(player, level).getPlayerLocation()));
            return;
        }

        FileUtils.savePlayerData(player, player.getWorld().getName(), player.getLocation());
    }


    /**
     * Handles the dynamic loading of playerdata after a player moves worlds.
     * This takes in the Set context, so if a world in a set is affected, the entire set will too.
     * @param event PlayerChangedWorldEvent
     */
    @EventHandler
    public void onWorldChangedPost(PlayerChangedWorldEvent event) {

        Player player = event.getPlayer();
        PlayerDataHolder playerdata = FileUtils.getPlayerData(player, player.getWorld().getName());
        playerdata.applyTo(player);
    }
}

