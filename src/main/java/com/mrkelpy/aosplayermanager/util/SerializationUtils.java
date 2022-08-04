package com.mrkelpy.aosplayermanager.util;

import com.mrkelpy.aosplayermanager.AOSPlayerManager;
import net.minecraft.server.v1_7_R4.*;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.*;
import java.math.BigInteger;

/**
 * This class implements a series of methods useful for serializing/deserializing objects used
 * in Minecraft.
 */
public class SerializationUtils {

    /**
     * Converts an ItemStack into a sign-magnitude string of base32. This is a type of serialization that not only is smaller
     * than just serializing the inventory to JSON, but also cleaner, and one that retains all the information.
     *
     * @param item The ItemStack to convert.
     * @return A sign-magnitude string of base32 string representing the ItemStack.
     */
    public static String itemStackToMagBase32(ItemStack item) {

        // Opens a ByteArrayOutputStream and a DataOutputStream to write the data into.
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             DataOutputStream dataOutput = new DataOutputStream(outputStream)) {

            // Converts the ItemStack into an NMS copy (Thus preserving all the NBT data) and saves it into the DataOutputStream.
            net.minecraft.server.v1_7_R4.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
            NBTCompressedStreamTools.a(nmsItem.save(new NBTTagCompound()), (DataOutput) dataOutput);

            // Encode the bytearray from the DataOutputStream into a sign-magnitude integer, and convert it to a base32 string.
            // We can't use base64 here because the number of bits would not be enough to decode later.
            return new BigInteger(1, outputStream.toByteArray()).toString(32);

        } catch (IOException | NullPointerException e) {
            // If an error occurs for some reason, just return null.
            return null;
        }
    }

    /**
     * Converts a sign-magnitude string of base32 into an ItemStack. This method expects a base32 string coming from
     * {@link #itemStackToMagBase32(ItemStack)}.
     *
     * @param data The base32 string to convert.
     * @return The ItemStack represented by the base32 string.
     */
    public static ItemStack itemStackFromMagBase32(String data) {

        // Opens a ByteArrayInputStream and a DataInputStream to read the bytes from the base32 string.
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(new BigInteger(data, 32).toByteArray());
             DataInputStream dataInput = new DataInputStream(inputStream)) {

            // Reads the data from the DataInputStream, in the form of an NBT compound, and creates an ItemStack from it.
            NBTTagCompound itemData = NBTCompressedStreamTools.a(dataInput);
            net.minecraft.server.v1_7_R4.ItemStack nmsItem = net.minecraft.server.v1_7_R4.ItemStack.createStack(itemData);

            // Return a bukkit copy of the NMS itemStack.
            return CraftItemStack.asBukkitCopy(nmsItem);

        } catch (IOException | NullPointerException e) {
            // If an error occurs for some reason, just return null.
            return null;
        }
    }

    /**
     * Converts an itemStack[] into a base64 string. This is a shortcut for manually converting each item through
     * {@link #itemStackToMagBase32(ItemStack)} and then concatenating them together.
     *
     * @param stackArray The itemStack[] to convert.
     * @return A base64 string representing the ItemStack[].
     */
    public static String itemStackArrayToBase64(ItemStack[] stackArray) {

        // Open a ByteArrayOutputStream and an ObjectOutputStream to write the data into.
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {

            dataOutput.writeInt(stackArray.length);  // Write the size of the inventory into the stream, so it can be decoded later

            // Loop through each item in the inventory and convert it to a base32 string.
            for (ItemStack itemStack : stackArray) {
                String base64ItemStack = SerializationUtils.itemStackToMagBase32(itemStack);

                // Check if the base32 string is null and if not, write it to the stream.
                if (base64ItemStack != null) {
                    dataOutput.writeObject(base64ItemStack);
                    continue;
                }

                // Writes a null string to the stream if the base64 string is null.
                dataOutput.writeObject(null);
            }

            dataOutput.close();
            // Return the base64 string of the inventory. We can use base64 here, since everything is pretty much already handled
            // by the Mag32 strings, so let's do it.
            return Base64Coder.encodeLines(outputStream.toByteArray());

        } catch (IOException e) {
            // If an error occurs for some reason, just log it as a warning and return null.
            AOSPlayerManager.LOGGER.warning("Failed to encode inventory to base64 string.");
            return null;
        }
    }

    /**
     * Converts a base64 string into an itemStack[]. This is a shortcut for manually converting each item through
     * {@link #itemStackFromMagBase32(String)}, and is meant to be used with inventories.
     *
     * @param data The base64 string to convert.
     * @return The ItemStack[] represented by the base64 string.
     */
    public static ItemStack[] itemStackArrayFromBase64(String data) {

        // Open a ByteArrayInputStream and an ObjectInputStream to read the data from the base64 string.
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
             BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {

            // Read the size of the inventory from the stream, and create an instance of PlayerInventory with that size.
            ItemStack[] inventoryContents = new ItemStack[dataInput.readInt()];

            // Loop through each item in the inventory and convert them from a base64 string.
            for (int i = 0; inventoryContents.length > i; i++) {
                Object base64Data = dataInput.readObject();

                // If the itemStack is not null, set the item in the inventory at the current index to the itemStack.
                if (base64Data != null)
                    inventoryContents[i] = SerializationUtils.itemStackFromMagBase32((String) base64Data);
            }

            dataInput.close();
            return inventoryContents;

        } catch (IOException | ClassNotFoundException e) {
            // If an error occurs for some reason, just log it as a warning and return null.
            AOSPlayerManager.LOGGER.warning("Failed to decode inventory from base64 string with data: " + data);
            return null;
        }
    }

    /**
     * Converts a CraftEntity into a sign-magnitude string of base32. This type of serialization
     * preserves all the NBT data of the entity.
     * @param entity The CraftEntity to convert.
     * @return A base32 string representing the CraftEntity.
     */
    @SuppressWarnings("deprecation")
    public static String entityToMagBase32(CraftEntity entity) {

        // Opens a ByteArrayOutputStream and a DataOutputStream to write the data into.
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             DataOutputStream dataOutput = new DataOutputStream(outputStream)) {

            NBTTagCompound entityNBT = getEntityNBTCompound(entity);
            entityNBT.set("id", new NBTTagString(entity.getType().getName()));

            // Write the NBTTagCompound into the DataOutputStream.
            NBTCompressedStreamTools.a(entityNBT, (DataOutput) dataOutput);

            // Encode the bytearray from the DataOutputStream into a sign-magnitude integer, and convert it to a base32 string.
            // We can't use base64 here because the number of bits would not be enough to decode later.
            return new BigInteger(1, outputStream.toByteArray()).toString(32);

        } catch (IOException | NullPointerException e) {
            // If an error occurs for some reason, just return null.
            return null;
        }
    }

    /**
     * Converts a sign-magnitude string of base32 into an NBTTagCompound. This return value can later
     * be applied to an entity at spawn-time to set its NBT data. (See {@link World#spawn(Location, Class)}
     * and {@link net.minecraft.server.v1_7_R4.CommandSummon#execute(ICommandListener, String[])}
     * for the nms entity class creation).
     * @param data The base32 string to convert.
     * @return The NBTTagCompound represented by the base32 string.
     */
    public static NBTTagCompound entityFromMagBase32(String data) {

        // Opens a ByteArrayInputStream and a DataInputStream to read the bytes from the base32 string.
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(new BigInteger(data, 32).toByteArray());
             DataInputStream dataInput = new DataInputStream(inputStream)) {

            // Reads the data from the DataInputStream, in the form of an NBT compound, and returns it.
            return NBTCompressedStreamTools.a(dataInput);

        } catch (IOException | NullPointerException e) {
            // If an error occurs for some reason, just return null.
            return null;
        }
    }

    /**
     * Uses NMS to obtain the NBT data of an entity and returns it in the form of an
     * NBTTagCompound.
     * @param entity (CraftEntity) The entity to get the NBT data of.
     * @return The NBTTagCompound of the entity.
     */
    public static NBTTagCompound getEntityNBTCompound(CraftEntity entity) {

        // Converts the Entity into an NMS EntityLiving, and saves its NBT into an NBTTagCompound
        NBTTagCompound entityNBT = new NBTTagCompound();
        net.minecraft.server.v1_7_R4.EntityLiving nmsEntity = (EntityLiving) entity.getHandle();
        nmsEntity.e(entityNBT);

        return entityNBT;
    }
}


