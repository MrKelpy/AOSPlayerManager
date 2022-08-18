package com.mrkelpy.aosplayermanager.util;

import com.google.gson.*;
import com.mrkelpy.aosplayermanager.AOSPlayerManager;
import com.mrkelpy.aosplayermanager.common.BackupHolder;
import com.mrkelpy.aosplayermanager.common.PartialLocation;
import com.mrkelpy.aosplayermanager.common.PlayerDataHolder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
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
     * Creates a backup of the player's current data inside plugin/AOSPlayerDimensions/backups/UUID/date.
     * <br>
     * This method is meant to be used in conjunction with {@link FileUtils#savePlayerData(Player, String, PartialLocation)},
     * but it can be used without it.
     * @param player The player to save the data from
     * @param levelName The level to save the data to
     * @return The File instance containing the data
     */
    public static File backupPlayerData(Player player, String levelName, PartialLocation location) {

        // Create the necessary resources to save the player's data (The path to the backup directory, the date formatted to the filename,
        // and the playerdata holder with the data to save)
        File playerLevelBackup = makeLevelDirectory("backups/" + levelName + "/" + player.getUniqueId().toString());
        String date = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
        PlayerDataHolder playerDataHolder = new PlayerDataHolder(player, location);

        // Create the final path destination for the backup and write the data to it
        File playerdataBackupFile = new File(playerLevelBackup, date + ".json");
        FileUtils.writeJson(playerdataBackupFile.getPath(), FileUtils.GSON.toJsonTree(playerDataHolder.serialize()).getAsJsonObject());
        return playerdataBackupFile;
    }

    /**
     * Shortcut for the {@link FileUtils#backupPlayerData(Player, String, PartialLocation)} method,
     * setting the location as the player's location.
     * @param player The player to save the data from
     * @param levelName The level to save the data to
     * @return The File instance containing the data
     */
    public static File backupPlayerData(Player player, String levelName) {
        return backupPlayerData(player, levelName, new PartialLocation(player.getLocation()));
    }

    /**
     * Acts like the {@link FileUtils#savePlayerData(Player, String, PartialLocation)}, but resets the HP and
     * any other not kept-on-death attributes to their default values, and also nulls out their location,
     * so minecraft handles the coordinate placement.
     */
    public static File savePlayerDataForDeath(Player player, String levelName, Inventory inventory, ItemStack[] armour) {

        // Adds the vital data for the player for the death case.
        Queue<Object> data = new LinkedList<>();
        data.add(inventory.getContents());
        data.add(armour);
        data.add(new ArrayList<>());
        data.add(null);
        data.add(player.getMaxHealth());
        data.add(20);

        PlayerDataHolder playerDataHolder = new PlayerDataHolder(player, data);

        File playerdataFile = new File(makeLevelDirectory(levelName), player.getUniqueId().toString() + ".json");
        FileUtils.writeJson(playerdataFile.getPath(), FileUtils.GSON.toJsonTree(playerDataHolder.serialize()).getAsJsonObject());
        return playerdataFile;
    }

    /**
     * Shortcut for the {@link FileUtils#savePlayerDataForDeath(Player, String, Inventory, ItemStack[])} method,
     * setting inventory as the player's inventory.
     */
    public static File savePlayerDataForDeath(Player player, String levelName) {
        return savePlayerDataForDeath(player, levelName, player.getInventory(), player.getInventory().getArmorContents());
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
     * Accesses the backups folders for a player in a certain level and returns an ArrayList containing all the backups
     * created in the form of a BackupHolder. There's also a begin and end parameters that can be used to optimise the searches
     * @param player The player to get the data from
     * @param levelName The level to get the data from
     * @param begin The beginning of the range to get the backups from
     * @param end The end of the range to get the backups from
     * @return The ArrayList with the BackupHolders containing the data
     */
    public static ArrayList<BackupHolder> getPlayerDataBackups(Player player, String levelName, int begin, int end) {

        File playerBackupsDirectory = makeLevelDirectory("backups/" + levelName + "/" + player.getUniqueId().toString());
        ArrayList<BackupHolder> playerDataHolders = new ArrayList<>();
        File[] playerLevelBackups = playerBackupsDirectory.listFiles();
        DateFormat backupDateFormat = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss");

        // Return an empty deque if there are no backups
        if (playerLevelBackups == null || playerLevelBackups.length <= 0) return playerDataHolders;
        Arrays.sort(playerLevelBackups);
        Collections.reverse(Arrays.asList(playerLevelBackups));

        if (end == -1) end = playerLevelBackups.length - 1;

        for (int i = begin; i <= end; i++) {

            // Stops the loop if the end of the list is reached.
            if (playerLevelBackups.length <= i) break;

            // Get the playerdata and the date from the file
            File file = playerLevelBackups[i];
            PlayerDataHolder playerdata = new PlayerDataHolder((HashMap<String, Object>) FileUtils.readJson(file.getPath()));
            Date saveDate = FileUtils.tryParseDate(backupDateFormat, file.getName());

            // Add the playerdata to the ArrayList if the date is valid
            if (saveDate == null) continue;
            playerDataHolders.add(new BackupHolder(playerdata, saveDate));
        }

        return playerDataHolders;
    }

    /**
     * Shortcut for {@link FileUtils#getPlayerDataBackups(Player, String, int, int)} with the begin and end parameters set to 0 and -1,
     * telling the method to get all the backups.
     * @param player The player to get the data from
     * @param levelName The level to get the data from
     * @return The ArrayList with the BackupHolders containing the data
     */
    public static ArrayList<BackupHolder> getPlayerDataBackups(Player player, String levelName) {
        return getPlayerDataBackups(player, levelName, 0, -1);
    }

    /**
     * Tries to parse the date from the filename of a backup file. If it fails, it returns null.
     * @param format The format of the date to parse
     * @param date The date to parse
     * @return The parsed date, or null if it failed
     */
    private static Date tryParseDate(DateFormat format, String date) {

        try {
            return format.parse(date);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * Formats the target date into the predetermined format of "Month day, year at hour:minute:second".
     * @param targetDate The date to format
     * @return The formatted date
     */
    public static String formatToReadable(Date targetDate) {

        DateFormat dateFormat = new SimpleDateFormat("LLLL dd, yyyy");
        DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        String date = dateFormat.format(targetDate);
        String time = timeFormat.format(targetDate);
        return date + " at " + time;
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
            if (levelListDirectory.isDirectory() && !levelListDirectory.getName().equals("backups")) {
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

        } catch (IOException ignored) {}

        return new HashMap<>();
    }
}

