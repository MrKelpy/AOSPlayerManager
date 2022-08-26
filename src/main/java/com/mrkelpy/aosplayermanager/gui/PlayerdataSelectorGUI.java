package com.mrkelpy.aosplayermanager.gui;

import com.mrkelpy.aosplayermanager.AOSPlayerManager;
import com.mrkelpy.aosplayermanager.common.BackupHolder;
import com.mrkelpy.aosplayermanager.util.FileUtils;
import com.mrkelpy.aosplayermanager.util.GUIUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class creates a GUI meant to browse through one of the managed worlds, displaying
 * all the different playerdata backups, including the currently saved data. This GUI is player and world-bound.
 */
public class PlayerdataSelectorGUI<T extends OfflinePlayer> extends PagedGUI {

    private final T player;
    private final Player sender;
    private final String levelName;

    /**
     * Main constructor for the PlayerdataSelectorGUI.
     */
    public PlayerdataSelectorGUI(T player, Player sender, String worldName) {
        super("Backups for " + player.getName(), 27);
        this.player = player;
        this.sender = sender;
        this.levelName = worldName;
        this.setItems(this.makeItemPlayerdataList(28));
        this.reload();
        this.registerListeners();
    }

    /**
     * Handles the selection of a playerdata backup in the GUI.
     * @param event InventoryClickEvent
     */
    @Override
    @EventHandler
    public void onItemClick(InventoryClickEvent event) {
        super.onItemClick(event);

        // Gets the correct backup for the item clicked by comparing the backups' formatted saved date with the item's name
        // And opens the PlayerdataVisualizationGUI for the selected backup
        if (event.getCurrentItem().getType() == Material.PAPER) {
            int firstItemIndex = (this.getPage() - 1) * (this.storageSlots + 1);
            int lastItemIndex = firstItemIndex + this.storageSlots;

            List<BackupHolder> backups = FileUtils.getPlayerDataBackups(this.player, this.levelName, firstItemIndex, lastItemIndex).stream()
                    .filter(b -> FileUtils.formatToReadable(b.getSaveDate()).equals(event.getCurrentItem().getItemMeta().getDisplayName().substring(4)))
                    .collect(Collectors.toList());

            if (backups.isEmpty()) return;

            new PlayerdataVisualizationGUI<>(this.player, this.sender, this.levelName, backups.get(0)).openInventory();
            return;
        }

        // Creates a BackupHolder for the currently saved playerdata and opens the PlayerdataVisualizationGUI for it
        if (event.getCurrentItem().getType() == Material.WRITTEN_BOOK) {
            BackupHolder backup = new BackupHolder(FileUtils.getPlayerData(this.player, this.levelName), null);
            new PlayerdataVisualizationGUI<>(this.player, this.sender, this.levelName, backup).openInventory();
        }
    }

    /**
     * Opens this inventory for the bound player.
     */
    public void openInventory() {
        this.sender.openInventory(this.inventory);
    }

    /**
     * Goes back to the previous GUI, the playerdata level selector GUI for the current player.
     */
    @Override
    protected void goBack() {
        new PlayerdataLevelSelectorGUI<>(this.player, this.sender).openInventory();
    }

    @Override
    protected void sendToPage(int page) {
        int firstItemIndex = (page - 1) * (this.storageSlots + 1);
        int lastItemIndex = firstItemIndex + this.storageSlots;
        this.addToPlayerdataList(firstItemIndex, lastItemIndex);
        super.sendToPage(page);
    }

    /**
     * Creates a list of ItemStacks, representing the playerdata backups for the bound world, mostly as paper ItemStacks.
     * The current data is represented as a written book.
     * @param limit Index of the backup gathering limit
     * @return ArrayList(ItemStack)
     */
    @SuppressWarnings("SameParameterValue")
    private ArrayList<ItemStack> makeItemPlayerdataList(int limit) {

        ArrayList<ItemStack> playerdataButtons = new ArrayList<>();

        // Adds the separate, current data item to the first place of the list.
        playerdataButtons.add(GUIUtils.createItemPlaceholder(Material.WRITTEN_BOOK, "§eCurrent Data",
                Collections.singletonList("§9Click to view the saved data for this instance."), (short) 0));

        // Adds the backups to the list
        for (BackupHolder backup : FileUtils.getPlayerDataBackups(this.player, this.levelName, 0, limit))
            playerdataButtons.add(GUIUtils.createItemPlaceholder(Material.PAPER, "§e" + FileUtils.formatToReadable(backup.getSaveDate()),
                    Collections.singletonList("§9Click to view the saved data for this instance."), (short) 0));

        return playerdataButtons;
    }

    /**
     * Adds a certain number of playerdata item placeholders to the items list.
     * @param begin The index of the first item to add
     * @param end The index of the last item to add
     */
    private void addToPlayerdataList(int begin, int end) {

        List<ItemStack> itemList = this.getItems();

        for (BackupHolder backup : FileUtils.getPlayerDataBackups(this.player, this.levelName, begin, end)) {

            // Check if the backup item has already been added to the list, if so, skip it.
            String filename = FileUtils.formatToReadable(backup.getSaveDate());
            if (itemList.stream().anyMatch(i -> i.getItemMeta().getDisplayName().substring(4).equals(filename))) continue;

            // Add the backup item to the list
            itemList.add(GUIUtils.createItemPlaceholder(Material.PAPER, "§e" + filename,
                    Collections.singletonList("§9Click to view the saved data for this instance."), (short) 0));
        }

        this.setItems(itemList);
    }

    /**
     * Registers all event listeners used by an instance of this GUI.
     */
    private void registerListeners() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(AOSPlayerManager.PLUGIN_NAME);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
}

