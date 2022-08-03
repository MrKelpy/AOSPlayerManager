package com.mrkelpy.aosplayermanager.listeners;

import com.mrkelpy.aosplayermanager.AOSPlayerManager;
import com.mrkelpy.aosplayermanager.common.DefaultGamemodes;

import com.mrkelpy.aosplayermanager.common.PlayerDataHolder;
import com.mrkelpy.aosplayermanager.util.EventUtils;
import com.mrkelpy.aosplayermanager.util.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

public class onWorldChangedEvents implements Listener {

    /**
     * Handles the dynamic loading of playerdata before a player moves worlds.
     * This takes in the Set context, so if a world in a set is affected, the entire set will too.
     * @param event PlayerTeleportEvent
     */
    @EventHandler
    public void onWorldChangedPre(PlayerTeleportEvent event) {

        if (event.getTo() == null || event.getFrom().getWorld() == event.getTo().getWorld())
            return;

        Player player = event.getPlayer();

        EventUtils.eventPlayerdataSave(player, player.getWorld().getName());
        player.eject();  // Ejects the player from a vehicle if they're in one

        for (ItemStack item : player.getInventory().getArmorContents()) {

            if (item.getType() == Material.AIR) continue;
            player.getWorld().dropItem(player.getLocation(), item).setPickupDelay(3);
        }
    }

    /**
     * Does the same as {@link #onWorldChangedPre(PlayerTeleportEvent)} but for PlayerPortalEvent.
     * @param event PlayerPortalEvent
     */
    @EventHandler
    public void onWorldChangedPre(PlayerPortalEvent event) {
        this.onWorldChangedPre((PlayerTeleportEvent) event);
    }

    /**
     * Handles the dynamic loading of playerdata after a player moves worlds.
     * This takes in the Set context, so if a world in a set is affected, the entire set will too.
     * @param event PlayerChangedWorldEvent
     */
    @EventHandler
    public void onWorldChangedPost(PlayerChangedWorldEvent event) {

        Player player = event.getPlayer();
        player.getInventory().setArmorContents(null);
        PlayerDataHolder playerdata = FileUtils.getPlayerData(player, player.getWorld().getName());
        // Force default gamemode
        player.setGameMode(DefaultGamemodes.get(player.getWorld().getName()));

        // Applies the data a tick later to avoid issues with Multiverse.
        Bukkit.getScheduler().runTaskLater(
                Bukkit.getPluginManager().getPlugin(AOSPlayerManager.PLUGIN_NAME),
                () -> playerdata.applyTo(player, event.getPlayer().getWorld()), 1);

    }
}

