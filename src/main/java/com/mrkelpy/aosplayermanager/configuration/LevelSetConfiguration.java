package com.mrkelpy.aosplayermanager.configuration;

import com.google.gson.*;
import com.mrkelpy.aosplayermanager.AOSPlayerManager;
import com.mrkelpy.aosplayermanager.util.FileUtils;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implements a custom configuration in the form of a JSON file that allows
 * the user to create Level sets that will share a common PlayerDataHolder.
 */
@SuppressWarnings({"unchecked", "deprecation"})
public class LevelSetConfiguration {

    public static final File LEVEL_SET_FILE = new File(AOSPlayerManager.DataFolder, "LevelSetConfigs.json");
    private static final ArrayList<ArrayList<String>> LEVEL_SETS = readLevelSetConfig();

    public LevelSetConfiguration() {
        setup();
    }

    /**
     * Ensures that a LevelSetConfig file exists, and if not, creates a default one, with
     * an example set containing the world, nether and end.
     */
    public static void setup() {

        if (LEVEL_SET_FILE.exists())
            return;

        // Create all the needed json objects
        JsonObject levelSetConfig = new JsonObject();
        JsonArray worlds = new JsonArray();
        JsonArray defalultLevelSet = new JsonArray();

        // Add the levels into the default set
        defalultLevelSet.add(new JsonPrimitive("world"));
        defalultLevelSet.add(new JsonPrimitive("world_nether"));
        defalultLevelSet.add(new JsonPrimitive("world_the_end"));

        worlds.add(defalultLevelSet);  // Add the default set to the list of level sets

        levelSetConfig.add("level_sets", worlds);
        FileUtils.writeJson(LEVEL_SET_FILE.getPath(), levelSetConfig);
    }

    public static ArrayList<ArrayList<String>> getLevelSets() {
        return LEVEL_SETS;
    }

    /**
     * Returns the Level Set that contains the given world.
     * @param worldName The name of the world to search for.
     * @return The Level Set that contains the given world.
     */
    public static ArrayList<String> getLevelSetFor(String worldName) {

        for (ArrayList<String> levelSet : LEVEL_SETS) {
            if (levelSet.contains(worldName)) {
                return levelSet;
            }
        }
        return new ArrayList<>(Arrays.asList(worldName, ""));
    }

    /**
     * Reads the JsonObject from the level set config file and returns a list of all the level sets.
     * @return The map containing the json data.
     */
    private static ArrayList<ArrayList<String>> readLevelSetConfig() {

        try (FileReader file = new FileReader(LEVEL_SET_FILE)) {
            JsonParser parser = new JsonParser();

            // Go over all the entries in the dictionary, map them to a key:val pair, get the level_sets key, and turn it into an arraylist
            return FileUtils.GSON.fromJson(parser.parse(file).getAsJsonObject().entrySet()
                    .stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
                    .get("level_sets"), ArrayList.class);

        } catch (IOException e) {
            AOSPlayerManager.LOGGER.warning("Failed to read JSON from file " + LEVEL_SET_FILE);
            e.printStackTrace();
        }

        return new ArrayList<>();
    }
}
