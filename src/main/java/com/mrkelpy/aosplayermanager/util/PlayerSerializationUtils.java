package com.mrkelpy.aosplayermanager.util;

import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * This class implements a series of utilitary functions for usage whilst serializing
 * and deserializing PlayerDataHolders.
 */
public class PlayerSerializationUtils {

    /**
     * Since inventories cannot be serialized by themselves, builds a map of the inventory's contents
     * one by one, and returns it.
     *
     * @param inventory The inventory to serialize
     * @return A map of the inventory's contents
     */
    public static Map<Integer, Object> serializeInventory(Inventory inventory) {

        Map<Integer, Object> serializedInventory = new HashMap<>();

        for (int i = 0; i < inventory.getSize(); i++) {

            if (inventory.getItem(i) == null) continue;
            serializedInventory.put(i, inventory.getItem(i).serialize());
        }

        return serializedInventory;
    }

    /**
     * Creates a new ownerless inventory from a JSON serialized inventory. This inventory
     * can later be applied to any player.
     * @param inventory The serialized inventory to create the inventory from
     * @return The new ownerless inventory
     */
    @SuppressWarnings("unchecked")
    public static Inventory deserializeInventory(Map<Integer, Object> inventory) {

        Inventory deserializedInventory = Bukkit.createInventory(null, InventoryType.PLAYER);

        for (Map.Entry<Integer, Object> entry : inventory.entrySet()) {
            deserializedInventory.setItem(entry.getKey(), ItemStack.deserialize((Map<String, Object>) entry.getValue()));
        }

        return deserializedInventory;


    }
}

