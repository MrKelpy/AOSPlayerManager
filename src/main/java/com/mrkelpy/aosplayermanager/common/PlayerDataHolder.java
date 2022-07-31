package com.mrkelpy.aosplayermanager.common;

import com.mrkelpy.aosplayermanager.util.PlayerSerializationUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * This class implements a way to store, serialize, and de-serialize selective sections of a player's
 * data, in order to load it into a player when needed.
 */
@SuppressWarnings("unchecked")
public class PlayerDataHolder implements Serializable {

    private Inventory playerInventory;
    private Location playerCoordinates;
    private Number[] playerExperience = new Number[2]; // A Pair of Int (Level) and Float (Points)
    private Entity playerVehicle;
    private double playerHealth;
    private int playerHunger;


    /**
     * Constructs the PlayerDataHolder instance from a given player, extracting
     * the needed data from the object.
     * <br>
     * At construction time, the data will be saved,so that a perfect replica of the saved state can be
     * created.
     * @param player The player to get the data from
     * @param location The player location.
     *                 (This is here because the player's location can vary even for two worlds in a Set.)
     */
    public PlayerDataHolder(Player player, Location location) {
        player.saveData();
        this.playerInventory = player.getInventory();
        this.playerCoordinates = location;
        this.playerExperience[0] = player.getLevel();
        this.playerExperience[1] = player.getExp();
        this.playerVehicle = player.getVehicle();
        this.playerHealth = player.getHealth();
        this.playerHunger = player.getFoodLevel();
    }

    /**
     * Takes in a serialized PlayerDataHolder and de-serializes it, so that the PlayerDataHolder
     * can be used as normal.
     */
    public PlayerDataHolder(Map<String, Object> serializedPlayerData) {
        this.deserialize(serializedPlayerData);
    }

    /**
     * Serializes the PlayerDataHolder into a JSON-like object, so that the player state can be stored
     * in the correct file.
     * @return A JSON-like object representing the PlayerDataHolder
     */
    public Map<String, Object> serialize() {

        Map<String, Object> serializedPlayerdata = new HashMap<>();

        serializedPlayerdata.put("inventory",
                PlayerSerializationUtils.serializeInventory(this.playerInventory));

        serializedPlayerdata.put("coordinates", this.playerCoordinates.serialize());
        serializedPlayerdata.put("experience", this.playerExperience);
        serializedPlayerdata.put("vehicle", this.playerVehicle);
        serializedPlayerdata.put("health", this.playerHealth);
        serializedPlayerdata.put("hunger", this.playerHunger);

        return serializedPlayerdata;
    }

    /**
     * De-serializes the PlayerDataHolder from a JSON-like object into an instance of PlayerDataHolder
     * so that it can be normally used.
     *
     * @param serializedPlayerData The JSON-like object to de-serialize
     */
    public void deserialize(Map<String, Object> serializedPlayerData) {
        this.playerInventory = serializedPlayerData.get("inventory") != null
                ? PlayerSerializationUtils.deserializeInventory((Map<Integer, Object>) serializedPlayerData.get("inventory"))
                : Bukkit.createInventory(null, InventoryType.PLAYER);

        this.playerExperience = serializedPlayerData.get("experience") != null ? (Number[]) serializedPlayerData.get("experience") : new Number[2];
        this.playerHealth = serializedPlayerData.get("health") != null ? (double) serializedPlayerData.get("health") : 20;
        this.playerHunger = serializedPlayerData.get("hunger") != null ? (int) serializedPlayerData.get("hunger") : 20;

        // These two are the only properties that can be null, even if unknown.
        this.playerCoordinates = serializedPlayerData.get("coordinates") != null ? Location.deserialize((Map<String, Object>) serializedPlayerData.get("coordinates")) : null;
        this.playerVehicle = serializedPlayerData.get("vehicle") != null ? (Entity) serializedPlayerData.get("vehicle") : null;
    }

    /**
     * The player needs to be somewhere in the world, so if the coordinates are null, the holder
     * is empty.
     * @return boolean indicating whether the holder is empty or not.
     */
    public boolean isEmpty() {
        return this.playerCoordinates == null;
    }

    /**
     * Returns the player location for the level
     * @return
     */
    public Location getPlayerLocation() {
        return this.playerCoordinates;
    }

    /**
     * Applies the Playerdata held in the PlayerDataHolder to a given player.
     */
    public void applyTo(Player player) {
        player.getInventory().setContents(this.playerInventory.getContents());
        player.setLevel(this.playerExperience[0].intValue());
        player.setExp(this.playerExperience[1].floatValue());
        player.setHealth(this.playerHealth);
        player.setFoodLevel(this.playerHunger);

        if (this.isEmpty()) {  // See the logic for this implementation at PlayerDataHolder#isEmpty
            player.teleport(this.playerCoordinates);
            this.playerVehicle.setPassenger(player);
        }

        player.saveData();
    }

}

