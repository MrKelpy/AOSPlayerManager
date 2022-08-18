package com.mrkelpy.aosplayermanager.events;

import com.mrkelpy.aosplayermanager.AOSPlayerManager;
import com.mrkelpy.aosplayermanager.configuration.AOSPlayerManagerConfig;
import com.mrkelpy.aosplayermanager.util.EventUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

/**
 * Handles the global saving of playerdata every x ticks set on the configuration file.
 */
public class GlobalPeriodicDataSavingEvent extends BukkitRunnable {

    /**
     * Saves all the online players' playerdatas.
     */
    @Override
    public void run() {
        for (Player player : Bukkit.getServer().getOnlinePlayers())
            EventUtils.eventPlayerdataSave(player, player.getWorld().getName());
    }

    /**
     * Obtains the value for the global save tick interval from the configuration file, and runs the task based on it.
     * <br>
     * Shadow-sets the lower bound for the interval to 1 minute, to avoid issues.
     * @param plugin The plugin instance
     * @return The BukkitTask for the global save task
     */
    @SuppressWarnings("UnusedReturnValue")
    public BukkitTask runTaskTimer(Plugin plugin) throws IllegalArgumentException, IllegalStateException {

        long globalSavingDelay = AOSPlayerManagerConfig.getConfig().getInt("general.global-save-tick-interval");
        return super.runTaskTimer(plugin, 0, globalSavingDelay > 20*60 ? globalSavingDelay : 20*60L);
    }
}

