package com.mrkelpy.aosplayermanager.common;

import com.google.gson.*;
import com.mrkelpy.aosplayermanager.util.FileUtils;
import com.mrkelpy.aosplayermanager.util.SerializationUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * This class implements a way to store, serialize, and de-serialize selective sections of a player's
 * data, in order to load it into a player when needed.
 */
@SuppressWarnings("unused")
public class PlayerDataHolder implements Serializable {

    private ItemStack[] playerInventory;
    private ItemStack[] playerArmour;
    private ArrayList<SimplePotionEffect> playerPotionEffects;
    private PartialLocation playerCoordinates;
    private int playerExperienceLevels;
    private float playerExperiencePoints;
    private double playerHealth;
    private int playerHunger;

    /**
     * Constructs the PlayerDataHolder instance from the given parameters.
     * At construction time, the data will be saved,so that a perfect replica of the saved state can be
     * created.
     * <br>
     * This constructor can be used for precise control over the data that is saved.
     */
    public PlayerDataHolder(PlayerInventory playerInventory, PartialLocation location, Collection<PotionEffect> potionEffects, int experienceLevels,
                            float experiencePoints, double health, int hunger) {
        this.playerInventory = playerInventory.getContents();
        this.playerArmour = playerInventory.getArmorContents();
        this.playerPotionEffects = potionEffects.stream().map(SimplePotionEffect::new).collect(Collectors.toCollection(ArrayList::new));
        this.playerCoordinates = location;
        this.playerExperienceLevels = experienceLevels;
        this.playerExperiencePoints = experiencePoints;
        this.playerHealth = health;
        this.playerHunger = hunger;
    }

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
    public PlayerDataHolder(Player player, PartialLocation location) {
        player.saveData();
        this.playerInventory = player.getInventory().getContents();
        this.playerArmour = player.getInventory().getArmorContents();
        this.playerPotionEffects = player.getActivePotionEffects().stream().map(SimplePotionEffect::new).collect(Collectors.toCollection(ArrayList::new));
        this.playerCoordinates = location;
        this.playerExperienceLevels = player.getLevel();
        this.playerExperiencePoints = player.getExp();
        this.playerHealth = player.getHealth();
        this.playerHunger = player.getFoodLevel();
    }

    /**
     * Alternative for {@link #PlayerDataHolder(Player, PartialLocation)} that takes a location and
     * converts it into a PartialLocation for convenience.
     * @param player The player to get the data from
     * @param location The player location.
     */
    public PlayerDataHolder(Player player, Location location) {
        this(player, new PartialLocation(location));
    }

    /**
     * Takes in a serialized PlayerDataHolder and de-serializes it, so that the PlayerDataHolder
     * can be used as normal.
     */
    public PlayerDataHolder(HashMap<String, Object> serializedPlayerData) {
        this.deserialize(FileUtils.GSON.toJsonTree(serializedPlayerData).getAsJsonObject());
    }

    /**
     * Serializes the PlayerDataHolder into a JSON-like object, so that the player state can be stored
     * in the correct file.
     * @return A JSON-like object representing the PlayerDataHolder
     */
    public HashMap<String, Object> serialize() {

        HashMap<String, Object> serializedPlayerdata = new HashMap<>();

        serializedPlayerdata.put("inventory",
                SerializationUtils.itemStackArrayToBase64(this.playerInventory));

        serializedPlayerdata.put("armour",
                SerializationUtils.itemStackArrayToBase64(this.playerArmour));

        serializedPlayerdata.put("potionEffects", this.playerPotionEffects.size() > 0
                ? this.playerPotionEffects.stream().map(SimplePotionEffect::serialize).collect(Collectors.toList()) : null);

        serializedPlayerdata.put("coordinates", this.playerCoordinates != null ? this.playerCoordinates.serialize() : null);
        serializedPlayerdata.put("experienceLevels", this.playerExperienceLevels);
        serializedPlayerdata.put("experiencePoints", this.playerExperiencePoints);
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
    @SuppressWarnings("unchecked")
    public void deserialize(JsonObject serializedPlayerData) {

         this.playerInventory = serializedPlayerData.get("inventory") != null
                ? SerializationUtils.itemStackArrayFromBase64(FileUtils.GSON.fromJson(serializedPlayerData.get("inventory").toString(), String.class))
                : new ItemStack[InventoryType.PLAYER.getDefaultSize()];

         this.playerArmour = serializedPlayerData.get("inventory") != null ?
                SerializationUtils.itemStackArrayFromBase64(FileUtils.GSON.fromJson(serializedPlayerData.get("armour").toString(), String.class))
                : new ItemStack[4];

        // Returns a list of the potion effects, if present, without the effects being cast to SimplePotionEffect
        ArrayList<Object> uncastedPotionEffectList = serializedPlayerData.get("potionEffects") != null ?
                FileUtils.GSON.fromJson(serializedPlayerData.get("potionEffects").toString(), ArrayList.class) : null;

        // Casts the potion effects to SimplePotionEffect inside the list, if present.
        this.playerPotionEffects = uncastedPotionEffectList != null ? uncastedPotionEffectList.stream().map(effect -> FileUtils.GSON.fromJson(effect.toString(), SimplePotionEffect.class))
                .collect(Collectors.toCollection(ArrayList::new)) : null;

        this.playerExperienceLevels = serializedPlayerData.get("experienceLevels") != null ? serializedPlayerData.get("experienceLevels").getAsInt() : 0;
        this.playerExperiencePoints = serializedPlayerData.get("experiencePoints") != null ? serializedPlayerData.get("experiencePoints").getAsFloat() : 0.0F;
        this.playerHealth = serializedPlayerData.get("health") != null ? serializedPlayerData.get("health").getAsDouble() : 20.0D;
        this.playerHunger = serializedPlayerData.get("hunger") != null ? serializedPlayerData.get("hunger").getAsInt() : 20;

        // These two are the only properties that can be null, even if unknown.
        this.playerCoordinates = serializedPlayerData.get("coordinates") != null ? FileUtils.GSON.fromJson(serializedPlayerData.get("coordinates"), PartialLocation.class)
                : null;
    }

    /**
     * Returns the player location for the level
     * @return The player location
     */
    public PartialLocation getPlayerLocation() {
        return this.playerCoordinates;
    }

    /**
     * The player needs to exist somewhere, so if the coordinates are nulled, the data holder is empty.
     * @return True if the data holder is empty, false otherwise.
     */
    public boolean isEmpty() {
        return this.playerCoordinates == null;
    }

    /**
     * Applies the Playerdata held in the PlayerDataHolder to a given player.
     */
    public void applyTo(Player player, World level) {
        player.getInventory().setContents(this.playerInventory);
        player.getInventory().setArmorContents(this.playerArmour);
        player.setLevel(this.playerExperienceLevels);
        player.setExp(this.playerExperiencePoints);
        player.setHealth(this.playerHealth);
        player.setFoodLevel(this.playerHunger);

        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        if (this.playerPotionEffects != null) {
            for (SimplePotionEffect potionEffect : this.playerPotionEffects)
                player.addPotionEffect(potionEffect.toPotionEffect());
        }

        // Handles the player coordinate placement. See the logic for this implementation at PlayerDataHolder#isEmpty
        // Also, if specified in the config, the level will not handle coordinate placement.
        if (!this.isEmpty() && !AOSPlayerManagerConfig.getConfig().getList("worlds.disable-coordinate-handling").contains(level.getName())) {
            player.teleport(this.playerCoordinates.toLocation(level));
        }

        player.saveData();
    }


}

