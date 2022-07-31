package com.mrkelpy.aosplayermanager;

import com.mrkelpy.aosplayermanager.common.LevelSetConfiguration;
import com.mrkelpy.aosplayermanager.listeners.AOSPlayerManagerCommands;
import com.mrkelpy.aosplayermanager.listeners.onWorldChanged;
import com.mrkelpy.aosplayermanager.util.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Logger;

public class AOSPlayerManager extends JavaPlugin {

    public static final Logger LOGGER = Bukkit.getLogger();
    public static final String PLUGIN_NAME = "AOSPlayerManager";
    public static File DataFolder;

    /**
     * On plugin loading, creates the plugin folder if it doesn't exist and loops through all the
     * available worlds, creating their subdirectories in the plugin folder if they don't exist.
     */
    @Override
    public void onLoad() {
        DataFolder = getDataFolder();
        LOGGER.info("Ensuring plugin folder for " + PLUGIN_NAME);
        FileUtils.makeLevelListDirectory();

        for (World world : Bukkit.getWorlds()) {
            LOGGER.info("Verifying world directory for " + world.getName());
            FileUtils.makeLevelDirectory(world.getName());
        }
    }

    @Override
    public void onEnable() {
        LOGGER.info("AOSPlayerManager has been enabled!");
        this.getServer().getPluginManager().registerEvents(new onWorldChanged(), this);
        this.registerCommands();
        new LevelSetConfiguration();
    }

    @Override
    public void onDisable() {
        LOGGER.info("AOSPlayerManager has been disabled!");
    }

    /**
     * Registers all the commands for the plugin.
     */
    public void registerCommands() {
        getCommand("levellist").setExecutor(AOSPlayerManagerCommands.INSTANCE);
    }



}

