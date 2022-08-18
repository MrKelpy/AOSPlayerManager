package com.mrkelpy.aosplayermanager.gui;

import com.mrkelpy.aosplayermanager.AOSPlayerManager;
import com.mrkelpy.aosplayermanager.common.BackupHolder;
import com.mrkelpy.aosplayermanager.util.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collections;

/**
 * This class creates a GUI meant to browse through one of the managed worlds, displaying
 * all the different playerdata backups, including the currently saved data. This GUI is player and world-bound.
 */
public class PlayerdataSelectorGUI extends PagedGUI {

    private final Player player;
    private final String levelName;

    /**
     * Main constructor for the PlayerdataLevelSelectorGUI.
     */
    public PlayerdataSelectorGUI(Player player, String worldName) {
        super("Backups for " + player.getName(), 27);
        this.player = player;
        this.levelName = worldName;
        this.setItems(this.makeItemPlayerdataList());
        this.reload();
        this.registerListeners();
    }

    /**
     * Opens this inventory for the bound player.
     */
    public void openInventory() {
        this.player.openInventory(this.inventory);
    }

    /**
     * Creates a list of ItemStacks, representing the playerdata backups for the bound world, mostly as paper ItemStacks.
     * The current data is represented as a written book.
     * @return ArrayList(ItemStack)
     */
    public ArrayList<ItemStack> makeItemPlayerdataList() {

        ArrayList<ItemStack> playerdataButtons = new ArrayList<>();

        // Adds the separate, current data item to the first place of the list.
        if (!FileUtils.getPlayerData(this.player, this.levelName).isEmpty())

            playerdataButtons.add(PagedGUI.createItemPlaceholder(Material.WRITTEN_BOOK, "§eCurrent Data",
                    Collections.singletonList("§9Click to view the saved data for this instance."), (short) 0));

        // Adds the backups to the list
        for (BackupHolder backup : FileUtils.getPlayerDataBackups(this.player, this.levelName))
            playerdataButtons.add(PagedGUI.createItemPlaceholder(Material.PAPER, "§e" + FileUtils.formatToReadable(backup.getSaveDate()),
                    Collections.singletonList("§9Click to view the saved data for this instance."), (short) 0));

        return playerdataButtons;

    }

    /**
     * Registers all event listeners used by an instance of this GUI.
     */
    private void registerListeners() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(AOSPlayerManager.PLUGIN_NAME);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
}

