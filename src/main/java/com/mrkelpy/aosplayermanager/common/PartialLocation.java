package com.mrkelpy.aosplayermanager.common;

import org.bukkit.Location;
import org.bukkit.World;

import java.io.Serializable;
import java.util.HashMap;

/**
 * This class aims to reduce the complexity of serialization and de-serialization
 * when it comes to the playerdata, by removing the World out of the equation on a Location
 * at serialization-time.
 */
public class PartialLocation implements Serializable {

    public double x;
    public double y;
    public double z;
    public float pitch;
    public float yaw;

    /**
     * Creates a PartialLocation manually by assigning the x, y, z, pitch, and yaw values.
     * @param x The x coordinate of the location.
     * @param y The y coordinate of the location.
     * @param z The z coordinate of the location.
     * @param pitch The player look-pitch.
     * @param yaw The player look-yaw.
     */
    public PartialLocation(double x, double y, double z, float pitch, float yaw) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
    }

    /**
     * This is a shortcut for {@link #PartialLocation(double, double, double, float, float)}, where
     * the PartialLocation will be created from a given Location
     * @param location The location to create the PartialLocation from.
     */
    public PartialLocation(Location location) {
       this(location.getX(), location.getY(), location.getZ(), location.getPitch(), location.getYaw());
    }

    /**
     * Creates a Location from the current PartialLocation.
     * @param world The world to create the location in.
     * @return Location
     */
    public Location toLocation(World world) {
        return new Location(world, this.x, this.y, this.z, this.yaw, this.pitch);
    }

    @Override
    public String toString() {
        return String.format("x%s y%s z%s", this.x, this.y, this.z);
    }

    /**
     * Creates a new HashMap from the current PartialLocation.
     * @return Serialized PartialLocation.
     */
    public HashMap<String, Object> serialize() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("x", this.x);
        map.put("y", this.y);
        map.put("z", this.z);
        map.put("pitch", this.pitch);
        map.put("yaw", this.yaw);
        return map;
    }

}

