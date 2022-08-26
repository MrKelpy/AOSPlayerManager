package com.mrkelpy.aosplayermanager.gui;

import com.mrkelpy.aosplayermanager.AOSPlayerManager;
import com.mrkelpy.aosplayermanager.util.GUIUtils;
import jdk.nashorn.internal.objects.annotations.Getter;
import jdk.nashorn.internal.objects.annotations.Setter;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * This abstract class handles the pagination for any GUI that extends it.
 */
public abstract class PagedGUI implements Listener {

    // This inventory instance should be used by any classes inheriting from the class.
    protected final Inventory inventory;
    protected final int storageSlots;
    private List<ItemStack> itemList;
    private int page;

    /**
     * Main constructor for the PagedGUI. Create the inventory and set the items inside it.
     * @param inventoryName The title of the inventory.
     * @param inventorySize The base inventory size, where items can be stored.
     */
    public PagedGUI(String inventoryName, int inventorySize, List<ItemStack> items) {
        this(inventoryName, inventorySize);
        this.itemList = items;
        this.sendToPage(this.page);
        this.registerListeners();
    }

    /**
     * This constructor allows one to set the item list manually after the GUI has been created.
     * @param inventoryName The title of the inventory.
     * @param inventorySize The base inventory size, where items can be stored.
     */
    public PagedGUI(String inventoryName, int inventorySize) {
        this.inventory = Bukkit.createInventory(null, inventorySize+9, inventoryName);
        this.storageSlots = inventorySize - 1;
        this.page = 1;
        this.registerListeners();
    }

    /**
     * Reloads the inventory, re-sending the GUI to the current page.
     */
    public void reload() {
        this.sendToPage(this.page);
    }

    @Getter
    public List<ItemStack> getItems() {
    	return this.itemList;
    }

    @Setter
    public void setItems(List<ItemStack> itemList) {
    	this.itemList = itemList;
    }

    /**
     * Prevents an item from being clicked on, to prevent exploits, and checks if the slot clicked
     * was any of the paging button ones. If so, handle them accordingly.
     * <br>
     * This method can, and should be overriden to process clicks on GUIs that extend PagedGUI, but the super
     * should be called first.
     * @param event InventoryClickEvent
     */
    @EventHandler
    public void onItemClick(InventoryClickEvent event) {
        if (event.getClickedInventory().equals(this.inventory)) event.setCancelled(true);
        else return;

        if (event.getSlot() == this.storageSlots + 9) this.sendToPage(this.page + 1);
        if (event.getSlot() == this.storageSlots + 1) this.sendToPage(this.page - 1);
        if (event.getSlot() == this.storageSlots + 5) this.goBack();
    }

    /**
     * Prevents an item from being dragged on, to prevent exploits.
     * @param event InventoryDragEvent
     */
    @EventHandler
    public void onItemDrag(InventoryDragEvent event) {
        if (event.getInventory().equals(this.inventory)) event.setCancelled(true);
    }

    /**
     * Unregisters all event listeners present in an instance of this GUI to save resources.
     * @param event InventoryCloseEvent
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(this.inventory))
            HandlerList.unregisterAll(this);
    }

    /**
     * This method sends a GUI to a specific page in the PagedGUI.
     * @param page The page to send the GUI to.
     */
    protected void sendToPage(int page) {

        if (page <= 0) return; // Paging starts at 1.

        // These indexes represent the first and last items' location from inside the GUI page to the itemlist.
        int firstItemIndex = (page - 1) * (this.storageSlots + 1);  // There's a slight offset equal to the page for the first index per page, so fix it.
        int lastItemIndex = firstItemIndex + this.storageSlots;

        // Ignores the sendToPage call if list of items doesn't account for this many pages.
        if (this.itemList.size() - 1 < firstItemIndex && !this.itemList.isEmpty()) return;

        // Creates a list of items to display on the page, starting from the first item and ending at the last item indexes on the item list.
        List<ItemStack> itemsForPage = new ArrayList<>();
        for (int i = firstItemIndex; i <= lastItemIndex; i++) {
            if (i - firstItemIndex >= this.storageSlots + 1 || this.itemList.size() <= i) break;
            itemsForPage.add(this.itemList.get(i));
        }

        this.page = !this.itemList.isEmpty() ? page : 1;
        this.setPageItems(itemsForPage);
        this.setPagingButtons();
    }

    /**
     * This method is called when the "Go Back" button is pressed.
     */
    protected abstract void goBack();

    /**
     * @return The current page of the PagedGUI.
     */
    @Getter
    protected int getPage() { return this.page; }

    /**
     * Evaluates whether there is a previous page in the GUI or not.
     * @return boolean
     */
    private boolean hasPreviousPage() {
        return page > 1;
    }

    /**
     * Evaluates whether there is a next page in the GUI or not.
     * @return boolean
     */
    private boolean hasNextPage() {
        return this.itemList.size() > (this.page) * (this.storageSlots + 1);
    }

    /**
     * Sets the next/previous page button items in the GUI. The items set for the buttons
     * will depend on whether there is a previous or next page.
     * <br>
     * <ul>
     *     <li>GREEN Dye - There is a next/previous page.</li>
     *     <li>RED Dye - There's no next/previous page.</li>
     * </ul>
     */
    @SuppressWarnings("deprecation")
    private void setPagingButtons() {
        this.inventory.setItem(this.storageSlots + 1,
                GUIUtils.createItemPlaceholder(Material.INK_SACK, "§bPrevious Page", null,
                        this.hasPreviousPage() ? DyeColor.PURPLE.getData() : DyeColor.SILVER.getData()));

        this.inventory.setItem(this.storageSlots + 5,
                GUIUtils.createItemPlaceholder(Material.INK_SACK, "§bGo Back", null, DyeColor.RED.getData()));

        this.inventory.setItem(this.storageSlots + 9,
                GUIUtils.createItemPlaceholder(Material.INK_SACK, "§bNext Page", null,
                        this.hasNextPage() ? DyeColor.PURPLE.getData() : DyeColor.SILVER.getData()));
    }

    /**
     * Sets the items in the given list of ItemStacks into the inventory page.
     * @param items The list of items to be set
     */
    private void setPageItems(List<ItemStack> items) {

        this.inventory.setContents(new ItemStack[this.inventory.getSize()]);  // Clears the inventory.

        for (int i = 0; i < items.size(); i++) {
            this.inventory.setItem(i, items.get(i));
        }
    }

    /**
     * Registers all event listeners used by an instance of this GUI.
     */
    private void registerListeners() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(AOSPlayerManager.PLUGIN_NAME);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
}

