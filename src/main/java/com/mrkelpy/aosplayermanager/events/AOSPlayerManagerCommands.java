package com.mrkelpy.aosplayermanager.events;

import com.mrkelpy.aosplayermanager.configuration.LevelSetConfiguration;
import com.mrkelpy.aosplayermanager.gui.PlayerdataLevelSelectorGUI;
import com.mrkelpy.aosplayermanager.util.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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

        if (command.getName().equalsIgnoreCase("levellist"))
            return levelListCommand(commandSender);

        if (command.getName().equalsIgnoreCase("checkdata"))
            return checkDataCommand(commandSender, args);

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
        StringBuilder finalMessage = new StringBuilder();

        // Formats every LevelSet into a string and adds it to the final message
        for (int i = 0; i < levelSets.size(); i++) {

            if (i != 0) finalMessage.append(", ");
            finalMessage.append("Set").append(i).append("(").append(String.join(", ", levelSets.get(i))).append(")");
            levelSets.get(i).forEach(managedWorlds::remove);
        }

        // Adds the rest of the unsynced worlds to the final message, if there's any more.
        if (managedWorlds.size() != 0 && levelSets.size() != 0)
            finalMessage.append(", ");

        finalMessage.append(String.join(", ", managedWorlds));
        commandSender.sendMessage(String.format("§f----- §aManaged Worlds (%s) §f-----", FileUtils.getManagedWorlds().size()));
        commandSender.sendMessage(finalMessage.toString());
        return true;
    }

    /**
     * Displays the playerdata information for a player.
     * @param commandSender The sender of the command
     * @param args The arguments of the command
     * @return Boolean, feedback to the caller
     */
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

        // PlayerDataHolder latestData = FileUtils.getPlayerData(playerSender, playerSender.getWorld().getName());
        new PlayerdataLevelSelectorGUI(playerSender).openInventory();

        return true;
    }

}

