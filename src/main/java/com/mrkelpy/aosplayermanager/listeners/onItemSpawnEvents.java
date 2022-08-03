package com.mrkelpy.aosplayermanager.listeners;

import net.minecraft.server.v1_7_R4.NBTTagCompound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryPickupItemEvent;

public class onItemSpawnEvents implements Listener {

    /**
     * Removes any "LOTRRandomEnch" tags from an item when it is picked up in an inventory.
     * @param event ItemSpawnEvent
     */
    @EventHandler
    public void onInventoryItemPickup(InventoryPickupItemEvent event) {

        ItemStack item = this.removeTag(event.getItem().getItemStack(), "LOTRRandomEnch");
        event.getItem().setItemStack(item);
    }

    /**
     * Uses NMS to remove an NBT Tag from an item, if it exists.
     * @param itemstack The itemStack to remove the tag from
     * @param tag The tag to remove
     */
    public ItemStack removeTag(ItemStack itemstack, String tag) {

        net.minecraft.server.v1_7_R4.ItemStack item = CraftItemStack.asNMSCopy(itemstack);
        NBTTagCompound itemTags = item.getTag();

        if (itemTags != null && itemTags.hasKey(tag)) {
            itemTags.remove(tag);
            item.setTag(itemTags);
        }

        return CraftItemStack.asBukkitCopy(item);
    }
}

