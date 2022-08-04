package com.mrkelpy.aosplayermanager.common;

import net.minecraft.server.v1_7_R4.NBTTagCompound;
import org.bukkit.entity.Entity;

/**
 * This class implements an "entity holder" that can carry either the Entity object
 * for an entity or the Entity's NBT data. It can, however, only hold one of either at a time.
 */
public class EntityHolder {

    private Entity entityObject;
    private NBTTagCompound entityNBT;

    public EntityHolder(NBTTagCompound nbt) {
        this.entityNBT = nbt;
        this.entityObject = null;
    }

    public EntityHolder(Entity entity) {
        this.entityNBT = null;
        this.entityObject = entity;
    }


    /**
     * Sets the EntityHolder to hold the Entity object.
     *
     * @param entity The Entity object to be held.
     */
    public void set(Entity entity) {
        this.entityObject = entity;
        this.entityNBT = null;
    }

    /**
     * Sets the EntityHolder to hold the Entity's NBT data.
     *
     * @param nbt The NBT data to be held.
     */
    public void set(NBTTagCompound nbt) {
        this.entityObject = null;
        this.entityNBT = nbt;
    }

    /**
     * Returns the object currently held by the EntityHolder.
     *
     * @return Object (either Entity or NBTTagCompound)
     */
    public Object get() {
        return this.entityObject != null ? this.entityObject : this.entityNBT;
    }

}

