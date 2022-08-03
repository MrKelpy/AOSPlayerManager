package com.mrkelpy.aosplayermanager.util;

import com.google.gson.*;
import com.mrkelpy.aosplayermanager.AOSPlayerManager;
import com.mrkelpy.aosplayermanager.common.PartialLocation;
import com.mrkelpy.aosplayermanager.common.PlayerDataHolder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

import java.io.*;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class implements utilitary methods for dealing with playerdata and level files.
 */
@SuppressWarnings({"unused", "ResultOfMethodCallIgnored", "deprecation"})
public class FileUtils {

    /**
     * This GSON instance should be used for serialization and de-serialization, since
     * it contains the necessary settings to make the JSON-like objects function.
     */
    public final static Gson GSON = new GsonBuilder().setObjectToNumberStrategy(ToNumberPolicy.LAZILY_PARSED_NUMBER)
            .excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.STATIC)
            .setExclusionStrategies(new PlayerExclusionStrategy())
            .serializeNulls()
            .create();

    /**
     * Creates the levels' storage directory if it doesn't exist.
     */
    public static File makeLevelListDirectory() {

        File levelListDirectory = new File(AOSPlayerManager.DataFolder, "AOSPlayerManagerDimensions");

        if (!levelListDirectory.exists())
            levelListDirectory.mkdirs();

        return levelListDirectory;
    }

    /**
     * Creates a subdirectory inside the level list's directory representing a level's playerdata storage
     * if it doesn't already exist.
     */
    public static File makeLevelDirectory(String levelName) {

        File levelDirectory = new File(makeLevelListDirectory(), levelName);

        if (!levelDirectory.exists())
            levelDirectory.mkdirs();

        return levelDirectory;
    }

    /**
     * Creates a file inside the level's playerdata storage directory (if it doesn't exist already)
     * that represents a player's data for that level, based on the given PlayerDataHolder.
     * @param player The player to save the data from
     * @param levelName The level to save the data to
     * @param location The location that the player was in for the level. This needs to be here
     *                 because the player's location can vary even for two worlds in a Set.
     * @return The File instance containing the data
     */
    public static File savePlayerData(Player player, String levelName, PartialLocation location) {

        PlayerDataHolder playerDataHolder = new PlayerDataHolder(player, location);
        File playerdataFile = new File(makeLevelDirectory(levelName), player.getUniqueId().toString() + ".json");
        FileUtils.writeJson(playerdataFile.getPath(), FileUtils.GSON.toJsonTree(playerDataHolder.serialize()).getAsJsonObject());
        return playerdataFile;
    }

    /**
     * Shortcut for the {@link FileUtils#savePlayerData(Player, String, PartialLocation)} method, setting the location as the player's location
     * in that level.
     */
    public static File savePlayerData(Player player, String levelName) {
        return savePlayerData(player, levelName, new PartialLocation(player.getLocation()));
    }

    /**
     * Acts like the {@link FileUtils#savePlayerData(Player, String, PartialLocation)}, but resets the HP and
     * any other not kept-on-death attributes to their default values, and also nulls out their location,
     * so minecraft handles the coordinate placement.
     */
    public static File savePlayerDataForDeath(Player player, String levelName, PlayerInventory inventory) {
        PlayerDataHolder playerDataHolder = new PlayerDataHolder(inventory, null, new ArrayList<>(),
                player.getLevel(), player.getExp(), player.getMaxHealth(), 20);

        File playerdataFile = new File(makeLevelDirectory(levelName), player.getUniqueId().toString() + ".json");
        FileUtils.writeJson(playerdataFile.getPath(), FileUtils.GSON.toJsonTree(playerDataHolder.serialize()).getAsJsonObject());
        return playerdataFile;
    }

    /**
     * Shortcut for the {@link FileUtils#savePlayerDataForDeath(Player, String, PlayerInventory)} method,
     * setting inventory as the player's inventory.
     */
    public static File savePlayerDataForDeath(Player player, String levelName) {
        return savePlayerDataForDeath(player, levelName, player.getInventory());
    }


    /**
     * Accesses the playerdata file for a player in a level, and loads it into a PlayerDataHolder.
     * @param player The player to get the data from
     * @param levelName The level to get the data from
     * @return The PlayerDataHolder instance containing the data
     */
    public static PlayerDataHolder getPlayerData(Player player, String levelName) {

        File playerdataFile = new File(makeLevelDirectory(levelName), player.getUniqueId().toString() + ".json");
        HashMap<String, Object> playerData = (HashMap<String, Object>) FileUtils.readJson(playerdataFile.getPath());
        return new PlayerDataHolder(playerData);
    }


    /**
     * Returns a list of all the directories  names inside the plugin dimension list, which
     * match every managed world.
     * @return All the managed worlds
     */
    public static ArrayList<String> getManagedWorlds() {

        ArrayList<String> managedWorlds = new ArrayList<>();
        File[] levelListDirectories = makeLevelListDirectory().listFiles();

        // Return an empty list if the level list directory is empty
        if (levelListDirectories == null) return managedWorlds;

        for (File levelListDirectory : levelListDirectories) {
            if (levelListDirectory.isDirectory()) {
                managedWorlds.add(levelListDirectory.getName());
            }
        }

        return managedWorlds;
    }

    /**
     * Writes a JSONObject into the specified file.
     * @param json The JSONObject to write
     * @param filepath The path to the file to write to
     */
    public static void writeJson(String filepath, JsonObject json) {

        // Creates the JSON file if it doesn't exist
        File fileCheck = new File(filepath);
        if (!fileCheck.exists()) fileCheck.getParentFile().mkdirs();

        // Actually writes the stuff to the file
        try (FileWriter file = new FileWriter(filepath)) {
            file.write(json.toString());
            file.flush();

        } catch (IOException e) {
            AOSPlayerManager.LOGGER.warning("Failed to write JSON to file " + filepath);
            e.printStackTrace();
        }
    }

    /**
     * Reads a JSONObject from the specified file and translates it to a Map.
     * @param filepath The path to the file to read from
     * @return The map containing the json data.
     */
    public static Map<String, Object> readJson(String filepath) {

        // Checks if the file exists. If not, return an empty map.
        if (!new File(filepath).exists()) return new HashMap<>();

        try (FileReader file = new FileReader(filepath)) {
            JsonParser parser = new JsonParser();
            return parser.parse(file).getAsJsonObject().entrySet().stream().collect(
                    Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        } catch (IOException e) {
            AOSPlayerManager.LOGGER.warning("Failed to read JSON from file " + filepath);
            e.printStackTrace();
        }

        return new HashMap<>();
    }
}

