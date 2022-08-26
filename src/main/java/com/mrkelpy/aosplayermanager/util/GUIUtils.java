package com.mrkelpy.aosplayermanager.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * This class implements utility methods for GUI creation.
 */
public class GUIUtils {
    
    /**
     * Creates an item meant to be used as a placeholder for a button inside a GUI.
     *
     * @param itemType The item type of the item to be created.
     * @param itemName The item's name
     * @param itemLore (Optional) The item's lore.
     * @param data     (Optional) The item's extra data.
     * @return The ItemStack with the custom item.
     */
    public static ItemStack createItemPlaceholder(Material itemType, String itemName, List<String> itemLore, short data) {

        // Creates the item and obtains its metadata
        ItemStack placeholder = new ItemStack(itemType, 1, data);
        ItemMeta itemMeta = placeholder.getItemMeta();

        // Sets the custom data for the item
        itemMeta.setDisplayName("§f" + itemName);
        if (itemLore != null) itemMeta.setLore(itemLore);

        // Saves the data and returns the item
        placeholder.setItemMeta(itemMeta);
        return placeholder;
    }

    /**
     * Shortcut for {@link #createItemPlaceholder(Material, String, List, short)} with the lore and data set to null.
     * @param itemType The item type of the item to be created.
     * @param itemName The item's name
     * @return The ItemStack with the custom item.
     */
    public static ItemStack createItemPlaceholder(Material itemType, String itemName) {
        return createItemPlaceholder(itemType, itemName, null, (short) 0);
    }

}

