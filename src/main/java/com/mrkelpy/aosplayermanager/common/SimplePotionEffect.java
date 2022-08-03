package com.mrkelpy.aosplayermanager.common;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;

/**
 * This class implements a way to store and serialize a way simpler version of a PotionEffect.
 */
public class SimplePotionEffect implements ConfigurationSerializable {

    private final String type;
    private final int duration;
    private final int amplifier;

    /**
     * Constructs the SimplePotionEffect instance from the given parameters.
     * @param type The type of the potion effect.
     * @param duration The duration of the potion effect.
     * @param amplifier The amplifier of the potion effect.
     */
    public SimplePotionEffect(String type, int duration, int amplifier) {
        this.type = type;
        this.duration = duration;
        this.amplifier = amplifier;
    }

    /**
     * Shortcut for the {@link SimplePotionEffect#SimplePotionEffect(String, int, int)} constructor,
     * to build a new SimplePotionEffect instance from a given PotionEffect.
     * @param potionEffect The PotionEffect to build the SimplePotionEffect instance from.
     */
    public SimplePotionEffect(PotionEffect potionEffect) {
        this(potionEffect.getType().getName(), potionEffect.getDuration(), potionEffect.getAmplifier());
    }

    /**
     * Transforms the current SimplePotionEffect instance into a PotionEffect instance.
     * @return PotionEffect
     */
    public PotionEffect toPotionEffect() {
        return new PotionEffect(PotionEffectType.getByName(this.type), this.duration, this.amplifier);
    }

    /**
     * Serializes the current SimplePotionEffect instance into a Map.
     * @return Serialized SimplePotionEffect.
     */
    @Override
    public Map<String, Object> serialize() {

        Map<String, Object> map = new HashMap<>();
        map.put("type", this.type);
        map.put("duration", this.duration);
        map.put("amplifier", this.amplifier);
        return map;
    }
}

