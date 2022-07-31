package com.mrkelpy.aosplayermanager.listeners;

import com.mrkelpy.aosplayermanager.common.LevelSetConfiguration;
import com.mrkelpy.aosplayermanager.util.FileUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;

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
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        if (command.getName().equalsIgnoreCase("levellist"))
            return levelListCommand(commandSender);

        return true;
    }

    /**
     * This command will display a list of all the managed levels in the server, taking into
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
        commandSender.sendMessage(String.format("§6Showing list of managed worlds (%s):", FileUtils.getManagedWorlds().size()));
        commandSender.sendMessage(finalMessage.toString());
        return true;
    }

}

