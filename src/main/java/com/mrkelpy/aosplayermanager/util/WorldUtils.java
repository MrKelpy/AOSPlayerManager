package com.mrkelpy.aosplayermanager.util;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class WorldUtils {

    /**
     * Since {@link Player#getVehicle()} doesn't return the vehicle for modded entities,
     * this method will scan nearby entities to the player and check if they have it as a
     * passenger, and if any does, return it.
     *
     * @param player The player to scan for the vehicle
     * @return The vehicle, if found, null otherwise.
     */
    public static Entity scanForVehicle(Player player) {

        Location pivot = player.getLocation();
        Entity[] pivotChunk = pivot.getChunk().getEntities();

        for (Entity entity : pivotChunk) {
            if (entity.getPassenger() != null && entity.getPassenger().equals(player)) {
                return entity;
            }
        }
        return null;
    }
}

