package com.mrkelpy.aosplayermanager.gui;

import com.mrkelpy.aosplayermanager.AOSPlayerManager;
import com.mrkelpy.aosplayermanager.common.BackupHolder;
import com.mrkelpy.aosplayermanager.util.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class creates a GUI meant to browse through AOSPlayerManager's managed worlds, aiming
 * to check for playerbound data in the folders that are created for each world.
 */
public class PlayerdataLevelSelectorGUI extends PagedGUI {

    private final Player player;

    /**
     * Main constructor for the PlayerdataLevelSelectorGUI.
     */
    public PlayerdataLevelSelectorGUI(Player player) {
        super("Backups for " + player.getName(), 27);
        this.player = player;
        this.setItems(this.makeItemWorldList());
        this.reload();
        this.registerListeners();
    }

    /**
     * Handles the selection of a world in the GUI.
     * @param event InventoryClickEvent
     */
    @Override
    @EventHandler
    public void onItemClick(InventoryClickEvent event) {
        super.onItemClick(event);
        if (event.getCurrentItem().getType() != Material.WOOL) return;

        // Get the world name from the clicked item's name and open a PlayerdataSelectorGUI from it.
        new PlayerdataSelectorGUI(this.player, event.getCurrentItem().getItemMeta().getDisplayName().substring(4)).openInventory();
    }

    /**
     * Opens this inventory for the bound player.
     */
    public void openInventory() {
        this.player.openInventory(this.inventory);
    }

    /**
     * Registers all event listeners used by an instance of this GUI.
     */
    private void registerListeners() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(AOSPlayerManager.PLUGIN_NAME);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Obtains all the managed worlds and turns them into custom ItemStacks, to be placed into the inventory.
     *
     * @return A list of ItemStacks, representing the worlds.
     */
    private List<ItemStack> makeItemWorldList() {

        ArrayList<String> worlds = FileUtils.getManagedWorlds();
        worlds.sort(String.CASE_INSENSITIVE_ORDER);

        List<ItemStack> worldItemList = new ArrayList<>();

        for (String world : worlds)
            worldItemList.add(this.worldToBlock(world, "§a"));

        return worldItemList;
    }

    /**
     * Takes in the world name and returns either a white wool block or a green wool block.
     * The green wool would represent the world the player is in, and the green represents any other world.
     *
     * @param worldName The name of the world.
     * @param codes     The colour codes to be used to modify the name display
     * @return The wool block
     */
    @SuppressWarnings("deprecation")
    private ItemStack worldToBlock(String worldName, String codes) {

        ArrayList<BackupHolder> worldBackups = FileUtils.getPlayerDataBackups(this.player, worldName);
        String lastSaved = worldBackups.isEmpty() ? "Never" : FileUtils.formatToReadable(worldBackups.get(0).getSaveDate());

        return worldName.equals(this.player.getWorld().getName())
                ? PagedGUI.createItemPlaceholder(Material.WOOL, codes + worldName, Collections.singletonList("§eActive"), DyeColor.LIME.getData())
                : PagedGUI.createItemPlaceholder(Material.WOOL, codes + worldName,
                Collections.singletonList("§8Last saved: " + lastSaved), DyeColor.WHITE.getData());
    }

    /**
     * Alternative for {@link #worldToBlock(String, String)}, that doesn't take in any colour codes.
     *
     * @param worldName The name of the world.
     * @return The wool block
     */
    private ItemStack worldToBlock(String worldName) {
        return this.worldToBlock(worldName, "");
    }
}

