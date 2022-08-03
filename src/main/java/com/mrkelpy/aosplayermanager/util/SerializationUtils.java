package com.mrkelpy.aosplayermanager.util;

import com.mrkelpy.aosplayermanager.AOSPlayerManager;
import net.minecraft.server.v1_7_R4.NBTCompressedStreamTools;
import net.minecraft.server.v1_7_R4.NBTTagCompound;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.*;
import java.math.BigInteger;

public class SerializationUtils {


    /**
     * Converts an ItemStack into a base64 string. This is a type of serialization that not only is smaller
     * than just serializing the inventory to JSON, but also cleaner, and one that retains all the information.
     *
     * @param item The ItemStack to convert.
     * @return A base64 string representing the ItemStack.
     */
    public static String itemStackToBase64(ItemStack item) {

        // Opens a ByteArrayOutputStream and a DataOutputStream to write the data into.
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             DataOutputStream dataOutput = new DataOutputStream(outputStream)) {

            // Converts the ItemStack into an NMS copy (Thus preserving all the NBT data) and saves it into the DataOutputStream.
            net.minecraft.server.v1_7_R4.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
            NBTCompressedStreamTools.a(nmsItem.save(new NBTTagCompound()), (DataOutput) dataOutput);

            // Encode the bytearray from the DataOutputStream into a base64 string.
            return new BigInteger(1, outputStream.toByteArray()).toString(32);

        } catch (IOException | NullPointerException e) {
            // If an error occurs for some reason, just return null.
            return null;
        }
    }

    /**
     * Converts a base64 string into an ItemStack. This method expects a base64 string coming from
     * {@link #itemStackToBase64(ItemStack)}.
     *
     * @param data The base64 string to convert.
     * @return The ItemStack represented by the base64 string.
     */
    public static ItemStack itemStackFromBase64(String data) {

        // Opens a ByteArrayInputStream and a DataInputStream to read the bytes from the base64 string.
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
     * {@link #itemStackToBase64(ItemStack)} and then concatenating them together.
     *
     * @param stackArray The itemStack[] to convert.
     * @return A base64 string representing the PlayerInventory.
     */
    public static String itemStackArrayToBase64(ItemStack[] stackArray) {

        // Open a ByteArrayOutputStream and an ObjectOutputStream to write the data into.
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {

            dataOutput.writeInt(stackArray.length);  // Write the size of the inventory into the stream, so it can be decoded later

            // Loop through each item in the inventory and convert it to a base64 string.
            for (ItemStack itemStack : stackArray)
                handleConversion(itemStack, dataOutput);

            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());  // Return the base64 string of the inventory

        } catch (IOException e) {
            // If an error occurs for some reason, just log it as a warning and return null.
            AOSPlayerManager.LOGGER.warning("Failed to encode inventory to base64 string.");
            return null;
        }
    }

    /**
     * Converts a base64 string into an itemStack[]. This is a shortcut for manually converting each item through
     * {@link #itemStackFromBase64(String)}, and is meant to be used with inventories.
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
                    inventoryContents[i] = SerializationUtils.itemStackFromBase64((String) base64Data);
            }

            dataInput.close();
            return inventoryContents;

        } catch (IOException | ClassNotFoundException e) {
            // If an error occurs for some reason, just log it as a warning and return null.
            AOSPlayerManager.LOGGER.warning("Failed to decode inventory to base64 string with data: " + data);
            return null;
        }
    }

    /**
     * Handles the conversion of an item stack to a base64 string, within the context of an itemStack[] conversion.
     * @param itemStack The item stack to convert.
     * @param dataOutput The data output stream to write the base64 string to.
     * @throws IOException If an error occurs while writing to the data output stream.
     */
    public static void handleConversion(ItemStack itemStack, BukkitObjectOutputStream dataOutput) throws IOException {

        String base64ItemStack = SerializationUtils.itemStackToBase64(itemStack);

        // Check if the base64 string is null and if not, write it to the stream.
        if (base64ItemStack != null) {
            dataOutput.writeObject(base64ItemStack);
            return;
        }

        // Writes a null string to the stream if the base64 string is null.
        dataOutput.writeObject(null);
    }
}


