package com.mrkelpy.aosplayermanager.events;

import com.mrkelpy.aosplayermanager.AOSPlayerManager;
import com.mrkelpy.aosplayermanager.configuration.AOSPlayerManagerConfig;
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

import java.util.*;

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

        if (command.getName().equalsIgnoreCase("apm")) {
            return parseCommands(commandSender, args);
        }

        return true;
    }

    /**
     * Displays a list of available command for usage.
     * @param commandSender The sender of the command
     * @return Boolean, feedback to the caller
     */
    private boolean helpCommand(CommandSender commandSender) {

        commandSender.sendMessage(String.format("§f----- §a%s Command List§f-----", AOSPlayerManager.PLUGIN_NAME));
        commandSender.sendMessage("§b> §f/apm levellist §7-> Displays a list of all managed levels in the server.");
        commandSender.sendMessage("§b> §f/apm savedata [(Optional) target player] §7-> Saves the playerdata for a player or all players if unspecified.");
        commandSender.sendMessage("§b> §f/apm checkdata [target player] §7-> Checks the playerdata for a player, allowing the operator to restore or clone it.");
        commandSender.sendMessage("§b> §f/apm reload §7-> Reloads the plugin.");
        commandSender.sendMessage("§b> §f/apm help §7-> Displays this menu.");
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

        // Checks if the player specified in the arguments is online or has ever played on the server.
        if (Bukkit.getServer().getOnlinePlayers().stream().noneMatch(player -> Objects.equals(player.getName(), args[0])) && !Bukkit.getOfflinePlayer(args[0]).hasPlayedBefore()) {
            commandSender.sendMessage("§cThat player cannot be found");
            return true;
        }

        new PlayerdataLevelSelectorGUI<>(Bukkit.getPlayer(args[0]) != null ? Bukkit.getPlayer(args[0]) : Bukkit.getOfflinePlayer(args[0]), playerSender)
                .openInventory();

        return true;
    }

    /**
     * Saves the data for a player if specified, otherwise, save the data for all players.
     * @param commandSender The sender of the command
     * @param args The command arguments
     * @return Boolean, feedback to the caller
     */
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
     * Reloads the plugin configs and saves the players' data.
     * @param commandSender The sender of the command
     * @return Boolean, feedback to the caller
     */
    private boolean reloadCommand(CommandSender commandSender) {

        saveDataCommand(commandSender, new String[]{});
        AOSPlayerManagerConfig.reload();
        LevelSetConfiguration.reload();
        commandSender.sendMessage("§eReloaded " + AOSPlayerManager.PLUGIN_NAME);
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

    /**
     * Since the base command is /apm, this method takes in the first argument after that prefix and parses the command
     * normally from there. This serves to prevent the command from being used by other plugins, and to have a "command space"
     * for the plugin.
     * The "args" array will also be modified, removing the first element, because that's the command to be called.
     * @param commandSender The sender of the command
     * @param args The arguments of the command
     * @return Boolean, feedback to the caller
     */
    private boolean parseCommands(CommandSender commandSender, String[] args) {

        // Using just /apm shows the help menu
        if (args.length == 0) return helpCommand(commandSender);

        // Process the arguments and remove the first element
        String command = args[0];
        List<String> argumentProcessing = new LinkedList<>(Arrays.asList(args));
        argumentProcessing.remove(0);
        args = Arrays.copyOf(argumentProcessing.toArray(), args.length - 1, String[].class);

        // Check which command was meant to be fired given the command argument, and fire it
        if (command.equalsIgnoreCase("levellist")) {
            if (!this.checkPermission("aos.levellist", commandSender)) return true;
            return levelListCommand(commandSender);
        }

        if (command.equalsIgnoreCase("checkdata")) {
            if (!this.checkPermission("aos.checkdata", commandSender)) return true;
            return checkDataCommand(commandSender, args);
        }

        if (command.equalsIgnoreCase("savedata")) {
            if (!this.checkPermission("aos.savedata", commandSender)) return true;
            return saveDataCommand(commandSender, args);
        }

        if (command.equalsIgnoreCase("help")) {
            if (!this.checkPermission("aos.help", commandSender)) return true;
            return helpCommand(commandSender);
        }

        if (command.equalsIgnoreCase("reload")) {
            if (!this.checkPermission("aos.reload", commandSender)) return true;
            return reloadCommand(commandSender);
        }

        commandSender.sendMessage("§cUnknown command. Use /apm help for a list of available commands");
        return true;

}

}

