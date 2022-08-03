package com.mrkelpy.aosplayermanager.listeners;

import com.mrkelpy.aosplayermanager.util.EventUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.WorldSaveEvent;

import java.util.List;

public class PlayerDataSavingEvents implements Listener {

    /**
     * Handles the saving of playerdata when a player leaves the server
     * @param event PlayerQuitEvent
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {

        Player player = event.getPlayer();
        player.eject();
        EventUtils.eventPlayerdataSave(player, player.getWorld().getName());
    }

    /**
     * Handles the saving of playerdata when a player sets their spawn point
     * @param event PlayerBedLeaveEvent
     */
    @EventHandler
    public void onPlayerBedLeave(PlayerBedLeaveEvent event) {

        Player player = event.getPlayer();
        EventUtils.eventPlayerdataSave(player, player.getWorld().getName());
    }

    /**
     * Handles the saving of playerdata when a player opens an inventory
     * @param event InventoryOpenEvent
     */
    @EventHandler
    public void onPlayerInventoryOpen(InventoryOpenEvent event) {

        if (!(event.getPlayer() instanceof Player)) return;

        Player player = (Player) event.getPlayer();
        EventUtils.eventPlayerdataSave(player, player.getWorld().getName());
    }


    /**
     * Handles the saving of playerdata when a player closes their inventory
     * @param event InventoryCloseEvent
     */
    @EventHandler
    public void onPlayerInventoryClose(InventoryCloseEvent event) {

        if (!(event.getPlayer() instanceof Player)) return;

        Player player = (Player) event.getPlayer();
        EventUtils.eventPlayerdataSave(player, player.getWorld().getName());
    }

    /**
     * Handles the saving of playerdata when a player drops an item
     * @param event PlayerDropItemEvent
     */
    @EventHandler
    public void onPlayerItemDrop(PlayerDropItemEvent event) {

        Player player = event.getPlayer();
        EventUtils.eventPlayerdataSave(player, player.getWorld().getName());
    }

    /**
     * Handles the saving of playerdata when a player picks up an item
     * @param event PlayerPickupItemEvent
     */
    @EventHandler
    public void onPlayerItemPickup(PlayerPickupItemEvent event) {

        Player player = event.getPlayer();
        EventUtils.eventPlayerdataSave(player, player.getWorld().getName());
    }

    /**
     * Handles the saving of playerdata when a player dies, nulling out
     * their location for every level in the set, so minecraft handles the respawning.
     * @param event PlayerDeathEvent
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity().getPlayer();
        EventUtils.eventPlayerdataSaveForDeath(player, player.getWorld().getName());
    }

    /**
     * Handles the saving of all the players' playerdata when a world
     * is saved.
     * @param event WorldSaveEvent
     */
    @EventHandler
    public void onWorldSaveEvent(WorldSaveEvent event) {

        List<Player> worldPlayerList = event.getWorld().getPlayers();

        for (Player player : worldPlayerList)
            EventUtils.eventPlayerdataSave(player, player.getWorld().getName());

    }


}

