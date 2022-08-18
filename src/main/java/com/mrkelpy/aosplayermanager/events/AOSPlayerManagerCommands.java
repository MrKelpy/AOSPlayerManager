package com.mrkelpy.aosplayermanager.events;

import com.mrkelpy.aosplayermanager.configuration.LevelSetConfiguration;
import com.mrkelpy.aosplayermanager.gui.PlayerdataLevelSelectorGUI;
import com.mrkelpy.aosplayermanager.util.EventUtils;
import com.mrkelpy.aosplayermanager.util.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.world.WorldSaveEvent;

import java.util.ArrayList;
import java.util.Objects;

/**
 * This class handles all the custom commands added by the plugin
 */
public class AOSPlayerManagerCommands implements CommandExecutor {

    public static final AOSPlayerManagerCommands INSTANCE = new AOSPlayerManagerCommands();

    /**
     * Listen for commands and call the necessary methods to run them
     *
     * @return boolean, feedback to the caller
     */
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {

        if (command.getName().equalsIgnoreCase("levellist")) {
            if (!this.checkPermission("aos.levellist", commandSender)) return true;
            return levelListCommand(commandSender);
        }

        if (command.getName().equalsIgnoreCase("checkdata")) {
            if (!this.checkPermission("aos.checkdata", commandSender)) return true;
            return checkDataCommand(commandSender, args);
        }

        if (command.getName().equalsIgnoreCase("savedata")) {
            if (!this.checkPermission("aos.savedata", commandSender)) return true;
            return saveDataCommand(commandSender, args);
        }

        return true;
    }

    /**
     * Displays a list of all the managed levels in the server, taking into
     * account the Set context.
     *
     * @param commandSender The sender of the command
     * @return Boolean, feedback to the caller
     */
    private boolean levelListCommand(CommandSender commandSender) {

        ArrayList<String> managedWorlds = FileUtils.getManagedWorlds();
        ArrayList<ArrayList<String>> levelSets = LevelSetConfiguration.getLevelSets();
        commandSender.sendMessage(String.format("§f----- §aManaged Worlds (%s) §f-----", FileUtils.getManagedWorlds().size()));

        // Sends every separate level set to the player as a string
        for (int i = 0; i < levelSets.size(); i++) {

            commandSender.sendMessage("§b> " + "§eSet " + i + " -> §f" + String.join(", ", levelSets.get(i)));
            levelSets.get(i).forEach(managedWorlds::remove);
        }

        // Sends the rest of the managed worlds to the player as a string if there's any
        for (String managedWorld : managedWorlds) {
            commandSender.sendMessage("§b> §f" + managedWorld);
        }

        return true;
    }

    /**
     * Displays the playerdata information for a player.
     * @param commandSender The sender of the command
     * @param args The arguments of the command
     * @return Boolean, feedback to the caller
     */
    @SuppressWarnings("deprecation")
    private boolean checkDataCommand(CommandSender commandSender, String[] args) {

        // Ensure that this command can only be used by a player.
        if (!(commandSender instanceof Player)) return false;
        Player playerSender = (Player) commandSender;

        // Sends the command usage to the player in case they misuse it.
        if (args == null || args.length != 1) {
            commandSender.sendMessage("§c/checkdata [target player]");
            return true;
        }

        // Checks if the player specified in the arguments is online.
        if (Bukkit.getServer().getOnlinePlayers().stream().noneMatch(player -> Objects.equals(player.getName(), args[0]))) {
            commandSender.sendMessage("§cThat player cannot be found");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        new PlayerdataLevelSelectorGUI(target, playerSender).openInventory();

        return true;
    }

    private boolean saveDataCommand(CommandSender commandSender, String[] args) {

        // Ensure that this command can only be used by a player.
        if (!(commandSender instanceof Player)) return false;
        Player playerSender = (Player) commandSender;

        if (args.length == 0) {
            Bukkit.getPluginManager().callEvent(new WorldSaveEvent(playerSender.getWorld()));
            commandSender.sendMessage("§aSaved data for all players");
            return true;
        }

        // Checks if the player specified in the arguments is online.
        if (Bukkit.getServer().getOnlinePlayers().stream().noneMatch(player -> Objects.equals(player.getName(), args[0]))) {
            commandSender.sendMessage("§cThat player cannot be found");
            return true;
        }

        EventUtils.eventPlayerdataSave(playerSender, playerSender.getWorld().getName());
        EventUtils.eventPlayerdataBackup(playerSender, playerSender.getWorld().getName());
        commandSender.sendMessage("§aSaved data for " + args[0] + "");
        return true;

    }

    /**
     * Checks if a player has permission to use a command. If not, send a message to the player telling
     * them they do not have permission.
     * @param permission The permission to check for
     * @param sender The sender to check for the permission
     * @return Whether the player has permission or not
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean checkPermission(String permission, CommandSender sender) {
        if (sender.hasPermission("aos.all") || sender.isOp() || sender.hasPermission(permission))
            return true;

        sender.sendMessage("§cYou do not have permission to use this command");
        return false;
    }

}

