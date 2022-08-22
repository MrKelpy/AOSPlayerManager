package com.mrkelpy.aosplayermanager.gui;

import com.mrkelpy.aosplayermanager.AOSPlayerManager;
import com.mrkelpy.aosplayermanager.common.BackupHolder;
import com.mrkelpy.aosplayermanager.common.PlayerDataHolder;
import com.mrkelpy.aosplayermanager.common.SimplePotionEffect;
import com.mrkelpy.aosplayermanager.util.EventUtils;
import com.mrkelpy.aosplayermanager.util.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * This class creates a GUI showing all the items existent in a player's inventory, alongside
 * the armour and a placeholder showing all the other stats. There is also be a restore button
 * to set the instance as the current data.
 * <br>
 * This GUI is player, world, and playerdata-bound.
 */
public class PlayerdataVisualizationGUI<T extends OfflinePlayer> extends PagedGUI {

    private final T player;
    private final Player sender;
    private final String levelName;
    private final BackupHolder playerdata;

    /**
     * Main constructor for the PlayerdataVisualizationGUI.
     */
    public PlayerdataVisualizationGUI(T player, Player sender, String worldName, BackupHolder playerdata) {
        super(playerdata.getSaveDate() != null ? FileUtils.formatToReadable(playerdata.getSaveDate()) : "Current Data", 45);
        this.player = player;
        this.sender = sender;
        this.levelName = worldName;
        this.playerdata = playerdata;
        this.setItems(this.makeItemList());
        this.reload();
        this.registerListeners();
    }

    /**
     * Handles the selection of the "Restore" button.
     * @param event InventoryClickEvent
     */
    @Override
    @EventHandler
    public void onItemClick(InventoryClickEvent event) {
        super.onItemClick(event);

        // If the restore button is clicked, clone the data for the player.
        if (event.getCurrentItem().getType() == Material.REDSTONE_COMPARATOR)
            this.cloneInstance((Player) this.player);

        // If the clone button is clicked, clone the data for the sender.
        if (event.getCurrentItem().getType() == Material.REDSTONE_TORCH_ON)
            this.cloneInstance(this.sender);
    }

    /**
     * Opens this inventory for the bound player.
     */
    public void openInventory() {
        this.sender.openInventory(this.inventory);
    }

    /**
     * Goes back to the previous GUI, the playerdata selector GUI for the current player and world.
     */
    @Override
    protected void goBack() {
        new PlayerdataSelectorGUI<>(this.player, this.sender, this.levelName).openInventory();
    }

    /**
     * Sets the saved inventory items in the first 36 slots of the inventory, and
     * the armour + restore and stats placeholders in the last 9 slots.
     * @return ArrayList(ItemStack)
     */
    private ArrayList<ItemStack> makeItemList() {
        ArrayList<ItemStack> items = new ArrayList<>();
        PlayerDataHolder data = this.playerdata.getPlayerdata();

        Collections.addAll(items, data.getPlayerInventory());

        Collections.reverse(Arrays.asList(data.getPlayerArmour()));
        Collections.addAll(items, data.getPlayerArmour());
        for (int i = 0; i < 2; i++) items.add(null);

        items.add(PagedGUI.createItemPlaceholder(Material.SKULL_ITEM, "§a" + this.player.getName(),
                Arrays.asList(
                        "§eWorld: §f" + this.levelName,
                        "§eCoordinates: §f" + (data.getPlayerCoordinates() != null ? data.getPlayerCoordinates().toString() : "Unsaved"),
                        "§eHealth: §f" + data.getPlayerHealth(),
                        "§eFood: §f" + data.getPlayerHunger(),
                        "§eExperience: §f" + data.getPlayerExperienceLevels() + " Levels, " + data.getPlayerExperiencePoints() + " Points",
                        "§ePotion Effects: §f" + (data.getPlayerPotionEffects() != null
                                ? data.getPlayerPotionEffects().stream().map(SimplePotionEffect::toString).collect(Collectors.joining(", "))
                                : "None")
                        ),
                (short) 3));

        items.add(PagedGUI.createItemPlaceholder(Material.REDSTONE_TORCH_ON, "§eClone",
                Collections.singletonList("§a(Clones the instance to the operator, backing up their data first)"),
                (short) 0));

        // If the player is currently online, add the restore button.
        if (!player.isOnline()) {
            items.add(PagedGUI.createItemPlaceholder(Material.IRON_FENCE, "§cPlayer is Offline"));
            return items;
        }

        items.add(PagedGUI.createItemPlaceholder(Material.REDSTONE_COMPARATOR, "§eRestore",
                Collections.singletonList("§a(Automatically creates a backup before restoring)"),
                (short) 0));

        return items;
    }

    /**
     * Clones the selected Playerdata instance to the target, backing up their current data first.
     * @param target The player to clone the data to.
     */
    private void cloneInstance(Player target) {

        Collections.reverse(Arrays.asList(this.playerdata.getPlayerdata().getPlayerArmour()));
        EventUtils.eventPlayerdataBackup(target, target.getWorld().getName());

        this.playerdata.getPlayerdata().setPlayerCoordinates(null);
        this.playerdata.getPlayerdata().applyTo(target, Bukkit.getWorld(this.levelName));

        EventUtils.eventPlayerdataSave(target, target.getWorld().getName());
    }

    /**
     * Registers all event listeners used by an instance of this GUI.
     */
    private void registerListeners() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(AOSPlayerManager.PLUGIN_NAME);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
}

