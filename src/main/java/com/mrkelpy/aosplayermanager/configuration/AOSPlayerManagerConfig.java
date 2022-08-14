package com.mrkelpy.aosplayermanager.configuration;

import com.mrkelpy.aosplayermanager.AOSPlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;

/**
 * This class implements a custom yet simple configuration handler for the plugin
 */
public class AOSPlayerManagerConfig {

    private static final Plugin PLUGIN = Bukkit.getServer().getPluginManager().getPlugin(AOSPlayerManager.PLUGIN_NAME);

    /**
     * Sets up the configuration file for the plugin
     */
    public static void setup() {
        PLUGIN.saveDefaultConfig();
        addDefaults();
        PLUGIN.saveConfig();
    }

    /**
     * Adds all the default values to the configuration file
     */
    public static void addDefaults() {

        if (!getConfig().contains("worlds.disable-coordinate-handling"))
            getConfig().set("worlds.disable-coordinate-handling", Arrays.asList("example-world", "example-world2"));

        if (!getConfig().contains("worlds.null-coordinates"))
            getConfig().set("worlds.null-coordinates", Arrays.asList("example-world3", "example-world4"));

        if (!getConfig().contains("worlds.global-save-tick-interval"))
            getConfig().set("worlds.global-save-tick-interval", 20*60*5);
    }

    /**
     * Adds a small layer of abstraction over the Bukkit configuration API to return
     * the configuration file for the plugin without accessing the plugin itself.
     * @return The FileConfiguration for the plugin config
     */
    public static FileConfiguration getConfig() {
        return PLUGIN.getConfig();
    }
}

