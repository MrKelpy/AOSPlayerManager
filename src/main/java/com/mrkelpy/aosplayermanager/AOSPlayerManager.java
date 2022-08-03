package com.mrkelpy.aosplayermanager;

import com.mrkelpy.aosplayermanager.common.AOSPlayerManagerConfig;
import com.mrkelpy.aosplayermanager.common.LevelSetConfiguration;
import com.mrkelpy.aosplayermanager.listeners.AOSPlayerManagerCommands;
import com.mrkelpy.aosplayermanager.listeners.PlayerDataSavingEvents;
import com.mrkelpy.aosplayermanager.listeners.onWorldChangedEvents;
import com.mrkelpy.aosplayermanager.util.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

@SuppressWarnings("ResultOfMethodCallIgnored")
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

        DataFolder = this.getDataFolder();
        if (!AOSPlayerManager.DataFolder.exists()) DataFolder.mkdirs();

        LOGGER.info("Ensuring plugin folder for " + PLUGIN_NAME);
        FileUtils.makeLevelListDirectory();

        for (String world : this.getWorlds()) {
            LOGGER.info("Verifying world directory for " + world);
            FileUtils.makeLevelDirectory(world);
        }

        LevelSetConfiguration.setup();
        AOSPlayerManagerConfig.setup();
    }

    @Override
    public void onEnable() {
        LOGGER.info("AOSPlayerManager has been enabled!");
        this.getServer().getPluginManager().registerEvents(new onWorldChangedEvents(), this);
        this.getServer().getPluginManager().registerEvents(new PlayerDataSavingEvents(), this);
        this.registerCommands();
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


    /**
     * This method is tailor-made for Age Of Sauron; If the /LotrWorld/ folder exists,
     * then, assume the WorldContainer to be that folder.
     * @return File object representing the folder.
     */
    private File getWorldContainerPath() {

        File defaultWorldContainer = Bukkit.getServer().getWorldContainer();
        File aosWorldContainer = new File(defaultWorldContainer, "LotrWorld");

        return aosWorldContainer.exists() ? aosWorldContainer : defaultWorldContainer;
    }

    /**
     * Tries to retrieve all the world folders that exist in the world container.
     */
    private ArrayList<String> getWorlds() {

        ArrayList<String> worldNames = new ArrayList<>();
        List<String> excludeFolders = Arrays.asList(
                "data","lotr_cwp_logs", "playerdata", "region", "stats", "plugins",
                "logs", "backups", "config", "mods", "libraries");

        for (File file: Objects.requireNonNull(this.getWorldContainerPath().listFiles())) {
            if (file.isDirectory() && excludeFolders.stream().noneMatch(exclusion -> file.getName().toLowerCase().contains(exclusion)))
                worldNames.add(file.getName());
        }
        
       return worldNames;
    }

}

